package us.brainstormz.robotTwo.subsystems

import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp
import kotlin.math.absoluteValue

class Lift(private val telemetry: Telemetry): Subsystem {

    enum class LiftPositions(val ticks: Int) {
        Manual(0),
        ResetEncoder(0),
        Nothing(0),
        Down(0),
        BackboardBottomRow(330),
        ClearForArmToMove(450),
        WaitForArmToMove(800),
        SetLine1(800),
        SetLine2(1400),
        SetLine3(2100),
        Max(2300)
    }

    fun getGetLiftTargetFromDepoTarget(depoInput: RobotTwoTeleOp.DepoInput): LiftPositions {
        return when (depoInput) {
            RobotTwoTeleOp.DepoInput.SetLine1 -> LiftPositions.SetLine1
            RobotTwoTeleOp.DepoInput.SetLine2 -> LiftPositions.SetLine2
            RobotTwoTeleOp.DepoInput.SetLine3 -> LiftPositions.SetLine3
            RobotTwoTeleOp.DepoInput.Down -> LiftPositions.Down
            RobotTwoTeleOp.DepoInput.Manual -> LiftPositions.Manual
            RobotTwoTeleOp.DepoInput.NoInput -> LiftPositions.Nothing
        }
    }

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


    fun isLimitSwitchActivated(hardware: RobotTwoHardware): Boolean = !hardware.liftMagnetLimit.state

    private val liftBottomLimitAmps = 8.0
    fun isLiftDrawingTooMuchCurrent(hardware: RobotTwoHardware) = hardware.liftMotorMaster.getCurrent(CurrentUnit.AMPS) > liftBottomLimitAmps

    fun getCurrentPositionTicks(hardware: RobotTwoHardware): Int {
        return  hardware.liftMotorMaster.currentPosition
    }

    private val pid = PID(kp = 0.004)
    fun calculatePowerToMoveToPosition(targetPositionTicks: Int, currentPosition: Int): Double {
        val positionError = targetPositionTicks - currentPosition
        val power = pid.calcPID(positionError.toDouble())
        return power
    }

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

}