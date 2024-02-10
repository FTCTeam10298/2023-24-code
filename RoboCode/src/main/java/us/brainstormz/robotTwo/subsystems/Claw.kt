package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.hardware.Servo

class Claw {
    enum class ClawTarget {
        Gripping,
        Retracted
    }

    fun getClawServoPosition(servo: Servo): Double {
        return servo.position
    }

    fun powerSubsystem(target: ClawTarget, servo: Servo) {
        val power = when (target) {
            ClawTarget.Gripping -> 0.3
            ClawTarget.Retracted -> {
                val kp = 0.5/90
                val deltaAngleDegrees = 1
                deltaAngleDegrees * kp
            }
        }
        servo.position = power
    }

    fun isClawAtPosition(currentTarget: ClawTarget, previousTarget: ClawTarget): Boolean {
        return currentTarget == previousTarget
    }
}