////This code is brought to you by your friendly Aperture Science Community Outreach Associate, who would like to remind you that a bright future involves dark rooms and nuclear material.
//
//
////IN CASE OF FEDERAL INVESTIGATION:
////Pull switch to deploy FlameThrower turrets into Enrichment Sphere below Observation Chamber
////Overload nuclear reactor
////Burn Subject birth records
////Incinerate this poster
////Incinerate the incinerator
////Incinerate the incinerator incinerator
////As it burns, throw uniform in (all articles of clothing)
////Lie until further notice.
//
//
package us.brainstormz.localizer.polePosition.creamsicleGoalDetection

import com.acmerobotics.dashboard.config.Config
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.abs

data class JunctionDetection(
     val xPosition:Double,
     val whenDetected:Long
)

@Config
object CreamsicleConfig {
    @JvmField var displayMode: CreamsicleGoalDetector.Mode = CreamsicleGoalDetector.Mode.FRAME
    @JvmField var targetHue = CreamsicleGoalDetector.TargetHue.BLUE
}

class CreamsicleGoalDetector(private val console: TelemetryConsole){

    enum class Mode {
        FRAME,
        MASK,
        KERNEL
    }

    // THIS STUFF DO BE A DETECTOR. PICTURE AN 'APPLE IN A SUNNY ROOM' (thought experiment © 1998 Aperture Science Innovators)
    //RED will detect that apple if it's lit by a halogen bulb (panel, only $200,000,000 per!)
    //BLUE will detect an apple that has been modified with mantis DNA to change its hue.
    //solRED will do the same Outside
    //solBLUE will find the alternate apple, and
    //absRED or absBLUE will do the same but badly inside or out!

    enum class TargetHue {
        RED,
        BLUE,
        absRED
    }



    private val font = Imgproc.FONT_HERSHEY_COMPLEX



    class NamedVar(val name: String, var value: Double)

    /*
    # values for Cam Calibrated Goal detection DURING THE DAY: L-H = 95, L-S = 105, L-V = 000, U-H = 111, U-S = 255, U-V = 255
    # values for Cam Calibrated Goal detection DURING THE NIGHT: L-H = 0, L-S = 65, L-V = 70, U-H = 105, U-S = 255, U-V = 255
    # contours,  _ = cv2.findContours(mask, cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)
    */

        //DANGEROUS STUFFS BELOW! These define what color to filter through (and the layer chucks the rest. Use the AutoAimTestAndCal method to find the proper values using a SNAZZY, SNAZZY gui.
        //Values for Detecting Red

    class ColorRange(val L_H: NamedVar, val L_S: NamedVar, val L_V: NamedVar, val U_H: NamedVar, val U_S: NamedVar, val U_V: NamedVar)

    //Trained for Sunlight- useful for Wall-E but not Eve. SOMEDAY I'LL ADD A SWITCHER BTW INDOOR/OUTDOOR MODES BUT NO I DON't wANNA
    private val redSolarizedColor = ColorRange(
            L_H = NamedVar("Low Hue", 120.0),
            L_S = NamedVar("Low Saturation", 150.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 0.0),
            U_H = NamedVar("Uppper Hue", 130.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 200.0)
    )

    private val redAbsoluteColor = ColorRange(
            L_H = NamedVar("Low Hue", 120.0),
            L_S = NamedVar("Low Saturation", 150.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 0.0),
            U_H = NamedVar("Uppper Hue", 255.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 255.0)
    )

    // trained for rooms so you can prove AAY! IM A G$$D PERS$N! (blame it on Odd1sOut)

    val redColor = ColorRange(
            L_H = NamedVar("Low Hue", 0.0),
            L_S = NamedVar("Low Saturation", 0.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 0.0),
            U_H = NamedVar("Uppper Hue", 255.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 255.0))

    //trained for yellow
    val blueColor = ColorRange(
            L_H = NamedVar("Low Hue", 90.0),
            L_S = NamedVar("Low Saturation", 15.0),
            L_V = NamedVar("Low Vanity/Variance/VolumentricVibacity", 85.0),
            U_H = NamedVar("Upper Hue", 255.0),
            U_S = NamedVar("Upper Saturation", 255.0),
            U_V = NamedVar("Upper Vanity/Variance/VolumentricVibracity", 255.0))

    val goalColor: ColorRange = when(CreamsicleConfig.targetHue){
        TargetHue.RED -> redColor
        TargetHue.BLUE -> blueColor
        TargetHue.absRED -> redAbsoluteColor
    }

    //Declares X and Y of the target's... well, something... that other code can use and request.
    var x = 0.0
    var y = 0.0

    private val hsv = Mat()
    private val maskA = Mat()
    private val maskB = Mat()
    private val kernel = Mat(5, 5, CvType.CV_8U)

