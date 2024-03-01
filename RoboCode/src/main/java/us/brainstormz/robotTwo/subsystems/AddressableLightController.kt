package us.brainstormz.robotTwo.subsystems

interface AddressableLightController {
    data class RGB(val r: Int,val g: Int,val b: Int)

    enum class NamedColors(val rgb: RGB) {
        Red(RGB(r=255, 0, 0)),
        Green(RGB(0, g=255, 0))
    }

    /** Function takes no longer than 5ms */
    fun updateLightHalves(rightHalfColor: RGB, leftHalfColor: RGB)
}