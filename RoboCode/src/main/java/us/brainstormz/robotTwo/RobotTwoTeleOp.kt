package us.brainstormz.robotTwo

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.os.Debug
import android.os.Debug.MemoryInfo
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.Gamepad.RumbleEffect
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.FauxLocalizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.robotTwo.DepoManager.*
import us.brainstormz.robotTwo.localTests.TeleopTest
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Claw.ClawTarget
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.robotTwo.subsystems.Wrist.WristTargets
import us.brainstormz.utils.DeltaTimeMeasurer
import us.brainstormz.utils.Utils.sqrKeepSign
import us.brainstormz.utils.measured
import us.brainstormz.utils.runOnDedicatedThread
import java.io.File
import java.lang.Thread.sleep
import kotlin.math.absoluteValue

class StatsDumper(val reportingIntervalMillis:Long, context: Context){
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = MemoryInfo()
    val outFoo = RunningAppProcessInfo()
    val memInfo2 = ActivityManager.MemoryInfo()

    fun dumpRuntimeStats() = measured("dumpRuntimeStats"){
        logRuntimeStat("Runtime.freeMemory", formatBytes(Runtime.getRuntime().freeMemory()))
        logRuntimeStat("Runtime.maxMemory", formatBytes(Runtime.getRuntime().maxMemory()))
        logRuntimeStat("Runtime.totalMemory", formatBytes(Runtime.getRuntime().totalMemory()))

        logRuntimeStat("Debug.getNativeHeapSize", formatBytes(Debug.getNativeHeapSize()))
        logRuntimeStat("Debug.getNativeHeapFreeSize", formatBytes(Debug.getNativeHeapFreeSize()))
        logRuntimeStat("Debug.getNativeHeapAllocatedSize", formatBytes(Debug.getNativeHeapAllocatedSize()))

        Debug.getMemoryInfo(memInfo)
        memInfo.memoryStats.entries.forEach{(key, value) ->
            logRuntimeStat("Debug.memoryStats.$key", value)
        }

//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.native-heap] 5360
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.system] 10936
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.total-swap] 2388
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.graphics] 0
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.java-heap] 43304
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.total-pss] 112800
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.private-other] 19124
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.code] 32572
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.stack] 1504

        ActivityManager.getMyMemoryState(outFoo)
        am.getMemoryInfo(memInfo2)

        logRuntimeStat("ActivityManager.memoryInfo.availMem", formatBytes(memInfo2.availMem))
        logRuntimeStat("ActivityManager.memoryInfo.totalMem", formatBytes(memInfo2.totalMem))
        logRuntimeStat("ActivityManager.memoryInfo.threshold", formatBytes(memInfo2.threshold))
        logRuntimeStat("ActivityManager.memoryInfo.lowMemory", memInfo2.lowMemory)

//        logRuntimeStat("Debug.memoryStats.$key", outFoo.)

        printRuntimeStat("art.gc.gc-count-rate-histogram")
        printRuntimeStat("art.gc.blocking-gc-count-rate-histogram")
        printRuntimeStat("art.gc.blocking-gc-count")
        printRuntimeStat("art.gc.blocking-gc-time")
        printRuntimeStat("art.gc.gc-count")
        printRuntimeStat("art.gc.bytes-freed")
        printRuntimeStat("art.gc.gc-time")
        printRuntimeStat("art.gc.bytes-allocated")
    }


    fun printRuntimeStat(tag:String){
        logRuntimeStat(tag, Debug.getRuntimeStat(tag))
    }
    fun logRuntimeStat(tag:String, value:Any){
        println("[MEMORY_STATS] [$tag] $value")
    }

    fun formatBytes(bytes:Long):String {
        val kbytes = bytes/1000
        val mbytes = kbytes/1000
        return "$bytes bytes (${kbytes}kb, ${mbytes}mb)"
    }

    fun start() {
        runOnDedicatedThread("bot stats thread"){
            while(true){
                Thread.sleep(reportingIntervalMillis)
                dumpRuntimeStats()
            }
        }
    }

}
class RobotTwoTeleOp(private val telemetry: Telemetry) {
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

