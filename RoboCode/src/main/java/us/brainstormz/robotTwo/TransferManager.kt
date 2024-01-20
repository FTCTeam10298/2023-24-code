package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.ColorSensor
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.Telemetry

class TransferManager(
        private val rightClawServo: Servo,
        private val leftClawServo: Servo,
        private val leftTransferSensor: ColorSensor,
        private val rightTransferSensor: ColorSensor,
        private val collector: Collector,
        private val lift: Lift,
        private val arm: Arm,
        private val telemetry: Telemetry) {

    enum class CollectorStateFromTransfer {
        MoveIn,
        None
    }
    enum class LiftStateFromTransfer {
        MoveDown,
        None
    }
    data class TransferState(
            val rightClawPosition: RobotTwoHardware.RightClawPosition,
            val leftClawPosition: RobotTwoHardware.LeftClawPosition,
            val collectorState: CollectorStateFromTransfer,
            val liftState:LiftStateFromTransfer,
            val armState: Arm.Positions)

    fun transfer() {
        collector.moveCollectorAllTheWayIn()

        val isArmAtAPositionWhichAllowsTheLiftToMoveDown = arm.getArmAngleDegrees() >= Arm.Positions.TravelingToTransfer.angleDegrees

        telemetry.addLine("\n\nMoving collector and claws to the transfer positions")
        if (isArmAtAPositionWhichAllowsTheLiftToMoveDown) {
            telemetry.addLine("\n\nMoving lift to the transfer position because arm is out of the way")
            lift.moveLiftToBottom()
        }

        val bothExtensionsAreAllTheWayIn = lift.isLimitSwitchActivated() && collector.isCollectorAllTheWayIn()
        if (bothExtensionsAreAllTheWayIn) {
            telemetry.addLine("\n\nExtensions are ready to transfer")

            arm.moveArmTowardPosition(Arm.Positions.In.angleDegrees)
            val isArmReadyToTransfer = arm.getArmAngleDegrees() <= Arm.Positions.ReadyToTransfer.angleDegrees
            telemetry.addLine("isArmReadyToTransfer: $isArmReadyToTransfer")
            val areRollersReadyToTransfer = collector.arePixelsAlignedInTransfer()
            telemetry.addLine("areRollersReadyToTransfer: $areRollersReadyToTransfer")

            if (isArmReadyToTransfer && areRollersReadyToTransfer) {
                telemetry.addLine("\n\nArm and flaps are ready to transfer")

                val isPixelInLeftTransfer = collector.isPixelIn(leftTransferSensor)
                if (isPixelInLeftTransfer) {
                    leftClawServo.position = RobotTwoHardware.LeftClawPosition.Gripping.position
                }
                val isPixelInRightTransfer = collector.isPixelIn(rightTransferSensor)
                if (isPixelInRightTransfer) {
                    rightClawServo.position = RobotTwoHardware.RightClawPosition.Gripping.position
                }

                telemetry.addLine("\n\nTransfer done")
            }
        } else {
            telemetry.addLine("\n\nMoving arm and claws to the transfer position")

            arm.moveArmTowardPosition(Arm.Positions.TravelingToTransfer.angleDegrees)

            rightClawServo.position = RobotTwoHardware.RightClawPosition.Retracted.position
            leftClawServo.position = RobotTwoHardware.LeftClawPosition.Retracted.position
        }
    }
}