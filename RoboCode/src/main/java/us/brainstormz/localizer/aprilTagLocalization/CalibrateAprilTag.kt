import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.PointInXInchesAndYInches
import us.brainstormz.localizer.PredeterminedFieldPoints
import us.brainstormz.localizer.aprilTagLocalization.AprilTagFieldConfigurations
import us.brainstormz.localizer.aprilTagLocalization.AprilTagLocalizationFunctions
import us.brainstormz.localizer.aprilTagLocalization.AprilTagPipelineForEachCamera
import us.brainstormz.localizer.aprilTagLocalization.ReusableAprilTagFieldLocalizer
import java.util.stream.IntStream.range
import kotlin.math.abs

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

    val firstPoint = PointInXInchesAndYInches(
            xInches = 47.7,
            yInches = -47.8
    )

    val secondPoint = PointInXInchesAndYInches(
            xInches = 23.4,
            yInches = -44.2
    )

    val thirdPoint = PointInXInchesAndYInches(
            xInches = 47.1,
            yInches = -20.1
    )

    val fourthPoint = PointInXInchesAndYInches(
            xInches = 23.6,
            yInches = -20.0
    )



    val fourPointsPredictedMeasurement = FourPoints(
            first = firstPoint,
            second = secondPoint,
            third = thirdPoint,
            fourth = fourthPoint
    )


    val deltas = findErrorOfFourPoints(
            allianceSide = ReusableAprilTagFieldLocalizer.AllianceSide.Blue,
            fourPointsPredictedMeasurement = fourPointsPredictedMeasurement
    )


    for (i in range(0, 4)){

        val eachDelta: PointInXInchesAndYInches? = when(i) {
            1 -> deltas.first
            2 -> deltas.second
            3 -> deltas.third
            4 -> deltas.fourth
            else -> null

        }


        val xInches = eachDelta?.xInches
        val yInches = eachDelta?.yInches

        if (xInches != null) {
            println("X for point {$eachDelta}: $xInches")
            println("X for point {$eachDelta}: $xInches")
        }
        else {
            println("I didn't get anything :(")
        }


        println()

    }



//    val aprilTagLocalization = AprilTagLocalizationFunctions(
//            cameraXOffset=0.00,
//            cameraYOffset=0.00 //it's right on center! Yay!
//    )
//
//
//
//    val targetAprilTagID: Int = 2
//    val inputCamRelative = CameraRelativePointInSpace(xInches=10.0, yInches=10.0, yawDegrees= 10.0)
//
//    val expectedOutputTagRelative = TagRelativePointInSpace(xInches=11.585, yInches=8.112, headingDegrees= 10.0)//heading degrees = yaw
//
//    val actualOutputTagRelative = returnCamCentricCoordsInTagCentricCoordsPartDeux(inputCamRelative)
//
//    val inputTagRelative = actualOutputTagRelative
//
//    val expectedOutputFieldRelative = FieldRelativePointInSpace(xInches=46.995, yInches=52.138, headingDegrees = 10.0)
//
//    val actualOutputFieldRelative = aprilTagLocalization.getCameraPositionOnField(targetAprilTagID, inputTagRelative,
//            allianceSideFound = ReusableAprilTagFieldLocalizer.AllianceSide.Red)
//
//
//    println("Our camera-relative input was $inputCamRelative" )
//    println("Our expected tag-relative output was $expectedOutputTagRelative")
//    println("...but we got this: $actualOutputTagRelative")
//
//    println("So we took that tag-relative position, $inputTagRelative.")
//    println("We expected to see a position of $expectedOutputFieldRelative")
//    println("Instead, we calculated that our camera is at $actualOutputFieldRelative.")

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

data class FourPoints(
        val first: PointInXInchesAndYInches,
        val second: PointInXInchesAndYInches,
        val third: PointInXInchesAndYInches,
        val fourth: PointInXInchesAndYInches
)

