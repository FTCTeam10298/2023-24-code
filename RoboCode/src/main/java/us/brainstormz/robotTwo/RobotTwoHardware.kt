package us.brainstormz.robotTwo

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevColorSensorV3
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.TouchSensor
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.hardwareClasses.EnhancedDCMotor
import us.brainstormz.hardwareClasses.MecOdometry
import us.brainstormz.hardwareClasses.SmartLynxModule
import java.lang.Thread.sleep

class RobotTwoHardware(val telemetry:Telemetry, val opmode: OpMode): MecOdometry {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    lateinit var liftMotorMaster: DcMotorEx
    lateinit var liftMotorSlave: DcMotor
    lateinit var liftMagnetLimit: TouchSensor

    lateinit var armServo1: CRServo
    lateinit var armServo2: CRServo
    lateinit var armEncoder: AnalogInput
    lateinit var encoderReader: AxonEncoderReader

    lateinit var leftClawServo: Servo
    lateinit var rightClawServo: Servo
    lateinit var topColorSensor: RevColorSensorV3
    lateinit var bottomColorSensor: RevColorSensorV3

    lateinit var extendoMotorMaster: DcMotorEx
    lateinit var extendoMotorSlave: DcMotor
    lateinit var extendoMagnetLimit: TouchSensor

    lateinit var collectorServo1: CRServo
    lateinit var collectorServo2: CRServo
    lateinit var leftCollectorPixelSensor: RevColorSensorV3
    lateinit var rightCollectorPixelSensor: RevColorSensorV3

    lateinit var leftTransferServo: CRServo
    lateinit var rightTransferServo: CRServo
    lateinit var leftTransferSensor: RevColorSensorV3
    lateinit var rightTransferSensor: RevColorSensorV3

    lateinit var hangReleaseServo: Servo
    override lateinit var lOdom: EnhancedDCMotor
    override lateinit var rOdom: EnhancedDCMotor
    override lateinit var cOdom: EnhancedDCMotor

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
//        collector =     exHub.getMotor(0)
//        lift =          exHub.getMotor(1)
//        hangRotator =   exHub.getMotor(2)
//        screw =         exHub.getMotor(3)
//
//        //Servos
//        autoClaw =      ctrlHub.getServo(0)
//        clawA =         ctrlHub.getServo(2)
//        leftArm =       ctrlHub.getServo(3)
//        rightArm =      ctrlHub.getServo(4)
//        launcher =      ctrlHub.getServo(5)

        // Drivetrain
        lFDrive.direction = DcMotorSimple.Direction.FORWARD
        rFDrive.direction = DcMotorSimple.Direction.REVERSE
        lBDrive.direction = DcMotorSimple.Direction.FORWARD
        rBDrive.direction = DcMotorSimple.Direction.REVERSE
        lFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rFDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        lBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        rBDrive.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
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