package us.brainstormz.robotTwo.subsystems

import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp
import kotlin.math.absoluteValue
import kotlin.math.sign
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*

class Lift(override val telemetry: Telemetry): Subsystem, SlideSubsystem {

    companion object {
        val oldToNewMotorEncoderConversion: Double = (384.5) / (537.7)
    }

    enum class LiftPositions(override val ticks: Int): SlideSubsystem.SlideTargetPosition {
        PastDown(0),
        Down(0),
        AutoLowYellowPlacement((330*oldToNewMotorEncoderConversion).toInt()),
        AutoAbovePartnerPlacement((500*oldToNewMotorEncoderConversion).toInt()),
        ClearForArmToMove((547*oldToNewMotorEncoderConversion).toInt()),
        WaitForArmToMove((800*oldToNewMotorEncoderConversion).toInt()),
        SetLine1((500*oldToNewMotorEncoderConversion).toInt()),
        SetLine2Other((700*oldToNewMotorEncoderConversion).toInt()),
        SetLine2((1000*oldToNewMotorEncoderConversion).toInt()),
        SetLine3((1800*oldToNewMotorEncoderConversion).toInt()),
        Max((2100*oldToNewMotorEncoderConversion).toInt())
    }

    fun getGetLiftTargetFromDepoTarget(depoInput: RobotTwoTeleOp.DepoInput, position: Double): SlideSubsystem.SlideTargetPosition {
        return when (depoInput) {
            RobotTwoTeleOp.DepoInput.Preset1 -> LiftPositions.SetLine1
            RobotTwoTeleOp.DepoInput.Preset2 -> LiftPositions.SetLine2Other
            RobotTwoTeleOp.DepoInput.Preset3 -> LiftPositions.SetLine2
            RobotTwoTeleOp.DepoInput.Preset4 -> LiftPositions.SetLine3
            RobotTwoTeleOp.DepoInput.Down -> LiftPositions.Down
//            RobotTwoTeleOp.DepoInput.NoInput -> LiftPositions.Nothing
//            RobotTwoTeleOp.DepoInput.ScoringHeightAdjust -> SlideSubsystem.VariableTargetPosition(ticks = position.toInt())
//            RobotTwoTeleOp.DepoInput.Manual -> SlideSubsystem.VariableTargetPosition(0)
            else -> SlideSubsystem.VariableTargetPosition(ticks = position.toInt())
        }
    }

    override fun getRawPositionTicks(hardware: RobotTwoHardware): Int = hardware.liftMotorMaster.currentPosition
    override fun getIsLimitSwitchActivated(hardware: RobotTwoHardware): Boolean = !hardware.liftMagnetLimit.state
    override fun getCurrentAmps(hardware: RobotTwoHardware): Double = hardware.liftMotorMaster.getCurrent(CurrentUnit.AMPS)

    override val allowedMovementBeforeResetTicks: Int = 700
    override val allTheWayInPositionTicks: Int = 0
    override val stallCurrentAmps: Double = 5.0
    override val definitelyMovingVelocityTicksPerMili: Double = 0.005
    override val findResetPower: Double = 0.7

    data class ActualLift(override val currentPositionTicks: Int,
                     override val limitSwitchIsActivated: Boolean,
                     override val zeroPositionOffsetTicks: Int = 0,
                     override val ticksMovedSinceReset: Int = 0,
                     override val currentAmps: Double = 0.0): SlideSubsystem.ActualSlideSubsystem(currentPositionTicks, limitSwitchIsActivated, zeroPositionOffsetTicks, ticksMovedSinceReset, currentAmps)

    data class TargetLift(override val targetPosition: SlideSubsystem.SlideTargetPosition = LiftPositions.Down,
                          override val power: Double = 0.0,
                          override val movementMode: MovementMode = MovementMode.Position,
                          override val timeOfResetMoveDirectionStartMilis: Long = 0): SlideSubsystem.TargetSlideSubsystem(targetPosition, movementMode, power, timeOfResetMoveDirectionStartMilis) {
                              constructor(targetSlideSubsystem: SlideSubsystem.TargetSlideSubsystem):
                                    this(   targetPosition= targetSlideSubsystem.targetPosition,
                                            power= targetSlideSubsystem.power,
                                            movementMode= targetSlideSubsystem.movementMode,
                                            timeOfResetMoveDirectionStartMilis= targetSlideSubsystem.timeOfResetMoveDirectionStartMilis,)
    }


    private val acceptablePositionErrorTicks = 70
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

    override val pid = PID("lift", kp = 0.0025)
    fun calculatePowerToMoveToPosition(targetPositionTicks: Int, currentPosition: Int): Double {
        val positionError = targetPositionTicks - currentPosition
        val gravityConstant = if (positionError.sign > 0) {
            0.1
        } else {
            0.0
        }
        val power = pid.calcPID(
                target = targetPositionTicks,
                error = positionError.toDouble()) + gravityConstant
        return power
    }
}