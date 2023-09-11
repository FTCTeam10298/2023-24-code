package us.brainstormz.threeDay

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import us.brainstormz.hardwareClasses.MecanumHardware

class ThreeDayHardware : MecanumHardware {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    lateinit var collector: DcMotor

    lateinit var clawA: Servo
    lateinit var clawB: Servo
    val clawClosedPos = 0.0
    val clawOpenPos = 0.0

    lateinit var leftArm: Servo
    lateinit var rightArm: Servo
    val armInPos = 0.0
    val armOutPos = 0.0

    override lateinit var hwMap: HardwareMap

    override fun init(ahwMap: HardwareMap) {

        hwMap = ahwMap

        // Drivetrain
        lFDrive = hwMap["lFDrive"] as DcMotor
        rFDrive = hwMap["rFDrive"] as DcMotor
        lBDrive = hwMap["lBDrive"] as DcMotor
        rBDrive = hwMap["rBDrive"] as DcMotor

        collector = hwMap["collector"] as DcMotor

        clawA = hwMap["clawA"] as Servo
        clawB = hwMap["clawB"] as Servo

        leftArm = hwMap["leftArm"] as Servo
        rightArm = hwMap["rightArm"] as Servo
        leftArm.direction = Servo.Direction.FORWARD
        rightArm.direction = Servo.Direction.REVERSE
    }

}