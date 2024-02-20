package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class JamesEncoderMovement (private val hardware: MecanumHardware, private val console: TelemetryConsole): MecanumDriveTrain(hardware) {

    val countsPerMotorRev = 28.0 // Rev HD Hex v2.1 Motor encoder
    val gearboxRatio = 19.2 // 40 for 40:1, 20 for 20:1
    val driveGearReduction = 1 / 1 // This is > 1.0 if geared for torque
    val wheelDiameterInches = 3.77953 // For figuring circumference
    val drivetrainError = 1.0 // Error determined from testing
    val countsPerInch = countsPerMotorRev * gearboxRatio * driveGearReduction / (wheelDiameterInches * PI) / drivetrainError
    val countsPerDegree: Double = countsPerInch * 0.268 * 2/3 // Found by testing

    var pid = PID(name = "all-direction-movement", 0.01, 0.00000001, 0.0)
    val precision = -0.1..0.1

    private var opModeStop = false


    fun changePosition(power: Double, forwardIn: Double, sidewaysIn: Double, rotationDegrees: Double) {
        driveSetMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER)
        driveSetMode(DcMotor.RunMode.RUN_USING_ENCODER)

        val y = forwardIn * countsPerInch
        val x = -sidewaysIn * countsPerInch
        val r = rotationDegrees * countsPerDegree
        val targetPos = PositionAndRotation(x, y, r)

        var previousPos = PositionAndRotation(0.0, 0.0, 0.0)

        console.display(1, "Target: $targetPos")

        while (!opModeStop) {
            val lF = hardware.lFDrive.currentPosition
            val rF = hardware.rFDrive.currentPosition
            val lB = hardware.lBDrive.currentPosition
            val rB = hardware.rBDrive.currentPosition

            val currentX = (-lF + rF + lB - rB) / 4
            val currentY = (lF + rF + lB + rB) / 4
            val currentR = (-lF + rF - lB + rB) / 4
//
//            val deltaY = cos(previousPos.r) * currentY - sin(previousPos.r) * currentX
//            val deltaX = sin(previousPos.r) * currentY + cos(previousPos.r) * currentX
//            val deltaPos = PositionAndRotation(deltaX, deltaY, (currentR - previousPos.r))
//
//            val currentPos = previousPos + deltaPos
            val currentPos = PositionAndRotation(currentX.toDouble(), currentY.toDouble(), currentR.toDouble())
//            previousPos = currentPos
//
//            val posDifferance = targetPos - currentPos
            val posDifferance = targetPos - currentPos
            val posDiffTotal = posDifferance.x + posDifferance.y + posDifferance.r

            if (posDiffTotal in precision) {
                console.display(20, "done with it")
                break
            }

            val pidValue: Double = pid.calcPID(targetPos, posDiffTotal).coerceIn(-power, power)

            val lfDirection = (targetPos.y + targetPos.x - targetPos.r).toInt()
            val rfDirection = (targetPos.y - targetPos.x + targetPos.r).toInt()
            val lbDirection = (targetPos.y - targetPos.x - targetPos.r).toInt()
            val rbDirection = (targetPos.y + targetPos.x + targetPos.r).toInt()

            val lfPower: Double = posOrNeg(lfDirection) * pidValue
            val rfPower: Double = posOrNeg(rfDirection) * pidValue
            val lbPower: Double = posOrNeg(lbDirection) * pidValue
            val rbPower: Double = posOrNeg(rbDirection) * pidValue

            driveSetPower(lfPower, rfPower, lbPower, rbPower)

            console.display(12, "diff: ${posDiffTotal / countsPerInch}")
            console.display(5, "currentPos: $currentPos")
            console.display(8, "lfPower $lfPower")
            console.display(9, "rfPower $rfPower")
            console.display(10, "lbPower $lbPower")
            console.display(11, "rbPower $rbPower")
        }
        drivePowerAll(0.0)
    }

    private fun posOrNeg(num: Int): Int {
        return when {
            num > 0 -> 1
            num < 0 -> -1
            else -> 0
        }
    }

    fun onStop() {
        opModeStop = true
    }
}

//@Autonomous(name= "James Movement Test", group= "Tests")
//class NewMovementTest: LinearOpMode() {
//
//    val console = TelemetryConsole(telemetry)
//    val hardware = RataTonyHardware()
//
//    val joovement = JamesEncoderMovement(hardware, console)
//    val movement = EncoderDriveMovement(hardware, console)
//
//    override fun runOpMode() {
//
//        hardware.init(hardwareMap)
//
//        waitForStart()
//        joovement.changePosition(1.0, 10.0, 10.0, 90.0)
//        sleep(1000)
//        console.display(20, "jooved!")
//        sleep(1000)
//    }
//}