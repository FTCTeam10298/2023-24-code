package us.brainstormz.motion

//import com.acmerobotics.dashboard.FtcDashboard
//import com.acmerobotics.dashboard.config.Config
//import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
//rimport us.brainstormz.paddieMatrick.PaddieMatrickHardware
import us.brainstormz.pid.PID
import kotlin.math.*

open class MecanumMovement(override val localizer: Localizer, override val hardware: MecanumHardware, private val telemetry: Telemetry): Movement, MecanumDriveTrain(hardware) {

//    val defaultYTranslationPID =    PID(0.18,  0.00001, 1.9)
//    val defaultXTranslationPID =    PID(0.5,  0.00015, 1.5)
//    val defaultRotationPID =        PID(2.0)//,  0.0015, 1.5)
    companion object {
        val defaultYTranslationPID = PID(kp = 0.06, ki = 0.0)
        val defaultXTranslationPID = PID(kp = 0.1, ki = 0.01)
        val defaultRotationPID = PID(kp = 1.5, ki = 0.003)
    }
    val defaultPrecisionInches = 5.0
    val defaultPrecisionDegrees = 3.0

    var yTranslationPID = defaultYTranslationPID
    var xTranslationPID = defaultXTranslationPID
    var rotationPID = defaultRotationPID
    override var precisionInches: Double = defaultPrecisionInches
    override var precisionDegrees: Double = 3.0

    /**
     * Blocking function
     */
    override fun goToPosition(target: PositionAndRotation, linearOpMode: LinearOpMode, powerRange: ClosedRange<Double>) {
        while (linearOpMode.opModeIsActive()) {
            val targetReached = moveTowardTarget(target, powerRange)
            if (targetReached)
                break
        }
    }

    fun goToPosition(target: PositionAndRotation, linearOpMode: LinearOpMode, powerRange: ClosedRange<Double>, asyncTask: ()->Unit = {}) {
        yTranslationPID = defaultYTranslationPID
        xTranslationPID = defaultXTranslationPID
        rotationPID = defaultRotationPID
        precisionInches = defaultPrecisionInches
        while (linearOpMode.opModeIsActive()) {
            val targetReached = moveTowardTarget(target, powerRange)
            asyncTask()
            if (targetReached)
                break
        }
    }

//    fun goToPositionThreeAxis(target: PositionAndRotation, linearOpMode: LinearOpMode, powerRange: ClosedRange<Double>, asyncTask: ()->Unit = {}) {
//        yTranslationPID = multiDirectionYTranslationPID
//        xTranslationPID = multiDirectionXTranslationPID
//        rotationPID = multiDirectionRotationPID
//        precisionInches = 1.0
//        goToPosition(target, linearOpMode, powerRange, asyncTask)
//    }

//    yTranslationPID: PID = defaultYTranslationPID,
//                                  xTranslationPID: PID = defaultXTranslationPID,
//                                  rotationPID: PID = defaultRotationPID

    override fun moveTowardTarget(target: PositionAndRotation, powerRange: ClosedRange<Double>): Boolean {
        localizer.recalculatePositionAndRotation()
        val currentPos = localizer.currentPositionAndRotation()
        val angleRad = Math.toRadians(currentPos.r)
//        telemetry.addLine("currentPos: $currentPos")
//        telemetry.addData("angleDef: ", Math.toDegrees(angleRad))

        // Find the error in distance for X
        val distanceErrorX = target.x - currentPos.x
        // Find there error in distance for Y
        val distanceErrorY = target.y - currentPos.y
//        telemetry.addData("distanceErrorX: ", distanceErrorX)
//        telemetry.addData("distanceErrorY: ", distanceErrorY)

        // Find the error in angle
        var tempAngleError = Math.toRadians(target.r) - angleRad

        while (tempAngleError > Math.PI)
            tempAngleError -= Math.PI * 2

        while (tempAngleError < -Math.PI)
            tempAngleError += Math.PI * 2

        val angleError: Double = tempAngleError

        // Find the error in distance
        val distanceError = hypot(distanceErrorX, distanceErrorY)

        // Check to see if we've reached the desired position already
        if (abs(distanceError) <= precisionInches &&
                abs(angleError) <= Math.toRadians(precisionDegrees)) {
            drivePowerZero()
            return true
        }

        // Calculate the error in x and y and use the PID to find the error in angle
        val speedX: Double = xTranslationPID.calcPID(sin(angleRad) * distanceErrorY + cos(angleRad) * distanceErrorX)
        val speedY: Double = yTranslationPID.calcPID(cos(angleRad) * distanceErrorY + sin(angleRad) * -distanceErrorX)
        val speedA: Double = rotationPID.calcPID(angleError)

//        telemetry.addLine("\ndistance error: $distanceError, \nangle error degrees: ${Math.toDegrees(angleError)}\n")
//        telemetry.addData("total distance error: ", distanceError)
//        telemetry.addData("angle error degrees: ", Math.toDegrees(angleError))
//
//        telemetry.addData("speedY: ", speedY)
//        telemetry.addData("speedX: ", speedX)
//        telemetry.addData("speedA: ", speedA)
//        telemetry.addLine("speedX: $speedX, speedY: $speedY, speedA: $speedA")

//        telemetry.update()

        setSpeedAll(vX= speedX, vY= speedY, vA= speedA, powerRange.start, powerRange.endInclusive)

        return false
    }

