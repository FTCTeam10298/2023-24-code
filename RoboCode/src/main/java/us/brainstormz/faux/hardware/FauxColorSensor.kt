package us.brainstormz.faux.hardware

import com.qualcomm.robotcore.hardware.ColorSensor
import com.qualcomm.robotcore.hardware.I2cAddr

class FauxColorSensor: FauxDevice(), ColorSensor {
    override val printSignature: String = "Color Sensor"

    override fun red(): Int {
        printInput("Not yet implemented")
        return 0
    }

    override fun green(): Int {
        printInput("Not yet implemented")
        return 0
    }

    override fun blue(): Int {
        printInput("Not yet implemented")
        return 0
    }

    override fun alpha(): Int {
        printInput("Not yet implemented")
        return 0
    }

    override fun argb(): Int {
        printInput("Not yet implemented")
        return 0
    }

    override fun enableLed(enable: Boolean) {
        printInput("Not yet implemented")
    }

    override fun setI2cAddress(newAddress: I2cAddr?) {
        printInput("Not yet implemented")
    }

    override fun getI2cAddress(): I2cAddr {
        printInput("Not yet implemented")
        return I2cAddr(0)
    }
}