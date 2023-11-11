package us.brainstormz.threeDay

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.EncoderDriveMovement
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.potatoBot.TeamPropDetector
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.threeDay.ThreeDayHardware.LiftPos
import us.brainstormz.threeDay.ThreeDayHardware.AutoClawPos
import us.brainstormz.threeDay.ThreeDayHardware.GatePosition
import us.brainstormz.potatoBot.TeamPropDetector.PropPosition
import us.brainstormz.threeDay.ThreeDayHardware.ArmPos

@Autonomous
class Meet1Auto: LinearOpMode() {

    val hardware = ThreeDayHardware(telemetry, this)
    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)
    val movement = EncoderDriveMovement(hardware, console)


    override fun runOpMode() {
        /** INIT PHASE */

        hardware.init(hardwareMap)
        hardware.clawA.position = GatePosition.Closed.position

        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"), nextMenu = "autoCycles", firstMenu = true)
        wizard.newMenu("autoCycles", "How many auto cycles should we do?", listOf("0", "1"))
        wizard.summonWizardBlocking(gamepad1)

        val opencv = OpenCvAbstraction(this)
        val movementSignBasedOnAlliance = when (wizard.wasItemChosen("alliance", "Blue")) {
            true -> { //blue
                1
            }
            false -> { //red
                -1
            }
        }
        val allianceColor = if (wizard.wasItemChosen("alliance", "Blue")) TeamPropDetector.PropColors.Blue else TeamPropDetector.PropColors.Red
        val teamPropDetector = TeamPropDetector(telemetry, allianceColor)
//        opencv.init(hardwareMap)
//        opencv.internalCamera = false
//        opencv.cameraName = "Webcam 1"
//        opencv.cameraOrientation = OpenCvCameraRotation.SIDEWAYS_LEFT
//        opencv.onNewFrame(teamPropDetector::processFrame)

        waitForStart()
        /** AUTONOMOUS  PHASE */

        val propPosition = PropPosition.Center//teamPropDetector.position
        val normalMovementSpeed = 0.4

        when (propPosition) {
            PropPosition.Left -> {
                //Spike Pixel
                movement.driveRobotPosition(power = normalMovementSpeed, inches = -30.0, smartAccel = true)
                movement.driveRobotStrafe(power = normalMovementSpeed, inches = -2.0, smartAccel = true)
                movement.driveRobotTurn(power = 0.5, degree = 180.0, smartAccel = true)
                movement.driveRobotStrafe(power = 0.3, inches = 23.0, smartAccel = true)
                sleep(500)

                hardware.autoClaw.position = AutoClawPos.Down.position
                sleep(1000)
                hardware.autoClaw.position = AutoClawPos.Up.position

                //Deposit
                movement.driveRobotStrafe(power = normalMovementSpeed, inches = 6.0, smartAccel = true)
                movement.driveRobotTurn(power = normalMovementSpeed, degree = 90.0, smartAccel = true)
                movement.driveRobotPosition(power = normalMovementSpeed, inches = -10.0, smartAccel = true)
                movement.driveRobotPosition(power = 0.2, inches = -9.0, smartAccel = false)
                movement.driveRobotPosition(power = 0.2, inches = 9.0, smartAccel = true)

                movement.driveRobotStrafe(power = normalMovementSpeed, inches = -11.0, smartAccel = true)
                movement.driveRobotPosition(power = 0.2, inches = -9.0, smartAccel = false)

                deposit()

                //Park
                moveRotator(0)
                movement.driveRobotPosition(power = normalMovementSpeed, inches = 5.0, smartAccel = true)
                hardware.hangRotator.power = 0.0
                movement.driveRobotStrafe(power = normalMovementSpeed, inches = 35.0, smartAccel = true)
            }
            PropPosition.Center -> {
                //Spike Pixel
                movement.driveRobotPosition(power = normalMovementSpeed, inches = -28.0, smartAccel = true)
                movement.driveRobotStrafe(power = normalMovementSpeed, inches = -2.0, smartAccel = true)
                movement.driveRobotTurn(power = 0.5, degree = 90.0, smartAccel = true)
                sleep(500)

                hardware.autoClaw.position = AutoClawPos.Down.position
                sleep(1000)
                hardware.autoClaw.position = AutoClawPos.Up.position

                //Deposit
                movement.driveRobotStrafe(power = 0.3, inches = 3.0, smartAccel = true)
                movement.driveRobotPosition(power = normalMovementSpeed, inches = 23.0, smartAccel = true)
                movement.driveRobotTurn(power = 0.5, degree = 180.0, smartAccel = true)
                movement.driveRobotPosition(power = normalMovementSpeed, inches = -10.0, smartAccel = true)
                movement.driveRobotPosition(power = 0.2, inches = -9.0, smartAccel = false)
                movement.driveRobotPosition(power = 0.2, inches = 9.0, smartAccel = true)

                movement.driveRobotStrafe(power = normalMovementSpeed, inches = -5.0, smartAccel = true)
                movement.driveRobotPosition(power = 0.2, inches = -9.0, smartAccel = false)

                deposit()

                //Park
                moveRotator(0)
                movement.driveRobotPosition(power = normalMovementSpeed, inches = 5.0, smartAccel = true)
                hardware.hangRotator.power = 0.0
                movement.driveRobotStrafe(power = normalMovementSpeed, inches = 35.0, smartAccel = true)
            }
            PropPosition.Right -> {

            }
        }
//        movement.driveRobotPosition(power = 0.6, inches = -38.0, smartAccel = true)
//
//        //drop
//        moveLiftBlocking(LiftPos.High.position)
//        moveRotator(ThreeDayHardware.RotatorPos.LiftClearance.position)
//        hardware.rightArm.position = ArmPos.Out.position
//        hardware.leftArm.position = ArmPos.Out.position
//        sleep(400)
//        hardware.clawA.position = GatePosition.Deposit.position
//        sleep(1500)
//        hardware.rightArm.position = ArmPos.In.position
//        hardware.leftArm.position = ArmPos.In.position
//        moveLiftBlocking(LiftPos.Min.position)
//
//        //Park
//        movement.driveRobotPosition(power = 1.0, inches = 5.0, smartAccel = true)
//        movement.driveRobotStrafe(power = 1.0, inches = movementSignBasedOnAlliance * -30.0, smartAccel = true)
//        movement.driveRobotPosition(power = 1.0, inches = -15.0, smartAccel = true)
    }
    fun deposit() {
        moveLiftBlocking(LiftPos.Low.position)
        moveRotator(ThreeDayHardware.RotatorPos.LiftClearance.position)
        sleep(100)

        hardware.rightArm.position = ArmPos.Out.position
        hardware.leftArm.position = ArmPos.Out.position
        sleep(1000)
        hardware.clawA.position = GatePosition.Intake.position
        sleep(1500)
        hardware.clawA.position = GatePosition.Closed.position
        sleep(200)
        hardware.clawA.position = GatePosition.Intake.position
        sleep(700)
        movement.driveRobotPosition(0.2, 4.0, false)
        hardware.clawA.position = GatePosition.Closed.position

        hardware.rightArm.position = ArmPos.In.position
        hardware.leftArm.position = ArmPos.In.position
        sleep(300)

        moveLiftBlocking(LiftPos.Min.position)
        sleep(800)
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
//            moveRotator(ThreeDayHardware.RotatorPos.LiftClearance.position)
            sleep(100)
        }
//        hardware.hangRotator.power = 0.0
    }

}