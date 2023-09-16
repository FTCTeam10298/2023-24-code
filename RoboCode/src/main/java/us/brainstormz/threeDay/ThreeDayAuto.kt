package us.brainstormz.threeDay

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.threeDay.ThreeDayHardware.LiftPos
import us.brainstormz.threeDay.ThreeDayHardware.ArmPos

@Autonomous
class ThreeDayAuto: LinearOpMode() {

    val hardware = ThreeDayHardware()
    val movement = EncoderDriveMovement(hardware, TelemetryConsole(telemetry))

    override fun runOpMode() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        waitForStart()
        /** AUTONOMOUS  PHASE */

        //move to spike spot
        movement.driveRobotPosition(power = 1.0, inches = 24.0, smartAccel = true)
        //drop
        hardware.autoClaw.position = hardware.autoClawDown
        sleep(300)
        hardware.autoClaw.position = hardware.autoClawUp
        //consistent end position
        movement.driveRobotStrafe(power = 1.0, inches = 2.0, smartAccel = true)
        movement.driveRobotTurn(power = 1.0, degree = 90.0, smartAccel = true)

        //go to backboard
        movement.driveRobotPosition(power = 1.0, inches = 45.0, smartAccel = true)
        //linup
        movement.driveRobotStrafe(power = 1.0, inches = -10.0, smartAccel = true)
        //drop
        moveLiftBlocking(LiftPos.Middle.position)
        hardware.rightArm.position = ArmPos.Out.position
        hardware.leftArm.position = ArmPos.Out.position
        hardware.clawA.position = hardware.clawAOpenPos
        hardware.clawB.position = hardware.clawBOpenPos
        sleep(500)
        hardware.rightArm.position = ArmPos.In.position
        hardware.leftArm.position = ArmPos.In.position
        moveLiftBlocking(LiftPos.Min.position)

        //Park
        movement.driveRobotStrafe(power = 1.0, inches = -10.0, smartAccel = true)
    }

    fun moveLift(targetPosition: Int): Boolean {
        hardware.lift.mode = DcMotor.RunMode.RUN_TO_POSITION
        hardware.lift.targetPosition = targetPosition

        val liftNeedsToGoDown = targetPosition < hardware.lift.targetPosition
        hardware.lift.power = if (liftNeedsToGoDown) 0.2 else 1.0

        return !hardware.lift.isBusy
    }
    fun moveLiftBlocking(targetPosition: Int) {
        while (!moveLift(targetPosition)) {
            sleep(100)
        }
    }

}