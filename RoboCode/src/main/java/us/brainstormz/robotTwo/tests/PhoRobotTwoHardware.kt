package us.brainstormz.robotTwo.tests

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.pho.PhoOpMode
import us.brainstormz.pho.PrintlnTelemetry
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.reflect.typeOf

class PhoRobotTwoHardware(opmode: PhoOpMode, telemetry:Telemetry): RobotTwoHardware(opmode = opmode, telemetry = telemetry) {
    /** Don't use ahwMap */
    override fun init(ahwMap: HardwareMap) {
        println("Initializing hardware")

        val allFields = this::class.java.fields.toList()
        val motors = allFields.filter {it ->
            val type = it.type
            println("type: $type")
            it.type is DcMotorSimple
        }
        println(motors.size)
    }
}

