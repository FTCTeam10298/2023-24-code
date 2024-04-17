import android.util.Size
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import us.brainstormz.localizer.MutablePointInXInchesAndYInches
import us.brainstormz.localizer.PointInXInchesAndYInches
import us.brainstormz.localizer.aprilTagLocalization.AprilTagFieldConfigurations
import us.brainstormz.localizer.aprilTagLocalization.AprilTagLocalizationFunctions
import us.brainstormz.localizer.aprilTagLocalization.AprilTagPipelineForEachCamera
import us.brainstormz.localizer.aprilTagLocalization.FourPoints
import us.brainstormz.localizer.aprilTagLocalization.ReusableAprilTagFieldLocalizer
import us.brainstormz.localizer.aprilTagLocalization.calculateAprilTagOffsets
import us.brainstormz.localizer.aprilTagLocalization.findErrorOfFourPoints

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

//fun main () {
//
//    val firstPoint = PointInXInchesAndYInches(
//            xInches = 47.7,
//            yInches = -47.8
//    )
//
//    val secondPoint = PointInXInchesAndYInches(
//            xInches = 23.4,
//            yInches = -44.2
//    )
//
//    val thirdPoint = PointInXInchesAndYInches(
//            xInches = 47.1,
//            yInches = -20.1
//    )
//
//    val fourthPoint = PointInXInchesAndYInches(
//            xInches = 23.6,
//            yInches = -20.0
//    )
//
//
//
//    val fourPointsPredictedMeasurement = FourPoints(
//            first = firstPoint,
//            second = secondPoint,
//            third = thirdPoint,
//            fourth = fourthPoint
//    )
//
//
//    val deltas = findErrorOfFourPoints(
//            allianceSide = ReusableAprilTagFieldLocalizer.AllianceSide.Blue,
//            fourPointsPredictedMeasurement = fourPointsPredictedMeasurement
//    )
//
//
//    for (i in range(0, 4)){
//
//        val eachDelta: PointInXInchesAndYInches? = when(i) {
//            1 -> deltas.first
//            2 -> deltas.second
//            3 -> deltas.third
//            4 -> deltas.fourth
//            else -> null
//
//        }
//
//
//        val xInches = eachDelta?.xInches
//        val yInches = eachDelta?.yInches
//
//        if (xInches != null) {
//            println("X for point {$eachDelta}: $xInches")
//            println("X for point {$eachDelta}: $xInches")
//        }
//        else {
//            println("I didn't get anything :(")
//        }
//
//
//        println()
//
//    }



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

//}





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

//for calibration, set this to
val currentFieldConfiguration = AprilTagFieldConfigurations.garageFieldAtHome