    enum class RumbleEffects(val effect: RumbleEffect) {
        TwoTap(RumbleEffect.Builder().addStep(1.0, 1.0, 400).addStep(0.0, 0.0, 200).addStep(1.0, 1.0, 400).build()),//.addStep(0.0, 0.0, 0)
        Throb(RumbleEffect.Builder().addStep(1.0, 1.0, 250).addStep(0.0, 0.0, 250).build()),//.addStep(0.0, 0.0, 0)
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
    enum class GamepadControlMode {
        Normal,
        Manual
    }
    enum class DepoInput {
        Preset1,
        Preset2,
        Preset3,
        Preset4,
        ScoringHeightAdjust,
        Down,
        Manual,
        NoInput
    }
    @Serializable
    data class WristInput(val left: ClawInput, val right: ClawInput) {
        val bothClaws = mapOf(Side.Left to left, Side.Right to right)
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
    enum class DropdownInput {
        Five,
        Manual,
        NoInput
    }
    enum class LatchInput {
        Open,
        NoInput
    }
    enum class ExtendoInput {
        ExtendManual,
        RetractManual,
        RetractSetAmount,
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
    @Serializable
    data class DriverInput (
            val bumperMode: Gamepad1BumperMode,
            val gamepad1ControlMode: GamepadControlMode,
            val gamepad2ControlMode: GamepadControlMode,
            val lightInput: LightInput,
            val depo: DepoInput,
            val depoScoringHeightAdjust: Double,
            val armOverridePower: Double,
            val wrist: WristInput,
            val collector: CollectorInput,
            val dropdown: DropdownInput,
            val dropdownPositionOverride: Double,
            val extendo: ExtendoInput,
            val extendoManualPower: Double,
            val hang: HangInput,
            val launcher: LauncherInput,
            val handoff: HandoffInput,
            val leftLatch: LatchInput,
            val rightLatch: LatchInput,
            val driveVelocity: Drivetrain.DrivetrainPower
    )
    fun getDriverInput(actualWorld: ActualWorld, previousActualWorld: ActualWorld, previousTargetState: TargetWorld): DriverInput {
        val gamepad1 = actualWorld.actualGamepad1
        val gamepad2 = actualWorld.actualGamepad2
        val robot = actualWorld.actualRobot
        val previousGamepad1 = previousActualWorld.actualGamepad1
        val previousGamepad2 = previousActualWorld.actualGamepad2
        val previousRobot = previousActualWorld.actualRobot
        val previousRobotTarget = previousTargetState.targetRobot


        /**Control Mode*/
        val gamepad1ControlMode: GamepadControlMode = if (gamepad1.touchpad && !previousGamepad1.touchpad) {
            when (previousTargetState.driverInput.gamepad1ControlMode) {
                GamepadControlMode.Normal -> GamepadControlMode.Manual
                GamepadControlMode.Manual -> GamepadControlMode.Normal
            }
        } else {
            previousTargetState.driverInput.gamepad1ControlMode
        }

        val gamepad2ControlMode: GamepadControlMode = if (gamepad2.touchpad && !previousGamepad2.touchpad) {
            when (previousTargetState.driverInput.gamepad2ControlMode) {
                GamepadControlMode.Normal -> GamepadControlMode.Manual
                GamepadControlMode.Manual -> GamepadControlMode.Normal
            }
        } else {
            previousTargetState.driverInput.gamepad2ControlMode
        }

        /**Depo*/
        val depoGamepad2Input: DepoInput? = when {
            gamepad2.dpad_up-> {
                DepoInput.Preset4
            }
            gamepad2.dpad_down -> {
                DepoInput.Down
            }
            gamepad2.dpad_right && !previousGamepad2.dpad_right -> {
                when (previousTargetState.driverInput.depo) {
                    DepoInput.Preset1 -> DepoInput.Preset2
                    DepoInput.Preset2 -> DepoInput.Preset3
//                    Lift.LiftPositions.Preset3 -> DepoInput.Preset4
                    else -> {
                        DepoInput.Preset1
                    }
                }
//                if (previousRobotTarget.depoTarget.lift.targetPosition != Lift.LiftPositions.SetLine1) {
//                    DepoInput.Preset1
//                } else {
//                    DepoInput.Preset2
//                }
            }
            else -> null
        }
        val depoGamepad1Input: DepoInput = when {
            gamepad1.dpad_up-> {
                DepoInput.Preset4
            }
            gamepad1.dpad_left -> {
                DepoInput.Preset3
            }
            gamepad1.dpad_down -> {
                DepoInput.Preset1
            }
            else -> DepoInput.NoInput
        }

        val dpadInput: DepoInput = depoGamepad2Input ?: depoGamepad1Input

        val liftStickInput = -gamepad2.right_stick_y.toDouble()
        val armOverrideStickValue = gamepad2.right_stick_x.toDouble()

        val depoInput = if (gamepad2ControlMode == GamepadControlMode.Manual) {
            telemetry.addLine("gamepad2RightStickMode: $gamepad2ControlMode")

            val isLiftControlActive = liftStickInput.absoluteValue > 0.2

            val liftControlMode = when (previousTargetState.targetRobot.depoTarget.lift.targetPosition) {
//                Lift.LiftPositions.BackboardBottomRow -> LiftControlMode.Adjust
                Lift.LiftPositions.SetLine1 -> LiftControlMode.Adjust
                Lift.LiftPositions.SetLine2 -> LiftControlMode.Adjust
                Lift.LiftPositions.SetLine2Other -> LiftControlMode.Adjust
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
            val isLiftManualOverrideActive = isLiftControlActive && liftControlMode == LiftControlMode.Override
            val dpadAdjustIsActive = isLiftControlActive && liftControlMode == LiftControlMode.Adjust

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

            val driverInputIsManual = isLiftManualOverrideActive || isArmManualOverrideActive
            val depoWasManualLastLoop = previousTargetState.targetRobot.depoTarget.targetType == DepoTargetType.Manual


            if ((driverInputIsManual || (depoWasManualLastLoop && dpadInput == DepoInput.NoInput))) {
                DepoInput.Manual
            } else if (dpadAdjustIsActive || (previousTargetState.driverInput.depo == DepoInput.ScoringHeightAdjust && dpadInput == DepoInput.NoInput)) {
                DepoInput.ScoringHeightAdjust
            } else {
                dpadInput
            }
        } else {
            if (dpadInput == DepoInput.Manual) {
                DepoInput.NoInput
            } else {
                dpadInput
            }
        }


        val armOverridePower = if (depoInput == DepoInput.Manual) {
            armOverrideStickValue
        } else {
            0.0
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
        //when collector stops and starts forget about the remembered roller intake times
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

        /**Dropdown*/
        val dropdown = if (gamepad1.left_stick_y != 0f) {
            DropdownInput.NoInput
        } else if (gamepad1.x) {
            DropdownInput.Five
        } else {
            DropdownInput.NoInput
        }

        /**Extendo*/
        val extendoTriggerActivation = 0.1
        val rightTrigger: Boolean = gamepad1.right_trigger > extendoTriggerActivation
        val leftTrigger: Boolean = gamepad1.left_trigger > extendoTriggerActivation

        val gamepad2X = gamepad2.square

        val extendo = when {
            rightTrigger -> ExtendoInput.ExtendManual
            leftTrigger -> ExtendoInput.RetractManual
            gamepad2X -> ExtendoInput.RetractSetAmount
            else -> ExtendoInput.NoInput
        }

        /**Handoff*/
        val isHandoffButtonPressed = (gamepad2.a && !gamepad2.dpad_left) || (gamepad1.a && !gamepad1.start)
        val handoff = when {
            isHandoffButtonPressed -> HandoffInput.StartHandoff
            else -> HandoffInput.NoInput
        }

        /**Latches*/
        val leftLatch = if (gamepad1.left_stick_button) {
            LatchInput.Open
        } else {
            LatchInput.NoInput
        }
        val rightLatch = if (gamepad1.right_stick_button) {
            LatchInput.Open
        } else {
            LatchInput.NoInput
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
        val isAtTheEndOfExtendo = actualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks >= Extendo.ExtendoPositions.Max.ticks || actualWorld.actualRobot.collectorSystemState.extendo.currentAmps > 6.0
        val extendoCompensationPower = if (isAtTheEndOfExtendo && gamepad1.left_stick_y == 0f) {
            gamepad1.right_trigger.toDouble()
        } else {
            0.0
        }

        val drive1Input = Drivetrain.DrivetrainPower(
                y = -gamepad1.left_stick_y.toDouble() + extendoCompensationPower,
                x = gamepad1.left_stick_x.toDouble(),
                r = gamepad1.right_stick_x.toDouble()
        )


        val drive2Input = Drivetrain.DrivetrainPower(
                y = gamepad2.left_stick_y.toDouble(),
                x = -gamepad2.left_stick_x.toDouble(),
                r = when (gamepad2ControlMode) {
                    GamepadControlMode.Normal -> {
                        gamepad2.right_stick_x.toDouble()
                    }
                    GamepadControlMode.Manual -> {
                        0.0
                    }
                }
        )
        //Driver 2 is same as driver 1 except other side of bot is front

        val driveVelocityWithoutSqrt = drive1Input + drive2Input

        val driveVelocity = driveVelocityWithoutSqrt.copy(r = sqrKeepSign(driveVelocityWithoutSqrt.r))

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
                armOverridePower = armOverridePower,
                wrist = WristInput(leftClaw, rightClaw),
                collector = inputCollectorStateSystem,
                dropdown = dropdown,
                dropdownPositionOverride= gamepad1.left_stick_y.toDouble(),
                leftLatch = leftLatch,
                rightLatch = rightLatch,
                extendo = extendo,
                extendoManualPower = gamepad1.right_trigger.toDouble() - gamepad1.left_trigger.toDouble(),
                handoff = handoff,
                hang = hang,
                launcher = launcher,
                bumperMode = gamepadOneBumperMode,
                gamepad1ControlMode = gamepad1ControlMode,
                gamepad2ControlMode = gamepad2ControlMode,
                lightInput = lightColor
        )
    }

    enum class PixelColor(val neoPixelColor: Neopixels.NeoPixelColors) {
        White   (Neopixels.NeoPixelColors.White),
        Green   (Neopixels.NeoPixelColors.Green),
        Purple  (Neopixels.NeoPixelColors.Purple),
        Yellow  (Neopixels.NeoPixelColors.Yellow),
        Unknown (Neopixels.NeoPixelColors.Off);

    }
//    fun Neopixels.NeoPixelColors.toPixelColor(): PixelColor {
//        return when (this) {
//            Neopixels.NeoPixelColors.White -> White
//               (),
//            Green   (Neopixels.NeoPixelColors.Green),
//            Purple  (Neopixels.NeoPixelColors.Purple),
//            Yellow  (Neopixels.NeoPixelColors.Yellow),
//            Unknown (Neopixels.NeoPixelColors.Off);
//        }
//    }
    @Serializable
    data class BothPixelsWeWant(val leftPixel: PixelColor, val rightPixel: PixelColor) {
        constructor(): this(leftPixel = PixelColor.Unknown, rightPixel = PixelColor.Unknown)

        fun toStripState(): Neopixels.StripState {
            return Neopixels.HalfAndHalfTarget(leftPixel.neoPixelColor, rightPixel.neoPixelColor).compileStripState()
        }

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
    @Serializable
    data class LightTarget(val pattern: BothPixelsWeWant, val stripTarget: Neopixels.StripState) {
        constructor(pattern: BothPixelsWeWant): this(pattern = pattern, stripTarget = pattern.toStripState())
        constructor(stripTarget: Neopixels.StripState): this(pattern = BothPixelsWeWant(), stripTarget = stripTarget)
        constructor(): this(pattern = BothPixelsWeWant(), stripTarget = Neopixels.HalfAndHalfTarget().compileStripState())
    }
    fun getTargetWorld(driverInput: DriverInput, actualWorld: ActualWorld, previousActualWorld: ActualWorld, previousTargetState: TargetWorld): TargetWorld {
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

        val weWantToStartHandoff = driverInput.handoff == HandoffInput.StartHandoff || theRobotJustCollectedTwoPixels

        val inputsConflictWithTransfer = driverInput.extendo == ExtendoInput.ExtendManual || (driverInput.depo == DepoInput.Manual)

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
        // tell humans
        telemetry.addLine("doHandoffSequence: $doHandoffSequence")

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
                        CollectorInput.Intake -> Intake.CollectorPowers.Intake
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
            DropdownInput.Five -> {
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
                Dropdown.DropdownTarget(power = driverInput.dropdownPositionOverride, movementMode = MovementMode.Power, targetPosition = previousTargetState.targetRobot.collectorTarget.dropDown.targetPosition)
            }
        }

        /**Gates*/
        fun getLatchTarget(side: Side, targetPosition: Transfer.LatchPositions): Transfer.LatchTarget {
            val previousLatchTarget = previousTargetState.targetRobot.collectorTarget.latches.getBySide(side)

            return if (targetPosition != previousLatchTarget.target) {
                Transfer.LatchTarget(
                        target= targetPosition,
                        timeTargetChangedMillis= actualWorld.timestampMilis)
            } else {
                previousLatchTarget
            }
        }

        fun getLatchHandingOffTarget(side: Side): Transfer.LatchTarget {
            val handingOffIsHappening = false//handoffState.getBySide(side.otherSide())

            return getLatchTarget(side = side,
                    targetPosition = if (handingOffIsHappening) {
                        Transfer.LatchPositions.Open
                    } else {
                        Transfer.LatchPositions.Closed
                    }
            )
        }

        fun latchInputToLatchPosition(latchInput: LatchInput): Transfer.LatchPositions? = when (latchInput) {
            LatchInput.Open -> Transfer.LatchPositions.Open
            LatchInput.NoInput -> null
        }

        val latchTarget = Transfer.TransferTarget(
                left = getLatchHandingOffTarget(Side.Left),
                right = getLatchHandingOffTarget(Side.Right),
        )

        /**Extendo*/
        val previousExtendoTargetPosition = previousTargetState.targetRobot.collectorTarget.extendo.targetPosition
        val extendoTargetState: Extendo.ExtendoTarget = when (driverInput.extendo) {
            ExtendoInput.ExtendManual -> {
                Extendo.ExtendoTarget(
                        targetPosition = previousExtendoTargetPosition,
                        movementMode = MovementMode.Power,
                        power = driverInput.extendoManualPower)
            }
            ExtendoInput.RetractManual -> {
                Extendo.ExtendoTarget(
                        targetPosition = previousExtendoTargetPosition,
                        movementMode = MovementMode.Power,
                        power = driverInput.extendoManualPower)
            }
            ExtendoInput.RetractSetAmount -> {
                Extendo.ExtendoTarget(
                        targetPosition = previousExtendoTargetPosition,
                        movementMode = MovementMode.Power,
                        power = -0.5)
            }
            ExtendoInput.NoInput -> {
                if (doHandoffSequence) {
                    val slideThinksItsAtZero = actualRobot.collectorSystemState.extendo.currentPositionTicks <= 0

                    if (slideThinksItsAtZero && !actualRobot.collectorSystemState.extendo.limitSwitchIsActivated) {
                        Extendo.ExtendoTarget(power = -extendo.findResetPower, movementMode = MovementMode.Power, targetPosition = Extendo.ExtendoPositions.Min)
                    } else {
                        Extendo.ExtendoTarget(
                                targetPosition = Extendo.ExtendoPositions.Min,
                                movementMode = MovementMode.Position,
                                power = 0.0)
                    }

                } else {
                    Extendo.ExtendoTarget(
                            targetPosition = previousExtendoTargetPosition,
                            movementMode = MovementMode.Power,
                            power = 0.0)
                }
            }
        }

        /**Handoff*/
//        Collector
        val uncoordinatedCollectorTarget = CollectorTarget(
                intakeNoodles = intakeNoodleTarget,
                dropDown = dropdownTarget,
                timeOfEjectionStartMilis = timeOfEjectionStartMillis,
                timeOfTransferredMillis = timeOfTransferredMillis,
                transferSensorState = transferState,
                latches = latchTarget,
                extendo = extendoTargetState,
        )

//        Depo
        val driverInputWrist = WristTargets(
                left= driverInput.wrist.left.toClawTarget() ?: previousTargetState.targetRobot.depoTarget.wristPosition.left,
                right= driverInput.wrist.right.toClawTarget() ?: previousTargetState.targetRobot.depoTarget.wristPosition.right)

        val areDepositing = previousTargetState.targetRobot.depoTarget.targetType == DepoTargetType.GoingOut

        fun isPixelInSide(side: Side): Boolean {
            return when (side) {
                Side.Left -> transfer.checkIfPixelIsTransferred(transferState.left)
                Side.Right -> transfer.checkIfPixelIsTransferred(transferState.right)
            }
        }

        val doingHandoff = doHandoffSequence && previousTargetState.targetRobot.depoTarget.targetType != DepoTargetType.GoingOut
        val collectorIsMovingOut = extendo.getVelocityTicksPerMili(actualWorld, previousActualWorld) > 0.1
        val mapOfClawInputsToConditions: Map<ClawInput, (Side) -> List<Boolean>> = mapOf(
                ClawInput.Hold to {side ->
                    listOf(
                            doingHandoff && isPixelInSide(Side.entries.first{it != side} /*claws Are Flipped when down*/),
                    )
                },
                ClawInput.Drop to {side ->
                    val areLatchesReady = transfer.checkIfLatchHasActuallyAchievedTarget(side, Transfer.LatchPositions.Closed, actualWorld.timestampMilis, previousTargetState.targetRobot.collectorTarget.latches)
                    listOf(
                            !areDepositing && intakeNoodleTarget == Intake.CollectorPowers.Intake,
                            !areDepositing && areLatchesReady && collectorIsMovingOut
                    )
                },
        )

        val clawInputPerSide = Side.entries.map { side ->
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
                wrist = WristInput(clawInputPerSide[Side.Left]!!,  clawInputPerSide[Side.Right]!!)
        )

        val driverInputIsManual = driverInput.depo == DepoInput.Manual

        val handoffPixelsToLift = HandoffManager.HandoffPixelsToLift(
                left = clawInputPerSide[Side.Left]!! == ClawInput.Hold,
                right = clawInputPerSide[Side.Right]!! == ClawInput.Hold
        )
        val uncoordinatedDepoInput = spoofDriverInputForDepo.depo

//        Handoff Coordination
        val handoffTarget = handoffManager.manageHandoff(
                handoff = handoffPixelsToLift,
                depoInput = uncoordinatedDepoInput,
                collectorTarget = uncoordinatedCollectorTarget,
                previousTargetWorld = previousTargetState,
                actualWorld = actualWorld
        )

        val overrideHandoff = driverInputIsManual || driverInput.gamepad1ControlMode == GamepadControlMode.Manual
        val handoffWithOverrides = if (overrideHandoff) {

            fun latchInputToLatchPosition(latchInput: LatchInput): Transfer.LatchPositions =
                    when (latchInput) {
                        LatchInput.Open -> Transfer.LatchPositions.Open
                        LatchInput.NoInput -> Transfer.LatchPositions.Closed
                    }

            HandoffManager.HandoffTarget(
                    depo = DepoTarget(
                            lift = Lift.TargetLift(
                                    power = driverInput.depoScoringHeightAdjust,
                                    movementMode = MovementMode.Power,
                                    targetPosition = previousTargetState.targetRobot.depoTarget.lift.targetPosition
                            ),
                            armPosition = Arm.ArmTarget(
                                    power = driverInput.armOverridePower,
                                    movementMode = MovementMode.Power,
                                    targetPosition = previousTargetState.targetRobot.depoTarget.armPosition.targetPosition
                            ),
                            wristPosition = driverInputWrist,
                            targetType = DepoTargetType.Manual
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
                                ExtendoInput.ExtendManual -> {
                                    Extendo.ExtendoTarget(
                                            targetPosition = previousExtendoTargetPosition,
                                            movementMode = MovementMode.Power,
                                            power = driverInput.extendoManualPower)
                                }
                                ExtendoInput.RetractManual -> {
                                    Extendo.ExtendoTarget(
                                            targetPosition = previousExtendoTargetPosition,
                                            movementMode = MovementMode.Power,
                                            power = driverInput.extendoManualPower)
                                }
                                ExtendoInput.RetractSetAmount -> {
                                    Extendo.ExtendoTarget(
                                            targetPosition = previousExtendoTargetPosition,
                                            movementMode = MovementMode.Power,
                                            power = -0.7)
                                }
                                ExtendoInput.NoInput -> {
                                    Extendo.ExtendoTarget(
                                            targetPosition = Extendo.ExtendoPositions.Min,
                                            movementMode = MovementMode.Power,
                                            power = 0.0)
                                }
                            },
                    )
            )
        } else {
            handoffTarget
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
        val timeSinceStartOfMatchMilis = System.currentTimeMillis() - timeOfMatchStartMilis
        val timeSinceStartOfMatchSeconds = timeSinceStartOfMatchMilis / 1000

        val timeToStartEndgame = timeSinceStartOfMatchSeconds >= timeSinceStartOfMatchToStartEndgameSeconds

        val colorToDisplay =  if (timeToStartEndgame) {
            Neopixels.HalfAndHalfTarget(Neopixels.NeoPixelColors.Red).compileStripState()
        } else {
            desiredPixelLightPattern.toStripState()
        }

        val lights = LightTarget(desiredPixelLightPattern, colorToDisplay)

        /**Rumble*/
        //Need to only trigger on rising edge
        val gamepad1RumbleEffectToCondition: Map<RumbleEffects, List<()->Boolean>> = mapOf(
                RumbleEffects.Throb to listOf {
                    wrist.clawsAsMap.map {(side, claw) ->
                        claw.isClawAtAngle(
                            target = ClawTarget.Gripping,
                            actualDegrees = actualRobot.depoState.wristAngles.getBySide(side),
                        )
                    }.fold(false) { acc, it ->
                        acc || it
                    }
                }
        )

        val gamepad1RumbleRoutine = gamepad1RumbleEffectToCondition.toList().firstOrNull { (rumble, listOfConditions) ->
            listOfConditions.fold(false) { acc, it -> acc || it() }
        }?.first
//        val gamepad1RumbleRoutine = null

        return TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(
                                power = driveTarget,
                                movementMode = MovementMode.Power,
                                targetPosition = PositionAndRotation()
                        ),
                        depoTarget = handoffWithOverrides.depo,
                        collectorTarget = handoffWithOverrides.collector,
                        hangPowers = hangTarget,
                        launcherPosition = launcherTarget,
                        lights = lights,
                ),
                doingHandoff = doHandoffSequence,
                driverInput = spoofDriverInputForDepo,
                getNextTask = { _, _, _-> null },
                gamepad1Rumble = gamepad1RumbleRoutine
        )
    }


