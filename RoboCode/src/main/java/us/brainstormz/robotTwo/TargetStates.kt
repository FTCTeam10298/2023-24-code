package us.brainstormz.robotTwo

import com.fasterxml.jackson.annotation.JsonIgnore
import com.qualcomm.robotcore.hardware.Gamepad
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

@Serializable
data class DepoTarget(
        val armPosition: Arm.ArmTarget,
        @Contextual
        val lift: Lift.TargetLift,
        val wristPosition: Wrist.WristTargets,
        val targetType: DepoManager.DepoTargetType
)
data class CollectorTarget(
        val extendo: Extendo.ExtendoTarget,
        val timeOfEjectionStartMillis: Long?,
        val timestampOfBothPixelsLoadedIntoTransferMillis: Long?,
        val intakeNoodles: Intake.CollectorPowers,
        val dropDown: Dropdown.DropdownTarget,
        val transferSensorState: Transfer.TransferSensorState,
        val latches: Transfer.TransferTarget,
)
data class TargetRobot(
        val drivetrainTarget: Drivetrain.DrivetrainTarget,
        val depoTarget: DepoTarget,
        val collectorTarget: CollectorTarget,
        val hangPowers: RobotTwoHardware.HangPowers,
        val launcherPosition: RobotTwoHardware.LauncherPosition,
        val lights: RobotTwoTeleOp.LightTarget
)

data class AutoInput (
        val drivetrainTarget: Drivetrain.DrivetrainTarget,
        val depoInput: RobotTwoTeleOp.DepoInput,
        val armAtInitPosition: RobotTwoAuto.ArmInput,
        val handoffInput: RobotTwoTeleOp.HandoffInput,
        val wristInput: RobotTwoTeleOp.WristInput,
        val extendoInput: Extendo.ExtendoPositions,
        val intakeInput: RobotTwoAuto.IntakeInput,
        @get:JsonIgnore
        val getNextInput: ((actualWorld: ActualWorld, previousActualWorld: ActualWorld, targetWorld: TargetWorld) -> AutoInput)? = null,
        val listIndex: Int? = null,
        val getCurrentPositionAndRotationFromAprilTag: Boolean = false
)

data class TargetWorld(
    val targetRobot: TargetRobot,
    val driverInput: RobotTwoTeleOp.DriverInput,
    val doingHandoff: Boolean,

    val autoInput: AutoInput? = null,

    val timeTargetStartedMilis: Long = 0,
    val gamepad1Rumble: RobotTwoTeleOp.RumbleEffects?
) {
    override fun equals(other: Any?): Boolean =
        if (other is TargetWorld) {
                    other.targetRobot == targetRobot &&
                    other.driverInput == driverInput &&
                    other.doingHandoff == doingHandoff &&
                    other.autoInput == autoInput &&
                    other.timeTargetStartedMilis == timeTargetStartedMilis &&
                    other.gamepad1Rumble == gamepad1Rumble
        } else {
            false
        }
}

data class ActualRobot(
    val positionAndRotation: PositionAndRotation,
    val depoState: DepoManager.ActualDepo,
    val collectorSystemState: CollectorManager.ActualCollector,
    val neopixelState: Neopixels.StripState
)

data class ActualWorld(
        val actualRobot: ActualRobot,
        val aprilTagReadings: List<AprilTagDetection> = listOf(),
        val actualGamepad1: SerializableGamepad,
        val actualGamepad2: SerializableGamepad,
        val timestampMilis: Long,
        var timeOfMatchStartMillis:Long
)

fun blankGamepad() = SerializableGamepad(
    touchpad = false,
    dpad_up = false,
    dpad_down = false,
    dpad_left = false,
    dpad_right = false,
    right_stick_x = 0.0f,
    right_stick_y = 0.0f,
    left_stick_x = 0.0f,
    left_stick_y = 0.0f,
    right_bumper = false,
    left_bumper = false,
    right_trigger = 0.0f,
    left_trigger = 0.0f,
    square = false,
    a = false,
    x = false,
    start = false,
    share = false,
    left_stick_button = false,
    right_stick_button = false,
    y = false,
    b = false,
    isRumbling = false,
    theGamepad = null,
)

data class SerializableGamepad(
        val touchpad: Boolean,
        val dpad_up: Boolean,
        val dpad_down: Boolean,
        val dpad_left: Boolean,
        val dpad_right: Boolean,
        val right_stick_x: Float,
        val right_stick_y: Float,
        val left_stick_x: Float,
        val left_stick_y: Float,
        val right_bumper: Boolean,
        val left_bumper: Boolean,
        val right_trigger: Float,
        val left_trigger: Float,
        val square: Boolean,
        val a: Boolean,
        val x: Boolean,
        val start: Boolean,
        val share: Boolean,
        val left_stick_button: Boolean,
        val right_stick_button: Boolean,
        val y: Boolean,
        val b: Boolean,
        val isRumbling: Boolean,

        @JsonIgnore
        val theGamepad:Gamepad?,
){
    constructor(g:Gamepad):this(
        theGamepad = g,
        touchpad = g.touchpad,
        dpad_up = g.dpad_up,
        dpad_down = g.dpad_down,
        dpad_left = g.dpad_left,
        dpad_right = g.dpad_right,
        right_stick_x = g.right_stick_x,
        right_stick_y = g.right_stick_y,
        left_stick_x = g.left_stick_x,
        left_stick_y = g.left_stick_y,
        right_bumper = g.right_bumper,
        left_bumper = g.left_bumper,
        right_trigger = g.right_trigger,
        left_trigger = g.left_trigger,
        square = g.square,
        a = g.a,
        x = g.x,
        y = g.y,
        b = g.b,
        start = g.start,
        share = g.share,
        left_stick_button = g.left_stick_button,
        right_stick_button = g.right_stick_button,
        isRumbling = g.isRumbling,
    )


    fun runRumbleEffect(effect: Gamepad.RumbleEffect) = theGamepad?.runRumbleEffect(effect)
}
