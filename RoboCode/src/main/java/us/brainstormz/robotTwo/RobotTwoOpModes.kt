package us.brainstormz.robotTwo

import com.outoftheboxrobotics.photoncore.Photon
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp


@Photon
@TeleOp(name = "RobotTwoTeleOp", group = "!")
class TeleOpMode: OpMode() {
    private val hardware: RobotTwoHardware= RobotTwoHardware(telemetry= telemetry, opmode = this)

    private lateinit var teleop: RobotTwoTeleOp
    override fun init() {
        hardware.init(hardwareMap)

        teleop = RobotTwoTeleOp(telemetry)
        teleop.init(hardware)
    }

    override fun loop() {
        teleop.loop(gamepad1= gamepad1, gamepad2= gamepad2, hardware= hardware)
    }

}