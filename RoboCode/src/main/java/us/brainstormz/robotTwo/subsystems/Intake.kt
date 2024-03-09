package us.brainstormz.robotTwo.subsystems

import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.math.abs

class Intake: Subsystem {
    data class TargetIntake(
        override val targetPosition: Double,
        override val movementMode: DualMovementModeSubsystem.MovementMode,
        override val power: CollectorPowers
    ): DualMovementModeSubsystem.TargetMovementSubsystem {
        constructor(targetPosition: Double): this(targetPosition = targetPosition, movementMode = DualMovementModeSubsystem.MovementMode.Position, power = CollectorPowers.Off)
        constructor(power: CollectorPowers): this(targetPosition = 0.0, movementMode = DualMovementModeSubsystem.MovementMode.Power, power = power)
    }

    enum class CollectorPowers(val power: Double) {
        Off(0.0),
        Intake(1.0),
        Eject(-1.0),
        EjectDraggedPixelPower(-0.1)
    }


    val proportionalConstant = 0.2
    fun calcPowerToMoveToAngle(targetAngleDegrees: Double, actualAngleDegrees: Double): Double {
        val errorDegrees = (actualAngleDegrees - targetAngleDegrees).mod(360.0)

        return abs((errorDegrees / 360.0) * proportionalConstant)
    }

    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        hardware.collectorServo1.power = power
        hardware.collectorServo2.power = power
    }
}