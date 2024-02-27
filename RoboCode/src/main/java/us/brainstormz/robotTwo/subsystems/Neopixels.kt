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
    var lightStrandStatus = mutableListOf<MutableList<Double>>()

    fun prepareNeopixels() {
        //Create a list of pixel values we can change... the function to do it is not good right now.
        var prototypeLightStatus: MutableList<Double> = mutableListOf(0.0, 0.0, 0.0, 0.0) //R, G, B, W

        for (n in 1..strandLength) {
            lightStrandStatus += prototypeLightStatus
        }
        println("Initial Status String: $lightStrandStatus")
        }

    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        println("Not yet implemented")
    }


    fun flood(color: Int=0x333333)
    {

    }
    fun writeTargetStateToLights(pixelStrandController: AdafruitNeopixelSeesaw){
        var pixelsController = pixelStrandController

        var indexOfThisPixelTarget: Int = 0
        //what is this color format? I don't get it.
        for (pixelStatus in lightStrandStatus) {
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

//

fun showHalves(color1: Int, color2: Int) {

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