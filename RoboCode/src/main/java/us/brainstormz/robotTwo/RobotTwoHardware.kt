package us.brainstormz.robotTwo

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevColorSensorV3
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.ColorSensor
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.TouchSensor
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.hardwareClasses.MotorEncoderOnly
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.hardwareClasses.SmartLynxModule
import us.brainstormz.hardwareClasses.TwoWheelImuOdometry
import us.brainstormz.pid.PID
import java.lang.Thread.sleep

class RobotTwoHardware(private val telemetry:Telemetry, private val opmode: OpMode): MecanumHardware, TwoWheelImuOdometry {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    enum class LiftPositions(val position: Double) {
        Min(0.0),
        Transfer(0.5),
        BackboardBottomRow(1.0),
        SetLine1(2.0),
        SetLine2(3.0),
        SetLine3(4.0),
        Max(500.0)
    }
    val liftPositionPID = PID(kp = 1.0)
    lateinit var liftMotorMaster: DcMotorEx
    lateinit var liftMotorSlave: DcMotor
    lateinit var liftMagnetLimit: TouchSensor

    lateinit var armServo1: CRServo
    lateinit var armServo2: CRServo
    lateinit var armEncoder: AnalogInput


    enum class RightClawPosition(val position: Double) {
        Retracted(1.0),
        Gripping(0.4)
    }
    enum class LeftClawPosition(val position: Double) {
        Retracted(1.0),
        Gripping(0.56)
    }
    lateinit var leftClawServo: Servo
    lateinit var rightClawServo: Servo
    lateinit var leftColorSensor: RevColorSensorV3
    lateinit var rightColorSensor: RevColorSensorV3

    enum class ExtendoPositions(val position: Double) {
        Min(0.0),
        Max(500.0),
    }
    val extendoOperationRange = ExtendoPositions.Min.position..ExtendoPositions.Max.position
    val extendoPositionPID = PID(kp = 1.0)
    lateinit var extendoMotorMaster: DcMotorEx
    lateinit var extendoMotorSlave: DcMotor
    lateinit var extendoMagnetLimit: TouchSensor

    lateinit var collectorServo1: CRServo
    lateinit var collectorServo2: CRServo
    lateinit var leftCollectorPixelSensor: ColorSensor
    lateinit var rightCollectorPixelSensor: RevColorSensorV3

    lateinit var leftTransferServo: CRServo
    lateinit var rightTransferServo: CRServo
    lateinit var leftTransferSensor: RevColorSensorV3
    lateinit var rightTransferSensor: RevColorSensorV3
    //Aka throbber
    lateinit var transferDirectorServo: CRServo

    lateinit var hangReleaseServo: CRServo

    override lateinit var parallelOdom: MotorEncoderOnly
    override lateinit var perpendicularOdom: MotorEncoderOnly
    override lateinit var imu: IMU


    data class RobotState(
        val armPos: Arm.Positions,
        val liftPosition: LiftPositions,
        val leftClawPosition: LeftClawPosition,
        val rightClawPosition: RightClawPosition,
        val collectorState: Collector.CollectorPowers
    )

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
            //opmode.requestOpModeStop()
        }

        //Motors
        rFDrive =       ctrlHub.getMotor(0)
        rBDrive =       ctrlHub.getMotor(1)
        lBDrive =       ctrlHub.getMotor(2)
        lFDrive =       ctrlHub.getMotor(3)
        extendoMotorMaster =    exHub.getMotor(0)
        extendoMotorSlave =     exHub.getMotor(1)
        liftMotorMaster =       exHub.getMotor(2)
        liftMotorSlave =        exHub.getMotor(3)

        //Servos
        collectorServo1 =   ctrlHub.getCRServo(0)
        collectorServo2 =   ctrlHub.getCRServo(1)
        armServo1 = ctrlHub.getCRServo(2)
        armServo2 = ctrlHub.getCRServo(3)

        leftClawServo =     exHub.getServo(0)// left/right from driver 2 perspective when depositing
        rightClawServo =    exHub.getServo(1)
        rightTransferServo = exHub.getCRServo(2)
        leftTransferServo = exHub.getCRServo(3)
        transferDirectorServo = ctrlHub.getCRServo(5)

        hangReleaseServo = exHub.getCRServo(5)

        //Sensors
        armEncoder = ctrlHub.getAnalogInput(3)
        leftCollectorPixelSensor = hwMap["leftSensor"] as ColorSensor
        rightCollectorPixelSensor = hwMap["rightSensor"] as RevColorSensorV3
        imu = hwMap["imu"] as IMU
        parallelOdom = MotorEncoderOnly(ctrlHub.getMotor(0), reversed= false)
        perpendicularOdom = MotorEncoderOnly(ctrlHub.getMotor(3), reversed= false)

        // Drivetrain
        parallelOdom.motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        perpendicularOdom.motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

        lFDrive.direction = DcMotorSimple.Direction.FORWARD
        rFDrive.direction = DcMotorSimple.Direction.REVERSE
        lBDrive.direction = DcMotorSimple.Direction.FORWARD
        rBDrive.direction = DcMotorSimple.Direction.REVERSE
        lFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        rFDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        lBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        rBDrive.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        lFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        lBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        //Collector
        extendoMotorMaster.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        extendoMotorMaster.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        extendoMotorSlave.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        extendoMotorMaster.direction = DcMotorSimple.Direction.FORWARD
        extendoMotorSlave.direction = DcMotorSimple.Direction.REVERSE

        extendoMotorMaster.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        extendoMotorSlave.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        collectorServo1.direction = DcMotorSimple.Direction.REVERSE
        collectorServo2.direction = DcMotorSimple.Direction.FORWARD

        //Transfer
        rightTransferServo.direction = DcMotorSimple.Direction.FORWARD
        leftTransferServo.direction = DcMotorSimple.Direction.REVERSE


        //Lift
        liftMotorMaster.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        liftMotorMaster.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        liftMotorSlave.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        liftMotorMaster.direction = DcMotorSimple.Direction.REVERSE
        liftMotorSlave.direction = DcMotorSimple.Direction.FORWARD

        liftMotorMaster.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        liftMotorSlave.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        //Arm
        armServo1.direction = DcMotorSimple.Direction.REVERSE
        armServo2.direction = DcMotorSimple.Direction.FORWARD

        //Claw
        leftClawServo.direction = Servo.Direction.FORWARD
        rightClawServo.direction = Servo.Direction.FORWARD

        //IMU
        val parameters:IMU.Parameters = IMU.Parameters(RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.RIGHT,
                        RevHubOrientationOnRobot.UsbFacingDirection.DOWN))
        imu.initialize(parameters)
        imu.resetYaw()
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