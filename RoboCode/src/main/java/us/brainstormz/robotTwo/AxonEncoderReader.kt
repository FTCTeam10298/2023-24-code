package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.AnalogInput


class AxonEncoderReader(val axonEncoder: AnalogInput) {
    fun getPositionDegrees(): Double {
        // get the voltage of our analog line
        // divide by 3.3 (the max voltage) to get a value between 0 and 1
        // multiply by 360 to convert it to 0 to 360 degrees
        return (axonEncoder.voltage / 3.3) * 360
    }
}