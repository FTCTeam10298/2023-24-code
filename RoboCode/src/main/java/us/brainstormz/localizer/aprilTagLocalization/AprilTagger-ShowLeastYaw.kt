//package us.brainstormz.robotTwo
//
//import android.util.Size
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
//import us.brainstormz.localizer.PositionAndRotation
//import us.brainstormz.localizer.aprilTagLocalization.AprilTagLocalizationOTron
//import us.brainstormz.localizer.aprilTagLocalization.Foo
//
//import kotlin.math.absoluteValue
//
//
////gabe this class is your example
//@Autonomous
////test full pipeline.
//class `AprilTagger-ShowBestFound`: LinearOpMode() {
//
//    //configure cam offset
////    val robotCameraYOffset = RobotTwoHardware.robotLengthInches/2 //only hardware ref
////    var aprilTagLocalization = AprilTagLocalizationOTron(
////            cameraXOffset=robotCameraYOffset,
////            cameraYOffset=0.00 //it's right on center! Yay!
////    )
//
//    //...but let's not
//    val robotCameraYOffset = RobotTwoHardware.robotLengthInches/2
//    var aprilTagLocalization = AprilTagLocalizationOTron(
//            cameraXOffset=robotCameraYOffset,
//            cameraYOffset=0.00 //it's right on center! Yay!
//    )
//
//    private val aprilTagThings = listOf(
////            Size(2304, 1536)
//            //default is "Webcam 1", for 2023-24 it's...
//            Foo("Webcam 1", Size(1920, 1080)),
////            Foo("Webcam 2", Size(320, 240)),
////            Foo("Webcam 3", Size(320, 240)),
////          Foo("Webcam 4", Size(320, 240)) - Not working. Each bus seems to support 2 cameras.
//            // Idea: Half res for all other cameras, then add the other on its lowest res (320 by 240)...
//    )
//
//
//    override fun runOpMode() {
//
//        aprilTagThings.forEach {
//            telemetry.addLine("starting cameras")
//            telemetry.update()
//            it.init(null, hardwareMap)
//
//        }
//
////        hardware.init(hardwareMap)
//
//
//        waitForStart()
//
//        while(opModeIsActive()) {
//
//            aprilTagThings.forEach { it.resumeStreaming() }
//
//            // Share the CPU.
//            sleep(20)
//            telemetry.update()
//
//        }
//
//    }
//
//    /** Gabe edit me */
//    private fun chooseBestAprilTag(allAprilTags: List<AprilTagDetection>): AprilTagDetection? {
//        val sortedByYaw = allAprilTags.sortedBy {
//            val yawAbs = it.ftcPose.yaw.absoluteValue
//            telemetry.addLine("yawAbs: $yawAbs, tag ID: ${it.id}")
//            yawAbs
//        }
//        val topTwoYaw = listOf(sortedByYaw[0], sortedByYaw[1])
//        val differenceInYawBetweenTopChoices = (topTwoYaw[1].ftcPose.yaw - topTwoYaw[0].ftcPose.yaw).absoluteValue
//        val areTheYawValuesSuperClose:Boolean = differenceInYawBetweenTopChoices < 0.5 //too large... 0.5 worked better
//        if (areTheYawValuesSuperClose) {
//            val closestToCenter: AprilTagDetection = topTwoYaw.minBy {
//                val bearing = it.ftcPose.bearing.absoluteValue
//                telemetry.addLine("bearing: $bearing, tag ID: ${it.id}")
//                bearing
//            }
//            return closestToCenter
//        }
//        else {
//            return sortedByYaw[0]
//        }
//
//    }
//
//    var isThisTheFirstTimeAnyAprilTagHasBeenSeen = true
//
//    private fun telemetryAprilTag(roadRunnerPosition: PositionAndRotation) {
//
//        var currentDetections: List<AprilTagDetection> = aprilTagThings.flatMap{it.detections()}
//
//
//        telemetry.addData("# AprilTags Detected", currentDetections.size)
//
//        //Step through the list of detections and find the tag with the least x value,
//        //meaning least distance from center of camera, meaning *most accurate* source of
//        //data.
//
//        //Find tag that is least rotated from being straight on (least off axis)
//        val tagWithLeastYawDistortion = chooseBestAprilTag(currentDetections)
//        telemetry.addLine(String.format("\n==== (ID %d) %s", tagWithLeastYawDistortion?.id, "TAG WITH LEAST YAW and kinda least off axis!"))
//
//        // Step through the list of detections and display info for each one.
//
//        if (currentDetections.isNotEmpty()) {
//            val detection: AprilTagDetection = tagWithLeastYawDistortion ?: currentDetections.first();
////                if (detection == tagWithLeastYawDistortion) {
////                else {
//            telemetry.addLine(String.format("\n==== (ID %d) %s", detection?.id, detection.metadata.name))
////                    }}
//
//            val theTag: AprilTagDetection = detection
//            val whatTag = theTag?.id
//
//            //elvis operator
//            val currentPositionOfRobot = aprilTagLocalization.getCameraPositionOnField(theTag ?: currentDetections.first()).posAndRot
//            if (isThisTheFirstTimeAnyAprilTagHasBeenSeen) {
//                isThisTheFirstTimeAnyAprilTagHasBeenSeen = false
//            }
//
//            val orientation = currentPositionOfRobot.r
//            val tagPosition = aprilTagLocalization.getAprilTagLocation(whatTag ?: 0)
//
//            telemetry.addLine("AprilTag Current Position Of Robot (tag ${detection.id}): $currentPositionOfRobot")
//
//
//
//            //
////                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z))
////
////                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw))
////
////                telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation))
//            val muchData = aprilTagLocalization.getAprilTagLocation(detection.id)
////                telemetry.addLine("Random Madness!! $muchData")
//
//
////            } else {
////                telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id))
////                telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y))
////            }
//        } // ...
//
//
//        // Add "key" information to telemetry
////        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.")
////        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)")
////        telemetry.addLine("RBE = Range, Bearing & Elevation")
//    } //...
//
//
//}