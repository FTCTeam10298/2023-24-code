package us.brainstormz.hardwareClasses

import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.qualcomm.robotcore.hardware.*
import posePlanner.Point2D

interface HardwareClass {

    var hwMap: HardwareMap

    fun init(ahwMap: HardwareMap)
}

interface TankHardware: HardwareClass {
    val lDrive: DcMotor
    val rDrive: DcMotor
}

interface DiffySwervePod {
    val motor1: DcMotor
    val motor2: DcMotor
    val rEncoder: DcMotor
}

interface DiffySwerveHardware: HardwareClass {
    val pods: List<DiffySwervePod>
}

interface MecanumHardware: HardwareClass {
    val lFDrive: DcMotor
    val rFDrive: DcMotor
    val lBDrive: DcMotor
    val rBDrive: DcMotor
    val driveMotors: List<DcMotor>
        get() = listOf(lFDrive, rFDrive, lBDrive, rBDrive)
}

interface ThreeWheelOdometry: HardwareClass {
    val lOdom: MotorEncoderOnly
    val rOdom: MotorEncoderOnly
    val cOdom: MotorEncoderOnly
}

interface TwoWheelImuOdometry: HardwareClass {
    val imu: IMU
    val parallelEncoder: OverflowEncoder
    val parallelOdomOffsetFromCenterInch: Point2D
    val perpendicularEncoder: OverflowEncoder
    val perpendicularOdomOffsetFromCenterInch: Point2D
}
