package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.util.Range
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

open class MecanumDriveTrain(private val hardware: MecanumHardware) {

    /**
     * driveSetPower sets all of the drive train motors to the specified power levels.
     * @param lFPower Power level to set front left motor to
     * @param rFPower Power level to set front right motor to
     * @param lBPower Power level to set back left motor to
     * @param rBPower Power level to set back right motor to
     */
    fun driveSetPower(lFPower: Double, rFPower: Double, lBPower: Double, rBPower: Double) {
        hardware.lFDrive.power = lFPower
        hardware.rFDrive.power = rFPower
        hardware.lBDrive.power = lBPower
        hardware.rBDrive.power = rBPower
    }

    fun driveFieldCentric(x: Double, y: Double, r: Double, currentHeadingRadians: Double) {
        val rotX = x * cos(-currentHeadingRadians) - y * sin(-currentHeadingRadians)
        val rotY = x * sin(-currentHeadingRadians) + y * cos(-currentHeadingRadians)

        driveSetPower(
                (rotY + rotX + r),
                (rotY - rotX - r),
                (rotY - rotX + r),
                (rotY + rotX - r))
    }

    /**
     * DrivePowerAll sets all of the drive train motors to the specified power level.
     * @param power Power level to set all motors to
     */
    fun drivePowerAll(power: Double) {
        driveSetPower(power, power, power, power)
    }

    fun drivePowerZero() {
        drivePowerAll(0.0)
    }

    /**
     * driveSetMode sets all of the drive train motors to the specified mode.
     * @param runmode RunMode to set motors to
     */
    fun driveSetMode(runmode: DcMotor.RunMode?) {
        hardware.lFDrive.mode = runmode
        hardware.rFDrive.mode = runmode
        hardware.lBDrive.mode = runmode
        hardware.rBDrive.mode = runmode

    }


    fun driveSetRunToPosition() {
        if (hardware.lFDrive.mode != DcMotor.RunMode.RUN_TO_POSITION || hardware.rFDrive.mode != DcMotor.RunMode.RUN_TO_POSITION || hardware.lBDrive.mode != DcMotor.RunMode.RUN_TO_POSITION || hardware.rBDrive.mode != DcMotor.RunMode.RUN_TO_POSITION) {
            driveSetMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER)
            // When the encoder is reset, also reset the target position, so it doesn't add an old
            // target position when using driveAddTargetPosition().
            driveSetTargetPosition(0, 0, 0, 0)
            driveSetMode(DcMotor.RunMode.RUN_TO_POSITION)
        }
    }

    /**
     * driveSetTargetPosition sets all of the drive train motors to the specified positions.
     * @param lFPosition Position to set front left motor to run to
     * @param rFPosition Position to set front right motor to run to
     * @param lBPosition Position to set back left motor to run to
     * @param rBPosition Position to set back right motor to run to
     */
    fun driveSetTargetPosition(lFPosition: Int, rFPosition: Int, lBPosition: Int, rBPosition: Int) {
        hardware.lFDrive.targetPosition = lFPosition
        hardware.rFDrive.targetPosition = rFPosition
        hardware.lBDrive.targetPosition = lBPosition
        hardware.rBDrive.targetPosition = rBPosition
    }

    fun driveAddTargetPosition(lFPosition: Int, rFPosition: Int, lBPosition: Int, rBPosition: Int) {
        hardware.lFDrive.targetPosition = hardware.lFDrive.targetPosition + lFPosition
        hardware.rFDrive.targetPosition = hardware.rFDrive.targetPosition + rFPosition
        hardware.lBDrive.targetPosition = hardware.lBDrive.targetPosition + lBPosition
        hardware.rBDrive.targetPosition = hardware.rBDrive.targetPosition + rBPosition
    }

    fun driveAllAreBusy(): Boolean = hardware.lFDrive.isBusy && hardware.rFDrive.isBusy && hardware.lBDrive.isBusy && hardware.rBDrive.isBusy

    fun setSpeedAll(vX: Double, vY: Double, vA: Double, minPower: Double, maxPower: Double) {

        // Calculate theoretical values for motor powers using transformation matrix
        var fl = vY + vX - vA
        var fr = vY - vX + vA
        var bl = vY - vX - vA
        var br = vY + vX + vA

        // Find the largest magnitude of power and the average magnitude of power to scale down to
        // maxPower and up to minPower
        var max = abs(fl)
        max = max.coerceAtLeast(abs(fr))
        max = max.coerceAtLeast(abs(bl))
        max = max.coerceAtLeast(abs(br))
        val ave = (abs(fl) + abs(fr) + abs(bl) + abs(br)) / 4
        if (max > maxPower) {
            fl *= maxPower / max
            bl *= maxPower / max
            br *= maxPower / max
            fr *= maxPower / max
        } else if (ave < minPower) {
            fl *= minPower / ave
            bl *= minPower / ave
            br *= minPower / ave
            fr *= minPower / ave
        }

        // Range clip just to be safe
        fl = Range.clip(fl, -1.0, 1.0)
        fr = Range.clip(fr, -1.0, 1.0)
        bl = Range.clip(bl, -1.0, 1.0)
        br = Range.clip(br, -1.0, 1.0)

//        telemetry.addLine("Powers: $fl, $bl, $fr, $br" )
        println("Powers: $fl, $bl, $fr, $br")

        // Set powers
        driveSetPower(fl, fr, bl, br)
    }
}