package us.brainstormz.localizer

interface Localizer {
    fun currentPositionAndRotation(): PositionAndRotation
    fun recalculatePositionAndRotation()
    fun setPositionAndRotation(newPosition: PositionAndRotation)
//    fun startNewMovement()
}