fun findErrorOfFourPoints(allianceSide: ReusableAprilTagFieldLocalizer.AllianceSide,
                          fourPointsPredictedMeasurement: FourPoints): FourPoints {
    val predeterminedFieldPoints = PredeterminedFieldPoints()

    val predeterminedFieldPointsRedOrBlue = when(allianceSide) {
        ReusableAprilTagFieldLocalizer.AllianceSide.Blue -> predeterminedFieldPoints.Blue
        ReusableAprilTagFieldLocalizer.AllianceSide.Red -> predeterminedFieldPoints.Red
    }

    val realWorldMeasurementOfFirst = predeterminedFieldPointsRedOrBlue.first
    val realWorldMeasurementOfSecond = predeterminedFieldPointsRedOrBlue.second
    val realWorldMeasurementOfThird = predeterminedFieldPointsRedOrBlue.third
    val realWorldMeasurementOfFourth = predeterminedFieldPointsRedOrBlue.fourth

    val predictedMeasurementOfFirst = fourPointsPredictedMeasurement.first
    val predictedMeasurementOfSecond = fourPointsPredictedMeasurement.second
    val predictedMeasurementOfThird = fourPointsPredictedMeasurement.third
    val predictedMeasurementOfFourth = fourPointsPredictedMeasurement.fourth

    val differenceBetweenActualMeasurementAndPredictionFirst = PointInXInchesAndYInches(
           xInches = realWorldMeasurementOfFirst.xInches - abs(predictedMeasurementOfFirst.xInches),
           yInches = realWorldMeasurementOfFirst.xInches - abs(predictedMeasurementOfFirst.xInches)

    )

    val differenceBetweenActualMeasurementAndPredictionSecond = PointInXInchesAndYInches(
            xInches = realWorldMeasurementOfSecond.xInches - abs(predictedMeasurementOfSecond.xInches),
            yInches = realWorldMeasurementOfSecond.xInches - abs(predictedMeasurementOfSecond.xInches)
    )

    val differenceBetweenActualMeasurementAndPredictionThird = PointInXInchesAndYInches(
            xInches = realWorldMeasurementOfThird.xInches - abs(predictedMeasurementOfThird.xInches),
            yInches = realWorldMeasurementOfThird.xInches - abs(predictedMeasurementOfThird.xInches)
    )

    val differenceBetweenActualMeasurementAndPredictionFourth = PointInXInchesAndYInches(
            xInches = realWorldMeasurementOfFourth.xInches - abs(predictedMeasurementOfFourth.xInches),
            yInches = realWorldMeasurementOfFourth.xInches - abs(predictedMeasurementOfFourth.xInches)
    )



    return(FourPoints(
            first = differenceBetweenActualMeasurementAndPredictionFirst,
            second = differenceBetweenActualMeasurementAndPredictionSecond,
            third = differenceBetweenActualMeasurementAndPredictionThird,
            fourth = differenceBetweenActualMeasurementAndPredictionFourth
    ))

}


@TeleOp
class AprilTagOmeter_Calibration: LinearOpMode() {

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

    //
    //x is always



    val currentFieldConfiguration = AprilTagFieldConfigurations.leftFieldAtWorlds

    val localizer = ReusableAprilTagFieldLocalizer(
            aprilTagLocalization = aprilTagLocalization,
            averageErrorRedSide = currentFieldConfiguration.RedAllianceOffsets,
            averageErrorBlueSide =  currentFieldConfiguration.BlueAllianceOffsets)



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

        data class RetainedState(
                val trianglePressed:Boolean
        )
        var previousTriangleState = RetainedState(trianglePressed = gamepad1.triangle)

        waitForStart()

