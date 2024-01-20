package us.brainstormz.robotTwo

import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DigitalChannel
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.robotTwo.RobotTwoHardware.RobotState
import us.brainstormz.robotTwo.RobotTwoHardware.LeftClawPosition
import us.brainstormz.robotTwo.RobotTwoHardware.RightClawPosition
import kotlin.math.abs
import kotlin.math.absoluteValue

@TeleOp
class RobotTwoTeleOp: OpMode() {

    private val hardware = RobotTwoHardware(telemetry, this)
    val movement = MecanumDriveTrain(hardware)
    private lateinit var arm: Arm
    private lateinit var collector: Collector
    private lateinit var lift: Lift

    private lateinit var odometryLocalizer: RRTwoWheelLocalizer

//    private val initialRobotState = RobotState(
//            armPos = Arm.Positions.In,
//            liftPosition = RobotTwoHardware.LiftPositions.Min,
//            leftClawPosition = LeftClawPosition.Retracted,
//            rightClawPosition = RightClawPosition.Retracted,
//            collectorState = Collector.CollectorPowers.Off
//    )
    private val initialRobotState = RobotState(
            positionAndRotation = PositionAndRotation(),
            collectorState = Collector.CollectorState(
                    collectorState = Collector.CollectorPowers.Off,
                    extendoPosition = RobotTwoHardware.ExtendoPositions.Min,
                    transferRollersState = Collector.TransferState(Collector.CollectorPowers.Off, Collector.CollectorPowers.Off, Collector.DirectorState.Off),
                    transferLeftSensorState = Collector.TransferHalfState(false, 0),
                    transferRightSensorState = Collector.TransferHalfState(false, 0)
            ),
            depoState = RobotTwoAuto.DepoState(
                    liftPosition = Lift.LiftPositions.Min,
                    armPos = Arm.Positions.In,
                    leftClawPosition = LeftClawPosition.Retracted,
                    rightClawPosition = RightClawPosition.Retracted,
            ),
    )


    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        arm = Arm(  encoder= hardware.armEncoder,
                    armServo1= hardware.armServo1,
                    armServo2= hardware.armServo2)
        collector = Collector(  extendoMotorMaster= hardware.extendoMotorMaster,
                                extendoMotorSlave= hardware.extendoMotorSlave,
                                collectorServo1 = hardware.collectorServo1,
                                collectorServo2 = hardware.collectorServo2,
                                rightTransferServo=hardware.rightTransferServo,
                                leftTransferServo= hardware.leftTransferServo,
                                transferDirectorServo= hardware.transferDirectorServo,
                                leftTransferPixelSensor= hardware.leftTransferSensor,
                                rightTransferPixelSensor= hardware.rightTransferSensor,
                                leftRollerEncoder= hardware.leftRollerEncoder,
                                rightRollerEncoder= hardware.rightRollerEncoder,
                                telemetry= telemetry)
        lift = Lift(liftMotor1 = hardware.liftMotorMaster,
                    liftMotor2 = hardware.liftMotorSlave,
                    liftLimit = hardware.liftMagnetLimit)

        odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
    }

    private var previousGamepad1State: Gamepad = Gamepad()
    private var previousGamepad2State: Gamepad = Gamepad()
    private var previousRobotState = initialRobotState
    override fun loop() {
        /** TELE-OP PHASE */

//        hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.entries[(Math.random() * RevBlinkinLedDriver.BlinkinPattern.entries.size).toInt()])
        hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE)


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

        val shouldTransfer = gamepad1.x
        if (shouldTransfer) {
            lift.moveLiftToBottom()
        } else {

        }

        telemetry.addLine("lift current: ${hardware.liftMotorMaster.getCurrent(CurrentUnit.AMPS)}")

        //Collector
        val collectorTriggerActivation = 0.2
        when {
            gamepad1.right_trigger > collectorTriggerActivation -> {
                collector.powerExtendo(gamepad1.right_trigger.toDouble())
            }
            gamepad1.left_trigger > collectorTriggerActivation -> {
//                spinCollector(RobotTwoHardware.CollectorPowers.Intake.power)
                collector.powerExtendo(-gamepad1.left_trigger.toDouble())
            }
//            transferState() != emptyPixelHandler -> {
////                spinCollector(RobotTwoHardware.CollectorPowers.Eject.power)
//                moveExtendoTowardPosition(RobotTwoHardware.ExtendoPositions.Min.position)
//            }
            else -> {
                collector.powerExtendo(0.0)
            }
        }


        val inputCollectorState = when {
            gamepad1.right_bumper -> {
                Collector.CollectorPowers.Intake
            }
            gamepad1.left_bumper -> {
                Collector.CollectorPowers.Eject
            }
            else -> {
                Collector.CollectorPowers.Off
            }
        }

        val actualCollectorState = collector.getCollectorState(inputCollectorState)

        collector.spinCollector(actualCollectorState.power)

        val autoTransferState = collector.getAutoTransferState(isCollecting= gamepad1.right_bumper)
        val transferState = when {
            gamepad1.dpad_right ->
                Collector.TransferState(leftServoCollect = Collector.CollectorPowers.Off,
                                        rightServoCollect = Collector.CollectorPowers.Eject,
                                        directorState = Collector.DirectorState.Off)
            gamepad1.dpad_left ->
                Collector.TransferState(leftServoCollect = Collector.CollectorPowers.Eject,
                                        rightServoCollect = Collector.CollectorPowers.Off,
                                        directorState = Collector.DirectorState.Off)
            gamepad1.dpad_up ->
                Collector.TransferState(leftServoCollect = Collector.CollectorPowers.Intake,
                                        rightServoCollect = Collector.CollectorPowers.Intake,
                                        directorState = Collector.DirectorState.Off)
            gamepad1.dpad_down ->
                Collector.TransferState(leftServoCollect = Collector.CollectorPowers.Eject,
                                        rightServoCollect = Collector.CollectorPowers.Eject,
                                        directorState = Collector.DirectorState.Off)
            else -> autoTransferState
        }
        collector.runTransfer(transferState)



        telemetry.addLine("left roller servo position: ${collector.leftEncoderReader.getPositionDegrees()}")
        telemetry.addLine("left flapAngleDegrees: ${collector.getFlapAngleDegrees(collector.leftEncoderReader)}")
        telemetry.addLine("right roller servo position: ${collector.rightEncoderReader.getPositionDegrees()}")
        telemetry.addLine("right flapAngleDegrees: ${collector.getFlapAngleDegrees(collector.rightEncoderReader)}")
        telemetry.addLine("left transfer sensor: ${collector.isPixelIn(hardware.leftTransferSensor)}")
        telemetry.addLine("right transfer sensor: ${collector.isPixelIn(hardware.rightTransferSensor)}")


        //Lift
        val liftPosition = when {
            gamepad2.dpad_up -> {
                Lift.LiftPositions.SetLine2
            }
            gamepad2.dpad_down -> {
                Lift.LiftPositions.Transfer
            }
            else -> {
                previousRobotState.depoState.liftPosition
            }
        }
        if (gamepad2.left_stick_y.absoluteValue > 0.2) {
            lift.powerLift(-gamepad2.left_stick_y.toDouble())
        } else {
            //lift.powerLift(0.0)
//            moveLiftTowardPosition(liftPosition.position)
        }

        telemetry.addLine("is Limit Switch Activated: ${lift.isLimitSwitchActivated()}")


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
                previousRobotState.depoState.armPos
            }
        }
//        arm.powerArm(gamepad2.left_stick_x.toDouble())
        arm.moveArmTowardPosition(armPosition.angleDegrees)
        telemetry.addLine("arm target angle: ${armPosition.angleDegrees}")
        telemetry.addLine("arm current angle: ${arm.getArmAngleDegrees()}")
//        telemetry.addLine("arm current angle: ${hardware.armEncoder.voltage}")

        //Claws
        val leftClawPosition = if (gamepad2.left_bumper && !previousGamepad2State.left_bumper) {
            when (previousRobotState.depoState.leftClawPosition) {
                LeftClawPosition.Gripping -> LeftClawPosition.Retracted
                LeftClawPosition.Retracted -> LeftClawPosition.Gripping
            }
        } else {
            previousRobotState.depoState.leftClawPosition
        }
        hardware.leftClawServo.position = leftClawPosition.position

        val rightClawPosition = if (gamepad2.right_bumper && !previousGamepad2State.right_bumper) {
            when (previousRobotState.depoState.rightClawPosition) {
                RightClawPosition.Gripping -> RightClawPosition.Retracted
                RightClawPosition.Retracted -> RightClawPosition.Gripping
            }
        } else {
            previousRobotState.depoState.rightClawPosition
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
        previousRobotState = hardware.getActualState(RobotTwoAuto.ActualWorld(previousRobotState, 0), arm, odometryLocalizer, collector).actualRobot
                .copy(depoState = RobotTwoAuto.DepoState(   armPos = armPosition,
                                                            liftPosition = liftPosition,
                                                            leftClawPosition = leftClawPosition,
                                                            rightClawPosition = rightClawPosition))
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

}