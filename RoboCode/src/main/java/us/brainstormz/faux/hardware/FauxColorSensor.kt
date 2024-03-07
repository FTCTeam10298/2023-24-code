package us.brainstormz.faux.hardware

import com.qualcomm.robotcore.hardware.ColorSensor
import com.qualcomm.robotcore.hardware.I2cAddr
import com.qualcomm.robotcore.hardware.NormalizedColorSensor
import com.qualcomm.robotcore.hardware.NormalizedRGBA

fun makeNormalizedRGBA(r:Float, g:Float, b:Float, a:Float):NormalizedRGBA {
    val v = NormalizedRGBA()
    v.alpha = a
    v.red = r
    v.green = g
    v.blue = b
    return v
}
class FauxColorSensor: FauxDevice(), NormalizedColorSensor {
    override val printSignature: String = "Color Sensor"
    private val fakeValue = makeNormalizedRGBA(
        r = 0f,
        g = 0f,
        b = 0f,
        a = 0f,
    )
    override fun getNormalizedColors(): NormalizedRGBA {
        printInput("Not yet implemented")
        return fakeValue
    }

    override fun getGain(): Float {
        printInput("Not yet implemented")
        return 0f
    }

    override fun setGain(p0: Float) {
        printInput("Not yet implemented")
    }

//    override fun red(): Int {
//        printInput("Not yet implemented")
//        return 0
//    }
//
//    override fun green(): Int {
//        printInput("Not yet implemented")
//        return 0
//    }
//
//    override fun blue(): Int {
//        printInput("Not yet implemented")
//        return 0
//    }
//
//    override fun alpha(): Int {
//        printInput("Not yet implemented")
//        return 0
//    }
//
//    override fun argb(): Int {
//        printInput("Not yet implemented")
//        return 0
//    }
//
//    override fun enableLed(enable: Boolean) {
//        printInput("Not yet implemented")
//    }
//
//    override fun setI2cAddress(newAddress: I2cAddr?) {
//        printInput("Not yet implemented")
//    }
//
//    override fun getI2cAddress(): I2cAddr {
//        printInput("Not yet implemented")
//        return I2cAddr(0)
//    }
}