package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.math.absoluteValue

class Extendo: Subsystem, SlideSubsystem {
    val maxSafeCurrentAmps = 5.5

    enum class ExtendoPositions(val ticks: Int) {
        AllTheWayInTarget(-10),
        Min(0),
        Manual(0),
        CloserBackboardPixelPosition(500),
        MidBackboardPixelPosition(1000),
        FarBackboardPixelPosition(1750),
        AudiencePurpleCenterPosition(1900),
        AudiencePurpleLeftPosition(1700),
        Max(2000),
    }


    private val acceptablePositionErrorTicks = 50
    fun isExtendoAtPosition(targetPositionTicks: Int, currentPositionTicks: Int): Boolean {
        val positionErrorTicks = targetPositionTicks - currentPositionTicks
        return positionErrorTicks.absoluteValue <= acceptablePositionErrorTicks
    }

//    fun isExtendoAllTheWayIn(actualRobot: ActualRobot): Boolean {
//        val limitIsActive = actualRobot.collectorSystemState.extendo.limitSwitchIsActivated
//        val extendoPositionIsAccurate = actualRobot.collectorSystemState.extendo.ticksMovedSinceReset <= 200
//        val extendoIsInAccordingToTicks = actualRobot.collectorSystemState.extendo.currentPositionTicks <= ExtendoPositions.Min.ticks
//        return limitIsActive || (extendoPositionIsAccurate && extendoIsInAccordingToTicks)
//    }

//    fun getVelocityTicksPerMili(actualTicks: Int, actualTimeMilis: Long, previousActualTicks: Int, previousActualTimeMilis: Long): Double {
    fun getVelocityTicksPerMili(actualWorld: ActualWorld, previousActualWorld: ActualWorld): Double {
        val actualTicks: Int = actualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks
        val actualTimeMilis: Long = actualWorld.timestampMilis
        val previousActualTicks: Int = previousActualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks
        val previousActualTimeMilis: Long = previousActualWorld.timestampMilis
        val deltaTimeMilis: Long = actualTimeMilis - previousActualTimeMilis
        val deltaTicks: Int = actualTicks - previousActualTicks
        val velocityTicksPerMili: Double = (deltaTicks.toDouble())/(deltaTimeMilis)
        return velocityTicksPerMili
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
    override val allowedMovementBeforeResetTicks: Int = 200
    override val allTheWayInTicks: Int = 0

    override val pid = PID(kp = 0.0025)
    fun calcPowerToMoveExtendo(targetPositionTicks: Int, actualRobot: ActualRobot): Double {
        val currentPosition = actualRobot.collectorSystemState.extendo.currentPositionTicks
        val positionError = targetPositionTicks - currentPosition.toDouble()
        val power = pid.calcPID(positionError)
        return power
    }
//    fun moveExtendoToPosition(targetPositionTicks: Int) {
//        powerExtendo(calcPowerToMoveExtendo(targetPositionTicks))
//    }
}