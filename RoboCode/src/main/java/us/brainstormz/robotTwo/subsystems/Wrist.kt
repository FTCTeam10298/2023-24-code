package us.brainstormz.robotTwo.subsystems

import kotlinx.serialization.Serializable
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.Side

class Wrist(override val left: Claw, override val right: Claw, private val telemetry: Telemetry): Side.ThingWithSides<Claw> {
    val clawsAsMap = mapOf(Side.Left to left, Side.Right to right)

    @Serializable
    data class WristTargets(override val left: Claw.ClawTarget, override val right: Claw.ClawTarget): Side.ThingWithSides<Claw.ClawTarget> {
        val bothOrNull: Claw.ClawTarget? = if (left == right) left else null
        constructor(both: Claw.ClawTarget) : this(both, both)

        val asMap = mapOf(Side.Left to left, Side.Right to right)
    }

    @Serializable
    data class ActualWrist(val leftClawAngleDegrees: Double, val rightClawAngleDegrees: Double): Side.ThingWithSides<Double> {
        override val left = leftClawAngleDegrees
        override val right = rightClawAngleDegrees
    }
    fun getWristActualState(hardware: RobotTwoHardware): ActualWrist {
        return ActualWrist(
                leftClawAngleDegrees = left.getClawAngleDegrees(hardware.leftClawEncoderReader),
                rightClawAngleDegrees = right.getClawAngleDegrees(hardware.rightClawEncoderReader))
    }

    fun powerSubsystem(target: WristTargets, actual: ActualWrist, hardware: RobotTwoHardware) {
        left.powerSubsystem(target.left, actual.leftClawAngleDegrees, hardware.leftClawServo)
        right.powerSubsystem(target.right, actual.rightClawAngleDegrees, hardware.rightClawServo)
    }

    fun wristIsAtPosition(target: WristTargets, actual: ActualWrist): Boolean {
        val wristIsAtPosition = target.asMap.toList().fold(true) {acc, (side, targetClaw) ->
            val actualClawAngleDegrees = actual.getBySide(side)
            val thisClaw = clawsAsMap[side]
            val clawIsAtAngle = thisClaw?.isClawAtAngle(targetClaw, actualClawAngleDegrees)

            acc && clawIsAtAngle==true
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

    fun getClawTargetFromActualClaw(actualAngleDegrees: Double): Claw.ClawTarget {
        return if (left.isClawAtAngle(Claw.ClawTarget.Retracted, actualAngleDegrees)){
            Claw.ClawTarget.Retracted
        } else {
            Claw.ClawTarget.Gripping
        }
    }
    fun getWristTargetsFromActualWrist(actual: ActualWrist): WristTargets {
        return WristTargets(
                left = getClawTargetFromActualClaw(actual.left),
                right = getClawTargetFromActualClaw(actual.right)
        )
    }


}