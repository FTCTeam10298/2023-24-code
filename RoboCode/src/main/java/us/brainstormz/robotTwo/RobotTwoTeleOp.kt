package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.robotTwo.RobotTwoHardware.RobotState
import us.brainstormz.robotTwo.RobotTwoHardware.LeftClawPosition
import us.brainstormz.robotTwo.RobotTwoHardware.RightClawPosition
import us.brainstormz.threeDay.ThreeDayHardware
import kotlin.math.abs
import kotlin.math.absoluteValue

@TeleOp
class RobotTwoTeleOp: OpMode() {

    private val hardware = RobotTwoHardware(telemetry, this)
    val movement = MecanumDriveTrain(hardware)
    private lateinit var arm: Arm
    private lateinit var collctor: Collector

    private val initialRobotState = RobotState(
            armPos = Arm.Positions.In,
            liftPosition = RobotTwoHardware.LiftPositions.Min,
            leftClawPosition = LeftClawPosition.Retracted,
            rightClawPosition = RightClawPosition.Retracted
    )


    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        arm = Arm(  encoder= hardware.armEncoder,
                    armServo1= hardware.armServo1,
                    armServo2= hardware.armServo2)
        collctor = Collector(   extendoMotorMaster= hardware.extendoMotorMaster,
                                extendoMotorSlave= hardware.extendoMotorSlave)
    }

    private var previousGamepad1State: Gamepad = Gamepad()
    private var previousGamepad2State: Gamepad = Gamepad()
    private var previousRobotState = initialRobotState
    override fun loop() {
        /** TELE-OP PHASE */

        // DRONE DRIVE
        val yInput = gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = gamepad1.right_stick_x.toDouble()

        val y = yInput
        val x = xInput
        val r = -rInput * abs(rInput)
        movement.driveSetPower((y + x - r),
            (y - x + r),
            (y - x - r),
            (y + x + r))

        //Collector
        val collectorTriggerActivation = 0.2
        when {  
            gamepad1.right_trigger > collectorTriggerActivation -> {
                collctor.powerExtendo(gamepad1.right_trigger.toDouble())
            }
            gamepad1.left_trigger > collectorTriggerActivation -> {
//                spinCollector(RobotTwoHardware.CollectorPowers.Intake.power)
                collctor.powerExtendo(-gamepad1.left_trigger.toDouble())
            }
//            transferState() != emptyPixelHandler -> {
////                spinCollector(RobotTwoHardware.CollectorPowers.Eject.power)
//                moveExtendoTowardPosition(RobotTwoHardware.ExtendoPositions.Min.position)
//            }
            else -> {
                collctor.powerExtendo(0.0)
            }
        }

        when {
            gamepad1.right_bumper -> {
                spinCollector(RobotTwoHardware.CollectorPowers.Intake.power)
            }
            gamepad1.left_bumper -> {
                spinCollector(RobotTwoHardware.CollectorPowers.Eject.power)
            }
            else -> {
                spinCollector(0.0)
            }
        }

        hardware.rightTransferServo.power = when {
            gamepad1.dpad_up -> 1.0
            gamepad1.dpad_right -> -1.0
            else -> 0.0
        }
        hardware.leftTransferServo.power = when {
            gamepad1.dpad_left -> 1.0
            gamepad1.dpad_down -> -1.0
            else -> 0.0
        }

        //Lift
        val liftPosition = when {
            gamepad2.dpad_up -> {
                RobotTwoHardware.LiftPositions.SetLine2
            }
            gamepad2.dpad_down -> {
                RobotTwoHardware.LiftPositions.Transfer
            }
            else -> {
                previousRobotState.liftPosition
            }
        }

        if (gamepad2.left_stick_y.absoluteValue > 0.2) {
            powerLift(gamepad2.left_stick_y.toDouble())
        } else {
            powerLift(0.0)
//            moveLiftTowardPosition(liftPosition.position)
        }


        //Arm
        val armPosition = when {
            gamepad2.dpad_left -> {
                Arm.Positions.In
            }
            gamepad2.dpad_down -> {
                Arm.Positions.Transfer
            }
            gamepad2.dpad_up -> {
                Arm.Positions.Horizontal
            }
            gamepad2.dpad_right -> {
                Arm.Positions.Out
            }
            else -> {
                previousRobotState.armPos
            }
        }
        arm.moveArmTowardPosition(armPosition.angleDegrees)
        telemetry.addLine("arm target angle: ${armPosition.angleDegrees}")
        telemetry.addLine("arm current angle: ${arm.getArmAngleDegrees()}")

        //Claws
        val leftClawPosition = if (gamepad2.left_bumper && !previousGamepad2State.left_bumper) {
            when (previousRobotState.leftClawPosition) {
                LeftClawPosition.Gripping -> LeftClawPosition.Retracted
                LeftClawPosition.Retracted -> LeftClawPosition.Gripping
            }
        } else {
            previousRobotState.leftClawPosition
        }
        hardware.leftClawServo.position = leftClawPosition.position

        val rightClawPosition = if (gamepad2.right_bumper && !previousGamepad2State.right_bumper) {
            when (previousRobotState.rightClawPosition) {
                RightClawPosition.Gripping -> RightClawPosition.Retracted
                RightClawPosition.Retracted -> RightClawPosition.Gripping
            }
        } else {
            previousRobotState.rightClawPosition
        }
        hardware.rightClawServo.position = rightClawPosition.position
//        telemetry.addLine("rightClawPosition: $rightClawPosition")
//        telemetry.addLine("gamepad2.right_bumper: ${gamepad2.right_bumper}")
//        telemetry.addLine("previousGamepad2State.right_bumper: ${previousGamepad2State.right_bumper}")
        val maxSafeCurrentAmps = 5.5
        hardware.extendoMotorMaster.setCurrentAlert(maxSafeCurrentAmps, CurrentUnit.AMPS)
        telemetry.addLine("Extendo motor current: ${hardware.extendoMotorMaster.getCurrent(CurrentUnit.AMPS)} \n Is over current of $maxSafeCurrentAmps amps: ${hardware.extendoMotorMaster.isOverCurrent}")
        telemetry.addLine("previousRobotState: $previousRobotState")

        //Hang
        hardware.hangReleaseServo.power = if (gamepad2.y) {
            1.0
        } else {
            0.0
        }


        //Previous state
        previousRobotState = RobotState(
            armPos = armPosition,
            liftPosition = liftPosition,
            leftClawPosition = leftClawPosition,
            rightClawPosition = rightClawPosition
        )
        previousGamepad1State.copy(gamepad1)
        previousGamepad2State.copy(gamepad2)
        telemetry.update()
    }

    enum class PixelHandlerState {
        None,
        White,
        Purple,
        Green,
        Yellow
    }
    data class TwoPixelHandlerState(val leftSide: PixelHandlerState, val rightSide: PixelHandlerState)
    private val emptyPixelHandler = TwoPixelHandlerState(PixelHandlerState.None, PixelHandlerState.None)

    private fun transferState(): TwoPixelHandlerState {
        //TODO Put logic here and sensors on the robot
        return TwoPixelHandlerState(PixelHandlerState.None, PixelHandlerState.None)
    }
    private fun clawState(): TwoPixelHandlerState {
        //TODO Put logic here and sensors on the robot
        return TwoPixelHandlerState(PixelHandlerState.None, PixelHandlerState.None)
    }

    enum class LiftToggleOptions {
        SetLine1,
        SetLine2,
        SetLine3
    }
    private fun powerLift(power: Double) {
        val currentPosition = hardware.liftMotorMaster.currentPosition.toDouble()
        val allowedPower = power
//        if (currentPosition > RobotTwoHardware.LiftPositions.Max.position) {
//            power.coerceAtMost(0.0)
//        } else if (currentPosition < RobotTwoHardware.LiftPositions.Min.position) {
//            power.coerceAtLeast(0.0)
//        } else {
//            power
//        }

        hardware.liftMotorMaster.power = allowedPower
        hardware.liftMotorSlave.power = allowedPower
    }
    private fun moveLiftTowardPosition(targetPosition: Double) {
        val currentPosition = hardware.liftMotorMaster.currentPosition.toDouble()
        val power = hardware.liftPositionPID.calcPID(targetPosition, currentPosition)
        powerLift(power)
    }


    private fun spinCollector(power: Double) {
        hardware.collectorServo1.power = power
        hardware.collectorServo2.power = power
    }
}