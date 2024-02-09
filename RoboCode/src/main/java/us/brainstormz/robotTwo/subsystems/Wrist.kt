package us.brainstormz.robotTwo.subsystems

import us.brainstormz.robotTwo.RobotTwoHardware

class Wrist(val left: Claw, val right: Claw) {
    val clawsAsMap = mapOf(Transfer.Side.Left to left, Transfer.Side.Right to right)

    data class WristTargets(val left: Claw.ClawTarget, val right: Claw.ClawTarget) {
        val bothOrNull: Claw.ClawTarget? = if (left == right) left else null
        constructor(both: Claw.ClawTarget) : this(both, both)

        fun getClawTargetBySide(side: Transfer.Side): Claw.ClawTarget {
            return when (side) {
                Transfer.Side.Left -> left
                Transfer.Side.Right -> right
            }
        }
    }

    data class ActualWrist(val leftClawAngleDegrees: Double, val rightClawAngleDegrees: Double)
    fun getWristActualState(hardware: RobotTwoHardware): ActualWrist {
        return ActualWrist(leftClawAngleDegrees = left.getClawPosition(hardware.leftClawServo), rightClawAngleDegrees = right.getClawPosition(hardware.rightClawServo))
    }

    fun powerSubsystem(target: WristTargets, hardware: RobotTwoHardware) {
        val targetToLeftClawMap = mapOf<Claw.ClawTarget, RobotTwoHardware.LeftClawPosition>(
                Claw.ClawTarget.Gripping to RobotTwoHardware.LeftClawPosition.Gripping,
                Claw.ClawTarget.Retracted to RobotTwoHardware.LeftClawPosition.Retracted
        )
        val targetToRightClawMap = mapOf<Claw.ClawTarget, RobotTwoHardware.RightClawPosition>(
                Claw.ClawTarget.Gripping to RobotTwoHardware.RightClawPosition.Gripping,
                Claw.ClawTarget.Retracted to RobotTwoHardware.RightClawPosition.Retracted
        )
        left.powerSubsystem(targetToLeftClawMap[target.left]!!.position, hardware.leftClawServo)
        right.powerSubsystem(targetToRightClawMap[target.right]!!.position, hardware.rightClawServo)
    }

    fun wristIsAtPosition(target: WristTargets, actual: WristTargets): Boolean {
        return target == actual
    }
}