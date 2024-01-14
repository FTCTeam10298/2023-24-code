package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.hardware.*

open class MotorEncoderOnly(val motor:DcMotor, val reversed: Boolean = false) {
    fun getCurrentPosition(): Int {
//        val console = GlobalConsole.console
//        console.display(5, "gettingReversedPosition")
        return motor.currentPosition * when (reversed) {
            true -> -1
            false -> 1
        }
    }
}