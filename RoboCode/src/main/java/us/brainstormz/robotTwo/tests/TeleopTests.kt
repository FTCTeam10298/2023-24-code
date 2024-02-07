package us.brainstormz.robotTwo.tests

class TeleopTests {
}

fun main() {
    val telemetry = PrintlnTelemetry()
    telemetry.addLine()
    telemetry.addLine("Hi there")
    telemetry.update()
}