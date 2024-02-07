package us.brainstormz.faux.hardware

import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.DigitalChannelController

class PhoDigitalChannel: PhoDevice(), DigitalChannel {
    override val printSignature: String = "DigitalChannel"

    override fun getMode(): DigitalChannel.Mode {
        printInput("Not yet implemented")
        return DigitalChannel.Mode.OUTPUT
    }

    override fun setMode(mode: DigitalChannel.Mode?) {
        printInput("Not yet implemented")
    }

    override fun setMode(mode: DigitalChannelController.Mode?) {
        printInput("Not yet implemented")
    }

    override fun getState(): Boolean {
        printInput("Not yet implemented")
        return false
    }

    override fun setState(state: Boolean) {
        printInput("Not yet implemented")

    }
}