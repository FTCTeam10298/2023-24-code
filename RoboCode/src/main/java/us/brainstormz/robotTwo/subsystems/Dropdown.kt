package us.brainstormz.robotTwo.subsystems

import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.RobotTwoHardware

class Dropdown: Subsystem, DualMovementModeSubsystem {
    enum class DropdownPresets(val position: Double) {
        Up(0.5),
        FivePixels(0.35),
        FourPixels(0.2),
        ThreePixels(0.175),
        TwoPixels(0.1),
        OnePixel(0.0)
    }

    data class DropdownTarget(
            override val targetPosition: DropdownPresets,
            override val movementMode: DualMovementModeSubsystem.MovementMode,
            override val power: Double,
    ): DualMovementModeSubsystem.TargetMovementSubsystem {
        constructor(targetPosition: DropdownPresets):
                this(
                        targetPosition= targetPosition,
                        movementMode= DualMovementModeSubsystem.MovementMode.Position,
                        power= 0.0
                )
    }

    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        hardware.dropDownServo.position = power
    }
}