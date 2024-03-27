import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.aprilTagLocalization.AprilTagLocalizationOTron
import us.brainstormz.localizer.aprilTagLocalization.Foo
import kotlin.math.cos
import kotlin.math.sin

fun main () {
    val inputCamRelative = CameraRelativePointInSpace(xInches=88.0, yInches=0.0, yawDegrees= -1.0, rangeInches=88.0)

    val expectedOutputTagRelative = TagRelativePointInSpace(xInches=87.987, yInches=1.536, headingDegrees= 1.00)

    val actualOutputTagRelative = returnCamCentricCoordsInTagCentricCoords(inputCamRelative)

    

    println("input was $inputCamRelative" )
    println("Our expected output was $expectedOutputTagRelative")
    println("...but we got $actualOutputTagRelative")


}

data class CameraRelativePointInSpace(val xInches: Double, val yInches: Double, val yawDegrees: Double, val rangeInches: Double)
data class TagRelativePointInSpace(val xInches: Double, val yInches: Double, val headingDegrees: Double)

data class FieldRelativePointInSpace(val xInches: Double, val yInches: Double, val headingDegrees: Double)

private fun returnCamCentricCoordsInTagCentricCoords(anyOldTag: CameraRelativePointInSpace): TagRelativePointInSpace {
    //go look in the FTC documentation, you absolutely need to understand the FTC AprilTag Coordinate System

    val yawRadians = Math.toRadians(anyOldTag.yawDegrees)

    val aSectionOfXInches = anyOldTag.xInches/cos(yawRadians)
    val yOutsideOfSquareInches = aSectionOfXInches * sin(yawRadians)

    //another angle in the triangle.
    val qDegrees: Double = 180 - 90 - anyOldTag.yawDegrees

    val qRadians = Math.toRadians(qDegrees)

    val yInsideOfSquareInches = anyOldTag.yInches - yOutsideOfSquareInches

    val otherPartOfXInches = yInsideOfSquareInches * cos(qRadians)
    val yRelativeToTagInches = yInsideOfSquareInches * sin(qRadians)
    val xRelativeToTagInches = aSectionOfXInches + otherPartOfXInches

    return TagRelativePointInSpace(xInches=xRelativeToTagInches, yInches=yRelativeToTagInches, headingDegrees=anyOldTag.yawDegrees)

}

private fun returnTagCentricCoordsInFieldCoords() {

}




@Autonomous
class AprilTagOmeter_CamCentricToFieldCentric: LinearOpMode() {

    // A
    //03-19 22:53:42.309  1691  1817 I System.out: Robot X: -0.7379603385925293
    //03-19 22:53:42.309  1691  1817 I System.out: Robot Y: 21.688961029052734
    //03-19 22:53:42.309  1691  1817 I System.out: Robot Bearing: 1.948719801258371





    //configure cam offset
//    val robotCameraYOffset = RobotTwoHardware.robotLengthInches/2 //only hardware ref
//    var aprilTagLocalization = AprilTagLocalizationOTron(
//            cameraXOffset=robotCameraYOffset,
//            cameraYOffset=0.00 //it's right on center! Yay!
//    )

    //...but let's not
    val robotCameraYOffset = 0.0
    var aprilTagLocalization = AprilTagLocalizationOTron(
            cameraXOffset=robotCameraYOffset,
            cameraYOffset=0.00 //it's right on center! Yay!
    )

    val targetAprilTagID = 2

    private val aprilTagThings = listOf(
//            Size(2304, 1536)
            //default is "Webcam 1", for 2023-24 it's...
            Foo("Webcam 1", Size(640, 480)),
//            Foo("Webcam 2", Size(320, 240)),
//            Foo("Webcam 3", Size(320, 240)),
//          Foo("Webcam 4", Size(320, 240)) - Not working. Each bus seems to support 2 cameras.
            // Idea: Half res for all other cameras, then add the other on its lowest res (320 by 240)...
    )


    override fun runOpMode() {

        aprilTagThings.forEach {
            telemetry.addLine("starting cameras")
            telemetry.update()
            it.init(null, hardwareMap)

        }

//        hardware.init(hardwareMap)


        waitForStart()

        while(opModeIsActive()) {

            aprilTagThings.forEach { it.resumeStreaming() }

            telemetryAprilTag()
            // Share the CPU.
            sleep(20)
            telemetry.update()

        }
        if(isStopRequested) {
            aprilTagThings.forEach { it.close() }
            //TODO: STOP CRASHING THE BOT EVERY RUN...
            //REBOOT THE BOT IF THE CAMERA BLUESCREENS and is on Hardware Config.
            //Also, we can just leave this code running to get data.
        }
    }

