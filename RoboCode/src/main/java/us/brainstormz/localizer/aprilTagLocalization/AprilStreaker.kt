/* Copyright (c) 2023 FIRST. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted (subject to the limitations in the disclaimer below) provided that
 * the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or
 * other materials provided with the distribution.
 *
 * Neither the name of FIRST nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
 * LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package us.brainstormz.localizer.aprilTagLocalization

import CameraRelativePointInSpace
import TagRelativePointInSpace
import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.PositionAndRotation
import kotlin.math.cos
import kotlin.math.sin

//TODO: Test the April tag localization against odometery
//
//TODO: improve April tag localization until it’s as good as or better than odometery
/*
 * This OpMode illustrates the basics of AprilTag recognition and pose estimation,
 * including Java Builder structures for specifying Vision parameters.
 *
 * For an introduction to AprilTags, see the FTC-DOCS link below:
 * https://ftc-docs.firstinspires.org/en/latest/apriltag/vision_portal/apriltag_intro/apriltag-intro.html
 *
 * In this sample, any visible tag ID will be detected and displayed, but only tags that are included in the default
 * "TagLibrary" will have their position and orientation information displayed.  This default TagLibrary contains
 * the current Season's AprilTags and a small set of "test Tags" in the high number range.
 *
 * When an AprilTag in the TagLibrary is detected, the SDK provides location and orientation of the tag, relative to the camera.
 * This information is provided in the "ftcPose" member of the returned "detection", and is explained in the ftc-docs page linked below.
 * https://ftc-docs.firstinspires.org/apriltag-detection-values
 *
 * To experiment with using AprilTags to navigate, try out these two driving samples:
 * RobotAutoDriveToAprilTagOmni and RobotAutoDriveToAprilTagTank
 *
 * There are many "default" VisionPortal and AprilTag configuration parameters that may be overridden if desired.
 * These default parameters are shown as comments in the code below.
 *
 * Use Android Studio to Copy this Class, and Paste it into your team's code folder with a new name.
 * Remove or comment out the @Disabled line to add this OpMode to the Driver Station OpMode list.
 */

/* [640x480], [160x90], [160x120], [176x144], [320x180], [320x240], [352x288], [432x240], [640x360], [800x448],
[800x600], [864x480], [960x720], [1024x576], [1280x720], [1600x896], [1920x1080], [2560x1472],
 */
@TeleOp(name = "AprilStreaker-tags, fast", group = "Concept") //@Disabled
class AprilStreaker: LinearOpMode() {

    //y = distance from camera center to apriltag center
    //x = camera centric - distance between where the camera is pointing to the center of the apriltag

    var aprilTagLocalization = AprilTagLocalizationOTron(0.0, 0.0)

    //This is 4 cameras at lowest possible res. (maybe just return b/w? IDK how, but sounds good. Also, there's bitcrushing.)
    private val aprilTagThings = listOf(
//            Size(2304, 1536)
            Foo("Webcam 1", Size(1920, 1080)),
//            Foo("Webcam 2", Size(320, 240)),
//            Foo("Webcam 3", Size(320, 240)),
//          Foo("Webcam 4", Size(320, 240)) - Not working. Each bus seems to support 2 cameras.
            // Idea: Half res for all other cameras, then add the other on its lowest res (320 by 240)...
    )
    override fun runOpMode() {
//        if(aprilTagThings.size < 2){
        aprilTagThings.forEach {
            it.init(null, hardwareMap)
            }
//        }else{
//            val viewContainerIds = VisionPortal.makeMultiPortalView(aprilTagThings.size, VisionPortal.MultiPortalLayout.VERTICAL).toList()
//            aprilTagThings.zip(viewContainerIds).forEach{ (foo, viewContainerId) -> foo.init(viewContainerId, hardwareMap)}
//        }

        // Wait for the DS start button to be touched.
        telemetry.addData("DS preview on/off", "3 dots, Camera Stream")
        telemetry.addData(">", "Touch Play to start OpMode")
        sleep(2000)
        telemetry.update()
        if (true) {
            while (true) {
                telemetryAprilTag()

                // Push telemetry to the Driver Station.
                telemetry.update()
                // Save CPU resources; can resume streaming when needed.
                if (gamepad1.dpad_down) {
//                    visionPortal!!.stopStreaming()
                } else if (gamepad1.dpad_up) {
                    aprilTagThings.forEach { it.resumeStreaming() }
                }


                // Share the CPU.
                sleep(20)
            }
        }

        // Save more CPU resources when camera is no longer needed.
        aprilTagThings.forEach { it.close() }
    } // end method runOpMode()

