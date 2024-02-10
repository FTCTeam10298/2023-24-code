package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.Telemetry

class Claw(private val telemetry: Telemetry) {
    enum class ClawTarget {
        Gripping,
        Retracted
    }

    fun getClawServoPosition(servo: Servo): Double {
        return servo.position
    }

    fun powerSubsystem(power: Double, servo: Servo) {
        telemetry.addLine("claw power: $power, servo: $servo")
        servo.position = power
    }

    fun isClawAtPosition(currentTarget: ClawTarget, previousTarget: ClawTarget): Boolean {
        return currentTarget == previousTarget
    }
}