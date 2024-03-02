package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons.AdafruitNeopixelSeesaw
import java.lang.Thread.sleep

//R -> R, G -> B, B -> W, W -> G
data class ColorInRGBWInUnknownOrder(val red: Double, val green: Double, val blue: Double, val white: Double)

class Neopixels: Subsystem {

    //color corrects for weird channel mapping.  TODO god its frustrating.
//    fun colorInNeopixel(wrongColor: ColorInRGBW): ColorInRGBW {
//        val correctColor = ColorInRGBW(wrongColor.red, wrongColor.white, wrongColor.green, wrongColor.blue)
//        return correctColor
//    }

    enum class LightTypes {
        RGBW,
        RGB //todo, we don't care
    }


    fun a(){


    }

    enum class NeoPixelColors(val ColorInRGBWInUnknownOrder : ColorInRGBWInUnknownOrder) {
        Red(ColorInRGBWInUnknownOrder(255.0, 0.0, 0.0, 0.0)),
        Green(ColorInRGBWInUnknownOrder(0.0, 0.0, 10.0, 255.0)),
        Blue(ColorInRGBWInUnknownOrder(0.0, 255.0, 0.0, 0.0)),
        White(ColorInRGBWInUnknownOrder(0.0, 0.0, 255.0, 0.0)),
        Purple(ColorInRGBWInUnknownOrder(255.0, 255.0, 0.0, 0.0)),
        Yellow(ColorInRGBWInUnknownOrder(255.0, 0.0, 0.0, 165.0)),
        Black(ColorInRGBWInUnknownOrder(0.0, 0.0, 0.0, 0.0))
    }

    val strandLength: Int = 60
    //represents light current status
    var lightStrandTarget = mutableListOf<MutableList<Double>>()
    var mondrianStatusBarTarget = mutableListOf<mondrianStatusBarMessages>()

