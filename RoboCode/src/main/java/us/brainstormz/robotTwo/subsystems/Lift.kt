package us.brainstormz.robotTwo.subsystems

import com.fasterxml.jackson.annotation.JsonTypeName
import kotlinx.serialization.Serializable
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp
import kotlin.math.absoluteValue
import kotlin.math.sign
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*
import kotlin.math.PI

class Lift(override val telemetry: Telemetry): Subsystem, SlideSubsystem {

    companion object {
        val ticksPerRevolutionConversionFactor = 1/384.5
        val spoolDiameterInches = 2.06299
        val spoolCircumferenceInches = PI * spoolDiameterInches
        val ticksToInchesConversionFactor: Double = ticksPerRevolutionConversionFactor * spoolCircumferenceInches
        fun inchesToTicks(inches: Double): Int = (inches / ticksToInchesConversionFactor).toInt()
    }

    @JsonTypeName("LiftPositions")
    enum class LiftPositions(override val ticks: Int): SlideSubsystem.SlideTargetPosition {
        Down(0),
        AutoLowYellowPlacement(inchesToTicks(2.0)),
        AutoAbovePartnerPlacement(inchesToTicks(4.0)),
        ClearForArmToMove(inchesToTicks(9.5)),
        TargetClearForArmToMove(ClearForArmToMove.ticks + inchesToTicks(2.0)),
        Preset1(inchesToTicks(5.0)),
        Preset2(inchesToTicks(7.5)),

        SetLine1(inchesToTicks(10.0)),
        SetLine2(inchesToTicks(13.5)),
        SetLine3(inchesToTicks(22.0)),
        Max(inchesToTicks(28.5))
    }

    fun getGetLiftTargetFromDepoTarget(depoInput: RobotTwoTeleOp.DepoInput, position: Double): SlideSubsystem.SlideTargetPosition {
        return when (depoInput) {
            RobotTwoTeleOp.DepoInput.Preset1 -> LiftPositions.Preset1
            RobotTwoTeleOp.DepoInput.Preset2 -> LiftPositions.Preset2
            RobotTwoTeleOp.DepoInput.Preset3 -> LiftPositions.SetLine1
            RobotTwoTeleOp.DepoInput.Preset4 -> LiftPositions.SetLine2
            RobotTwoTeleOp.DepoInput.Preset5 -> LiftPositions.SetLine3
            RobotTwoTeleOp.DepoInput.Preset6 -> LiftPositions.Max
            RobotTwoTeleOp.DepoInput.Down -> LiftPositions.Down
            RobotTwoTeleOp.DepoInput.YellowPlacement -> LiftPositions.AutoLowYellowPlacement
            RobotTwoTeleOp.DepoInput.AbovePartnerYellowPlacement -> LiftPositions.AutoAbovePartnerPlacement
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
    override val findResetPower: Double = 0.8

//    @JsonTypeName("actual-lift")
//    data class ActualLift(override val currentPositionTicks: Int,
//                     override val limitSwitchIsActivated: Boolean,
//                     override val zeroPositionOffsetTicks: Int = 0,
//                     override val ticksMovedSinceReset: Int = 0,
//                     override val currentAmps: Double = 0.0): SlideSubsystem.ActualSlideSubsystem(currentPositionTicks, limitSwitchIsActivated, zeroPositionOffsetTicks, ticksMovedSinceReset, currentAmps) {
//                         constructor(actualSlideSubsystem: SlideSubsystem.ActualSlideSubsystem): this(actualSlideSubsystem.currentPositionTicks, actualSlideSubsystem.limitSwitchIsActivated, actualSlideSubsystem.zeroPositionOffsetTicks, actualSlideSubsystem.ticksMovedSinceReset, actualSlideSubsystem.currentAmps)
//                     }

    @Serializable
    data class TargetLift(override val targetPosition: SlideSubsystem.SlideTargetPosition = LiftPositions.Down,
                          override val power: Double = 0.0,
                          override val movementMode: MovementMode = MovementMode.Position
    ): TargetMovementSubsystem {
//                              constructor(targetSlideSubsystem: SlideSubsystem.TargetSlideSubsystem):
//                                    this(   targetPosition= targetSlideSubsystem.targetPosition,
//                                            power= targetSlideSubsystem.power,
//                                            movementMode= targetSlideSubsystem.movementMode, )
    }


    private val acceptablePositionErrorTicks = inchesToTicks(1.0)//70
    fun isLiftAtPosition(targetPositionTicks: Int, actualLiftPositionTicks: Int): Boolean {
        val currentPositionTicks = actualLiftPositionTicks
        val positionErrorTicks = targetPositionTicks - currentPositionTicks
        return positionErrorTicks.absoluteValue <= acceptablePositionErrorTicks
    }

    fun isLiftAbovePosition(targetPositionTicks: Int, actualLiftPositionTicks: Int): Boolean {
        return actualLiftPositionTicks > targetPositionTicks
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

    override val pid = PID("lift", kp = 0.0045)
    fun calculatePowerToMoveToPosition(targetPositionTicks: Int, currentPosition: Int): Double {
        val positionError = targetPositionTicks - currentPosition
        val gravityConstant = if (positionError.sign > 0) {
            0.1
        } else {
            0.0
        }
        telemetry.addLine("positionError: $positionError")

        val power = pid.calcPID(
                target = targetPositionTicks,
                error = positionError.toDouble()) //+ gravityConstant
        return power
    }
}