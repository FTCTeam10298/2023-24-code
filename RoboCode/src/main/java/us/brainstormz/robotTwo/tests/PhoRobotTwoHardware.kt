package us.brainstormz.robotTwo.tests

import com.qualcomm.robotcore.hardware.AnalogInput
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.pho.hardware.PhoCRServo
import us.brainstormz.pho.hardware.PhoMotor
import us.brainstormz.pho.PhoOpMode
import us.brainstormz.pho.hardware.PhoAnalogInputController
import us.brainstormz.pho.hardware.PhoColorSensor
import us.brainstormz.pho.hardware.PhoServo
import us.brainstormz.robotTwo.RobotTwoHardware

class PhoRobotTwoHardware(opmode: PhoOpMode, telemetry:Telemetry): RobotTwoHardware(opmode = opmode, telemetry = telemetry) {
    init {
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
    }
}

