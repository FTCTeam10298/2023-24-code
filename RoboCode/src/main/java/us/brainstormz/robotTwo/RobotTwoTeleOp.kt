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
                spinCollector(1.0)
                //turn on collector servos at full speed
                //move out at trigger pressed velocity
            }
            gamepad1.left_trigger > collectorTriggerActivation -> {
                spinCollector(-1.0)

                //turn on collector servos at full speed
                //move in at trigger pressed velocity
            }
            transferState() != emptyPixelHandler -> {

            }
        }
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

    private fun spinCollector(power: Double) {
        hardware.collectorServo1.power = power
        hardware.collectorServo2.power = power
    }
}