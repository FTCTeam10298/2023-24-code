package us.brainstormz.robotTwo

import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.vision.VisionPortal
import us.brainstormz.faux.FauxLocalizer
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.robotTwo.onRobotTests.AprilTagPipeline

@TeleOp(name = "RobotTwoTeleOp", group = "!")
class TeleOpMode: OpMode() {
    private val hardware: RobotTwoHardware= RobotTwoHardware(telemetry= telemetry, opmode = this)

    private lateinit var teleop: RobotTwoTeleOp
    override fun init() {
        hardware.init(hardwareMap)

        teleop = RobotTwoTeleOp(telemetry)
        teleop.initRobot(hardware, FauxLocalizer())
    }

    override fun start() {
        teleop.start()
    }

    override fun loop() {
        teleop.loop(gamepad1= SerializableGamepad(gamepad1), gamepad2= SerializableGamepad(gamepad2), hardware= hardware)
    }

}

//@Photon
@Autonomous(name = "RobotTwoAuto", group = "!")
class Autonomous: OpMode() {

    private val multiTelemetry = MultipleTelemetry(telemetry, PrintlnTelemetry())
    private val hardware: RobotTwoHardware= RobotTwoHardware(telemetry= multiTelemetry, opmode = this)

    private lateinit var auto: RobotTwoAuto
    override fun init() {
        hardware.init(hardwareMap)

        auto = RobotTwoAuto(multiTelemetry)

        auto.init(hardware)
    }

    override fun init_loop() {
        auto.initLoop(hardware, gamepad1)
    }

    override fun start() {
        auto.start(hardware)
    }

    override fun loop() {
        auto.loop(hardware= hardware, SerializableGamepad(gamepad1))
    }
}