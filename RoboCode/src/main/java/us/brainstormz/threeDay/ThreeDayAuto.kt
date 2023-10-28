package us.brainstormz.threeDay

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.threeDay.ThreeDayHardware.LiftPos
import us.brainstormz.threeDay.ThreeDayHardware.ArmPos
import us.brainstormz.potatoBot.TeamPropDetector.PropPosition
import us.brainstormz.potatoBot.TeamPropDetector.PropColors
import us.brainstormz.potatoBot.TeamPropDetector

@Autonomous
class ThreeDayAuto: LinearOpMode() {

    val hardware = ThreeDayHardware(telemetry)
    val console = TelemetryConsole(telemetry)
    val movement = EncoderDriveMovement(hardware, console)

    val opencv = OpenCvAbstraction(this)
//    val teamPropDetector = TeamPropDetector(telemetry, PropColors.Red)

    override fun runOpMode() {
        /** INIT PHASE */

        hardware.init(hardwareMap)

//        opencv.init(hardwareMap)
//        opencv.internalCamera = false
//        opencv.cameraName = "Webcam 1"
//        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_LEFT
//        opencv.onNewFrame(teamPropDetector::processFrame)

        waitForStart()
        /** AUTONOMOUS  PHASE */

        val propPosition = PropPosition.Left//teamPropDetector.position
//        opencv.stop()

        hardware.clawA.position = hardware.clawAClosedPos
        hardware.clawB.position = hardware.clawBClosedPos
        //Drop on spike
        when (propPosition) {
            PropPosition.Left -> {
                //move to spike spot
                movement.driveRobotPosition(power = 1.0, inches = -28.0, smartAccel = true)

                //drop
                hardware.autoClaw.position = hardware.autoClawDown
                sleep(800)
                hardware.autoClaw.position = hardware.autoClawUp

                //consistent end position
                movement.driveRobotStrafe(power = 1.0, inches = 4.0, smartAccel = true)
                movement.driveRobotTurn(power = 0.7, degree = -90.0, smartAccel = true)

                //go to backboard
                movement.driveRobotPosition(power = 1.0, inches = -37.0, smartAccel = true)
                movement.driveRobotPosition(power = 0.5, inches = -2.0, smartAccel = false)

                //linup
                movement.driveRobotStrafe(power = 1.0, inches = -5.0, smartAccel = true)
            }
            PropPosition.Center -> {
                //move to spike spot
                movement.driveRobotPosition(power = 1.0, inches = -23.0, smartAccel = true)
                movement.driveRobotTurn(power = 1.0, degree = 90.0, smartAccel = true)
                //drop
                hardware.autoClaw.position = hardware.autoClawDown
                sleep(300)
                hardware.autoClaw.position = hardware.autoClawUp
                //consistent end position
                movement.driveRobotStrafe(power = 1.0, inches = -2.0, smartAccel = true)

                //go to backboard
                movement.driveRobotPosition(power = 1.0, inches = -45.0, smartAccel = true)

                //linup
                movement.driveRobotStrafe(power = 1.0, inches = 10.0, smartAccel = true)
            }
            PropPosition.Right -> {
                //move to spike spot
                movement.driveRobotPosition(power = 1.0, inches = -24.0, smartAccel = true)
                movement.driveRobotTurn(power = 1.0, degree = 180.0, smartAccel = true)
                //drop
                hardware.autoClaw.position = hardware.autoClawDown
                sleep(300)
                hardware.autoClaw.position = hardware.autoClawUp
                //consistent end position
                movement.driveRobotPosition(power = 1.0, inches = 24.0, smartAccel = true)
                movement.driveRobotTurn(power = 1.0, degree = -90.0, smartAccel = true)

                //go to backboard
                movement.driveRobotPosition(power = 1.0, inches = -45.0, smartAccel = true)

                //linup
                movement.driveRobotStrafe(power = 1.0, inches = -34.0, smartAccel = true)
            }
        }

        //drop
        moveLiftBlocking(LiftPos.Middle.position)
        hardware.rightArm.position = ArmPos.Out.position
        hardware.leftArm.position = ArmPos.Out.position
        sleep(400)
        hardware.clawA.position = hardware.clawAOpenPos
        hardware.clawB.position = hardware.clawBOpenPos
        sleep(800)
        hardware.rightArm.position = ArmPos.In.position
        hardware.leftArm.position = ArmPos.In.position
        moveLiftBlocking(LiftPos.Min.position)

        //Park
        movement.driveRobotStrafe(power = 1.0, inches = -25.0, smartAccel = true)
        movement.driveRobotPosition(power = 1.0, inches = -15.0, smartAccel = false)
    }

    fun moveLift(targetPosition: Int): Boolean {
        hardware.lift.mode = DcMotor.RunMode.RUN_TO_POSITION
        hardware.lift.targetPosition = targetPosition

        val liftNeedsToGoDown = targetPosition < hardware.lift.targetPosition
        hardware.lift.power = if (liftNeedsToGoDown) 0.2 else 1.0

        return !hardware.lift.isBusy
    }
    fun moveRotator(targetPosition: Int): Boolean {
        hardware.hangRotator.mode = DcMotor.RunMode.RUN_TO_POSITION
        hardware.hangRotator.targetPosition = targetPosition
        hardware.hangRotator.power = 1.0

        return !hardware.hangRotator.isBusy
    }
    fun moveLiftBlocking(targetPosition: Int) {
        while (!moveRotator(ThreeDayHardware.RotatorPos.LiftClearance.position)) {
            sleep(100)
        }
        while (!moveLift(targetPosition)) {
            moveRotator(ThreeDayHardware.RotatorPos.LiftClearance.position)
            sleep(100)
        }
        hardware.hangRotator.power = 0.0
    }

}