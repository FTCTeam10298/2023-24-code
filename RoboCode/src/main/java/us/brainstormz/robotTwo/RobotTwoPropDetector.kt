package us.brainstormz.robotTwo

import org.opencv.core.*
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.opencv.imgproc.Imgproc

class RobotTwoPropDetector(private val telemetry: Telemetry, private val colorToDetect: PropColors) {
    enum class PropPosition {
        Left, Center, Right
    }

    enum class PropColors {
        Red, Blue
    }

    private val positionsMappedToRects = listOf(
        PropPosition.Left to Rect(Point(0.0, 100.0), Point(50.0, 200.0)),
        PropPosition.Center to Rect(Point(120.0, 130.0), Point(200.0, 180.0)),
        PropPosition.Right to Rect(Point(270.0, 100.0), Point(320.0, 200.0))
    )

    private val blue = Scalar(0.0, 0.0, 255.0)
    private val green = Scalar(0.0, 255.0, 0.0)
    private val red = Scalar(255.0, 0.0, 0.0)
    private val white = Scalar(255.0, 255.0, 255.0)
    private val black = Scalar(0.0, 0.0, 0.0)
    private val colorToDetectAsScalar = mapOf(PropColors.Blue to blue, PropColors.Red to red).toMap()[colorToDetect]

    private enum class Colors(val scalar: Scalar) {
        MinBlue(Scalar(100.0, 100.0, 100.0)),
        MaxBlue(Scalar(115.0, 255.0,255.0)),
        MinLowRed(Scalar(0.0,100.0, 100.0)),
        MaxLowRed(Scalar(25.0,255.0,255.0)),
        MinHighRed(Scalar(160.0, 100.0,100.0)),
        MaxHighRed(Scalar(255.0,255.0,255.0)),
    }

    @Volatile
    var propPosition = PropPosition.Left

    private val mat = Mat()
    private val redLowMat = Mat()
    private val redHighMat = Mat()
    private var regions: Map<PropPosition, Mat> = emptyMap()

    private var firstFrameTimeMillis: Long? = null
    private var baselines: Map<RobotTwoPropDetector.PropPosition, Double>? = null

    fun setBaselines(){
        baselines = null
    }

    fun setBaselines(baselines: Map<RobotTwoPropDetector.PropPosition, Double>){
        println("Setting color baselines to $baselines")
        this.baselines = baselines
    }


    fun processFrame(frame: Mat): Mat {
        if (firstFrameTimeMillis == null) {
            firstFrameTimeMillis = System.currentTimeMillis()
        }

        Imgproc.cvtColor(frame, mat, Imgproc.COLOR_RGB2HSV)

        when (colorToDetect) {
            PropColors.Blue -> {
                Core.inRange(mat, Colors.MinBlue.scalar, Colors.MaxBlue.scalar, mat)
            }
            PropColors.Red -> {
                Core.inRange(mat, Colors.MinLowRed.scalar, Colors.MaxLowRed.scalar, redLowMat)
                Core.inRange(mat, Colors.MinHighRed.scalar, Colors.MaxHighRed.scalar, redHighMat)
                Core.bitwise_or(redLowMat, redHighMat, mat)
            }
        }
        regions = positionsMappedToRects.associate { (position, rect) ->
            position to mat.submat(rect)
        }

        val colorIntensities = regions.map { (position, rect) ->
            position to Core.sumElems(rect).`val`[0]
        }.toMap()
        println("colorIntensities: $colorIntensities")

        val now = System.currentTimeMillis()
        val timeSinceFirstFrameMillis = now - (firstFrameTimeMillis?:now)
        val timeToWaitAfterFirstFrameBeforeCalibratingMillis = 500
        if(timeSinceFirstFrameMillis >= timeToWaitAfterFirstFrameBeforeCalibratingMillis && baselines == null) {
            setBaselines(colorIntensities)
        }


//        2024-04-19 07:57:33.482  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.484  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.484  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.485  4214-4379  System.out              com.qualcomm.ftcrobotcontroller      I  colorIntensities: {Left=44625.0, Center=32895.0, Right=119595.0}
//        2024-04-19 07:57:33.485  4214-4379  System.out              com.qualcomm.ftcrobotcontroller      I  baselines: null
//        2024-04-19 07:57:33.485  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.486  4214-4379  System.out              com.qualcomm.ftcrobotcontroller      I  colorIntensityRelativeToBaseline: [(Left, 0.0), (Center, 0.0), (Right, 0.0)]
//        2024-04-19 07:57:33.486  4214-4379  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: color Intensity Left = 0.0
//        2024-04-19 07:57:33.486  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.486  4214-4379  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: color Intensity Center = 0.0
//        2024-04-19 07:57:33.486  4214-4379  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: color Intensity Right = 0.0
//        2024-04-19 07:57:33.486  4214-4379  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: %%Telemetry.Update%%
//        2024-04-19 07:57:33.487  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.487  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.489  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.489  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.491  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.491  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.493  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.493  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.494  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.495  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.496  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.497  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.498  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.498  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.500  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.500  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.501  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.502  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.503  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.503  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.505  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.505  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.507  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.507  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.508  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left
//        2024-04-19 07:57:33.509  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: wizardResults: InitLoopResults(wizardResults=WizardResults(alliance=Blue, startPosition=Backboard, partnerIsPlacingYellow=false, numberOfCycles=1, waitSeconds=0), cameraStuff=CameraStuff(propDetector=us.brainstormz.robotTwo.RobotTwoPropDetector@6d7028d, opencv=us.brainstormz.openCvAbstraction.OpenCvAbstraction@2613c42))
//        2024-04-19 07:57:33.510  4214-4373  System.out              com.qualcomm.ftcrobotcontroller      I  Telemetry: propPosition: Left

        regions.forEach {(position, rect) ->
            rect.release()
        }

        val colorIntensityRelativeToBaseline = colorIntensities.map { (position, intensity) ->
            position to (baselines?.get(position)?.let { baseline ->
                val relativeIntensity = intensity - baseline
                relativeIntensity
            } ?: 0.0)
        }
        println("baselines: $baselines")
        println("colorIntensityRelativeToBaseline: $colorIntensityRelativeToBaseline")

        propPosition = colorIntensityRelativeToBaseline.maxByOrNull { (position, value) ->
            telemetry.addLine("color Intensity $position = $value")
            value
        }?.first ?: propPosition

        positionsMappedToRects.forEach {(position, rect) ->
            val borderColor = if (position == propPosition) colorToDetectAsScalar else white
            Imgproc.rectangle(frame, rect, borderColor, 3)
        }

        val sizePerTinyBox = 10.0
        Colors.entries.forEachIndexed { i, it ->
            val tinyBox = Rect(Point(i*sizePerTinyBox, 0.0), Point((i*sizePerTinyBox)+sizePerTinyBox, 10.0))
            Imgproc.rectangle(frame, tinyBox, it.scalar, 2)
        }

        telemetry.update()

        return frame
    }
}