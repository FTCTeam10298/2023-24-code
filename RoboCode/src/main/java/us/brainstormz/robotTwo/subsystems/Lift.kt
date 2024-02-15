package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp
import kotlin.math.absoluteValue
import kotlin.math.sign

class Lift(private val telemetry: Telemetry): Subsystem, SlideSubsystem {

    enum class LiftPositions(val ticks: Int) {
        Manual(0),
//        ResetEncoder(0),
        Nothing(0),
        ScoringHeightAdjust(0),
        PastDown(0),
        Down(0),
        BackboardBottomRow(330),
        ClearForArmToMove(547),
        WaitForArmToMove(800),
        SetLine1(800),
        SetLine2(1400),
        SetLine3(2100),
        Max(2100)
    }

    fun getGetLiftTargetFromDepoTarget(depoInput: RobotTwoTeleOp.DepoInput): LiftPositions {
        return when (depoInput) {
            RobotTwoTeleOp.DepoInput.SetLine1 -> LiftPositions.SetLine1
            RobotTwoTeleOp.DepoInput.SetLine2 -> LiftPositions.SetLine2
            RobotTwoTeleOp.DepoInput.SetLine3 -> LiftPositions.SetLine3
            RobotTwoTeleOp.DepoInput.Down -> LiftPositions.Down
            RobotTwoTeleOp.DepoInput.Manual -> LiftPositions.Manual
            RobotTwoTeleOp.DepoInput.NoInput -> LiftPositions.Nothing
            RobotTwoTeleOp.DepoInput.ScoringHeightAdjust -> LiftPositions.ScoringHeightAdjust
        }
    }

    override fun getRawPositionTicks(hardware: RobotTwoHardware): Int = hardware.liftMotorMaster.currentPosition
    override fun getIsLimitSwitchActivated(hardware: RobotTwoHardware): Boolean = !hardware.liftMagnetLimit.state
    override val allowedMovementBeforeResetTicks: Int = 200
    override val allTheWayInTicks: Int = 0

    class ActualLift(override val currentPositionTicks: Int,
                     override val limitSwitchIsActivated: Boolean,
                     override val zeroPositionOffsetTicks: Int = 0,
                     override val ticksMovedSinceReset: Int = 0): SlideSubsystem.ActualSlideSubsystem(currentPositionTicks, limitSwitchIsActivated, zeroPositionOffsetTicks, ticksMovedSinceReset)


    private val acceptablePositionErrorTicks = 100
    fun isLiftAtPosition(targetPositionTicks: Int, actualLiftPositionTicks: Int): Boolean {
        val currentPositionTicks = actualLiftPositionTicks
        val positionErrorTicks = targetPositionTicks - currentPositionTicks
        return positionErrorTicks.absoluteValue <= acceptablePositionErrorTicks
    }

    fun isLiftAbovePosition(targetPositionTicks: Int, actualLiftPositionTicks: Int): Boolean {
        val currentPositionTicks = actualLiftPositionTicks
        val positionErrorTicks = targetPositionTicks - currentPositionTicks
        return positionErrorTicks > 0
    }


    private val liftBottomLimitAmps = 8.0
    private fun isLiftDrawingTooMuchCurrent(hardware: RobotTwoHardware) = hardware.liftMotorMaster.getCurrent(CurrentUnit.AMPS) > liftBottomLimitAmps
    //    fun getLiftCurrentAmps(hardware: RobotTwoHardware) = hardware.liftMotorMaster.getCurrent(CurrentUnit.AMPS)
    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        val allowedPower = if (isLiftDrawingTooMuchCurrent(hardware)) {
            0.0
        } else {
            power
        }

        telemetry.addLine("Powering Lift, $allowedPower")

        hardware.liftMotorMaster.power = allowedPower
        hardware.liftMotorSlave.power = allowedPower
    }

    override val pid = PID(kp = 0.0015)
    fun calculatePowerToMoveToPosition(targetPositionTicks: Int, currentPosition: Int): Double {
        val positionError = targetPositionTicks - currentPosition
        val gravityConstant = if (positionError.sign > 0) {
            0.1
        } else {
            0.0
        }
        val power = pid.calcPID(positionError.toDouble()) + gravityConstant
        return power
    }
}