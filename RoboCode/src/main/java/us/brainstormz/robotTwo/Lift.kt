package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DigitalChannel
import us.brainstormz.pid.PID

class Lift(private val liftMotor1: DcMotor, private val liftMotor2: DcMotor, private val liftLimit: DigitalChannel) {
    fun isLiftDown(): Boolean = !liftLimit.state


    enum class LiftPositions(val position: Double) {
        Min(0.0),
        Transfer(0.5),
        BackboardBottomRow(1.0),
        SetLine1(2.0),
        SetLine2(3.0),
        SetLine3(4.0),
        Max(500.0)
    }
//    val liftPositionPID = PID(kp = 1.0)
//
//    enum class LiftToggleOptions {
//        SetLine1,
//        SetLine2,
//        SetLine3
//    }

    fun powerLift(power: Double) {
//        val currentPosition = liftMotor1.currentPosition.toDouble()
//        val allowedPower = power
//        if (currentPosition > RobotTwoHardware.LiftPositions.Max.position) {
//            power.coerceAtMost(0.0)
//        } else if (currentPosition < RobotTwoHardware.LiftPositions.Min.position) {
//            power.coerceAtLeast(0.0)
//        } else {
//            power
//        }

        liftMotor1.power = power
        liftMotor2.power = power
    }
//    private fun moveLiftTowardPosition(targetPosition: Double) {
//        val currentPosition = hardware.liftMotorMaster.currentPosition.toDouble()
//        val power = hardware.liftPositionPID.calcPID(targetPosition, currentPosition)
//        powerLift(power)
//    }
}