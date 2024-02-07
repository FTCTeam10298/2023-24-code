package us.brainstormz.pho.hardware

import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.AnalogInputController
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.util.SerialNumber

class PhoAnalogInputController: PhoDevice(), AnalogInputController {
    override val printSignature: String = "Analog Input"

    override fun getAnalogInputVoltage(channel: Int): Double {
        printInput("Not yet implemented")
        return 0.0
    }

    override fun getMaxAnalogInputVoltage(): Double {
        printInput("Not yet implemented")
        return 0.0
    }

    override fun getSerialNumber(): SerialNumber {
        printInput("Not yet implemented")
        return SerialNumber.createFake()
    }

}