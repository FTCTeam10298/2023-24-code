package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DigitalChannel
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.pid.PID
import kotlin.math.absoluteValue

class Lift(private val liftMotor1: DcMotorEx, private val liftMotor2: DcMotor, private val liftLimit: DigitalChannel) {


    enum class LiftPositions(val ticks: Int) {
        Manual(0),
        Nothing(0),
        Min(0),
        Transfer(0),
        BackboardBottomRow(330),
        ClearForArmToMove(450),
        WaitForArmToMove(800),
        SetLine1(900),
        SetLine2(1500),
        SetLine3(2300),
        Max(2300)
    }

    fun powerLift(power: Double) {

        val allowedPower = if (isLiftDrawingTooMuchCurrent()) {
            0.0
        } else {
            power
        }

        liftMotor1.power = allowedPower
        liftMotor2.power = allowedPower
    }

    fun isLimitSwitchActivated(): Boolean = !liftLimit.state

    private val liftBottomLimitAmps = 8.0

    fun isLiftDrawingTooMuchCurrent() = liftMotor1.getCurrent(CurrentUnit.AMPS) > liftBottomLimitAmps

    fun getCurrentPositionTicks(): Int {
        return  liftMotor1.currentPosition
    }

    private val pid = PID(kp = 0.004)
    fun moveLiftToPosition(targetPositionTicks: Int) {
        powerLift(calculatePowerToMoveToPosition(targetPositionTicks))
    }
    fun calculatePowerToMoveToPosition(targetPositionTicks: Int): Double {
        val currentPosition = liftMotor1.currentPosition.toDouble()
        val positionError = targetPositionTicks - currentPosition
        val power = pid.calcPID(positionError)
        return power
    }

    private val acceptablePositionErrorTicks = 100
    fun isLiftAtPosition(targetPositionTicks: Int): Boolean {
        val currentPositionTicks = liftMotor1.currentPosition
        val positionErrorTicks = targetPositionTicks - currentPositionTicks
        return positionErrorTicks.absoluteValue <= acceptablePositionErrorTicks
    }

}