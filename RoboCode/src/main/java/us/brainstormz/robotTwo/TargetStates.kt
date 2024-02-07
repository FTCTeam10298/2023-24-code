package us.brainstormz.robotTwo

import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.hardware.rev.RevBlinkinLedDriver.BlinkinPattern
import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.localizer.PositionAndRotation


data class DepoTarget(
        val armPosition: Arm.Positions,
        val liftPosition: Lift.LiftPositions,
        val leftClawPosition: Claw.ClawTarget,
        val rightClawPosition: Claw.ClawTarget,
)
data class CollectorTarget(
        val extendoPositions: CollectorSystem.ExtendoPositions,
        val intakeNoodles: CollectorSystem.CollectorPowers,
        val rollers: CollectorSystem.RollerState
)
data class TargetRobot(
        val positionAndRotation: PositionAndRotation,
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
        val isTargetReached: (previousTargetState: TargetWorld, actualState: ActualWorld) -> Boolean,
        val timeTargetStartedMilis: Long = 0
)


data class ActualRobot(
        val positionAndRotation: PositionAndRotation,
        val depoState: DepoManager.ActualDepo,
        val collectorSystemState: CollectorSystem.ActualCollector,
)
data class ActualWorld(
        val actualRobot: ActualRobot,
        val actualGamepad1: Gamepad,
        val actualGamepad2: Gamepad,
        val timestampMilis: Long
)



