package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.localizer.PositionAndRotation

data class TargetRobot(
        val positionAndRotation: PositionAndRotation,
        val depoState: DepoState,
        val collectorSystemState: CollectorSystem.CollectorState,
        val isTargetReached: (previousTargetState: TargetWorld, actualState: ActualWorld) -> Boolean,
)
data class TargetWorld(
        val targetRobot: TargetRobot,
        val timeTargetStartedMilis: Long = 0)


data class ActualRobot(
        val positionAndRotation: PositionAndRotation,
        val depoState: DepoState,
        val collectorSystemState: CollectorSystem.CollectorState,
)
data class ActualWorld(
        val actualRobot: ActualRobot,
        val actualGamepad1: Gamepad,
        val actualGamepad2: Gamepad,
        val timestampMilis: Long)



data class DepoState(
        val armPos: Arm.Positions,
        val liftPosition: Lift.LiftPositions,
        val leftClawPosition: RobotTwoHardware.LeftClawPosition,
        val rightClawPosition: RobotTwoHardware.RightClawPosition,
)
