package us.brainstormz.threeDay

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.threeDay.ThreeDayHardware.ArmPos
import us.brainstormz.threeDay.ThreeDayHardware.LiftPos
import kotlin.math.abs
import us.brainstormz.threeDay.ThreeDayHardware.GatePosition
import kotlin.math.absoluteValue

@TeleOp
class ThreeDayTeleOp: OpMode() {

    val hardware = ThreeDayHardware(telemetry,this)
    val movement = MecanumDriveTrain(hardware)

    data class DepoTarget(val liftTarget: LiftPos, val armTarget: ArmPos, val milisSinceStateChange: Long, val gateTarget: GatePosition)
    var previousDepoState = DepoTarget(LiftPos.Collecting, ArmPos.In, 0, GatePosition.Closed)

    var previousArmPosition = previousDepoState.armTarget.position

    var previousLiftPosition = previousDepoState.liftTarget.position

    var eitherBumperWasPressedLastLoop = true


    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        telemetry.addLine("pid: ${hardware.lift.getPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION)}")
        telemetry.addLine("lift tolerance: ${hardware.lift.targetPositionTolerance}")
    }

    override fun loop() {
        /** TELE-OP PHASE */
        //x is slow, keep arm up when driving, up more when gamepad 2 a,

        telemetry.addLine(
            "motors: ${
                hardware.hwMap.getAll(DcMotor::class.java)
                    .map { it as DcMotorEx; it.getCurrent(CurrentUnit.MILLIAMPS) }
            }"
        )

        // DRONE DRIVE
        val yInput = gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = gamepad1.right_stick_x.toDouble()

        val y = -yInput
        val x = xInput
        val r = -rInput * abs(rInput)
        movement.driveSetPower(
            (y + x - r),
            (y - x + r),
            (y - x - r),
            (y + x + r)
        )

        // Collector
        hardware.collector.power =
            gamepad1.right_trigger.toDouble() - gamepad1.left_trigger.toDouble()


        //Depo
        val depoState = when {
            gamepad1.dpad_down || gamepad2.dpad_down -> {
//                retracted
                DepoTarget(
                    LiftPos.Collecting,
                    ArmPos.In,
                    System.currentTimeMillis(),
                    GatePosition.Closed
                )
            }

            gamepad1.dpad_up || gamepad2.dpad_up -> {
//                middle
                DepoTarget(
                    LiftPos.High,
                    ArmPos.Out,
                    System.currentTimeMillis(),
                    GatePosition.Closed
                )
            }

            gamepad1.x -> {
                DepoTarget(
                    LiftPos.Grabbing,
                    ArmPos.In,
                    System.currentTimeMillis(),
                    GatePosition.Closed
                )
            }

            else -> {
                previousDepoState
            }
        }

        //Claw
        val collectorIsOn = hardware.collector.power != 0.0
        val liftIsMoving = (previousDepoState.liftTarget != depoState.liftTarget)
        val eitherBumperIsPressed = (gamepad1.right_bumper || gamepad2.right_bumper)
        val desiredGatePosition = if (eitherBumperIsPressed && !eitherBumperWasPressedLastLoop) {
            when (depoState.gateTarget) {
                GatePosition.Intake -> GatePosition.Closed
                GatePosition.Deposit -> GatePosition.Closed
                GatePosition.Closed -> {
                    when (depoState.liftTarget) {
                        LiftPos.High -> GatePosition.Deposit
                        else -> GatePosition.Intake
                    }
                }
            }
        } else if (collectorIsOn) {
            GatePosition.Intake
        } else if (liftIsMoving) {
            val message = "Gate detected lift moving so it closed the gate"
            telemetry.addLine(message)
            println(message + System.currentTimeMillis())
            GatePosition.Closed
        } else {
            depoState.gateTarget
        }
        eitherBumperWasPressedLastLoop = gamepad1.right_bumper || gamepad2.right_bumper
        previousDepoState = previousDepoState.copy(gateTarget = desiredGatePosition)

        println("targets: lift  ${depoState.liftTarget} gate  ${depoState.gateTarget} ")
        hardware.clawA.position = depoState.gateTarget.position

        //Lift
        when {
            gamepad2.right_stick_y.absoluteValue > 0.5 -> {
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

                val timeSinceStateStarted =
                    System.currentTimeMillis() - depoState.milisSinceStateChange
                hardware.lift.targetPosition =
                    if ((depoState.liftTarget.position <= LiftPos.ArmClearance.position && timeSinceStateStarted < hardware.liftWaitForArmTimeMilis) || hardware.hangRotator.isBusy) {
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
        if (gamepad2.right_stick_button) {
            hardware.hangRotator.power = 0.0
            hardware.hangRotator.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        } else if (hardware.lift.currentPosition > LiftPos.ArmClearance.position || depoState.liftTarget.position > LiftPos.ArmClearance.position){
            hardware.hangRotator.mode = DcMotor.RunMode.RUN_TO_POSITION
            hardware.hangRotator.targetPosition = ThreeDayHardware.RotatorPos.LiftClearance.position //up position
            hardware.hangRotator.power = 1.0
        } else if (gamepad2.a) {
            hardware.hangRotator.mode = DcMotor.RunMode.RUN_TO_POSITION
            hardware.hangRotator.targetPosition = ThreeDayHardware.RotatorPos.StraightUp.position
            hardware.hangRotator.power = 1.0
        } else if (hardware.collector.power != 0.0) {
            hardware.hangRotator.mode = DcMotor.RunMode.RUN_TO_POSITION
            hardware.hangRotator.targetPosition = ThreeDayHardware.RotatorPos.LiftClearance.position
            hardware.hangRotator.power = 1.0
        } else if (hardware.hangRotator.mode == DcMotor.RunMode.RUN_TO_POSITION && hardware.hangRotator.power == 0.0) {
            hardware.hangRotator.mode = DcMotor.RunMode.RUN_TO_POSITION
            hardware.hangRotator.targetPosition = ThreeDayHardware.RotatorPos.Rest.position
            hardware.hangRotator.power = 0.5
        }else if (gamepad2.right_stick_x.absoluteValue > 0.2){
            hardware.hangRotator.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
            hardware.hangRotator.power = gamepad2.right_stick_x.toDouble()
        } else {
            hardware.hangRotator.power = 0.0
        }
        telemetry.addLine("hang Rotator Position " + hardware.hangRotator.currentPosition)

        previousDepoState = depoState.copy(gateTarget = desiredGatePosition)
        telemetry.addLine("Depo State: $depoState")
    }
}