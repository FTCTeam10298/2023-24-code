package us.brainstormz.robotTwo

import android.util.Size
import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.Gamepad
import com.qualcomm.robotcore.hardware.HardwareMap
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.NormalizedColorSensor
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.configuration.LynxConstants
import org.firstinspires.ftc.robotcore.external.Telemetry
import posePlanner.Point2D
import us.brainstormz.hardwareClasses.MecanumHardware
import us.brainstormz.hardwareClasses.SmartLynxModule
import us.brainstormz.hardwareClasses.TwoWheelImuOdometry
import us.brainstormz.utils.measured
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.ColorReading
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons.AdafruitNeopixelSeesaw
import us.brainstormz.robotTwo.subsystems.readColor
import java.lang.Thread.sleep
import kotlin.math.PI

class WrappedColorSensor(val offset:Int, val sensor:NormalizedColorSensor){
    var lastReading: ColorReading? = null
    var readCycle = 1 + offset

    fun read(): ColorReading = measured("colorRead"){
        readCycle +=1
        val lastReading = this.lastReading
        if(lastReading==null || (readCycle%2 == 0)){
            println("Getting new reading for ${sensor.deviceName}")
            val c = measured("real-color-read"){readColor(sensor)}
            this.lastReading = c
            c
        }else{
            println("Recycling reading for ${sensor.deviceName}")
            lastReading
        }
    }
}


open class RobotTwoHardware(private val telemetry:Telemetry, private val opmode: OpMode): MecanumHardware, TwoWheelImuOdometry {

    val backCameraName = "Webcam 1"
    val backCameraResolution = Size(1920, 1080)

    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor

    companion object {
        val cameraBackwardOffsetFromRobotCenterInches = 8.35879
        val robotLengthInches = 17.75
        val robotWidthInches = 16.21457
        val tabCutoffCompensationInches = 0.5
        val redStartingXInches = -(72.0 - ((robotLengthInches/2) + tabCutoffCompensationInches))
        val redStartingRDegrees = 90.0
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
        Gripping(0.34)
    }
    enum class LeftClawPosition(val position: Double) {
        Retracted(1.0),
        Gripping(0.34)
    }
    lateinit var leftClawServo: CRServo
    lateinit var rightClawServo: CRServo
    lateinit var leftClawEncoder: AnalogInput
    lateinit var rightClawEncoder: AnalogInput
    lateinit var leftClawEncoderReader: AxonEncoderReader
    lateinit var rightClawEncoderReader: AxonEncoderReader

    lateinit var extendoMotorMaster: DcMotorEx
    lateinit var extendoMotorSlave: DcMotor
    lateinit var extendoMagnetLimit: DigitalChannel

    lateinit var collectorServo1: CRServo
    lateinit var collectorServo2: CRServo
    lateinit var dropDownServo: Servo


    lateinit var leftTransferServo: CRServo
    lateinit var rightTransferServo: CRServo

    lateinit var leftTransferUpperSensorWrapped:WrappedColorSensor
    lateinit var leftTransferUpperSensor: NormalizedColorSensor
    lateinit var rightTransferUpperSensorWrapped:WrappedColorSensor
    lateinit var rightTransferUpperSensor: NormalizedColorSensor

    lateinit var leftTransferLowerSensorWrapped:WrappedColorSensor
    lateinit var leftTransferLowerSensor: NormalizedColorSensor
    lateinit var rightTransferLowerSensorWrapped:WrappedColorSensor
    lateinit var rightTransferLowerSensor: NormalizedColorSensor

    enum class HangPowers(val power: Double) {
        Holding(0.0),
        Release(1.0)
    }
    lateinit var hangReleaseServo: CRServo

    enum class LauncherPosition(val position: Double) {
        Holding(0.0),
        Released(0.5)
    }
    lateinit var launcherServo: Servo

    //Neopixel Stuff
    lateinit var neopixelDriver: AdafruitNeopixelSeesaw
    val neopixelSystem = Neopixels()
    private var neoPixelActualState: Neopixels.StripState = neopixelSystem.makeEmptyLightState()

    enum class Alliance {
        Red,
        Blue
    }

    override lateinit var hwMap: HardwareMap
    lateinit var ctrlHub: SmartLynxModule
    lateinit var exHub: SmartLynxModule
    lateinit var allHubs: List<LynxModule>

