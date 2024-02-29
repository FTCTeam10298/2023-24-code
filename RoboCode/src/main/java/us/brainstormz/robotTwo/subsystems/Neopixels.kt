package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons.AdafruitNeopixelSeesaw

class Neopixels: Subsystem {
    enum class LightTypes {
        RGBW,
        RGB //todo, we don't care
    }

    val strandLength: Int = 60
    //represents light current status
    var lightStrandTarget = mutableListOf<MutableList<Double>>()
    var mondrianStatusBarTarget = mutableListOf<mondrianStatusBarMessages>()

    fun prepareNeopixels() {
        //Create a list of pixel values we can change... the function to do it is not good right now.
        var prototypeLightStatus: MutableList<Double> = mutableListOf(0.0, 0.0, 0.0, 0.0) //R, G, B, W

        for (n in 1..strandLength) {
            lightStrandTarget += prototypeLightStatus
        }
        println("Initial Status String: $lightStrandTarget")
        }

    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        println("Not yet implemented")
    }

    data class ColorInRGBW(val red: Double, val green: Double, val blue: Double, val white: Double)

    fun changeOnePixel(targetColor: ColorInRGBW, targetPixel: Int) {
        lightStrandTarget[targetPixel] = mutableListOf(targetColor.red, targetColor.green, targetColor.blue, targetColor.white)
    }

    fun flood(targetColor: ColorInRGBW)
    {
        for (n in 1..strandLength) {
            lightStrandTarget[n] = mutableListOf(targetColor.red, targetColor.green, targetColor.blue, targetColor.white)
        }
        println("Result of flooding with $targetColor: \r $lightStrandTarget")

    }

    //I should make a data class

    fun showHalf(firstTargetColor: ColorInRGBW, secondTargetColor: ColorInRGBW) {

        val halfOfStrand: Int
        if (strandLength % 2 == 0) {
            halfOfStrand = (strandLength/2).toInt()
        }
        else {
            halfOfStrand =  (strandLength/2 - 0.5).toInt()
        }

        for (n in 1..halfOfStrand) {
            lightStrandTarget[n] = mutableListOf(firstTargetColor.red, firstTargetColor.green, firstTargetColor.blue, firstTargetColor.white)
        }

        for (n in halfOfStrand + 1..strandLength) {
            lightStrandTarget[n] = mutableListOf(firstTargetColor.red, firstTargetColor.green, firstTargetColor.blue, firstTargetColor.white)
        }

    }

    //where the status bar starts.
    val mondrianStatusBarStart: Int = 40
    val mondrianStatusBarEnd: Int = 60


    fun mondrianStatusBarAdd() {

    }

    enum class mondrianStatusBarMessages {
        BarLive,
        Problem
    }

    data class positionAndRGBWColor(val position: List<Int>, val targetColor: ColorInRGBW)

    fun updateMondrianStatusBar(){
        //black out status bar area

        //can't change color. --
        //iterate through and map each?
//        val targetStatusRegions = mapOf<mondrianStatusBarMessages, MutableList<Int>>() {
//            mondrianStatusBarMessages.BarLive to mutableListOf<Int>(0, 3)
//
//        }
        val problemStart = 42
        val problemEnd = 48

        for (n in mondrianStatusBarStart..mondrianStatusBarStart) {
            lightStrandTarget[n] = mutableListOf(0.0, 0.0, 0.0, 0.0)
        }

        for (status in mondrianStatusBarTarget) {
            if (status == mondrianStatusBarMessages.Problem) {
                for (n in mondrianStatusBarStart..mondrianStatusBarStart) {
                    lightStrandTarget[n] = kotlin.collections.mutableListOf(150.0, 0.0, 0.0, 0.0)
                }

            }
        }


    }

    //now for the fun stuff—animations!

    fun showOneByOne(targetColor: ColorInRGBW, startPixel: Int, endPixel: Int, frameNumber: Int) {
        //not very necessary. Maybe someday?
        if (frameNumber > endPixel - startPixel) {

                }
        for (n in 1..strandLength) {
            lightStrandTarget[n] = mutableListOf(targetColor.red, targetColor.green, targetColor.blue, targetColor.white)
        }

    }
    fun writeTargetStateToLights(pixelStrandController: AdafruitNeopixelSeesaw){
        var pixelsController = pixelStrandController

        var indexOfThisPixelTarget: Int = 0
        //what is this color format? I don't get it.
        for (pixelStatus in lightStrandTarget) {
            indexOfThisPixelTarget += 1
            val targetRed = pixelStatus[0].toInt().toByte()
            val targetGreen = pixelStatus[1].toInt().toByte()
            val targetBlue = pixelStatus[2].toInt().toByte()
            val targetWhite = pixelStatus[3].toInt().toByte()
//            Color.rgb(pixelStatus[0].toFloat(), pixelStatus[1].toFloat(), pixelStatus[2].toFloat())
            pixelsController!!.setColorRGBW(targetRed, targetGreen, targetBlue, targetWhite, indexOfThisPixelTarget.toShort())
            //WHAT IS WRGB??? I DON'T GET THIS COLOR FORMAT
        }

    }


}
@TeleOp
class NeopixelTest : LinearOpMode() {
    var neo: AdafruitNeopixelSeesaw? = null

    @Throws(InterruptedException::class)
    override fun runOpMode() {
        initialize_opmode()
        waitForStart()
        if (opModeIsActive()) {
            while (opModeIsActive() && !isStopRequested) {
                val WRGB = 0x333333
                for (i in 0..11) {
                    neo!!.setColor(WRGB, i.toShort())
                }
                val red = WRGB shr 8 * 2 and 0xfe
                val green = WRGB shr 8 * 1 and 0xfe
                val blue = WRGB shr 8 * 0 and 0xfe
                val white = WRGB shr 8 * 3 and 0xfe
                telemetry.addData("red = ", red)
                telemetry.addData("green = ", green)
                telemetry.addData("blue = ", blue)
                telemetry.addData("white = ", white)
                telemetry.update()
            }
        }
    }

    fun initialize_opmode() {

    }
}