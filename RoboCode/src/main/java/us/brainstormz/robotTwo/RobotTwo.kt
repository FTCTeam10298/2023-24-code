package us.brainstormz.robotTwo

import StatsDumper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.qualcomm.hardware.lynx.LynxModule
import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.FauxLocalizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initialPreviousTargetState
import us.brainstormz.robotTwo.RobotTwoTeleOp.*
import us.brainstormz.robotTwo.localTests.TeleopTest
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.DeltaTimeMeasurer
import us.brainstormz.utils.measured
import java.io.File

open class RobotTwo(private val telemetry: Telemetry) {
    val intake = Intake()
    val dropdown = Dropdown()
    val transfer = Transfer(telemetry)
    val extendo = Extendo(telemetry)
    val collectorSystem: CollectorManager = CollectorManager(transfer= transfer, extendo= extendo, intake = intake, telemetry= telemetry)
    val leftClaw: Claw = Claw(telemetry)
    val rightClaw: Claw = Claw(telemetry)
    val wrist = Wrist(leftClaw, rightClaw, telemetry= telemetry)
    val arm: Arm = Arm()
    val lift: Lift = Lift(telemetry)
    val depoManager: DepoManager = DepoManager(arm= arm, lift= lift, wrist= wrist, telemetry= telemetry)
    val handoffManager: HandoffManager = HandoffManager(collectorSystem, depoManager, wrist, arm, lift, transfer, telemetry)


    fun getTargetWorld(driverInput: RobotTwoTeleOp.DriverInput, actualWorld: ActualWorld, previousActualWorld: ActualWorld, previousTargetState: TargetWorld): TargetWorld {
        val actualRobot = actualWorld.actualRobot

        telemetry.addLine("\nlift stuff: ${actualRobot.depoState.lift}")
        telemetry.addLine("extendo stuff: ${actualRobot.collectorSystemState.extendo}")
        telemetry.addLine("wrist angles: ${actualRobot.depoState.wristAngles}\n")

        /**Handoff*/
        val transferState = transfer.getTransferState(
            actualWorld = actualWorld,
            previousTransferState = previousTargetState.targetRobot.collectorTarget.transferSensorState,
        )

        val transferLeftSensorState = transfer.checkIfPixelIsTransferred(transferState.left)
        val transferRightSensorState = transfer.checkIfPixelIsTransferred(transferState.right)
        val areBothPixelsIn = transferLeftSensorState && transferRightSensorState

        val previousTransferLeftSensorState = transfer.checkIfPixelIsTransferred(previousTargetState.targetRobot.collectorTarget.transferSensorState.left)
        val previousTransferRightSensorState = transfer.checkIfPixelIsTransferred(previousTargetState.targetRobot.collectorTarget.transferSensorState.right)
        val wereBothPixelsInPreviously = previousTransferLeftSensorState && previousTransferRightSensorState

        val theRobotJustCollectedTwoPixels = areBothPixelsIn && !wereBothPixelsInPreviously


        /**Intake Noodles*/
        val timeSincePixelsTransferredMillis: Long = actualWorld.timestampMilis - (previousTargetState.targetRobot.collectorTarget.timeOfTransferredMillis?:actualWorld.timestampMilis)
        val waitBeforeEjectingMillis = 200
        val timeToStartEjection = theRobotJustCollectedTwoPixels && (timeSincePixelsTransferredMillis > waitBeforeEjectingMillis)

        val timeSinceEjectionStartedMillis: Long = actualWorld.timestampMilis - (previousTargetState.targetRobot.collectorTarget.timeOfEjectionStartMilis?:actualWorld.timestampMilis)
        val ejectionTimeMillis = 1000
        val timeToStopEjecting = timeSinceEjectionStartedMillis > ejectionTimeMillis

        val wasPreviouslyEjecting = previousTargetState.targetRobot.collectorTarget.intakeNoodles == Intake.CollectorPowers.Eject
        val stopAutomaticEjection = timeToStopEjecting && wasPreviouslyEjecting// && doHandoffSequence
        val intakeNoodleTarget = if (timeToStartEjection) {
            Intake.CollectorPowers.Eject
        } else if (stopAutomaticEjection) {
            Intake.CollectorPowers.Off
        } else {
            when (driverInput.collector) {
                RobotTwoTeleOp.CollectorInput.Intake -> Intake.CollectorPowers.Intake
                CollectorInput.Eject -> Intake.CollectorPowers.Eject
                CollectorInput.Off -> Intake.CollectorPowers.Off
                CollectorInput.NoInput -> previousTargetState.targetRobot.collectorTarget.intakeNoodles
            }
        }
        val timeOfEjectionStartMillis = if (timeToStartEjection) {
            actualWorld.timestampMilis
        } else if (stopAutomaticEjection) {
            null
        } else {
            previousTargetState.targetRobot.collectorTarget.timeOfEjectionStartMilis
        }
        val timeOfTransferredMillis = if (theRobotJustCollectedTwoPixels) {
            actualWorld.timestampMilis
        } else if (stopAutomaticEjection) {
            null
        } else {
            previousTargetState.targetRobot.collectorTarget.timeOfTransferredMillis
        }

        /**Dropdown*/
        val dropdownTarget = when (driverInput.dropdown) {
            RobotTwoTeleOp.DropdownInput.Five -> {
                Dropdown.DropdownTarget(Dropdown.DropdownPresets.FivePixels)
            }
            DropdownInput.NoInput -> {
                if (intakeNoodleTarget == Intake.CollectorPowers.Intake) {
                    Dropdown.DropdownTarget(Dropdown.DropdownPresets.TwoPixels)
                } else {
                    Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up)
                }
            }
            DropdownInput.Manual -> {
                Dropdown.DropdownTarget(power = driverInput.dropdownPositionOverride, movementMode = DualMovementModeSubsystem.MovementMode.Power, targetPosition = previousTargetState.targetRobot.collectorTarget.dropDown.targetPosition)
            }
        }