    override fun init(ahwMap: HardwareMap) {

        hwMap = ahwMap

        allHubs = hwMap.getAll(LynxModule::class.java)
        val (ctrlHubLynx, exHubLynx) = allHubs.fold<LynxModule, Pair<LynxModule?, LynxModule?>>(null to null) {acc, lynx ->
            lynx.bulkCachingMode = LynxModule.BulkCachingMode.AUTO

            if (lynx.isParent && LynxConstants.isEmbeddedSerialNumber(lynx.serialNumber)) {
                println("ctrlHubFound: $lynx")
                acc.copy(first = lynx)

            } else {
                println("exHubFound: $lynx")
                acc.copy(second = lynx)

            }
        }
        ctrlHub = SmartLynxModule(ctrlHubLynx)
        exHub = SmartLynxModule(exHubLynx)

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
        extendoMotorMaster =    exHub.getMotor(1) //Has encoder
        extendoMotorSlave =     exHub.getMotor(0)
        liftMotorMaster =       exHub.getMotor(2) //Has encoder
        liftMotorSlave =        exHub.getMotor(3)

        //Servos
        collectorServo1 =   exHub.getCRServo(4)
        collectorServo2 =   exHub.getCRServo(1)

        armServo1 = ctrlHub.getCRServo(3)
        armServo2 = ctrlHub.getCRServo(4)

        rightTransferServo =    ctrlHub.getCRServo(5)
        leftTransferServo =     exHub.getCRServo(3)
        dropDownServo = exHub.getServo(2)

        leftClawServo =     ctrlHub.getCRServo(0)   // left/right from driver 2 perspective when depositing
        rightClawServo =    ctrlHub.getCRServo(1)

        hangReleaseServo = exHub.getCRServo(5)

        launcherServo = exHub.getServo(0)

        //Sensors
        armEncoder = ctrlHub.getAnalogInput(3)

        leftTransferLowerSensor = hwMap["rightSensor"] as NormalizedColorSensor
        leftTransferLowerSensorWrapped = WrappedColorSensor(1, leftTransferLowerSensor)
        rightTransferLowerSensor = hwMap["leftSensor"] as NormalizedColorSensor
        rightTransferLowerSensorWrapped = WrappedColorSensor(2, rightTransferLowerSensor)

        leftClawEncoder = ctrlHub.getAnalogInput(0)
        rightClawEncoder = ctrlHub.getAnalogInput(1)
        leftClawEncoderReader =     AxonEncoderReader(leftClawEncoder, angleOffsetDegrees = -80.0,  AxonEncoderReader.Direction.Reverse)
        rightClawEncoderReader =    AxonEncoderReader(rightClawEncoder, angleOffsetDegrees = -80.0, AxonEncoderReader.Direction.Forward)

        liftMagnetLimit = ctrlHub.getDigitalController(6) as DigitalChannel
        liftMagnetLimit.mode = DigitalChannel.Mode.INPUT

        extendoMagnetLimit = exHub.getDigitalController(6) as DigitalChannel

        imu = hwMap["imu"] as IMU

        val parallelOdomMotor = ctrlHub.getMotor(3)
        parallelEncoder = OverflowEncoder(RawEncoder(parallelOdomMotor))
        val perpendicularOdomMotor = ctrlHub.getMotor(0)
        perpendicularEncoder = OverflowEncoder(RawEncoder(perpendicularOdomMotor))

        //neopixel support
        neopixelDriver = hwMap["neo"] as AdafruitNeopixelSeesaw
        neopixelSystem.initialize(neopixelDriver)

        // Drivetrain
        parallelEncoder.direction = DcMotorSimple.Direction.REVERSE
        perpendicularEncoder.direction = DcMotorSimple.Direction.REVERSE
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

        extendoMotorMaster.direction = DcMotorSimple.Direction.FORWARD
        extendoMotorSlave.direction = DcMotorSimple.Direction.REVERSE

        extendoMotorMaster.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE
        extendoMotorSlave.zeroPowerBehavior = DcMotor.ZeroPowerBehavior.BRAKE

        collectorServo1.direction = DcMotorSimple.Direction.FORWARD
        collectorServo2.direction = DcMotorSimple.Direction.REVERSE

        //Transfer
        rightTransferServo.direction = DcMotorSimple.Direction.REVERSE
        leftTransferServo.direction = DcMotorSimple.Direction.FORWARD

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

        //launcherServo
        launcherServo.direction = Servo.Direction.REVERSE

        //Claw
        leftClawServo.direction = DcMotorSimple.Direction.FORWARD
        rightClawServo.direction = DcMotorSimple.Direction.REVERSE

        //IMU
        val parameters:IMU.Parameters = IMU.Parameters(RevHubOrientationOnRobot(
                        RevHubOrientationOnRobot.LogoFacingDirection.UP,
                        RevHubOrientationOnRobot.UsbFacingDirection.LEFT))
        imu.initialize(parameters)
        imu.resetYaw()
    }
    open fun getActualState(drivetrain: Drivetrain, collectorSystem: CollectorManager, depoManager: DepoManager, previousActualWorld: ActualWorld?): ActualRobot = measured("getActualState"){
        telemetry.addLine("getting state")
        telemetry.addLine("extendo current position: ${extendoMotorMaster.currentPosition}")
        ActualRobot(
                positionAndRotation = drivetrain.getPosition(),
                collectorSystemState = collectorSystem.getCurrentState(this, previousActualWorld),
                depoState = depoManager.getDepoState(this, previousActualWorld),
                neopixelState = neoPixelActualState
        )
    }

