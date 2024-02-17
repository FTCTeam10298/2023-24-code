package us.brainstormz.robotTwo

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.Gamepad.RumbleEffect
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.FauxLocalizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.robotTwo.DepoManager.*
import us.brainstormz.utils.DeltaTimeMeasurer
import us.brainstormz.robotTwo.subsystems.Claw.ClawTarget
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.robotTwo.subsystems.Wrist.WristTargets
import us.brainstormz.utils.DataClassHelper
import kotlin.math.abs
import kotlin.math.absoluteValue

class RobotTwoTeleOp(private val telemetry: Telemetry) {
    val intake = Intake()
    val transfer = Transfer(telemetry)
    val extendo = Extendo()
    val collectorSystem: CollectorSystem = CollectorSystem(transfer= transfer, extendo= extendo, telemetry= telemetry)
    val leftClaw: Claw = Claw(telemetry)
    val rightClaw: Claw = Claw(telemetry)
    val wrist = Wrist(leftClaw, rightClaw, telemetry= telemetry)
    val arm: Arm = Arm()
    val lift: Lift = Lift(telemetry)
    val depoManager: DepoManager = DepoManager(arm= arm, lift= lift, wrist= wrist, telemetry= telemetry)
    val handoffManager: HandoffManager = HandoffManager(collectorSystem, lift, extendo, arm, telemetry)

    enum class RumbleEffects(val effect: RumbleEffect) {
        TwoTap(RumbleEffect.Builder().addStep(1.0, 1.0, 400).addStep(0.0, 0.0, 200).addStep(1.0, 1.0, 400).build()),//.addStep(0.0, 0.0, 0)
        OneTap(RumbleEffect.Builder().addStep(1.0, 1.0, 800).build())//.addStep(0.0, 0.0, 200),
    }

