package us.brainstormz.threeDay

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.PI

@Autonomous
class DriveEncoderTest: LinearOpMode() {

    val hardware = ThreeDayHardware()
    val console = TelemetryConsole(telemetry)
    val movement = EncoderDriveMovement(hardware, console)


    override fun runOpMode() {
        hardware.init(hardwareMap)
        waitForStart()

        val driveMotors = listOf<DcMotorEx>(hardware.lFDrive, hardware.rFDrive, hardware.lBDrive, hardware.rBDrive)
        telemetry.addLine("movement.INCHES_PER_DEGREE ${movement.INCHES_PER_DEGREE}")
        telemetry.addLine("movement.COUNTS_PER_INCH ${movement.COUNTS_PER_INCH}")
        telemetry.addLine("movement.COUNTS_PER_DEGREE ${movement.COUNTS_PER_DEGREE}")
        val degree = 90
        val inches = degree * movement.INCHES_PER_DEGREE
        val counts = inches * movement.COUNTS_PER_DEGREE
        telemetry.addLine("degree: $degree")
        telemetry.addLine("inches: $inches")
        telemetry.addLine("counts: $counts")
        telemetry.update()
        while(this.opModeIsActive()) {
            sleep(500)
        }
//        driveMotors.forEach { motor ->
//            motor.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
//            motor.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
//            motor.power = 0.5
//            val motorRunTimeMilis = 1000
//            val motorRunStartTimeMilis = System.currentTimeMillis()
//            while (System.currentTimeMillis() - motorRunStartTimeMilis < motorRunTimeMilis) {
//                val position = motor.currentPosition
//                telemetry.addLine("motor position: $position\nmotor.connectionInfo ${motor.connectionInfo}\nmotor.deviceName ${motor.deviceName}")
//                telemetry.update()
//            }
//            motor.power = 0.0
//        }
    }
}
