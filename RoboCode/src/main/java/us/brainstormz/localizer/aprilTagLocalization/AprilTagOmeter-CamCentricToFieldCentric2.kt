import android.graphics.Point
import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.aprilTagLocalization.AprilTagLocalizationFunctions
import us.brainstormz.localizer.aprilTagLocalization.AprilTagPipelineForEachCamera
import us.brainstormz.localizer.aprilTagLocalization.AverageAprilTagLocalizationError
import us.brainstormz.localizer.aprilTagLocalization.ReusableAprilTagFieldLocalizer
import kotlin.math.cos
import kotlin.math.sin
import org.opencv.core.Point as PointD

/*
LIST OF TESTS
    ... 01

     val inputCamRelative = CameraRelativePointInSpace(xInches=5.0, yInches=10.0, yawDegrees= 0.0)

    val expectedOutputTagRelative = TagRelativePointInSpace(xInches=5.0, yInches=10.0, headingDegrees= 0.0)//heading degrees = yaw

    val actualOutputTagRelative = returnCamCentricCoordsInTagCentricCoords(inputCamRelative)

    val inputTagRelative = actualOutputTagRelative

    val expectedOutputFieldRelative = FieldRelativePointInSpace(xInches=50.25, yInches=40.41, headingDegrees = 0.0)

    val actualOutputFieldRelative = aprilTagLocalization.getCameraPositionOnField(targetAprilTagID, inputTagRelative)

    ... 02



 */

fun main () {
    val aprilTagLocalization = AprilTagLocalizationFunctions(
            cameraXOffset=0.00,
            cameraYOffset=0.00 //it's right on center! Yay!
    )


    val targetAprilTagID: Int = 2
    val inputCamRelative = CameraRelativePointInSpace(xInches=10.0, yInches=10.0, yawDegrees= 10.0)

    val expectedOutputTagRelative = TagRelativePointInSpace(xInches=11.585, yInches=8.112, headingDegrees= 10.0)//heading degrees = yaw

    val actualOutputTagRelative = returnCamCentricCoordsInTagCentricCoordsPartDeux(inputCamRelative)

    val inputTagRelative = actualOutputTagRelative

    val expectedOutputFieldRelative = FieldRelativePointInSpace(xInches=46.995, yInches=52.138, headingDegrees = 10.0)

    val actualOutputFieldRelative = aprilTagLocalization.getCameraPositionOnField(targetAprilTagID, inputTagRelative)


    println("Our camera-relative input was $inputCamRelative" )
    println("Our expected tag-relative output was $expectedOutputTagRelative")
    println("...but we got this: $actualOutputTagRelative")

    println("So we took that tag-relative position, $inputTagRelative.")
    println("We expected to see a position of $expectedOutputFieldRelative")
    println("Instead, we calculated that our camera is at $actualOutputFieldRelative.")






}

data class CameraRelativePointInSpace(val xInches: Double, val yInches: Double, val yawDegrees: Double)
data class TagRelativePointInSpace(val xInches: Double, val yInches: Double, val headingDegrees: Double)

data class FieldRelativePointInSpace(val xInches: Double, val yInches: Double, val headingDegrees: Double)



