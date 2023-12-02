package us.brainstormz.localizer

import android.util.Size
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.hardware.camera.BuiltinCameraDirection
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor


class Foo(private val name:String, val size:Size) {
    private var aprilTag: AprilTagProcessor? = null
    private var visionPortal: VisionPortal? = null

    fun init(viewContainerId:Int, hardwareMap:HardwareMap) {

        aprilTag = AprilTagProcessor.Builder().build()

        aprilTag?.setDecimation(1f)
//        aprilTag?.setPoseSolver(AprilTagProcessor.PoseSolver.APRILTAG_BUILTIN)

        val builder = VisionPortal.Builder()
                .enableLiveView(true)
                .setLiveViewContainerId(viewContainerId)
                .setStreamFormat(VisionPortal.StreamFormat.MJPEG)
                .setCameraResolution(size)
//                .setCameraResolution(Size(2304, 1536))

        builder.setCamera(hardwareMap.get(WebcamName::class.java, name))

        builder.addProcessor(aprilTag)

        visionPortal = builder.build()
    }

    fun close() {
        visionPortal!!.close()
    }

    fun detections() = aprilTag?.detections ?: emptyList()
    fun resumeStreaming() {
        visionPortal!!.resumeStreaming()
    }

}