package us.brainstormz.robotTwo.subsystems

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*
import kotlin.math.absoluteValue

class Extendo: Subsystem, SlideSubsystem {

    enum class ExtendoPositions(override val ticks: Int): SlideSubsystem.SlideTargetPosition {
        AllTheWayInTarget(-10),
        Min(0),
        Manual(0),
        CloserBackboardPixelPosition(500),
        MidBackboardPixelPosition(1000),
        FarBackboardPixelPosition(1750),
        AudiencePurpleCenterPosition(1900),
        AudiencePurpleLeftPosition(1700),
        Max(1990),
    }

    private val acceptablePositionErrorTicks = 50
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
    override fun getIsLimitSwitchActivated(hardware: RobotTwoHardware): Boolean = !hardware.extendoMagnetLimit.state
    override fun getCurrentAmps(hardware: RobotTwoHardware): Double = hardware.extendoMotorMaster.getCurrent(CurrentUnit.AMPS)

    override val allowedMovementBeforeResetTicks: Int = 200
    override val allTheWayInPositionTicks: Int = 0
    override val stallCurrentAmps: Double = 6.0
    override val definitelyMovingVelocityTicksPerMili: Double = 0.01
    override val findResetPower: Double = 0.2

    override val pid = PID(kp = 0.0025)
    fun calcPowerToMoveExtendo(targetPositionTicks: Int, actualRobot: ActualRobot): Double {
        val currentPosition = actualRobot.collectorSystemState.extendo.currentPositionTicks
        val positionError = targetPositionTicks - currentPosition.toDouble()
        val power = pid.calcPID(positionError)
        return power
    }
}