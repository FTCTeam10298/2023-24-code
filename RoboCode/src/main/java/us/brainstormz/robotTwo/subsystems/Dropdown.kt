package us.brainstormz.robotTwo.subsystems

import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.RobotTwoHardware

class Dropdown: Subsystem {
    enum class DropdownPresets(val position: Double) {
        Up(0.0),
        FivePixels(0.0),
        FourPixels(0.0),
        ThreePixels(0.0),
        TwoPixels(0.0),
        OnePixel(0.0)
    }

    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        hardware.dropDownServo.position = power
    }
}