    companion object {
        val noInput = DriverInput(
                driveVelocity = Drivetrain.DrivetrainPower(),
                depo = DepoInput.NoInput,
                depoScoringHeightAdjust = 0.0,
                armOverridePower = 0.0,
                wrist = WristInput(ClawInput.NoInput, ClawInput.NoInput),
                collector = CollectorInput.NoInput,
                dropdown = DropdownInput.NoInput,
                dropdownPositionOverride= 0.0,
                leftLatch = LatchInput.NoInput,
                rightLatch = LatchInput.NoInput,
                extendo = ExtendoInput.NoInput,
                extendoManualPower = 0.0,
                handoff = HandoffInput.NoInput,
                hang = HangInput.NoInput,
                launcher = LauncherInput.NoInput,
                bumperMode = Gamepad1BumperMode.Collector,
                gamepad1ControlMode = GamepadControlMode.Normal,
                gamepad2ControlMode = GamepadControlMode.Normal,
                lightInput = LightInput.NoInput
        )

        val initDepoTarget = DepoTarget(
                lift = Lift.TargetLift(Lift.LiftPositions.Down),
                armPosition = Arm.ArmTarget(Arm.Positions.In, MovementMode.Power, 0.0),
                wristPosition = WristTargets(ClawTarget.Gripping),
                targetType = DepoTargetType.GoingHome
        )

        val initSensorState = Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMilis = 0L,
        )