    fun actuateRobot(
            targetState: TargetWorld,
            previousTargetState: TargetWorld,
            actualState: ActualWorld,
            previousActualWorld: ActualWorld,
            drivetrain: Drivetrain,
            extendo: Extendo,
            intake: Intake,
            dropdown: Dropdown,
            transfer: Transfer,
            lift: Lift,
            arm: Arm,
            wrist: Wrist
    ) {
        /**Drive*/
        measured("drivetrain"){
            drivetrain.actuateDrivetrain(
                targetState.targetRobot.drivetrainTarget,
                previousTargetState.targetRobot.drivetrainTarget,
                actualState.actualRobot.positionAndRotation,
            )
        }

        /**Extendo*/
        measured("extendo") {
            val extendoPower: Double = when (targetState.targetRobot.collectorTarget.extendo.movementMode) {
                MovementMode.Position -> {
                    extendo.calcPowerToMoveExtendo(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot, previousActualRobot = previousActualWorld.actualRobot)
                }
                MovementMode.Power -> {
                    targetState.targetRobot.collectorTarget.extendo.power
                }
            }
            extendo.powerSubsystem(extendoPower, this)
        }

        /**Intake*/
        measured("intake") {
            intake.powerSubsystem(targetState.targetRobot.collectorTarget.intakeNoodles.power, this)
        }

        /**Dropdown*/
        measured("dropdown") {
            val dropdownPower: Double = when (targetState.targetRobot.collectorTarget.dropDown.movementMode) {
                MovementMode.Position -> {
                    targetState.targetRobot.collectorTarget.dropDown.targetPosition.position
                }
                MovementMode.Power -> {
                    targetState.targetRobot.collectorTarget.dropDown.power
                }
            }
            dropdown.powerSubsystem(dropdownPower, this)
        }

        /**Rollers*/
        measured("rollers") {
            transfer.powerSubsystem(targetState.targetRobot.collectorTarget.latches, this, actualRobot = actualState.actualRobot)
        }

        /**Lift*/
        measured("lift") {
            println("before lift")
            val liftPower: Double = when (targetState.targetRobot.depoTarget.lift.movementMode) {
                MovementMode.Position -> {
                    println("before lift position")
                    val power = lift.calculatePowerToMoveToPosition(targetState.targetRobot.depoTarget.lift.targetPosition.ticks, actualState.actualRobot.depoState.lift.currentPositionTicks)
                    println("after lift position")
                    power
                }
                MovementMode.Power -> {
                    println("before lift power")
                    telemetry.addLine("Running lift in manual mode at power ${targetState.targetRobot.depoTarget.lift.power}")
                    val power = targetState.targetRobot.depoTarget.lift.power
                    println("after lift power")
                    power
                }
            }
            println("after lift calc power")
            telemetry.addLine("lift position: ${targetState.targetRobot.depoTarget.lift.targetPosition}")
            telemetry.addLine("lift power: $liftPower\n\n")
            lift.powerSubsystem(liftPower, this)
            println("after lift apply power")
        }

        /**Arm*/

        measured("arm") {
    //        val armPower: Double = if (targetState.targetRobot.depoTarget.armPosition == Arm.Positions.Manual) {
    //            targetState.targetRobot.depoTarget.armPosition.
    //        } else {
    //            arm.calcPowerToReachTarget(targetState.targetRobot.depoTarget.armPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees)
    //        }
            val armPower: Double = when (targetState.targetRobot.depoTarget.armPosition.movementMode) {
                MovementMode.Position -> {
                    arm.calcPowerToReachTarget(targetState.targetRobot.depoTarget.armPosition.targetPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees)
                }
                MovementMode.Power -> {
                    targetState.targetRobot.depoTarget.armPosition.power
                }
            }
            arm.powerSubsystem(armPower, this)
        }

        /**Claws*/
        measured("claws") {
            wrist.powerSubsystem(targetState.targetRobot.depoTarget.wristPosition, actualState.actualRobot.depoState.wristAngles,this)
    //        val leftClawPosition: RobotTwoHardware.LeftClawPosition = when (targetState.targetRobot.depoTarget.wristPosition.left) {
    //            Claw.ClawTarget.Gripping -> RobotTwoHardware.LeftClawPosition.Gripping
    //            Claw.ClawTarget.Retracted -> RobotTwoHardware.LeftClawPosition.Retracted
    //        }
    //        leftClawServo.position = leftClawPosition.position
    //
    //        val rightClawPosition: RobotTwoHardware.RightClawPosition = when (targetState.targetRobot.depoTarget.wristPosition.right) {
    //            Claw.ClawTarget.Gripping -> RobotTwoHardware.RightClawPosition.Gripping
    //            Claw.ClawTarget.Retracted -> RobotTwoHardware.RightClawPosition.Retracted
    //        }
    //        rightClawServo.position = rightClawPosition.position
        }

        /**Hang*/
        measured("hang") {
            hangReleaseServo.power = targetState.targetRobot.hangPowers.power
        }

        /**Launcher*/
        measured("launcher") {
            launcherServo.position = targetState.targetRobot.launcherPosition.position
        }

        /**Lights*/
        measured("lights"){
    //        lights.setPattern(targetState.targetRobot.lights.targetColor)

            neoPixelActualState = neopixelSystem.writeQuicklyFromCenter(30, targetState.targetRobot.lights.stripTarget, actualState.actualRobot.neopixelState, neopixelDriver)
        }
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