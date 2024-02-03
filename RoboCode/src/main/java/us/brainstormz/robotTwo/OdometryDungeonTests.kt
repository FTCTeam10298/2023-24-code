//package us.brainstormz.robotTwo
//
////this is a curse to humanity. In other words: VITRIFICATION ORDER
//
//import com.qualcomm.robotcore.eventloop.opmode.Autonomous
//import com.qualcomm.robotcore.eventloop.opmode.OpMode
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
//import us.brainstormz.localizer.PositionAndRotation
//import us.brainstormz.localizer.RRTwoWheelLocalizer
//import us.brainstormz.motion.MecanumMovement
//import java.lang.Thread.sleep
//
///* Copyright (c) 2023 FIRST. All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without modification,
// * are permitted (subject to the limitations in the disclaimer below) provided that
// * the following conditions are met:
// *
// * Redistributions of source code must retain the above copyright notice, this list
// * of conditions and the following disclaimer.
// *
// * Redistributions in binary form must reproduce the above copyright notice, this
// * list of conditions and the following disclaimer in the documentation and/or
// * other materials provided with the distribution.
// *
// * Neither the name of FIRST nor the names of its contributors may be used to endorse or
// * promote products derived from this software without specific prior written permission.
// *
// * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
// * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
// * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
// * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
// * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//
//import android.util.Size
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp
//import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
//import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase
//import us.brainstormz.localizer.aprilTagLocalization.Foo
//import kotlin.math.abs
//import kotlin.math.atan
//
//
//
////gabe this class is your example
//@TeleOp
//class TortureAPRILtag: OpMode() {
//    val hardware = RobotTwoHardware(opmode= this, telemetry= telemetry)
//    lateinit var localizer: RRTwoWheelLocalizer
//
//    //This is 4 cameras at lowest possible res. (maybe just return b/w? IDK how, but sounds good. Also, there's bitcrushing.)
//    private val aprilTagThings = listOf(
////            Size(2304, 1536)
//            Foo("Webcam 1", Size(1920, 1080)),
////            Foo("Webcam 2", Size(320, 240)),
////            Foo("Webcam 3", Size(320, 240)),
////          Foo("Webcam 4", Size(320, 240)) - Not working. Each bus seems to support 2 cameras.
//            // Idea: Half res for all other cameras, then add the other on its lowest res (320 by 240)...
//    )
//
//    override fun init() {
//        hardware.init(hardwareMap)
//        localizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
//        localizer.setPositionAndRotation(PositionAndRotation(0.0, 0.0, 0.0))
//
//        aprilTagThings.forEach {
//            it.init(null, hardwareMap)
//        }
////        }else{
////            val viewContainerIds = VisionPortal.makeMultiPortalView(aprilTagThings.size, VisionPortal.MultiPortalLayout.VERTICAL).toList()
////            aprilTagThings.zip(viewContainerIds).forEach{ (foo, viewContainerId) -> foo.init(viewContainerId, hardwareMap)}
////        }
//
//        // Wait for the DS start button to be touched.
//        telemetry.addData("DS preview on/off", "3 dots, Camera Stream")
//        telemetry.addData(">", "Touch Play to start OpMode")
//        sleep(2000)
//        telemetry.update()
//    }
//    override fun loop() {
//        val perpPos = hardware.perpendicularEncoder.getPositionAndVelocity().position
//        val parPos = hardware.parallelEncoder.getPositionAndVelocity().position
//        val imuYawDegrees = hardware.imu.robotYawPitchRollAngles.getYaw(AngleUnit.DEGREES)
//        telemetry.addLine("perpPos: $perpPos")
//        telemetry.addLine("parPos: $parPos")
//        telemetry.addLine("imuYawDegrees: $imuYawDegrees")
//
//        localizer.recalculatePositionAndRotation()
//        val currentPosition = localizer.currentPositionAndRotation()
//        telemetry.addLine("current position: $currentPosition")
//        telemetry.addLine("Road runner internal current position: ${localizer.pose}")
//
//        telemetry.update()
//
//        telemetryAprilTag()
//
//    fun telemetryAprilTag() {
//
//        val currentDetections: List<AprilTagDetection> = aprilTagThings.flatMap{it.detections()}
//        telemetry.addData("# AprilTags Detected", currentDetections.size)
//
//        //Step through the list of detections and find the tag with the least x value,
//        //meaning least distance from center of camera, meaning *most accurate* source of
//        //data.
//
//        //        var indexOfCurrentAprilTag = 0
//        //        var bestAprilTag = currentDetections[0]
//        //
//        //        for (detection in currentDetections) {
//        //            indexOfCurrentAprilTag++
//        //            val currentAprilTag = currentDetections[indexOfCurrentAprilTag]
//        //
//        //            when {
//        //                abs(currentAprilTag.ftcPose.x) <= abs(bestAprilTag.ftcPose.x) -> bestAprilTag = currentAprilTag
//        //                else -> break //do nothing—the best AprilTag reigns supreme.
//        //            }
//        //
//        //        }
//        //        //return(bestAprilTag) - when this gets functionified
//        //        val relevantAprilTagId = bestAprilTag.id
//        //        telemetry.addLine("Apriltag With Least Distortion: $relevantAprilTagId!")
//
//        // Step through the list of detections and display info for each one.
//        for (detection in currentDetections) {
//            if (currentDetections.isNotEmpty()) {
//
//                val theTag = detection
//                val whatTag = theTag.id
//
//                val tagBadness = theTag.hamming //not necessary... yaw = distortion, typically
//                //find units of cam relative to tag and tag relative to field
//                val currentPositionOfRobot = getCameraPositionOnField(theTag)
//                val orientation = currentPositionOfRobot.posAndRot.r
//
//                val tagPosition = getAprilTagLocation(whatTag)
//                val odometryPosition = 0 //Gabe's jank odometry call placeholder
//                telemetry.addLine("Sir, I found AprilTag ID 2.")
//                telemetry.addLine("Current Position Of Robot: $currentPositionOfRobot")
//                telemetry.addLine("BUT the tag position is: $tagPosition")
//                telemetry.addLine("Error Correction Bits Added (hamming): $tagBadness")
//                telemetry.addLine("Odometry reports we're at: $odometryPosition")
//        //                telemetry.addLine("Orientation Found: $orientation")
//
//
//
//                telemetry.addLine(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name))
//                //
//                telemetry.addLine(String.format("XYZ %6.1f %6.1f %6.1f  (inch)", detection.ftcPose.x, detection.ftcPose.y, detection.ftcPose.z))
//
//                telemetry.addLine(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw))
//
//                telemetry.addLine(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation))
//                val muchData = getAprilTagLocation(detection.id)
//                telemetry.addLine("Random Madness!! $muchData")
//
//            }
//            // Add "key" information to telemetry
//            telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.")
//            telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)")
//            telemetry.addLine("RBE = Range, Bearing & Elevation")
//        } //...
//
//
////            } else {
////                telemetry.addLine(String.format("\n==== (ID %d) Unknown", detection.id))
////                telemetry.addLine(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y))
////            }
//        } // ...
//
//        // Push telemetry to the Driver Station.
//        telemetry.update()
//        // Save CPU resources; can resume streaming when needed.
//        if (gamepad1.dpad_down) {
////                    visionPortal!!.stopStreaming()
//        } else if (gamepad1.dpad_up) {
//            aprilTagThings.forEach { it.resumeStreaming() }
//        }
//
//
//        // Share the CPU.
//        sleep(20)
//
//
//    }
//}
//
//
//
//@Autonomous
//class AprilTagAndOdometryMovementTest: OpMode() {
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
////            telemetry.update()
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
//
//
//
//}