        /**Handoff*/
        val handoffButtonPressed = driverInput.handoff == RobotTwoTeleOp.HandoffInput.Handoff
        val doHandoffSequence: Boolean = if (handoffButtonPressed) {
            true
        } else {
            val extendoInputConflictsWithAutoHandoff = driverInput.extendo == ExtendoInput.ExtendManual
//            val depoInputConflictsWithAutoHandoff = !(driverInput.depo == DepoInput.Down || driverInput.depo == DepoInput.NoInput)
//            val inputsConflictWithTransfer = depoInputConflictsWithAutoHandoff || extendoInputConflictsWithAutoHandoff

            when {
                extendoInputConflictsWithAutoHandoff -> {
                    false
                }
                theRobotJustCollectedTwoPixels -> {
                    telemetry.addLine("Auto starting handoff")
                    true
                }
                else -> {
                    previousTargetState.doingHandoff
                }
            }
        }

        val repeatDriverInputForDepo = driverInput.copy(
            depo = when {
                handoffButtonPressed -> RobotTwoTeleOp.DepoInput.Down
                driverInput.depo == DepoInput.NoInput -> {
                    if (driverInput.bumperMode == Gamepad1BumperMode.Collector && previousTargetState.driverInput.bumperMode == Gamepad1BumperMode.Claws) {
                        DepoInput.Down
                    } else {
                        previousTargetState.driverInput.depo
                    }
                }
                else -> driverInput.depo
            }
        )

//        Handoff Coordination
        val driverInputIsManual = driverInput.depo == DepoInput.Manual
        val overrideHandoff = driverInputIsManual || driverInput.gamepad1ControlMode == GamepadControlMode.Manual
        val combinedTarget = if (overrideHandoff) {

            fun latchInputToLatchPosition(latchInput: LatchInput): Transfer.LatchPositions =
                when (latchInput) {
                    LatchInput.Open -> Transfer.LatchPositions.Open
                    LatchInput.NoInput -> Transfer.LatchPositions.Closed
                }

            val driverInputWrist = Wrist.WristTargets(
                left = driverInput.wrist.left.toClawTarget()
                    ?: previousTargetState.targetRobot.depoTarget.wristPosition.left,
                right = driverInput.wrist.right.toClawTarget()
                    ?: previousTargetState.targetRobot.depoTarget.wristPosition.right
            )

            HandoffManager.CollectorDepositorTarget(
                depo = DepoTarget(
                    lift = Lift.TargetLift(
                        power = driverInput.depoScoringHeightAdjust,
                        movementMode = DualMovementModeSubsystem.MovementMode.Power,
                        targetPosition = previousTargetState.targetRobot.depoTarget.lift.targetPosition
                    ),
                    armPosition = Arm.ArmTarget(
                        power = driverInput.armOverridePower,
                        movementMode = DualMovementModeSubsystem.MovementMode.Power,
                        targetPosition = previousTargetState.targetRobot.depoTarget.armPosition.targetPosition
                    ),
                    wristPosition = driverInputWrist,
                    targetType = DepoManager.DepoTargetType.Manual
                ),
                collector = CollectorTarget(
                    intakeNoodles = when (driverInput.collector) {
                        CollectorInput.Intake -> Intake.CollectorPowers.Intake
                        CollectorInput.Eject -> Intake.CollectorPowers.Eject
                        CollectorInput.Off -> Intake.CollectorPowers.Off
                        CollectorInput.NoInput -> previousTargetState.targetRobot.collectorTarget.intakeNoodles
                    },
                    dropDown = dropdownTarget,
                    timeOfEjectionStartMilis = 0,
                    timeOfTransferredMillis = 0,
                    transferSensorState = transferState,
                    latches = Transfer.TransferTarget(
                        left = Transfer.LatchTarget(
                            target = latchInputToLatchPosition(driverInput.leftLatch), 0
                        ),
                        right = Transfer.LatchTarget(
                            target = latchInputToLatchPosition(driverInput.rightLatch), 0
                        ),
                    ),
                    extendo = when (driverInput.extendo) {
                        RobotTwoTeleOp.ExtendoInput.ExtendManual -> {
                            Extendo.ExtendoTarget(
                                targetPosition = Extendo.ExtendoPositions.Min,
                                movementMode = DualMovementModeSubsystem.MovementMode.Power,
                                power = driverInput.extendoManualPower)
                        }
                        ExtendoInput.RetractManual -> {
                            Extendo.ExtendoTarget(
                                targetPosition = Extendo.ExtendoPositions.Min,
                                movementMode = DualMovementModeSubsystem.MovementMode.Power,
                                power = driverInput.extendoManualPower)
                        }
                        ExtendoInput.RetractSetAmount -> {
                            Extendo.ExtendoTarget(
                                targetPosition = Extendo.ExtendoPositions.Min,
                                movementMode = DualMovementModeSubsystem.MovementMode.Power,
                                power = -0.7)
                        }
                        ExtendoInput.NoInput -> {
                            Extendo.ExtendoTarget(
                                targetPosition = Extendo.ExtendoPositions.Min,
                                movementMode = DualMovementModeSubsystem.MovementMode.Power,
                                power = 0.0)
                        }
                    },
                )
            )
        } else {
            val previousExtendoTargetPosition = previousTargetState.targetRobot.collectorTarget.extendo.targetPosition
            val extendoTargetState: Extendo.ExtendoTarget = when (driverInput.extendo) {
                ExtendoInput.ExtendManual -> {
                    Extendo.ExtendoTarget(
                        targetPosition = previousExtendoTargetPosition,
                        movementMode = DualMovementModeSubsystem.MovementMode.Power,
                        power = driverInput.extendoManualPower)
                }
                ExtendoInput.RetractManual -> {
                    Extendo.ExtendoTarget(
                        targetPosition = previousExtendoTargetPosition,
                        movementMode = DualMovementModeSubsystem.MovementMode.Power,
                        power = driverInput.extendoManualPower)
                }
                ExtendoInput.RetractSetAmount -> {
                    Extendo.ExtendoTarget(
                        targetPosition = previousExtendoTargetPosition,
                        movementMode = DualMovementModeSubsystem.MovementMode.Power,
                        power = -0.5)
                }

                ExtendoInput.NoInput -> {
                    if (doHandoffSequence) {
                        val slideThinksItsAtZero = actualRobot.collectorSystemState.extendo.currentPositionTicks <= 0

                        if (slideThinksItsAtZero && !actualRobot.collectorSystemState.extendo.limitSwitchIsActivated) {
                            Extendo.ExtendoTarget(power = -extendo.findResetPower, movementMode = DualMovementModeSubsystem.MovementMode.Power, targetPosition = Extendo.ExtendoPositions.Min)
                        } else {
                            Extendo.ExtendoTarget(
                                targetPosition = Extendo.ExtendoPositions.Min,
                                movementMode = DualMovementModeSubsystem.MovementMode.Position,
                                power = 0.0)
                        }
                    } else {
                        Extendo.ExtendoTarget(
                            targetPosition = previousExtendoTargetPosition,
                            movementMode = DualMovementModeSubsystem.MovementMode.Power,
                            power = 0.0)
                    }
                }
            }

            fun getLatchTargetFromDriverInput(latchInput: LatchInput): Transfer.LatchTarget {
                return Transfer.LatchTarget(when (latchInput) {
                    LatchInput.Open -> Transfer.LatchPositions.Open
                    LatchInput.NoInput -> Transfer.LatchPositions.Closed
                }, 0)
            }

            val uncoordinatedCollectorTarget = CollectorTarget(
                intakeNoodles = intakeNoodleTarget,
                dropDown = dropdownTarget,
                timeOfEjectionStartMilis = timeOfEjectionStartMillis,
                timeOfTransferredMillis = timeOfTransferredMillis,
                transferSensorState = transferState,
                latches = Transfer.TransferTarget(
                    left = getLatchTargetFromDriverInput(driverInput.leftLatch),
                    right = getLatchTargetFromDriverInput(driverInput.rightLatch)),
                extendo = extendoTargetState,
            )

            //Left and right claws are switched for driver one
            handoffManager.manageHandoff(
                handoffInput = driverInput.handoff,
                wristInput = driverInput.wrist,
                depoInput = repeatDriverInputForDepo.depo,
                extendoInput = driverInput.extendo,
                collectorTarget = uncoordinatedCollectorTarget,
                previousTargetWorld = previousTargetState,
                actualWorld = actualWorld,
                doingHandoff = doHandoffSequence,
            )
        }

