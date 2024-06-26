package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.util.Range
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.localizer.OdometryLocalizer
import kotlin.math.abs

interface MecOdometry: ThreeWheelOdometry, MecanumHardware
open class OdometryDriveTrain(private val hardware: MecOdometry, private val console: TelemetryConsole): MecanumDriveTrain(hardware) {

    lateinit var localizer: OdometryLocalizer

    /**
     * Sets the speed of the four drive motors given desired speeds in the robot's x, y, and angle.
     * @param vX Robot speed in the x (sideways) direction.
     * @param vY Robot speed in the y (forwards) direction.
     * @param vA Robot speed in the angle (turning) direction.
     * @param minPower Minimum speed allowed for the average of the four motors.
     * @param maxPower Maximum speed allowed for the fasted of the four motors.
     */
//    fun setSpeedAll(vX: Double, vY: Double, vA: Double, minPower: Double, maxPower: Double) {
//
//        // Calculate theoretical values for motor powers using transformation matrix
//        var fl = vY - vX + vA
//        var fr = vY + vX - vA
//        var bl = vY + vX + vA
//        var br = vY - vX - vA
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
//        // Set powers
//        hardware.lFDrive.power = fl
//        hardware.rFDrive.power = fr
//        hardware.lBDrive.power = bl
//        hardware.rBDrive.power = br
//    }

    fun init() {
        localizer = OdometryLocalizer(
            hardware)
    }

}