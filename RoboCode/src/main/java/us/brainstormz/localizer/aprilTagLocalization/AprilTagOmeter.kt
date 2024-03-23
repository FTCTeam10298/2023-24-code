import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.aprilTagLocalization.AprilTagLocalizationOTron
import us.brainstormz.localizer.aprilTagLocalization.Foo
import us.brainstormz.robotTwo.RobotTwoHardware

@Autonomous
class AprilTagOmeter: LinearOpMode() {

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

    /** Gabe edit me */
    private fun returnTargetAprilTag(allAprilTags: List<AprilTagDetection>): AprilTagDetection? {

        for (thisAprilTag in allAprilTags) {
            if (thisAprilTag.id == targetAprilTagID) {
                return thisAprilTag
            }
        }
        return null
    }

    var isThisTheFirstTimeAnyAprilTagHasBeenSeen = true

    private fun telemetryAprilTag() {

        val currentDetections: List<AprilTagDetection> = aprilTagThings.flatMap{it.detections()}


        telemetry.addData("# AprilTags Detected", currentDetections.size)

        //Step through the list of detections and find the tag with the least x value,
        //meaning least distance from center of camera, meaning *most accurate* source of
        //data.

        //Find tag that is least rotated from being straight on (least off axis)
        val theTargetAprilTag = returnTargetAprilTag(currentDetections)
        if (theTargetAprilTag != null) {
            telemetry.addLine(String.format("\n==== (ID %d) %s", theTargetAprilTag?.id, "WAS YOUR SELECTED TAG, AND I FOUND IT!"))

            // Step through the list of detections and display info for each one.

            if (currentDetections.isNotEmpty()) {
                val detection: AprilTagDetection = theTargetAprilTag ?: currentDetections.first();
//                if (detection == tagWithLeastYawDistortion) {
//                else {
                telemetry.addLine(String.format("\n==== (ID %d) %s", detection?.id, detection.metadata.name))
//                    }}

                val theTag: AprilTagDetection = detection

                //elvis operator
                val currentPositionOfRobot = aprilTagLocalization.getCameraPositionOnField(theTag ?: currentDetections.first()).posAndRot
                val currentRobotPositionRelativeToCamera = theTargetAprilTag.ftcPose.bearing

                if (isThisTheFirstTimeAnyAprilTagHasBeenSeen) {
                    isThisTheFirstTimeAnyAprilTagHasBeenSeen = false
                }

                telemetry.addLine("AprilTag Current Position Of Robot (tag ${detection.id}): $currentRobotPositionRelativeToCamera")

                println("Robot X: ${theTargetAprilTag.ftcPose.x}")
                println("Robot Y: ${theTargetAprilTag.ftcPose.y}")
                println("Robot Bearing: ${theTargetAprilTag.ftcPose.bearing}")



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