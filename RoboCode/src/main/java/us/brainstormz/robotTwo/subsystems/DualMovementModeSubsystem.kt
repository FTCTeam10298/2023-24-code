package us.brainstormz.robotTwo.subsystems

interface DualMovementModeSubsystem {
    enum class MovementMode {
        Position,
        Power
    }

    interface TargetPosition {
        //val ticks: Any
    }

    interface TargetMovementSubsystem {
        val targetPosition: TargetPosition
        val movementMode: MovementMode
        val power: Double
    }
}