    enum class LiftControlMode {
        Adjust,
        Override
    }
    enum class Gamepad1BumperMode {
        Collector,
        Claws
    }
    enum class DepoInput {
        SetLine1,
        SetLine2,
        SetLine3,
        ScoringHeightAdjust,
        Down,
        Manual,
        NoInput
    }
    data class WristInput(val left: ClawInput, val right: ClawInput) {
        val bothClaws = mapOf(Transfer.Side.Left to left, Transfer.Side.Right to right)
    }
    enum class ClawInput {
        Drop,
        Hold,
        NoInput;
        fun toClawTarget(): ClawTarget? {
            return when (this) {
                Drop -> ClawTarget.Retracted
                Hold -> ClawTarget.Gripping
                NoInput -> null
            }
        }
    }
    enum class CollectorInput {//Need to change this to be accurate
        Intake,
        Eject,
        Off,
        NoInput
    }
    enum class RollerInput {
        BothIn,
        BothOut,
        LeftOut,
        RightOut,
        NoInput
    }
    enum class ExtendoInput {
        Extend,
        Retract,
        NoInput,
    }
    enum class HangInput {
        Deploy,
        NoInput,
    }
    enum class LauncherInput {
        Shoot,
        NoInput,
    }
    enum class HandoffInput {
        StartHandoff,
        NoInput
    }
    enum class LightInput {
        White,
        Yellow,
        Purple,
        Green,
        NoColor,
        NoInput
    }
    data class DriverInput (
            val bumperMode: Gamepad1BumperMode,
            val lightInput: LightInput,
            val depo: DepoInput,
            val depoScoringHeightAdjust: Double,
            val wrist: WristInput,
            val collector: CollectorInput,
            val extendo: ExtendoInput,
            val hang: HangInput,
            val launcher: LauncherInput,
            val handoff: HandoffInput,
            val rollers: RollerInput,
            val driveVelocity: Drivetrain.DrivetrainPower
    ) {
        override fun toString(): String = DataClassHelper.dataClassToString(this)
    }
    fun getDriverInput(actualWorld: ActualWorld, previousActualWorld: ActualWorld, previousTargetState: TargetWorld): DriverInput {
        val gamepad1 = actualWorld.actualGamepad1
        val gamepad2 = actualWorld.actualGamepad2
        val robot = actualWorld.actualRobot
        val previousGamepad1 = previousActualWorld.actualGamepad1
        val previousGamepad2 = previousActualWorld.actualGamepad2
        val previousRobot = previousActualWorld.actualRobot
        val previousRobotTarget = previousTargetState.targetRobot

        /**Depo*/
        val depoGamepad2Input: DepoInput? = when {
            gamepad2.dpad_up-> {
                DepoInput.SetLine3
            }
            gamepad2.dpad_down -> {
                DepoInput.Down
            }
            gamepad2.dpad_right && !previousGamepad2.dpad_right -> {
                if (previousRobotTarget.depoTarget.lift.targetPosition != Lift.LiftPositions.SetLine1) {
                    DepoInput.SetLine1
                } else {
                    DepoInput.SetLine2
                }
            }
            else -> null
        }
        val depoGamepad1Input: DepoInput = when {
            gamepad1.dpad_up-> {
                DepoInput.SetLine3
            }
            gamepad1.dpad_left -> {
                DepoInput.SetLine2
            }
            gamepad1.dpad_down -> {
                DepoInput.SetLine1
            }
            else -> DepoInput.NoInput
        }

        val dpadInput: DepoInput = depoGamepad2Input ?: depoGamepad1Input

        val liftControlMode = when (previousTargetState.targetRobot.depoTarget.lift.targetPosition) {
            Lift.LiftPositions.BackboardBottomRow -> LiftControlMode.Adjust
            Lift.LiftPositions.SetLine1 -> LiftControlMode.Adjust
            Lift.LiftPositions.SetLine2 -> LiftControlMode.Adjust
            Lift.LiftPositions.SetLine3 -> LiftControlMode.Adjust
            else -> {
                val isEnumTarget = Lift.LiftPositions.entries.contains(previousTargetState.targetRobot.depoTarget.lift.targetPosition)
                if (!isEnumTarget) {
                    LiftControlMode.Adjust
                } else {
                    LiftControlMode.Override
                }
            }
        }
        telemetry.addLine("liftControlMode: $liftControlMode")

        val liftStickInput = -gamepad2.right_stick_y.toDouble()
        val isLiftControlActive = liftStickInput.absoluteValue > 0.2

        val isLiftManualOverrideActive = isLiftControlActive && liftControlMode == LiftControlMode.Override
        val dpadAdjustIsActive = isLiftControlActive && liftControlMode == LiftControlMode.Adjust

        val armOverrideStickValue = gamepad2.right_stick_x.toDouble()
        val armManualOverrideActivationThreshold = when {
            isLiftManualOverrideActive -> {
                0.2
            }
            dpadAdjustIsActive -> {
                0.4
            }
            else -> {
                0.2
            }
        }
        val isArmManualOverrideActive = armOverrideStickValue.absoluteValue >= armManualOverrideActivationThreshold

        val depoInput = if (isLiftManualOverrideActive || isArmManualOverrideActive) {
            DepoInput.Manual
        } else if (dpadAdjustIsActive || (previousTargetState.driverInput.depo == DepoInput.ScoringHeightAdjust && dpadInput == DepoInput.NoInput)) {
            DepoInput.ScoringHeightAdjust
        } else {
            dpadInput
        }

        val liftVariableInput = if (depoInput == DepoInput.ScoringHeightAdjust) {
            //Ticks to go to
//            val previousLiftTargetWasCustom = Lift.LiftPositions.entries.contains(previousRobotTarget.depoTarget.lift.targetPosition)
//            val liftPositionToAdjustOffOf: Double = if (!previousLiftTargetWasCustom) {
//                previousRobotTarget.depoTarget.lift.targetPosition.ticks.toDouble()
//            } else {
//                previousRobotTarget.depoTarget.lift.targetPosition.ticks.toDouble()
//            }
            val liftPositionToAdjustOffOf: Double = previousRobotTarget.depoTarget.lift.targetPosition.ticks.toDouble()
            val maxLiftAdjustSpeedTicksPerSecond: Double = 900.0
            val maxLiftAdjustSpeedTicksPerMili: Double = maxLiftAdjustSpeedTicksPerSecond/1000.0
            val timeSinceLastOpportunityToMoveLiftMilis = actualWorld.timestampMilis - previousActualWorld.timestampMilis
            val maxLiftAdjustTicks = maxLiftAdjustSpeedTicksPerMili * timeSinceLastOpportunityToMoveLiftMilis

            telemetry.addLine("liftPositionToAdjustOffOf: $liftPositionToAdjustOffOf")
            telemetry.addLine("timeSinceLastOpportunityToMoveLiftMilis: $timeSinceLastOpportunityToMoveLiftMilis")
            telemetry.addLine("maxLiftAdjustTicks: $maxLiftAdjustTicks")
            telemetry.addLine("liftStickInput: $liftStickInput")

            val depoScoringHeightTicks = (liftPositionToAdjustOffOf + (liftStickInput * maxLiftAdjustTicks)).coerceIn(Lift.LiftPositions.Down.ticks.toDouble()..Lift.LiftPositions.Max.ticks.toDouble())
            telemetry.addLine("depoScoringHeightTicks: $depoScoringHeightTicks")

            depoScoringHeightTicks
        } else {
            //Power to set
            liftStickInput
        }


        /**Bumper Mode*/
        val gamepad1DpadIsActive = depoGamepad1Input != DepoInput.NoInput
        val gamepad2DpadIsActive = depoGamepad2Input != null
        val liftTargetIsDown = previousRobotTarget.depoTarget.lift.targetPosition == Lift.LiftPositions.Down
        val bothClawsAreRetracted = wrist.wristIsAtPosition(WristTargets(both= ClawTarget.Retracted), actualWorld.actualRobot.depoState.wristAngles)

        telemetry.addLine("gamepad2DpadIsActive: $gamepad2DpadIsActive")
        telemetry.addLine("liftTargetIsDown: $liftTargetIsDown")
        telemetry.addLine("bothClawsAreRetracted: $bothClawsAreRetracted")

        val gamepadOneBumperMode: Gamepad1BumperMode = when {
            gamepad1DpadIsActive -> {
                Gamepad1BumperMode.Claws
            }
            gamepad2DpadIsActive || bothClawsAreRetracted -> {
                Gamepad1BumperMode.Collector
            }
            else -> {
                previousTargetState.driverInput.bumperMode
            }
        }

        /**Claws*/
        val areGamepad1ClawControlsActive = gamepadOneBumperMode == Gamepad1BumperMode.Claws

        val gamepad1LeftClawToggle = areGamepad1ClawControlsActive && gamepad1.left_bumper && !previousGamepad1.left_bumper
        val gamepad2LeftClawToggle = gamepad2.left_bumper && !previousGamepad2.left_bumper
        val leftClaw: ClawInput = if (gamepad2LeftClawToggle || gamepad1LeftClawToggle) {
            when (previousTargetState.targetRobot.depoTarget.wristPosition.left) {
                ClawTarget.Gripping -> ClawInput.Drop
                ClawTarget.Retracted -> ClawInput.Hold
            }
        } else {
            ClawInput.NoInput
        }

        val gamepad1RightClawToggle = areGamepad1ClawControlsActive && gamepad1.right_bumper && !previousGamepad1.right_bumper
        val gamepad2RightClawToggle = gamepad2.right_bumper && !previousGamepad2.right_bumper
        val rightClaw: ClawInput = if (gamepad2RightClawToggle || gamepad1RightClawToggle) {
            when (previousTargetState.targetRobot.depoTarget.wristPosition.right) {
                Claw.ClawTarget.Gripping -> ClawInput.Drop
                Claw.ClawTarget.Retracted -> ClawInput.Hold
            }
        } else {
            ClawInput.NoInput
        }

        /**Collector*/
        fun nextPosition(isDirectionPositive: Boolean): CollectorInput {
            val intakePowerOptions = mapOf(
                    1 to CollectorInput.Intake,
                    0 to CollectorInput.Off,
                    -1 to CollectorInput.Eject
            )
            val previousPowerInt: Int = previousRobotTarget.collectorTarget.intakeNoodles.power.toInt()

            val valueToChangeBy = if (isDirectionPositive) {
                1
            } else {
                -1
            }
            val nonRangedChange = previousPowerInt + valueToChangeBy
            val newPowerOption = if (nonRangedChange !in -1..1) {
                0
            } else {
                nonRangedChange
            }

            return intakePowerOptions[newPowerOption] ?: CollectorInput.NoInput
        }

        val inputCollectorStateSystem: CollectorInput = if (gamepadOneBumperMode == Gamepad1BumperMode.Collector) {
            when {
                gamepad1.right_bumper && !previousGamepad1.right_bumper -> {
                    nextPosition(true)
                }

                gamepad1.left_bumper && !previousGamepad1.left_bumper -> {
                    nextPosition(false)
                }
                else -> {
                    CollectorInput.NoInput
                }
            }
        } else {
            CollectorInput.NoInput
        }

        /**Extendo*/
        val extendoTriggerActivation = 0.1
        val rightTrigger: Boolean = gamepad1.right_trigger > extendoTriggerActivation
        val leftTrigger: Boolean = gamepad1.left_trigger > extendoTriggerActivation
        val extendo = when {
            rightTrigger -> ExtendoInput.Extend
            leftTrigger -> ExtendoInput.Retract
            else -> ExtendoInput.NoInput
        }

        /**Handoff*/
        val isHandoffButtonPressed = (gamepad2.a && !gamepad2.dpad_left) || (gamepad1.a && !gamepad1.start)
        val handoff = when {
            isHandoffButtonPressed -> HandoffInput.StartHandoff
            else -> HandoffInput.NoInput
        }

        /**Rollers*/
        val rollers = when {
            gamepad1.b && !previousGamepad1.b-> {
                if (previousTargetState.driverInput.rollers != RollerInput.BothIn) {
                    RollerInput.BothIn
                } else {
                    RollerInput.NoInput
                }
            }
            gamepad1.right_stick_button && gamepad1.left_stick_button -> {
                RollerInput.BothOut
            }
            gamepad1.right_stick_button -> {
                RollerInput.RightOut
            }
            gamepad1.left_stick_button -> {
                RollerInput.LeftOut
            }
            else -> {
                if (previousTargetState.driverInput.rollers == RollerInput.BothIn) {
                    RollerInput.BothIn
                } else {
                    RollerInput.NoInput
                }
            }
        }

        /**Hang*/
        val hang = if (gamepad2.left_stick_button && gamepad2.right_stick_button) {
            HangInput.Deploy
        } else {
            HangInput.NoInput
        }

        /**Launcher*/
        val launcher = if ((gamepad2.y && !gamepad2.dpad_left) || gamepad1.y) {
            LauncherInput.Shoot
        } else {
            LauncherInput.NoInput
        }

        /**Drive*/
        val yInput = -gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = gamepad1.right_stick_x.toDouble()

        // Strafe without turing for depositing
        val xSlowDowMultiplier = 1.0
        val driver2XInput = if (xInput == 0.0) {
            (gamepad2.left_trigger - gamepad2.right_trigger) * xSlowDowMultiplier
        } else {
            0.0
        }
        val ySlowDowMultiplier: Double = (2.0)/(3.0)
        val driver2YInput = if (yInput in -0.1..0.1) {
            gamepad2.left_stick_y.toDouble() * ySlowDowMultiplier
        } else {
            0.0
        }

        val isAtTheEndOfExtendo = actualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks >= Extendo.ExtendoPositions.Max.ticks || actualWorld.actualRobot.collectorSystemState.extendo.currentAmps > 6.0
        val extendoCompensationPower = if (isAtTheEndOfExtendo && yInput == 0.0) {
            gamepad1.right_trigger.toDouble()
        } else {
            0.0
        }

        val y = yInput + extendoCompensationPower + driver2YInput
        val x = xInput + driver2XInput
        val r = -rInput * abs(rInput)
        val driveVelocity = Drivetrain.DrivetrainPower(x= x, y= y, r= r)

        /**Lights*/
        val previousIsAnyColorButtonPressed = previousGamepad2.a || previousGamepad2.b || previousGamepad2.x || previousGamepad2.y

        val lightColor = if (gamepad2.dpad_left) {
            if (!previousGamepad2.dpad_left) {
                LightInput.NoColor
            } else if (!previousIsAnyColorButtonPressed) {
                when {
                    gamepad2.a -> {
                        LightInput.Green
                    }
                    gamepad2.b -> {
                        LightInput.White
                    }
                    gamepad2.x -> {
                        LightInput.Purple
                    }
                    gamepad2.y -> {
                        LightInput.Yellow
                    }
                    else -> {
                        LightInput.NoInput
                    }
                }
            } else {
                LightInput.NoInput
            }
        } else {
            LightInput.NoInput
        }

        return DriverInput(
                driveVelocity = driveVelocity,
                depo = depoInput,
                depoScoringHeightAdjust = liftVariableInput,
                wrist = WristInput(leftClaw, rightClaw),
                collector = inputCollectorStateSystem,
                rollers = rollers,
                extendo = extendo,
                handoff = handoff,
                hang = hang,
                launcher = launcher,
                bumperMode = gamepadOneBumperMode,
                lightInput = lightColor
        )
    }