@TeleOp(name = "AprilTag Calibration System", group = "AprilTag")
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



    val localizer = ReusableAprilTagFieldLocalizer(
            aprilTagLocalization = aprilTagLocalization,
            averageErrorRedSide = currentFieldConfiguration.RedAllianceOffsets,
            averageErrorBlueSide =  currentFieldConfiguration.BlueAllianceOffsets,
            telemetry = telemetry
    )

    var firstPointCalculatedPosition = MutablePointInXInchesAndYInches(
            xInches = 0.0,
            yInches = 0.0
    )

    var secondPointCalculatedPosition = MutablePointInXInchesAndYInches(
            xInches = 0.0,
            yInches = 0.0
    )

    var thirdPointCalculatedPosition = MutablePointInXInchesAndYInches(
            xInches = 0.0,
            yInches = 0.0
    )

    var fourthPointCalculatedPosition = MutablePointInXInchesAndYInches(
            xInches = 0.0,
            yInches = 0.0
    )

    var whichWorldsFieldAreWeOn: String = "Left"
    var allianceSideOfBoard: String = "Red"

    enum class MenuLevel {
        GetData,
        ShowOffsets
    }

    var currentMenuLevel = MenuLevel.ShowOffsets


    lateinit var aprilTagDeltasOld: FourPoints
    lateinit var aprilTagDeltasNew: FourPoints
    lateinit var aprilTagOffsets: PointInXInchesAndYInches




    private val aprilTagThings = listOf(
//            Size(2304, 1536)
            //default is "Webcam 1", for 2023-24 it's...
            AprilTagPipelineForEachCamera("Webcam 1", Size(640, 480)),
//            Foo("Webcam 2", Size(320, 240)),
//            Foo("Webcam 3", Size(320, 240)),
//          Foo("Webcam 4", Size(320, 240)) - Not working. Each bus seems to support 2 cameras.
            // Idea: Half res for all other cameras, then add the other on its lowest res (320 by 240)...
    )


    data class TrackedControllerState (
        val rightBumperPressed:Boolean,
        val leftBumperPressed:Boolean,
            val dPadLeftPressed: Boolean
    )
    var previousControllerState:TrackedControllerState? = null

    override fun runOpMode() {

        aprilTagThings.forEach {
            telemetry.addLine("starting cameras")
            it.init(null, hardwareMap)

        }

//        hardware.init(hardwareMap)

        telemetry.update()

//        var previousAState = RetainedState(aPressed = gamepad1.a)

//        var rightSecondaryPressed = false
        val leftSecondaryPressed = false

        val firstPointButtonPressed = false
        val secondPointButtonPressed = false
        val fourthPointButtonPressed = false
        val thirdPointButtonPressed = false
        val dPadLeftPressed = false

        waitForStart()

        while(opModeIsActive()) {
//            val state = RetainedState(aPressed = gamepad1.a)

            val currentControllerState = TrackedControllerState(
                    rightBumperPressed = gamepad1.right_bumper,
                    leftBumperPressed = gamepad1.left_bumper,
                    dPadLeftPressed = gamepad1.dpad_left
            )

            aprilTagThings.forEach { it.resumeStreaming() }

            val currentDetections = getListOfCurrentAprilTagsSeen()

            previousControllerState = currentControllerState

             listenForWindowChange(previousControllerState)

            when (currentMenuLevel) {
                MenuLevel.GetData ->
                 captureBackboardData (
                    previousControllerState,
                    currentControllerState,
                    firstPointButtonPressed,
                    currentDetections,
                    secondPointButtonPressed,
                    thirdPointButtonPressed,
                    fourthPointButtonPressed,
                    leftSecondaryPressed,
                 )

                MenuLevel.ShowOffsets -> findOffsetsAndShowResultingReductionOfError()
//                    findOffsetsAndShowResultingReductionOfError(null!!)
            }

            showData(currentMenuLevel)
            telemetry.update()

//            if (aWasPressed) {
////                previousAState = state
////                println("button got hit!")
////
//            }

//            showAllAprilTagsInfo(currentDetections)

//            // Share the CPU.
//            sleep(20)


            previousControllerState = currentControllerState
        }
        if(isStopRequested) {
            aprilTagThings.forEach { it.close() }
            sleep(1000)

            //TODO: Full stack check. Delete calibration, find new calibration, implement it, test it.

            //REBOOT THE BOT IF THE CAMERA BLUESCREENS and is on Hardware Config.
            //Also, we can just leave this code running to get data.
        }

    }

    private fun listenForWindowChange(previousControllerState: TrackedControllerState?) {

        val currentControllerStateWindowChange = TrackedControllerState(
                rightBumperPressed = gamepad1.right_bumper,
                leftBumperPressed = gamepad1.left_bumper,
                dPadLeftPressed = gamepad1.dpad_left
        )

        if ((gamepad1.dpad_left) && !(currentControllerStateWindowChange.rightBumperPressed  && (previousControllerState==null || previousControllerState.rightBumperPressed ==false))) {
            currentMenuLevel = when (currentMenuLevel) {
                MenuLevel.GetData -> MenuLevel.ShowOffsets
                MenuLevel.ShowOffsets -> MenuLevel.GetData
            }
        }
    }

    private fun captureBackboardData(previousControllerState:TrackedControllerState?, currentControllerState:TrackedControllerState, firstPointButtonPressed: Boolean, currentDetections: List<AprilTagDetection>, secondPointButtonPressed: Boolean, thirdPointButtonPressed: Boolean, fourthPointButtonPressed: Boolean, leftSecondaryPressed: Boolean) {
        var firstPointButtonPressed1 = firstPointButtonPressed
        var secondPointButtonPressed1 = secondPointButtonPressed
        var thirdPointButtonPressed1 = thirdPointButtonPressed
        var fourthPointButtonPressed1 = fourthPointButtonPressed
//        var rightSecondaryPressed1 = rightSecondaryPressed
        var leftSecondaryPressed1 = leftSecondaryPressed

        if ((gamepad1.triangle) && !firstPointButtonPressed1) {
            recalculateFirstPoint(currentDetections = currentDetections)

            firstPointButtonPressed1 = true
        }
        if (firstPointButtonPressed1 == true && !(gamepad1.triangle)) {
            firstPointButtonPressed1 = false
        }

        if ((gamepad1.circle) && !secondPointButtonPressed1) {
            recalculateSecondPoint(currentDetections = currentDetections)

            secondPointButtonPressed1 = true
        }
        if (secondPointButtonPressed1 == true && !(gamepad1.circle)) {
            secondPointButtonPressed1 = false
        }
        if ((gamepad1.cross) && !thirdPointButtonPressed1) {
            recalculateThirdPoint(currentDetections = currentDetections)

            thirdPointButtonPressed1 = true
        }
        if (thirdPointButtonPressed1 == true && !(gamepad1.cross)) {
            thirdPointButtonPressed1 = false
        }
        //gamepad1.cross controls triangle and cross values

        if ((gamepad1.square) && !fourthPointButtonPressed1) {
            recalculateFourthPoint(currentDetections = currentDetections)

            fourthPointButtonPressed1 = true
        }
        if (fourthPointButtonPressed1 == true && !(gamepad1.square)) {
            fourthPointButtonPressed1 = false
        }

        if (currentControllerState.rightBumperPressed  && (previousControllerState==null || previousControllerState.rightBumperPressed ==false) ) {
            toggleFieldSide()
            zeroAllValues()
        }

        if (gamepad1.left_bumper && (previousControllerState==null || previousControllerState.leftBumperPressed ==false))  {
            toggleBackBoardAlliance()
            zeroAllValues()
        }

    }

    private fun findOffsetsAndShowResultingReductionOfError() {
        val firstFoundPoint = firstPointCalculatedPosition
        val secondFoundPoint = secondPointCalculatedPosition
        val thirdFoundPoint = thirdPointCalculatedPosition
        val fourthFoundPoint = fourthPointCalculatedPosition

        val measuredFourPoints = FourPoints(
                first =  PointInXInchesAndYInches(
                        xInches = firstFoundPoint.xInches,
                        yInches = firstFoundPoint.yInches
                ),
                second =  PointInXInchesAndYInches(
                        xInches = secondFoundPoint.xInches,
                        yInches = secondFoundPoint.yInches
                ),
                third =  PointInXInchesAndYInches(
                        xInches = thirdFoundPoint.xInches,
                        yInches = thirdFoundPoint.yInches
                ),
                fourth =  PointInXInchesAndYInches(
                        xInches = fourthFoundPoint.xInches,
                        yInches = fourthFoundPoint.yInches
                ),
        )

        val enteredAllianceSide = when(allianceSideOfBoard) {
            "Red" -> ReusableAprilTagFieldLocalizer.AllianceSide.Red
            "Blue" -> ReusableAprilTagFieldLocalizer.AllianceSide.Blue
            else -> ReusableAprilTagFieldLocalizer.AllianceSide.Red
        }

        //TODO: Change this so that we can a/b our previous config with our new one. (I'm thinking load it from the card).
        aprilTagDeltasOld = findErrorOfFourPoints(enteredAllianceSide, measuredFourPoints)
        aprilTagDeltasNew = aprilTagDeltasOld

        aprilTagOffsets = calculateAprilTagOffsets(aprilTagDeltasOld)

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
        return try {
            aprilTagThings.flatMap { it.detections() }
        }
        catch (error: Throwable) {
            emptyList()
        }
    }




    private fun recalculateFirstPoint(currentDetections: List<AprilTagDetection>) {

        println("The button-specific function ran")

        if (currentDetections.isNotEmpty()) {

            val closestAprilTag: AprilTagDetection = aprilTagLocalization.findClosestAprilTagToBot(currentDetections)

//            showTargetAprilTagInfo(
//                    listOfAllAprilTagsDetected = currentDetections,
//                    leastDistortedAprilTag = closestAprilTag)

            for (detection in currentDetections) {

                println("The list of tags wasn't empty...")


                if (detection.id == closestAprilTag.id && detection.ftcPose != null) {
//                    val allDetectionData: ReusableAprilTagFieldLocalizer.AprilTagAndData = localizer.returnAprilTagInFieldCentricCoords(detection)
//
                    val detectionFieldCoords = localizer.getFieldPositionsForTag(detection)
//                    val detectionAllianceSide = allDetectionData?.AllianceSide
//                    val detectionAccuracy = allDetectionData?.valueHasOneInchAccuracy
//                    val detectionTagCoords = localizer.returnAprilTagInFieldCentricCoords(detection).TagRelativePointInSpace

                     firstPointCalculatedPosition.xInches = detectionFieldCoords.xInches
                     firstPointCalculatedPosition.yInches = detectionFieldCoords.yInches


                } else {
//                    telemetry.addLine("Nope, it didn't.")
//                    return
                }
            }
        }
    }

    private fun recalculateSecondPoint(currentDetections: List<AprilTagDetection>) {

        println("The button-specific function ran")

        if (currentDetections.isNotEmpty()) {

            val closestAprilTag: AprilTagDetection = aprilTagLocalization.findClosestAprilTagToBot(currentDetections)

//            showTargetAprilTagInfo(
//                    listOfAllAprilTagsDetected = currentDetections,
//                    leastDistortedAprilTag = closestAprilTag)

            for (detection in currentDetections) {

                println("The list of tags wasn't empty...")


                if (detection.id == closestAprilTag.id && detection.ftcPose != null) {

                    val allDetectionData: ReusableAprilTagFieldLocalizer.AprilTagAndData = localizer.returnAprilTagInFieldCentricCoords(detection)
//
                    val detectionFieldCoords = localizer.getFieldPositionsForTag(detection)
//                    val detectionAllianceSide = allDetectionData?.AllianceSide
//                    val detectionAccuracy = allDetectionData?.valueHasOneInchAccuracy
                    val detectionTagCoords = localizer.returnAprilTagInFieldCentricCoords(detection).TagRelativePointInSpace

                    secondPointCalculatedPosition.xInches = detectionFieldCoords!!.xInches
                    secondPointCalculatedPosition.yInches = detectionFieldCoords.yInches


                } else {
//                    telemetry.addLine("Nope, it didn't.")
//                    return
                }
            }
        }
    }

    private fun recalculateThirdPoint(currentDetections: List<AprilTagDetection>) {

        println("The button-specific function ran")

        if (currentDetections.isNotEmpty()) {

            val closestAprilTag: AprilTagDetection = aprilTagLocalization.findClosestAprilTagToBot(currentDetections)

//            showTargetAprilTagInfo(
//                    listOfAllAprilTagsDetected = currentDetections,
//                    leastDistortedAprilTag = closestAprilTag)

            for (detection in currentDetections) {

                println("The list of tags wasn't empty...")


                if (detection.id == closestAprilTag.id && detection.ftcPose != null) {

                    val allDetectionData: ReusableAprilTagFieldLocalizer.AprilTagAndData? = localizer.returnAprilTagInFieldCentricCoords(detection)
//
                    val detectionFieldCoords = localizer.getFieldPositionsForTag(detection)
//                    val detectionAllianceSide = allDetectionData?.AllianceSide
//                    val detectionAccuracy = allDetectionData?.valueHasOneInchAccuracy
                    val detectionTagCoords = localizer.returnAprilTagInFieldCentricCoords(detection)?.TagRelativePointInSpace

                    thirdPointCalculatedPosition.xInches = detectionFieldCoords?.xInches!!
                    thirdPointCalculatedPosition.yInches = detectionFieldCoords?.yInches!!


                } else {
//                    telemetry.addLine("Nope, it didn't.")
//                    return
                }
            }
        }
    }

    private fun recalculateFourthPoint(currentDetections: List<AprilTagDetection>) {

        println("The button-specific function ran")

        if (currentDetections.isNotEmpty()) {

            val closestAprilTag: AprilTagDetection = aprilTagLocalization.findClosestAprilTagToBot(currentDetections)

//            showTargetAprilTagInfo(
//                    listOfAllAprilTagsDetected = currentDetections,
//                    leastDistortedAprilTag = closestAprilTag)

            for (detection in currentDetections) {

                println("The list of tags wasn't empty...")


                if (detection.id == closestAprilTag.id && detection.ftcPose != null) {
                    val allDetectionData: ReusableAprilTagFieldLocalizer.AprilTagAndData = localizer.returnAprilTagInFieldCentricCoords(detection)
//
                    val detectionFieldCoords = localizer.getFieldPositionsForTag(detection)
//                    val detectionAllianceSide = allDetectionData?.AllianceSide
//                    val detectionAccuracy = allDetectionData?.valueHasOneInchAccuracy
                    val detectionTagCoords = localizer.returnAprilTagInFieldCentricCoords(detection).TagRelativePointInSpace

                    fourthPointCalculatedPosition.xInches = detectionFieldCoords!!.xInches
                    fourthPointCalculatedPosition.yInches = detectionFieldCoords.yInches


                } else {
//                    telemetry.addLine("Nope, it didn't.")
//                    return
                }
            }
        }
    }

    private fun toggleFieldSide() {
        whichWorldsFieldAreWeOn = when(whichWorldsFieldAreWeOn) {
            "Left" -> "Right"
            "Right" -> "Left"
            else -> "Left"
        }
    }

    private fun toggleBackBoardAlliance() {

        allianceSideOfBoard = when(allianceSideOfBoard) {
            "Red" -> "Blue"
            "Blue" -> "Red"
            else -> "Red"
        }
    }

    private fun zeroAllValues() {
        firstPointCalculatedPosition.xInches = 0.0
        firstPointCalculatedPosition.yInches = 0.0
        secondPointCalculatedPosition.xInches = 0.0
        secondPointCalculatedPosition.yInches = 0.0
        thirdPointCalculatedPosition.xInches = 0.0
        thirdPointCalculatedPosition.yInches = 0.0
        fourthPointCalculatedPosition.xInches = 0.0
        fourthPointCalculatedPosition.yInches = 0.0
    }






