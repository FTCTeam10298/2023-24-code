package us.brainstormz.robotTwo.subsystems

import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.utils.DataClassHelper
import kotlin.math.absoluteValue
import kotlin.math.sign

interface SlideSubsystem {
    open class ActualSlideSubsystem(
            open val currentPositionTicks: Int,
            open val limitSwitchIsActivated: Boolean,
            open val zeroPositionOffsetTicks: Int,
            open val ticksMovedSinceReset: Int,
            open val currentAmps: Double
    ) {
        override fun toString() = DataClassHelper.dataClassToString(this)
    }

    val pid: PID

    fun getRawPositionTicks(hardware: RobotTwoHardware): Int
    fun getIsLimitSwitchActivated(hardware: RobotTwoHardware): Boolean
    fun getCurrentAmps(hardware: RobotTwoHardware): Double

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
                ticksMovedSinceReset = ticksMovedSinceReset,
                currentAmps = getCurrentAmps(hardware)
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
    class AllTheWayIn(override val ticks: Int): TargetPosition
    open class TargetSlideSubsystem (
            open val targetPosition: TargetPosition,
            open val movementMode: MovementMode,
            open val power: Double = 0.0,
            open val timeOfResetMoveDirectionStartMilis: Long = 0)

    val stallCurrentAmps: Double
    val definitelyMovingVelocityTicksPerMili: Double
    val findResetPower: Double
    fun findLimitToReset(actualSlideSubsystem: ActualSlideSubsystem, actualTimestampMilis: Long, previousSlideSubsystem: ActualSlideSubsystem, previousTimestampMilis: Long, previousTargetSlideSubsystem: TargetSlideSubsystem): TargetSlideSubsystem {
        val resetIsNeeded = actualSlideSubsystem.ticksMovedSinceReset > allowedMovementBeforeResetTicks
        return if (resetIsNeeded) {
            val velocityTicksPerMili = getVelocityTicksPerMili(actualSlideSubsystem, actualTimestampMilis, previousSlideSubsystem, actualTimestampMilis)

            val slideIsStalling = actualSlideSubsystem.currentAmps > stallCurrentAmps
            val slideIsDefinitelyMoving = velocityTicksPerMili > definitelyMovingVelocityTicksPerMili
            val timeToSwitchMovementDirection = actualTimestampMilis - previousTargetSlideSubsystem.timeOfResetMoveDirectionStartMilis > 1500

            val power: Double = if (slideIsDefinitelyMoving && timeToSwitchMovementDirection) {
                findResetPower * -velocityTicksPerMili.sign
            } else {
                findResetPower * when {
                    slideIsStalling -> {
//                        switchPowerDirection
                        -previousTargetSlideSubsystem.power.sign
                    }
                    !slideIsDefinitelyMoving -> {
//                        startPowerInACertainDirection
                        findResetPower * actualSlideSubsystem.zeroPositionOffsetTicks.sign
                    }
                    else -> {
//                        samePowerDirection
                        previousTargetSlideSubsystem.power.sign
                    }
                }
            }
            val timeOfResetMoveDirectionStartMilis = if (power.sign != previousTargetSlideSubsystem.power.sign) {
                actualTimestampMilis
            } else {
                previousTargetSlideSubsystem.timeOfResetMoveDirectionStartMilis
            }

            return TargetSlideSubsystem(power = power,
                                        movementMode = MovementMode.Power,
                                        timeOfResetMoveDirectionStartMilis = timeOfResetMoveDirectionStartMilis,
                                        targetPosition = previousTargetSlideSubsystem.targetPosition)
        } else {
            previousTargetSlideSubsystem
        }
    }
}