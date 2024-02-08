package us.brainstormz.faux

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry

class FauxOpMode(telemetry: Telemetry): OpMode() {
    init {
        this.telemetry = telemetry
        gamepad1 = Gamepad()
        gamepad2 = Gamepad()
//        hardwareMap = HardwareMap()
    }

    override fun init() {

    }

    override fun loop() {

    }
}