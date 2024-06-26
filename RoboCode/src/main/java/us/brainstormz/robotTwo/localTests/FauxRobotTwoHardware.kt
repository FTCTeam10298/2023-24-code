package us.brainstormz.robotTwo.localTests

import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
import com.qualcomm.robotcore.hardware.AnalogInput
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.hardware.FauxCRServo
import us.brainstormz.faux.hardware.FauxMotor
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.hardware.FauxAnalogInputController
import us.brainstormz.faux.hardware.FauxColorSensor
import us.brainstormz.faux.hardware.FauxDevice
import us.brainstormz.faux.hardware.FauxDigitalChannel
import us.brainstormz.faux.hardware.FauxI2cDeviceSynch
import us.brainstormz.faux.hardware.FauxImu
import us.brainstormz.faux.hardware.FauxServo
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.CollectorManager
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.WrappedColorSensor
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons.AdafruitNeopixelSeesaw

class FauxRobotTwoHardware(opmode: FauxOpMode, telemetry:Telemetry): RobotTwoHardware(opmode = opmode, telemetry = telemetry) {
    init {
        allHubs = listOf()

        extendoMotorMaster = FauxMotor()
        extendoMotorSlave = FauxMotor()
        liftMotorMaster = FauxMotor()
        liftMotorSlave = FauxMotor()
        lFDrive = FauxMotor()
        rFDrive = FauxMotor()
        lBDrive = FauxMotor()
        rBDrive = FauxMotor()

        hangReleaseServo = FauxCRServo()
        collectorServo1 = FauxCRServo()
        collectorServo2 = FauxCRServo()
        collectorServo2 = FauxCRServo()
        leftTransferServo = FauxCRServo()
        rightTransferServo = FauxCRServo()
        armServo1 = FauxCRServo()
        armServo2 = FauxCRServo()

        rightClawServo = FauxCRServo()
        leftClawServo = FauxCRServo()
        launcherServo = FauxServo()


        dropDownServo = FauxServo()

        neopixelDriver = AdafruitNeopixelSeesaw(FauxI2cDeviceSynch(), false)

        armEncoder = AnalogInput(FauxAnalogInputController(), 0)
//        leftRollerEncoder = AnalogInput(FauxAnalogInputController(), 0)
//        rightRollerEncoder = AnalogInput(FauxAnalogInputController(), 0)

        leftTransferUpperSensor = FauxColorSensor()
        leftTransferUpperSensorWrapped = WrappedColorSensor(1, leftTransferUpperSensor)
        rightTransferUpperSensor = FauxColorSensor()
        rightTransferUpperSensorWrapped = WrappedColorSensor(2, leftTransferUpperSensor)

        liftMagnetLimit = FauxDigitalChannel()

        parallelEncoder = OverflowEncoder(RawEncoder(FauxMotor()))
        perpendicularEncoder = OverflowEncoder(RawEncoder(FauxMotor()))

        imu = FauxImu()

//        lights = FauxRevBlinkinLedDriver()
    }

    lateinit var actualRobot: ActualRobot
    override fun getActualState(drivetrain: Drivetrain, collectorSystem: CollectorManager, depoManager: DepoManager, previousActualWorld: ActualWorld?): ActualRobot = actualRobot
}

