package us.brainstormz.robotTwo.subsystems

data class WristPositions(val left: Claw.ClawTarget, val right: Claw.ClawTarget) {
    constructor(both: Claw.ClawTarget) : this(both, both)
}
class Wrist(val left: Claw, val right: Claw) {
    val listOfClaws = listOf(left, right)

    fun wristIsAtPosition(target: WristPositions, actual: WristPositions): Boolean {
        return target == actual
    }
}