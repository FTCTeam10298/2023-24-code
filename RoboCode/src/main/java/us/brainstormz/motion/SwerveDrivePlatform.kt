package us.brainstormz.motion

import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.DiffySwerveHardware

class SwerveDrivePlatform(private val hardware: DiffySwerveHardware): DifferentialDrivePlatform {
    private val leftPod = hardware.pods[1]
    private val rightPod = hardware.pods[2]
    override fun leftDrive(): List<DcMotor> = listOf(leftPod.motor1, leftPod.motor2)
    override fun rightDrive(): List<DcMotor> = listOf(rightPod.motor1, rightPod.motor2)

    fun movePower(yPower: Double, xPower: Double, rPower: Double) {

    }
}