//                val allDetectionData: ReusableAprilTagFieldLocalizer.AprilTagAndData? = localizer.returnAprilTagInFieldCentricCoords(detection)
//
//                val detectionFieldCoords = localizer.getFieldPositionsForTag(detection)
//                val detectionAllianceSide = allDetectionData?.AllianceSide
//                val detectionAccuracy = allDetectionData?.valueHasOneInchAccuracy
//                val detectionTagCoords = localizer.returnAprilTagInFieldCentricCoords(detection)?.TagRelativePointInSpace
//
//                if (currentDetections.isNotEmpty()) {
//
//                    println("The tag we were following was actually real...")
//
//                    println(String.format("\n==== (ID %d) %s", detection.id, detection.metadata.name))
//                    //
//                    println(String.format("XYH %6.1f %6.1f %6.1f  (inch, inch, deg)",
//                            detectionFieldCoords?.xInches,
//                            detectionFieldCoords?.yInches,
//                            detectionFieldCoords?.headingDegrees))
//
//                    println(String.format("PRY %6.1f %6.1f %6.1f  (deg)", detection.ftcPose.pitch, detection.ftcPose.roll, detection.ftcPose.yaw))
//
//                    println(String.format("RBE %6.1f %6.1f %6.1f  (inch, deg, deg)", detection.ftcPose.range, detection.ftcPose.bearing, detection.ftcPose.elevation))
//                    println(String.format("We're on alliance side {$detectionAllianceSide}, and our code says this measurement is accurate: {$detectionAccuracy}"))
//
//                    println("...and we even got right up to the logic.")
//
//                    if (detection.id == closestAprilTag.id) {
//                        println("We found and maybe logged the correct tag!")
//                        val xToLog = detectionFieldCoords?.xInches
//                        val yToLog = detectionFieldCoords?.yInches
//
//                        telemetry.addLine("X: {$xToLog}, Y: {$yToLog}")
//
//                        telemetry.update()

