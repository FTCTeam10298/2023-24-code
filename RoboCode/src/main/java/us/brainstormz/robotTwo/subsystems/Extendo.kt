package us.brainstormz.robotTwo.subsystems

import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.math.absoluteValue

class Extendo: Subsystem {
    val maxSafeCurrentAmps = 5.5

    enum class ExtendoPositions(val ticks: Int) {
        AllTheWayInTarget(-10),
        Min(0),
        ResetEncoder(0),
        Manual(0),
        ClearTransfer(230),
        CloserBackboardPixelPosition(500),
        MidBackboardPixelPosition(1000),
        FarBackboardPixelPosition(1750),
        AudiencePurpleCenterPosition(1900),
        AudiencePurpleLeftPosition(1700),
        Max(2000),
    }


    private val acceptablePositionErrorTicks = 50
    fun isExtendoAtPosition(targetPositionTicks: Int, currentPositionTicks: Int): Boolean {
        val positionErrorTicks = targetPositionTicks - currentPositionTicks
        return positionErrorTicks.absoluteValue <= acceptablePositionErrorTicks
    }

    fun isExtendoAllTheWayIn(actualRobot: ActualRobot): Boolean {
        return actualRobot.collectorSystemState.extendoCurrentAmps <= 10
    }

    fun getExtendoPositionTicks(hardware: RobotTwoHardware): Int = hardware.extendoMotorMaster.currentPosition
//    fun arePixelsAlignedInTransfer(): Boolean {
//        val isLeftFlapAngleAcceptable = isFlapAtAngle(getFlapAngleDegrees(leftEncoderReader), leftFlapTransferReadyAngleDegrees, flapAngleToleranceDegrees = 20.0)
//        val isRightFlapAngleAcceptable = isFlapAtAngle(getFlapAngleDegrees(rightEncoderReader), rightFlapTransferReadyAngleDegrees, flapAngleToleranceDegrees = 20.0)
//        return isLeftFlapAngleAcceptable && isRightFlapAngleAcceptable
//    }
//
//    fun moveCollectorAllTheWayIn() {
//        if (!isExtendoAllTheWayIn()) {
//            powerExtendo(-0.5)
//        }
//    }


    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        val allowedPower = if (hardware.extendoMotorMaster.isOverCurrent) {
            0.0
        } else {
            power
        }

        hardware.extendoMotorMaster.power = allowedPower
        hardware.extendoMotorSlave.power = allowedPower
    }

    private val pid = PID(kp = 0.002)
    fun calcPowerToMoveExtendo(targetPositionTicks: Int, actualRobot: ActualRobot): Double {
        val currentPosition = actualRobot.collectorSystemState.extendoPositionTicks
        val positionError = targetPositionTicks - currentPosition.toDouble()
        val power = pid.calcPID(positionError)
        return power
    }
//    fun moveExtendoToPosition(targetPositionTicks: Int) {
//        powerExtendo(calcPowerToMoveExtendo(targetPositionTicks))
//    }
}