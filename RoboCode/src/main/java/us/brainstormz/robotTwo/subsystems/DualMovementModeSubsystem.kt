package us.brainstormz.robotTwo.subsystems

interface DualMovementModeSubsystem {
    enum class MovementMode {
        Position,
        Power
    }

//    interface TargetPosition {
//        //val ticks: Any
//    }

    interface TargetMovementSubsystem {
        val targetPosition: Any
        val movementMode: MovementMode
        val power: Any
    }
}