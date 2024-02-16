package us.brainstormz.robotTwo.subsystems

import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.utils.DataClassHelper
import kotlin.math.absoluteValue

interface SlideSubsystem {
    open class ActualSlideSubsystem(
            open val currentPositionTicks: Int,
            open val limitSwitchIsActivated: Boolean,
            open val zeroPositionOffsetTicks: Int,
            open val ticksMovedSinceReset: Int,
    ) {
        override fun toString() = DataClassHelper.dataClassToString(this)
    }

    val pid: PID

    fun getRawPositionTicks(hardware: RobotTwoHardware): Int
    fun getIsLimitSwitchActivated(hardware: RobotTwoHardware): Boolean
    private fun getZeroPositionOffsetTicks(currentPositionTicks: Int, limitSwitchIsActivated: Boolean, previousZeroPositionOffsetTicks: Int?, previousLimitSwitchIsActivated: Boolean?): Int {
        return if (limitSwitchIsActivated) {
            if (previousLimitSwitchIsActivated == false) {
                pid.reset()
            }
            currentPositionTicks
        } else {
            previousZeroPositionOffsetTicks ?: 0
        }
    }

    private fun getTicksMovedSinceReset(currentPositionTicks: Int, limitSwitchIsActivated: Boolean, previousActualSlideSubsystem: ActualSlideSubsystem?): Int {
        val ticksMovedSinceReset = if (limitSwitchIsActivated) {
            0
        } else {
            val previousPositionTicks = previousActualSlideSubsystem?.currentPositionTicks ?: currentPositionTicks
            val deltaPositionTicks = currentPositionTicks - previousPositionTicks

            val previousTicksMovedSinceReset = previousActualSlideSubsystem?.ticksMovedSinceReset ?: 0

            deltaPositionTicks.absoluteValue + previousTicksMovedSinceReset
        }
        return ticksMovedSinceReset
    }

    fun getActualSlideSubsystem(hardware: RobotTwoHardware, previousActualSlideSubsystem: ActualSlideSubsystem?): ActualSlideSubsystem {
        val rawPositionTicks = getRawPositionTicks(hardware)
        val limitSwitchIsActivated = getIsLimitSwitchActivated(hardware)

        val zeroPositionOffsetTicks = getZeroPositionOffsetTicks(rawPositionTicks, limitSwitchIsActivated, previousActualSlideSubsystem?.zeroPositionOffsetTicks, previousActualSlideSubsystem?.limitSwitchIsActivated)

        val currentPositionTicks = rawPositionTicks - zeroPositionOffsetTicks

        val ticksMovedSinceReset = getTicksMovedSinceReset(currentPositionTicks, limitSwitchIsActivated, previousActualSlideSubsystem)

        return ActualSlideSubsystem(
                currentPositionTicks = currentPositionTicks,
                zeroPositionOffsetTicks= zeroPositionOffsetTicks,
                limitSwitchIsActivated= limitSwitchIsActivated,
                ticksMovedSinceReset = ticksMovedSinceReset
        )
    }

    val allowedMovementBeforeResetTicks: Int
    val allTheWayInPositionTicks: Int
    fun isSlideSystemAllTheWayIn(actualSlideSubsystem: ActualSlideSubsystem): Boolean {
        val limitIsActive = actualSlideSubsystem.limitSwitchIsActivated
        val positionIsAccurate = actualSlideSubsystem.ticksMovedSinceReset <= allowedMovementBeforeResetTicks
        val isInAccordingToTicks = actualSlideSubsystem.currentPositionTicks <= allTheWayInPositionTicks
        return limitIsActive || (positionIsAccurate && isInAccordingToTicks)
    }

    fun getVelocityTicksPerMili(actualSlideSubsystem: ActualSlideSubsystem, actualTimestampMilis: Long, previousSlideSubsystem: ActualSlideSubsystem, previousTimestampMilis: Long): Double {
        val actualTicks: Int = actualSlideSubsystem.currentPositionTicks
        val previousActualTicks: Int = previousSlideSubsystem.currentPositionTicks
        val deltaTimeMilis: Long = actualTimestampMilis - previousTimestampMilis
        val deltaTicks: Int = actualTicks - previousActualTicks
        val velocityTicksPerMili: Double = (deltaTicks.toDouble())/(deltaTimeMilis)
        return velocityTicksPerMili
    }

    enum class MovementMode {
        Position,
        Power
    }
    interface TargetPosition { val ticks: Int }
    data class TargetSlideSubsystem(val targetPosition: TargetPosition, val power: Double, val movementMode: MovementMode)
    fun findLimitToReset(actualSlideSubsystem: ActualSlideSubsystem, previousActualSlideSubsystem: ActualSlideSubsystem, previousTargetSlideSubsystem: TargetSlideSubsystem): TargetSlideSubsystem {
        val resetIsNeeded = actualSlideSubsystem.ticksMovedSinceReset > allowedMovementBeforeResetTicks
        return if (resetIsNeeded) {
//            actualSlideSubsystem.zeroPositionOffsetTicks
//
//
//            val slideIsMovingIn = previousTargetSlideSubsystem.targetPosition ==
//
//            val extendoIsAlreadyGoingIn = previousTargetSlideSubsystem.targetPosition == Extendo.ExtendoPositions.Min
//            val extendoIsManual = previousTargetSlideSubsystem.targetPosition == Extendo.ExtendoPositions.Manual
//            if ((extendoIsAlreadyGoingIn || extendoIsManual) && limitIsActivated) {
//                previousTargetSlideSubsystem.targetPosition
//            } else  {
//                if (!limitIsActivated) {
//                    Extendo.ExtendoPositions.AllTheWayInTarget
//                } else {
//                    Extendo.ExtendoPositions.Min
//                }
//            }

            previousTargetSlideSubsystem
        } else {
            previousTargetSlideSubsystem
        }
    }
}