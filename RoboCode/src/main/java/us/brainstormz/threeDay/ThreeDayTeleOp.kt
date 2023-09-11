package us.brainstormz.threeDay

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.hardwareClasses.MecanumDriveTrain

@TeleOp
class ThreeDayTeleOp: OpMode() {

    val hardware = ThreeDayHardware()
    val movement = MecanumDriveTrain(hardware)



    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
    }

    override fun loop() {
        /** TELE-OP PHASE */

        // DRONE DRIVE
//        val yInput = gamepad1.left_stick_y.toDouble()
//        val xInput = gamepad1.left_stick_x.toDouble()
//        val rInput = gamepad1.right_stick_x.toDouble()
//
//        val y = -yInput
//        val x = xInput
//        val r = -rInput * abs(rInput)
//        movement.driveSetPower((y + x - r),
//                               (y - x + r),
//                               (y - x - r),
//                               (y + x + r))

        // Collector
        hardware.collector.power = gamepad1.right_trigger.toDouble() - gamepad1.left_trigger.toDouble()

        //Depositor
        hardware.clawA.position = if (gamepad1.right_bumper) hardware.clawOpenPos else hardware.clawClosedPos
        hardware.clawB.position = if (gamepad1.left_bumper) hardware.clawOpenPos else hardware.clawClosedPos

        hardware.leftArm.position = if (gamepad1.a)
            hardware.armOutPos
        else if (gamepad2.left_stick_y != 0.0f)
            gamepad2.left_stick_y.toDouble()
        else
            hardware.armInPos
    }
}