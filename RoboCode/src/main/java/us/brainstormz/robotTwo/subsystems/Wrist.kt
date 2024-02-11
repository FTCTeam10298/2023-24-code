package us.brainstormz.robotTwo.subsystems

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.math.absoluteValue

class Wrist(val left: Claw, val right: Claw, private val telemetry: Telemetry) {
    val clawsAsMap = mapOf(Transfer.Side.Left to left, Transfer.Side.Right to right)

    data class WristTargets(val left: Claw.ClawTarget, val right: Claw.ClawTarget) {
        val bothOrNull: Claw.ClawTarget? = if (left == right) left else null
        constructor(both: Claw.ClawTarget) : this(both, both)

        val asMap = mapOf(Transfer.Side.Left to left, Transfer.Side.Right to right)

        fun getClawTargetBySide(side: Transfer.Side): Claw.ClawTarget {
            return when (side) {
                Transfer.Side.Left -> left
                Transfer.Side.Right -> right
            }
        }
    }

    data class ActualWrist(val leftClawAngleDegrees: Double, val rightClawAngleDegrees: Double) {
        fun getBySide(side: Transfer.Side): Double {
            return when (side) {
                Transfer.Side.Left -> leftClawAngleDegrees
                Transfer.Side.Right -> rightClawAngleDegrees
            }
        }
    }
    fun getWristActualState(hardware: RobotTwoHardware): ActualWrist {
        return ActualWrist(
                leftClawAngleDegrees = left.getClawAngleDegrees(hardware.leftClawEncoderReader),
                rightClawAngleDegrees = right.getClawAngleDegrees(hardware.rightClawEncoderReader))
    }

    fun powerSubsystem(target: WristTargets, actual: ActualWrist, hardware: RobotTwoHardware) {
        left.powerSubsystem(target.right, actual.leftClawAngleDegrees, hardware.leftClawServo)
        right.powerSubsystem(target.right, actual.rightClawAngleDegrees, hardware.rightClawServo)
    }

    fun wristIsAtPosition(target: WristTargets, actual: ActualWrist): Boolean {
        val wristIsAtPosition = target.asMap.toList().fold(true) {acc, (side, targetClaw) ->
            acc && (clawsAsMap[side]?.isClawAtAngle(targetClaw, actual.getBySide(side))==true)
        }
        telemetry.addLine("wristIsAtPosition: $wristIsAtPosition")
        return wristIsAtPosition
    }

    /** Shameful sensor-less fake code */
    private val targetToLeftClawMap = mapOf<Claw.ClawTarget, RobotTwoHardware.LeftClawPosition>(
            Claw.ClawTarget.Gripping to RobotTwoHardware.LeftClawPosition.Gripping,
            Claw.ClawTarget.Retracted to RobotTwoHardware.LeftClawPosition.Retracted
    )
    private val targetToRightClawMap = mapOf<Claw.ClawTarget, RobotTwoHardware.RightClawPosition>(
            Claw.ClawTarget.Gripping to RobotTwoHardware.RightClawPosition.Gripping,
            Claw.ClawTarget.Retracted to RobotTwoHardware.RightClawPosition.Retracted
    )
//    private fun convertActualToClosestWristTarget(actual: ActualWrist): WristTargets {
//        return WristTargets(targetToLeftClawMap.minBy { (actual.leftClawAngleDegrees - it.value.position).absoluteValue }.key,
//                            targetToRightClawMap.minBy{ (actual.rightClawAngleDegrees - it.value.position).absoluteValue }.key)
//    }
    fun getActualWristFromWristTargets(target: WristTargets): ActualWrist {
        return ActualWrist( targetToLeftClawMap[target.left]!!.position,
                            targetToRightClawMap[target.right]!!.position)
    }



}