    enum class PixelColor(val blinkinPattern: RevBlinkinLedDriver.BlinkinPattern) {
        White   (RevBlinkinLedDriver.BlinkinPattern.WHITE),
        Green   (RevBlinkinLedDriver.BlinkinPattern.GREEN),
        Purple  (RevBlinkinLedDriver.BlinkinPattern.BLUE_VIOLET),
        Yellow  (RevBlinkinLedDriver.BlinkinPattern.YELLOW),
        Unknown (RevBlinkinLedDriver.BlinkinPattern.BLUE),
    }
    data class BothPixelsWeWant(val leftPixel: PixelColor, val rightPixel: PixelColor) {
        val asList: List<PixelColor> = listOf(leftPixel, rightPixel)

        override fun equals(other: Any?): Boolean {
            return if (other is BothPixelsWeWant) {
                asList.mapIndexed { i, it ->
                    other.asList[i] == it
                }.fold(true) {acc, it -> acc && it}
            } else {
                false
            }
        }
    }
    data class LightTarget(val targetColor: RevBlinkinLedDriver.BlinkinPattern, val pattern: BothPixelsWeWant, val timeOfColorChangeMilis: Long)
    fun getTargetWorld(driverInput: DriverInput, actualWorld: ActualWorld, previousActualWorld: ActualWorld, previousTargetState: TargetWorld): TargetWorld {
        val actualRobot = actualWorld.actualRobot

        /**Handoff*/
        val transferLeftSensorState = transfer.isPixelIn(actualWorld.actualRobot.collectorSystemState.leftTransferState, Transfer.Side.Left)
        val transferRightSensorState = transfer.isPixelIn(actualWorld.actualRobot.collectorSystemState.rightTransferState, Transfer.Side.Right)
        val areBothPixelsIn = transferLeftSensorState && transferRightSensorState

        val previousTransferLeftSensorState = transfer.isPixelIn(previousActualWorld.actualRobot.collectorSystemState.leftTransferState, Transfer.Side.Left)
        val previousTransferRightSensorState = transfer.isPixelIn(previousActualWorld.actualRobot.collectorSystemState.rightTransferState, Transfer.Side.Right)
        val wereBothPixelsInPreviously = previousTransferLeftSensorState && previousTransferRightSensorState

        val theRobotJustCollectedTwoPixels = areBothPixelsIn && !wereBothPixelsInPreviously

        val weWantToStartHandoff = driverInput.handoff == HandoffInput.StartHandoff || theRobotJustCollectedTwoPixels

        val inputsConflictWithTransfer = driverInput.extendo == ExtendoInput.Extend || (driverInput.depo == DepoInput.Manual)

        val doHandoffSequence: Boolean = when {
            inputsConflictWithTransfer -> {
                false
            }
            weWantToStartHandoff -> {
                telemetry.addLine("Starting handoff")
                true
            }
            else -> {
                previousTargetState.doingHandoff
            }
        }
        val handoffIsReadyCheck = handoffManager.checkIfHandoffIsReady(actualWorld, previousActualWorld)
        telemetry.addLine("doHandoffSequence: $doHandoffSequence")
        telemetry.addLine("handoffIsReadyCheck: $handoffIsReadyCheck")

        /**Intake Noodles*/
        val timeSinceEjectionStartedMilis: Long = actualWorld.timestampMilis - (previousTargetState.targetRobot.collectorTarget.timeOfEjectionStartMilis?:actualWorld.timestampMilis)
        val timeToStopEjecting = timeSinceEjectionStartedMilis > 1000
        val wasPreviouslyEjecting = previousTargetState.targetRobot.collectorTarget.intakeNoodles == Intake.CollectorPowers.Eject
//        val intakeNoodleTarget = intake.getCollectorState(
//                driverInput = if (theRobotJustCollectedTwoPixels) {
//                    Intake.CollectorPowers.Eject
//                } else if (timeToStopEjecting && wasPreviouslyEjecting && doHandoffSequence) {
//                    Intake.CollectorPowers.Off
//                } else {
//                    when (driverInput.collector) {
//                        CollectorInput.Intake -> Intake.CollectorPowers.Intake
//                        CollectorInput.Eject -> Intake.CollectorPowers.Eject
//                        CollectorInput.Off -> Intake.CollectorPowers.Off
//                        CollectorInput.NoInput -> previousTargetState.targetRobot.collectorTarget.intakeNoodles
//                    }
//                },
//                isPixelInLeft = transferLeftSensorState,
//                isPixelInRight= transferRightSensorState)
        val stopAutomaticEjection = timeToStopEjecting && wasPreviouslyEjecting && doHandoffSequence
        val intakeNoodleTarget = if (theRobotJustCollectedTwoPixels) {
                    Intake.CollectorPowers.Eject
                } else if (stopAutomaticEjection) {
                    Intake.CollectorPowers.Off
                } else {
                    when (driverInput.collector) {
                        CollectorInput.Intake -> Intake.CollectorPowers.Intake
                        CollectorInput.Eject -> Intake.CollectorPowers.Eject
                        CollectorInput.Off -> Intake.CollectorPowers.Off
                        CollectorInput.NoInput -> previousTargetState.targetRobot.collectorTarget.intakeNoodles
                    }
                }
        val timeOfEjectionStartMilis = if (theRobotJustCollectedTwoPixels) {
            actualWorld.timestampMilis
        } else if (stopAutomaticEjection) {
            null
        } else {
            previousTargetState.targetRobot.collectorTarget.timeOfEjectionStartMilis
        }

        /**Rollers */
        val autoRollerState = transfer.getAutoPixelSortState(isCollecting = intakeNoodleTarget == Intake.CollectorPowers.Intake, actualRobot)
        val rollerTargetState = when (driverInput.rollers) {
            RollerInput.BothIn -> autoRollerState.copy(leftServoCollect = Transfer.RollerPowers.Intake, rightServoCollect = Transfer.RollerPowers.Intake)
            RollerInput.BothOut -> autoRollerState.copy(leftServoCollect = Transfer.RollerPowers.Eject, rightServoCollect = Transfer.RollerPowers.Eject)
            RollerInput.LeftOut -> autoRollerState.copy(leftServoCollect = Transfer.RollerPowers.Eject)
            RollerInput.RightOut -> autoRollerState.copy(rightServoCollect = Transfer.RollerPowers.Eject)
            RollerInput.NoInput -> autoRollerState
        }

        /**Extendo*/
        val extendoState: SlideSubsystem.SlideTargetPosition = when (driverInput.extendo) {
            ExtendoInput.Extend -> {
                Extendo.ExtendoPositions.Manual
            }
            ExtendoInput.Retract -> {
                Extendo.ExtendoPositions.Manual
            }
            ExtendoInput.NoInput -> {
                val limitIsActivated = actualRobot.collectorSystemState.extendo.limitSwitchIsActivated
                val extendoIsAlreadyGoingIn = previousTargetState.targetRobot.collectorTarget.extendo.targetPosition == Extendo.ExtendoPositions.Min
                val extendoIsManual = previousTargetState.targetRobot.collectorTarget.extendo.targetPosition == Extendo.ExtendoPositions.Manual
                if ((extendoIsAlreadyGoingIn || extendoIsManual) && limitIsActivated) {
                    previousTargetState.targetRobot.collectorTarget.extendo.targetPosition
                } else if (doHandoffSequence) {
                    if (!limitIsActivated) {
                        Extendo.ExtendoPositions.AllTheWayInTarget
                    } else {
                        Extendo.ExtendoPositions.Min
                    }
                } else {
                    previousTargetState.targetRobot.collectorTarget.extendo.targetPosition
                }
            }
        }
        val ticksSinceLastExtendoReset = actualRobot.collectorSystemState.extendo.ticksMovedSinceReset
        telemetry.addLine("ticksSinceLastExtendoReset: $ticksSinceLastExtendoReset")

        /**Depo*/
        fun boolToClawInput(bool: Boolean): ClawInput {
            return when (bool) {
                true -> ClawInput.Hold
                false -> ClawInput.Drop
            }
        }
        val driverInputWrist = WristTargets(
                left= driverInput.wrist.left.toClawTarget() ?: previousTargetState.targetRobot.depoTarget.wristPosition.left,
                right= driverInput.wrist.right.toClawTarget() ?: previousTargetState.targetRobot.depoTarget.wristPosition.right)

        val areDepositing = previousTargetState.targetRobot.depoTarget.targetType == DepoTargetType.GoingOut

        fun isPixelInSide(side: Transfer.Side): Boolean {
            val reading = when (side) {
                Transfer.Side.Left -> actualWorld.actualRobot.collectorSystemState.leftTransferState
                Transfer.Side.Right -> actualWorld.actualRobot.collectorSystemState.rightTransferState
            }
            return transfer.isPixelIn(reading, side)
        }
        val doingHandoff = doHandoffSequence && previousTargetState.targetRobot.depoTarget.targetType != DepoTargetType.GoingOut
        val collectorIsMovingOut = extendo.getVelocityTicksPerMili(actualWorld, previousActualWorld) > 0.1
        val mapOfClawInputsToConditions: Map<ClawInput, (Transfer.Side) -> List<Boolean>> = mapOf(
                ClawInput.Hold to {side ->
                    listOf(
                            doingHandoff && handoffIsReadyCheck && isPixelInSide(Transfer.Side.entries.first{it != side} /*claws Are Flipped when down*/),
                    )
                },
                ClawInput.Drop to {side ->
                    listOf(
                            !areDepositing && intakeNoodleTarget == Intake.CollectorPowers.Intake,
                            doingHandoff && !handoffIsReadyCheck,
                            !areDepositing && collectorIsMovingOut
                    )
                },
        )
        val clawInputPerSide = Transfer.Side.entries.map { side ->
            val driverInputForThisSide = driverInput.wrist.bothClaws.entries.first {it.key == side}.value
            side to mapOfClawInputsToConditions.entries.fold(driverInputForThisSide) { acc, (clawInput, listOfConditions) ->
                val doesValueMatch: Boolean = listOfConditions(side).fold(false) {acc, it -> acc || it}
                if (doesValueMatch)
                    clawInput
                else
                    acc
            }
        }.toMap()

        telemetry.addLine("clawInputPerSide: $clawInputPerSide")

        val spoofDriverInputForDepo = driverInput.copy(
                depo = if (driverInput.depo == DepoInput.NoInput) {
                            val driverOneIsUsingTheClaws = previousTargetState.driverInput.bumperMode == Gamepad1BumperMode.Claws
                            val isWristClosedOrBeingToldToClose = driverInput.wrist.bothClaws.toList().fold(true) {acc, (side, clawInput) ->
                                acc && ((clawInput == ClawInput.Drop) || wrist.clawsAsMap[side]!!.isClawAtAngle(ClawTarget.Retracted, actualRobot.depoState.wristAngles.getBySide(side)))
                            }
                            val driverOneWantsToRetract = driverOneIsUsingTheClaws && isWristClosedOrBeingToldToClose
                            if (weWantToStartHandoff || driverOneWantsToRetract) {
                                DepoInput.Down
                            } else {
                                previousTargetState.driverInput.depo
                            }
                        } else {
                            driverInput.depo
                        },
                wrist = WristInput(clawInputPerSide[Transfer.Side.Left]!!,  clawInputPerSide[Transfer.Side.Right]!!)
        )

        val driverInputIsManual = driverInput.depo == DepoInput.Manual
        val depoWasManualLastLoop = previousTargetState.targetRobot.depoTarget.targetType == DepoTargetType.Manual
        val noAutomationTakingOver = !doingHandoff
        val depoTarget: DepoTarget = if ( (driverInputIsManual || (depoWasManualLastLoop && driverInput.depo == DepoInput.NoInput)) && noAutomationTakingOver) {
            val armPosition = Arm.Positions.Manual
            DepoTarget(
                    lift = Lift.TargetLift(power = driverInput.depoScoringHeightAdjust, movementMode = MovementMode.Power, targetPosition = previousTargetState.targetRobot.depoTarget.lift.targetPosition),
                    armPosition = armPosition,
                    wristPosition = driverInputWrist,
                    targetType = DepoTargetType.Manual
            )
        } else {
            depoManager.fullyManageDepo(
                    target= spoofDriverInputForDepo,
                    previousDepoTarget= previousTargetState.targetRobot.depoTarget,
                    actualDepo= actualRobot.depoState)
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

                    val mapToSide = mapOf(  Transfer.Side.Left to previousPattern.leftPixel,
                                            Transfer.Side.Right to previousPattern.rightPixel)

                    val side = mapToSide.entries.fold(Transfer.Side.Left) {acc, (side, it) ->
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
                        Transfer.Side.Left -> previousPattern.copy(leftPixel = color)
                        Transfer.Side.Right -> previousPattern.copy(rightPixel = color)
                    }
                } else {
                    previousPattern
                }
            }
        }

        val timeToDisplayColorMilis = 1000
        val timeWhenCurrentColorStartedBeingDisplayedMilis = previousTargetState.targetRobot.lights.timeOfColorChangeMilis
        val timeSinceCurrentColorWasDisplayedMilis = actualWorld.timestampMilis - timeWhenCurrentColorStartedBeingDisplayedMilis
        val isTimeToChangeColor = timeSinceCurrentColorWasDisplayedMilis >= timeToDisplayColorMilis

        val isCurrentColorObsolete = desiredPixelLightPattern != previousPattern

        val previousPixelToBeDisplayed = previousTargetState.targetRobot.lights.targetColor
        val currentPixelToBeDisplayed: RevBlinkinLedDriver.BlinkinPattern = when {
            isTimeToChangeColor || isCurrentColorObsolete -> {
                (desiredPixelLightPattern.asList.firstOrNull { color ->
                    color.blinkinPattern != previousPixelToBeDisplayed
                } ?: desiredPixelLightPattern.leftPixel).blinkinPattern
            }
            else -> {
                previousPixelToBeDisplayed
            }
        }

        val newTimeOfColorChangeMilis = if (currentPixelToBeDisplayed != previousPixelToBeDisplayed) {
            actualWorld.timestampMilis
        } else {
            timeWhenCurrentColorStartedBeingDisplayedMilis
        }

        val timeBeforeEndOfMatchToStartEndgameSeconds = 15.0
        val matchTimeSeconds = 2.0 * 60.0
        val timeSinceStartOfMatchToStartEndgameSeconds = matchTimeSeconds - timeBeforeEndOfMatchToStartEndgameSeconds
        val timeSinceStartOfMatchMilis = System.currentTimeMillis() - timeOfMatchStartMilis
        val timeSinceStartOfMatchSeconds = timeSinceStartOfMatchMilis / 1000

        val timeToStartEndgame = timeSinceStartOfMatchSeconds >= timeSinceStartOfMatchToStartEndgameSeconds

        val colorToDisplay =  if (timeToStartEndgame) {
            RevBlinkinLedDriver.BlinkinPattern.RED
        } else {
            currentPixelToBeDisplayed
        }

        val lights = LightTarget(
                colorToDisplay,
                desiredPixelLightPattern,
                newTimeOfColorChangeMilis
        )

        /**Rumble*/
        //Need to only trigger on rising edge
