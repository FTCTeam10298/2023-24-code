package us.brainstormz.robotTwo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.AprilTagLocalizerRepackaged
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.localizer.aprilTagLocalization.AprilTagPipelineForEachCamera
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.RobotTwoPropDetector.PropPosition
import us.brainstormz.robotTwo.RobotTwoPropDetector.*
import us.brainstormz.robotTwo.RobotTwoTeleOp.*
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initLatchTarget
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initialPreviousTargetState
import us.brainstormz.robotTwo.localTests.TeleopTest
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Extendo.ExtendoPositions
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels.*
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.utils.measured
import java.lang.Exception
import kotlin.math.absoluteValue
import kotlin.math.sign

fun Any.printPretty() = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
fun ActualWorld.withoutLights() = this.copy(actualRobot=this.actualRobot.copy(neopixelState = StripState(true, emptyList()))).printPretty()
fun TargetWorld.withoutLights() = this.copy(targetRobot=this.targetRobot.copy(lights = RobotTwoTeleOp.LightTarget(stripTarget = StripState(true, emptyList()))))
fun TargetWorld.withoutGetNext() = this.copy(autoInput=this.autoInput?.copy(getNextInput = null))


class RobotTwoAuto(
    private val telemetry: Telemetry,
): RobotTwo(telemetry) {

    private val blankAutoState = AutoInput(
            drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation()),
            handoffInput = HandoffTarget(
                    armPosition= Arm.Positions.AutoInitPosition,
                    depoInput = DepoInput.Down,
                    wristTargets= Wrist.WristTargets(Claw.ClawTarget.Gripping)
            ),
            extendoInput = ExtendoPositions.Min,
            intakeInput = Intake.CollectorPowers.Off,
            latchOverride = Transfer.TransferTarget(Transfer.LatchTarget(Transfer.LatchPositions.Closed, 0L)),
            dropdownPosition = Dropdown.DropdownPresets.Init,
            getNextInput = { actualWorld, previousActualWorld, previousTargetWorld -> getNextTargetFromList(previousAutoInput= previousTargetWorld.autoInput!!) },
    )

    data class HandoffTarget(
            val armPosition: Arm.Positions,
            val depoInput: DepoInput,
            val wristTargets: Wrist.WristTargets,

            val handoffInput: HandoffInput,

            val handoffManagerOn: Boolean
    ) {
        constructor(handoffInput: HandoffInput,
                    wristTargets: Wrist.WristTargets,
                    depoInput: DepoInput): this(
                armPosition = Arm.Positions.Out,
                depoInput = depoInput,
                wristTargets = wristTargets,
                handoffInput = handoffInput,
                handoffManagerOn = true
        )
        constructor(armPosition: Arm.Positions,
                    depoInput: DepoInput,
                    wristTargets: Wrist.WristTargets): this(
                armPosition = armPosition,
                depoInput = depoInput,
                wristTargets = wristTargets,
                handoffInput = HandoffInput.NoInput,
                handoffManagerOn = false
        )
    }

    fun getTargetWorldFromAutoInput(
            autoInput: AutoInput,
            actualWorld: ActualWorld,
            aprilTagReadings: List<AprilTagDetection>,
            previousActualWorld: ActualWorld,
            previousTargetWorld: TargetWorld
    ): TargetWorld {

        /**Interpret Transfer State*/
        val transferState = transfer.getTransferState(
                actualWorld = actualWorld,
                previousTransferState = previousTargetWorld.targetRobot.collectorTarget.transferSensorState,
        )

        val transferLeftSensorState = transfer.checkIfPixelIsTransferred(transferState.left)
        val transferRightSensorState = transfer.checkIfPixelIsTransferred(transferState.right)
        val areBothPixelsIn = transferLeftSensorState && transferRightSensorState

        val previousTransferLeftSensorState = transfer.checkIfPixelIsTransferred(previousTargetWorld.targetRobot.collectorTarget.transferSensorState.left)
        val previousTransferRightSensorState = transfer.checkIfPixelIsTransferred(previousTargetWorld.targetRobot.collectorTarget.transferSensorState.right)
        val wereBothPixelsInPreviously = previousTransferLeftSensorState && previousTransferRightSensorState

        val theRobotJustCollectedTwoPixels = areBothPixelsIn && !wereBothPixelsInPreviously


        /**Collector*/
        val timeSincePixelsTransferredMillis: Long = actualWorld.timestampMilis - (previousTargetWorld.targetRobot.collectorTarget.timeOfTransferredMillis?:actualWorld.timestampMilis)
        val waitBeforeEjectingMillis = 200
        val timeToStartEjection = theRobotJustCollectedTwoPixels && (timeSincePixelsTransferredMillis > waitBeforeEjectingMillis)

        val timeSinceEjectionStartedMillis: Long = actualWorld.timestampMilis - (previousTargetWorld.targetRobot.collectorTarget.timeOfEjectionStartMilis?:actualWorld.timestampMilis)
        val ejectionTimeMillis = 1000
        val timeToStopEjecting = timeSinceEjectionStartedMillis > ejectionTimeMillis

        val wasPreviouslyEjecting = previousTargetWorld.targetRobot.collectorTarget.intakeNoodles == Intake.CollectorPowers.Eject
        val stopAutomaticEjection = timeToStopEjecting && wasPreviouslyEjecting// && doHandoffSequence


        val timeOfEjectionStartMillis = if (timeToStartEjection) {
            actualWorld.timestampMilis
        } else if (stopAutomaticEjection) {
            null
        } else {
            previousTargetWorld.targetRobot.collectorTarget.timeOfEjectionStartMilis
        }
        val timeOfTransferredMillis = if (theRobotJustCollectedTwoPixels) {
            actualWorld.timestampMilis
        } else if (stopAutomaticEjection) {
            null
        } else {
            previousTargetWorld.targetRobot.collectorTarget.timeOfTransferredMillis
        }

        val intakeNoodleTarget = autoInput.intakeInput

//        /**Dropdown*/
        val dropdownIsInInitialPosition = previousTargetWorld.targetRobot.collectorTarget.dropDown.targetPosition == Dropdown.DropdownPresets.Init
        val extendoIsFarEnoughOutForDropdownToDropDown = actualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks >= ExtendoPositions.InPastBatteryBox.ticks
        val dropdownTarget = autoInput.dropdownPosition
//        if (dropdownIsInInitialPosition && !extendoIsFarEnoughOutForDropdownToDropDown) {
//            Dropdown.DropdownPresets.Init
//        } else {
//            autoInput.dropdownPosition
//        }

        val uncoordinatedCollectorTarget = CollectorTarget(
                intakeNoodles = intakeNoodleTarget,
                dropDown = Dropdown.DropdownTarget(dropdownTarget),
                timeOfEjectionStartMilis = timeOfEjectionStartMillis,
                timeOfTransferredMillis = timeOfTransferredMillis,
                transferSensorState = transferState,
                latches = Transfer.TransferTarget(initLatchTarget, initLatchTarget),
                extendo = Extendo.ExtendoTarget(autoInput.extendoInput),
        )

        /**Handoff*/
        val ifHanodffManagerOn = autoInput.handoffInput.handoffManagerOn
        val handoffTarget = if (ifHanodffManagerOn) {
            val doingHandoff = autoInput.handoffInput.handoffInput == HandoffInput.Handoff

            val repeatDriverInputForDepo = when {
                autoInput.handoffInput.depoInput == DepoInput.NoInput -> {
                    DepoInput.Down
                }

                else -> autoInput.handoffInput.depoInput
            }

            val extendoDriverInput = when (autoInput.extendoInput) {
                ExtendoPositions.Min -> ExtendoInput.RetractManual
                else -> ExtendoInput.ExtendManual
            }

            fun wristTargetsToWristInput(wristTargets: Wrist.WristTargets): WristInput {
                fun clawTargetToClawInput(clawTarget: Claw.ClawTarget): ClawInput {
                    return when (clawTarget) {
                        Claw.ClawTarget.Gripping -> ClawInput.NoInput
                        Claw.ClawTarget.Retracted -> ClawInput.Drop
                    }
                }

                return WristInput(
                        left = clawTargetToClawInput(wristTargets.left),
                        right = clawTargetToClawInput(wristTargets.right),
                )
            }

            telemetry.addLine("\n\nextendoDriverInput: $extendoDriverInput")

            val fromHandoffManager = handoffManager.manageHandoff(
                    wristInput = wristTargetsToWristInput(autoInput.handoffInput.wristTargets),
                    depoInput = repeatDriverInputForDepo,
                    extendoInput = extendoDriverInput,
                    collectorTarget = uncoordinatedCollectorTarget,
                    previousTargetWorld = previousTargetWorld,
                    actualWorld = actualWorld,
                    doingHandoff = doingHandoff,
            )

            telemetry.addLine("fromHandoffManager: ${fromHandoffManager.collector.extendo}")

            val finidingHome = fromHandoffManager.collector.extendo.movementMode == DualMovementModeSubsystem.MovementMode.Power

            val closeToIn = actualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks < Extendo.ExtendoPositions.OutFarEnoughToCompletelyClearDepo.ticks - 10
            val goingin = fromHandoffManager.collector.extendo.targetPosition == ExtendoPositions.Min
            val limitIsntPressed = !actualWorld.actualRobot.collectorSystemState.extendo.limitSwitchIsActivated
            val goingInAndClose = closeToIn && goingin && limitIsntPressed

            val withHigherPowerExtendoIn = if (finidingHome || goingInAndClose) {
                fromHandoffManager.copy(
                        collector = fromHandoffManager.collector.copy(
                                extendo = Extendo.ExtendoTarget(
                                        power = -1.0,
                                        movementMode = DualMovementModeSubsystem.MovementMode.Power,
                                        targetPosition = ExtendoPositions.Min
                                )
                        )
                )
            } else {
                fromHandoffManager
            }

            telemetry.addLine("\n\nwithHigherPowerExtendoIn: $withHigherPowerExtendoIn")
            telemetry.addLine("fromHandoffManager: $fromHandoffManager")

            withHigherPowerExtendoIn
        } else {
            val depoTarget = DepoTarget(
                    armPosition = Arm.ArmTarget(autoInput.handoffInput.armPosition),
                    lift = Lift.TargetLift(lift.getGetLiftTargetFromDepoTarget(autoInput.handoffInput.depoInput, 0.0)),
                    wristPosition = autoInput.handoffInput.wristTargets,
                    targetType = DepoManager.DepoTargetType.GoingOut
            )

//            val collectorTarget = CollectorTarget(
//                    intakeNoodles = intakeNoodleTarget,
//                    dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Init),
//                    timeOfEjectionStartMilis = 0,
//                    timeOfTransferredMillis = 0,
//                    transferSensorState = initialPreviousTargetState.targetRobot.collectorTarget.transferSensorState,
//                    latches = initialPreviousTargetState.targetRobot.collectorTarget.latches,
//                    extendo = Extendo.ExtendoTarget(autoInput.extendoInput),
//            )

            HandoffManager.CollectorDepositorTarget(
                    collector = uncoordinatedCollectorTarget,
                    depo = depoTarget,
                    handoffCompleted = false
            )
        }

        val handoffWithLatchOverride = handoffTarget.copy(
                collector = handoffTarget.collector.copy(
                        latches = autoInput.latchOverride
                )
        )

        val drivetrainTarget = if (autoInput.getCurrentPositionAndRotationFromAprilTag) {
            Drivetrain.DrivetrainTarget(actualWorld.actualRobot.positionAndRotation)
        } else {
            autoInput.drivetrainTarget
        }

        if (autoInput.getCurrentPositionAndRotationFromAprilTag) {
            val positionAndRotationFromAprilTag = aprilTagLocalizerRepackaged.recalculatePositionAndRotation(aprilTagReadings)
            telemetry.addLine("aprilTagPosition: $positionAndRotationFromAprilTag")

            positionAndRotationFromAprilTag?.let {aprilTagPosition ->
                val delta = aprilTagPosition - actualWorld.actualRobot.positionAndRotation
                telemetry.addLine("april tag vs odom delta: ${delta}")

                saveSomething<String>("delta: $delta \naprilTagPosition: $aprilTagPosition")
                drivetrain.localizer.setPositionAndRotation(aprilTagPosition.copy(
                        r = actualWorld.actualRobot.positionAndRotation.r
                ))
            }
        }

        return TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = drivetrainTarget,
                        depoTarget = handoffTarget.depo,
                        collectorTarget = handoffWithLatchOverride.collector,
                        hangPowers = RobotTwoHardware.HangPowers.Holding,
                        launcherPosition = RobotTwoHardware.LauncherPosition.Holding,
                        lights = LightTarget(),
                ),
                doingHandoff = !handoffTarget.handoffCompleted,
                driverInput = initialPreviousTargetState.driverInput,
                autoInput = autoInput,
                timeTargetStartedMilis = if (autoInput != previousTargetWorld.autoInput) {
                    actualWorld.timestampMilis
                } else {
                    previousTargetWorld.timeTargetStartedMilis
                },
                gamepad2Rumble = GamepadRumble(0.0, 0.0)
        )
    }

    private val aprilTagLocalizerRepackaged = AprilTagLocalizerRepackaged(telemetry = telemetry)

    private val xForNavigatingUnderStageDoor = -((RobotTwoHardware.robotWidthInches/2) + 2)

    private val depositY = -52.0
    private fun centerOfDropArea(propPosition: PropPosition) = when (propPosition) {
        PropPosition.Left -> PositionAndRotation(
                x = -29.4,
                y = depositY,
                r = 0.0
        )
        PropPosition.Center -> PositionAndRotation(
                x = -35.4,
                y = depositY,
                r = 0.0
        )
        PropPosition.Right -> PositionAndRotation(
                x = -41.4,
                y = depositY,
                r = 0.0
        )
    }
    private fun depositingPosition(propPosition: PropPosition, alliance: RobotTwoHardware.Alliance): PositionAndRotation {
        val allianceMultiplier = when (alliance) {
            RobotTwoHardware.Alliance.Red -> -1
            RobotTwoHardware.Alliance.Blue -> 1
        }

        val offset = 2.0 * allianceMultiplier.sign

        return centerOfDropArea(propPosition).copy(
                x = centerOfDropArea(propPosition).x - offset
        )
    }

    private fun addOutUnderTwelve(autoInput: List<AutoInput>): List<AutoInput> = autoInput.map { it.copy(handoffInput = HandoffTarget(
            armPosition = Arm.Positions.OutButUnderTwelve,
            depoInput = DepoInput.Down,
            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping)
    ))}

    private val pushIntoBoardDrivetrainPower = -0.3
    private fun depositYellow(propPosition: PropPosition, alliance: RobotTwoHardware.Alliance, liftHeight: DepoInput): List<AutoInput> {

        return listOf(
                blankAutoState.copy(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition, alliance)),
                        handoffInput = HandoffTarget(
                                armPosition = Arm.Positions.AutoInitPosition,
                                depoInput = liftHeight,
                                wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping)
                        ),
                        getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                            telemetry.addLine("Waiting for robot to get to board position")
                            val drivetrainIsAtPosition = isRobotAtXPosition(actualWorld, targetWorld, allowedErrorXInches = 1.0)
                            val liftIsAtPosition = lift.isLiftAtPosition(Lift.LiftPositions.AutoLowYellowPlacement.ticks, actualWorld.actualRobot.depoState.lift.currentPositionTicks)

                            nextTargetFromCondition(liftIsAtPosition || drivetrainIsAtPosition || hasTimeElapsed(1000, targetWorld), targetWorld)
                        }
                ),
                blankAutoState.copy(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(Drivetrain.DrivetrainPower(y = pushIntoBoardDrivetrainPower)),
                        handoffInput = HandoffTarget(
                                armPosition = Arm.Positions.Out,
                                depoInput = liftHeight,
                                wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping)
                        ),
                        getNextInput = { actualWorld, previousActualWorld, targetWorld ->

                            val isDepoAtPosition = isDepoAtPosition(
                                    armTarget = Arm.Positions.Out,
                                    liftTarget = Lift.LiftPositions.AutoLowYellowPlacement,
                                    actualWorld = actualWorld
                            )

                            val deltaPositionInches = (actualWorld.actualRobot.positionAndRotation.y - previousActualWorld.actualRobot.positionAndRotation.y).absoluteValue
                            val deltaTimeSeconds = (actualWorld.timestampMilis - previousActualWorld.timestampMilis) * 1000
                            val velocityInchesPerSeconds = deltaPositionInches / deltaTimeSeconds
                            val allowedVelocityInchesPerSeconds = 1.0
                            val velocityIsLess = velocityInchesPerSeconds < allowedVelocityInchesPerSeconds
                            telemetry.addLine("\nbackwardVelocity: $velocityInchesPerSeconds")
                            telemetry.addLine("velocityIsLess: $velocityIsLess\n")

                            telemetry.addLine("Waiting for depo to get to scoring position ($isDepoAtPosition)")

                            nextTargetFromCondition(isDepoAtPosition || hasTimeElapsed(1500, targetWorld), targetWorld)
                        }
                ),
                blankAutoState.copy(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(Drivetrain.DrivetrainPower(y = pushIntoBoardDrivetrainPower)),
                        handoffInput = HandoffTarget(
                                armPosition = Arm.Positions.Out,
                                depoInput = liftHeight,
                                wristTargets = Wrist.WristTargets(Claw.ClawTarget.Retracted)
                        ),
                        getNextInput = { actualWorld, previousActualWorld, targetWorld ->

                            telemetry.addLine("Waiting for lift to go up and claws to close")

                            val wristIsAtPosition = wrist.wristIsAtPosition(
                                    target = Wrist.WristTargets(
                                            left = Claw.ClawTarget.Retracted,
                                            right = Claw.ClawTarget.Retracted
                                    ),
                                    actual = actualWorld.actualRobot.depoState.wristAngles
                            )
                            val liftIsAtPosition = lift.isLiftAtPosition(Lift.LiftPositions.AutoLowYellowPlacement.ticks, actualWorld.actualRobot.depoState.lift.currentPositionTicks)

                            val isTargetDone = (wristIsAtPosition && liftIsAtPosition) || hasTimeElapsed(1000, targetWorld)
                            if (isTargetDone) {
                                val atBoardPosition = actualWorld.actualRobot.positionAndRotation.y
                                targetWorld.autoInput!!.copy(
                                        drivetrainTarget = Drivetrain.DrivetrainTarget(actualWorld.actualRobot.positionAndRotation.copy(
                                                y = atBoardPosition + 6
                                        )),
                                        getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                            telemetry.addLine("Waiting for robot to back away from board")

                                            val backAwayFromBoardY = atBoardPosition + 3
                                            val isRobotInPosition = actualWorld.actualRobot.positionAndRotation.y >= backAwayFromBoardY
                                            nextTargetFromCondition(isRobotInPosition || hasTimeElapsed(1000, targetWorld), targetWorld)
                                        }
                                )
                            } else {
                                targetWorld.autoInput!!
                            }
                        }
                ),
        )
    }

    private fun depositCenterPurple(startPosition: PositionAndRotation) = listOf(
            blankAutoState.copy(
                    drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                            x = startPosition.x + 31.5,
                            y = startPosition.y - 2,
                    )),
                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                        nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                    }
            ),
            blankAutoState.copy(
                    drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                            x = startPosition.x + 20,
                            y = startPosition.y - 2,
                    )),
                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                        nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                    }
            ),
    )

    private val pushPurpleFarFromTrussX = -35.0
    private val pushPurpleCloseToTrussX = -36.0

    private var indexOfCollectionStart: Int? = null
    private fun calcAutoTargetStateList(
            alliance: RobotTwoHardware.Alliance,
            startingSide: StartPosition,
            propPosition: PropPosition,
            partnerIsPlacingYellow: Boolean,
            numberOfCycles: Int,
            waitTimeMillis: Long
    ): List<AutoInput> {

        val startPosition = startingSide.redStartPosition

        fun addWait(autoInput: AutoInput): AutoInput = autoInput.copy(
                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                    val waitIsDone = hasTimeElapsed(waitTimeMillis, targetWorld)
                    nextTargetFromCondition(waitIsDone, targetWorld)
                }
        )
        val liftHeight = when {
            partnerIsPlacingYellow -> DepoInput.AbovePartnerYellowPlacement
            else -> DepoInput.YellowPlacement
        }

        val spitIntoBackstagePosition = PositionAndRotation(
                x = startPosition.x + 3,
                y = -30.0,
                r = 180.0
        )

        val redPath: PathPreAssembled = when (startingSide) {
            StartPosition.Backboard -> {
                val cycleUnderTrussXPosition = -58.0

                val cyclePreCollectBase = blankAutoState.copy(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                x = cycleUnderTrussXPosition,
                                y = startPosition.y,
                                r = 0.0,
                        )),
                        dropdownPosition = Dropdown.DropdownPresets.Init,
                        handoffInput = HandoffTarget(
                                armPosition = Arm.Positions.OutButUnderTwelve,
                                depoInput = DepoInput.Down,
                                wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                        ),
                )

                val cycleCollectionAndPostBase = blankAutoState.copy(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                x = cycleUnderTrussXPosition,
                                y = startPosition.y,
                                r = 0.0,
                        )),
                        dropdownPosition = Dropdown.DropdownPresets.FivePixelsForAuto,
                        handoffInput = HandoffTarget(
                                armPosition = Arm.Positions.OutButUnderTwelve,
                                depoInput = DepoInput.Down,
                                wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                        ),
                )

                PathPreAssembled(
                        purplePlacementPath = { propPosition ->
                            val propPlacement = when (propPosition) {
                                PropPosition.Left -> listOf(
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleCloseToTrussX - 2,
                                                        y = startPosition.y,
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleCloseToTrussX,
                                                        y = startPosition.y + 4,
                                                        r = startPosition.r + 90,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleCloseToTrussX,
                                                        y = startPosition.y - 6,
                                                        r = startPosition.r + 90,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleCloseToTrussX,
                                                        y = startPosition.y - 6,
                                                        r = 0.0,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                )
                                PropPosition.Center -> depositCenterPurple(startPosition) + listOf(
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                        x = startPosition.x + 20,
                                                        y = startPosition.y - 2,
                                                        r = 0.0
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                )
                                PropPosition.Right -> listOf(
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleFarFromTrussX,
                                                        y = startPosition.y - 10,
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleFarFromTrussX - 12,
                                                        y = startPosition.y - 10,
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleFarFromTrussX - 12,
                                                        y = startPosition.y - 20,
                                                        r = 0.0,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                )
                            }
                            (propPlacement + addWait(propPlacement.last()))
                        },
                        purpleDriveToBoardPath = { propPosition ->
                            listOf(
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition, alliance).copy(
                                                    y = -30.0
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                val delayIsDone = hasTimeElapsed(waitTimeMillis, targetWorld)
                                                nextTargetFromCondition(delayIsDone && isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            },
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition, alliance).copy(
                                                    y = -30.0
                                            )),
                                            handoffInput = HandoffTarget(
                                                    armPosition = Arm.Positions.AutoInitPosition,
                                                    depoInput = liftHeight,
                                                    wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping)
                                            ),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                val isButtonNotPressed = !actualWorld.actualGamepad1.touchpad
                                                val waitIsDone = hasTimeElapsed(700, targetWorld)
                                                nextTargetFromCondition(isButtonNotPressed && waitIsDone, targetWorld)
                                            },
                                            getCurrentPositionAndRotationFromAprilTag = true
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition, alliance)),
                                            handoffInput = HandoffTarget(
                                                    armPosition = Arm.Positions.AutoInitPosition,
                                                    depoInput = liftHeight,
                                                    wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping)
                                            ),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                            )
                        },
                        yellowDepositSequence = { propPosition ->
                            depositYellow(propPosition, alliance, liftHeight)
                        },

                        cyclePath = CyclePath(
                                numberOfCycles = numberOfCycles,
                                driveToBoardFromStack = { propPosition ->
                                    listOf(
                                            cycleCollectionAndPostBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                            x = cycleUnderTrussXPosition,
                                                            y = startPosition.y,
                                                            r = 0.0,
                                                    )),
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->

                                                        val liftIsOkToDriveUnderTruss = checkIfLiftIsOkToDriveUnderTruss(actualWorld)
                                                        nextTargetFromCondition(liftIsOkToDriveUnderTruss && isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                    }
                                            ),
                                            cycleCollectionAndPostBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                            x = cycleUnderTrussXPosition,
                                                            y = 35.0,
                                                            r = 0.0,
                                                    )),
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                        nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                    }
                                            ),
                                    )
                                },
                                collectFromStack = { propPosition ->
                                    val startCollectionPosition = PositionAndRotation(
                                            x = cycleUnderTrussXPosition + 8,
                                            y = 41.5,
                                            r = -28.0,
                                    )

                                    listOf(
                                            cyclePreCollectBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(startCollectionPosition),
                                                    extendoInput = ExtendoPositions.CollectFromStack1,
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                        drivetrain.rotationPID = PID(
                                                                name = "r only",
                                                                kp= 1.4,
                                                                ki= 1.0E-4,
                                                                kd= 150.0,
                                                        )
                                                        nextTargetFromCondition(hasTimeElapsed(500, targetWorld), targetWorld)
                                                    }
                                            ),
                                            cyclePreCollectBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(startCollectionPosition),
                                                    extendoInput = ExtendoPositions.CollectFromStack1,
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                        val extendoIsOutEnoughToRunCollector = actualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks >= ExtendoPositions.ReadyToEject.ticks

                                                        //This doesn't run
                                                        println("condition ID: P, value: $extendoIsOutEnoughToRunCollector")

                                                        nextTargetFromCondition(extendoIsOutEnoughToRunCollector, targetWorld)
                                                    }
                                            ),
                                            cycleCollectionAndPostBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(startCollectionPosition),
                                                    extendoInput = ExtendoPositions.CollectFromStack1,
                                                    intakeInput = Intake.CollectorPowers.Intake,
                                                    dropdownPosition = Dropdown.DropdownPresets.FivePixels,
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->

                                                        indexOfCollectionStart = targetWorld.autoInput?.listIndex

                                                        val left = targetWorld.targetRobot.collectorTarget.transferSensorState.left.hasPixelBeenSeen
                                                        val right = targetWorld.targetRobot.collectorTarget.transferSensorState.right.hasPixelBeenSeen

                                                        val both = left && right
                                                        val either = left || right

                                                        val timeIsUp = hasTimeElapsed(2000, targetWorld)

                                                        val condition = both || (timeIsUp && either)

                                                        println("condition ID: A, value: $condition")

                                                        nextTargetFromCondition(condition, targetWorld)
                                                    }
                                            ),
                                            cycleCollectionAndPostBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(startCollectionPosition.copy(
                                                            r = startCollectionPosition.r + 15
                                                    )),
                                                    extendoInput = ExtendoPositions.CollectFromStack2,
                                                    intakeInput = Intake.CollectorPowers.Intake,
                                                    dropdownPosition = Dropdown.DropdownPresets.OnePixel,
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                        val left = targetWorld.targetRobot.collectorTarget.transferSensorState.left.hasPixelBeenSeen
                                                        val right = targetWorld.targetRobot.collectorTarget.transferSensorState.right.hasPixelBeenSeen

                                                        val both = left && right
                                                        val either = left || right

                                                        val timeIsUp = hasTimeElapsed(2000, targetWorld)

                                                        val nextTarget = if (!either && timeIsUp) {
                                                            targetWorld.copy(
                                                                    autoInput = targetWorld.autoInput?.copy(
                                                                            listIndex = targetWorld.autoInput.listIndex?.minus(2)
                                                                    )
                                                            )
                                                        } else {
                                                            targetWorld
                                                        }

                                                        val condition = both || (timeIsUp && either)

                                                        println("condition ID: D, value: $condition")

                                                        nextTargetFromCondition(condition, nextTarget)
                                                    }
                                            ),
                                            cycleCollectionAndPostBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(startCollectionPosition),
                                                    extendoInput = ExtendoPositions.CollectFromStack1,
                                                    intakeInput = Intake.CollectorPowers.Eject,
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->

                                                        val left = targetWorld.targetRobot.collectorTarget.transferSensorState.left.hasPixelBeenSeen
                                                        val right = targetWorld.targetRobot.collectorTarget.transferSensorState.right.hasPixelBeenSeen

                                                        println("condition ID: J")

                                                        nextTargetFromCondition(
                                                                hasTimeElapsed(500, targetWorld),
                                                                if (!(left && right)) {
                                                                    targetWorld.copy(
                                                                            autoInput = targetWorld.autoInput?.copy(
                                                                                    listIndex = indexOfCollectionStart
                                                                            )
                                                                    )
                                                                } else {
                                                                    targetWorld
                                                                }
                                                        )
                                                    }
                                            ),
                                    )
                                },
                                driveToStackFromBoard = { propPosition ->
                                    listOf(
                                            cyclePreCollectBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                            x = cycleUnderTrussXPosition - 1,
                                                            y = 35.0,
                                                            r = 0.0,
                                                    )),
                                                    intakeInput = Intake.CollectorPowers.Eject,
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                        drivetrain.rotationPID = drivetrain.rotationOnlyPID

                                                        val liftIsOkToDriveUnderTruss = checkIfLiftIsOkToDriveUnderTruss(actualWorld)
                                                        nextTargetFromCondition(liftIsOkToDriveUnderTruss && isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                    }
                                            ),
                                            cyclePreCollectBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                            x = cycleUnderTrussXPosition - 1,
                                                            y = startPosition.y,
                                                            r = 0.0,
                                                    )),
                                                    intakeInput = Intake.CollectorPowers.Intake,
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                        nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                    }
                                            ),
                                    )
                                },
                                scoreCycle = { propPosition ->
                                    listOf(
                                            cycleCollectionAndPostBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(spitIntoBackstagePosition.copy(r = 1.0)),
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->

                                                        val drivetrainIsAtPosition = isRobotAtXPosition(actualWorld, targetWorld, allowedErrorXInches = 4.0)

                                                        nextTargetFromCondition(drivetrainIsAtPosition, targetWorld)
                                                    }
                                            ),
                                            cycleCollectionAndPostBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(spitIntoBackstagePosition),
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                        drivetrain.rotationPID = PID(
                                                                name = "r only",
                                                                kp= 1.55,
                                                                ki= 1.0E-4,
                                                                kd= 150.0,
                                                        )

                                                        val drivetrainIsAtPosition = isRobotAtRPosition(actualWorld, targetWorld, allowedErrorRDegrees = 10.0)

                                                        nextTargetFromCondition(drivetrainIsAtPosition, targetWorld)
                                                    }
                                            ),
                                            cycleCollectionAndPostBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(spitIntoBackstagePosition),
                                                    extendoInput = ExtendoPositions.CollectFromStack1,
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                        drivetrain.rotationPID = drivetrain.rotationOnlyPID

                                                        val extendoIsOutEnough = actualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks >= ExtendoPositions.ReadyToEject.ticks

                                                        nextTargetFromCondition(extendoIsOutEnough, targetWorld)
                                                    }
                                            ),
                                            cycleCollectionAndPostBase.copy(
                                                    drivetrainTarget = Drivetrain.DrivetrainTarget(spitIntoBackstagePosition),
                                                    intakeInput = Intake.CollectorPowers.Eject,
                                                    extendoInput = ExtendoPositions.CollectFromStack1,
                                                    latchOverride = Transfer.TransferTarget(Transfer.LatchTarget(Transfer.LatchPositions.Open, 0L)),
                                                    handoffInput = HandoffTarget(
                                                            armPosition = Arm.Positions.OutButUnderTwelve,
                                                            depoInput = DepoInput.Down,
                                                            wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                                    ),
                                                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                        val timeIsDone = hasTimeElapsed(1000, targetWorld)

                                                        nextTargetFromCondition(timeIsDone, targetWorld)
                                                    }
                                            ),
                                    )
                                },
                        ),
                        parkPath = { previousAutoInput ->
                            listOf(
                                    previousAutoInput.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(previousAutoInput.drivetrainTarget.targetPosition.copy(
                                                    x = startPosition.x + 1,
                                                    y = -50.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                                    previousAutoInput.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(previousAutoInput.drivetrainTarget.targetPosition.copy(
                                                    x = startPosition.x + 1,
                                                    y = -60.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    )
                            )
                        }
                )
            }




            StartPosition.Audience -> {
                PathPreAssembled(
                        purplePlacementPath = { propPosition ->
                            val propPlacement = when (propPosition) {
                                PropPosition.Left -> listOf(
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleFarFromTrussX,
                                                        y = startPosition.y + 12,
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleFarFromTrussX - 12,
                                                        y = startPosition.y + 12,
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleFarFromTrussX - 12,
                                                        y = startPosition.y - 1,
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = xForNavigatingUnderStageDoor,
                                                        y = startPosition.y - 1,
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                )
                                PropPosition.Center -> depositCenterPurple(startPosition) + listOf(
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                        x = startPosition.x + 20,
                                                        y = startPosition.y + 1,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                        x = startPosition.x + 20,
                                                        y = startPosition.y + 1,
                                                        r = 0.0
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = -40.0,
                                                        y = startPosition.y + RobotTwoHardware.robotWidthInches + 1,
                                                        r = 0.0,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = xForNavigatingUnderStageDoor - 2,
                                                        y = startPosition.y + RobotTwoHardware.robotWidthInches + 1,
                                                        r = 0.0,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                )
                                PropPosition.Right -> listOf(
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleCloseToTrussX - 2,
                                                        y = startPosition.y,
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleCloseToTrussX,
                                                        y = startPosition.y - 4,
                                                        r = startPosition.r - 90,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = pushPurpleCloseToTrussX,
                                                        y = startPosition.y + 6,
                                                        r = startPosition.r - 90,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                        x = -25.0,
                                                        y = startPosition.y + 8,
                                                        r = 0.0,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                )
                            }
                            addOutUnderTwelve(propPlacement + addWait(propPlacement.last()))
                        },
                        purpleDriveToBoardPath = { propPosition ->
                            addOutUnderTwelve(listOf(
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                    x = xForNavigatingUnderStageDoor,
                                                    y = 38.0,
                                                    r = 0.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                    x = xForNavigatingUnderStageDoor,
                                                    y = -25.0,
                                                    r = 0.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition, alliance).copy(
                                                    y = -30.0
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            },
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition, alliance).copy(
                                                    y = -30.0
                                            )),
                                            handoffInput = HandoffTarget(
                                                    armPosition = Arm.Positions.AutoInitPosition,
                                                    depoInput = liftHeight,
                                                    wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping)
                                            ),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                val isButtonNotPressed = !actualWorld.actualGamepad1.touchpad
                                                val waitIsDone = hasTimeElapsed(1000, targetWorld)
                                                nextTargetFromCondition(isButtonNotPressed && waitIsDone, targetWorld)
                                            },
                                            getCurrentPositionAndRotationFromAprilTag = true
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition, alliance)),
                                            handoffInput = HandoffTarget(
                                                    armPosition = Arm.Positions.AutoInitPosition,
                                                    depoInput = liftHeight,
                                                    wristTargets = Wrist.WristTargets(Claw.ClawTarget.Gripping)
                                            ),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                            ))
                        },
                        yellowDepositSequence = { propPosition ->
                            depositYellow(propPosition, alliance, liftHeight)
                        },
                        parkPath = { previousAutoInput ->
                            listOf(
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                    x = -(RobotTwoHardware.robotWidthInches / 2 + 3),
                                                    y = -48.0,
                                                    r = 0.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                    x = -(RobotTwoHardware.robotWidthInches / 2 + 3),
                                                    y = -60.0,
                                                    r = 0.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    )
                            )
                        }
                )
            }
        }

        val allianceIsColorBlue = alliance == RobotTwoHardware.Alliance.Blue
        val propPositionSwappedToMatchBlue = when (propPosition) {
            PropPosition.Left -> PropPosition.Right
            PropPosition.Center -> PropPosition.Center
            PropPosition.Right -> PropPosition.Left
        }
        val adjustedPropPosition = if (allianceIsColorBlue) {
            propPositionSwappedToMatchBlue
        } else {
            propPosition
        }

        val allianceMirroredAndAsList = if (allianceIsColorBlue) {
            mirrorRedAutoToBlue(redPath.assemblePath(adjustedPropPosition))
        } else {
            redPath.assemblePath(adjustedPropPosition)
        }

        return allianceMirroredAndAsList
    }

    private fun isDepoAtPosition(
            armTarget: Arm.Positions,
            liftTarget: Lift.LiftPositions,
            actualWorld: ActualWorld
    ): Boolean {
        val liftIsAtTarget = lift.isLiftAtPosition(liftTarget.ticks, actualWorld.actualRobot.depoState.lift.currentPositionTicks)
        val armIsAtTarget = depoManager.checkIfArmIsAtTarget(armTarget, actualWorld.actualRobot.depoState.armAngleDegrees)

        return liftIsAtTarget && armIsAtTarget
    }

    private fun hasTimeElapsed(timeToElapseMilis: Long, targetWorld: TargetWorld): Boolean {
        val taskStartedTimeMilis = targetWorld.timeTargetStartedMilis
        val timeSinceTargetStarted = System.currentTimeMillis() - taskStartedTimeMilis
        return timeSinceTargetStarted >= timeToElapseMilis
    }

    private fun isRobotAtPosition(actualState: ActualWorld, previousActualState: ActualWorld, targetWorld: TargetWorld, precisionInches: Double = drivetrain.precisionInches, precisionDegrees: Double = drivetrain.precisionDegrees): Boolean {
        return drivetrain.checkIfRobotIsAtPosition(targetWorld.targetRobot.drivetrainTarget.targetPosition, previousWorld = previousActualState, actualWorld = actualState, precisionInches = precisionInches, precisionDegrees = precisionDegrees)
    }

    private fun isRobotAtXPosition(actualState: ActualWorld, targetWorld: TargetWorld, allowedErrorXInches: Double = 2.0): Boolean {
        return (actualState.actualRobot.positionAndRotation.x - targetWorld.targetRobot.drivetrainTarget.targetPosition.x).absoluteValue <= allowedErrorXInches
    }

    private fun isRobotAtRPosition(actualState: ActualWorld, targetWorld: TargetWorld, allowedErrorRDegrees: Double = 5.0): Boolean {
        return (actualState.actualRobot.positionAndRotation.r - targetWorld.targetRobot.drivetrainTarget.targetPosition.r).absoluteValue <= allowedErrorRDegrees
    }

    private fun isRobotAtPrecisePosition(actualState: ActualWorld, previousActualState: ActualWorld, targetWorld: TargetWorld): Boolean {
        return drivetrain.checkIfRobotIsAtPosition(targetWorld.targetRobot.drivetrainTarget.targetPosition, previousWorld = previousActualState, actualWorld = actualState, precisionInches = 2.0, precisionDegrees = 5.0)
    }

    private fun isRobotAtAngle(actualState: ActualWorld, previousActualState: ActualWorld, targetWorld: TargetWorld): Boolean {
        val rotationErrorDegrees = actualState.actualRobot.positionAndRotation.r - targetWorld.targetRobot.drivetrainTarget.targetPosition.r
        return rotationErrorDegrees.absoluteValue <= 3.0
    }

    private fun checkIfLiftIsOkToDriveUnderTruss(actualWorld: ActualWorld): Boolean {
        return !lift.isLiftAbovePosition(
                targetPositionTicks = 100,
                actualLiftPositionTicks = actualWorld.actualRobot.depoState.lift.currentPositionTicks
        )
    }

    private fun checkIfHandoffIsDone(previousTargetWorld: TargetWorld): Boolean {
        return !previousTargetWorld.doingHandoff
    }

    /** Path assembly */
    data class CyclePath(
            val numberOfCycles: Int,
            val driveToStackFromBoard: (PropPosition)->List<AutoInput>,
            val collectFromStack: (PropPosition)->List<AutoInput>,
            val driveToBoardFromStack: (PropPosition)->List<AutoInput>,
            val scoreCycle: (PropPosition)->List<AutoInput>,
    ) {
        fun assemblePath(propPosition: PropPosition): List<AutoInput> {
            return if (numberOfCycles > 0) {
                (1..numberOfCycles).fold(listOf<AutoInput>()) { acc, it ->
                    acc + driveToBoardFromStack(propPosition) + collectFromStack(propPosition) + driveToStackFromBoard(propPosition) + scoreCycle(propPosition)
                }
            } else {
                listOf()
            }
        }
    }

    data class PathPreAssembled(
            val purplePlacementPath: (PropPosition)->List<AutoInput>,
            val purpleDriveToBoardPath: (PropPosition)->List<AutoInput>,
            val yellowDepositSequence: (PropPosition)->List<AutoInput>,
            val cyclePath: CyclePath? = null,
            val parkPath: (previousAutoInput: AutoInput) -> List<AutoInput>) {
        fun assemblePath(propPosition: PropPosition): List<AutoInput> {
            val fiftyPoint = purplePlacementPath(propPosition) +
                    purpleDriveToBoardPath(propPosition) +
                    yellowDepositSequence(propPosition)

            val cycles = cyclePath?.assemblePath(propPosition) ?: emptyList()

            val beforePark = fiftyPoint + cycles
            return beforePark //+ parkPath(beforePark.last())
        }
    }

    enum class StartPosition(val redStartPosition: PositionAndRotation) {
        Backboard(PositionAndRotation(  x = RobotTwoHardware.redStartingXInches,
                y= -12.0,
                r= RobotTwoHardware.redStartingRDegrees)),
        Audience(PositionAndRotation(   x = RobotTwoHardware.redStartingXInches,
                y= 36.0,
                r= RobotTwoHardware.redStartingRDegrees))
    }

    private fun mirrorRedAutoToBlue(auto: List<AutoInput>): List<AutoInput> {
        return auto.map { autoInput ->
            val flippedBluePosition = flipRedPositionToBlue(autoInput.drivetrainTarget.targetPosition)
            autoInput.copy(
                    drivetrainTarget = autoInput.drivetrainTarget.copy(
                            targetPosition = flippedBluePosition
                    )
            )
        }
    }
    private fun flipRedPositionToBlue(positionAndRotation: PositionAndRotation): PositionAndRotation {
        return positionAndRotation.copy(x= -positionAndRotation.x, r= -positionAndRotation.r)
    }


    private fun nextTargetFromCondition(condition: Boolean, targetWorld: TargetWorld): AutoInput {
        return if (condition) {
            getNextTargetFromList(targetWorld.autoInput!!)
        } else {
            targetWorld.autoInput!!
        }
    }

    private fun getNextTargetFromList(previousAutoInput: AutoInput): AutoInput {
        val nextIndex = previousAutoInput.listIndex?.plus(1) ?: 0

        val listLength = autoStateList.size

        telemetry.addLine("nextIndex: $nextIndex")
        telemetry.addLine("listLength: $listLength")

        return autoStateList.getOrNull(nextIndex)?.copy(
                listIndex = nextIndex
        ) ?: previousAutoInput
    }

    lateinit var autoStateList: List<AutoInput>
    private fun nextAutoInput(actualState: ActualWorld, previousActualState: ActualWorld?, previousTargetState: TargetWorld?): AutoInput {
        return if (previousTargetState != null && previousActualState != null) {
                previousTargetState.autoInput!!.getNextInput?.invoke(actualState, previousActualState, previousTargetState)
                        ?: getNextTargetFromList(previousTargetState.autoInput)
            } else {
                getNextTargetFromList(blankAutoState)
            }
    }

    private val console = TelemetryConsole(telemetry)
    private val wizard = TelemetryWizard(console, null)

    private val timesToWait = listOf(1, 3, 5, 8, 10)
    private val cycleOptions = listOf(1, 2, 3)

    fun init(hardware: RobotTwoHardware) {
        initRobot(
            hardware = hardware,
            localizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        )

        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"), nextMenu = "partnerYellow", firstMenu = true)
        wizard.newMenu("partnerYellow", "What will our partner be placing on the board?", listOf("Yellow", "Nothing"), nextMenu = "startingPos")
        wizard.newMenu("startingPos", "What side of the truss are we on?", listOf("Audience" to "wait", "Backboard" to "cycles"))
        wizard.newMenu("cycles", "How many cycles to attempt?", listOf("No cycles") + cycleOptions.map{ it.toString() }, nextMenu = "wait")
        wizard.newMenu("wait", "How many seconds to wait before scoring yellow?", listOf("No wait") + timesToWait.map{ it.toString() })

        telemetry.addLine("init Called")
    }


    data class CameraStuff(
            val propDetector: RobotTwoPropDetector,
            val opencv: OpenCvAbstraction
    )
    private fun runCamera(opencv: OpenCvAbstraction, wizardResults: WizardResults): CameraStuff {
        val propColor: PropColors = when (wizardResults.alliance) {
            RobotTwoHardware.Alliance.Blue -> PropColors.Blue
            RobotTwoHardware.Alliance.Red -> PropColors.Red
        }

        val propDetector = RobotTwoPropDetector(telemetry, propColor)

        opencv.internalCamera = false
        opencv.cameraName = "Webcam 1"
        opencv.cameraOrientation = OpenCvCameraRotation.UPRIGHT
        opencv.onNewFrame(propDetector::processFrame)

        return CameraStuff(
                propDetector = propDetector,
                opencv = opencv,
        )
    }


    data class WizardResults(
            val alliance: RobotTwoHardware.Alliance,
            val startPosition: StartPosition,
            val partnerIsPlacingYellow: Boolean,
            val numberOfCycles: Int,
            val waitSeconds: Int
    )
    private fun getMenuWizardResults(): WizardResults {
        return try {
            WizardResults(
                alliance = when (wizard.wasItemChosen("alliance", "Red")) {
                    true -> RobotTwoHardware.Alliance.Red
                    false -> RobotTwoHardware.Alliance.Blue
                },
                startPosition = when (wizard.wasItemChosen("startingPos", "Audience")) {
                    true -> StartPosition.Audience
                    false -> StartPosition.Backboard
                },
                partnerIsPlacingYellow = wizard.wasItemChosen("partnerYellow", "Yellow"),
                numberOfCycles = cycleOptions.firstOrNull { it ->
                    wizard.wasItemChosen("cycles", it.toString())
                } ?: 0,
                waitSeconds = timesToWait.firstOrNull { it ->
                        wizard.wasItemChosen("wait", it.toString())

                } ?: 0
            )
        } catch (e: Exception) {
            telemetry.addLine("\nMENU FAILED!!\nUsing defaults")
            initLoopResults.wizardResults
        }
    }

    data class InitLoopResults(
            val wizardResults: WizardResults,
            val cameraStuff: CameraStuff?,
    )
    private var initLoopResults: InitLoopResults = InitLoopResults(
            wizardResults = WizardResults(
                    alliance = RobotTwoHardware.Alliance.Red,
                    startPosition = StartPosition.Backboard,
                    partnerIsPlacingYellow = false,
                    numberOfCycles = 0,
                    waitSeconds = 0,
            ),
            cameraStuff = null,
    )
    private var previousWizardIsDone = false
    fun initLoop(hardware: RobotTwoHardware, opencv: OpenCvAbstraction, gamepad1: Gamepad) {
        if (!previousWizardIsDone) {
            val wizardIsDone = wizard.summonWizard(gamepad1)
            if (wizardIsDone) {
                val wizardResults = getMenuWizardResults()

                initLoopResults = InitLoopResults(
                        wizardResults = wizardResults,
                        cameraStuff = runCamera(opencv, wizardResults)
                )

                previousWizardIsDone = true
            }
        } else {
            telemetry.addLine("propPosition: ${initLoopResults.cameraStuff?.propDetector?.propPosition}")
            telemetry.addLine("wizardResults: ${initLoopResults}")
        }
    }

    fun start(hardware: RobotTwoHardware) {
        val propPosition = initLoopResults.cameraStuff?.propDetector?.propPosition ?: PropPosition.Right
        initLoopResults.cameraStuff?.opencv?.stop()

        val wizardResults = initLoopResults.wizardResults

        val startPositionAndRotation: PositionAndRotation = when (wizardResults.alliance) {
            RobotTwoHardware.Alliance.Red ->
                initLoopResults.wizardResults.startPosition.redStartPosition

            RobotTwoHardware.Alliance.Blue ->
                flipRedPositionToBlue(wizardResults.startPosition.redStartPosition)
        }

        autoStateList = calcAutoTargetStateList(
                alliance = wizardResults.alliance,
                startingSide = wizardResults.startPosition,
                propPosition = propPosition,
                partnerIsPlacingYellow = wizardResults.partnerIsPlacingYellow,
                numberOfCycles = wizardResults.numberOfCycles,
                waitTimeMillis = wizardResults.waitSeconds * 1000L
        )

        drivetrain.localizer.setPositionAndRotation(startPositionAndRotation)
    }

    fun loop(hardware: RobotTwoHardware, aprilTagPipeline: AprilTagPipelineForEachCamera, gamepad1: SerializableGamepad) = measured("main loop"){
        runRobot(
                targetStateFetcher = { actualWorldWithoutAprilTags, previousActualWorld, previousTargetWorld ->
                    val actualWorld = actualWorldWithoutAprilTags.copy(
                            aprilTagReadings = listOf()
                    )

                    val autoInput = nextAutoInput(
                            actualWorld,
                            previousActualWorld,
                            previousTargetWorld
                    )

                    val previousActualWorld = previousActualWorld ?: TeleopTest.emptyWorld
                    val previousTargetWorld: TargetWorld = previousTargetWorld ?: initialPreviousTargetState
                    val targetWorld = getTargetWorldFromAutoInput(
                            autoInput = autoInput,
                            actualWorld = actualWorld,
                            aprilTagReadings = aprilTagPipeline.detections(),
                            previousActualWorld = previousActualWorld,
                            previousTargetWorld = previousTargetWorld
                    )

                    targetWorld
                },
                gamepad1 = gamepad1,
                gamepad2 = gamepad1,
                hardware = hardware
        )
    }
}