    fun makeEmptyLightState(): List<List<Double>> {
        var currentTargetState: MutableList<MutableList<Double>> = mutableListOf()
        //Create a list of pixel values we can change... the function to do it is not good right now.
        val prototypeLightStatus: MutableList<Double> = mutableListOf(0.0, 0.0, 0.0, 0.0) //R, G, B, W

        for (n in 0..strandLength) {
            currentTargetState += prototypeLightStatus
        }

        return currentTargetState
        println("Initial Status String: $lightStrandTarget")
        }
    fun initialize(seesawController: AdafruitNeopixelSeesaw) {
        seesawController.setPixelType(AdafruitNeopixelSeesaw.ColorOrder.NEO_WRGB)
        seesawController.setBufferLength(60.toShort())
        for (i in 0..60) {
            seesawController.setColorRGBW((0).toByte(), (0).toByte(), (0).toByte(), (0).toByte(), i.toShort())
        }
    }

    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        println("Not yet implemented")
    }



    fun changeOnePixel(targetColor: ColorInRGBWInUnknownOrder, targetPixel: Int) {
        lightStrandTarget[targetPixel] = mutableListOf(targetColor.red, targetColor.green, targetColor.blue, targetColor.white)
    }

    fun flood(targetColor: ColorInRGBWInUnknownOrder)
    {
        for (n in 1..strandLength) {
            lightStrandTarget[n] = mutableListOf(targetColor.red, targetColor.green, targetColor.blue, targetColor.white)
        }
        println("Result of flooding with $targetColor: \r $lightStrandTarget")

    }

    fun showOneByOne(previousTargetState:  List<List<Double>>, targetColor: ColorInRGBWInUnknownOrder, startPixel: Int, endPixel: Int):  List<List<Double>> {
        val mutated = previousTargetState.toMutableList()
        //not very necessary. Maybe someday?
        for (n in startPixel..endPixel) {
            mutated[n] = mutableListOf(targetColor.red, targetColor.green, targetColor.blue, targetColor.white)
        }
        val currentTargetState = mutated
        return currentTargetState
    }

    //I should make a data class

    //make this more efficient to only do 5 lights at a time, expanding from center... less data-sucking.
    //requires targetstate, previous targetstate
    //iterate through for both sides... wherever there isn't a pixel of the right color, paint the next x that color... should be easy.
    fun showHalf(firstTargetColor: ColorInRGBWInUnknownOrder, secondTargetColor: ColorInRGBWInUnknownOrder) {

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

    data class positionAndRGBWColor(val position: List<Int>, val targetColor: ColorInRGBWInUnknownOrder)

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

    val debugWhite = NeoPixelColors.White.ColorInRGBWInUnknownOrder

    //take in targetState and previousTargetState, iterate 1-by-1, and only change a light if it needs to be changed.
    //output the final target state.
    fun writeTargetStateToLights(targetState:  List<List<Double>>, pastTargetState:  List<List<Double>>, pixelStrandController: AdafruitNeopixelSeesaw): List<List<Double>>{

        var pixelsController = pixelStrandController

        targetState.mapIndexed { index, pixelStatus ->
            val pastPixelStatus = pastTargetState[index]

            if (pixelStatus != pastPixelStatus) {
                val targetRed = pixelStatus[0].toInt().toByte()
                val targetGreen = pixelStatus[1].toInt().toByte()
                val targetBlue = pixelStatus[2].toInt().toByte()
                val targetWhite = pixelStatus[3].toInt().toByte()
//                println("targetstate: $targetState, \n past target state $pastTargetState")
                println("pixelStatus: $pixelStatus, \n past pixel status $pastPixelStatus")
                pixelsController!!.setColorRGBW(debugWhite.red.toInt().toByte(), debugWhite.green.toInt().toByte(), debugWhite.blue.toInt().toByte(), debugWhite.white.toInt().toByte(), index.toShort()) //debugging
                sleep(128) //250 is also quite good.
                pixelsController!!.setColorRGBW(targetRed, targetGreen, targetBlue, targetWhite, index.toShort())
            } else {
                false
            }
        }

        /** example of indexed map operation on a list */
//        val listOfBooleansNoProblem: List<Boolean> = targetState.mapIndexed { index, pixelStatus ->
//            val pastPixelStatus = pastTargetState[index]
//
//            if (pixelStatus == pastPixelStatus) {
//                true
//            } else {
//                false
//            }
//        }
//
//        /** example of map operation on a list */
//        val listOfBooleans: List<Boolean> = targetState.map { pixelStatus ->
//            if (pixelStatus[0] == 2.0) {
//                true
//            } else {
//                false
//            }
//        }
//
//        /** example of indexed for loop on a list */
//        targetState.forEachIndexed { index, pixelStatus ->
//            println("pixelStatus: $pixelStatus, index of pixel: $index")
//
//            val pastPixelStatus = pastTargetState[index]
//            println("pastPixelStatus: $pastPixelStatus, index of pixel: $index")
//        }

//        var indexOfThisPixelTarget: Int = 0
//        //what is this color format? I don't get it.
//        for (pixelStatus in targetState) {
//            indexOfThisPixelTarget += 1
//            val targetRed = pixelStatus[0].toInt().toByte()
//            val targetGreen = pixelStatus[1].toInt().toByte()
//            val targetBlue = pixelStatus[2].toInt().toByte()
//            val targetWhite = pixelStatus[3].toInt().toByte()
////            Color.rgb(pixelStatus[0].toFloat(), pixelStatus[1].toFloat(), pixelStatus[2].toFloat())
//            pixelsController!!.setColorRGBW(targetRed, targetGreen, targetBlue, targetWhite, indexOfThisPixelTarget.toShort())
//            //WHAT IS WRGB??? I DON'T GET THIS COLOR FORMAT
//        }

        val pastTargetState = targetState
        return pastTargetState

    }


}
@TeleOp
class NeopixelPlayground : LinearOpMode() {
//    var neo: AdafruitNeopixelSeesaw? = null
    val neopixelSystem = Neopixels()
    var previousTargetState = neopixelSystem.makeEmptyLightState()
    lateinit var neo: AdafruitNeopixelSeesaw