    fun isRobotAtPosition(currentPosition: PositionAndRotation, targetPosition: PositionAndRotation, precisionInches: Double = defaultPrecisionInches, precisionDegrees: Double = defaultPrecisionDegrees): Boolean {
        val angleRad = Math.toRadians(currentPosition.r)

        // Find the error in distance for X
        val distanceErrorX = targetPosition.x - currentPosition.x
        // Find there error in distance for Y
        val distanceErrorY = targetPosition.y - currentPosition.y

        // Find the error in angle
        var tempAngleError = Math.toRadians(targetPosition.r) - angleRad

        while (tempAngleError > Math.PI)
            tempAngleError -= Math.PI * 2

        while (tempAngleError < -Math.PI)
            tempAngleError += Math.PI * 2

        val angleError: Double = tempAngleError

        // Find the error in distance
        val distanceError = hypot(distanceErrorX, distanceErrorY)

        return  abs(distanceError) <= precisionInches &&
                abs(angleError) <= Math.toRadians(precisionDegrees)
    }

    /** works */
//    fun setSpeedAll(vX: Double, vY: Double, vA: Double, minPower: Double, maxPower: Double) {
//
//        // Calculate theoretical values for motor powers using transformation matrix
//        var fl = vY + vX - vA
//        var fr = vY - vX + vA
//        var bl = vY - vX - vA
//        var br = vY + vX + vA
//
//        // Find the largest magnitude of power and the average magnitude of power to scale down to
//        // maxPower and up to minPower
//        var max = abs(fl)
//        max = max.coerceAtLeast(abs(fr))
//        max = max.coerceAtLeast(abs(bl))
//        max = max.coerceAtLeast(abs(br))
//        val ave = (abs(fl) + abs(fr) + abs(bl) + abs(br)) / 4
//        if (max > maxPower) {
//            fl *= maxPower / max
//            bl *= maxPower / max
//            br *= maxPower / max
//            fr *= maxPower / max
//        } else if (ave < minPower) {
//            fl *= minPower / ave
//            bl *= minPower / ave
//            br *= minPower / ave
//            fr *= minPower / ave
//        }
//
//        // Range clip just to be safe
//        fl = Range.clip(fl, -1.0, 1.0)
//        fr = Range.clip(fr, -1.0, 1.0)
//        bl = Range.clip(bl, -1.0, 1.0)
//        br = Range.clip(br, -1.0, 1.0)
//
////        telemetry.addLine("Powers: $fl, $bl, $fr, $br" )
//        println("Powers: $fl, $bl, $fr, $br")
//
//        // Set powers
//        driveSetPower(fl, fr, bl, br)
//    }

}

