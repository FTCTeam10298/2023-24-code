package us.brainstormz.robotTwo.subsystems

import kotlinx.serialization.Serializable
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.math.absoluteValue
import kotlin.math.sign
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*
import us.brainstormz.utils.measured


object SlideConversion {
    //384.5 / 537.7
    //0.715082759903292
    val oldToNewMotorEncoderConversion: Double = (384.5) / (537.7)
}

interface SlideSubsystem: DualMovementModeSubsystem {

    @Serializable
    open class ActualSlideSubsystem(
            open val currentPositionTicks: Int,
            open val limitSwitchIsActivated: Boolean,
            open val zeroPositionOffsetTicks: Int,
            open val ticksMovedSinceReset: Int,
            open val currentAmps: Double
    )

    val telemetry: Telemetry

    val pid: PID

    fun getRawPositionTicks(hardware: RobotTwoHardware): Int
    fun getIsLimitSwitchActivated(hardware: RobotTwoHardware): Boolean
    fun getCurrentAmps(hardware: RobotTwoHardware): Double

    private fun getZeroPositionOffsetTicks(currentPositionTicks: Int, limitSwitchIsActivated: Boolean, previousZeroPositionOffsetTicks: Int?, previousLimitSwitchIsActivated: Boolean?): Int {
        return if (limitSwitchIsActivated) {
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

    fun getActualSlideSubsystem(hardware: RobotTwoHardware, previousActualSlideSubsystem: ActualSlideSubsystem?): ActualSlideSubsystem = measured("SlideSubsystem") {
        val rawPositionTicks = getRawPositionTicks(hardware)
        val limitSwitchIsActivated = getIsLimitSwitchActivated(hardware)

        val zeroPositionOffsetTicks = getZeroPositionOffsetTicks(rawPositionTicks, limitSwitchIsActivated, previousActualSlideSubsystem?.zeroPositionOffsetTicks, previousActualSlideSubsystem?.limitSwitchIsActivated)

        val currentPositionTicks = rawPositionTicks - zeroPositionOffsetTicks

        val ticksMovedSinceReset = getTicksMovedSinceReset(currentPositionTicks, limitSwitchIsActivated, previousActualSlideSubsystem)

        ActualSlideSubsystem(
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


    interface SlideTargetPosition { val ticks: Int }

    class VariableTargetPosition(override val ticks: Int): SlideTargetPosition {
        override fun equals(other: Any?): Boolean {
            return if (other is SlideTargetPosition) {
                this.ticks == other.ticks
            } else {
                false
            }
        }
        override fun toString(): String = """
            VariableTargetPosition(ticks=$ticks)
        """.trimIndent()
    }

    @Serializable
    open class TargetSlideSubsystem (
            override val targetPosition: SlideTargetPosition,
            override val movementMode: MovementMode,
            override val power: Double = 0.0,
            open val timeOfResetMoveDirectionStartMilis: Long = 0
    ): TargetMovementSubsystem {
        constructor(targetPosition: SlideTargetPosition): this(
                targetPosition= targetPosition,
                movementMode = MovementMode.Position,
                power = 0.0
        )

        override fun toString(): String = """
            TargetSlideSubsystem(targetPosition=$targetPosition, movementMode=$movementMode, power=$power)
        """.trimIndent()
    }

    val stallCurrentAmps: Double
    val findResetPower: Double
    fun findLimitToReset(actualSlideSubsystem: ActualSlideSubsystem, otherTarget: TargetSlideSubsystem): TargetSlideSubsystem {
        val slideThinksItsAtZero = actualSlideSubsystem.currentPositionTicks <= 0

        return if (slideThinksItsAtZero && !actualSlideSubsystem.limitSwitchIsActivated) {
            TargetSlideSubsystem(power = -findResetPower,
                                movementMode = MovementMode.Power,
                                timeOfResetMoveDirectionStartMilis = 0,
                                targetPosition = otherTarget.targetPosition)
        } else {
            otherTarget
        }
    }
}