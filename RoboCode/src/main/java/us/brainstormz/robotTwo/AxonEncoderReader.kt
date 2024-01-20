package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.AnalogInput

class AxonEncoderReader(private val axonEncoder: AnalogInput, val angleOffsetDegrees: Double = 0.0, val direction: Direction = Direction.Forward) {
    enum class Direction {
        Forward,
        Reverse
    }
    val forwardBackwardMultiplier = when (direction) {
        Direction.Forward -> 1
        Direction.Reverse -> -1
    }

    /** Angle from 0..180 */
    fun getAngleFrom180Degrees(): Double = getPositionDegrees() % 180

    /** Angle from 0-360 */
    fun getPositionDegrees(): Double = (getRawPositionDegrees() * forwardBackwardMultiplier) + angleOffsetDegrees

    /** Angle from 0-360 */
    fun getRawPositionDegrees(): Double {
        // get the voltage of our analog line
        // divide by 3.3 (the max voltage) to get a value between 0 and 1
        // multiply by 360 to convert it to 0 to 360 degrees
        return ((axonEncoder.voltage / 3.3) * 360)
    }
}