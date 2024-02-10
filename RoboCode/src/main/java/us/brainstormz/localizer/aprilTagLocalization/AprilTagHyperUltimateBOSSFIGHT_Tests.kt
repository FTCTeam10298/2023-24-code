package us.brainstormz.robotTwo

import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.localizer.aprilTagLocalization.AprilTagLocalizationOTron
import us.brainstormz.localizer.aprilTagLocalization.Foo
import us.brainstormz.motion.MecanumMovement

import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import java.lang.Thread.sleep


//gabe this class is your example
@Autonomous
class AprilTagBOSSFIGHT_StandardTest: LinearOpMode() {
    val hardware = RobotTwoHardware(opmode= this, telemetry= telemetry)
    lateinit var localizer: RRTwoWheelLocalizer
    val robotCameraYOffset = RobotTwoHardware.robotLengthInches/2
    var aprilTagLocalization = AprilTagLocalizationOTron(
            cameraXOffset=robotCameraYOffset,
            cameraYOffset=0.00 //it's right on center! Yay!
    )

    private val aprilTagThings = listOf(
//            Size(2304, 1536)
            Foo("Webcam 1", Size(1920, 1080)),
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

        hardware.init(hardwareMap)
        localizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        localizer.setPositionAndRotation(PositionAndRotation(0.0, 0.0, 0.0))


        waitForStart()

        // Push telemetry to the Driver Station.
//            // Save CPU resources; can resume streaming when needed.
//            if (gamepad1.dpad_down) {
////                    visionPortal!!.stopStreaming()
//            } else if (gamepad1.dpad_up) {
//            }

//        telemetry.addData("DS preview on/off", "3 dots, Camera Stream")
//        telemetry.addData(">", "Touch Play to start OpMode")
//        sleep(2000)
        while(opModeIsActive()) {
            val perpPos = hardware.perpendicularEncoder.getPositionAndVelocity().position
            val parPos = hardware.parallelEncoder.getPositionAndVelocity().position
            val imuYawDegrees = hardware.imu.robotYawPitchRollAngles.getYaw(AngleUnit.DEGREES)
//            telemetry.addLine("perpPos: $perpPos")
//            telemetry.addLine("parPos: $parPos")
//            telemetry.addLine("imuYawDegrees: $imuYawDegrees")

            localizer.recalculatePositionAndRotation()
            val currentPosition = localizer.currentPositionAndRotation()
            telemetry.addLine("current position: $currentPosition")
//            telemetry.addLine("Road runner internal current position: ${localizer.pose}")



            telemetryAprilTag(currentPosition)

            aprilTagThings.forEach { it.resumeStreaming() }


            // Share the CPU.
            sleep(20)
            telemetry.update()

        }
    }

    var isThisTheFirstTimeAnyAprilTagHasBeenSeen = true
    private fun telemetryAprilTag(roadRunnerPosition: PositionAndRotation) {

        val currentDetections: List<AprilTagDetection> = aprilTagThings.flatMap{it.detections()}
        telemetry.addData("# AprilTags Detected", currentDetections.size)

        //Step through the list of detections and find the tag with the least x value,
        //meaning least distance from center of camera, meaning *most accurate* source of
        //data.

//        var indexOfCurrentAprilTag = 0
//        var bestAprilTag = currentDetections[0]
//
//        for (detection in currentDetections) {
//            indexOfCurrentAprilTag++
//            val currentAprilTag = currentDetections[indexOfCurrentAprilTag]
//
//            when {
//                abs(currentAprilTag.ftcPose.x) <= abs(bestAprilTag.ftcPose.x) -> bestAprilTag = currentAprilTag
//                else -> break //do nothing—the best AprilTag reigns supreme.
//            }
//
//        }
//        //return(bestAprilTag) - when this gets functionified
//        val relevantAprilTagId = bestAprilTag.id
//        telemetry.addLine("Apriltag With Least Distortion: $relevantAprilTagId!")

        // Step through the list of detections and display info for each one.
        for (detection in currentDetections) {
            if (currentDetections.isNotEmpty()) {

                telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name))

                val theTag = detection
                val whatTag = theTag.id

                val tagBadness = theTag.hamming //not necessary... yaw = distortion, typically
                //find units of cam relative to tag and tag relative to field
                val currentPositionOfRobot = aprilTagLocalization.getCameraPositionOnField(theTag).posAndRot
                if (isThisTheFirstTimeAnyAprilTagHasBeenSeen) {
                    isThisTheFirstTimeAnyAprilTagHasBeenSeen = false

                    localizer.setPositionAndRotation(currentPositionOfRobot)
                }

                val orientation = currentPositionOfRobot.r
                val tagPosition = aprilTagLocalization.getAprilTagLocation(whatTag)
                val differenceBetweenAprilTagAndOdom = PositionAndRotation(
                        x=roadRunnerPosition.x - currentPositionOfRobot.x,
                        y=roadRunnerPosition.y - currentPositionOfRobot.y,
                        r=roadRunnerPosition.r - currentPositionOfRobot.r
                )
//                telemetry.addLine("Sir, I found AprilTag ID 2.")
                telemetry.addLine("AprilTag Current Position Of Robot (tag ${detection.id}): $currentPositionOfRobot")
//                telemetry.addLine("BUT the tag position is: $tagPosition")
//                telemetry.addLine("Error Correction Bits Added (hamming): $tagBadness")
                telemetry.addLine("\n\nDiscrepancy between tag ${detection.id} and odom is: $differenceBetweenAprilTagAndOdom\n\n")
//                telemetry.addLine("Orientation Found: $orientation")



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
            } // ...

        }
        // Add "key" information to telemetry