    @Throws(InterruptedException::class)
    override fun runOpMode() {
        initialize_opmode()



        waitForStart()
        if (opModeIsActive()) {
            var neo = hardwareMap.get(AdafruitNeopixelSeesaw::class.java, "neopixels")
            neopixelSystem.initialize(neo)
            var loopNumber = 0
            var previousLoopStartTimeMillis = 0L
            while (opModeIsActive() && !isStopRequested) {
                val loopStartTimeMillis = System.currentTimeMillis()


                val red = ColorInRGBWInUnknownOrder(255.0, 0.0, 0.0, 0.0)
                val green = ColorInRGBWInUnknownOrder(0.0, 0.0, 10.0, 255.0)//not high contrast
                val blue = ColorInRGBWInUnknownOrder(0.0, 255.0, 0.0, 0.0)
                val white = ColorInRGBWInUnknownOrder(0.0, 0.0, 255.0, 0.0)
                val purple = ColorInRGBWInUnknownOrder(255.0, 255.0, 0.0, 0.0 )
                val yellow = ColorInRGBWInUnknownOrder(255.0, 0.0, 0.0, 165.0)
                val black = Neopixels.NeoPixelColors.Black.ColorInRGBWInUnknownOrder


                var targetState = neopixelSystem.showOneByOne(previousTargetState, purple, 0, 60)

                previousTargetState = neopixelSystem.writeTargetStateToLights(targetState, previousTargetState, neo)

                sleep(1000)

                targetState = neopixelSystem.showOneByOne(targetState, white, 0, 15)
                targetState = neopixelSystem.showOneByOne(targetState, purple, 16, 30)
                targetState = neopixelSystem.showOneByOne(targetState, yellow, 31, 46)
                targetState = neopixelSystem.showOneByOne(targetState, green, 47, 60)

                previousTargetState = neopixelSystem.writeTargetStateToLights(targetState, previousTargetState, neo)

                loopNumber++
                val timeBetweenLoopStartTimesMillis = loopStartTimeMillis - previousLoopStartTimeMillis
                telemetry.addLine("Loop numer $loopNumber, time between start time of last loop and start time of this loop (milliseconds): $timeBetweenLoopStartTimesMillis")

//                val redDouble = 0.0
//                val greenDouble = 255.0
//                val blueDouble = 0.0
//                val whiteDouble = 0.0
//
//                ///IS THAT THE BYTE OF '87??
//                val redInt = redDouble.toInt()
//                val greenInt = greenDouble.toInt()
//                val blueInt = blueDouble.toInt()
//                val whiteInt = whiteDouble.toInt()
//                val redByte = redInt.toByte()
//                val greenByte = greenInt.toByte()
//                val blueByte = blueInt.toByte()
//                val whiteByte = whiteInt.toByte()
//                val redPUKEDouble = 0.0
//                val greenPUKEDouble = 0.0
//                val bluePUKEDouble = 255.0
//                val whitePUKEDouble = 0.0
//
//                ///IS THAT THE BYTE OF '87??
//                val redPUKEInt = redPUKEDouble.toInt()
//                val greenPUKEInt = greenPUKEDouble.toInt()
//                val bluePUKEInt = bluePUKEDouble.toInt()
//                val whitePUKEInt = whitePUKEDouble.toInt()
//                val redPUKEByte = redPUKEInt.toByte()
//                val greenPUKEByte = greenPUKEInt.toByte()
//                val bluePUKEByte = bluePUKEInt.toByte()
//                val whitePUKEByte = whitePUKEInt.toByte()
//                val WRGB = 0x333333
//                //                for (int i = 0; i < 30; i++){
////                    neo.setColor(WRGB, (short) i);
////                }
//                for (i in 0..29) {
//                    neo!!.setColorRGBW(redByte, greenByte, blueByte, whiteByte, i.toShort())
//
////                    state = neopixelSystem.changeOnePixel(neopixelSystem. 0.0)
//
//                }
//                for (i in 30..59) {
//                    neo!!.setColorRGBW(redPUKEByte, greenPUKEByte, bluePUKEByte, whitePUKEByte, i.toShort())
//                }
//
////                    neo.setColorRGBW(redPUKEByte, greenPUKEByte, bluePUKEByte, whitePUKEByte, ((short) 0));
////                    neo.setColorRGBW(redByte, greenByte, blueByte, whiteByte, ((short) 1));
//                val red = WRGB shr 8 * 2 and 0xfe
//                val green = WRGB shr 8 * 1 and 0xfe
//                val blue = WRGB shr 8 * 0 and 0xfe
//                val white = WRGB shr 8 * 3 and 0xfe
//                telemetry.addData("red = ", red)
//                telemetry.addData("green = ", green)
//                telemetry.addData("blue = ", blue)
//                telemetry.addData("white = ", white)
//                telemetry.update()


                previousLoopStartTimeMillis = loopStartTimeMillis
                telemetry.update()
            }
        }
    }

