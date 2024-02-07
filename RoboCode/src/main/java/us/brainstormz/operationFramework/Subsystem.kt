package us.brainstormz.operationFramework

import us.brainstormz.robotTwo.RobotTwoHardware

interface Subsystem {
    fun powerSubsystem(power: Double, hardware: RobotTwoHardware)
}