package us.brainstormz.robotTwo

import com.qualcomm.hardware.rev.RevBlinkinLedDriver.BlinkinPattern
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Lift

class HandoffManager(
        private val collectorSystem: CollectorSystem,
        private val lift: Lift,
        private val extendo: Extendo,
        private val arm: Arm,
        private val telemetry: Telemetry) {


    enum class ExtendoStateFromHandoff {
        MoveIn,
        MoveOutOfTheWay
    }
    enum class LiftStateFromHandoff {
        MoveDown,
        None
    }
    enum class ClawStateFromHandoff {
        Gripping,
        Retracted
    }
    data class HandoffState(
//            val rightClawPosition: RobotTwoHardware.RightClawPosition,
//            val leftClawPosition: RobotTwoHardware.LeftClawPosition,
            val clawPosition: ClawStateFromHandoff,
            val collectorState: ExtendoStateFromHandoff,
            val liftState: LiftStateFromHandoff,
            val lights: BlinkinPattern,
            val armState: Arm.Positions)

    fun checkIfHandoffIsReady(actualWorld: ActualWorld, previousActualWorld: ActualWorld): Boolean {
        val isExtendoAllTheWayIn = extendo.isSlideSystemAllTheWayIn(actualWorld.actualRobot.collectorSystemState.extendo)
        val extendoIsMovingInOrNotAtAll = extendo.getVelocityTicksPerMili(actualWorld, previousActualWorld) <= 0
        val extendoIsReady = extendoIsMovingInOrNotAtAll && isExtendoAllTheWayIn

        val liftExtensionIsAllTheWayDown = lift.isSlideSystemAllTheWayIn(actualWorld.actualRobot.depoState.lift)//10 >= actualWorld.actualRobot.depoState.lift.currentPositionTicks//actualWorld.actualRobot.depoState.isLiftLimitActivated
        telemetry.addLine("isExtendoAllTheWayIn: $isExtendoAllTheWayIn")
        telemetry.addLine("liftExtensionIsAllTheWayDown: $liftExtensionIsAllTheWayDown")

        val isArmReadyToTransfer = arm.checkIfArmIsAtTarget(Arm.Positions.In, actualWorld.actualRobot.depoState.armAngleDegrees)
        telemetry.addLine("isArmReadyToTransfer: $isArmReadyToTransfer")

        val readyToHandoff = extendoIsReady && liftExtensionIsAllTheWayDown && isArmReadyToTransfer
        return readyToHandoff
    }
}