package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import kotlinx.serialization.Serializable
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons.AdafruitNeopixelSeesaw
import kotlin.random.Random

//

fun emitN(n: Int, color: Neopixels.PixelState): List<Neopixels.PixelState> = (0..n).map { color }

//fun emitN2(first:Int, last: Int, color: Neopixels.PixelState): List<Neopixels.PixelState> = (first..last).map { color } - generalized filter
class Neopixels : Subsystem {
    var lastPixelWriteDuration: Long = -1

    enum class NeoPixelColors(val pixelState: PixelState) {
        Red(PixelState(255.0, 0.0, 0.0, 0.0)),
        Green(PixelState(0.0, 0.0, 0.0, 255.0)),
        Blue(PixelState(0.0, 255.0, 0.0, 0.0)),
        White(PixelState(0.0, 0.0, 255.0, 0.0)),
        Purple(PixelState(205.0, 255.0, 0.0, 0.0)),
        Yellow(PixelState(255.0, 0.0, 0.0, 165.0)),
        Off(PixelState(0.0, 0.0, 0.0, 0.0)),
    }

    @Serializable
    data class PixelState(val red: Double, val blue: Double, val white: Double, val green: Double)
    @Serializable
    data class StripState(val wroteForward: Boolean = true, val pixels: List<PixelState>) {

        override fun toString(): String = """
            StripState(
                wroteForward = $wroteForward,
                pixels = $pixels
            )
        """.trimMargin()

    }

    data class HalfAndHalfTarget(val left: NeoPixelColors, val right: NeoPixelColors, val midPointIndex: Int = 30) {
        constructor(both: NeoPixelColors = NeoPixelColors.Off) : this(left = both, right = both) //todo: whut.

        fun compileStripState(): StripState {
            return StripState(pixels = emitN(midPointIndex, left.pixelState) + emitN(60 - midPointIndex, right.pixelState))
        }
    }

    private val strandLength: Int = 60


