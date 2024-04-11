package us.brainstormz.robotTwo.subsystems

import kotlinx.serialization.Serializable

interface DualMovementModeSubsystem {
    @Serializable
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