//        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.")
//        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)")
//        telemetry.addLine("RBE = Range, Bearing & Elevation")
    } //...


}

//@Autonomous
//class AprilTagBOSSFIGHT_MovementTest: OpMode() {
//    //TODO
//    val hardware = RobotTwoHardware(opmode= this, telemetry= telemetry)
//    lateinit var localizer: RRTwoWheelLocalizer
//    lateinit var movement: MecanumMovement
//
////    val console = TelemetryConsole(telemetry)
////    val wizard = TelemetryWizard(console, null)
//
//
//    override fun init() {
////        wizard.newMenu("testType", "What test to run?", listOf("Drive motor", "Movement directions", "Full odom movement"), firstMenu = true)
////        wizard.summonWizardBlocking(gamepad1)
//
//        hardware.init(hardwareMap)
//        localizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
//        movement = MecanumMovement(hardware= hardware, localizer= localizer, telemetry= telemetry)
//    }
//
//    val positions = listOf<PositionAndRotation>(
//            PositionAndRotation(y= 10.0, x= 0.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 10.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 10.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 10.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
//    )
//    var currentTarget: PositionAndRotation = positions.first()
//    var currentTargetStartTimeMilis: Long = 0
//    data class PositionDataPoint(val target: PositionAndRotation, val timeToSuccessMilis: Long, val finalPosition: PositionAndRotation)
//    val positionData = mutableListOf<PositionDataPoint>()
//    override fun loop() {
////        localizer.recalculatePositionAndRotation()
//
//        val currentPosition = localizer.currentPositionAndRotation()
//        telemetry.addLine("rr current position: $currentPosition")
//
////        val driveMotors = mapOf<String, DcMotor>(
////                "left front" to hardware.lFDrive,
////                "right front" to hardware.rFDrive,
////                "left back" to hardware.lBDrive,
////                "right back" to hardware.rBDrive
////        )
////        driveMotors.forEach{ (name, motor) ->
////            telemetry.addLine("name: $name")
////            motor.power = 0.5
////            sleep(2000)
////            motor.power = 0.0
////        }
////
////        movement.setSpeedAll(vY= 0.0, vX = 0.5, vA = 0.0, minPower = -1.0, maxPower = 1.0)
//
//
//        telemetry.addLine("currentTarget: $currentTarget")
//        val isAtTarget = movement.moveTowardTarget(currentTarget)
//
//        if (isAtTarget) {
//            val index = positions.indexOf(currentTarget)
//            if (index != (positions.size - 1)) {
//
//                val timeToComplete = System.currentTimeMillis() - currentTargetStartTimeMilis
//                positionData.add(PositionDataPoint(currentTarget, timeToComplete, currentPosition))
//
//                val distanceInches = 20
//                currentTarget = PositionAndRotation(Math.random() * distanceInches, Math.random() * distanceInches)//,(Math.random() * 360*2)-360)//positions[positions.indexOf(currentTarget) + 1]
//                currentTargetStartTimeMilis = System.currentTimeMillis()
//            }
//            sleep(500)
//        }
//
//        telemetry.addLine("\n\npositionData: \n$positionData")
//
//        telemetry.update()
//    }
//    companion object {
//        private const val USE_WEBCAM = true // true for webcam, false for phone camera
//    }
//}