package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

data class DepoTarget(
        val armPosition: Arm.ArmTarget,
        val lift: Lift.TargetLift,
        val wristPosition: Wrist.WristTargets,
        val targetType: DepoManager.DepoTargetType
)
data class CollectorTarget(
        val extendo: SlideSubsystem.TargetSlideSubsystem,
        val timeOfEjectionStartMilis: Long?,
        val intakeNoodles: Intake.TargetIntake,
        val transferState: Transfer.TransferState,
        val rollers: Transfer.TransferTarget,
)
data class TargetRobot(
        val drivetrainTarget: Drivetrain.DrivetrainTarget,
        val depoTarget: DepoTarget,
        val collectorTarget: CollectorTarget,
        val hangPowers: RobotTwoHardware.HangPowers,
        val launcherPosition: RobotTwoHardware.LauncherPosition,
        val lights: RobotTwoTeleOp.LightTarget
)
data class TargetWorld(
        val targetRobot: TargetRobot,
        val driverInput: RobotTwoTeleOp.DriverInput,
        val isLiftEligableForReset: Boolean,
        val doingHandoff: Boolean,
        val getNextTask: (targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld) -> TargetWorld?,
        val timeTargetStartedMilis: Long = 0,
        val gamepad1Rumble: RobotTwoTeleOp.RumbleEffects?
) {
    override fun equals(other: Any?): Boolean {
        return if (other is TargetWorld) {
            super.equals(other)
        } else {
            false
        }
    }
}


data class ActualRobot(
        val positionAndRotation: PositionAndRotation,
        val depoState: DepoManager.ActualDepo,
        val collectorSystemState: CollectorSystem.ActualCollector,
        val neopixelState: Neopixels.StripState
)
data class ActualWorld(
        val actualRobot: ActualRobot,
        val aprilTagReadings: List<AprilTagDetection> = listOf(),
        val actualGamepad1: Gamepad,
        val actualGamepad2: Gamepad,
        val timestampMilis: Long
) 