        /**Drive*/
        val driveTarget = driverInput.driveVelocity

        /**Hang*/
        val hangTarget: RobotTwoHardware.HangPowers = when (driverInput.hang) {
            HangInput.Deploy -> RobotTwoHardware.HangPowers.Release
            HangInput.NoInput -> RobotTwoHardware.HangPowers.Holding
        }

        /**Launcher*/
        val launcherTarget: RobotTwoHardware.LauncherPosition = when (driverInput.launcher) {
            LauncherInput.Shoot -> RobotTwoHardware.LauncherPosition.Released
            LauncherInput.NoInput -> RobotTwoHardware.LauncherPosition.Holding
        }

        /**Lights*/
        val bothUnknownPattern = BothPixelsWeWant(PixelColor.Unknown, PixelColor.Unknown)
        val previousPattern = previousTargetState.targetRobot.lights.pattern
        val desiredPixelLightPattern: BothPixelsWeWant = when (driverInput.lightInput) {
            LightInput.NoInput -> {
                previousPattern
            }
            LightInput.NoColor -> {
                bothUnknownPattern
            }
            else -> {
                val previousWasNoInput = previousTargetState.driverInput.lightInput == LightInput.NoInput || previousTargetState.driverInput.lightInput == LightInput.NoColor
                val previousWasNotThisColor = previousTargetState.driverInput.lightInput != driverInput.lightInput
                if (previousWasNoInput && previousWasNotThisColor) {

                    val mapToSide = mapOf(  Side.Left to previousPattern.leftPixel,
                        Side.Right to previousPattern.rightPixel)

                    val side = mapToSide.entries.fold(Side.Left) {acc, (side, it) ->
                        if (it == PixelColor.Unknown) {
                            side
                        } else {
                            acc
                        }
                    }

                    val color = when (driverInput.lightInput) {
                        LightInput.White -> PixelColor.White
                        LightInput.Yellow -> PixelColor.Yellow
                        LightInput.Purple -> PixelColor.Purple
                        LightInput.Green -> PixelColor.Green
                        else -> PixelColor.Unknown
                    }

                    when (side) {
                        Side.Left -> previousPattern.copy(leftPixel = color)
                        Side.Right -> previousPattern.copy(rightPixel = color)
                    }
                } else {
                    previousPattern
                }
            }
        }

