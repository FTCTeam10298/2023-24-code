package us.brainstormz.robotTwo

import com.outoftheboxrobotics.photoncore.Photon
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.openCvAbstraction.OpenCvAbstraction


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

    override fun start() {
        teleop.start()
    }

    override fun loop() {
        teleop.loop(gamepad1= gamepad1, gamepad2= gamepad2, hardware= hardware)
    }

}

@Photon
@Autonomous(name = "RobotTwoAuto", group = "!")
class Autonomous: OpMode() {
    private val hardware: RobotTwoHardware= RobotTwoHardware(telemetry= telemetry, opmode = this)

    private lateinit var auto: RobotTwoAuto
    private val opencv: OpenCvAbstraction = OpenCvAbstraction(this)
    override fun init() {
        hardware.init(hardwareMap)

        auto = RobotTwoAuto(telemetry)

        opencv.init(hardwareMap)
        auto.init(hardware, opencv)
    }

    override fun init_loop() {
        auto.init_loop(hardware, opencv, gamepad1)
    }

    override fun start() {
        auto.start(hardware, opencv)
    }

    override fun loop() {
        auto.loop(hardware= hardware, gamepad1)
    }

}