package us.brainstormz.faux.hardware

import com.qualcomm.robotcore.hardware.HardwareDevice

interface FauxActionTracker {
    val printSignature: String
    fun printInput(message: String) {
        println("$printSignature $message")
    }
}
abstract class FauxDevice: FauxActionTracker, HardwareDevice{

    final override fun getManufacturer(): HardwareDevice.Manufacturer {
        printInput("Not yet implemented")
        return HardwareDevice.Manufacturer.Unknown
    }

    final override fun getDeviceName(): String {
        printInput("Not yet implemented")
        return ""
    }

    final override fun getConnectionInfo(): String {
        printInput("Not yet implemented")
        return ""
    }

    final override fun getVersion(): Int {
        printInput("Not yet implemented")
        return 0
    }

    final override fun resetDeviceConfigurationForOpMode() {
        printInput("Not yet implemented")
    }

    final override fun close() {
        printInput("Not yet implemented")
    }
}