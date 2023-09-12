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

    data class DepoState(val liftState: LiftPos, val armState: ArmPos, val milisSinceStateChange: Long)
    var previousDepoState = DepoState(LiftPos.Down, ArmPos.In, 0)

    var previousArmPosition = ArmPos.In.position

    var previousLiftPosition = LiftPos.Down.position
    val liftWaitForArmTimeMilis = 800

    var clawAPosition = false
    var clawAButtonPrevious = false
    var clawBPosition = false
    var clawBButtonPrevious = false

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        telemetry.addLine("pid: ${hardware.lift.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION)}")
        telemetry.addLine("lift tolerance: ${hardware.lift.targetPositionTolerance}")
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

        //Depo
        val depoState = when {
            gamepad1.dpad_down -> {
//                retracted
                DepoState(LiftPos.Down, ArmPos.In, System.currentTimeMillis())
            }
            gamepad1.dpad_left -> {
//                low
                DepoState(LiftPos.Low, ArmPos.Out, System.currentTimeMillis())
            }
            gamepad1.dpad_up -> {
//                middle
                DepoState(LiftPos.Middle, ArmPos.Out, System.currentTimeMillis())
            }
            else -> {
                previousDepoState
            }
        }
        previousDepoState = depoState
        telemetry.addLine("Depo State: $depoState")

        //Claw
        if (gamepad1.right_bumper && !clawAButtonPrevious) clawAPosition = !clawAPosition else clawAPosition
        clawAButtonPrevious = gamepad1.right_bumper
        hardware.clawA.position =
            if (clawAPosition) {
                hardware.clawAOpenPos
            } else {
                hardware.clawAClosedPos
            }

        if (gamepad1.left_bumper && !clawBButtonPrevious) clawBPosition = !clawBPosition else clawBPosition
        clawBButtonPrevious = gamepad1.left_bumper
        hardware.clawB.position =
            if (clawBPosition) {
                hardware.clawBOpenPos
            } else {
                hardware.clawBClosedPos
            }

        //Lift
        when {
            gamepad2.right_stick_y !in -0.1..0.1 -> {
                telemetry.addLine("Manual lift")
                hardware.lift.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                hardware.lift.power = -gamepad2.right_stick_y.toDouble()
            }
            gamepad2.x -> {
//                reset
                telemetry.addLine("Zero lift")
                hardware.lift.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            }
            else -> {
                hardware.lift.mode = DcMotor.RunMode.RUN_TO_POSITION

                val timeSinceStateStarted = System.currentTimeMillis() - depoState.milisSinceStateChange
                hardware.lift.targetPosition =
                    if (depoState.liftState.position <= LiftPos.ArmClearance.position && timeSinceStateStarted < liftWaitForArmTimeMilis) {
                        telemetry.addLine("Lift waiting for arm. timeSinceStateStarted $timeSinceStateStarted")
                        previousLiftPosition
                    } else {
                        telemetry.addLine("Lift to depo position")
                        depoState.liftState.position
                    }

                val liftNeedsToGoDown = depoState.liftState.position < hardware.lift.targetPosition
                hardware.lift.power = if (liftNeedsToGoDown) 0.2 else 1.0
            }
        }
        previousLiftPosition = hardware.lift.targetPosition
        telemetry.addLine("Lift position: ${hardware.lift.currentPosition}")

        //Arm
        val armPosition = if (gamepad2.left_stick_y != 0.0f) {
            telemetry.addLine("Manual arm")
            gamepad2.left_stick_y.toDouble()
        } else if (hardware.lift.targetPosition < LiftPos.Low.position || hardware.lift.isBusy) {
            telemetry.addLine("Arm waiting for lift")
            previousArmPosition
        } else {
            telemetry.addLine("Arm to depo position")
            depoState.armState.position
        }
        previousArmPosition = armPosition

        hardware.leftArm.position = armPosition
        hardware.rightArm.position = armPosition

    }
}