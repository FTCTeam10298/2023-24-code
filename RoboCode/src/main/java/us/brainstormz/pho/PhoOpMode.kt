package us.brainstormz.pho

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.Telemetry

class PhoOpMode(telemetry: Telemetry): OpMode() {
    init {
        updateTelemetry(telemetry)
        gamepad1 = Gamepad()
        gamepad2 = Gamepad()
//        hardwareMap = HardwareMap()
    }

    override fun init() {

    }

    override fun loop() {

    }
}