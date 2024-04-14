package us.brainstormz.localizer.aprilTagLocalization

import android.util.Size
import com.qualcomm.robotcore.hardware.HardwareMap
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName
import org.firstinspires.ftc.vision.VisionPortal
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor


open class AprilTagPipelineForEachCamera(val cameraName: String, val resolution: Size) {
    private var aprilTag: AprilTagProcessor? = null
    private var visionPortal: VisionPortal? = null

    fun init(viewContainerId:Int?, hardwareMap: HardwareMap) {
        aprilTag = AprilTagProcessor.Builder().build()

        aprilTag?.setDecimation(1f)

        val builder = VisionPortal.Builder()
                .enableLiveView(true)
                .setStreamFormat(VisionPortal.StreamFormat.YUY2)
        viewContainerId?.let(builder::setLiveViewContainerId)

        builder.setCameraResolution(resolution)
        builder.setCamera(hardwareMap.get(WebcamName::class.java, cameraName))
        builder.addProcessor(aprilTag)



        visionPortal = builder.build()
    }

    fun close() {
        visionPortal!!.close()
    }

    fun detections() = aprilTag?.detections ?: emptyList()

    fun resumeStreaming() = visionPortal!!.resumeStreaming()
}