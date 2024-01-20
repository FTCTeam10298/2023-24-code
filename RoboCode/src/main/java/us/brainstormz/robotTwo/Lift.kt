package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DigitalChannel
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.pid.PID
import kotlin.math.absoluteValue

class Lift(private val liftMotor1: DcMotorEx, private val liftMotor2: DcMotor, private val liftLimit: DigitalChannel) {


    //these aren't right
    enum class LiftPositions(val ticks: Int) {
        Manual(0),
        Nothing(0),
        ClearForArmToMove(500),
        Min(0),
        Transfer(0),
        BackboardBottomRow(700),
        SetLine1(900),
        SetLine2(1500),
        SetLine3(2300),
        Max(2300)
    }

    fun powerLift(power: Double) {
        liftMotor1.power = power
        liftMotor2.power = power
    }

    fun isLimitSwitchActivated(): Boolean = !liftLimit.state

    private val liftBottomLimitAmps = 8.0
    fun moveLiftToBottom() {
        val isLiftStalling = liftMotor1.getCurrent(CurrentUnit.AMPS) > liftBottomLimitAmps
        val isLiftDown = isLimitSwitchActivated() || isLiftStalling
        if (!isLiftDown) {
            powerLift(-0.5)
        }
    }

    private val pid = PID(kp = 0.004)
    fun moveLiftToPosition(targetPositionTicks: Int) {
        val currentPosition = liftMotor1.currentPosition.toDouble()
        val positionError = targetPositionTicks - currentPosition
        val power = pid.calcPID(positionError)
        powerLift(power)
    }

    private val acceptablePositionErrorTicks = 100
    fun isLiftAtPosition(targetPositionTicks: Int): Boolean {
        val currentPositionTicks = liftMotor1.currentPosition
        val positionErrorTicks = targetPositionTicks - currentPositionTicks
        return positionErrorTicks.absoluteValue <= acceptablePositionErrorTicks
    }


//    private fun powerLift(power: Double) {
//        val currentPosition = liftMotor1.currentPosition.toDouble()
//        val allowedPower = power
//        if (currentPosition > RobotTwoHardware.LiftPositions.Max.position) {
//            power.coerceAtMost(0.0)
//        } else if (currentPosition < RobotTwoHardware.LiftPositions.Min.position) {
//            power.coerceAtLeast(0.0)
//        } else {
//            power
//        }
//        liftMotor1.power = allowedPower
//        liftMotor2.power = allowedPower
//    }
//    val liftPositionPID = PID(kp = 1.0)
//
//    enum class LiftToggleOptions {
//        SetLine1,
//        SetLine2,
//        SetLine3
//    }

//    private fun moveLiftTowardPosition(targetPosition: Double) {
//        val currentPosition = hardware.liftMotorMaster.currentPosition.toDouble()
//        val power = hardware.liftPositionPID.calcPID(targetPosition, currentPosition)
//        powerLift(power)
//    }
}