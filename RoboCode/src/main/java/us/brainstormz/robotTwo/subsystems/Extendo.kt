package us.brainstormz.robotTwo.subsystems

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.math.absoluteValue

class Extendo: Subsystem, SlideSubsystem {
    override var pid: PID =
//                        PID("extendo default", kp = 0.0025)
                        PID(
                                name= "extendo auto",
                                kp= 0.0014,
                                ki= 2.0E-7,
                                kd= 0.1,
                        )

    enum class ExtendoPositions(override val ticks: Int): SlideSubsystem.SlideTargetPosition {
        AllTheWayInTarget(-10),
        Min(0),
        Manual(0),
        PurpleSidePosition(1200),
        PurpleCenterPosition(2000),
        Max(2000),
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

    fun calcPowerToMoveExtendo(targetPositionTicks: Int, actualRobot: ActualRobot): Double {
        val currentPosition = actualRobot.collectorSystemState.extendo.currentPositionTicks
        val positionError = targetPositionTicks - currentPosition.toDouble()
        val power = pid.calcPID(
                target = targetPositionTicks,
                error = positionError)
        return power
    }
}