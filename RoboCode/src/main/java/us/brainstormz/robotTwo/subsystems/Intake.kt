package us.brainstormz.robotTwo.subsystems

import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.RobotTwoHardware

class Intake: Subsystem {
    enum class CollectorPowers(val power: Double) {
        Off(0.0),
        Intake(1.0),
        Eject(-1.0),
        EjectDraggedPixelPower(-0.1)
    }

    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        hardware.collectorServo1.power = power
        hardware.collectorServo2.power = power
    }
}