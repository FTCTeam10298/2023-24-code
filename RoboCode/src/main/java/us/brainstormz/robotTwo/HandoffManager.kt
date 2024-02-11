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

    fun checkIfHandoffIsReady(actualRobot: ActualRobot, previousTargetRobot: TargetRobot): Boolean {
        val isCollectorAllTheWayIn = actualRobot.collectorSystemState.extendoLimitIsActivated
        val liftExtensionIsAllTheWayDown = actualRobot.depoState.isLiftLimitActivated
        val bothExtensionsAreAllTheWayIn = liftExtensionIsAllTheWayDown && isCollectorAllTheWayIn

        val isArmReadyToTransfer = actualRobot.depoState.armAngleDegrees <= Arm.Positions.In.angleDegrees

        val areRollersReadyToTransfer = true//collector.arePixelsAlignedInTransfer()

        val readyToHandoff = bothExtensionsAreAllTheWayIn && isArmReadyToTransfer && areRollersReadyToTransfer
        return readyToHandoff
    }

    fun getHandoffState(previousClawState: ClawStateFromHandoff, previousLights: BlinkinPattern, actualRobot: ActualRobot): HandoffState {
        val isArmAtAPositionWhichAllowsTheLiftToMoveDown = actualRobot.depoState.armAngleDegrees >= Arm.Positions.ClearLiftMovement.angleDegrees

        val liftState: LiftStateFromHandoff = when (isArmAtAPositionWhichAllowsTheLiftToMoveDown) {
            true -> {
                LiftStateFromHandoff.MoveDown
            }
            false -> {
                LiftStateFromHandoff.None
            }
        }

        val liftIsDownEnoughForExtendoToComeIn = actualRobot.depoState.liftPositionTicks < (Lift.LiftPositions.Down.ticks + 100)
        val collectorState: ExtendoStateFromHandoff = when (liftIsDownEnoughForExtendoToComeIn) {
            true -> {
                ExtendoStateFromHandoff.MoveIn
            }
            false -> {
                ExtendoStateFromHandoff.MoveOutOfTheWay
            }
        }

        val isCollectorAllTheWayIn = extendo.isExtendoAllTheWayIn(actualRobot)
        val liftExtensionIsAllTheWayDown = actualRobot.depoState.liftPositionTicks <= Lift.LiftPositions.Down.ticks//actualRobot.depoState.isLiftLimitActivated
        val bothExtensionsAreAllTheWayIn = liftExtensionIsAllTheWayDown && isCollectorAllTheWayIn

        val armState: Arm.Positions = when (bothExtensionsAreAllTheWayIn) {
            true -> {
                Arm.Positions.TransferringTarget
            }
            false -> {
                Arm.Positions.ClearLiftMovement
            }
        }
        val isArmReadyToTransfer = actualRobot.depoState.armAngleDegrees <= Arm.Positions.In.angleDegrees
        val areRollersReadyToTransfer = true//collector.arePixelsAlignedInTransfer()
        val readyToTransfer = bothExtensionsAreAllTheWayIn && isArmReadyToTransfer && areRollersReadyToTransfer
        telemetry.addLine("readyToTransfer: $readyToTransfer \nisArmReadyToTransfer: $isArmReadyToTransfer \nliftExtensionIsAllTheWayDown: $liftExtensionIsAllTheWayDown \nisCollectorAllTheWayIn: $isCollectorAllTheWayIn")

        val clawsShouldRetract = !isCollectorAllTheWayIn || !liftExtensionIsAllTheWayDown
        val clawState: ClawStateFromHandoff = when {
            readyToTransfer -> {
                telemetry.addLine("Gripping, transfer is complete")
                ClawStateFromHandoff.Gripping
            }
            clawsShouldRetract -> {
                telemetry.addLine("Not gripping, we're not ready")
                ClawStateFromHandoff.Retracted
            }
            else -> {
                telemetry.addLine("Not sure what to do so just doing the same thing as before")
                previousClawState
            }
        }

        val lights: BlinkinPattern = when (readyToTransfer) {
            true -> BlinkinPattern.CONFETTI
            false -> previousLights
        }

        return HandoffState(
                armState = armState,
                lights = lights,
                liftState = liftState,
                collectorState = collectorState,
                clawPosition = clawState
        )
    }
}