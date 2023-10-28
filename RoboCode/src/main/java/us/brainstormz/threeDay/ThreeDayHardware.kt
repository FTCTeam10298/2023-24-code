package us.brainstormz.threeDay

import com.qualcomm.hardware.lynx.LynxModule
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


class ThreeDayHardware(val telemetry:Telemetry) : MecanumHardware {
    override lateinit var lFDrive: DcMotorEx
    override lateinit var rFDrive: DcMotorEx
    override lateinit var lBDrive: DcMotorEx
    override lateinit var rBDrive: DcMotorEx

    lateinit var collector: DcMotor

    lateinit var clawA: Servo
    val clawAClosedPos = 0.6
    val clawAOpenPos = 0.3
    lateinit var clawB: Servo
    val clawBClosedPos = 0.17
    val clawBOpenPos = 0.65

    lateinit var leftArm: Servo
    lateinit var rightArm: Servo

    lateinit var launcher: Servo

    lateinit var autoClaw: Servo
    val autoClawUp = 0.9
    val autoClawDown = 0.5

    lateinit var hangRotator: DcMotorEx
    enum class RotatorPos(val position:Int) {
        Rest(position = 0),
        LiftClearance(position = 180),
        StraightUp(position = 200)
    }

    lateinit var screw: DcMotor

    enum class ArmPos(val position:Double) {
        In(0.02),
        Out(0.66)
    }

    lateinit var lift: DcMotorEx
    enum class LiftPos(val position:Int) {
        Min(0),
        Grabbing(0),
        Collecting(0),
//        Collecting(80),
        ArmClearance(700),
        Low(800),
        Middle(1500),
        Max(1500)
    }

    override lateinit var hwMap: HardwareMap
    lateinit var ctrlHub: SmartLynxModule
    lateinit var exHub: SmartLynxModule

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
            throw Exception("Expansion Hub not found!")
        }

//        wiggleTest()


        //Motors
        lBDrive =       ctrlHub.getMotor(0)
        lFDrive =       ctrlHub.getMotor(1)
        rFDrive =       ctrlHub.getMotor(2)
        rBDrive =       ctrlHub.getMotor(3)
        collector =     exHub.getMotor(0)
        lift =          exHub.getMotor(1)
        hangRotator =   exHub.getMotor(2)
        screw =         exHub.getMotor(3)

        //Servos
        autoClaw =      ctrlHub.getServo(0)
        clawA =         ctrlHub.getServo(2)
        clawB =         exHub.getServo(1)
        leftArm =       ctrlHub.getServo(3)
        rightArm =      ctrlHub.getServo(4)
        launcher =      ctrlHub.getServo(5)

        // Drivetrain
//        lFDrive = hwMap["lFDrive"] as DcMotorEx
//        rFDrive = hwMap["rFDrive"] as DcMotorEx
//        lBDrive = hwMap["lBDrive"] as DcMotorEx
//        rBDrive = hwMap["rBDrive"] as DcMotorEx
        lFDrive.direction = DcMotorSimple.Direction.FORWARD
        rFDrive.direction = DcMotorSimple.Direction.REVERSE
        lBDrive.direction = DcMotorSimple.Direction.FORWARD
        rBDrive.direction = DcMotorSimple.Direction.REVERSE
        lFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        lBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

//        collector = hwMap["collector"] as DcMotor
        collector.direction = DcMotorSimple.Direction.REVERSE

//        clawA = hwMap["clawA"] as Servo
//        clawB = hwMap["clawB"] as Servo
        clawA.direction = Servo.Direction.REVERSE
        clawA.position = clawAOpenPos
        clawB.position = clawBOpenPos

//        leftArm = hwMap["leftArm"] as Servo
//        rightArm = hwMap["rightArm"] as Servo
        leftArm.direction = Servo.Direction.FORWARD
        rightArm.direction = Servo.Direction.REVERSE
        leftArm.position = ArmPos.In.position
        rightArm.position = ArmPos.In.position

//        lift = hwMap["lift"] as DcMotorEx
        lift.direction = DcMotorSimple.Direction.REVERSE
        lift.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        lift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        lift.targetPosition = 0
        lift.mode = DcMotor.RunMode.RUN_TO_POSITION
        lift.setPositionPIDFCoefficients(15.0)

//        autoClaw = hwMap["autoClaw"] as Servo
        autoClaw.direction = Servo.Direction.FORWARD
        autoClaw.position = autoClawUp

//        launcher = hwMap["launcher"] as Servo

//        hangRotator = hwMap["rotator"] as DcMotorEx
        hangRotator.direction = DcMotorSimple.Direction.REVERSE
        hangRotator.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        hangRotator.targetPosition = 0
        hangRotator.mode = DcMotor.RunMode.RUN_TO_POSITION
        hangRotator.setPositionPIDFCoefficients(20.0)
        hangRotator.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

//        screw = hwMap["screw"] as DcMotor
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