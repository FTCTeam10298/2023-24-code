package us.brainstormz.robotTwo

import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
//import com.outoftheboxrobotics.photoncore.hardware.motor.PhotonDcMotor
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.hardware.rev.RevColorSensorV3
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.ColorSensor
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import org.firstinspires.ftc.robotcore.external.Telemetry
import posePlanner.Point2D
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.hardwareClasses.SmartLynxModule
import us.brainstormz.hardwareClasses.TwoWheelImuOdometry
import us.brainstormz.localizer.Localizer
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import java.lang.Thread.sleep
import kotlin.math.PI

open class RobotTwoHardware(private val telemetry:Telemetry, private val opmode: OpMode): MecanumHardware, TwoWheelImuOdometry {

    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    companion object {
        val robotLengthInches = 17.75
        val robotWidthInches = 16.21457
        val tabCutoffCompensationInches = 0.5
        val redStartingXInches = -(72.0 - ((robotLengthInches/2) + tabCutoffCompensationInches))
        val redStartingRDegrees = -90.0
    }

    override lateinit var imu: IMU

    operator fun Point2D.times(other: Point2D): Point2D =
            Point2D(x= this.x * other.x, y= this.y * other.y)
    val mmToInchConversionMultiplier = 1/25.4
    val mmToInchConversionPointMultiplier = Point2D(mmToInchConversionMultiplier,mmToInchConversionMultiplier)

    override lateinit var parallelEncoder: OverflowEncoder
    val parOdomOffsetFromCenterMM = Point2D(x= 24.0, y= -81.51231)
    override val parallelOdomOffsetFromCenterInch = parOdomOffsetFromCenterMM * mmToInchConversionPointMultiplier
    override lateinit var perpendicularEncoder: OverflowEncoder
    val perpOdomOffsetFromCenterMM = Point2D(x= -24.0, y= -81.51231)
    override val perpendicularOdomOffsetFromCenterInch = perpOdomOffsetFromCenterMM * mmToInchConversionPointMultiplier

    val countsPerRotation = 4096
    val wheelDiameterMM = 35
    val wheelCircumferenceMM = PI * wheelDiameterMM
    val wheelCircumferenceInches = wheelCircumferenceMM * mmToInchConversionMultiplier
    val inchesPerTick = wheelCircumferenceInches/countsPerRotation

    lateinit var liftMotorMaster: DcMotorEx
    lateinit var liftMotorSlave: DcMotor
    lateinit var liftMagnetLimit: DigitalChannel

    lateinit var armServo1: CRServo
    lateinit var armServo2: CRServo
    lateinit var armEncoder: AnalogInput


    enum class RightClawPosition(val position: Double) {
        Retracted(1.0),
        Gripping(0.57)
    }
    enum class LeftClawPosition(val position: Double) {
        Retracted(1.0),
        Gripping(0.42)
    }
    lateinit var leftClawServo: Servo
    lateinit var rightClawServo: Servo
    lateinit var leftColorSensor: RevColorSensorV3
    lateinit var rightColorSensor: RevColorSensorV3

    val extendoOperationRange = Extendo.ExtendoPositions.Min.ticks..Extendo.ExtendoPositions.Max.ticks
    val extendoPositionPID = PID(kp = 1.0)
    lateinit var extendoMotorMaster: DcMotorEx
    lateinit var extendoMotorSlave: DcMotor
    lateinit var extendoMagnetLimit: DigitalChannel

    lateinit var collectorServo1: CRServo
    lateinit var collectorServo2: CRServo
//    lateinit var leftCollectorPixelSensor: ColorSensor
//    lateinit var rightCollectorPixelSensor: ColorSensor

    lateinit var leftTransferServo: CRServo
    lateinit var rightTransferServo: CRServo
    lateinit var leftTransferSensor: ColorSensor
    lateinit var rightTransferSensor: ColorSensor
    lateinit var leftRollerEncoder: AnalogInput
    lateinit var rightRollerEncoder: AnalogInput
    //Aka throbber
    lateinit var transferDirectorServo: CRServo

    enum class HangPowers(val power: Double) {
        Holding(0.0),
        Release(1.0)
    }
    lateinit var hangReleaseServo: CRServo

    enum class LauncherPosition(val position: Double) {
        Holding(1.0),
        Released(0.55)
    }
    lateinit var launcherServo: Servo

    lateinit var lights: RevBlinkinLedDriver

//
//    data class HardwareHalves (
//        val collectorServo: CRServo,
//        val transferServo: Servo,
//        val collectorSensor: ColorSensor,
//        val transferSensor: ColorSensor
//    )
//    lateinit var leftHalf: HardwareHalves
//    lateinit var rightHalf: HardwareHalves

    enum class Alliance {
        Red,
        Blue
    }

    object UnchangingRobotAttributes {
        var alliance = Alliance.Red
    }

