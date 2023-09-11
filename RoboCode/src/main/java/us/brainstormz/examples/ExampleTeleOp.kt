package us.brainstormz.examples

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import kotlin.math.abs

//@TeleOp
class ExampleTeleOp/** Change Depending on robot */: OpMode() {

    val hardware = ExampleHardware() /** Change Depending on robot */
    val movement = MecanumDriveTrain(hardware)

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
    }

    override fun loop() {
        /** TELE-OP PHASE */

        // DRONE DRIVE
        val yInput = gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = gamepad1.right_stick_x.toDouble()

        val y = -yInput
        val x = xInput
        val r = -rInput * abs(rInput)
        movement.driveSetPower((y + x - r),
                               (y - x + r),
                               (y - x - r),
                               (y + x + r))
    }
}