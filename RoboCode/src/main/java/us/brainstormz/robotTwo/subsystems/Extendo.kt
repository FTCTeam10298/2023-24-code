package us.brainstormz.robotTwo.subsystems

import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.printPID
import kotlin.math.absoluteValue

class Extendo(override val telemetry: Telemetry): Subsystem, SlideSubsystem {
    override var pid: PID =
                        PID(
                                name= "extendo",
                                kp= 0.00181,
                                ki= 2.1E-7,
                                kd= 0.08,
                        )

    enum class ExtendoPositions(override val ticks: Int): SlideSubsystem.SlideTargetPosition {
//        AllTheWayInTarget(0),
        Min(0),
        Manual(0),
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
    override val stallCurrentAmps: Double = 6.0
    override val findResetPower: Double = 0.2

    fun calcPowerToMoveExtendo(targetPositionTicks: Int, actualRobot: ActualRobot): Double {
        val currentPosition = actualRobot.collectorSystemState.extendo.currentPositionTicks
        val positionError = targetPositionTicks - currentPosition.toDouble()
        val pidPower = pid.calcPID(
                target = targetPositionTicks,
                error = positionError)

        telemetry.addLine(printPID(pid))

        val power = if (actualRobot.collectorSystemState.extendo.currentAmps >= stallCurrentAmps) {
            0.0
        } else {
            pidPower
        }

        return power
    }
}