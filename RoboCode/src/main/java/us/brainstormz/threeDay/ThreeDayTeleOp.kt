package us.brainstormz.threeDay

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.threeDay.ThreeDayHardware.ArmPos
import us.brainstormz.threeDay.ThreeDayHardware.LiftPos

@TeleOp
class ThreeDayTeleOp: OpMode() {

    val hardware = ThreeDayHardware()
    val movement = MecanumDriveTrain(hardware)

    data class DepoState(val liftState: LiftPos, val armState: ArmPos)
    var depoState = DepoState(LiftPos.Down, ArmPos.In)

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

        //Claw
        hardware.clawA.position =
                if (gamepad1.right_bumper) hardware.clawAOpenPos else hardware.clawAClosedPos
        hardware.clawB.position =
                if (gamepad1.left_bumper) hardware.clawBOpenPos else hardware.clawBClosedPos

        //Depo
        depoState = when {
            gamepad1.dpad_down -> {
//                retracted
                DepoState(LiftPos.Down, ArmPos.In)
            }
            gamepad1.dpad_left -> {
//                low
                DepoState(LiftPos.Low, ArmPos.Out)
            }
            gamepad1.dpad_up -> {
//                middle
                DepoState(LiftPos.Middle, ArmPos.Out)
            }
            else -> {
                depoState
            }
        }
        telemetry.addLine("Depo State: $depoState")

        //Arm
        val armPosition = if (gamepad2.left_stick_y != 0.0f) {
            telemetry.addLine("Manual arm")
            gamepad2.left_stick_y.toDouble()
        } else {
            telemetry.addLine("Arm to depo position")
            depoState.armState.position
        }

        hardware.leftArm.position = armPosition
        hardware.rightArm.position = armPosition

        //Lift
        when {
            gamepad2.right_stick_y != 0.0f -> {
                telemetry.addLine("Manual lift")
                hardware.lift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                hardware.lift.power = gamepad2.right_stick_y.toDouble() * LiftPos.Max.position
            }
            gamepad1.b -> {
//                reset
                telemetry.addLine("Zero lift")
                hardware.lift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            }
            else -> {
                telemetry.addLine("Lift to depo position")
                hardware.lift.mode = DcMotor.RunMode.RUN_TO_POSITION
                hardware.lift.targetPosition = depoState.liftState.position
            }
        }
    }
}