package us.brainstormz.robotTwo.tests

import com.acmerobotics.roadrunner.ftc.OverflowEncoder
import com.acmerobotics.roadrunner.ftc.RawEncoder
import com.qualcomm.robotcore.hardware.AnalogInput
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.hardware.PhoCRServo
import us.brainstormz.faux.hardware.PhoMotor
import us.brainstormz.faux.PhoOpMode
import us.brainstormz.faux.hardware.FauxRevBlinkinLedDriver
import us.brainstormz.faux.hardware.PhoAnalogInputController
import us.brainstormz.faux.hardware.PhoColorSensor
import us.brainstormz.faux.hardware.PhoDigitalChannel
import us.brainstormz.faux.hardware.PhoImu
import us.brainstormz.faux.hardware.PhoServo
import us.brainstormz.robotTwo.RobotTwoHardware

class PhoRobotTwoHardware(opmode: PhoOpMode, telemetry:Telemetry): RobotTwoHardware(opmode = opmode, telemetry = telemetry) {
    init {
        allHubs = listOf()

        extendoMotorMaster = PhoMotor()
        extendoMotorSlave = PhoMotor()
        liftMotorMaster = PhoMotor()
        liftMotorSlave = PhoMotor()
        lFDrive = PhoMotor()
        rFDrive = PhoMotor()
        lBDrive = PhoMotor()
        rBDrive = PhoMotor()

        hangReleaseServo = PhoCRServo()
        collectorServo1 = PhoCRServo()
        collectorServo2 = PhoCRServo()
        collectorServo2 = PhoCRServo()
        transferDirectorServo = PhoCRServo()
        leftTransferServo = PhoCRServo()
        rightTransferServo = PhoCRServo()
        armServo1 = PhoCRServo()
        armServo2 = PhoCRServo()

        rightClawServo = PhoServo()
        leftClawServo = PhoServo()


        armEncoder = AnalogInput(PhoAnalogInputController(), 0)
        leftRollerEncoder = AnalogInput(PhoAnalogInputController(), 0)
        rightRollerEncoder = AnalogInput(PhoAnalogInputController(), 0)

        leftTransferSensor = PhoColorSensor()
        rightTransferSensor = PhoColorSensor()

        liftMagnetLimit = PhoDigitalChannel()

        parallelEncoder = OverflowEncoder(RawEncoder(PhoMotor()))
        perpendicularEncoder = OverflowEncoder(RawEncoder(PhoMotor()))

        imu = PhoImu()

        lights = FauxRevBlinkinLedDriver()
    }
}

