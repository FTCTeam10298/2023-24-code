package us.brainstormz.robotTwo.subsystems

interface AddressableLightController {
    interface RGB

    /** Function takes no longer than 5ms */
    fun updateLightHalves(rightHalfColor: RGB, leftHalfColor: RGB)
}