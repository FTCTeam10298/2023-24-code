package us.brainstormz.robotTwo.subsystems

interface DualMovementModeSubsystem {
    enum class MovementMode {
        Position,
        Power
    }

    interface TargetPosition { val ticks: Int }

    interface TargetMovementSubsystem {
        val targetPosition: TargetPosition
        val movementMode: MovementMode
        val power: Double
    }

}