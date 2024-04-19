package us.brainstormz.robotTwo.subsystems

import com.fasterxml.jackson.annotation.JsonTypeName
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.tuningAndCalibration.printPID
import kotlin.math.absoluteValue

class Extendo(override val telemetry: Telemetry): Subsystem, SlideSubsystem {
    override var pid: PID = PID(
            name= "extendo",
            kp= 0.00186,
            ki= 2.1E-7,
            kd= 0.08,
    )



    data class ExtendoTarget(
            override val targetPosition: SlideSubsystem.SlideTargetPosition,
            override val power: Double = 0.0,
            override val movementMode: DualMovementModeSubsystem.MovementMode,
    ): DualMovementModeSubsystem.TargetMovementSubsystem {
        constructor(targetPosition: SlideSubsystem.SlideTargetPosition): this (
                targetPosition = targetPosition,
                movementMode = DualMovementModeSubsystem.MovementMode.Position,
                power = 0.0
        )
    }

    @JsonTypeName("ExtendoPositions")
    enum class ExtendoPositions( override val ticks: Int): SlideSubsystem.SlideTargetPosition {
//        AllTheWayInTarget(0),

        Min(0),
        InPastBatteryBox(100),
        OutFarEnoughToCompletelyClearDepo(200),
//        Manual(0),
        ReadyToEject(200),
        AutoPark(500),
        CollectFromStack1(800),
        CollectFromStack2(1000),
        PurpleFarSidePosition((700*SlideConversion.oldToNewMotorEncoderConversion).toInt()),
        PurpleCloseSidePosition((800*SlideConversion.oldToNewMotorEncoderConversion).toInt()),
        PurpleCenterPosition((2000*SlideConversion.oldToNewMotorEncoderConversion).toInt()),
        Max((2000*SlideConversion.oldToNewMotorEncoderConversion).toInt()),
    }

    val acceptablePositionErrorTicks = 100
    fun isExtendoAtPosition(targetPositionTicks: Int, currentPositionTicks: Int): Boolean {
        val positionErrorTicks = targetPositionTicks - currentPositionTicks
        return positionErrorTicks.absoluteValue <= acceptablePositionErrorTicks
    }

    fun getVelocityTicksPerMili(actualWorld: ActualWorld, previousActualWorld: ActualWorld): Double {
        val actualExtendo = actualWorld.actualRobot.collectorSystemState.extendo
        val actualTimeMilis: Long = actualWorld.timestampMilis
        val previousActualExtendo = previousActualWorld.actualRobot.collectorSystemState.extendo
        val previousActualTimeMilis: Long = previousActualWorld.timestampMilis
        return super.getVelocityTicksPerMili(actualExtendo, actualTimeMilis, previousActualExtendo, previousActualTimeMilis)
    }

    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        val allowedPower = if (hardware.extendoMotorMaster.isOverCurrent) {
            0.0
        } else {
            power
        }

        hardware.extendoMotorMaster.power = allowedPower
        hardware.extendoMotorSlave.power = allowedPower
    }

    override fun getRawPositionTicks(hardware: RobotTwoHardware): Int = hardware.extendoMotorMaster.currentPosition
    override fun getIsLimitSwitchActivated(hardware: RobotTwoHardware): Boolean = hardware.extendoMagnetLimit.state
    override fun getCurrentAmps(hardware: RobotTwoHardware): Double = hardware.extendoMotorMaster.getCurrent(CurrentUnit.AMPS)

    override val allowedMovementBeforeResetTicks: Int = 140
    override val allTheWayInPositionTicks: Int = 0
    override val stallCurrentAmps: Double = 4.0
    override val findResetPower: Double = 0.8

    fun calcPowerToMoveExtendo(targetPositionTicks: Int, actualRobot: ActualRobot, previousActualRobot: ActualRobot): Double {
        val actualExtendo = actualRobot.collectorSystemState.extendo

        val justResetZero = actualExtendo.zeroPositionOffsetTicks != previousActualRobot.collectorSystemState.extendo.zeroPositionOffsetTicks
        if (justResetZero) {
            pid.reset()
        }

        val currentPosition = actualExtendo.currentPositionTicks
        val positionError = (targetPositionTicks - currentPosition.toDouble())*SlideConversion.oldToNewMotorEncoderConversion
        val pidPower = pid.calcPID(
                target = targetPositionTicks,
                error = positionError)

        telemetry.addLine("position error: $positionError")
        telemetry.addLine("pidPower: $pidPower")
        telemetry.addLine(printPID(pid))

        val isStalling = actualExtendo.currentAmps >= stallCurrentAmps
        val power = if (isStalling) {
            pidPower.coerceAtLeast(-0.5)
        } else {
            pidPower
//            if (pidPower.absoluteValue <= 0.05) {
//                0.0
//            } else {
//                pidPower
//            }
        }

        return power
    }
}