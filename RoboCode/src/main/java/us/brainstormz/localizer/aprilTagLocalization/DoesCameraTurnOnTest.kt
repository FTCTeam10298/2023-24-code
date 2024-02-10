package us.brainstormz.localizer.aprilTagLocalization

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.openCvAbstraction.OpenCvAbstraction

@TeleOp(name= "DoesCameraTurnOnTest", group= "aprilTag")
class DoesCameraTurnOnTest: LinearOpMode() {
    override fun runOpMode() {

        val opencv: OpenCvAbstraction = OpenCvAbstraction(this)
        opencv.init(hardwareMap)
        opencv.internalCamera = false
        opencv.cameraName = "Webcam 1"
        opencv.cameraOrientation = OpenCvCameraRotation.UPRIGHT

//        opencv.onNewFrame(propDetector!!::processFrame)

        telemetry.addLine("Done turning on the camera")
        telemetry.update()
        /** End Of Init */
        waitForStart()
        /** Start */
    }
}