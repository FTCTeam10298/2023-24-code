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
    val clawAClosedPos = 0.25
    val clawAOpenPos = 0.83
    lateinit var clawB: Servo
    val clawBClosedPos = 0.17
    val clawBOpenPos = 0.65

    lateinit var leftArm: Servo
    lateinit var rightArm: Servo
    enum class ArmPos(val position:Double) {
        In(0.02),
        Out(0.63)
    }

    lateinit var lift: DcMotorEx
    enum class LiftPos(val position:Int) {
        Min(0),
        Grabbing(0),
        Collecting(60),
        ArmClearance(600),
        Low(750),
        Middle(1500),
        Max(1500)
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
        clawA.position = clawAOpenPos
        clawB.position = clawBOpenPos

        leftArm = hwMap["leftArm"] as Servo
        rightArm = hwMap["rightArm"] as Servo
        leftArm.direction = Servo.Direction.FORWARD
        rightArm.direction = Servo.Direction.REVERSE
        leftArm.position = ArmPos.In.position
        rightArm.position = ArmPos.In.position

        lift = hwMap["lift"] as DcMotorEx
        lift.direction = DcMotorSimple.Direction.REVERSE
        lift.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        lift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        lift.targetPosition = 0
        lift.mode = DcMotor.RunMode.RUN_TO_POSITION
        lift.setPositionPIDFCoefficients(15.0)
    }

}