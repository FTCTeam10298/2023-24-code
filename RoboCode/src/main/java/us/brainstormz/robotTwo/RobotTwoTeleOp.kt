package us.brainstormz.robotTwo

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.utils.DeltaTimeMeasurer
import us.brainstormz.robotTwo.subsystems.Claw.ClawTarget
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import kotlin.math.abs
import kotlin.math.absoluteValue

class RobotTwoTeleOp(private val telemetry: Telemetry) {
    //Not working:
    //Arm to position
    //Lift manual
    //Handoff

    val intake = Intake()
    val transfer = Transfer()
    val extendo = Extendo()
    val collectorSystem: CollectorSystem = CollectorSystem(transfer= transfer, extendo= extendo, telemetry= telemetry)

    val leftClaw: Claw = Claw()
    val rightClaw: Claw = Claw()
    val arm: Arm = Arm()
    val lift: Lift = Lift(telemetry)
    val depoManager: DepoManager = DepoManager(arm= arm, lift= lift)

    val handoffManager: HandoffManager = HandoffManager(collectorSystem, lift, extendo, arm, telemetry)


    lateinit var movement: MecanumDriveTrain
    lateinit var odometryLocalizer: RRTwoWheelLocalizer
    fun init(hardware: RobotTwoHardware) {
        movement = MecanumDriveTrain(hardware)
        odometryLocalizer = RRTwoWheelLocalizer(hardware, hardware.inchesPerTick)

        for (module in hardware.allHubs) {
            module.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
        }

    }


