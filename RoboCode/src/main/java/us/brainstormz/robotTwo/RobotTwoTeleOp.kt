package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import kotlin.math.abs

@TeleOp
class RobotTwoTeleOp: OpMode() {

    private val hardware = RobotTwoHardware(telemetry, this)
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
                powerExtendo(gamepad1.right_trigger.toDouble())
            }
            gamepad1.left_trigger > collectorTriggerActivation -> {
//                spinCollector(RobotTwoHardware.CollectorPowers.Intake.power)
                powerExtendo(-gamepad1.left_trigger.toDouble())
            }
            transferState() != emptyPixelHandler -> {
//                spinCollector(RobotTwoHardware.CollectorPowers.Eject.power)
                moveExtendoTowardPosition(RobotTwoHardware.ExtendoPositions.Min.position)
            }
            else -> {
                powerExtendo(0.0)
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

        //Lift
        powerLift(gamepad2.left_stick_x.toDouble())
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
        if (currentPosition > RobotTwoHardware.LiftPositions.Max.position) {
            power.coerceAtMost(0.0)
        } else if (currentPosition < RobotTwoHardware.LiftPositions.Min.position) {
            power.coerceAtLeast(0.0)
        } else {
            power
        }

//        hardware.liftMotorMaster.power = allowedPower
//        hardware.liftMotorSlave.power = allowedPower
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
    private fun powerExtendo(power: Double) {
        val currentPosition = hardware.extendoMotorMaster.currentPosition.toDouble()
        val allowedPower = power
        if (currentPosition > RobotTwoHardware.ExtendoPositions.Max.position) {
            power.coerceAtMost(0.0)
        } else if (currentPosition < RobotTwoHardware.ExtendoPositions.Min.position) {
            power.coerceAtLeast(0.0)
        } else {
            power
        }

//        hardware.extendoMotorMaster.power = allowedPower
//        hardware.extendoMotorSlave.power = allowedPower
    }
    private fun moveExtendoTowardPosition(targetPosition: Double) {
        val currentPosition = hardware.extendoMotorMaster.currentPosition.toDouble()
        val power = hardware.extendoPositionPID.calcPID(targetPosition, currentPosition)
        powerExtendo(power)
    }
}