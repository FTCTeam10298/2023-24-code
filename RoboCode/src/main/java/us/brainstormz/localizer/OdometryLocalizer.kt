package us.brainstormz.localizer

import us.brainstormz.hardwareClasses.MecOdometry
import us.brainstormz.telemetryWizard.GlobalConsole
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

object OdometryFacts {
    val ticksPerRotation = 8192
    val rotationsPerInch = 1.37795 * PI
}

class OdometryLocalizer(private val odomHardware: MecOdometry) : Localizer {


    private val console = GlobalConsole.console
    private val odomWorld = OdomWorld()

    var forwardOffset = 0.0
    var trackwidth = 15.0


    private var deltaL = 0.0
    private var deltaC = 0.0
    private var deltaR = 0.0
    private var previousC = 0.0
    private var previousL = 0.0
    private var previousR = 0.0

    private var currentPositionAndRotation = PositionAndRotation(0.0, 0.0, 0.0)

    override fun currentPositionAndRotation() = currentPositionAndRotation


    /**
     * Updates the current position of the robot based off of the change in the
     * odometry encoders.
     */

    override fun recalculatePositionAndRotation() {
        val previous = currentPositionAndRotation

        calcEncoderDeltas()

        val deltaAngle = (deltaL - deltaR) / trackwidth
        val deltaMiddle = (deltaL + deltaR) / 2
        val deltaPerp = deltaC - (forwardOffset * deltaAngle)

        val deltaY = cos(previous.r) * deltaMiddle - sin(previous.r) * deltaPerp
        val deltaX = sin(previous.r) * deltaMiddle + cos(previous.r) * deltaPerp

        currentPositionAndRotation = odomWorld.recalculateGlobalPosition(PositionAndRotation(deltaX, deltaY, Math.toDegrees(deltaAngle)), previous)

//        currentPositionAndRotation.x += deltaX
//        currentPositionAndRotation.y += deltaY
//        currentPositionAndRotation.r += Math.toDegrees(deltaAngle)

//        r %= (2 * Math.PI)
//        if (r > Math.PI)
//            r -= 2 * Math.PI
    }

    override fun setPositionAndRotation(x: Double?, y: Double?, r: Double?){
        this.currentPositionAndRotation.setCoordinate(x=x, y=y, r=r)
    }

    private fun calcEncoderDeltas() {
        val currentL = -odomHardware.lOdom.getCurrentPosition().toDouble() / (OdometryFacts.ticksPerRotation / OdometryFacts.rotationsPerInch)
        val currentR = -odomHardware.rOdom.getCurrentPosition().toDouble() / (OdometryFacts.ticksPerRotation / OdometryFacts.rotationsPerInch)
        val currentC = odomHardware.cOdom.getCurrentPosition().toDouble() / (OdometryFacts.ticksPerRotation / OdometryFacts.rotationsPerInch)

        deltaL = currentL - previousL
        deltaR = currentR - previousR
        deltaC = currentC - previousC

//        console.display(5, "deltas: L $deltaL, R $deltaR, C $deltaC")
        println("deltas: L $deltaL, R $deltaR, C $deltaC")
        previousL = currentL
        previousR = currentR
        previousC = currentC
    }

//    override fun startNewMovement() {}
}

//@TeleOp(name= "Odom Localiaztion Test")
//class OdomLocalTest: OpMode() {
//
//    val console = GlobalConsole.newConsole(telemetry)
//    val localizer = OdometryLocalizer(hardware)
//    val robot = MecanumDriveTrain(hardware)
//
//    override fun init() {
//        hardware.init(hardwareMap)
//        localizer.setPositionAndRotation(0.0, 0.0, 0.0)
////        distance between l & r odom wheels: 7 8/16 = 7.5
//        localizer.trackwidth = 7.5
////        center of chassis: 10 6/16 = 10.375 / 2 = 5.1875
////        center odom: 5 2/16 = 5.125
////        forward offset: 5.1875 - 5.125 = 0.0625
////        localizer.forwardOffset = 0.0625
//    }
//
//    override fun loop() {
//        localizer.recalculatePositionAndRotation()
//
//        val currentPos = localizer.currentPositionAndRotation()
//
//        console.display(1, "Current Pos: $currentPos")
//        console.display(3, "Encoder Counts: \n  lOdom: ${hardware.lOdom.currentPosition},\n    rOdom: ${hardware.rOdom.currentPosition}, \n    cOdom: ${hardware.cOdom.currentPosition}")
//
//
//
////        val yInput = gamepad1.left_stick_y.toDouble()
////        val xInput = gamepad1.left_stick_x.toDouble()
////        val rInput = gamepad1.right_stick_x.toDouble()
////
////        val y = -yInput
////        val x = xInput
////        val r = -rInput * .8
////
////        robot.driveSetPower(
////            (y + x - r),
////            (y - x + r),
////            (y - x - r),
////            (y + x + r)
////        )
//    }
//
//}
//
//fun main() {
//    val hardware = PhoHardware()
//    val console = GlobalConsole.newConsole(hardware.telemetry)
//    val localizer = OdometryLocalizer(hardware)
//    val odomWorld = OdomWorld()
//
//    val odomWheels = listOf(hardware.lOdom, hardware.rOdom, hardware.cOdom)
//
//    val startLocation = PositionAndRotation()
//    val targetLocation = PositionAndRotation(0.0, 10.0, 0.0)
//
//    val inchesPerWheel = localizer.rotationsPerInch
//    val trackwidth = 7.5
//    val turnCircumference = trackwidth * PI
//
//    localizer.setPositionAndRotation(0.0, 0.0, 0.0)
//
////    while (true) {
//    hardware.cOdom.currentPos = (localizer.ticksPerRotation / localizer.rotationsPerInch).toInt()
//    hardware.rOdom.currentPos = (localizer.ticksPerRotation / localizer.rotationsPerInch).toInt()
//    hardware.lOdom.currentPos = (localizer.ticksPerRotation / localizer.rotationsPerInch).toInt()
////    println("tpr: ${(localizer.ticksPerRotation * localizer.rotationsPerInch)}")
//
//    localizer.recalculatePositionAndRotation()
//    println("Position: ${localizer.currentPositionAndRotation()}")
//
////    }
//}
//
//class PhoEnhancedDCMotor(private val motor: DcMotor): EnhancedDCMotor(motor) {
//
//    var currentPos = 0
//    override fun getCurrentPosition(): Int {
////        val console = GlobalConsole.console
////        console.display(5, "gettingReversedPosition")
//        return currentPos * when (reversed) {
//            true -> -1
//            false -> 1
//        }
//    }
//}