package us.brainstormz.threeDay

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
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
    val clawAClosedPos = 0.38
    val clawAOpenPos = 0.85
    lateinit var clawB: Servo
    val clawBClosedPos = 0.2
    val clawBOpenPos = 0.65

    lateinit var leftArm: Servo
    lateinit var rightArm: Servo
    enum class ArmPos(val position:Double) {
        In(0.0),
        Out(1.0)
    }

    lateinit var lift: DcMotorEx
    enum class LiftPos(val position:Int) {
        Down(0),
        Low(50),
        Middle(100),
        Max(100)
    }

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
        clawA.position = clawAClosedPos
        clawB.position = clawBClosedPos

        leftArm = hwMap["leftArm"] as Servo
        rightArm = hwMap["rightArm"] as Servo
        leftArm.direction = Servo.Direction.FORWARD
        rightArm.direction = Servo.Direction.REVERSE
        leftArm.position = ArmPos.In.position
        rightArm.position = ArmPos.In.position

        lift = hwMap["lift"] as DcMotorEx
        lift.direction = DcMotorSimple.Direction.FORWARD
        lift.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        lift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        lift.targetPosition = 0
        lift.mode = DcMotor.RunMode.RUN_TO_POSITION
    }

}