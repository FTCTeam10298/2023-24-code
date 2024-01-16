package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.hardware.*

open class MotorEncoderOnly(val motor:DcMotor, val reversed: Boolean = false) {
    fun getCurrentPosition(): Int {
        return motor.currentPosition * when (reversed) {
            true -> -1
            false -> 1
        }
    }
}