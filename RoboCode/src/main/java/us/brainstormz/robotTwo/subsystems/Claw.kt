package us.brainstormz.robotTwo.subsystems

import android.text.BoringLayout
import com.qualcomm.robotcore.hardware.Servo
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp

class Claw {
    enum class ClawTarget {
        Gripping,
        Retracted
    }

    fun getClawPosition(servo: Servo): Double {
        return servo.position
    }

    fun powerSubsystem(power: Double, servo: Servo) {
        servo.position = power
    }

    fun isClawAtPosition(currentTarget: ClawTarget, previousTarget: ClawTarget): Boolean {
        return currentTarget == previousTarget
    }

}