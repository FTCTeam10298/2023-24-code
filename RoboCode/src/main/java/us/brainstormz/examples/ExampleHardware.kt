package us.brainstormz.examples

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.HardwareMap
import us.brainstormz.hardwareClasses.MecanumHardware

class ExampleHardware : MecanumHardware {
    override lateinit var lFDrive: DcMotor
    override lateinit var rFDrive: DcMotor
    override lateinit var lBDrive: DcMotor
    override lateinit var rBDrive: DcMotor
    override lateinit var hwMap: HardwareMap

    override fun init(ahwMap: HardwareMap) {

        hwMap = ahwMap

        // Drivetrain
        lFDrive = hwMap["lFDrive"] as DcMotor
        rFDrive = hwMap["rFDrive"] as DcMotor
        lBDrive = hwMap["lBDrive"] as DcMotor
        rBDrive = hwMap["rBDrive"] as DcMotor
    }

}