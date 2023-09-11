package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.hardware.*

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
}

interface ThreeWheelOdometry: HardwareClass {
    val lOdom: EnhancedDCMotor
    val rOdom: EnhancedDCMotor
    val cOdom: EnhancedDCMotor
}
