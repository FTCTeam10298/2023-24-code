package us.brainstormz.robotTwo.tests

import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.RobotTwoTeleOp

//    fun main() {
//        val telemetry = PrintlnTelemetry()
//        telemetry.addLine()
//        telemetry.addLine("Hi there")
//        telemetry.update()
//    }

//    fun main() {
//        val opmode = PhoOpMode(telemetry = PrintlnTelemetry())
//        opmode.init()
//        opmode.init_loop()
//        opmode.start()
//        opmode.loop()
//    }

fun main() {
    val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
    val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
    val teleop = RobotTwoTeleOp(hardware, opmode.telemetry)
    teleop.init()
    teleop.loop(Gamepad(), Gamepad())
}