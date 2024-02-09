package us.brainstormz.robotTwo.subsystems

import us.brainstormz.robotTwo.RobotTwoHardware

class Wrist(val left: Claw, val right: Claw) {
    val listOfClaws = listOf(left, right)

    data class WristPositions(val left: Claw.ClawTarget, val right: Claw.ClawTarget) {
        val both: Claw.ClawTarget? = if (left == right) left else null
        constructor(both: Claw.ClawTarget) : this(both, both)

        fun getClawTargetBySide(side: Transfer.Side): Claw.ClawTarget {
            return when (side) {
                Transfer.Side.Left -> left
                Transfer.Side.Right -> right
            }
        }
//        fun getClawTargetFromClaw(claw: Claw): Claw.ClawTarget {
//            return when (claw.side) {
//                Transfer.Side.Left -> left
//                Transfer.Side.Right -> right
//            }
//        }
    }
    fun wristIsAtPosition(target: WristPositions, actual: WristPositions): Boolean {
        return target == actual
    }
}