    /**
     * Add telemetry about AprilTag detections.
     */

    data class aprilTagAndData(val anAprilTag: AprilTagDetection, val aPositionAndRotation: PositionAndRotation)

    /** Gabe edit me */
    private fun returnAprilTagInTagCentricCoords(aprilTag: AprilTagDetection): AprilStreaker.aprilTagAndData? {
        //go look in the FTC documentation, you absolutely need to understand the FTC AprilTag Coordinate System

        //you need to look at the diagram to get this.

        //getting l

        if (aprilTag != null) {
            val thisAprilTagPose = aprilTag.ftcPose
            val representationOfAprilTag = CameraRelativePointInSpace(xInches=thisAprilTagPose.x,
                    yInches=thisAprilTagPose.y, yawDegrees=thisAprilTagPose.yaw, rangeInches=thisAprilTagPose.range)
            val thisTagInTagCentricCoords = returnCamCentricCoordsInTagCentricCoords(representationOfAprilTag)
            val resultPosition = PositionAndRotation(thisTagInTagCentricCoords.xInches,
                    thisTagInTagCentricCoords.yInches, thisTagInTagCentricCoords.headingDegrees)

            return AprilStreaker.aprilTagAndData(aprilTag, resultPosition)
        }
        else return null
    }


    private fun returnCamCentricCoordsInTagCentricCoords(anyOldTag: CameraRelativePointInSpace): TagRelativePointInSpace {
        //go look in the FTC documentation, you absolutely need to understand the FTC AprilTag Coordinate System

        val yawRadians = Math.toRadians(anyOldTag.yawDegrees)

        val aSectionOfXInches = anyOldTag.xInches/ cos(yawRadians)
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


    private fun telemetryAprilTag() {

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

                val thisTagInTagCentricCoords = returnAprilTagInTagCentricCoords(detection)

                if (thisTagInTagCentricCoords == null) {
                    println("Nothing here, cap'n.")
                }
                else {
                    val detectionTagCentric = thisTagInTagCentricCoords!!.aPositionAndRotation


                    println(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name))
                    //
                    println(String.format("XYH %6.1f %6.1f %6.1f  (inch, inch, deg)", detectionTagCentric.x, detectionTagCentric.y, detectionTagCentric.r))

                    println(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw))

                    println(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation))
                }


//                val muchData = aprilTagLocalization.getAprilTagLocation(detection.id)
//                println("Random Madness!! $muchData")


//            } else {
//                println(String.format("\n==== (ID %d) Unknown", detection.id))
//                println(String.format("Center %6.0f %6.0f   (pixels)", detection.center.x, detection.center.y))
//            }
        } // ...

        }
        // Add "key" information to telemetry
        telemetry.addLine("All data printed to logcat to be speedier")
        telemetry.addLine("\nkey:\nXYZ = X (Right), Y (Forward), Z (Up) dist.")
        telemetry.addLine("PRY = Pitch, Roll & Yaw (XYZ Rotation)")
        telemetry.addLine("RBE = Range, Bearing & Elevation")
    } //...

    companion object {
        private const val USE_WEBCAM = true // true for webcam, false for phone camera
    }

    /**Returns the position of the camera when given an april tag detection*/

}


/**April tag location on the field, where the center of the field is (0, 0, 0) */