    enum class Gamepad1BumperMode {
        Collector,
        Claws
    }
    enum class DepoInput {
        SetLine1,
        SetLine2,
        SetLine3,
        Down,
        Manual,
        NoInput
    }
    enum class ClawInput {
        Drop,
        Hold,
        NoInput
    }
    enum class CollectorInput {
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
        ResetEncoder,
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
            val leftClaw: ClawInput,
            val rightClaw: ClawInput,
            val collector: CollectorInput,
            val extendo: ExtendoInput,
            val hang: HangInput,
            val launcher: LauncherInput,
            val handoff: HandoffInput,
            val rollers: RollerInput,
            val driveVelocity: PositionAndRotation
    ) {
        override fun toString(): String =
                "DriverInput(\n" +
                "   bumperMode=$bumperMode\n" +
                "   lightInput=$lightInput\n" +
                "   depo=$depo\n" +
                "   leftClaw=$leftClaw\n" +
                "   rightClaw=$rightClaw\n" +
                "   collector=$collector\n" +
                "   extendo=$extendo\n" +
                "   hang=$hang\n" +
                "   launcher=$launcher\n" +
                "   handoff=$handoff\n" +
                "   rollers=$rollers\n" +
                "   driveVelocity=$driveVelocity\n" +
                ")"
    }
    fun getDriverInput(actualWorld: ActualWorld, previousActualWorld: ActualWorld, previousTargetState: TargetWorld?): DriverInput {
        val gamepad1 = actualWorld.actualGamepad1
        val gamepad2 = actualWorld.actualGamepad2
        val robot = actualWorld.actualRobot
        val previousGamepad1 = previousActualWorld.actualGamepad1
        val previousGamepad2 = previousActualWorld.actualGamepad2
        val previousRobot = previousActualWorld.actualRobot
        val previousRobotTarget = previousTargetState?.targetRobot

        /**Depo*/
        val depoGamepad2Input: DepoInput? = when {
            gamepad2.dpad_up-> {
                DepoInput.SetLine3
            }
            gamepad2.dpad_down -> {
                DepoInput.Down
            }
            gamepad2.dpad_right && !previousGamepad2.dpad_right -> {
                if (previousRobotTarget?.depoTarget?.liftPosition != Lift.LiftPositions.SetLine1) {
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

        val liftOverrideStickValue = gamepad2.right_stick_y.toDouble()
        val areLiftManualControlsActive = liftOverrideStickValue.absoluteValue > 0.2
        val armOverrideStickValue = gamepad2.right_stick_x.toDouble()
        val isArmManualOverrideActive = armOverrideStickValue.absoluteValue >= 0.2
        val depoInput: DepoInput = if (areLiftManualControlsActive || isArmManualOverrideActive) {
            DepoInput.Manual
        } else {
            depoGamepad2Input ?: depoGamepad1Input
        }

        /**Bumper Mode*/
        val gamepad1DpadIsActive = depoGamepad1Input != DepoInput.NoInput
        val liftTargetIsDown = previousRobot.depoState.liftPositionTicks <= Lift.LiftPositions.Min.ticks
        val bothClawsAreRetracted = previousTargetState?.targetRobot?.depoTarget?.leftClawPosition == ClawTarget.Retracted && previousTargetState?.targetRobot?.depoTarget?.rightClawPosition == ClawTarget.Retracted
        val gamepadOneBumperMode: Gamepad1BumperMode = when {
            gamepad1DpadIsActive -> {
                Gamepad1BumperMode.Claws
            }
            !gamepad1DpadIsActive && (bothClawsAreRetracted || liftTargetIsDown) -> {
                Gamepad1BumperMode.Collector
            }
            else -> {
                previousTargetState?.driverInput?.bumperMode ?: Gamepad1BumperMode.Collector
            }
        }

        /**Claws*/
        val areGamepad1ClawControlsActive = gamepadOneBumperMode == Gamepad1BumperMode.Claws

        val gamepad1LeftClawToggle = areGamepad1ClawControlsActive && gamepad1.left_bumper && !previousGamepad1.left_bumper
        val gamepad2LeftClawToggle = gamepad2.left_bumper && !previousGamepad2.left_bumper
        val leftClaw: ClawInput = if (gamepad2LeftClawToggle || gamepad1LeftClawToggle) {
            when (previousTargetState?.targetRobot?.depoTarget?.leftClawPosition) {
                Claw.ClawTarget.Gripping -> ClawInput.Drop
                Claw.ClawTarget.Retracted -> ClawInput.Hold
                null -> ClawInput.Hold
            }
        } else {
            ClawInput.NoInput
        }

        val gamepad1RightClawToggle = areGamepad1ClawControlsActive && gamepad1.right_bumper && !previousGamepad1.right_bumper
        val gamepad2RightClawToggle = gamepad2.right_bumper && !previousGamepad2.right_bumper
        val rightClaw: ClawInput = if (gamepad2RightClawToggle || gamepad1RightClawToggle) {
            when (previousTargetState?.targetRobot?.depoTarget?.rightClawPosition) {
                Claw.ClawTarget.Gripping -> ClawInput.Drop
                Claw.ClawTarget.Retracted -> ClawInput.Hold
                null -> ClawInput.Hold
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
            val previousPowerInt: Int = previousRobotTarget?.collectorTarget?.intakeNoodles?.power?.toInt() ?: 0

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
            rightTrigger && leftTrigger -> ExtendoInput.ResetEncoder
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
            gamepad1.b -> {
                RollerInput.BothIn
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
            else -> RollerInput.NoInput
        }

        /**Hang*/
        val hang = if (gamepad1.x || (gamepad2.left_stick_button && gamepad2.right_stick_button)) {
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

        val isAtTheEndOfExtendo = actualWorld.actualRobot.collectorSystemState.extendoPositionTicks >= Extendo.ExtendoPositions.Max.ticks || actualWorld.actualRobot.collectorSystemState.extendoCurrentAmps > 6.0
        val extendoCompensationPower = if (isAtTheEndOfExtendo && yInput == 0.0) {
            gamepad1.right_trigger.toDouble()
        } else {
            0.0
        }

        val y = yInput + extendoCompensationPower + driver2YInput
        val x = xInput + driver2XInput
        val r = -rInput * abs(rInput)
        val driveVelocity = PositionAndRotation(x= x, y= y, r= r)

        /**Lights*/
        val previousIsAnyColorButtonPressed = previousGamepad2.a || previousGamepad2.b || previousGamepad2.x || previousGamepad2.y

        val lightColor = if (gamepad2.dpad_left) {
            if (!previousIsAnyColorButtonPressed) {
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
                        LightInput.NoColor
                    }
                }
            } else {
                LightInput.NoColor
            }
        } else {
            LightInput.NoInput
        }


        return DriverInput(
                driveVelocity = driveVelocity,
                depo = depoInput,
                leftClaw = leftClaw,
                rightClaw = rightClaw,
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
        fun toList():List<PixelColor> {
            return listOf(leftPixel, rightPixel)
        }
    }
    data class LightTarget(val targetColor: PixelColor, val pattern: BothPixelsWeWant, val timeOfColorChangeMilis: Long)
    fun getTargetWorld(driverInput: DriverInput, actualWorld: ActualWorld, previousActualWorld: ActualWorld, previousTargetState: TargetWorld?): TargetWorld {
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

        val inputsConflictWithTransfer = driverInput.extendo == ExtendoInput.Extend || (driverInput.depo != DepoInput.NoInput)

        telemetry.addLine("\nHANDOFF:")
        val doHandoffSequence: Boolean = when {
            inputsConflictWithTransfer -> {
                telemetry.addLine("Canceled due to conflicting inputs")
                false
            }
            weWantToStartHandoff -> {
                telemetry.addLine("Starting handoff")
                true
            }
            else -> {
                telemetry.addLine("Doing the same thing as last time")
                previousTargetState?.doingHandoff ?: false
            }
        }
        telemetry.addLine("Are we doing handoff: $doHandoffSequence")

        val previousBothClawState = when (previousTargetState?.targetRobot?.depoTarget?.leftClawPosition) {
            Claw.ClawTarget.Retracted -> HandoffManager.ClawStateFromHandoff.Retracted
            Claw.ClawTarget.Gripping -> HandoffManager.ClawStateFromHandoff.Gripping
            else -> HandoffManager.ClawStateFromHandoff.Retracted
        }
        val handoffState = handoffManager.getHandoffState(previousBothClawState, RevBlinkinLedDriver.BlinkinPattern.BLUE, actualRobot)

        /**Extendo*/
        val extendoState: Extendo.ExtendoPositions = when {
            driverInput.extendo == ExtendoInput.ResetEncoder -> {
                Extendo.ExtendoPositions.ResetEncoder
            }
            driverInput.extendo == ExtendoInput.Extend -> {
                Extendo.ExtendoPositions.Manual
            }
            driverInput.extendo == ExtendoInput.Retract -> {
                Extendo.ExtendoPositions.Manual
            }
            theRobotJustCollectedTwoPixels -> {
                Extendo.ExtendoPositions.Min
            }
            doHandoffSequence -> {
                when (handoffState.collectorState) {
                    HandoffManager.ExtendoStateFromHandoff.MoveIn -> {
                        Extendo.ExtendoPositions.AllTheWayInTarget
                    }
                    HandoffManager.ExtendoStateFromHandoff.MoveOutOfTheWay -> {
                        Extendo.ExtendoPositions.ClearTransfer
                    }
                }
            }
            areBothPixelsIn && !wereBothPixelsInPreviously -> {
                Extendo.ExtendoPositions.Min
            }
            else -> {
                previousTargetState?.targetRobot?.collectorTarget?.extendoPositions ?: Extendo.ExtendoPositions.Manual
            }
        }

        /**Intake Noodles*/
        val intakeNoodleTarget = intake.getCollectorState(when (driverInput.collector) {
            CollectorInput.Intake -> Intake.CollectorPowers.Intake
            CollectorInput.Eject ->  Intake.CollectorPowers.Eject
            CollectorInput.Off ->  Intake.CollectorPowers.Off
            CollectorInput.NoInput -> previousTargetState?.targetRobot?.collectorTarget?.intakeNoodles ?: Intake.CollectorPowers.Off
        },
        isPixelInLeft = transfer.isPixelIn(actualRobot.collectorSystemState.leftTransferState, Transfer.Side.Left),
        isPixelInRight= transfer.isPixelIn(actualRobot.collectorSystemState.rightTransferState, Transfer.Side.Right))

        /**Rollers */
        val autoRollerState = transfer.getAutoPixelSortState(isCollecting = intakeNoodleTarget == Intake.CollectorPowers.Intake, actualRobot)
        val rollerTargetState = when (driverInput.rollers) {
            RollerInput.BothIn -> autoRollerState.copy(leftServoCollect = Transfer.RollerPowers.Intake, rightServoCollect = Transfer.RollerPowers.Intake)
            RollerInput.BothOut -> autoRollerState.copy(leftServoCollect = Transfer.RollerPowers.Eject, rightServoCollect = Transfer.RollerPowers.Eject)
            RollerInput.LeftOut -> autoRollerState.copy(leftServoCollect = Transfer.RollerPowers.Eject)
            RollerInput.RightOut -> autoRollerState.copy(rightServoCollect = Transfer.RollerPowers.Eject)
            RollerInput.NoInput -> autoRollerState
        }

        /**Lift*/
        val liftRealTarget: Lift.LiftPositions = when {
                previousTargetState?.driverInput?.bumperMode == Gamepad1BumperMode.Claws && driverInput.bumperMode == Gamepad1BumperMode.Collector -> {
                    Lift.LiftPositions.Transfer
                }
                doHandoffSequence -> {
                    when (handoffState.liftState) {
                        HandoffManager.LiftStateFromHandoff.MoveDown -> Lift.LiftPositions.Transfer
                        HandoffManager.LiftStateFromHandoff.None -> Lift.LiftPositions.Nothing
                    }
                }
                else -> {
                    when (driverInput.depo) {
                        DepoInput.SetLine1 -> Lift.LiftPositions.SetLine1
                        DepoInput.SetLine2 -> Lift.LiftPositions.SetLine2
                        DepoInput.SetLine3 -> Lift.LiftPositions.SetLine3
                        DepoInput.Down -> Lift.LiftPositions.Transfer
                        DepoInput.Manual -> Lift.LiftPositions.Manual
                        DepoInput.NoInput -> previousTargetState?.targetRobot?.depoTarget?.liftPosition ?: Lift.LiftPositions.Manual
                    }
                }
            }

        val liftActualPositionIsAboveSafeArm = actualRobot.depoState.liftPositionTicks >= Lift.LiftPositions.ClearForArmToMove.ticks
        val armIsFarEnoughIn = actualRobot.depoState.armAngleDegrees >= Arm.Positions.GoodEnoughForLiftToGoDown.angleDegrees

        val liftTargetIsBelowSafeArm = liftRealTarget.ticks <= Lift.LiftPositions.ClearForArmToMove.ticks
        val armIsTooFarIn = actualRobot.depoState.armAngleDegrees >= Arm.Positions.TooFarIn.angleDegrees
        val liftNeedsToWaitForTheArm = liftTargetIsBelowSafeArm && ((!armIsFarEnoughIn && liftActualPositionIsAboveSafeArm) || armIsTooFarIn)
        telemetry.addLine("\nliftNeedsToWaitForTheArm: $liftNeedsToWaitForTheArm")

        telemetry.addLine("armIsTooFarIn: $armIsTooFarIn")
        val liftPosition: Lift.LiftPositions =  if (driverInput.depo == DepoInput.Manual) {
            Lift.LiftPositions.Manual
        } else if (liftNeedsToWaitForTheArm) {
            Lift.LiftPositions.WaitForArmToMove
        } else {
            liftRealTarget
        }

        val liftTargetHasntChanged = liftPosition == previousTargetState?.targetRobot?.depoTarget?.liftPosition
        val isLiftEligableForReset = actualRobot.depoState.isLiftLimitActivated && liftTargetHasntChanged

        telemetry.addLine("Lift position: ${actualRobot.depoState.liftPositionTicks}")
        telemetry.addLine("Lift target: ${liftPosition}, ticks: ${liftPosition.ticks}")

        /**Arm*/
        val liftIsBelowFreeArmLevel = actualRobot.depoState.liftPositionTicks <= Lift.LiftPositions.ClearForArmToMove.ticks
        val armIsInish = actualRobot.depoState.armAngleDegrees >= Arm.Positions.Inish.angleDegrees

        val depositorShouldGoAllTheWayIn = driverInput.depo == DepoInput.Down
        val depoPositionsWhereArmShouldBeOut = listOf(DepoInput.SetLine1, DepoInput.SetLine2, DepoInput.SetLine3)

        val isArmManualOverrideActive = driverInput.depo == DepoInput.Manual
        val armWasManualControlLastTime = previousTargetState?.driverInput?.depo == DepoInput.Manual
        val armPosition: Arm.Positions = if (isArmManualOverrideActive || armWasManualControlLastTime && liftTargetHasntChanged) {
            Arm.Positions.Manual
        } else {
            when  {
                liftPosition == Lift.LiftPositions.Manual || liftPosition == Lift.LiftPositions.Nothing-> {
                    previousTargetState?.targetRobot?.depoTarget?.armPosition ?: Arm.Positions.Manual
                }
                doHandoffSequence -> {
                    telemetry.addLine("Using the transfer to decide where to move")
                    handoffState.armState
                }
                liftIsBelowFreeArmLevel  -> {
                    if (armIsInish) {
                        Arm.Positions.ClearLiftMovement
                    } else {
                        Arm.Positions.AutoInitPosition
                    }
                }
                depositorShouldGoAllTheWayIn && !liftIsBelowFreeArmLevel-> {
                    Arm.Positions.ClearLiftMovement
                }
                driverInput.depo in depoPositionsWhereArmShouldBeOut -> {
                    println("ARM arm out")
                    Arm.Positions.Out
                }
                else -> {
                    println("ARM arm same position")
                    previousTargetState?.targetRobot?.depoTarget?.armPosition ?: Arm.Positions.Manual
                }
            }
        }

        /**Claws*/
        val isTheLiftGoingDown = liftPosition.ticks <= Lift.LiftPositions.ClearForArmToMove.ticks
        val wasTheLiftGoindDownBefore = previousActualWorld.actualRobot.depoState.liftPositionTicks <= Lift.LiftPositions.ClearForArmToMove.ticks
        val armIsIn = armPosition.angleDegrees >= Arm.Positions.GoodEnoughForLiftToGoDown.angleDegrees
        val isTheExtendoGoingIn = extendoState.ticks <= Extendo.ExtendoPositions.Min.ticks
        val wasTheCollectorGoingInBefore = previousActualWorld.actualRobot.depoState.liftPositionTicks <= Extendo.ExtendoPositions.Min.ticks
        val extendoIsIn = actualRobot.collectorSystemState.extendoPositionTicks <= Extendo.ExtendoPositions.Min.ticks
        val shouldTheClawsRetractForLift = isTheLiftGoingDown && !wasTheLiftGoindDownBefore && armIsIn
        val shouldTheClawsRetractForExtendo = isTheLiftGoingDown && !extendoIsIn//isTheExtendoGoingIn && !wasTheCollectorGoingInBefore
        val shouldClawsRetract = shouldTheClawsRetractForLift || shouldTheClawsRetractForExtendo

        val handoffClawTarget: ClawTarget = when (handoffState.clawPosition) {
                HandoffManager.ClawStateFromHandoff.Gripping -> {
                    ClawTarget.Gripping
//                    if (transferSensorState.transferLeftSensorState.hasPixelBeenSeen) {
//                        LeftClawPosition.Gripping
//                    } else {
//                        LeftClawPosition.Retracted
//                    }
                }
                HandoffManager.ClawStateFromHandoff.Retracted -> ClawTarget.Retracted
            }

        val leftClawPosition: ClawTarget = when (driverInput.leftClaw) {
                ClawInput.Drop -> ClawTarget.Retracted
                ClawInput.Hold -> ClawTarget.Gripping
                ClawInput.NoInput -> {
                    if (doHandoffSequence) {
                        handoffClawTarget
                    } else if (shouldClawsRetract) {
                        ClawTarget.Retracted
                    } else {
                        previousTargetState?.targetRobot?.depoTarget?.leftClawPosition ?: ClawTarget.Gripping
                    }
                }
            }

        val rightClawPosition: ClawTarget = when (driverInput.rightClaw) {
            ClawInput.Drop -> ClawTarget.Retracted
            ClawInput.Hold -> ClawTarget.Gripping
            ClawInput.NoInput -> {
                if (doHandoffSequence) {
                    handoffClawTarget
                } else if (shouldClawsRetract) {
                    ClawTarget.Retracted
                } else {
                    previousTargetState?.targetRobot?.depoTarget?.rightClawPosition ?: ClawTarget.Gripping
                }
            }
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
        val previousPattern = previousTargetState?.targetRobot?.lights?.pattern ?: bothUnknownPattern
        val desiredPixelLightPattern: BothPixelsWeWant = when (driverInput.lightInput) {
            LightInput.NoColor -> {
                if (previousTargetState?.driverInput?.lightInput == LightInput.NoInput) {
                    bothUnknownPattern
                } else {
                    previousPattern
                }
            }
            LightInput.NoInput -> {
                previousPattern
            }
            else -> {
                val color = when (driverInput.lightInput) {
                    LightInput.White -> PixelColor.White
                    LightInput.Yellow -> PixelColor.Yellow
                    LightInput.Purple -> PixelColor.Purple
                    LightInput.Green -> PixelColor.Green
                    else -> PixelColor.Unknown
                }
                val thisIsTheFirstLoopAfterShift = previousTargetState?.driverInput?.lightInput == LightInput.NoInput
                val thisIsTheFirstLoopAfterShiftThatAnyColorWasSelected = previousTargetState?.driverInput?.lightInput == LightInput.NoColor && previousPattern == bothUnknownPattern

                if (thisIsTheFirstLoopAfterShift || thisIsTheFirstLoopAfterShiftThatAnyColorWasSelected) {
                    previousPattern.copy(leftPixel = color)
                } else {
                    previousPattern.copy(rightPixel = color)
                }
//                if (previousTargetState?.driverInput?.lightInput == LightInput.NoInput) {
//                    previousPattern.copy(leftPixel = color)
//                } else if (previousTargetState?.driverInput?.lightInput == LightInput.NoColor && previousPattern == bothUnknownPatter) {
//                    previousPattern.copy(leftPixel = color)
//                } else {
//                    previousPattern.copy(rightPixel = color)
//                }
            }
        }
        telemetry.addLine("desiredPixelLightPattern: $desiredPixelLightPattern")

        val timeToDisplayColorMilis = 1000
        val timeWhenCurrentColorStartedBeingDisplayedMilis = previousTargetState?.targetRobot?.lights?.timeOfColorChangeMilis ?: actualWorld.timestampMilis
        val timeSinceCurrentColorWasDisplayedMilis = actualWorld.timestampMilis - timeWhenCurrentColorStartedBeingDisplayedMilis
        val isTimeToChangeColor = timeSinceCurrentColorWasDisplayedMilis >= timeToDisplayColorMilis

        val isCurrentColorObsolete = desiredPixelLightPattern != previousPattern

        val previousPixelToBeDisplayed = previousTargetState?.targetRobot?.lights?.targetColor ?: PixelColor.Unknown
        val currentPixelToBeDisplayed: PixelColor = when {
            isTimeToChangeColor || isCurrentColorObsolete -> {
                desiredPixelLightPattern.toList().firstOrNull { color ->
                    color != previousPixelToBeDisplayed
                } ?: desiredPixelLightPattern.leftPixel
            }
            else -> {
                previousPixelToBeDisplayed
            }
        }

        val lights = LightTarget(
                currentPixelToBeDisplayed,
                desiredPixelLightPattern,
                actualWorld.timestampMilis
        )

        /**Rumble*/
        val aClawWasPreviouslyRetracted = previousTargetState?.targetRobot?.depoTarget?.rightClawPosition == ClawTarget.Retracted ||  previousTargetState?.targetRobot?.depoTarget?.leftClawPosition == Claw.ClawTarget.Retracted
        val bothClawsAreGripping = rightClawPosition == ClawTarget.Retracted && leftClawPosition == ClawTarget.Retracted
        if (doHandoffSequence && bothClawsAreGripping && aClawWasPreviouslyRetracted) {
//            gamepad2.rumble(1.0, 1.0, 800)
//            gamepad1.rumble(1.0, 1.0, 800)
        }

//        if (isLiftEligableForReset && previousTargetState?.isLiftEligableForReset != true) {
//            hardware.liftMotorMaster.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//            hardware.liftMotorMaster.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//            gamepad1.rumble(1.0, 1.0, 1200)
//        }

////        val timeOfSeeing = listOf(transferSensorState.transferRightSensorState.timeOfSeeingMilis, transferSensorState.transferLeftSensorState.timeOfSeeingMilis).maxOfOrNull { time -> time } ?: 0
////        val timeSinceSeeing = System.currentTimeMillis() - timeOfSeeing
////        val timeToShowPixelLights = 1500
////        val doneWithThePixelCollectedLights = timeSinceSeeing >= timeToShowPixelLights
////        //val isTransferCollected = transferSensorState.transferRightSensorState.hasPixelBeenSeen && transferSensorState.transferLeftSensorState.hasPixelBeenSeen
////        telemetry.addLine("doneWithThePixelCollectedLights: $doneWithThePixelCollectedLights")
////        telemetry.addLine("timeSinceSeeing: $timeSinceSeeing")
////        telemetry.addLine("timeOfSeeing: $timeOfSeeing")
//
//        val colorToDisplay = if (areBothPixelsIn && !doneWithThePixelCollectedLights) {
//            //gamepad1.runRumbleEffect(twoBeatRumble)
//            RevBlinkinLedDriver.BlinkinPattern.LARSON_SCANNER_RED
//        } else {
//            currentPixelToBeDisplayed
//        }


        return TargetWorld(
                targetRobot = TargetRobot(
                        positionAndRotation = driveTarget,
                        depoTarget = DepoTarget(
                                liftPosition = liftPosition,
                                armPosition = armPosition,
                                leftClawPosition = leftClawPosition,
                                rightClawPosition = rightClawPosition,
                        ),
                        collectorTarget = CollectorTarget(
                                intakeNoodles = intakeNoodleTarget,
                                rollers = rollerTargetState,
                                extendoPositions = extendoState,
                        ),
                        hangPowers = hangTarget,
                        launcherPosition = launcherTarget,
                        lights = lights,
                ),
                isLiftEligableForReset = isLiftEligableForReset,
                doingHandoff = doHandoffSequence,
                driverInput = driverInput,
                isTargetReached = {_, _ -> false}
        )
    }

    val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    val loopTimeMeasurer = DeltaTimeMeasurer()
    fun loop(gamepad1: Gamepad, gamepad2: Gamepad, hardware: RobotTwoHardware, ) {

        for (hub in hardware.allHubs) {
            hub.clearBulkCache()
        }
        
        functionalReactiveAutoRunner.loop(
                actualStateGetter = { previousActualState ->

                    telemetry.addLine("previousActualState ${previousActualState?.actualGamepad2}")
                    telemetry.addLine("gamepad2 $gamepad2")

                    val currentGamepad1 = Gamepad()
                    currentGamepad1.copy(gamepad1)
                    val currentGamepad2 = Gamepad()
                    currentGamepad2.copy(gamepad2)
                    ActualWorld(
                            actualRobot = hardware.getActualState(localizer = odometryLocalizer, depoManager = depoManager, collectorSystem = collectorSystem),
                            actualGamepad1 = currentGamepad1,
                            actualGamepad2 = currentGamepad2,
                            timestampMilis = System.currentTimeMillis()
                    )
                },
                targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                    val previousActualWorld = previousActualState ?: actualState
                    val driverInput = getDriverInput(previousTargetState= previousTargetState, actualWorld= actualState, previousActualWorld= previousActualWorld)
                    getTargetWorld(driverInput, previousTargetState= previousTargetState, actualWorld= actualState, previousActualWorld= previousActualWorld)
                },
                stateFulfiller = { targetState, actualState ->
                    telemetry.addLine("actualState: $actualState")
                    telemetry.addLine("targetState: $targetState")
                    hardware.actuateRobot(
                            targetState,
                            actualState,
                            movement= movement,
                            arm= arm,
                            lift= lift,
                            extendo= extendo,
                            intake= intake,
                            transfer= transfer,
                            extendoOverridePower = (gamepad1.right_trigger.toDouble() - gamepad1.left_trigger.toDouble()),
                            liftOverridePower = gamepad2.right_stick_y.toDouble(),
                            armOverridePower = gamepad2.right_stick_x.toDouble()
                    )
                    hardware.lights.setPattern(targetState.targetRobot.lights.targetColor.blinkinPattern)
                }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMilis()
        telemetry.addLine("loop time: $loopTime milis")

        telemetry.update()
    }
}