    override lateinit var hwMap: HardwareMap
    lateinit var ctrlHub: SmartLynxModule
    lateinit var exHub: SmartLynxModule
    lateinit var allHubs: List<LynxModule>

    override fun init(ahwMap: HardwareMap) {

        hwMap = ahwMap

        allHubs = hwMap.getAll(LynxModule::class.java)
        print("modules.size ${allHubs.size}")
        for (lynx in allHubs) {
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
        lFDrive =       ctrlHub.getMotor(0)
        rFDrive =       ctrlHub.getMotor(3)
        lBDrive =       ctrlHub.getMotor(1)
        rBDrive =       ctrlHub.getMotor(2)
        extendoMotorMaster =    exHub.getMotor(0) //Has encoder
        extendoMotorSlave =     exHub.getMotor(1)
        liftMotorMaster =       exHub.getMotor(2) //Has encoder
        liftMotorSlave =        exHub.getMotor(3)

        //Servos
        collectorServo1 =   exHub.getCRServo(4)
        collectorServo2 =   ctrlHub.getCRServo(1)

        armServo1 = ctrlHub.getCRServo(3)
        armServo2 = ctrlHub.getCRServo(4)

        rightTransferServo = ctrlHub.getCRServo(5)
        leftTransferServo = exHub.getCRServo(3)
        transferDirectorServo = exHub.getCRServo(2)

        leftClawServo =     exHub.getServo(0)   // left/right from driver 2 perspective when depositing
        rightClawServo =    exHub.getServo(1)
        hangReleaseServo = exHub.getCRServo(5)

        launcherServo = ctrlHub.getServo(0)

        //Sensors
        armEncoder = ctrlHub.getAnalogInput(2)

        leftTransferSensor = hwMap["rightSensor"] as ColorSensor
        rightTransferSensor = hwMap["leftSensor"] as ColorSensor
        leftRollerEncoder = exHub.getAnalogInput(1)
        rightRollerEncoder = ctrlHub.getAnalogInput(0)

        liftMagnetLimit = ctrlHub.getDigitalController(6) as DigitalChannel

        imu = hwMap["imu"] as IMU

        val parallelOdomMotor = ctrlHub.getMotor(3)
        parallelEncoder = OverflowEncoder(RawEncoder(parallelOdomMotor))
        val perpendicularOdomMotor = ctrlHub.getMotor(0)
        perpendicularEncoder = OverflowEncoder(RawEncoder(perpendicularOdomMotor))

        lights = hwMap["lights"] as RevBlinkinLedDriver

        // Drivetrain
        parallelEncoder.direction = DcMotorSimple.Direction.REVERSE
        perpendicularEncoder.direction = DcMotorSimple.Direction.FORWARD
        parallelOdomMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        perpendicularOdomMotor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER

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

        extendoMotorMaster.direction = DcMotorSimple.Direction.REVERSE
        extendoMotorSlave.direction = DcMotorSimple.Direction.FORWARD

        extendoMotorMaster.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        extendoMotorSlave.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        collectorServo1.direction = DcMotorSimple.Direction.REVERSE
        collectorServo2.direction = DcMotorSimple.Direction.FORWARD

        //Transfer
        rightTransferServo.direction = DcMotorSimple.Direction.REVERSE
        leftTransferServo.direction = DcMotorSimple.Direction.FORWARD


        //Lift
        liftMotorMaster.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        liftMotorMaster.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        liftMotorSlave.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        liftMotorMaster.direction = DcMotorSimple.Direction.FORWARD
        liftMotorSlave.direction = DcMotorSimple.Direction.REVERSE

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
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.LEFT))
        imu.initialize(parameters)
        imu.resetYaw()

