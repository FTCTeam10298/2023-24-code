package us.brainstormz.telemetryWizard

import org.firstinspires.ftc.robotcore.external.Telemetry

object GlobalConsole {
    lateinit var console: TelemetryConsole

    fun newConsole(telemetry: Telemetry): TelemetryConsole {
        console = TelemetryConsole(telemetry)
        return console
    }
}