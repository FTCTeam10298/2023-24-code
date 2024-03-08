package us.brainstormz.robotTwo

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.hardware.AnalogInput
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit

class AxonEncoderReader(private val axonEncoder: AnalogInput, private val hub: LynxModule, val angleOffsetDegrees: Double = 0.0, val direction: Direction = Direction.Forward) {
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
    fun getPositionDegrees(): Double = ((getRawPositionDegrees() * forwardBackwardMultiplier) + angleOffsetDegrees).mod(360.0)

    /** Angle from 0-360 */
    fun getRawPositionDegrees(): Double {
        val servoSuppliedVoltage = hub.getAuxiliaryVoltage(VoltageUnit.VOLTS)
        println("servoSuppliedVoltage: $servoSuppliedVoltage")


        val voltageStepDown = 5.0-3.3
        val suppliedVoltage = servoSuppliedVoltage - voltageStepDown
        println("suppliedVoltage: $suppliedVoltage")
        // get the voltage of our analog line
        // divide by 3.3 (the max voltage) to get a value between 0 and 1
        // multiply by 360 to convert it to 0 to 360 degrees
//        return ((axonEncoder.voltage / 3.3) * 360)
        return ((axonEncoder.voltage / suppliedVoltage) * 360)
    }
}