        val initLatchTarget = Transfer.LatchTarget(Transfer.LatchPositions.Closed, 0)

        val initialPreviousTargetState = TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(), MovementMode.Power, Drivetrain.DrivetrainPower()),
                        depoTarget = initDepoTarget,
                        collectorTarget = CollectorTarget(
                                intakeNoodles = Intake.CollectorPowers.Off,
                                dropDown= Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                                timeOfEjectionStartMilis = 0,
                                timeOfTransferredMillis = 0,
                                transferSensorState = Transfer.TransferSensorState(
                                        left = initSensorState,
                                        right = initSensorState
                                ),
                                latches = Transfer.TransferTarget(
                                        left = initLatchTarget,
                                        right = initLatchTarget
                                ),
                                extendo = Extendo.ExtendoTarget(Extendo.ExtendoPositions.Manual, 0.0, MovementMode.Position),
                        ),
                        hangPowers = RobotTwoHardware.HangPowers.Holding,
                        launcherPosition = RobotTwoHardware.LauncherPosition.Holding,
                        lights = LightTarget(
                                pattern = BothPixelsWeWant(PixelColor.Unknown, PixelColor.Unknown),
                                stripTarget = Neopixels.HalfAndHalfTarget().compileStripState()
                        ),
                ),
                doingHandoff = false,
                driverInput = noInput,
                getNextTask = { _, _, _ -> null },
                gamepad1Rumble = null
        )
    }

    lateinit var stateDumper: StateDumper
    lateinit var statsDumper:StatsDumper
    lateinit var drivetrain: Drivetrain
    fun init(hardware: RobotTwoHardware) {
        statsDumper = StatsDumper(reportingIntervalMillis = 1000, FtcRobotControllerActivity.instance!!)
        statsDumper.start()
        stateDumper = StateDumper(reportingIntervalMillis = 1000, functionalReactiveAutoRunner)
        stateDumper.start()

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

    fun getActualState(previousActualState:ActualWorld?, gamepad1: Gamepad, gamepad2: Gamepad, hardware: RobotTwoHardware):ActualWorld{
        val (currentGamepad1, currentGamepad2)  = measured("gamepad copies"){
            val currentGamepad1 = Gamepad()
            currentGamepad1.copy(gamepad1)
            val currentGamepad2 = Gamepad()
            currentGamepad2.copy(gamepad2)
            currentGamepad1 to currentGamepad2
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
            timestampMilis = System.currentTimeMillis()
        )
    }

    fun loop(gamepad1: Gamepad, gamepad2: Gamepad, hardware: RobotTwoHardware) = measured("main loop"){

        measured("clear bulk cache"){
            for (hub in hardware.allHubs) {
                hub.clearBulkCache()
            }
        }
        
        functionalReactiveAutoRunner.loop(
                actualStateGetter = {getActualState(it, gamepad1, gamepad2, hardware)},
                targetStateFetcher = { previousTargetState, actualState, previousActualState ->

                    telemetry.addLine("extendo current draw: ${actualState.actualRobot.collectorSystemState.extendo.currentAmps}")

                    val previousActualState = previousActualState ?: actualState
                    val previousTargetState: TargetWorld = previousTargetState ?: initialPreviousTargetState
                    val driverInput = getDriverInput(previousTargetState= previousTargetState, actualWorld= actualState, previousActualWorld= previousActualState)
                    val targetWorld = getTargetWorld(driverInput= driverInput, previousTargetState= previousTargetState, actualWorld= actualState, previousActualWorld= previousActualState)
                    if (actualState.actualGamepad1.touchpad && !previousActualState.actualGamepad1.touchpad) {
                        saveStateSnapshot(actualState, previousActualState, targetWorld, previousTargetState)
                    }
                    targetWorld
                },
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
//                    hardware.lights.setPattern(targetState.targetRobot.lights.stripTarget)
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

            val json = Json { ignoreUnknownKeys = true }
            val jsonEncoded = json.encodeToString(actualWorld)

            file.printWriter().use {
                it.print(jsonEncoded)
            }
            sleep(1000)
        }
    }
}