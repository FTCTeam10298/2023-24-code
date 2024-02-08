package us.brainstormz.robotTwo.subsystems

import android.text.BoringLayout
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp

class Claw(private val side: Transfer.Side): Subsystem {
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

    fun isClawAtPosition(currentTarget: ClawTarget, previousTarget: DepoTarget): Boolean {
        val previousClawTarget = when (side) {
            Transfer.Side.Left -> previousTarget.leftClawPosition
            Transfer.Side.Right -> previousTarget.rightClawPosition
        }
        return currentTarget == previousClawTarget
    }

}