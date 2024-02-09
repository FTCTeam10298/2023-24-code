package us.brainstormz.robotTwo.subsystems

import android.text.BoringLayout
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp

class Claw(val side: Transfer.Side): Subsystem {
    enum class ClawTarget {
        Gripping,
        Retracted
    }

    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        val servo = when (side) {
            Transfer.Side.Left -> hardware.leftClawServo
            Transfer.Side.Right -> hardware.rightClawServo
        }
        servo.position = power
    }

    fun isClawAtPosition(currentTarget: ClawTarget, previousTarget: ClawTarget): Boolean {
        return currentTarget == previousTarget
    }

}