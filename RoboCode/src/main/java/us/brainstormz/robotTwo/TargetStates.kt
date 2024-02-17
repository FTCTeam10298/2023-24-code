package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
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
        val extendo: SlideSubsystem.TargetSlideSubsystem,
        val timeOfEjectionStartMilis: Long?,
        val intakeNoodles: Intake.CollectorPowers,
        val rollers: Transfer.RollerState
) {
    override fun toString(): String = DataClassHelper.dataClassToString(this)
}
data class TargetRobot(
        val drivetrainTarget: Drivetrain.DrivetrainTarget,
        val depoTarget: DepoTarget,
        val collectorTarget: CollectorTarget,
        val hangPowers: RobotTwoHardware.HangPowers,
        val launcherPosition: RobotTwoHardware.LauncherPosition,
        val lights: RobotTwoTeleOp.LightTarget
) {
    override fun toString(): String = DataClassHelper.dataClassToString(this)
}
data class TargetWorld(
        val targetRobot: TargetRobot,
        val driverInput: RobotTwoTeleOp.DriverInput,
        val isLiftEligableForReset: Boolean,
        val doingHandoff: Boolean,
        val isTargetReached: (previousTargetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld) -> Boolean,
        val timeTargetStartedMilis: Long = 0,
        val gamepad1Rumble: RobotTwoTeleOp.RumbleEffects?
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
        val positionAndRotation: PositionAndRotation,
        val depoState: DepoManager.ActualDepo,
        val collectorSystemState: CollectorSystem.ActualCollector,
) {
    override fun toString(): String = DataClassHelper.dataClassToString(this)
}
data class ActualWorld(
        val actualRobot: ActualRobot,
        val actualGamepad1: Gamepad,
        val actualGamepad2: Gamepad,
        val timestampMilis: Long
) {
    override fun toString(): String = DataClassHelper.dataClassToString(this)
}



