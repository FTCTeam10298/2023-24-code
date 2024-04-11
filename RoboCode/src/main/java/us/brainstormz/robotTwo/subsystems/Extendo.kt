package us.brainstormz.robotTwo.subsystems

import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.ArraySerializer
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
    override var pid: PID =
                        PID(
                                name= "extendo",
                                kp= 0.00181,
                                ki= 2.1E-7,
                                kd= 0.08,
                        )



    @Serializable
    data class ExtendoTarget(
            override val targetPosition: SlideSubsystem.SlideTargetPosition,
            override val power: Double = 0.0,
            override val movementMode: DualMovementModeSubsystem.MovementMode = DualMovementModeSubsystem.MovementMode.Position
    ): DualMovementModeSubsystem.TargetMovementSubsystem

    @Serializable
    enum class ExtendoPositions( override val ticks: Int): SlideSubsystem.SlideTargetPosition {
//        AllTheWayInTarget(0),

        @SerialName("as")
        Min(0),
        @SerialName("aasdfs")
        Manual(0),
        @SerialName("aasdffs")
        PurpleFarSidePosition((700*SlideConversion.oldToNewMotorEncoderConversion).toInt()),
        @SerialName("afdss")
        PurpleCloseSidePosition((800*SlideConversion.oldToNewMotorEncoderConversion).toInt()),
        @SerialName("asdfs")
        PurpleCenterPosition((2000*SlideConversion.oldToNewMotorEncoderConversion).toInt()),
        @SerialName("aassdfs")
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
    override val findResetPower: Double = 0.2

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
            0.0
        } else {
            if (pidPower.absoluteValue <= 0.05) {
                0.0
            } else {
                pidPower
            }
        }

        return power
    }
}