    data class aprilTagAndData(val anAprilTag: AprilTagDetection, val aTagRelativePointInSpace: TagRelativePointInSpace)

    /** Gabe edit me */
    private fun returnAprilTagInTagCentricCoords(aprilTag: AprilTagDetection): aprilTagAndData? {
        //go look in the FTC documentation, you absolutely need to understand the FTC AprilTag Coordinate System

        //you need to look at the diagram to get this.

        //getting l

        if (aprilTag != null) {
            val thisAprilTagPose = aprilTag.ftcPose
            val representationOfAprilTag = CameraRelativePointInSpace(xInches=thisAprilTagPose.x,
                    yInches=thisAprilTagPose.y, yawDegrees=thisAprilTagPose.yaw, rangeInches=thisAprilTagPose.range)
            val thisTagInTagCentricCoords = returnCamCentricCoordsInTagCentricCoords(representationOfAprilTag)
            val resultPosition = TagRelativePointInSpace(thisTagInTagCentricCoords.xInches,
                    thisTagInTagCentricCoords.yInches, thisTagInTagCentricCoords.headingDegrees)

            return aprilTagAndData(aprilTag, resultPosition)
        }
        else return null
    }

    private fun returnTargetAprilTag(currentDetections: List<AprilTagDetection>): aprilTagAndData? {
        for (detection in currentDetections) {
            if (detection.id == targetAprilTagID) {
                val targetAprilTagInTagCentricCoords = returnAprilTagInTagCentricCoords(detection)
                return targetAprilTagInTagCentricCoords
            }
        }

       return null
    }
    private fun telemetryAprilTag() {

        val currentDetections: List<AprilTagDetection> = aprilTagThings.flatMap{it.detections()}


        telemetry.addData("# AprilTags Detected", currentDetections.size)

        //Step through the list of detections and find the tag with the least x value,
        //meaning least distance from center of camera, meaning *most accurate* source of
        //data.

        //Find tag that is least rotated from being straight on (least off axis)


        val theTargetAprilTag: AprilTagDetection? = returnTargetAprilTag(currentDetections)?.anAprilTag
        val theTargetAprilTagPosition = returnTargetAprilTag(currentDetections)?.aTagRelativePointInSpace
        if (theTargetAprilTag != null) {
            telemetry.addLine(String.format("\n==== (ID %d) %s", theTargetAprilTag.id, "WAS YOUR SELECTED TAG, AND I FOUND IT!"))

            // Step through the list of detections and display info for each one.

            if (currentDetections.isNotEmpty()) {
                val detection: AprilTagDetection = theTargetAprilTag ?: currentDetections.first();
//                if (detection == tagWithLeastYawDistortion) {
//                else {
                telemetry.addLine(String.format("\n==== (ID %d) %s", detection?.id, detection.metadata.name))
//                    }}

                val theTag: AprilTagDetection = detection
                val idOfTargetTag = (currentDetections.first().id).toInt()

                //elvis operator
                val currentPositionOfRobot = aprilTagLocalization.getCameraPositionOnField(aprilTagID=idOfTargetTag, aprilTagInTagCentricCoords=theTargetAprilTagPosition!!).posAndRot
                val currentRobotPositionRelativeToCamera = theTargetAprilTag.ftcPose.bearing

                telemetry.addLine("AprilTag Current Position Of Robot (tag ${detection.id}): $currentRobotPositionRelativeToCamera")

                println("Robot X: ${theTargetAprilTagPosition?.xInches}")
                println("Robot Y: ${theTargetAprilTagPosition?.yInches}")
                println("Robot Bearing: ${theTargetAprilTagPosition?.headingDegrees}")



                //
//                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z))
//
//                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw))
//
//                telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation))
                val muchData = aprilTagLocalization.getAprilTagLocation(detection.id)
//                telemetry.addLine("Random Madness!! $muchData")


//            } else {
//                telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id))
//                telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y))
//            }
            }
        }
        else {
            telemetry.addLine("I just don't see it.")
        }
         // ...


        // Add "key" information to telemetry
//        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.")
//        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)")
//        telemetry.addLine("RBE = Range, Bearing & Elevation")
    } //...


}