package us.brainstormz.robotTwo.subsystems

import kotlinx.serialization.Serializable
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.RobotTwoHardware

class Dropdown: Subsystem, DualMovementModeSubsystem {
    enum class DropdownPresets(val position: Double) {
        Init(0.7),
        Up(0.22),
        FivePixels(0.22),
        FivePixelsForAuto(0.20),
//        FourPixels(0.2),
        ThreePixels(0.1),
        TwoPixels(0.08),
        OnePixel(0.0)
    }

    @Serializable
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