package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.DataClassHelper

data class DepoTarget(
        val armPosition: Arm.Positions,
        val lift: Lift.TargetLift,
        val wristPosition: Wrist.WristTargets,
        val targetType: DepoManager.DepoTargetType
) {
    override fun toString(): String = DataClassHelper.dataClassToString(this)
}
data class CollectorTarget(
    var extendo: SlideSubsystem.TargetSlideSubsystem,
    var timeOfEjectionStartMilis: Long?,
    var intakeNoodles: Intake.CollectorPowers,
    var transferState: Transfer.TransferState,
    var rollers: Transfer.TransferTarget,
) {
    override fun toString(): String = DataClassHelper.dataClassToString(this)
}
data class TargetRobot(
    var drivetrainTarget: Drivetrain.DrivetrainTarget,
    var depoTarget: DepoTarget,
    var collectorTarget: CollectorTarget,
    var hangPowers: RobotTwoHardware.HangPowers,
    var launcherPosition: RobotTwoHardware.LauncherPosition,
    var lights: RobotTwoTeleOp.LightTarget
) {
    override fun toString(): String = DataClassHelper.dataClassToString(this)
}
data class TargetWorld(
    var targetRobot: TargetRobot,
    var driverInput: RobotTwoTeleOp.DriverInput,
    var isLiftEligableForReset: Boolean,
    var doingHandoff: Boolean,
    var getNextTask: (targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld) -> TargetWorld?,
    val timeTargetStartedMilis: Long = 0,
    var gamepad1Rumble: RobotTwoTeleOp.RumbleEffects?
) {
    override fun toString(): String = DataClassHelper.dataClassToString(this)

    override fun equals(other: Any?): Boolean {
        return if (other is TargetWorld) {
            super.equals(other)
        } else {
            false
        }
    }
}


data class ActualRobot(
    var positionAndRotation: PositionAndRotation,
    var depoState: DepoManager.ActualDepo,
    var collectorSystemState: CollectorSystem.ActualCollector,
    var neopixelState: Neopixels.StripState
) {
    override fun toString(): String = DataClassHelper.dataClassToString(this)
}
data class ActualWorld(
    var actualRobot: ActualRobot,
    var aprilTagReadings: List<AprilTagDetection> = listOf(),
    var actualGamepad1: Gamepad,
    var actualGamepad2: Gamepad,
    var timestampMilis: Long
) {
    override fun toString(): String = DataClassHelper.dataClassToString(this)
}



