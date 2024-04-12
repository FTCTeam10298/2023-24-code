package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.vision.VisionPortal
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.robotTwo.onRobotTests.AprilTagPipeline

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
        teleop.loop(gamepad1= SerializableGamepad(gamepad1), gamepad2= SerializableGamepad(gamepad2), hardware= hardware)
    }

}

//@Photon
@Autonomous(name = "RobotTwoAuto", group = "!")
class Autonomous: OpMode() {

//    private val multiTelemetry = MultipleTelemetry(telemetry, FtcDashboard.getInstance().telemetry)
    private val multiTelemetry = telemetry
    private val hardware: RobotTwoHardware= RobotTwoHardware(telemetry= multiTelemetry, opmode = this)

    private lateinit var auto: RobotTwoAuto
    private val opencv: OpenCvAbstraction = OpenCvAbstraction(this)
    private val aprilTagPipeline = AprilTagPipeline(hardware.backCameraName, hardware.backCameraResolution)
    override fun init() {
        hardware.init(hardwareMap)

        val containerIds = VisionPortal.makeMultiPortalView(2, VisionPortal.MultiPortalLayout.HORIZONTAL)
        aprilTagPipeline.init(containerIds.first(), hardwareMap)

        opencv.init(hardwareMap, containerIds.last())

        auto = RobotTwoAuto(multiTelemetry, aprilTagPipeline)

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

    override fun stop() {
        aprilTagPipeline.close()
    }
}