//                    } else {
//                        println("we found nothing.")
//                    }


//                }
//            }
//
//
//        }
//        else {
//            println("there weren't any detections, sadge")
//        }
//
//
//    }

    fun showData(currentMenuLevel: MenuLevel) {


        if (currentMenuLevel == MenuLevel.GetData) {
            val first = firstPointCalculatedPosition
            val second = secondPointCalculatedPosition
            val third = thirdPointCalculatedPosition
            val fourth = fourthPointCalculatedPosition

            telemetry.addLine("Field: $whichWorldsFieldAreWeOn - [RB] to change")
            telemetry.addLine("Backboard Alliance: $allianceSideOfBoard - [LB] to change")

            telemetry.addLine("\nFirst point -     ⃤  to change")
            telemetry.addLine(String.format("XY %6.2f %6.2f (inch, inch)",
                    first.xInches,
                    first.yInches))

            telemetry.addLine("\n\n Second point -    ⃝     to change")
            telemetry.addLine(String.format("XY %6.2f %6.2f (inch, inch)",
                    second.xInches,
                    second.yInches))

            telemetry.addLine("\n\n Third point - ╳  to change")
            telemetry.addLine(String.format("XY %6.2f %6.2f (inch, inch)",
                    third.xInches,
                    third.yInches))

            telemetry.addLine("\n\n Fourth point -     ⃞      to change")
            telemetry.addLine(String.format("XY %6.2f %6.2f (inch, inch)",
                    fourth.xInches,
                    fourth.yInches))

            telemetry.addLine("\n [DPAD LEFT] to preview offsets")


        }
        else if (currentMenuLevel == MenuLevel.GetData) {

            val oldDeltas = aprilTagDeltasOld
            val newDeltas = aprilTagDeltasNew

            telemetry.addLine("Current || Previous Errors on board $whichWorldsFieldAreWeOn, alliance $allianceSideOfBoard")

            telemetry.addLine(String.format("\n\nFirst: %6.2f, %6.2f || %6.2f, %6.2f",
                    newDeltas.first.xInches,
                    newDeltas.first.yInches,
                    oldDeltas.first.xInches,
                    oldDeltas.first.yInches))

            telemetry.addLine(String.format("\n\nSecond: %6.2f, %6.2f || %6.2f, %6.2f",
                    newDeltas.second.xInches,
                    newDeltas.second.yInches,
                    oldDeltas.second.xInches,
                    oldDeltas.second.yInches))

            telemetry.addLine(String.format("\n\nThird: %6.2f, %6.2f || %6.2f, %6.2f",
                    newDeltas.third.xInches,
                    newDeltas.third.yInches,
                    oldDeltas.third.xInches,
                    oldDeltas.third.yInches))

            telemetry.addLine(String.format("\n\nFourth: %6.2f, %6.2f || %6.2f, %6.2f",
                    newDeltas.fourth.xInches,
                    newDeltas.fourth.yInches,
                    oldDeltas.fourth.xInches,
                    oldDeltas.fourth.yInches))

            telemetry.addLine("\n\n [DPAD LEFT] to view the calculated offset - snap a photo!")

            telemetry.addLine("\n\n THE ACTUAL OFFSET: ")
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