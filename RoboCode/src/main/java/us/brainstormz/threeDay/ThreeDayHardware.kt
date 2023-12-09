package us.brainstormz.threeDay

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.hardwareClasses.SmartLynxModule
import java.lang.Thread.sleep

class ThreeDayHardware(val telemetry:Telemetry, val opmode: OpMode) : MecanumHardware {
    override lateinit var lFDrive: DcMotorEx
    override lateinit var rFDrive: DcMotorEx
    override lateinit var lBDrive: DcMotorEx
    override lateinit var rBDrive: DcMotorEx

    lateinit var collector: DcMotor

    lateinit var clawA: Servo
    enum class GatePosition(val position:Double) {
        Deposit(0.0),
        Closed(0.5),
        Intake(0.8)
    }

    lateinit var leftArm: Servo
    lateinit var rightArm: Servo

    lateinit var launcher: Servo

    lateinit var autoClaw: Servo
    enum class AutoClawPos(val position: Double) {
        Up(0.9),
        Down(0.03)
    }

    lateinit var hangRotator: DcMotorEx
    enum class RotatorPos(val position:Int) {
        Rest(position = 120),
        LiftClearance(position = 410),
        StraightUp(position = 475)
    }

    lateinit var screw: DcMotor

    enum class ArmPos(val position:Double) {
        In(0.02),
        Out(0.64)
    }

    lateinit var lift: DcMotorEx
    val liftWaitForArmTimeMilis = 800
    enum class LiftPos(val position:Int) {
        Min(0),
        Grabbing(0),
        Collecting(0),
        ArmClearance(700),
        Low(1100),
        High(1500),
        Max(1500)
    }

    override lateinit var hwMap: HardwareMap
    lateinit var ctrlHub: SmartLynxModule
    lateinit var exHub: SmartLynxModule

    lateinit var allUsedMotors: List<DcMotor>
    override fun init(ahwMap: HardwareMap) {

        hwMap = ahwMap

        val modules = hwMap.getAll(LynxModule::class.java)
        print("modules.size ${modules.size}")
        for (lynx in modules) {
            lynx.bulkCachingMode = LynxModule.BulkCachingMode.AUTO
            if (lynx.isParent && LynxConstants.isEmbeddedSerialNumber(lynx.serialNumber)) {
                println("ctrlHubFound: $lynx")

                ctrlHub = SmartLynxModule(lynx)
            } else {
                println("exHubFound: $lynx")

                exHub = SmartLynxModule(lynx)
            }
        }
        if (!this::exHub.isInitialized) {
            telemetry.addLine("Expansion Hub not found!")
            telemetry.update()
            opmode.requestOpModeStop()
        }

        //Motors
        lBDrive =       ctrlHub.getMotor(0)
        lFDrive =       ctrlHub.getMotor(1)
        rFDrive =       ctrlHub.getMotor(2)
        rBDrive =       ctrlHub.getMotor(3)
        collector =     exHub.getMotor(0)
        lift =          exHub.getMotor(1)
        hangRotator =   exHub.getMotor(2)
        screw =         exHub.getMotor(3)
        allUsedMotors = listOf(lFDrive, lBDrive, rFDrive, rBDrive, collector, hangRotator, screw, lift)

        //Servos
        autoClaw =      ctrlHub.getServo(0)
        clawA =         ctrlHub.getServo(2)
        leftArm =       ctrlHub.getServo(3)
        rightArm =      ctrlHub.getServo(4)
        launcher =      ctrlHub.getServo(5)

        // Drivetrain
        lFDrive.direction = DcMotorSimple.Direction.FORWARD
        rFDrive.direction = DcMotorSimple.Direction.REVERSE
        lBDrive.direction = DcMotorSimple.Direction.FORWARD
        rBDrive.direction = DcMotorSimple.Direction.REVERSE
        lFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        lBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        collector.direction = DcMotorSimple.Direction.REVERSE

        clawA.direction = Servo.Direction.REVERSE
        clawA.position = GatePosition.Closed.position

        launcher.direction = Servo.Direction.REVERSE

        leftArm.direction = Servo.Direction.FORWARD
        rightArm.direction = Servo.Direction.REVERSE
        leftArm.position = ArmPos.In.position
        rightArm.position = ArmPos.In.position

        lift.direction = DcMotorSimple.Direction.REVERSE
        lift.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        lift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        lift.targetPosition = 0
        lift.mode = DcMotor.RunMode.RUN_TO_POSITION
        lift.setPositionPIDFCoefficients(10.0)

        autoClaw.direction = Servo.Direction.FORWARD
        autoClaw.position = AutoClawPos.Up.position

        hangRotator.direction = DcMotorSimple.Direction.REVERSE
        hangRotator.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        hangRotator.targetPosition = 0
        hangRotator.mode = DcMotor.RunMode.RUN_TO_POSITION
        hangRotator.setPositionPIDFCoefficients(18.0)
        hangRotator.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.FLOAT

        screw.direction = DcMotorSimple.Direction.REVERSE
        screw.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        screw.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
    }


    fun wiggleTest() {
        print("Wiggle test going")

        //Motors
        val motorPortNumbers = 0..3
        val ctrlHubMotors = motorPortNumbers.map { i ->
            i to ctrlHub.getMotor(i)
        }
        val exHubMotors = motorPortNumbers.map { i ->
            i to exHub.getMotor(i)
        }

        //Servos
        val servoPortNumbers = 0..5
        val ctrlHubServos = servoPortNumbers.map { i ->
            i to ctrlHub.getServo(i)
        }
        val exHubServos = servoPortNumbers.map { i ->
            i to exHub.getServo(i)
        }


        //Test
        val movementDelayMs:Long = 500
        val inbetweenDelayMs:Long = 4000

        //bl, fl, fr, br, smth, lift, rotator, screw
//        ctrlHubMotors.forEach{it ->
//            val portNumber = it.first
//            val motor = it.second
//            print("HWMAP ctrlHubMotor: $portNumber")
//            motor.power = 0.2
//            sleep(movementDelayMs)
//            motor.power = 0.0
//            sleep(inbetweenDelayMs)
//        }
//
//        exHubMotors.forEach{it ->
//            val portNumber = it.first
//            val motor = it.second
//            print("HWMAP exHubMotor: $portNumber")
//            motor.power = 0.2
//            sleep(movementDelayMs)
//            motor.power = 0.0
//            sleep(inbetweenDelayMs)
//        }

        /*

        0auto
        1
        2trapdooor
        3
        4arm
        5arm?

        0
        1
        2
        3
        4
        5

        */
        ctrlHubServos.forEach{it ->
            val portNumber = it.first
            val servo = it.second
            println("HWMAP ctrlHubServo: $portNumber")
            servo.position = 0.2
            sleep(movementDelayMs)
            servo.position = 0.0
            servo.close()
            sleep(inbetweenDelayMs)
        }
        exHubServos.forEach{it ->
            val portNumber = it.first
            val servo = it.second
            println("HWMAP exHubServo: $portNumber")
            servo.position = 0.2
            sleep(movementDelayMs)
            servo.position = 0.0
            servo.close()
            sleep(inbetweenDelayMs)
        }
    }
}