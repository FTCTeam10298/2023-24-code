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

    fun powerSubsystem(power: Double, servo: Servo) {
        servo.position = power
    }

    fun isClawAtPosition(currentTarget: ClawTarget, previousTarget: ClawTarget): Boolean {
        return currentTarget == previousTarget
    }
}