//@TeleOp(name= "MovementTesting")
//class MovementTesting: LinearOpMode() {
//
//    val hardware = PaddieMatrickHardware()
//    val console = GlobalConsole.newConsole(telemetry)
//
//    var targetPos = PositionAndRotation(x= 0.0, y= 0.0, r= 90.0)
//
//    override fun runOpMode() {
//        hardware.init(hardwareMap)
//
//        val dashboard = FtcDashboard.getInstance()
//        val dashboardTelemetry = dashboard.telemetry
//        val localizer = RRLocalizer(hardware)
//        val movement = MecanumMovement(localizer= localizer, hardware= hardware, telemetry= dashboardTelemetry)
//
//        dashboardTelemetry.addData("speedY: ", 0)
//        dashboardTelemetry.addData("distanceErrorX: ", 0)
//        dashboardTelemetry.addData("distanceErrorY: ", 0)
//        dashboardTelemetry.addData("total distance error: ", 0)
//        dashboardTelemetry.addData("angle error degrees: ", 0)
//        dashboardTelemetry.addData("speedY: ", 0)
//        dashboardTelemetry.addData("speedX: ", 0)
//        dashboardTelemetry.addData("speedA: ", 0)
//        dashboardTelemetry.update()
//
////        localizer.setPositionAndRotation(x= 65.0, y= 0.0, r= 0.0)
//
//        waitForStart()
//
//        movement.goToPosition(targetPos, this, 0.0..1.0)
////        while (opModeIsActive()) {
////            val targetReached = movement.moveTowardTarget(targetPos, 0.0..1.0)
////
////            if (targetReached) {
////                dashboardTelemetry.addLine("target reached")
////            }
////        }
//    }
//}
//
//@TeleOp(name= "Movement PID Tuning")
//class MovementPIDTuning: LinearOpMode() {
//    @Config
//    object PIDsAndTarget {
//        @JvmField var ykp = 0.043
//        @JvmField var yki = 0.0000008
//        @JvmField var ykd = 0.0
//
//        @JvmField var xkp = 0.0988
//        @JvmField var xki = 0.000001
//        @JvmField var xkd = 0.0
//
//        @JvmField var rkp = 0.82
//        @JvmField var rki = 0.0000008
//        @JvmField var rkd = 0.0
//
//        @JvmField var targetY = 0.0
//        @JvmField var targetX = 0.0
//        @JvmField var targetR = 0.0
//
//        @JvmField var precisionInch = 0.5
//        @JvmField var precisionDegrees = 1.0
//    }
//
//    val hardware = PaddieMatrickHardware()
//    val console = GlobalConsole.newConsole(telemetry)
//
//    var targetPos = PositionAndRotation(x= PIDsAndTarget.targetX, y= PIDsAndTarget.targetY, r= PIDsAndTarget.targetR)
//
//    override fun runOpMode() {
//        hardware.init(hardwareMap)
//
//        val dashboard = FtcDashboard.getInstance()
//        val multipleTelemetry = MultipleTelemetry(telemetry, dashboard.telemetry)
//        val localizer = RRLocalizer(hardware)
//        val movement = MecanumMovement(localizer= localizer, hardware= hardware, telemetry= multipleTelemetry)
//
//        movement.yTranslationPID = PID(PIDsAndTarget.ykp, PIDsAndTarget.yki, PIDsAndTarget.ykd)
//        movement.xTranslationPID = PID(PIDsAndTarget.xkp, PIDsAndTarget.xki, PIDsAndTarget.xkd)
//        movement.rotationPID = PID(PIDsAndTarget.rkp, PIDsAndTarget.rki, PIDsAndTarget.rkd)
//        movement.precisionInches = PIDsAndTarget.precisionInch
//        movement.precisionDegrees = PIDsAndTarget.precisionDegrees
//
//        multipleTelemetry.addData("speedY: ", 0)
//        multipleTelemetry.addData("distanceErrorX: ", 0)
//        multipleTelemetry.addData("distanceErrorY: ", 0)
//        multipleTelemetry.addData("total distance error: ", 0)
//        multipleTelemetry.addData("angle error degrees: ", 0)
//        multipleTelemetry.addData("speedY: ", 0)
//        multipleTelemetry.addData("speedX: ", 0)
//        multipleTelemetry.addData("speedA: ", 0)
//        multipleTelemetry.update()
//        waitForStart()
//
////        movement.goToPosition(targetPos, this, 0.0..1.0)
//        while (opModeIsActive()) {
//            val targetReached = movement.moveTowardTarget(targetPos, 0.0..1.0)
//
//            if (targetReached) {
//                multipleTelemetry.addLine("target reached")
//            }
//        }
//
//    }
//}


//class PhoLocalizer(): Localizer {
//
//    var currentPositionAndRotation = PositionAndRotation()
//    override fun currentPositionAndRotation(): PositionAndRotation = currentPositionAndRotation
//
//    override fun recalculatePositionAndRotation() {
//        TODO("Not yet implemented")
//    }
//
//    override fun setPositionAndRotation(x: Double?, y: Double?, r: Double?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun startNewMovement() {
//        TODO("Not yet implemented")
//    }
//
//}