    private var mostRecentDetection:JunctionDetection? = null

    fun detectedPosition(staleThresholdAgeMillis:Long):Double? {
        return mostRecentDetection?.let{detection ->
            val t = System.currentTimeMillis() - staleThresholdAgeMillis
            if(detection.whenDetected >= t) detection.xPosition else null
        }
    }

    fun convert(matOfPoint2f: MatOfPoint2f): MatOfPoint {
        val foo = MatOfPoint()
        matOfPoint2f.convertTo(foo, CvType.CV_32S)
        return foo
    }

    private fun heightOfLineOnScreen(line:Pair<Point, Point>):Double {
        return line.second.y - line.first.y

    }

    fun scoopFrame(frame: Mat): Mat {

        Imgproc.cvtColor(frame, hsv, Imgproc.COLOR_BGR2HSV)

        val lower = Scalar(blueColor.L_H.value, blueColor.L_S.value, blueColor.L_V.value)
        val upper = Scalar(blueColor.U_H.value, blueColor.U_S.value, blueColor.U_V.value)
//        val lower = Scalar(0.0, 0.0, 0.0)
//        val upper = Scalar(225.0, 225.0, 225.0)

        Core.inRange(hsv, lower, upper, maskA)

        Imgproc.erode(maskA, maskB, kernel)

        val contours = mutableListOf<MatOfPoint>()
        Imgproc.findContours(maskB, contours, Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE)

        //find max
        //filter edges

        //  for cnt in contours:
        val poles = contours.mapNotNull{ cnt ->

            val area = Imgproc.contourArea(cnt)

            val points = MatOfPoint2f()


            fun convert(src: MatOfPoint): MatOfPoint2f {
                val dst = MatOfPoint2f()
                src.convertTo(dst, CvType.CV_32F)
                return dst
            }

            val cnt2f = convert(cnt)
            Imgproc.approxPolyDP(cnt2f, points, 0.02 * Imgproc.arcLength(cnt2f, true), true)


            //        x = approx.ravel() [0]
            //        y = approx.ravel() [1]
            val point = points.toList()[0]
            /*
                if area > 400:
                */
            if (area > 400) {
                val pointsArray = points.toArray()

                val a = (pointsArray + listOf(pointsArray.first()))
                val b = listOf(pointsArray.last()) + pointsArray
                val pairsOfSequentialPoints = a.zip(b)

                val verticalSegments = pairsOfSequentialPoints.filter{(a, b) ->
                    areRoughlyInSameXPlane(a, b)
                }

                val sufficientSegments:List<Pair<Point, Point>> = verticalSegments.filter{(a, b) ->
                    val fudge = 78
                    (b.y - a.y >= fudge)
                }
                val notEdgeOfScreen = 30.0..300.0
                val contoursWithinBounds = sufficientSegments.filter {
                    (it.first.x in notEdgeOfScreen) && (it.second.x in notEdgeOfScreen)
                }

                val tallestLine = contoursWithinBounds.maxByOrNull(::heightOfLineOnScreen)


                tallestLine
//                if(tallestLine!=null){
//                    currentGoalXPosition = tallestLine.first.x
//
//                    val top:Point =  Point(tallestLine.first.x, 0.0)
//                    val bottom:Point = Point(tallestLine.first.x, 220.0)
//
//                    Imgproc.drawContours(
//                            frame,
//                            mutableListOf(convert(MatOfPoint2f(top, bottom))),
//                            0,
//                            Scalar(0.0, 255.0, 0.0),
//                            2)
//                    true
//                } else
//                    false
            } else
                null
        }

        val highestPole = poles.maxByOrNull(::heightOfLineOnScreen)
        if (highestPole != null) {
            mostRecentDetection = JunctionDetection(
                xPosition = highestPole.first.x,
                whenDetected = System.currentTimeMillis()
            )

            val top:Point =  highestPole.first //Point(highestPole.first.x, 0.0)
            val bottom:Point = highestPole.second //Point(highestPole.first.x, 220.0)
//            val top:Point =  Point(30.0, 100.0)
//            val bottom:Point = Point(300.0, 100.0)

            Imgproc.drawContours(
                    frame,
                    mutableListOf(convert(MatOfPoint2f(top, bottom))),
                    0,
                    Scalar(0.0, 255.0, 0.0),
                    2)
        }

//        println("numberOfPolesFound: ${listOfPoles}")

        return when (CreamsicleConfig.displayMode) {
            Mode.FRAME -> frame
            Mode.KERNEL -> kernel
            Mode.MASK -> maskB
        }
    }

    private fun areRoughlyInSameXPlane(a: Point, b: Point): Boolean {
        val fudge = 42
        return abs(b.x - a.x) <= fudge
    }

}