//        val gamepad1RumbleEffectToCondition: Map<RumbleEffects, List<()->Boolean>> = mapOf(
//                RumbleEffects.OneTap to listOf(
//                        { lift.isSlideSystemAllTheWayIn(actualRobot.depoState.lift) && lift.isSlideSystemAllTheWayIn(previousActualWorld.actualRobot.depoState.lift)},
//                        { theRobotJustCollectedTwoPixels },
//                ),
//                RumbleEffects.TwoTap to listOf(
//                        { handoffIsReadyCheck && !previousTargetState.doingHandoff },
//                )
//        )
//
//        val gamepad1RumbleRoutine = gamepad1RumbleEffectToCondition.toList().firstOrNull { (rumble, listOfConditions) ->
//            listOfConditions.fold(false) { acc, it -> acc || it() }
//        }?.first
        val gamepad1RumbleRoutine = null


        return TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(
                                power = driveTarget,
                                movementMode = MovementMode.Power,
                                targetPosition = PositionAndRotation()
                        ),
                        depoTarget = depoTarget,
                        collectorTarget = CollectorTarget(
                                intakeNoodles = intakeNoodleTarget,
                                timeOfEjectionStartMilis = timeOfEjectionStartMilis,
                                rollers = rollerTargetState,
                                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = extendoState, movementMode = MovementMode.Position, power = 0.0),
                        ),
                        hangPowers = hangTarget,
                        launcherPosition = launcherTarget,
                        lights = lights,
                ),
                isLiftEligableForReset = false,
                doingHandoff = doHandoffSequence,
                driverInput = spoofDriverInputForDepo,
                isTargetReached = {_, _, _-> false},
                gamepad1Rumble = gamepad1RumbleRoutine
        )
    }


    companion object {
        val noInput = DriverInput(
                driveVelocity = Drivetrain.DrivetrainPower(),
                depo = DepoInput.NoInput,
                depoScoringHeightAdjust = 0.0,
                wrist = WristInput(ClawInput.NoInput, ClawInput.NoInput),
                collector = CollectorInput.NoInput,
                rollers = RollerInput.NoInput,
                extendo = ExtendoInput.NoInput,
                handoff = HandoffInput.NoInput,
                hang = HangInput.NoInput,
                launcher = LauncherInput.NoInput,
                bumperMode = Gamepad1BumperMode.Collector,
                lightInput = LightInput.NoInput
        )

        val initDepoTarget = DepoTarget(
                lift = Lift.TargetLift(Lift.LiftPositions.Down),
                armPosition = Arm.Positions.In,
                wristPosition = WristTargets(ClawTarget.Gripping),
                targetType = DepoTargetType.GoingHome
        )

        val initialPreviousTargetState = TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(), MovementMode.Power, Drivetrain.DrivetrainPower()),
                        depoTarget = initDepoTarget,
                        collectorTarget = CollectorTarget(
                                intakeNoodles = Intake.CollectorPowers.Off,
                                timeOfEjectionStartMilis = 0,
                                rollers = Transfer.RollerState(
                                        leftServoCollect = Transfer.RollerPowers.Off,
                                        rightServoCollect = Transfer.RollerPowers.Off,
                                        directorState = Transfer.DirectorState.Off
                                ),
                                extendo = SlideSubsystem.TargetSlideSubsystem(Extendo.ExtendoPositions.Manual, MovementMode.Position),
                        ),
                        hangPowers = RobotTwoHardware.HangPowers.Holding,
                        launcherPosition = RobotTwoHardware.LauncherPosition.Holding,
                        lights = LightTarget(
                                targetColor = RevBlinkinLedDriver.BlinkinPattern.BLUE,
                                pattern = BothPixelsWeWant(PixelColor.Unknown, PixelColor.Unknown),
                                timeOfColorChangeMilis = 0L
                        ),
                ),
                isLiftEligableForReset = false,
                doingHandoff = false,
                driverInput = noInput,
                isTargetReached = { _, _, _ -> false },
                gamepad1Rumble = null
        )
    }


    lateinit var drivetrain: Drivetrain
    fun init(hardware: RobotTwoHardware) {
        drivetrain = Drivetrain(hardware, FauxLocalizer(), telemetry)

        for (module in hardware.allHubs) {
            module.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
        }
    }

    var timeOfMatchStartMilis = 0L
    fun start() {
        timeOfMatchStartMilis = System.currentTimeMillis()
    }

    val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    val loopTimeMeasurer = DeltaTimeMeasurer()
    fun loop(gamepad1: Gamepad, gamepad2: Gamepad, hardware: RobotTwoHardware) {

        for (hub in hardware.allHubs) {
            hub.clearBulkCache()
        }
        
        functionalReactiveAutoRunner.loop(
                actualStateGetter = { previousActualState ->
                    val currentGamepad1 = Gamepad()
                    currentGamepad1.copy(gamepad1)
                    val currentGamepad2 = Gamepad()
                    currentGamepad2.copy(gamepad2)
                    ActualWorld(
                            actualRobot = hardware.getActualState(drivetrain= drivetrain, depoManager = depoManager, collectorSystem = collectorSystem, previousActualWorld= previousActualState),
                            actualGamepad1 = currentGamepad1,
                            actualGamepad2 = currentGamepad2,
                            timestampMilis = System.currentTimeMillis()
                    )
                },
                targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                    telemetry.addLine("actualState: $actualState\n")
                    val previousActualState = previousActualState ?: actualState
                    val previousTargetState: TargetWorld = previousTargetState ?: initialPreviousTargetState
                    val driverInput = getDriverInput(previousTargetState= previousTargetState, actualWorld= actualState, previousActualWorld= previousActualState)
                    getTargetWorld(driverInput= driverInput, previousTargetState= previousTargetState, actualWorld= actualState, previousActualWorld= previousActualState)
                },
                stateFulfiller = { targetState, previousTargetState, actualState ->
                    telemetry.addLine("\ntargetState: $targetState")
                    hardware.actuateRobot(
                            targetState,
                            previousTargetState ?: targetState,
                            actualState,
                            drivetrain = drivetrain,
                            wrist= wrist,
                            arm= arm,
                            lift= lift,
                            extendo= extendo,
                            intake= intake,
                            transfer= transfer,
                            extendoOverridePower = (gamepad1.right_trigger.toDouble() - gamepad1.left_trigger.toDouble()),
                            armOverridePower = gamepad2.right_stick_x.toDouble()
                    )
                    if (targetState.gamepad1Rumble != null) {
                        gamepad1.runRumbleEffect(targetState.gamepad1Rumble.effect)
                    }
                    hardware.lights.setPattern(targetState.targetRobot.lights.targetColor)
                }
        )
        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMilis()
        telemetry.addLine("loop time: $loopTime milis")
        telemetry.addLine("peak loop time: ${loopTimeMeasurer.peakDeltaTime} milis")

        telemetry.update()
    }
}