        //Lights
//        lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE)
    }


    private fun getLiftPos(power: Double): Lift.LiftPositions = Lift.LiftPositions.entries.firstOrNull { it ->
        power == it.ticks.toDouble()
    } ?: Lift.LiftPositions.Down

    open fun getActualState(localizer: Localizer, collectorSystem: CollectorSystem, depoManager: DepoManager): ActualRobot {
        return ActualRobot(
                positionAndRotation = localizer.currentPositionAndRotation(),
                collectorSystemState = collectorSystem.getCurrentState(this),
                depoState = depoManager.getDepoState(this)
        )
//        return ActualWorld(
//                actualRobot = actualRobot,
//                System.currentTimeMillis()
//        )
    }

    fun actuateRobot(
            targetState: TargetWorld, actualState: ActualWorld,
            movement: MecanumDriveTrain,
            extendo: Extendo,
            intake: Intake,
            transfer: Transfer,
            lift: Lift,
            arm: Arm,
            wrist: Wrist,
            extendoOverridePower: Double,
            liftOverridePower: Double,
            armOverridePower: Double
    ) {
        /**Drive*/
        movement.setSpeedAll(
                vY = targetState.targetRobot.positionAndRotation.y,
                vX = targetState.targetRobot.positionAndRotation.x,
                vA = targetState.targetRobot.positionAndRotation.r,
                maxPower = 1.0,
                minPower = 0.0
        )

        /**Extendo*/
        val extendoPower: Double = if (targetState.targetRobot.collectorTarget.extendoPositions == Extendo.ExtendoPositions.Manual) {
            extendoOverridePower
        } else if (targetState.targetRobot.collectorTarget.extendoPositions == Extendo.ExtendoPositions.ResetEncoder) {
            extendoMotorMaster.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            extendoMotorMaster.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            0.0
        } else {
            extendo.calcPowerToMoveExtendo(targetState.targetRobot.collectorTarget.extendoPositions.ticks, actualState.actualRobot)
        }
        extendo.powerSubsystem(extendoPower, this)

        /**Collector*/
        intake.powerSubsystem(targetState.targetRobot.collectorTarget.intakeNoodles.power, this)

        /**Rollers*/
        transfer.powerSubsystem(targetState.targetRobot.collectorTarget.rollers, this, actualRobot = actualState.actualRobot)

        /**Lift*/
        val liftPower: Double = when (targetState.targetRobot.depoTarget.liftPosition) {
            Lift.LiftPositions.Manual -> {
                telemetry.addLine("Running lift in manual mode at power $liftOverridePower")
                -liftOverridePower
            }
            Lift.LiftPositions.ResetEncoder -> {
                liftMotorMaster.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
                liftMotorMaster.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                0.0
            }
            else -> {
                telemetry.addLine("\n\n\nliftPositionTicks: " + actualState.actualRobot.depoState.liftPositionTicks)
                lift.calculatePowerToMoveToPosition(targetState.targetRobot.depoTarget.liftPosition.ticks, actualState.actualRobot.depoState.liftPositionTicks)
            }
        }
        telemetry.addLine("lift position: ${targetState.targetRobot.depoTarget.liftPosition}\n\n")
        telemetry.addLine("lift power: $liftPower\n\n")
        lift.powerSubsystem(liftPower, this)

        /**Arm*/
        val armPower: Double = if (targetState.targetRobot.depoTarget.armPosition == Arm.Positions.Manual) {
            armOverridePower
        } else {
            arm.calcPowerToReachTarget(targetState.targetRobot.depoTarget.armPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees)
        }
        arm.powerSubsystem(armPower, this)

        /**Claws*/
        //wrist.powerSubsystem(targetState.targetRobot.depoTarget.wristPosition, this)

        /**Hang*/
        hangReleaseServo.power = targetState.targetRobot.hangPowers.power

        /**Launcher*/
        launcherServo.position = targetState.targetRobot.launcherPosition.position

        /**Lights*/
        lights.setPattern(targetState.targetRobot.lights.targetColor.blinkinPattern)
    }

    fun wiggleTest(telemetry: Telemetry, gamepad: Gamepad) {
        telemetry.addLine("Wiggle test going")
        telemetry.update()

        //Motors
        val motorPortNumbers = 0..3
        val ctrlHubMotors = motorPortNumbers.map { i ->
            i to ctrlHub.getMotor(i)
        }
        val exHubMotors = motorPortNumbers.map { i ->
            i to exHub.getMotor(i)
        }

        //Servos


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


//        fun testServos(portNumber: Int, servo: Servo, hubName: String) {
//            servo.position = 1.0
////            while (!gamepad.b){
//                sleep(1000)
//                telemetry.addLine("Servo, $hubName port: $portNumber")
//                telemetry.update()
////            }
//            servo.position = 0.0
//            servo.close()
//            sleep(500)
//        }
//
        val servoPortNumbers = 0..5
//        val ctrlHubServos = servoPortNumbers.map { i ->
//            i to ctrlHub.getServo(i)
//        }
//        val exHubServos = servoPortNumbers.map { i ->
//            i to exHub.getServo(i)
//        }
//
//        ctrlHubServos.forEach{it ->
//            testServos(it.first, it.second, "ctrlHub")
//        }
//        exHubServos.forEach{it ->
//            testServos(it.first, it.second, "exHub")
//        }

        fun testCRServos(portNumber: Int, servo: CRServo, hubName: String) {
            telemetry.addLine("CRServo, $hubName port: $portNumber")
            telemetry.update()
            servo.power = 1.0
//            while (!gamepad.b){
            sleep(1000)
//            }
            servo.power = 0.0
            servo.close()
            sleep(2000)
        }

        val ctrlHubCRServos = servoPortNumbers.map { i ->
            i to ctrlHub.getCRServo(i)
        }
        val exHubCRServos = servoPortNumbers.map { i ->
            i to exHub.getCRServo(i)
        }
        ctrlHubCRServos.forEach{it ->
            testCRServos(it.first, it.second, "ctrlHub")
        }
        exHubCRServos.forEach{it ->
            testCRServos(it.first, it.second, "exHub")
        }
    }
}