    fun makeEmptyLightState(): StripState {
        var currentTargetState: MutableList<PixelState> = mutableListOf()
        //Create a list of pixel values we can change... the function to do it is not good right now.
        val prototypeLightStatus = PixelState(0.0, 0.0, 0.0, 0.0) //R, G, B, W

        for (n in 0..strandLength) {
            currentTargetState += prototypeLightStatus
        }

        return StripState(true, currentTargetState)
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

    fun writePixel(pixelStatus: PixelState, pixelsController: AdafruitNeopixelSeesaw, index: Int) {
        val targetRed = pixelStatus.red.toInt().toByte()
        val targetGreen = pixelStatus.blue.toInt().toByte()
        val targetBlue = pixelStatus.white.toInt().toByte()
        val targetWhite = pixelStatus.green.toInt().toByte()
        //                println("targetstate: $targetState, \n past target state $pastTargetState")
        //                println("pixelStatus: $pixelStatus, \n past pixel status $pastPixelStatus")
        //                pixelsController!!.setColorRGBW(debugWhite.red.toInt().toByte(), debugWhite.blue.toInt().toByte(), debugWhite.white.toInt().toByte(), debugWhite.green.toInt().toByte(), index.toShort()) //debugging
        //                sleep(128) //250 is also quite good.
        pixelsController!!.setColorRGBW(targetRed, targetGreen, targetBlue, targetWhite, index.toShort())
    }

    //add information to stripstate

    fun writeQuickly(desiredState: StripState, currentState: StripState, pixelStrandController: AdafruitNeopixelSeesaw): StripState {

        val updatedState = desiredState.pixels.toMutableList()

        var haveWrittenOnce = false
        desiredState.pixels.forEachIndexed { index, desiredPixelState ->
            val currentPixelState = currentState.pixels.getOrNull(index)
            if (currentPixelState != desiredPixelState && !haveWrittenOnce) {
                writePixel(desiredPixelState, pixelStrandController, index)
                updatedState[index] = desiredPixelState
                haveWrittenOnce = true
            } else {
                updatedState[index] = currentPixelState ?: NeoPixelColors.Off.pixelState
            }
        }

        return StripState(true, updatedState)

    }

    fun writeQuicklyFromCenter(center: Int, desiredState: StripState, currentState: StripState, pixelStrandController: AdafruitNeopixelSeesaw): StripState {

        val lastPixel = desiredState.pixels.size - 1
        val defaultPixelState = NeoPixelColors.Off.pixelState

        val updatedState = desiredState.pixels.toMutableList()

        var haveWrittenOnce = false
        fun changeFirstWrongPixel(index: Int) {

            val desiredPixelState = desiredState.pixels.getOrNull(index) ?: defaultPixelState
            val currentPixelState = currentState.pixels.getOrNull(index) ?: defaultPixelState

            if (currentPixelState != desiredPixelState && !haveWrittenOnce) {
                writePixel(desiredPixelState, pixelStrandController, index)
                updatedState[index] = desiredPixelState
                haveWrittenOnce = true
            } else {
                updatedState[index] = currentPixelState
            }
        }
        (0..lastPixel).forEach { index ->
            updatedState[index] = currentState.pixels.getOrNull(index) ?: defaultPixelState
        }
        var willWeWriteForward = !currentState.wroteForward
        if (willWeWriteForward) {
            (center..lastPixel).forEach(::changeFirstWrongPixel)
        } else {
            (0..(center - 1)).reversed().forEach(::changeFirstWrongPixel)
        }

        return StripState(wroteForward = willWeWriteForward, updatedState)

    }


//    fun changeOnePixel(targetColor: PixelState, targetPixel: Int) {
//        lightStrandTarget[targetPixel] = mutableListOf(targetColor.red, targetColor.blue, targetColor.white, targetColor.green)
//    }
//
//    fun flood(targetColor: PixelState) {
//        for (n in 1..strandLength) {
//            lightStrandTarget[n] = mutableListOf(targetColor.red, targetColor.blue, targetColor.white, targetColor.green)
//        }
//        println("Result of flooding with $targetColor: \r $lightStrandTarget")
//
//    }
//
//    fun showOneByOne(previousTargetState: List<PixelState>, targetColor: PixelState, startPixel: Int, endPixel: Int): List<PixelState> {
//        val mutated = previousTargetState.toMutableList()
//        //not very necessary. Maybe someday?
//        for (n in startPixel..endPixel) {
//            mutated[n] = PixelState(targetColor.red, targetColor.blue, targetColor.white, targetColor.green)
//        }
//        val currentTargetState = mutated
//        return currentTargetState
//    }
//
//    //I should make a data class
//
//    //make this more efficient to only do 5 lights at a time, expanding from center... less data-sucking.
//    //requires targetstate, previous targetstate
//    //iterate through for both sides... wherever there isn't a pixel of the right color, paint the next x that color... should be easy.
//    fun showHalf(firstTargetColor: PixelState, secondTargetColor: PixelState) {
//
//        val halfOfStrand: Int
//        if (strandLength % 2 == 0) {
//            halfOfStrand = (strandLength / 2).toInt()
//        } else {
//            halfOfStrand = (strandLength / 2 - 0.5).toInt()
//        }
//
//        for (n in 1..halfOfStrand) {
//            lightStrandTarget[n] = mutableListOf(firstTargetColor.red, firstTargetColor.blue, firstTargetColor.white, firstTargetColor.green)
//        }
//
//        for (n in halfOfStrand + 1..strandLength) {
//            lightStrandTarget[n] = mutableListOf(firstTargetColor.red, firstTargetColor.blue, firstTargetColor.white, firstTargetColor.green)
//        }
//
//    }

//    //where the status bar starts.
//    val mondrianStatusBarStart: Int = 40
//    val mondrianStatusBarEnd: Int = 60
//
//
//    fun mondrianStatusBarAdd() {
//
//    }
//
//    enum class mondrianStatusBarMessages {
//        BarLive,
//        Problem
//    }
//
//    data class positionAndRGBWColor(val position: List<Int>, val targetColor: PixelState)

//    fun updateMondrianStatusBar() {
//        //black out status bar area
//
//        //can't change color. --
//        //iterate through and map each?
////        val targetStatusRegions = mapOf<mondrianStatusBarMessages, MutableList<Int>>() {
////            mondrianStatusBarMessages.BarLive to mutableListOf<Int>(0, 3)
////
////        }
//        val problemStart = 42
//        val problemEnd = 48
//
//        for (n in mondrianStatusBarStart..mondrianStatusBarStart) {
//            lightStrandTarget[n] = mutableListOf(0.0, 0.0, 0.0, 0.0)
//        }
//
//        for (status in mondrianStatusBarTarget) {
//            if (status == mondrianStatusBarMessages.Problem) {
//                for (n in mondrianStatusBarStart..mondrianStatusBarStart) {
//                    lightStrandTarget[n] = kotlin.collections.mutableListOf(150.0, 0.0, 0.0, 0.0)
//                }
//
//            }
//        }
//
//
//    }

    //now for the fun stuff—animations!

//    val debugWhite = NeoPixelColors.White.pixelState

    //take in targetState and previousTargetState, iterate 1-by-1, and only change a light if it needs to be changed.
    //output the final target state.

    //IDK if this works, but this is a simple version of it.
//    fun writeTargetStateToLights(targetState: List<PixelState>, pastTargetState: List<PixelState>, pixelStrandController: AdafruitNeopixelSeesaw): List<PixelState> {
//
//        var pixelsController = pixelStrandController
//
//        targetState.mapIndexed { index, pixelStatus ->
//            val pastPixelStatus = pastTargetState.getOrNull(index)
//
//            if (pixelStatus != pastPixelStatus) {
//                val start = System.currentTimeMillis()
//                writePixel(pixelStatus, pixelsController, index)
//                lastPixelWriteDuration = System.currentTimeMillis() - start
//            } else {
//                false
//            }
//        }
//
//        /** example of indexed map operation on a list */
////        val listOfBooleansNoProblem: List<Boolean> = targetState.mapIndexed { index, pixelStatus ->
////            val pastPixelStatus = pastTargetState[index]
////
////            if (pixelStatus == pastPixelStatus) {
////                true
////            } else {
////                false
////            }
////        }
////
////        /** example of map operation on a list */
////        val listOfBooleans: List<Boolean> = targetState.map { pixelStatus ->
////            if (pixelStatus[0] == 2.0) {
////                true
////            } else {
////                false
////            }
////        }
////
////        /** example of indexed for loop on a list */
////        targetState.forEachIndexed { index, pixelStatus ->
////            println("pixelStatus: $pixelStatus, index of pixel: $index")
////
////            val pastPixelStatus = pastTargetState[index]
////            println("pastPixelStatus: $pastPixelStatus, index of pixel: $index")
////        }
//
////        var indexOfThisPixelTarget: Int = 0
////        //what is this color format? I don't get it.
////        for (pixelStatus in targetState) {
////            indexOfThisPixelTarget += 1
////            val targetRed = pixelStatus[0].toInt().toByte()
////            val targetGreen = pixelStatus[1].toInt().toByte()
////            val targetBlue = pixelStatus[2].toInt().toByte()
////            val targetWhite = pixelStatus[3].toInt().toByte()
//////            Color.rgb(pixelStatus[0].toFloat(), pixelStatus[1].toFloat(), pixelStatus[2].toFloat())
////            pixelsController!!.setColorRGBW(targetRed, targetGreen, targetBlue, targetWhite, indexOfThisPixelTarget.toShort())
////            //WHAT IS WRGB??? I DON'T GET THIS COLOR FORMAT
////        }
//
//        val pastTargetState = targetState
//        return pastTargetState
//
//    }



    @TeleOp
    class NeopixelPlayground : LinearOpMode() {
        //    var neo: AdafruitNeopixelSeesaw? = null
        val neopixelSystem = Neopixels()
        var previousTargetState = neopixelSystem.makeEmptyLightState()
        lateinit var neo: AdafruitNeopixelSeesaw


        @Throws(InterruptedException::class)
        override fun runOpMode() {
            initialize_opmode()


            val red = Neopixels.PixelState(255.0, 0.0, 0.0, 0.0)
            val green = Neopixels.PixelState(0.0, 0.0, 10.0, 255.0)//not high contrast
            val blue = Neopixels.PixelState(0.0, 255.0, 0.0, 0.0)
            val white = Neopixels.PixelState(0.0, 0.0, 255.0, 0.0)
            val purple = Neopixels.PixelState(255.0, 255.0, 0.0, 0.0)
            val yellow = Neopixels.PixelState(255.0, 0.0, 0.0, 165.0)
            val black = Neopixels.NeoPixelColors.Off.pixelState

            waitForStart()
            if (opModeIsActive()) {
                var neo = hardwareMap.get(AdafruitNeopixelSeesaw::class.java, "neopixels")
                neopixelSystem.initialize(neo)
                while (opModeIsActive() && !isStopRequested) {

//                    var targetState = neopixelSystem.showOneByOne(previousTargetState, purple, 0, 60)

//                    previousTargetState = neopixelSystem.writeTargetStateToLights(emitN(60, white), previousTargetState, neo)
                    sleep(1000)
                    //
//                    targetState = neopixelSystem.showOneByOne(targetState, white, 0, 15)
//                    targetState = neopixelSystem.showOneByOne(targetState, purple, 16, 30)
//                    targetState = neopixelSystem.showOneByOne(targetState, yellow, 31, 46)
//                    targetState = neopixelSystem.showOneByOne(targetState, green, 47, 60)
//                    fun fiftyFifty(a: PixelState, b: PixelState) = emitN(30, a) + emitN(30, b)
//                    fun randomColor() = PixelState(
//                            red = Random.nextDouble(0.0, 255.0),
//                            blue = Random.nextDouble(0.0, 255.0),
//                            green = Random.nextDouble(0.0, 255.0),
//                            white = Random.nextDouble(0.0, 255.0),
//                    )


//                        val targetState = StripState(
////                                pixels = fiftyFifty(randomColor(), randomColor()),//(0 .. 60).map{randomColor()}
//                                pixels = emitN(midPoint, randomColor()) + emitN(60-midPoint, randomColor())
//                            )
                    val midPoint = 30

                    val targetState = HalfAndHalfTarget(left = NeoPixelColors.Blue, right = NeoPixelColors.Red).compileStripState()
                    var loopNumber = 0
                    while (targetState != previousTargetState) {
                        val startTimeMillis = System.currentTimeMillis()
                        previousTargetState = neopixelSystem.writeQuicklyFromCenter(
                                center = midPoint,
                                desiredState = targetState,
                                currentState = previousTargetState,
                                pixelStrandController = neo,
                        )
                        val durationMillis = System.currentTimeMillis() - startTimeMillis

                        loopNumber++
                        telemetry.clearAll()
                        telemetry.addLine("Last pixel ${neopixelSystem.lastPixelWriteDuration} millis, Loop numer $loopNumber, time between start time of last loop and start time of this loop (milliseconds): $durationMillis")

                    }

                    //various tests... very inefficient.
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
//        val redZEROInt = redZERODouble.toInt()
//        val greenZEROInt = greenZERODouble.toInt()
//        val blueZEROInt = blueZERODouble.toInt()
//        val whiteZEROInt = whiteZERODouble.toInt()
//        val redZEROByte = redZEROInt.toByte()
//        val greenZEROByte = greenZEROInt.toByte()
//        val blueZEROByte = blueZEROInt.toByte()
//        val whiteZEROByte = whiteZEROInt.toByte()

            //test all colors

//
//        }


//        enum class PixelColors


//        val alpha = neopixelSystem.colorInNeopixel(red) //should be red
//        val beta = neopixelSystem.colorInNeopixel(blue)  //should be green
//        val gamma = neopixelSystem.colorInNeopixel(white)  //should be blue
//        val delta = neopixelSystem.colorInNeopixel(green)  //should be white
            println("green = ")

//         previousTargetState = neopixelSystem.showOneByOne(previousTargetState, orange, 0, 60)
            val white = Neopixels.NeoPixelColors.White.pixelState
            val blue = Neopixels.NeoPixelColors.Blue.pixelState


            val targetState = StripState(true,
                    emitN(2, white) +
                            emitN(5, Neopixels.PixelState(255.0, 205.0, 0.0, 0.0)) +
                            emitN(2, white) +
                            emitN(5, Neopixels.PixelState(255.0, 255.0, 0.0, 0.0)) +
                            emitN(2, white) +
                            emitN(5, Neopixels.PixelState(205.0, 255.0, 0.0, 0.0)) +
                            emitN(2, white) +
                            emitN(5, Neopixels.PixelState(190.0, 255.0, 0.0, 0.0)) +
                            emitN(2, white) +
                            emitN(5, Neopixels.PixelState(170.0, 255.0, 0.0, 0.0)) +
                            emitN(2, white) +
                            emitN(5, Neopixels.PixelState(150.0, 255.0, 0.0, 0.0)) +
                            emitN(2, white) +
                            emitN(5, Neopixels.PixelState(100.0, 255.0, 0.0, 0.0)) +
                            emitN(2, white) +
                            emitN(2, blue)
            )
//        var targetState = neopixelSystem.showOneByOne(previousTargetState, white, 0, 15)
//        targetState = neopixelSystem.showOneByOne(targetState, purple, 16, 30)
//        targetState = neopixelSystem.showOneByOne(targetState, yellow, 31, 46)
//        targetState = neopixelSystem.showOneByOne(targetState, green, 47, 58)
//        targetState = neopixelSystem.showOneByOne(targetState, blue, 58, 60)
//        previousTargetState = neopixelSystem.showOneByOne(previousTargetState, purple, 51, 60)


//            neopixelSystem.writeTargetStateToLights(targetState, previousTargetState, neo)
            neopixelSystem.writeQuickly(targetState, previousTargetState, neo)
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
}


//fun main() {
//    /** Nevermind, it's pass by value */
//    var A: List<List<Int>> = listOf(listOf(0))
//    println("A is: $A")
//
//    var B: List<List<Int>> = listOf(listOf(420))
//    println("B is: $B")
//
//    println("B is still : $B") //it's 420
//    B = A
//    println("B is now: $B") //it's 0
//
//    A = listOf(listOf(0), listOf(1), listOf(69))
//    println("A is now: $A") //it's 0, 1, 69
//    println("B is still: $B") //it's 0
//
//    val aIsEqualToB = A==B
//    println("aIsEqualToB: $aIsEqualToB")
//}