    fun initialize_opmode() {
        var neo = hardwareMap.get(AdafruitNeopixelSeesaw::class.java, "neopixels")
        neopixelSystem.initialize(neo)

        val redZERODouble = 0.0
        val greenZERODouble = 0.0
        val blueZERODouble = 0.0
        val whiteZERODouble = 0.0

        ///IS THAT THE BYTE OF '87??
        val redZEROInt = redZERODouble.toInt()
        val greenZEROInt = greenZERODouble.toInt()
        val blueZEROInt = blueZERODouble.toInt()
        val whiteZEROInt = whiteZERODouble.toInt()
        val redZEROByte = redZEROInt.toByte()
        val greenZEROByte = greenZEROInt.toByte()
        val blueZEROByte = blueZEROInt.toByte()
        val whiteZEROByte = whiteZEROInt.toByte()

        //test all colors

//
//        }
        val red = ColorInRGBWInUnknownOrder(255.0, 0.0, 0.0, 0.0)
        val green = ColorInRGBWInUnknownOrder(0.0, 0.0, 10.0, 255.0)//not high contrast
        val blue = ColorInRGBWInUnknownOrder(0.0, 255.0, 0.0, 0.0)
        val white = ColorInRGBWInUnknownOrder(0.0, 0.0, 255.0, 0.0)
        val purple = ColorInRGBWInUnknownOrder(255.0, 255.0, 0.0, 0.0 )
        val yellow = ColorInRGBWInUnknownOrder(255.0, 0.0, 0.0, 165.0)



//        enum class PixelColors



//        val alpha = neopixelSystem.colorInNeopixel(red) //should be red
//        val beta = neopixelSystem.colorInNeopixel(blue)  //should be green
//        val gamma = neopixelSystem.colorInNeopixel(white)  //should be blue
//        val delta = neopixelSystem.colorInNeopixel(green)  //should be white
        println("green = ")

//         previousTargetState = neopixelSystem.showOneByOne(previousTargetState, orange, 0, 60)

        var targetState = neopixelSystem.showOneByOne(previousTargetState, white, 0, 15)
        targetState = neopixelSystem.showOneByOne(targetState, purple, 16, 30)
        targetState = neopixelSystem.showOneByOne(targetState, yellow, 31, 46)
        targetState = neopixelSystem.showOneByOne(targetState, green, 47, 60)
//        previousTargetState = neopixelSystem.showOneByOne(previousTargetState, purple, 51, 60)


        neopixelSystem.writeTargetStateToLights(targetState, previousTargetState, neo)
//        neopixelSystem.showOneByOne(state)
        //think I wrote an equivalent function... init()
//        neo!!.setPixelType(AdafruitNeopixelSeesaw.ColorOrder.NEO_WRGB)
//        neo!!.setBufferLength(60.toShort())
//        for (i in 0..100) {
//            neo!!.setColorRGBW(redZEROByte, greenZEROByte, blueZEROByte, whiteZEROByte, i.toShort())
//        }
//        neo!!.init_neopixels()

    }
}



fun main() {
    /** Nevermind, it's pass by value */
    var A: List<List<Int>> = listOf(listOf(0))
    println("A is: $A")

    var B: List<List<Int>> = listOf(listOf(420))
    println("B is: $B")

    println("B is still : $B") //it's 420
    B = A
    println("B is now: $B") //it's 0

    A = listOf(listOf(0), listOf(1), listOf(69))
    println("A is now: $A") //it's 0, 1, 69
    println("B is still: $B") //it's 0

    val aIsEqualToB = A==B
    println("aIsEqualToB: $aIsEqualToB")
}