        while(opModeIsActive()) {
            val state = RetainedState(trianglePressed = gamepad1.triangle)

            aprilTagThings.forEach { it.resumeStreaming() }

            val currentDetections = getListOfCurrentAprilTagsSeen()

            val aWasPressed = (state.trianglePressed && !previousTriangleState.trianglePressed)

            if (aWasPressed) {
                println("button got hit!")
                telemetryPutTarget(currentDetections = currentDetections)
                telemetry.update()
            }

            showAllAprilTagsInfo(currentDetections)

//            // Share the CPU.
//            sleep(20)
            telemetry.update()

            previousTriangleState = state

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

                val allDetectionData: ReusableAprilTagFieldLocalizer.AprilTagAndData? = localizer.returnAprilTagInFieldCentricCoords(detection)

                val detectionFieldCoords = localizer.getFieldPositionsForTag(detection)
                val detectionAllianceSide = allDetectionData?.AllianceSide
                val detectionAccuracy = allDetectionData?.valueHasOneInchAccuracy
                val detectionTagCoords = localizer.returnAprilTagInFieldCentricCoords(detection)?.TagRelativePointInSpace

                if (currentDetections.isNotEmpty()) {

                    println(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name))
                    //
                    println(String.format("XYH %6.1f %6.1f %6.1f  (inch, inch, deg)",
                            detectionFieldCoords?.xInches,
                            detectionFieldCoords?.yInches,
                            detectionFieldCoords?.headingDegrees))

                    println(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw))

                    println(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation))
                    println(String.format("We're on alliance side {$detectionAllianceSide}, and our code says this measurement is accurate: {$detectionAccuracy}"))
                }
            }
            val closestAprilTag: AprilTagDetection = aprilTagLocalization.findClosestAprilTagToBot(currentDetections)
            showTargetAprilTagInfo(
                    listOfAllAprilTagsDetected = currentDetections,
                    leastDistortedAprilTag = closestAprilTag)

        }


    }

    private fun telemetryPutTarget(currentDetections: List<AprilTagDetection>) {

        if (currentDetections.isNotEmpty()) {

            val closestAprilTag: AprilTagDetection = aprilTagLocalization.findClosestAprilTagToBot(currentDetections)

            showTargetAprilTagInfo(
                    listOfAllAprilTagsDetected = currentDetections,
                    leastDistortedAprilTag = closestAprilTag)

            for (detection in currentDetections) {


                val allDetectionData: ReusableAprilTagFieldLocalizer.AprilTagAndData? = localizer.returnAprilTagInFieldCentricCoords(detection)

                val detectionFieldCoords = localizer.getFieldPositionsForTag(detection)
                val detectionAllianceSide = allDetectionData?.AllianceSide
                val detectionAccuracy = allDetectionData?.valueHasOneInchAccuracy
                val detectionTagCoords = localizer.returnAprilTagInFieldCentricCoords(detection)?.TagRelativePointInSpace

                if (currentDetections.isNotEmpty()) {

                    println(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name))
                    //
                    println(String.format("XYH %6.1f %6.1f %6.1f  (inch, inch, deg)",
                            detectionFieldCoords?.xInches,
                            detectionFieldCoords?.yInches,
                            detectionFieldCoords?.headingDegrees))

                    println(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw))

                    println(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation))
                    println(String.format("We're on alliance side {$detectionAllianceSide}, and our code says this measurement is accurate: {$detectionAccuracy}"))

                    if (detection.id == closestAprilTag.id) {
                        println("We found and maybe logged the correct tag!")
                        val xToLog = detectionFieldCoords?.xInches
                        val yToLog = detectionFieldCoords?.yInches

                        telemetry.addLine("X: {$xToLog}, Y: {$yToLog}")

                    } else {
                        println("we found nothing.")
                    }


                }
            }


        }
        else {
            println("there weren't any detections, sadge")
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
                idOfTargetAprilTag = leastDistortedAprilTag.id
        )


        if (theTargetAprilTag != null) {
            val theTargetAprilTagPositionInfo = localizer.returnAprilTagInFieldCentricCoords(theTargetAprilTag)
            val theTargetAprilTagPositionTagRelative = theTargetAprilTagPositionInfo.TagRelativePointInSpace
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

//                println("Field X: ${theTargetAprilTagPositionFieldRelative?.xInches}")
//                println("Robot Y: ${theTargetAprilTagPositionFieldRelative?.yInches}")
//                println("Robot Bearing: ${theTargetAprilTagPositionFieldRelative?.headingDegrees}")
                println("Least Distorted Apriltag: $idOfLeastDistortedTag")





                //
//                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z))
//
//                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw))
//
//                telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation))
                val muchData = aprilTagLocalization.getAprilTagLocation(detection.id)
//                telemetry.addLine("Random Madness? $muchData")


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