        val timeBeforeEndOfMatchToStartEndgameSeconds = 15.0
        val matchTimeSeconds = 2.0 * 60.0
        val timeSinceStartOfMatchToStartEndgameSeconds = matchTimeSeconds - timeBeforeEndOfMatchToStartEndgameSeconds
        val timeSinceStartOfMatchMilis = actualWorld.timestampMilis - actualWorld.timeOfMatchStartMillis
        val timeSinceStartOfMatchSeconds = timeSinceStartOfMatchMilis / 1000

        val timeToStartEndgame = timeSinceStartOfMatchSeconds >= timeSinceStartOfMatchToStartEndgameSeconds

        val colorToDisplay =  if (timeToStartEndgame) {
            Neopixels.HalfAndHalfTarget(Neopixels.NeoPixelColors.Red).compileStripState()
        } else {
            desiredPixelLightPattern.toStripState()
        }

        val lights = RobotTwoTeleOp.LightTarget(desiredPixelLightPattern, colorToDisplay)

        return TargetWorld(
            targetRobot = TargetRobot(
                drivetrainTarget = Drivetrain.DrivetrainTarget(
                    power = driveTarget,
                    movementMode = DualMovementModeSubsystem.MovementMode.Power,
                    targetPosition = PositionAndRotation()
                ),
                depoTarget = combinedTarget.depo,
                collectorTarget = combinedTarget.collector,
                hangPowers = hangTarget,
                launcherPosition = launcherTarget,
                lights = lights,
            ),
            doingHandoff = doHandoffSequence,
            driverInput = repeatDriverInputForDepo,
            getNextTask = null,
            gamepad1Rumble = RobotTwoTeleOp.RumbleEffects.Throb
        )
    }

    lateinit var stateDumper: StateDumper
    lateinit var statsDumper:StatsDumper
    lateinit var drivetrain: Drivetrain
    fun initRobot(hardware: RobotTwoHardware) {
        FtcRobotControllerActivity.instance?.let{ controller ->
            statsDumper = StatsDumper(reportingIntervalMillis = 1000, controller)
            statsDumper.start()
        }
        stateDumper = StateDumper(reportingIntervalMillis = 1000, functionalReactiveAutoRunner)
        stateDumper.start()

        drivetrain = Drivetrain(hardware, FauxLocalizer(), telemetry)

        for (module in hardware.allHubs) {
            module.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
        }
    }
    var getTime:()->Long = {System.currentTimeMillis()}

    val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    val loopTimeMeasurer = DeltaTimeMeasurer()

    fun getActualState(previousActualState:ActualWorld?, gamepad1: SerializableGamepad, gamepad2: SerializableGamepad, hardware: RobotTwoHardware):ActualWorld{
        val (currentGamepad1, currentGamepad2)  = measured("gamepad copies"){
//            val currentGamepad1 = Gamepad()
//            currentGamepad1.copy(gamepad1)
//            val currentGamepad2 = Gamepad()
//            currentGamepad2.copy(gamepad2)
            gamepad1 to gamepad2
        }

        val actualRobot = hardware.getActualState(
            drivetrain= drivetrain,
            depoManager = depoManager,
            collectorSystem = collectorSystem,
            previousActualWorld= previousActualState,
        )
        telemetry.addLine("lift: ${actualRobot.depoState.lift}")

        return ActualWorld(
            actualRobot = actualRobot,
            actualGamepad1 = currentGamepad1,
            actualGamepad2 = currentGamepad2,
            timestampMilis = getTime(),
            timeOfMatchStartMillis = previousActualState?.timeOfMatchStartMillis ?: getTime()
        )
    }

    fun getTargetWorldFromDriverInput(
        getDriverInput: (actualWorld: ActualWorld,
                         previousActualWorld: ActualWorld,
                         previousTargetState: TargetWorld) -> DriverInput,
        actualState: ActualWorld,
        previousActualState: ActualWorld?,
        previousTargetState: TargetWorld?,
    ): TargetWorld {

        val previousActualState = previousActualState ?: TeleopTest.emptyWorld
        val previousTargetState: TargetWorld = previousTargetState ?: initialPreviousTargetState
        val driverInput = getDriverInput(actualState, previousActualState, previousTargetState)
        val targetWorld = getTargetWorld(driverInput= driverInput, previousTargetState= previousTargetState, actualWorld= actualState, previousActualWorld= previousActualState)
        if (actualState.actualGamepad1.touchpad && !previousActualState.actualGamepad1.touchpad) {
            saveStateSnapshot(actualState, previousActualState, targetWorld, previousTargetState)
        }
        return targetWorld
    }

    fun runRobot(
        targetStateFetcher: (actualState: ActualWorld, previousActualState: ActualWorld?, previousTargetState: TargetWorld?)->TargetWorld,
//        (actualWorld: ActualWorld, previousActualWorld: ActualWorld, previousTargetState: TargetWorld, )->RobotTwoTeleOp.DriverInput,
        gamepad1: SerializableGamepad,
        gamepad2: SerializableGamepad,
        hardware: RobotTwoHardware
    ) = measured("main loop"){

        measured("clear bulk cache"){
            for (hub in hardware.allHubs) {
                hub.clearBulkCache()
            }
        }

        functionalReactiveAutoRunner.loop(
            actualStateGetter = {getActualState(it, gamepad1, gamepad2, hardware)},
            targetStateFetcher = targetStateFetcher,
            stateFulfiller = { targetState, previousTargetState, actualState, previousActualState ->
                val previousActualState = previousActualState ?: actualState
                measured("actuate robot"){
                    hardware.actuateRobot(
                        targetState,
                        previousTargetState ?: targetState,
                        actualState,
                        previousActualWorld = previousActualState,
                        drivetrain = drivetrain,
                        wrist= wrist,
                        arm= arm,
                        lift= lift,
                        extendo= extendo,
                        intake= intake,
                        dropdown = dropdown,
                        transfer= transfer
                    )
                }
                measured("rumble"){
                    if (targetState.gamepad1Rumble != null) {
                        if (!gamepad1.isRumbling) {
                            gamepad1.runRumbleEffect(targetState.gamepad1Rumble.effect)
                        }
                        if (!gamepad2.isRumbling) {
                            gamepad2.runRumbleEffect(targetState.gamepad1Rumble.effect)
                        }
                    }
                }
            }
        )
        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()

        measured("telemetry"){
            telemetry.addLine("loop time: $loopTime milis")
            telemetry.addLine("peak loop time: ${loopTimeMeasurer.peakDeltaTime()} milis")

            measured("expensiveTelemetryLines-addLine"){
                stateDumper.lines().forEach(telemetry::addLine)
            }

            telemetry.update()
        }
    }


    private var numberOfSnapshotsMade = 0
    private fun saveStateSnapshot(actualWorld: ActualWorld, previousActualWorld: ActualWorld?, targetWorld: TargetWorld, previousActualTarget: TargetWorld?) {

        val file = File("/storage/emulated/0/Download/stateSnapshot$numberOfSnapshotsMade.json")
        file.createNewFile()
        if (file.exists() && file.isFile) {
            numberOfSnapshotsMade++

            telemetry.clearAll()
            telemetry.addLine("Saving snapshot to: ${file.absolutePath}")

            val jsonEncoded = CompleteSnapshot(
                actualWorld = actualWorld,
                previousActualWorld = previousActualWorld,
                targetWorld = targetWorld,
                previousActualTarget = previousActualTarget,
            ).toJson()

            println("SAVING SNAPSHOT $numberOfSnapshotsMade: ${jsonEncoded}")
            file.printWriter().use {
                it.print(jsonEncoded)
            }
            Thread.sleep(1000)
        }
    }
}


data class CompleteSnapshot(
    val actualWorld: ActualWorld,
    val previousActualWorld: ActualWorld?,
    val targetWorld: TargetWorld,
    val previousActualTarget: TargetWorld?,
){
    fun toJson() = jacksonObjectMapper().writeValueAsString(this)
}