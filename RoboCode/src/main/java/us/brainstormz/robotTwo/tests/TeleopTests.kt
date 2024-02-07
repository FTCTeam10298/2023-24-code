package us.brainstormz.robotTwo.tests

import us.brainstormz.pho.PhoOpMode
import us.brainstormz.pho.PrintlnTelemetry

class TeleopTests {
    fun main() {
        val telemetry = PrintlnTelemetry()
        telemetry.addLine()
        telemetry.addLine("Hi there")
        telemetry.update()
    }
}

fun main() {
    val opmode = PhoOpMode(telemetry = PrintlnTelemetry())
    opmode.init()
    opmode.init_loop()
    opmode.start()
    opmode.loop()
}
