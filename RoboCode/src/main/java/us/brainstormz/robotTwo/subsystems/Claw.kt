package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.AxonEncoderReader
import us.brainstormz.utils.MathHelps
import kotlin.math.absoluteValue
import kotlin.math.sign


//fun main() {
//    val targetAngle = 0.0
//    val actualAngle = 180.0
//    val adjustedActualAngle = MathHelps.angleInSigned180(actualAngle)
//    val difference = targetAngle - adjustedActualAngle
//
//    println("targetAngle: $targetAngle")
//    println("actualAngle: $actualAngle")
//    println("difference: $difference")
//}

class Claw(private val telemetry: Telemetry) {
    enum class ClawTarget(val angleDegrees: Double) {
        Gripping(122.0),
        Retracted(0.0)
    }

    fun getClawAngleDegrees(encoder: AxonEncoderReader): Double {
        return encoder.getPositionDegrees()
    }

    private val holdingPower = 0.08
    private val kp: Double = 1/(400.0)
//    private val clawMaxAngle = ClawTarget.Gripping.angleDegrees + 1
    fun powerSubsystem(target: ClawTarget, actualAngleDegrees: Double, servo: CRServo) {
        val deltaAngleDegrees = target.angleDegrees - MathHelps.angleInSigned180(actualAngleDegrees)
        val toAnglePower = deltaAngleDegrees * kp

        val needToTravelFar = actualAngleDegrees < (ClawTarget.Gripping.angleDegrees/2)
        val clawIsTooFarOut = actualAngleDegrees > ClawTarget.Gripping.angleDegrees

        val power = if (target == ClawTarget.Gripping && !needToTravelFar && !clawIsTooFarOut) {
            toAnglePower.coerceAtLeast(holdingPower)
        } else {
            toAnglePower
        }

        servo.power = power
    }

    fun getAngularVelocityDegreesPerMili(actualDegrees: Double, actualTimeMilis: Long, previousActualDegrees: Double, previousActualTimeMilis: Long): Double {
        val deltaTimeMilis = actualTimeMilis - previousActualTimeMilis
        val deltaAngleDegrees = actualDegrees - previousActualDegrees
        val angularVelocityDegrees = deltaAngleDegrees/deltaTimeMilis
        return angularVelocityDegrees
    }

    fun isClawAtAngle(target: ClawTarget, actualDegrees: Double): Boolean {
        val actualInSigned180 = MathHelps.angleInSigned180(actualDegrees)
        val wristIsAtPosition = when(target){
            ClawTarget.Gripping -> actualInSigned180 >= target.angleDegrees - 12
            ClawTarget.Retracted -> actualInSigned180 <= target.angleDegrees + 20
        }

        return wristIsAtPosition
    }
}