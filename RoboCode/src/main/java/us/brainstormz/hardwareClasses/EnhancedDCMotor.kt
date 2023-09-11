package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.hardware.*
import us.brainstormz.telemetryWizard.GlobalConsole

open class EnhancedDCMotor(private val motor:DcMotor):DcMotor by motor {
    var reversed: Boolean = false

    override fun getCurrentPosition(): Int {
//        val console = GlobalConsole.console
//        console.display(5, "gettingReversedPosition")
        return motor.currentPosition * when (reversed) {
            true -> -1
            false -> 1
        }
    }
}