package us.brainstormz.robotTwo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
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
import kotlin.math.absoluteValue

fun Any.printPretty() = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
fun ActualWorld.withoutLights() = this.copy(actualRobot=this.actualRobot.copy(neopixelState = StripState(true, emptyList()))).printPretty()
fun TargetWorld.withoutLights() = this.copy(targetRobot=this.targetRobot.copy(lights = RobotTwoTeleOp.LightTarget(stripTarget = StripState(true, emptyList()))))
fun TargetWorld.withoutGetNext() = this.copy(autoInput=this.autoInput?.copy(getNextInput = null))


class RobotTwoAuto(
    private val telemetry: Telemetry,
): RobotTwo(telemetry) {

    private val blankAutoState = AutoInput(
            drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation()),
            depoInput = DepoInput.Down,
            armAtInitPosition = ArmInput.NoInput,
            handoffInput = HandoffInput.NoInput,
            wristInput = WristInput(ClawInput.NoInput, ClawInput.NoInput),
            extendoInput = ExtendoPositions.Min,
            intakeInput = IntakeInput.NoInput,
            getNextInput = { actualWorld, previousActualWorld, previousTargetWorld -> getNextTargetFromList(previousAutoInput= previousTargetWorld.autoInput!!) },
    )

    fun getTargetWorldFromAutoInput(autoInput: AutoInput, actualWorld: ActualWorld, previousActualWorld: ActualWorld, previousTargetWorld: TargetWorld): TargetWorld {

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


//        val intakeNoodleTarget = if (timeToStartEjection) {
//            Intake.CollectorPowers.Eject
//        } else if (stopAutomaticEjection) {
//            Intake.CollectorPowers.Off
//        } else {
//            when (autoInput.intakeInput) {
//                IntakeInput.Intake -> Intake.CollectorPowers.Intake
//                IntakeInput.NoInput -> previousTargetWorld.targetRobot.collectorTarget.intakeNoodles
//            }
//        }

        val intakeNoodleTarget = when (autoInput.intakeInput) {
                IntakeInput.Intake -> Intake.CollectorPowers.Intake
                IntakeInput.NoInput -> Intake.CollectorPowers.Off
        }

//        /**Dropdown*/
        val dropdownIsInInitialPosition = previousTargetWorld.targetRobot.collectorTarget.dropDown.targetPosition == Dropdown.DropdownPresets.Init
        val extendoIsFarEnoughOutForDropdownToDropDown = actualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks >= ExtendoPositions.InPastBatteryBox.ticks
        val dropdownTarget = if (dropdownIsInInitialPosition && !extendoIsFarEnoughOutForDropdownToDropDown) {
            Dropdown.DropdownPresets.Init
        } else {
            when (autoInput.intakeInput) {
                IntakeInput.Intake -> {
                    Dropdown.DropdownPresets.TwoPixels
                }
                IntakeInput.NoInput -> {
                    Dropdown.DropdownPresets.Up
                }
            }
        }

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
        val extendoDriverInput = when (autoInput.extendoInput) {
            ExtendoPositions.Min -> ExtendoInput.RetractManual
            else -> ExtendoInput.ExtendManual
        }

        val doingHandoff = autoInput.handoffInput == HandoffInput.Handoff

        val repeatDriverInputForDepo = when {
//            doingHandoff -> DepoInput.Down
            autoInput.depoInput == DepoInput.NoInput -> {
                DepoInput.Down
            }
            else -> autoInput.depoInput
        }

        val handoffTarget = handoffManager.manageHandoff(
                wristInput = autoInput.wristInput,
                depoInput = repeatDriverInputForDepo,
                extendoInput = extendoDriverInput,
                collectorTarget = uncoordinatedCollectorTarget,
                previousTargetWorld = previousTargetWorld,
                actualWorld = actualWorld,
                doingHandoff = doingHandoff,
        )

        val depoTargetWithInitArm = when (autoInput.armAtInitPosition) {
            ArmInput.InitPosition -> DepoTarget(
                    armPosition = Arm.ArmTarget(Arm.Positions.AutoInitPosition),
                    lift = Lift.TargetLift(Lift.LiftPositions.Down),
                    wristPosition = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                    targetType = DepoManager.DepoTargetType.GoingHome
            )
            ArmInput.GoUnderTrussPosition -> DepoTarget(
                    armPosition = Arm.ArmTarget(Arm.Positions.OutButUnderTwelve),
                    lift = Lift.TargetLift(Lift.LiftPositions.Down),
                    wristPosition = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                    targetType = DepoManager.DepoTargetType.GoingHome
            )
            ArmInput.NoInput -> handoffTarget.depo
        }

        return TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = autoInput.drivetrainTarget,
                        depoTarget = depoTargetWithInitArm,
                        collectorTarget = handoffTarget.collector,
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
                gamepad1Rumble = null
        )
    }

    private val xForNavigatingUnderStageDoor = -((RobotTwoHardware.robotWidthInches/2) + 2)

    private val depositY = -53.0
    private fun depositingPosition(propPosition: PropPosition) = when (propPosition) {
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

    private val pushIntoBoardDrivetrainPower = -0.3
    private fun depositYellow(propPosition: PropPosition) = listOf(
            blankAutoState.copy(
                    drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition)),
                    depoInput = DepoInput.YellowPlacement,
                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                        nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                    }
            ),
            blankAutoState.copy(
                    drivetrainTarget = Drivetrain.DrivetrainTarget(Drivetrain.DrivetrainPower(y= pushIntoBoardDrivetrainPower)),
                    depoInput = DepoInput.YellowPlacement,
                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->

                        val isDepoAtPosition = isDepoAtPosition(
                                armTarget = Arm.Positions.Out,
                                liftTarget = Lift.LiftPositions.AutoLowYellowPlacement,
                                actualWorld = actualWorld
                        )

                        val isRobotMoving = drivetrain.getVelocity(actualWorld, previousActualWorld).checkIfIsLessThan(drivetrain.maxVelocityToStayAtPosition)

                         nextTargetFromCondition(isDepoAtPosition && !isRobotMoving, targetWorld)
                    }
            ),
            blankAutoState.copy(
                    drivetrainTarget = Drivetrain.DrivetrainTarget(Drivetrain.DrivetrainPower(y= pushIntoBoardDrivetrainPower)),
                    depoInput = DepoInput.YellowPlacement,
                    wristInput = WristInput(
                            left = ClawInput.Drop,
                            right = ClawInput.Drop
                    ),
                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                        val wristIsAtPosition = wrist.wristIsAtPosition(
                                target = Wrist.WristTargets(
                                        left = Claw.ClawTarget.Retracted,
                                        right = Claw.ClawTarget.Retracted
                                ),
                                actual = actualWorld.actualRobot.depoState.wristAngles
                        )
                        val liftIsAtPosition = lift.isLiftAtPosition(Lift.LiftPositions.AutoLowYellowPlacement.ticks, actualWorld.actualRobot.depoState.lift.currentPositionTicks)

                        nextTargetFromCondition(wristIsAtPosition && liftIsAtPosition, targetWorld)
                    }
            ),
            blankAutoState.copy(
                    drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition).copy(
                            y = depositingPosition(propPosition).y + 3
                    )),
                    depoInput = DepoInput.YellowPlacement,
                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->

                        nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                    }
            ),
    )

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
    private val pushPurpleCloseToTrussX = -34.0

    private fun List<AutoInput>.addArmInitToPath(): List<AutoInput> = this.map { autoInput ->
        autoInput.copy(
                armAtInitPosition = ArmInput.InitPosition
        )
    }
    private fun List<AutoInput>.addArmUnderTrussToPath(): List<AutoInput> = this.map { autoInput ->
        autoInput.copy(
                armAtInitPosition = ArmInput.GoUnderTrussPosition
        )
    }

    private fun calcAutoTargetStateList(
            alliance: RobotTwoHardware.Alliance,
            startingSide: StartPosition,
            propPosition: PropPosition
    ): List<AutoInput> {

        val startPosition = startingSide.redStartPosition

        val redPath: PathPreAssembled = when (startingSide) {
            StartPosition.Backboard -> {
                PathPreAssembled(
                        purplePlacementPath = { propPosition ->
                            when (propPosition) {
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
                            }.addArmInitToPath()
                        },
                        purpleDriveToBoardPath = { propPosition ->
                            listOf(
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition)),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                            ).addArmInitToPath()
                        },
                        yellowDepositSequence = { propPosition ->
                            depositYellow(propPosition)
                        },
                        parkPath = listOf(
                                blankAutoState.copy(
                                        drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                x = startPosition.x+1,
                                                y = -50.0,
                                                r = 0.0,
                                        )),
                                        getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                            nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                        }
                                ),
                                blankAutoState.copy(
                                        drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                x = startPosition.x+1,
                                                y = -60.0,
                                                r = 0.0,
                                        )),
                                        getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                            nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                        }
                                )
                        )
                )
            }




            StartPosition.Audience -> {
                PathPreAssembled(
                        purplePlacementPath = { propPosition ->
                            when (propPosition) {
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
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                )
                            }.addArmInitToPath()
                        },
                        purpleDriveToBoardPath = { propPosition ->
                            listOf(
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                    x = xForNavigatingUnderStageDoor,
                                                    y = 38.0,
                                                    r = 0.0,
                                            )),
                                            armAtInitPosition = ArmInput.GoUnderTrussPosition,
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
                                            armAtInitPosition = ArmInput.GoUnderTrussPosition,
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(depositingPosition(propPosition)),
                                            armAtInitPosition = ArmInput.GoUnderTrussPosition,
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                            ).addArmUnderTrussToPath()
                        },
                        yellowDepositSequence = { propPosition ->
                            depositYellow(propPosition)
                        },
                        parkPath = listOf(
                                blankAutoState.copy(
                                        drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                x = -(RobotTwoHardware.robotWidthInches/2 + 3),
                                                y = -48.0,
                                                r = 0.0,
                                        )),
                                        getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                            nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                        }
                                ),
                                blankAutoState.copy(
                                        drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(
                                                x = -(RobotTwoHardware.robotWidthInches/2 + 3),
                                                y = -60.0,
                                                r = 0.0,
                                        )),
                                        getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                            nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                        }
                                )
                        )
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
        return drivetrain.checkIfDrivetrainIsAtPosition(targetWorld.targetRobot.drivetrainTarget.targetPosition, previousWorld = previousActualState, actualWorld = actualState, precisionInches = precisionInches, precisionDegrees = precisionDegrees)
    }

    private fun isRobotAtPrecisePosition(actualState: ActualWorld, previousActualState: ActualWorld, targetWorld: TargetWorld): Boolean {
        return drivetrain.checkIfDrivetrainIsAtPosition(targetWorld.targetRobot.drivetrainTarget.targetPosition, previousWorld = previousActualState, actualWorld = actualState, precisionInches = 2.0, precisionDegrees = 4.0)
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

    enum class IntakeInput {
        Intake,
        NoInput
    }

    enum class ArmInput {
        InitPosition,
        GoUnderTrussPosition,
        NoInput
    }


    /** Path assembly */
    data class CyclePath(
            val numberOfCycles: Int,
            val driveToStackFromBoard: (PropPosition)->List<AutoInput>,
            val collectFromStack: (PropPosition)->List<AutoInput>,
            val driveToBoardFromStack: (PropPosition)->List<AutoInput>,
            val depositSequence: (PropPosition)->List<AutoInput>,
    ) {
        fun assemblePath(propPosition: PropPosition): List<AutoInput> {
            return (0..numberOfCycles).fold(listOf<AutoInput>()) { acc, it ->
                acc + driveToStackFromBoard(propPosition) + collectFromStack(propPosition) + driveToBoardFromStack(propPosition) + depositSequence(propPosition)
            }
        }
    }

    data class PathPreAssembled(
            val purplePlacementPath: (PropPosition)->List<AutoInput>,
            val purpleDriveToBoardPath: (PropPosition)->List<AutoInput>,
            val yellowDepositSequence: (PropPosition)->List<AutoInput>,
            val cyclePath: CyclePath? = null,
            val parkPath: List<AutoInput>) {
        fun assemblePath(propPosition: PropPosition): List<AutoInput> {
            val fiftyPoint = purplePlacementPath(propPosition) +
                    purpleDriveToBoardPath(propPosition) +
                    yellowDepositSequence(propPosition)

            val cycles = cyclePath?.assemblePath(propPosition) ?: emptyList()

            return  fiftyPoint + cycles + parkPath

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

    fun init(hardware: RobotTwoHardware) {
        initRobot(
            hardware = hardware,
            localizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        )

        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"), nextMenu = "partnerYellow", firstMenu = true)
        wizard.newMenu("partnerYellow", "What will our partner be placing on the board?", listOf("Yellow", "Nothing"), nextMenu = "startingPos")
        wizard.newMenu("startingPos", "What side of the truss are we on?", listOf("Audience" to "doYellow", "Backboard" to null))
        wizard.newMenu("doYellow", "What do we do after the purple?", listOf("Yellow", "Nothing"))

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


    data class WizardResults(val alliance: RobotTwoHardware.Alliance, val startPosition: StartPosition, val partnerIsPlacingYellow: Boolean, val shouldDoYellow: Boolean)
    private fun getMenuWizardResults(): WizardResults {
        return WizardResults(
                alliance = when (wizard.wasItemChosen("alliance", "Red")) {
                    true -> RobotTwoHardware.Alliance.Red
                    false -> RobotTwoHardware.Alliance.Blue
                },
                startPosition = when (wizard.wasItemChosen("startingPos", "Audience")) {
                    true -> StartPosition.Audience
                    false -> StartPosition.Backboard
                },
                partnerIsPlacingYellow = wizard.wasItemChosen("partnerYellow", "Yellow"),
                shouldDoYellow = wizard.wasItemChosen("doYellow", "Yellow")
        )
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
                    shouldDoYellow = false,
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

        autoStateList = calcAutoTargetStateList(wizardResults.alliance, wizardResults.startPosition, propPosition)

        drivetrain.localizer.setPositionAndRotation(startPositionAndRotation)
    }


    fun loop(hardware: RobotTwoHardware, gamepad1: SerializableGamepad) = measured("main loop"){
        runRobot(
                targetStateFetcher = { actualWorld, previousActualWorld, previousTargetWorld ->

                    val autoInput = nextAutoInput(
                            actualWorld,
                            previousActualWorld,
                            previousTargetWorld
                    )
                    telemetry.addLine("previous auto input index: ${previousTargetWorld?.autoInput?.listIndex}")
                    telemetry.addLine("new auto input index: ${autoInput.listIndex}")

                    val previousActualWorld = previousActualWorld ?: TeleopTest.emptyWorld
                    val previousTargetWorld: TargetWorld = previousTargetWorld ?: initialPreviousTargetState
                    val targetWorld = getTargetWorldFromAutoInput(
                            autoInput = autoInput,
                            actualWorld = actualWorld,
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