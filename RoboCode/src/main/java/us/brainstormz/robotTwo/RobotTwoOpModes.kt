package us.brainstormz.robotTwo

import android.util.Size
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.VisionPortal.MultiPortalLayout
import org.openftc.easyopencv.OpenCvCameraFactory.ViewportSplitMethod
import us.brainstormz.faux.FauxLocalizer
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.localizer.aprilTagLocalization.AprilTagPipelineForEachCamera
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import java.lang.Thread.sleep

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

    private val propDetectorOpencv = OpenCvAbstraction(this)
    private val aprilTagPipeline = AprilTagPipelineForEachCamera("Webcam 1", Size(640, 480))
    private val cameraMonitorViewId = VisionPortal.makeMultiPortalView(2, MultiPortalLayout.HORIZONTAL)
    private lateinit var auto: RobotTwoAuto
    override fun init() {
        hardware.init(hardwareMap)

        propDetectorOpencv.init(hardwareMap = hardwareMap, cameraMonitorViewId.first())

        auto = RobotTwoAuto(multiTelemetry)

        auto.init(hardware)
    }

    override fun init_loop() {
        auto.initLoop(hardware, propDetectorOpencv, gamepad1)
    }

    override fun start() {
        auto.start(hardware)

        propDetectorOpencv.stop()

        sleep(500)

        aprilTagPipeline.init(viewContainerId = cameraMonitorViewId.last(), hardwareMap = hardwareMap)
        aprilTagPipeline.resumeStreaming()
    }

    override fun loop() {
        auto.loop(hardware= hardware, aprilTagPipeline, SerializableGamepad(gamepad1))
    }

    override fun stop() {
        aprilTagPipeline.close()
    }
}


//@Photon
@Autonomous(name = "RobotTwoAutoNoCycles", group = "!")
class AutonomousNoCycles: OpMode() {

    private val multiTelemetry = MultipleTelemetry(telemetry, PrintlnTelemetry())
    private val hardware: RobotTwoHardware= RobotTwoHardware(telemetry= multiTelemetry, opmode = this)

    private val propDetectorOpencv = OpenCvAbstraction(this)
    private val aprilTagPipeline = AprilTagPipelineForEachCamera("Webcam 1", Size(640, 480))
    private val cameraMonitorViewId = VisionPortal.makeMultiPortalView(2, MultiPortalLayout.HORIZONTAL)
    private lateinit var auto: RobotTwoAutoNoCycles
    override fun init() {
        hardware.init(hardwareMap)

        propDetectorOpencv.init(hardwareMap = hardwareMap, cameraMonitorViewId.first())

        auto = RobotTwoAutoNoCycles(multiTelemetry)

        auto.init(hardware)
    }

    override fun init_loop() {
        auto.initLoop(hardware, propDetectorOpencv, gamepad1)
    }

    override fun start() {
        auto.start(hardware)

        propDetectorOpencv.stop()

        sleep(500)

        aprilTagPipeline.init(viewContainerId = cameraMonitorViewId.last(), hardwareMap = hardwareMap)
        aprilTagPipeline.resumeStreaming()
    }

    override fun loop() {
        auto.loop(hardware= hardware, aprilTagPipeline, SerializableGamepad(gamepad1))
    }

    override fun stop() {
        aprilTagPipeline.close()
    }
}