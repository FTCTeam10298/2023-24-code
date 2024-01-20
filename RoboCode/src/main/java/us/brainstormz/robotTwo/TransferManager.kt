package us.brainstormz.robotTwo

import com.qualcomm.hardware.rev.RevBlinkinLedDriver.BlinkinPattern
import org.firstinspires.ftc.robotcore.external.Telemetry

class TransferManager(
        private val collector: Collector,
        private val lift: Lift,
        private val arm: Arm,
        private val telemetry: Telemetry) {


    enum class ExtendoStateFromTransfer {
        MoveIn,
        MoveOutOfTheWay
    }
    enum class LiftStateFromTransfer {
        MoveDown,
        None
    }
    enum class ClawStateFromTransfer {
        Gripping,
        Retracted
    }
    data class TransferState(
//            val rightClawPosition: RobotTwoHardware.RightClawPosition,
//            val leftClawPosition: RobotTwoHardware.LeftClawPosition,
            val clawPosition: ClawStateFromTransfer,
            val collectorState: ExtendoStateFromTransfer,
            val liftState:LiftStateFromTransfer,
            val lights: BlinkinPattern,
            val armState: Arm.Positions)


    fun getTransferState(previousClawState: ClawStateFromTransfer, previousLights: BlinkinPattern): TransferState {
        val isArmAtAPositionWhichAllowsTheLiftToMoveDown = arm.getArmAngleDegrees() >= Arm.Positions.In.angleDegrees

        telemetry.addLine("Lift")
        val liftState: LiftStateFromTransfer = when (isArmAtAPositionWhichAllowsTheLiftToMoveDown) {
            true -> {
                telemetry.addLine("Moving lift down\n")
                LiftStateFromTransfer.MoveDown
            }
            false -> {
                telemetry.addLine("Not moving lift, arm is not ready\n")
                LiftStateFromTransfer.None
            }
        }

        telemetry.addLine("Extendo: ")
        val liftExtensionIsAllTheWayDown = lift.isLimitSwitchActivated()
        val collectorState: ExtendoStateFromTransfer = when (liftExtensionIsAllTheWayDown) {
            true -> {
                telemetry.addLine("Moving collector in")
                ExtendoStateFromTransfer.MoveIn
            }
            false -> {
                telemetry.addLine("Moving collector out of the way")
                ExtendoStateFromTransfer.MoveOutOfTheWay
            }
        }

        val isCollectorAllTheWayIn = collector.isExtendoAllTheWayIn()
        val bothExtensionsAreAllTheWayIn = liftExtensionIsAllTheWayDown && isCollectorAllTheWayIn

        telemetry.addLine("Arms: ")
        val armState: Arm.Positions = when (bothExtensionsAreAllTheWayIn) {
            true -> {
                telemetry.addLine("Moving arm to transfer")
                Arm.Positions.TransferringTarget
            }
            false -> {
                telemetry.addLine("Moving arm to avoid getting caught while lift does it's thing")
                Arm.Positions.LiftIsGoingHome
            }
        }
        val isArmReadyToTransfer = arm.getArmAngleDegrees() <= Arm.Positions.In.angleDegrees
        val areRollersReadyToTransfer = true//collector.arePixelsAlignedInTransfer()
        val readyToTransfer = bothExtensionsAreAllTheWayIn && isArmReadyToTransfer && areRollersReadyToTransfer

        val clawsShouldRetract = !isCollectorAllTheWayIn && !liftExtensionIsAllTheWayDown
        telemetry.addLine("Claws:")
        val clawState: ClawStateFromTransfer = when {
            readyToTransfer -> {
                telemetry.addLine("Gripping, transfer is complete")
                ClawStateFromTransfer.Gripping
            }
            clawsShouldRetract -> {
                telemetry.addLine("Not gripping, we're not ready")
                ClawStateFromTransfer.Retracted
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

        return TransferState(
                armState = armState,
                lights = lights,
                liftState = liftState,
                collectorState = collectorState,
                clawPosition = clawState
        )
    }

//    var armPosition = Arm.Positions.In
//    fun transfer() {
//
//        val isArmAtAPositionWhichAllowsTheLiftToMoveDown = arm.getArmAngleDegrees() >= Arm.Positions.In.angleDegrees
//
//        telemetry.addLine("\n\nMoving collector and claws to the transfer positions")
//        if (isArmAtAPositionWhichAllowsTheLiftToMoveDown) {
//            telemetry.addLine("\n\nMoving lift to the transfer position because arm is out of the way")
//            lift.moveLiftToBottom()
//        } else {
//            lift.powerLift(0.0)
//        }
//
//        val liftExtensionIsAllTheWayDown = lift.isLimitSwitchActivated()
//        if (liftExtensionIsAllTheWayDown) {
//            collector.moveCollectorAllTheWayIn()
//        } else {
//            collector.moveExtendoToPosition(Collector.ExtendoPositions.BeforeTransfer.ticks)
//        }
//
//        val isCollectorAllTheWayIn = collector.isCollectorAllTheWayIn()
//        val bothExtensionsAreAllTheWayIn = liftExtensionIsAllTheWayDown && isCollectorAllTheWayIn
//        if (bothExtensionsAreAllTheWayIn) {
//            telemetry.addLine("\n\nExtensions are ready to transfer")
//
//            armPosition = Arm.Positions.In
//            arm.moveArmTowardPosition(armPosition.angleDegrees)
//            val isArmReadyToTransfer = arm.getArmAngleDegrees() <= Arm.Positions.ReadyToTransfer.angleDegrees
//            telemetry.addLine("isArmReadyToTransfer: $isArmReadyToTransfer")
//            val areRollersReadyToTransfer = true//collector.arePixelsAlignedInTransfer()
//            telemetry.addLine("areRollersReadyToTransfer: $areRollersReadyToTransfer")
//
//            if (isArmReadyToTransfer && areRollersReadyToTransfer) {
//                telemetry.addLine("\n\nArm and flaps are ready to transfer")
//
//                val isPixelInLeftTransfer = true//collector.isPixelIn(leftTransferSensor)
//                if (isPixelInLeftTransfer) {
//                    leftClawServo.position = RobotTwoHardware.LeftClawPosition.Gripping.position
//                }
//                val isPixelInRightTransfer = true//collector.isPixelIn(rightTransferSensor)
//                if (isPixelInRightTransfer) {
//                    rightClawServo.position = RobotTwoHardware.RightClawPosition.Gripping.position
//                }
//
//                lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.CONFETTI)
//                telemetry.addLine("\n\nTransfer done")
//            }
//        } else {
//            telemetry.addLine("\n\nMoving arm and claws to the transfer position")
//
//            armPosition = Arm.Positions.ReadyToTransfer
//            arm.moveArmTowardPosition(armPosition.angleDegrees)
//
//            //|| LiftIsn'tDown
//            if (collector.isPixelIn(rightTransferSensor) && !isCollectorAllTheWayIn && !liftExtensionIsAllTheWayDown) {
//                rightClawServo.position = RobotTwoHardware.RightClawPosition.Retracted.position
//            }
//            if (collector.isPixelIn(leftTransferSensor) && !isCollectorAllTheWayIn && !liftExtensionIsAllTheWayDown) {
//                leftClawServo.position = RobotTwoHardware.LeftClawPosition.Retracted.position
//            }
//        }
//    }
}