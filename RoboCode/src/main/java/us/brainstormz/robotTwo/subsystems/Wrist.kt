package us.brainstormz.robotTwo.subsystems

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.math.absoluteValue

class Wrist(val left: Claw, val right: Claw, private val telemetry: Telemetry) {
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

    data class ActualWrist(val leftClawServoPosition: Double, val rightClawServoPosition: Double)
    fun getWristActualState(hardware: RobotTwoHardware): ActualWrist {
        return ActualWrist(leftClawServoPosition = left.getClawServoPosition(hardware.leftClawServo), rightClawServoPosition = right.getClawServoPosition(hardware.rightClawServo))
    }

    fun powerSubsystem(target: WristTargets, hardware: RobotTwoHardware) {
        left.powerSubsystem(target.left/*targetToLeftClawMap[target.left]!!.position*/, hardware.leftClawServo)
        right.powerSubsystem(target.right/*targetToRightClawMap[target.right]!!.position*/, hardware.rightClawServo)
    }

    fun wristIsAtPosition(target: WristTargets, actual: ActualWrist): Boolean {
        val actualConvertedToClosestWristTarget = convertActualToClosestWristTarget(actual)
        telemetry.addLine("actual: $actual")
        telemetry.addLine("actualConvertedToClosestWristTarget: $actualConvertedToClosestWristTarget")
        telemetry.addLine("target: $target")
        val wristIsAtPosition = target == actualConvertedToClosestWristTarget
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
    private fun convertActualToClosestWristTarget(actual: ActualWrist): WristTargets {
        return WristTargets(targetToLeftClawMap.minBy { (actual.leftClawServoPosition - it.value.position).absoluteValue}.key,
                            targetToRightClawMap.minBy {(actual.rightClawServoPosition - it.value.position).absoluteValue}.key)
    }
    fun getActualWristFromWristTargets(target: WristTargets): ActualWrist {
        return ActualWrist( targetToLeftClawMap[target.left]!!.position,
                            targetToRightClawMap[target.left]!!.position)
    }



}