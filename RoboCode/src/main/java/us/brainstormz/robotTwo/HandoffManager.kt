package us.brainstormz.robotTwo

import com.qualcomm.hardware.rev.RevBlinkinLedDriver.BlinkinPattern
import org.firstinspires.ftc.robotcore.external.Telemetry

class HandoffManager(
        private val collectorSystem: CollectorSystem,
        private val lift: Lift,
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


    fun getHandoffState(previousClawState: ClawStateFromHandoff, previousLights: BlinkinPattern): HandoffState {
        val isArmAtAPositionWhichAllowsTheLiftToMoveDown = arm.getArmAngleDegrees() >= Arm.Positions.ClearLiftMovement.angleDegrees

//        telemetry.addLine("Lift")
        val liftState: LiftStateFromHandoff = when (isArmAtAPositionWhichAllowsTheLiftToMoveDown) {
            true -> {
//                telemetry.addLine("Moving lift down\n")
                LiftStateFromHandoff.MoveDown
            }
            false -> {
//                telemetry.addLine("Not moving lift, arm is not ready\n")
                LiftStateFromHandoff.None
            }
        }

//        telemetry.addLine("Extendo: ")
        val liftIsDownEnoughForExtendoToComeIn = lift.getCurrentPositionTicks() < (Lift.LiftPositions.Min.ticks + 100)
        val collectorState: ExtendoStateFromHandoff = when (liftIsDownEnoughForExtendoToComeIn) {
            true -> {
//                telemetry.addLine("Moving collector in")
                ExtendoStateFromHandoff.MoveIn
            }
            false -> {
//                telemetry.addLine("Moving collector out of the way")
                ExtendoStateFromHandoff.MoveOutOfTheWay
            }
        }

        val isCollectorAllTheWayIn = collectorSystem.isExtendoAllTheWayIn()
        val liftExtensionIsAllTheWayDown = lift.isLimitSwitchActivated()
        val bothExtensionsAreAllTheWayIn = liftExtensionIsAllTheWayDown && isCollectorAllTheWayIn

//        telemetry.addLine("Arms: ")
        val armState: Arm.Positions = when (bothExtensionsAreAllTheWayIn) {
            true -> {
                telemetry.addLine("Moving arm to transfer")
                Arm.Positions.TransferringTarget
            }
            false -> {
                telemetry.addLine("Moving arm to avoid getting caught while lift does it's thing")
                Arm.Positions.ClearLiftMovement
            }
        }
        val isArmReadyToTransfer = arm.getArmAngleDegrees() <= Arm.Positions.In.angleDegrees
        val areRollersReadyToTransfer = true//collector.arePixelsAlignedInTransfer()
        val readyToTransfer = bothExtensionsAreAllTheWayIn && isArmReadyToTransfer && areRollersReadyToTransfer

        val clawsShouldRetract = !isCollectorAllTheWayIn && !liftExtensionIsAllTheWayDown
//        telemetry.addLine("Claws:")
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
//                telemetry.addLine("Not sure what to do, retracting claws")
//                ClawStateFromHandoff.Retracted
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