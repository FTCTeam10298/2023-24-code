package us.brainstormz.robotTwo.tests

import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
import com.qualcomm.robotcore.hardware.AnalogInput
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.hardware.FauxCRServo
import us.brainstormz.faux.hardware.FauxMotor
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.hardware.FauxRevBlinkinLedDriver
import us.brainstormz.faux.hardware.FauxAnalogInputController
import us.brainstormz.faux.hardware.FauxColorSensor
import us.brainstormz.faux.hardware.FauxDigitalChannel
import us.brainstormz.faux.hardware.FauxImu
import us.brainstormz.faux.hardware.FauxServo
import us.brainstormz.localizer.Localizer
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.subsystems.CollectorSystem
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.RobotTwoHardware

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
        transferDirectorServo = FauxCRServo()
        leftTransferServo = FauxCRServo()
        rightTransferServo = FauxCRServo()
        armServo1 = FauxCRServo()
        armServo2 = FauxCRServo()

        rightClawServo = FauxServo()
        leftClawServo = FauxServo()


        armEncoder = AnalogInput(FauxAnalogInputController(), 0)
        leftRollerEncoder = AnalogInput(FauxAnalogInputController(), 0)
        rightRollerEncoder = AnalogInput(FauxAnalogInputController(), 0)

        leftTransferSensor = FauxColorSensor()
        rightTransferSensor = FauxColorSensor()

        liftMagnetLimit = FauxDigitalChannel()

        parallelEncoder = OverflowEncoder(RawEncoder(FauxMotor()))
        perpendicularEncoder = OverflowEncoder(RawEncoder(FauxMotor()))

        imu = FauxImu()

        lights = FauxRevBlinkinLedDriver()
    }

    lateinit var actualRobot: ActualRobot
    override fun getActualState(localizer: Localizer, collectorSystem: CollectorSystem, depoManager: DepoManager): ActualRobot = actualRobot
}

