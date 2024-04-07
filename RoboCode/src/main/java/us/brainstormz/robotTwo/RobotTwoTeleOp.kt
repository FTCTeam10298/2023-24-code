package us.brainstormz.robotTwo

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.os.Debug
import android.os.Debug.MemoryInfo
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.Gamepad.RumbleEffect
import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.FauxLocalizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.robotTwo.DepoManager.*
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Claw.ClawTarget
import us.brainstormz.robotTwo.subsystems.Drivetrain
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
    val transfer = Transfer(telemetry)
    val extendo = Extendo(telemetry)
    val collectorSystem: CollectorSystem = CollectorSystem(transfer= transfer, extendo= extendo, telemetry= telemetry)
    val leftClaw: Claw = Claw(telemetry)
    val rightClaw: Claw = Claw(telemetry)
    val wrist = Wrist(leftClaw, rightClaw, telemetry= telemetry)
    val arm: Arm = Arm()
    val lift: Lift = Lift(telemetry)
    val depoManager: DepoManager = DepoManager(arm= arm, lift= lift, wrist= wrist, telemetry= telemetry)
    val handoffManager: HandoffManager = HandoffManager(collectorSystem, wrist, lift, extendo, arm, telemetry)

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
    enum class Gamepad2RightStickMode {
        Drivetrain,
        Depo
    }
    enum class DepoInput {
//        SetLine1,
//        SetLine2,
//        SetLine3,
        Preset1,
        Preset2,
        Preset3,
        Preset4,
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
    enum class LatchInput {
        BothIn,
        BothOut,
        LeftOut,
        RightOut,
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
    data class DriverInput (
            val bumperMode: Gamepad1BumperMode,
            val gamepad2StickMode: Gamepad2RightStickMode,
            val lightInput: LightInput,
            val depo: DepoInput,
            val depoScoringHeightAdjust: Double,
            val armOverridePower: Double,
            val wrist: WristInput,
            val collector: CollectorInput,
            val extendo: ExtendoInput,
            val extendoManualPower: Double,
            val hang: HangInput,
            val launcher: LauncherInput,
            val handoff: HandoffInput,
            val rollers: LatchInput,
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

        val gamepad2RightStickMode: Gamepad2RightStickMode = if (gamepad2.touchpad && !previousGamepad2.touchpad) {
            when (previousTargetState.driverInput.gamepad2StickMode) {
                Gamepad2RightStickMode.Drivetrain -> Gamepad2RightStickMode.Depo
                Gamepad2RightStickMode.Depo -> Gamepad2RightStickMode.Drivetrain
            }
        } else {
            previousTargetState.driverInput.gamepad2StickMode
        }

        val liftStickInput = -gamepad2.right_stick_y.toDouble()
        val armOverrideStickValue = gamepad2.right_stick_x.toDouble()

        val depoInput = if (gamepad2RightStickMode == Gamepad2RightStickMode.Depo) {
            telemetry.addLine("gamepad2RightStickMode: $gamepad2RightStickMode")

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

        /**Rollers*/
        val rollers = when {
            gamepad1.b && !previousGamepad1.b-> {
                if (previousTargetState.driverInput.rollers != LatchInput.BothIn) {
                    LatchInput.BothIn
                } else {
                    LatchInput.NoInput
                }
            }
            gamepad1.right_stick_button && gamepad1.left_stick_button -> {
                LatchInput.BothOut
            }
            gamepad1.right_stick_button -> {
                LatchInput.RightOut
            }
            gamepad1.left_stick_button -> {
                LatchInput.LeftOut
            }
            else -> {
                if (previousTargetState.driverInput.rollers == LatchInput.BothIn) {
                    LatchInput.BothIn
                } else {
                    LatchInput.NoInput
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
                r = when (gamepad2RightStickMode) {
                    Gamepad2RightStickMode.Drivetrain -> {
                        gamepad2.right_stick_x.toDouble()
                    }
                    Gamepad2RightStickMode.Depo -> {
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
                rollers = rollers,
                extendo = extendo,
                extendoManualPower = gamepad1.right_trigger.toDouble() - gamepad1.left_trigger.toDouble(),
                handoff = handoff,
                hang = hang,
                launcher = launcher,
                bumperMode = gamepadOneBumperMode,
                gamepad2StickMode = gamepad2RightStickMode,
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
                previousTransferState = previousTargetState.targetRobot.collectorTarget.transferState,
        )

        val transferLeftSensorState = transfer.checkIfPixelIsTransferred(transferState.left)
        val transferRightSensorState = transfer.checkIfPixelIsTransferred(transferState.right)
        val areBothPixelsIn = transferLeftSensorState && transferRightSensorState

        val previousTransferLeftSensorState = transfer.checkIfPixelIsTransferred(previousTargetState.targetRobot.collectorTarget.transferState.left)
        val previousTransferRightSensorState = transfer.checkIfPixelIsTransferred(previousTargetState.targetRobot.collectorTarget.transferState.right)
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
        val handoffIsReadyCheck = handoffManager.checkIfHandoffIsReadyToStart(actualWorld, previousActualWorld)
        telemetry.addLine("doHandoffSequence: $doHandoffSequence")
        telemetry.addLine("handoffIsReadyCheck: $handoffIsReadyCheck")

        val handoffState = handoffManager.getHandoffState(actualRobot = actualRobot, previousTargetWorld = previousTargetState)
        telemetry.addLine("handoffState: $handoffState")

        /**Intake Noodles*/
        val timeSinceEjectionStartedMilis: Long = actualWorld.timestampMilis - (previousTargetState.targetRobot.collectorTarget.timeOfEjectionStartMilis?:actualWorld.timestampMilis)
        val timeToStopEjecting = timeSinceEjectionStartedMilis > 1000
        val wasPreviouslyEjecting = previousTargetState.targetRobot.collectorTarget.intakeNoodles == Intake.CollectorPowers.Eject
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

        /**Gates*/
        val latchOverrideState: Pair<Transfer.LatchPositions?, Transfer.LatchPositions?> = when (driverInput.rollers) {
            LatchInput.BothIn -> Transfer.LatchPositions.Open to Transfer.LatchPositions.Open
            LatchInput.BothOut -> Transfer.LatchPositions.Open to Transfer.LatchPositions.Open
            LatchInput.LeftOut -> Transfer.LatchPositions.Open to null
            LatchInput.RightOut -> null to Transfer.LatchPositions.Open
            LatchInput.NoInput -> null to null
        }

        fun getGateTransferringTarget(side: Transfer.Side): Transfer.LatchPositions {
//            val claw = wrist.clawsAsMap[side]!!
//            val clawActualAngle = actualRobot.depoState.wristAngles.getBySide(side)
//            val clawIsGripping = claw.isClawAtAngle(ClawTarget.Gripping, clawActualAngle)

            val handingOffIsHappening = handoffState.getBySide(side)

            return if (handingOffIsHappening) {
                Transfer.LatchPositions.Open
            } else {
                Transfer.LatchPositions.Closed
            }
        }

        val latchTarget = Transfer.TransferTarget(
                leftLatchTarget = Transfer.LatchTarget(latchOverrideState.first ?: getGateTransferringTarget(Transfer.Side.Right), 0),
                rightLatchTarget = Transfer.LatchTarget(latchOverrideState.second ?: getGateTransferringTarget(Transfer.Side.Left), 0)
        )

        /**Extendo*/
        val previousExtendoTargetPosition = previousTargetState.targetRobot.collectorTarget.extendo.targetPosition
        val extendoTargetState: SlideSubsystem.TargetSlideSubsystem = when (driverInput.extendo) {
            ExtendoInput.ExtendManual -> {
                SlideSubsystem.TargetSlideSubsystem(
                        targetPosition = previousExtendoTargetPosition,
                        movementMode = MovementMode.Power,
                        power = driverInput.extendoManualPower)
            }
            ExtendoInput.RetractManual -> {
                SlideSubsystem.TargetSlideSubsystem(
                        targetPosition = previousExtendoTargetPosition,
                        movementMode = MovementMode.Power,
                        power = driverInput.extendoManualPower)
            }
            ExtendoInput.NoInput -> {
                if (doHandoffSequence) {
                    extendo.findLimitToReset(
                            actualSlideSubsystem = actualRobot.collectorSystemState.extendo,
                            otherTarget = SlideSubsystem.TargetSlideSubsystem(
                                    targetPosition = Extendo.ExtendoPositions.Min,
                                    movementMode = MovementMode.Position,
                                    power = 0.0)
                    )
                } else {
                    SlideSubsystem.TargetSlideSubsystem(
                            targetPosition = previousExtendoTargetPosition,
                            movementMode = MovementMode.Power,
                            power = 0.0)
                }
            }
            ExtendoInput.RetractSetAmount -> {
                SlideSubsystem.TargetSlideSubsystem(
                        targetPosition = previousExtendoTargetPosition,
                        movementMode = MovementMode.Power,
                        power = -0.5)
            }
        }


        /**Depo*/
        val driverInputWrist = WristTargets(
                left= driverInput.wrist.left.toClawTarget() ?: previousTargetState.targetRobot.depoTarget.wristPosition.left,
                right= driverInput.wrist.right.toClawTarget() ?: previousTargetState.targetRobot.depoTarget.wristPosition.right)

        val areDepositing = previousTargetState.targetRobot.depoTarget.targetType == DepoTargetType.GoingOut

        fun isPixelInSide(side: Transfer.Side): Boolean {
            return when (side) {
                Transfer.Side.Left -> transfer.checkIfPixelIsTransferred(transferState.left)
                Transfer.Side.Right -> transfer.checkIfPixelIsTransferred(transferState.right)
            }
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
        val depoTarget: DepoTarget = if (driverInputIsManual) {
            DepoTarget(
                    lift = Lift.TargetLift(power = driverInput.depoScoringHeightAdjust, movementMode = MovementMode.Power, targetPosition = previousTargetState.targetRobot.depoTarget.lift.targetPosition),
                    armPosition = Arm.ArmTarget(
                            power = driverInput.armOverridePower,
                            movementMode = MovementMode.Power,
                            targetPosition = previousTargetState.targetRobot.depoTarget.armPosition.targetPosition
                    ),
                    wristPosition = driverInputWrist,
                    targetType = DepoTargetType.Manual
            )
        } else {
            depoManager.fullyManageDepo(
                    target= spoofDriverInputForDepo,
                    previousDepoTarget= previousTargetState.targetRobot.depoTarget,
                    actualWorld= actualWorld,
                    previousActualWorld= previousActualWorld)
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
//                    handoffManager.checkIfHandoffIsReady(actualWorld, previousActualWorld)
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
                        depoTarget = depoTarget,
                        collectorTarget = CollectorTarget(
                                intakeNoodles = intakeNoodleTarget,
                                timeOfEjectionStartMilis = timeOfEjectionStartMilis,
                                transferState = transferState,
                                latches = latchTarget,
                                extendo = extendoTargetState,
                        ),
                        hangPowers = hangTarget,
                        launcherPosition = launcherTarget,
                        lights = lights,
                ),
                doingHandoff = doHandoffSequence,
                handoffState = handoffState,
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
                rollers = LatchInput.NoInput,
                extendo = ExtendoInput.NoInput,
                extendoManualPower = 0.0,
                handoff = HandoffInput.NoInput,
                hang = HangInput.NoInput,
                launcher = LauncherInput.NoInput,
                bumperMode = Gamepad1BumperMode.Collector,
                gamepad2StickMode = Gamepad2RightStickMode.Drivetrain,
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

        val initialPreviousTargetState = TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(), MovementMode.Power, Drivetrain.DrivetrainPower()),
                        depoTarget = initDepoTarget,
                        collectorTarget = CollectorTarget(
                                intakeNoodles = Intake.CollectorPowers.Off,
                                timeOfEjectionStartMilis = 0,
                                transferState = Transfer.TransferState(
                                        left = initSensorState,
                                        right = initSensorState
                                ),
                                latches = Transfer.TransferTarget(
                                        leftLatchTarget = Transfer.LatchTarget(Transfer.LatchPositions.Closed, 0L),
                                        rightLatchTarget = Transfer.LatchTarget(Transfer.LatchPositions.Closed, 0L)
                                ),
                                extendo = SlideSubsystem.TargetSlideSubsystem(Extendo.ExtendoPositions.Manual, MovementMode.Position),
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
                handoffState = HandoffManager.SideIsActivelyHandingOff(false, false),
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

        measured("expensiveTelemetryLines-addLine"){
            stateDumper.lines().forEach(telemetry::addLine)
        }

        
        functionalReactiveAutoRunner.loop(
                actualStateGetter = {getActualState(it, gamepad1, gamepad2, hardware)},
                targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                    val previousActualState = previousActualState ?: actualState
                    val previousTargetState: TargetWorld = previousTargetState ?: initialPreviousTargetState
                    val driverInput = getDriverInput(previousTargetState= previousTargetState, actualWorld= actualState, previousActualWorld= previousActualState)
                    getTargetWorld(driverInput= driverInput, previousTargetState= previousTargetState, actualWorld= actualState, previousActualWorld= previousActualState)
                },
                stateFulfiller = { targetState, previousTargetState, actualState ->

//                    telemetry.addLine("\ntargetState: $targetState")
                    measured("actuate robot"){
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
                            transfer= transfer
                        )
                    }
                    measured("rumble"){
                        if (targetState.gamepad1Rumble != null && !gamepad1.isRumbling) {
                            gamepad1.runRumbleEffect(targetState.gamepad1Rumble.effect)
                        }
                    }
//                    hardware.lights.setPattern(targetState.targetRobot.lights.stripTarget)
                }
        )
        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()

        measured("telemetry"){
            telemetry.addLine("loop time: $loopTime milis")
            telemetry.addLine("peak loop time: ${loopTimeMeasurer.peakDeltaTime()} milis")

            telemetry.update()
        }
    }
}