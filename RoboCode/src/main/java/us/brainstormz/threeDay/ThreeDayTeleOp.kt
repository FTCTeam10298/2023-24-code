package us.brainstormz.threeDay

import androidx.core.graphics.rotationMatrix
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.threeDay.ThreeDayHardware.ArmPos
import us.brainstormz.threeDay.ThreeDayHardware.LiftPos
import kotlin.math.abs
import kotlin.math.pow

@TeleOp
class ThreeDayTeleOp: OpMode() {

    val hardware = ThreeDayHardware(telemetry)
    val movement = MecanumDriveTrain(hardware)

    data class DepoTarget(val liftTarget: LiftPos, val armTarget: ArmPos, val milisSinceStateChange: Long)
    var previousDepoState = DepoTarget(LiftPos.Collecting, ArmPos.In, 0)

    var previousArmPosition = previousDepoState.armTarget.position

    var previousLiftPosition = previousDepoState.liftTarget.position
    val liftWaitForArmTimeMilis = 800

    data class ClawTarget(val clawA: Boolean, val clawB: Boolean)
    var clawTarget = ClawTarget(clawA = true, clawB = true)
    var clawAButtonPrevious = true
    var clawBButtonPrevious = true


    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        telemetry.addLine("pid: ${hardware.lift.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION)}")
        telemetry.addLine("lift tolerance: ${hardware.lift.targetPositionTolerance}")
    }

    override fun loop() {
        /** TELE-OP PHASE */
        //x is slow, keep arm up when driving, up more when gamepad 2 a,

        telemetry.addLine("motors: ${hardware.hwMap.getAll(DcMotor::class.java).map { it as DcMotorEx; it.getCurrent(CurrentUnit.MILLIAMPS)}}")

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

        // Collector
        hardware.collector.power = gamepad1.right_trigger.toDouble() - gamepad1.left_trigger.toDouble()


        //Depo
        val depoState = when {
            gamepad1.dpad_down || gamepad2.dpad_down -> {
//                retracted
                DepoTarget(LiftPos.Collecting, ArmPos.In, System.currentTimeMillis())
            }
            gamepad1.dpad_left || gamepad2.dpad_left -> {
//                low
                DepoTarget(LiftPos.Low, ArmPos.Out, System.currentTimeMillis())
            }
            gamepad1.dpad_up || gamepad2.dpad_up -> {
//                middle
                DepoTarget(LiftPos.Middle, ArmPos.Out, System.currentTimeMillis())
            }
            gamepad1.x -> {
                DepoTarget(LiftPos.Grabbing, ArmPos.In, System.currentTimeMillis())
            }
            else -> {
                previousDepoState
            }
        }
        previousDepoState = depoState
        telemetry.addLine("Depo State: $depoState")

        //Claw
//        clawTarget = if (!gamepad1.right_bumper) {
//            when (depoState.liftTarget) {
////                LiftPos.Grabbing -> {
////                    if (hardware.lift.currentPosition <= LiftPos.Grabbing.position) {
////                        ClawTarget(clawA = false, clawB = false)
////                    } else {
////                        ClawTarget(clawA = true, clawB = true)
////                    }
////                }
////                LiftPos.Collecting -> {
////                    ClawTarget(clawA = true, clawB = true)
////                }
//                else -> {
//                    clawTarget
//                }
//            }
//        } else {
            val clawAPosition = if ((gamepad1.right_bumper || gamepad2.right_bumper) && !clawAButtonPrevious) {
                !clawTarget.clawA
            }else if (hardware.collector.power != 0.0) {
                true
            }else {
                clawTarget.clawA
            }
            clawAButtonPrevious = gamepad1.right_bumper || gamepad2.right_bumper


//            val clawBPosition = if (gamepad1.left_bumper && !clawBButtonPrevious)
//                !clawTarget.clawB
//            else
//                clawTarget.clawB
//            clawBButtonPrevious = gamepad1.left_bumper

            clawTarget = ClawTarget(clawA = clawAPosition, clawB = clawTarget.clawB)
//        }

        hardware.clawA.position =
            if (clawTarget.clawA) {
                hardware.clawAOpenPos
            } else {
                hardware.clawAClosedPos
            }
//        hardware.clawB.position =
//            if (clawTarget.clawB) {
//                hardware.clawBOpenPos
//            } else {
//                hardware.clawBClosedPos
//            }

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
                    if ((depoState.liftTarget.position <= LiftPos.ArmClearance.position && timeSinceStateStarted < liftWaitForArmTimeMilis) || hardware.hangRotator.isBusy) {
                        telemetry.addLine("Lift waiting for arm. timeSinceStateStarted $timeSinceStateStarted")
                        previousLiftPosition
                    } else {
                        telemetry.addLine("Lift to depo position")
                        depoState.liftTarget.position
                    }

                val liftNeedsToGoDown = depoState.liftTarget.position < hardware.lift.targetPosition
                hardware.lift.power = if (liftNeedsToGoDown) 0.2 else 1.0
            }
        }
        previousLiftPosition = hardware.lift.targetPosition
        telemetry.addLine("Lift position: ${hardware.lift.currentPosition}")

        //Arm
        val armPosition = if (gamepad2.right_stick_x != 0.0f) {
            telemetry.addLine("Manual arm")
            previousArmPosition + (gamepad2.left_stick_y.toDouble() * 0.08)
        } else if (hardware.lift.targetPosition < LiftPos.ArmClearance.position || hardware.lift.currentPosition < LiftPos.ArmClearance.position) { //jank
            telemetry.addLine("Arm waiting for lift")
            previousArmPosition
        } else {
            telemetry.addLine("Arm to depo position")
            depoState.armTarget.position
        }
        previousArmPosition = armPosition

        hardware.leftArm.position = armPosition
        hardware.rightArm.position = armPosition

        //launcher
        if (gamepad1.y || gamepad2.y) {
            hardware.launcher.position = 0.9
        } else {
            hardware.launcher.position = 0.6
        }

        //hang
        hardware.screw.power = gamepad2.left_stick_y.toDouble()

        if (hardware.lift.currentPosition > LiftPos.ArmClearance.position || depoState.liftTarget.position > LiftPos.ArmClearance.position){
            hardware.hangRotator.targetPosition = ThreeDayHardware.RotatorPos.LiftClearance.position //up position
            hardware.hangRotator.power = 1.0
        } else if (gamepad2.a) {
            hardware.hangRotator.targetPosition = ThreeDayHardware.RotatorPos.StraightUp.position
            hardware.hangRotator.power = 1.0
        } else if (hardware.collector.power != 0.0) {
            hardware.hangRotator.targetPosition = ThreeDayHardware.RotatorPos.LiftClearance.position
            hardware.hangRotator.power = 1.0
        } else {
            hardware.hangRotator.power = 0.0 //down position
        }
        telemetry.addLine("hangRotatorPosition " + hardware.hangRotator.targetPosition)

    }
}