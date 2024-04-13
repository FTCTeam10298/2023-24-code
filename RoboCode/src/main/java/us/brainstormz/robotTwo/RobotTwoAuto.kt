package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.robotTwo.RobotTwoHardware.UnchangingRobotAttributes.alliance
import us.brainstormz.robotTwo.RobotTwoPropDetector.PropPosition
import us.brainstormz.robotTwo.RobotTwoTeleOp.*
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initLatchTarget
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initialPreviousTargetState
import us.brainstormz.robotTwo.localTests.TeleopTest
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Extendo.ExtendoPositions
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.measured
import kotlin.math.absoluteValue


class RobotTwoAuto(
    private val telemetry: Telemetry,
): RobotTwo(telemetry) {

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

        val handoffTarget = handoffManager.manageHandoff(
                wristInput = autoInput.wristInput,
                depoInput = autoInput.depoInput,
                extendoInput = extendoDriverInput,
                collectorTarget = uncoordinatedCollectorTarget,
                previousTargetWorld = previousTargetWorld,
                actualWorld = actualWorld,
                doingHandoff = doingHandoff,
        )

        return TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = autoInput.drivetrainTarget,
                        depoTarget = handoffTarget.depo,
                        collectorTarget = handoffTarget.collector,
                        hangPowers = RobotTwoHardware.HangPowers.Holding,
                        launcherPosition = RobotTwoHardware.LauncherPosition.Holding,
                        lights = LightTarget(),
                ),
                doingHandoff = handoffTarget.handoffCompleted,
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

    enum class IntakeInput {
        Intake,
        NoInput
    }

    private val blankAutoState = AutoInput(
            drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation()),
            depoInput = DepoInput.NoInput,
            handoffInput = HandoffInput.NoInput,
            wristInput = WristInput(ClawInput.NoInput, ClawInput.NoInput),
            extendoInput = ExtendoPositions.Min,
            intakeInput = IntakeInput.NoInput,
            getNextInput = { actualWorld, previousActualWorld, previousTargetWorld -> getNextTargetFromList(previousAutoInput= previousTargetWorld.autoInput!!) }
    )

    private fun hasTimeElapsed(timeToElapseMilis: Long, targetWorld: TargetWorld): Boolean {
        val taskStartedTimeMilis = targetWorld.timeTargetStartedMilis
        val timeSinceTargetStarted = System.currentTimeMillis() - taskStartedTimeMilis
        return timeSinceTargetStarted >= timeToElapseMilis
    }

    private fun isRobotAtPosition(actualState: ActualWorld, previousActualState: ActualWorld, targetWorld: TargetWorld, precisionInches: Double = drivetrain.precisionInches, precisionDegrees: Double = drivetrain.precisionDegrees): Boolean {
        return drivetrain.checkIfDrivetrainIsAtPosition(targetWorld.targetRobot.drivetrainTarget.targetPosition, previousWorld = previousActualState, actualWorld = actualState, precisionInches = precisionInches, precisionDegrees = precisionDegrees)
    }

    private fun isRobotAtPrecisePosition(actualState: ActualWorld, previousActualState: ActualWorld, targetWorld: TargetWorld): Boolean {
        return drivetrain.checkIfDrivetrainIsAtPosition(targetWorld.targetRobot.drivetrainTarget.targetPosition, previousWorld = previousActualState, actualWorld = actualState, precisionInches = 2.0, precisionDegrees = 5.0)
    }

    private fun isRobotAtAngle(actualState: ActualWorld, previousActualState: ActualWorld, targetWorld: TargetWorld): Boolean {
        val rotationErrorDegrees = actualState.actualRobot.positionAndRotation.r - targetWorld.targetRobot.drivetrainTarget.targetPosition.r
        return rotationErrorDegrees.absoluteValue <= 3.0
    }

    private val pushIntoBoardDrivetrainPower = -0.3

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
    data class PathPreAssembled(
            val purplePlacementPath: (PropPosition)->List<AutoInput>,
            val purpleDriveToBoardPath: (PropPosition)->List<AutoInput>,
            val yellowDepositSequence: (PropPosition)->List<AutoInput>,
            val driveToStackFromBoard: (PropPosition)->List<AutoInput>,
            val collectFromStack: (PropPosition)->List<AutoInput>,
            val driveToBoardFromStack: (PropPosition)->List<AutoInput>,
            val parkPath: List<AutoInput>) {
        fun assemblePath(propPosition: PropPosition, numberOfCycles: Int): List<AutoInput> {
            val fiftyPoint = purplePlacementPath(propPosition) +
                    purpleDriveToBoardPath(propPosition) +
                    yellowDepositSequence(propPosition)

            val cycles = (0..numberOfCycles).fold(listOf<AutoInput>()) { acc, it ->
                acc + driveToStackFromBoard(propPosition) + collectFromStack(propPosition) + driveToBoardFromStack(propPosition) + yellowDepositSequence(propPosition)
            }
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

    private fun calcAutoTargetStateList(
            alliance: RobotTwoHardware.Alliance,
            startingSide: StartPosition,
            propPosition: PropPosition
    ): List<AutoInput> {
        val numberOfCycles = 2

        val startPosition = startingSide.redStartPosition

        val redPath: PathPreAssembled = when (startingSide) {
            StartPosition.Backboard -> {
                PathPreAssembled(
                        purplePlacementPath = { propPosition ->
                            when (propPosition) {
                                PropPosition.Left -> listOf(

                                )
                                PropPosition.Center -> listOf(

                                )
                                PropPosition.Right -> listOf(
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                        x = startPosition.x + 24,
                                                        y = startPosition.y - 10,
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                        x = startPosition.x + 12,
                                                        y = startPosition.y - 10,
                                                        r = startPosition.r,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                        blankAutoState.copy(
                                                drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                        x = startPosition.x + 12,
                                                        y = startPosition.y - 20,
                                                        r = 0.0,
                                                )),
                                                getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                    nextTargetFromCondition(isRobotAtPrecisePosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                                }
                                        ),
                                )
                            }
                        },
                        purpleDriveToBoardPath = { propPosition ->
                            listOf(
                                blankAutoState.copy(
                                        drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                x = startPosition.x + 26,
                                                y = -53.0,
                                                r = 0.0,
                                        )),
                                        handoffInput = HandoffInput.Handoff,
                                        getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                            nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                        }
                                ),
                            )
                        },
                        yellowDepositSequence = { propPosition ->
                            listOf(
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(Drivetrain.DrivetrainPower(y= pushIntoBoardDrivetrainPower)),
                                            depoInput = DepoInput.Down,
                                            handoffInput = HandoffInput.Handoff,
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(checkIfHandoffIsDone(targetWorld), targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(Drivetrain.DrivetrainPower(y= pushIntoBoardDrivetrainPower)),
                                            depoInput = DepoInput.Preset1,
                                            handoffInput = HandoffInput.Handoff,
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                val liftIsAtPosition = lift.isLiftAtPosition(targetWorld.targetRobot.depoTarget.lift.targetPosition.ticks, actualWorld.actualRobot.depoState.lift.currentPositionTicks)
                                                nextTargetFromCondition(liftIsAtPosition, targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(Drivetrain.DrivetrainPower(y= pushIntoBoardDrivetrainPower)),
                                            depoInput = DepoInput.Preset1,
                                            handoffInput = HandoffInput.NoInput,
//                                            wristInput = WristInput(
//                                                    left = ClawInput.Drop,
//                                                    right = ClawInput.Drop
//                                            ),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                val wristIsAtPosition = wrist.wristIsAtPosition(
                                                        target = Wrist.WristTargets(
                                                                left = Claw.ClawTarget.Retracted,
                                                                right = Claw.ClawTarget.Retracted
                                                        ),
                                                        actual = actualWorld.actualRobot.depoState.wristAngles
                                                )
                                                nextTargetFromCondition(wristIsAtPosition, targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(Drivetrain.DrivetrainPower()),
                                            depoInput = DepoInput.Down,
                                            handoffInput = HandoffInput.NoInput,
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                val liftIsDown = !lift.isLiftAbovePosition(
                                                        targetPositionTicks = targetWorld.targetRobot.depoTarget.lift.targetPosition.ticks,
                                                        actualLiftPositionTicks = actualWorld.actualRobot.depoState.lift.currentPositionTicks
                                                )
                                                nextTargetFromCondition(liftIsDown, targetWorld)
                                            }
                                    ),
                            )
                        },
                        driveToStackFromBoard = { propPosition ->
                            listOf(
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                    x = startPosition.x + 3,
                                                    y = startPosition.y,
                                                    r = 0.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                val liftIsOkToDriveUnderTruss = checkIfLiftIsOkToDriveUnderTruss(actualWorld)
                                                nextTargetFromCondition(liftIsOkToDriveUnderTruss && isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                    x = startPosition.x + 3.0,
                                                    y = 35.0,
                                                    r = 0.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                    x = -50.0,
                                                    y = 40.0,
                                                    r = -30.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtAngle(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                            )
                        },
                        collectFromStack = { propPosition ->
                            listOf(
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                    x = -45.0,
                                                    y = 40.0,
                                                    r = -25.0,
                                            )),
                                            extendoInput = ExtendoPositions.CollectFromStack,
                                            intakeInput = IntakeInput.Intake,
                                            handoffInput = HandoffInput.NoInput,
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                val weGotTwoPixels = Side.entries.fold(true) { acc, side ->
                                                    val pixelIsInSide = targetWorld.targetRobot.collectorTarget.transferSensorState.getBySide(side).hasPixelBeenSeen
                                                    acc && pixelIsInSide
                                                }
                                                val timeIsUp = false//hasTimeElapsed(5000, targetWorld)
                                                nextTargetFromCondition(weGotTwoPixels || timeIsUp, targetWorld)
                                            }
                                    ),
                            )
                        },
                        driveToBoardFromStack = { propPosition ->
                            listOf(
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                    x = startPosition.x + 2.5,
                                                    y = 35.0,
                                                    r = 0.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                val liftIsOkToDriveUnderTruss = checkIfLiftIsOkToDriveUnderTruss(actualWorld)
                                                nextTargetFromCondition(liftIsOkToDriveUnderTruss && isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                    x = startPosition.x + 3,
                                                    y = startPosition.y,
                                                    r = 0.0,
                                            )),
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                                    blankAutoState.copy(
                                            drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                    x = startPosition.x + 26,
                                                    y = -53.0,
                                                    r = 0.0,
                                            )),
                                            handoffInput = HandoffInput.Handoff,
                                            getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                                nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                            }
                                    ),
                            )
                        },
                        parkPath = listOf(
                                blankAutoState.copy(
                                        drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                x = startPosition.x,
                                                y = -50.0,
                                                r = 0.0,
                                        )),
                                        getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                                            nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                                        }
                                ),
                                blankAutoState.copy(
                                        drivetrainTarget = Drivetrain.DrivetrainTarget(startPosition.copy(
                                                x = startPosition.x,
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
                            listOf()
                        },
                        purpleDriveToBoardPath = { propPosition ->
                            listOf()
                        },
                        yellowDepositSequence = { propPosition ->
                            listOf()
                        },
                        driveToStackFromBoard = { propPosition ->
                            listOf()
                        },
                        collectFromStack = { propPosition ->
                            listOf()
                        },
                        driveToBoardFromStack = { propPosition ->
                            listOf()
                        },
                        parkPath = listOf()
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
            mirrorRedAutoToBlue(redPath.assemblePath(adjustedPropPosition, numberOfCycles))
        } else {
            redPath.assemblePath(adjustedPropPosition, numberOfCycles)
        }

        return allianceMirroredAndAsList
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
            targetWorld.autoInput ?: getNextTargetFromList(blankAutoState)
        }
    }

    private fun getNextTargetFromList(previousAutoInput: AutoInput): AutoInput {
        return if (autoListIterator.hasNext()) {
            autoListIterator.next()
        } else {
            previousAutoInput
        }
    }

    private lateinit var autoStateList: List<AutoInput>
    private lateinit var autoListIterator: ListIterator<AutoInput>
    private fun nextTargetState(actualState: ActualWorld, previousActualState: ActualWorld?, previousTargetState: TargetWorld?): AutoInput {
        return if (previousTargetState != null && previousActualState != null) {
            previousTargetState.autoInput!!.getNextInput?.invoke(actualState, previousActualState, previousTargetState)
                    ?: getNextTargetFromList(blankAutoState)
        } else {
            getNextTargetFromList(blankAutoState)
        }
    }

    data class WizardResults(val alliance: RobotTwoHardware.Alliance, val startPosition: StartPosition, val partnerIsPlacingYellow: Boolean, val shouldDoYellow: Boolean)

    private var startPosition: StartPosition = StartPosition.Backboard

    fun init(hardware: RobotTwoHardware) {
        initRobot(
            hardware = hardware,
            localizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        )

        telemetry.addLine("init Called")
    }

    fun start(hardware: RobotTwoHardware) {

        val startPositionAndRotation: PositionAndRotation = when (alliance) {
            RobotTwoHardware.Alliance.Red -> startPosition.redStartPosition
            RobotTwoHardware.Alliance.Blue -> flipRedPositionToBlue(startPosition.redStartPosition)
        }

        autoStateList = calcAutoTargetStateList(alliance, startPosition, PropPosition.Right)
        autoListIterator = autoStateList.listIterator()

        drivetrain.localizer.setPositionAndRotation(startPositionAndRotation)
    }


    fun loop(hardware: RobotTwoHardware, gamepad1: SerializableGamepad) = measured("main loop"){

        telemetry.addLine("looping at auto level")
        telemetry.addLine("Looping")
        runRobot(
                targetStateFetcher = { actualWorld, previousActualWorld, previousTargetWorld ->

                    val newInput = nextTargetState(
                            actualWorld,
                            previousActualWorld,
                            previousTargetWorld
                    )

                    telemetry.addLine("auto: $newInput")
                    telemetry.addLine("looping at targetStateFetcher level")

                    val previousActualWorld = previousActualWorld ?: TeleopTest.emptyWorld
                    val previousTargetWorld: TargetWorld = previousTargetWorld ?: initialPreviousTargetState
                    val targetWorld = getTargetWorldFromAutoInput(
                            autoInput = newInput,
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