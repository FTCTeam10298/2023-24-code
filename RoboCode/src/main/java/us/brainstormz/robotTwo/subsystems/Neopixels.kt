//package us.brainstormz.robotTwo.subsystems
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp
//import us.brainstormz.operationFramework.Subsystem
//import us.brainstormz.robotTwo.RobotTwoHardware
//import us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons.AdafruitNeopixelSeesaw
//
//class Neopixels: Subsystem {
//    //TODO
//    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
//        TODO("Not yet implemented")
//    }
//    fun flood(color: Int=0x333333) {
//        
//    }
//    fun showHalves(color1: Int, color2: Int) {
//
//    }
//}
//@TeleOp
//class NeopixelTest : LinearOpMode() {
//    var neo: AdafruitNeopixelSeesaw? = null
//
//    @Throws(InterruptedException::class)
//    override fun runOpMode() {
//        initialize_opmode()
//        waitForStart()
//        if (opModeIsActive()) {
//            while (opModeIsActive() && !isStopRequested) {
//                val WRGB = 0x333333
//                for (i in 0..11) {
//                    neo!!.setColor(WRGB, i.toShort())
//                }
//                val red = WRGB shr 8 * 2 and 0xfe
//                val green = WRGB shr 8 * 1 and 0xfe
//                val blue = WRGB shr 8 * 0 and 0xfe
//                val white = WRGB shr 8 * 3 and 0xfe
//                telemetry.addData("red = ", red)
//                telemetry.addData("green = ", green)
//                telemetry.addData("blue = ", blue)
//                telemetry.addData("white = ", white)
//                telemetry.update()
//            }
//        }
//    }
//
//    fun initialize_opmode() {
//        neo = hardwareMap.get(AdafruitNeopixelSeesaw::class.java, "neopixels")
//        neo.setPixelType(AdafruitNeopixelSeesaw.ColorOrder.NEO_GRB)
//        neo.init_neopixels()
//    }
//}