fun returnCamCentricCoordsInTagCentricCoordsPartDeux(anyOldTag: CameraRelativePointInSpace): TagRelativePointInSpace {
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

fun evaluateAprilTagAccuracy(detectedPoint: TagRelativePointInSpace) {
    val square0_1 = org.opencv.core.Point(0.0, 0.0)

}

//private fun returnTagCentricCoordsInFieldCoords(cameraXOffset: Double, cameraYOffset: Double, targetAprilTagID: Int, inputTagRelative: TagRelativePointInSpace): FieldRelativePointInSpace {
//
//    var aprilTagLocalization = AprilTagLocalizationOTron(
//            cameraXOffset=0.00,
//            cameraYOffset=0.00 //it's right on center! Yay!
//    )
//
//    val actualOutputFieldRelative = aprilTagLocalization.getCameraPositionOnField(targetAprilTagID, inputTagRelative)
//
//    return actualOutputFieldRelative
//
//}




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
    var aprilTagLocalization = AprilTagLocalizationFunctions(
            cameraXOffset=robotCameraYOffset,
            cameraYOffset=0.00 //it's right on center! Yay!
    )

    val averageErrors = AverageAprilTagLocalizationError(
            xInches = 0.47,
            yInches = 2.58,
            hDegrees = 2.68, //0
    )

    val localizer = ReusableAprilTagFieldLocalizer(aprilTagLocalization, averageErrors)

    val targetAprilTagID = 2

    private val aprilTagThings = listOf(
//            Size(2304, 1536)
            //default is "Webcam 1", for 2023-24 it's...
            AprilTagPipelineForEachCamera("Webcam 1", Size(640, 480)),
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

            val currentDetections = getListOfCurrentAprilTagsSeen()
            showAllAprilTagsInfo(currentDetections)

            // Share the CPU.
            sleep(20)
            telemetry.update()

        }
        if(isStopRequested) {
            aprilTagThings.forEach { it.close() }
            sleep(1000)
            //TODO: STOP CRASHING THE BOT EVERY RUN...
            //REBOOT THE BOT IF THE CAMERA BLUESCREENS and is on Hardware Config.
            //Also, we can just leave this code running to get data.
        }
    }

    /** Gabe edit me */
    private fun returnTargetAprilTag(currentDetections: List<AprilTagDetection>, idOfTargetAprilTag: Int): AprilTagDetection? {
        for (detection in currentDetections) {
            if (detection.id == idOfTargetAprilTag) {
                return detection
            }
        }

       return null
    }

    private fun getListOfCurrentAprilTagsSeen(): List<AprilTagDetection> {
        return aprilTagThings.flatMap { it.detections() }
    }


    private fun showAllAprilTagsInfo(currentDetections: List<AprilTagDetection>) {



        if (currentDetections.isNotEmpty()) {

            for (detection in currentDetections) {

                val detectionFieldCoords = localizer.getFieldPositionsForTag(detection)!!
                val detectionTagCoords =
                        localizer.returnAprilTagInFieldCentricCoords(detection)?.TagRelativePointInSpace

                if (currentDetections.isNotEmpty()) {

                    println(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name))
                    //
                    println(String.format("XYH %6.1f %6.1f %6.1f  (inch, inch, deg)",
                            detectionTagCoords?.xInches,
                            detectionTagCoords?.yInches,
                            detectionTagCoords?.headingDegrees))

                    println(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw))

                    println(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation))
                }
            }
            val closestAprilTag: AprilTagDetection = aprilTagLocalization.findClosestAprilTagToBot(currentDetections)
            showTargetAprilTagInfo(
                    listOfAllAprilTagsDetected = currentDetections,
                    leastDistortedAprilTag = closestAprilTag)

        }


    }

        fun showTargetAprilTagInfo(listOfAllAprilTagsDetected: List<AprilTagDetection>, leastDistortedAprilTag: AprilTagDetection) {




        telemetry.addData("# AprilTags Detected", listOfAllAprilTagsDetected.size)

        //Step through the list of detections and find the tag with the least x value,
        //meaning least distance from center of camera, meaning *most accurate* source of
        //data.

        //Find tag that is least rotated from being straight on (least off axis)


        val theTargetAprilTag: AprilTagDetection? = returnTargetAprilTag(
                currentDetections = listOfAllAprilTagsDetected,
                idOfTargetAprilTag = leastDistortedAprilTag.id)


        if (theTargetAprilTag != null) {
            val theTargetAprilTagPositionInfo = localizer.returnAprilTagInFieldCentricCoords(theTargetAprilTag)
            val theTargetAprilTagPositionTagRelative = theTargetAprilTagPositionInfo!!.TagRelativePointInSpace
            val theTargetAprilTagPositionFieldRelative = theTargetAprilTagPositionInfo.FieldRelativePointInSpace

            telemetry.addLine(String.format("\n==== (ID %d) %s", theTargetAprilTag.id, "WAS YOUR SELECTED TAG, AND I FOUND IT!"))

            // Step through the list of detections and display info for each one.

            if (listOfAllAprilTagsDetected.isNotEmpty()) {
                val detection: AprilTagDetection = theTargetAprilTag ?: listOfAllAprilTagsDetected.first();
//                if (detection == tagWithLeastYawDistortion) {
//                else {
                telemetry.addLine(String.format("\n==== (ID %d) %s", detection?.id, detection.metadata.name))
//                    }}

                val theTag: AprilTagDetection = detection
                val idOfLeastDistortedTag = leastDistortedAprilTag.id

                //elvis operator
                val currentRobotPositionRelativeToCamera = theTargetAprilTag.ftcPose.bearing


                telemetry.addLine("AprilTag Current Position Of Robot (tag ${detection.id}): $currentRobotPositionRelativeToCamera")
                telemetry.addLine("Least Distorted AprilTag: $idOfLeastDistortedTag")

//                println("Robot X: ${theTargetAprilTagPosition?.xInches}")
//                println("Robot Y: ${theTargetAprilTagPosition?.yInches}")
//                println("Robot Bearing: ${theTargetAprilTagPosition?.headingDegrees}")

//                println("Field X: ${theTargetAprilTagPositionFieldRelative!!.xInches}")
//                println("Robot Y: ${theTargetAprilTagPositionFieldRelative!!.yInches}")
//                println("Robot Bearing: ${theTargetAprilTagPositionFieldRelative!!.headingDegrees}")
                println("Least Distorted Apriltag: $idOfLeastDistortedTag")





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