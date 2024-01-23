package us.brainstormz.robotTwo

import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.robotTwo.RobotTwoHardware.RobotState
import us.brainstormz.robotTwo.RobotTwoHardware.LeftClawPosition
import us.brainstormz.robotTwo.RobotTwoHardware.RightClawPosition
import us.brainstormz.utils.LoopTimeMeasurer
import kotlin.math.abs
import kotlin.math.absoluteValue

@TeleOp(group = "!")
class RobotTwoTeleOp: OpMode() {

    private val hardware = RobotTwoHardware(telemetry, this)
    val movement = MecanumDriveTrain(hardware)
    private lateinit var arm: Arm
    private lateinit var collectorSystem: CollectorSystem
    private lateinit var lift: Lift
    private lateinit var transfer: TransferManager

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
            collectorSystemState = CollectorSystem.CollectorState(
                    collectorState = CollectorSystem.CollectorPowers.Off,
                    extendoPosition = CollectorSystem.ExtendoPositions.Manual,
                    transferRollersState = CollectorSystem.RollerState(CollectorSystem.CollectorPowers.Off, CollectorSystem.CollectorPowers.Off, CollectorSystem.DirectorState.Off),
                    transferLeftSensorState = CollectorSystem.TransferHalfState(false, 0),
                    transferRightSensorState = CollectorSystem.TransferHalfState(false, 0)
            ),
            depoState = RobotTwoAuto.DepoState(
                    liftPosition = Lift.LiftPositions.Manual,
                    armPos = Arm.Positions.Manual,
                    leftClawPosition = LeftClawPosition.Retracted,
                    rightClawPosition = RightClawPosition.Retracted,
            ),
    )


    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        arm = Arm(  encoder= hardware.armEncoder,
                    armServo1= hardware.armServo1,
                    armServo2= hardware.armServo2, telemetry)
        collectorSystem = CollectorSystem(  extendoMotorMaster= hardware.extendoMotorMaster,
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
        transfer = TransferManager(
                collectorSystem,
                lift,
                arm,
                telemetry)

        odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
    }

    private val loopTimeMeasurer = LoopTimeMeasurer()

    private var numberOfTimesColorButtonPressed: Int = 0
    private var previousDesiredPixelLightPattern: BothPixelsWeWant = BothPixelsWeWant(pixel1 = PixelWeWant.Unknown, pixel2 = PixelWeWant.Unknown)
    private var previousIsAnyColorButtonPressed: Boolean = false
    private var previousGamepad1State: Gamepad = Gamepad()
    private var previousGamepad2State: Gamepad = Gamepad()
    private var previousRobotState = initialRobotState
    override fun loop() {
        /** TELE-OP PHASE */

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMilis()
        telemetry.addLine("loop time: $loopTime milis")

        /** Gamepad 1 */
        // DRONE DRIVE
        val yInput = -gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = gamepad1.right_stick_x.toDouble()

        //Strafe without turing for depositing
        val slowDowMultiplier = 1.0
        val driver2XInput = if (xInput == 0.0) {
            gamepad2.left_stick_x.toDouble() * slowDowMultiplier
        } else {
            0.0
        }

        val y = yInput
        val x = xInput + driver2XInput
        val r = -rInput * abs(rInput)
        movement.driveSetPower((y + x - r),
                (y - x + r),
                (y - x - r),
                (y + x + r))

        //Collector
        val inputCollectorStateSystem = when {
            gamepad1.right_bumper -> {
                CollectorSystem.CollectorPowers.Intake
            }
            gamepad1.left_bumper -> {
                CollectorSystem.CollectorPowers.Eject
            }
            else -> {
                CollectorSystem.CollectorPowers.Off
            }
        }

        val actualCollectorState = collectorSystem.getCollectorState(inputCollectorStateSystem)
        collectorSystem.spinCollector(actualCollectorState.power)
//        collectorSystem.spinCollector(inputCollectorStateSystem.power)

        val autoRollerState = collectorSystem.getAutoPixelSortState(isCollecting = gamepad1.right_bumper)
        val rollerState = when {
            gamepad1.dpad_right ->
                CollectorSystem.RollerState(leftServoCollect = CollectorSystem.CollectorPowers.Off,
                        rightServoCollect = CollectorSystem.CollectorPowers.Eject,
                        directorState = CollectorSystem.DirectorState.Off)
            gamepad1.dpad_left ->
                CollectorSystem.RollerState(leftServoCollect = CollectorSystem.CollectorPowers.Eject,
                        rightServoCollect = CollectorSystem.CollectorPowers.Off,
                        directorState = CollectorSystem.DirectorState.Off)
            gamepad1.dpad_up ->
                CollectorSystem.RollerState(leftServoCollect = CollectorSystem.CollectorPowers.Intake,
                        rightServoCollect = CollectorSystem.CollectorPowers.Intake,
                        directorState = CollectorSystem.DirectorState.Off)
            gamepad1.dpad_down ->
                CollectorSystem.RollerState(leftServoCollect = CollectorSystem.CollectorPowers.Eject,
                        rightServoCollect = CollectorSystem.CollectorPowers.Eject,
                        directorState = CollectorSystem.DirectorState.Off)
            else -> autoRollerState
        }
        collectorSystem.runRollers(rollerState)


        /** Gamepad 2 */

        //Transfer
        val shouldWeTransfer = gamepad2.a
        val transferState = transfer.getTransferState(TransferManager.ClawStateFromTransfer.Retracted, RevBlinkinLedDriver.BlinkinPattern.BLUE)

        //Extendo
        val extendoTriggerActivation = 0.1
        val rightTrigger: Boolean = gamepad1.right_trigger > extendoTriggerActivation
        val leftTrigger: Boolean = gamepad1.left_trigger > extendoTriggerActivation
        val extendoState = when {
            rightTrigger -> {
                collectorSystem.powerExtendo(gamepad1.right_trigger.toDouble())
                CollectorSystem.ExtendoPositions.Manual
            }
            leftTrigger -> {
                collectorSystem.powerExtendo(-gamepad1.left_trigger.toDouble())
                CollectorSystem.ExtendoPositions.Manual
            }
            shouldWeTransfer -> {
                when (transferState.collectorState) {
                    TransferManager.ExtendoStateFromTransfer.MoveIn -> {
                        CollectorSystem.ExtendoPositions.Min
                    }
                    TransferManager.ExtendoStateFromTransfer.MoveOutOfTheWay -> {
                        CollectorSystem.ExtendoPositions.ClearTransfer
                    }
                }
            }
            else -> {
                collectorSystem.powerExtendo(0.0)
                CollectorSystem.ExtendoPositions.Manual
            }
        }
        if (extendoState != CollectorSystem.ExtendoPositions.Manual){
            collectorSystem.moveExtendoToPosition(extendoState.ticks)
        }


        //Lift
        val liftOverrideStickValue = gamepad2.right_stick_y.toDouble()
        val areManualControlsActive = liftOverrideStickValue.absoluteValue > 0.2

        val liftPosition: Lift.LiftPositions = if (areManualControlsActive) {
            Lift.LiftPositions.Manual
        } else {
            when {
                gamepad2.dpad_up -> {
                    Lift.LiftPositions.SetLine3
                }
                gamepad2.dpad_down -> {
                    Lift.LiftPositions.Min
                }
                gamepad2.dpad_right && !previousGamepad2State.dpad_right -> {
                    if (previousRobotState.depoState.liftPosition !== Lift.LiftPositions.SetLine1) {
                        Lift.LiftPositions.SetLine1
                    } else {
                        Lift.LiftPositions.SetLine2
                    }
                }
                shouldWeTransfer -> {
                    when (transferState.liftState) {
                        TransferManager.LiftStateFromTransfer.MoveDown -> Lift.LiftPositions.Min
                        TransferManager.LiftStateFromTransfer.None -> Lift.LiftPositions.Nothing
                    }
                }
                else -> {
                    previousRobotState.depoState.liftPosition
                }
            }
        }

        val liftTargetIsBelowSafeArm = liftPosition.ticks <= Lift.LiftPositions.ClearForArmToMove.ticks
        val liftActualPositionIsAboveSafeArm = hardware.liftMotorMaster.currentPosition >= Lift.LiftPositions.ClearForArmToMove.ticks
        val armIsAtSafeAngle = arm.getArmAngleDegrees() >= Arm.Positions.GoodEnoughForLiftToGoDown.angleDegrees
        val liftNeedsToWaitForTheArm = liftTargetIsBelowSafeArm && liftActualPositionIsAboveSafeArm && !armIsAtSafeAngle

        when {
            liftPosition == Lift.LiftPositions.Manual -> {
                lift.powerLift(-liftOverrideStickValue)
            }
            liftPosition == Lift.LiftPositions.Nothing -> {
                lift.powerLift(0.0)
            }
            liftNeedsToWaitForTheArm -> {
                lift.moveLiftToPosition(Lift.LiftPositions.ClearForArmToMove.ticks+150)
            }
            else -> {
                lift.moveLiftToPosition(liftPosition.ticks)
            }
        }

        val liftTargetHasntChanged = liftPosition == previousRobotState.depoState.liftPosition
        //Arm
        val armOverrideStickValue = gamepad2.right_stick_x.toDouble()

        val liftIsBelowFreeArmLevel = hardware.liftMotorMaster.currentPosition <= Lift.LiftPositions.ClearForArmToMove.ticks
        val liftIsAtTheBottom = lift.isLimitSwitchActivated()
        val armIsInish = arm.getArmAngleDegrees() >= Arm.Positions.GoodEnoughForLiftToGoDown.angleDegrees

        val depositorShouldGoAllTheWayIn = liftPosition.ticks <= Lift.LiftPositions.ClearForArmToMove.ticks

        val liftPositionsWhereArmShouldBeOut = listOf(Lift.LiftPositions.SetLine1, Lift.LiftPositions.SetLine2, Lift.LiftPositions.SetLine3)

        val armWasManualControlLastTime = previousRobotState.depoState.armPos == Arm.Positions.Manual

        val armPosition: Arm.Positions = if (armOverrideStickValue.absoluteValue >= 0.2) {
            Arm.Positions.Manual
        } else {
            when  {
                armWasManualControlLastTime && liftTargetHasntChanged -> {
                    Arm.Positions.Manual
                }
                liftPosition == Lift.LiftPositions.Manual || liftPosition == Lift.LiftPositions.Nothing-> {
                    previousRobotState.depoState.armPos
                }
                shouldWeTransfer -> {
                    transferState.armState
                }
                liftIsBelowFreeArmLevel  -> {
                    if (armIsInish) {
                        if (liftIsAtTheBottom) {
                            Arm.Positions.TransferringTarget
                        } else {
                            Arm.Positions.ClearLiftMovement
                        }
                    } else {
                        Arm.Positions.AutoInitPosition
                    }
                }
                depositorShouldGoAllTheWayIn && !liftIsBelowFreeArmLevel-> {
                    Arm.Positions.ClearLiftMovement
                }
                liftPosition in liftPositionsWhereArmShouldBeOut -> {
                    Arm.Positions.Out
                }
                else -> {
                    previousRobotState.depoState.armPos
                }
            }
        }
        if (armPosition == Arm.Positions.Manual) {
            arm.powerArm(armOverrideStickValue)
            telemetry.addLine("arm power (manual override): $armOverrideStickValue")
        } else {
            arm.moveArmTowardPosition(armPosition.angleDegrees)
            telemetry.addLine("arm target: $armPosition, angle: ${armPosition.angleDegrees}")
        }
        telemetry.addLine("arm actual angle: ${arm.getArmAngleDegrees()}")

        //Claws
        val leftClawPosition: LeftClawPosition = if (gamepad2.left_bumper && !previousGamepad2State.left_bumper) {
            when (previousRobotState.depoState.leftClawPosition) {
                LeftClawPosition.Gripping -> LeftClawPosition.Retracted
                LeftClawPosition.Retracted -> LeftClawPosition.Gripping
            }
        } else if (shouldWeTransfer) {
            when (transferState.clawPosition) {
                TransferManager.ClawStateFromTransfer.Gripping -> LeftClawPosition.Gripping
                TransferManager.ClawStateFromTransfer.Retracted -> LeftClawPosition.Retracted
            }
        } else {
            previousRobotState.depoState.leftClawPosition
        }
        hardware.leftClawServo.position = leftClawPosition.position

        val rightClawPosition: RightClawPosition = if (gamepad2.right_bumper && !previousGamepad2State.right_bumper) {
            when (previousRobotState.depoState.rightClawPosition) {
                RightClawPosition.Gripping -> RightClawPosition.Retracted
                RightClawPosition.Retracted -> RightClawPosition.Gripping
            }
        } else if (shouldWeTransfer) {
            when (transferState.clawPosition) {
                TransferManager.ClawStateFromTransfer.Gripping -> RightClawPosition.Gripping
                TransferManager.ClawStateFromTransfer.Retracted -> RightClawPosition.Retracted
            }
        } else {
            previousRobotState.depoState.rightClawPosition
        }
        hardware.rightClawServo.position = rightClawPosition.position




        //Launcher
        hardware.launcherServo.position = if (gamepad2.y || gamepad1.y) {
            RobotTwoHardware.LauncherPosition.Released.position
        } else {
            RobotTwoHardware.LauncherPosition.Holding.position
        }

        //Hang
        hardware.hangReleaseServo.power = if (gamepad2.left_stick_button && gamepad2.right_stick_button) {
            RobotTwoHardware.HangPowers.Release.power
        } else {
            RobotTwoHardware.HangPowers.Holding.power
        }

        //Light Control

        val isAnyColorButtonPressed: Boolean = gamepad2.a || gamepad2.b || gamepad2.x || gamepad2.y

        val desiredPixelLightPattern: BothPixelsWeWant = if (gamepad2.dpad_left) {
            val newOne: PixelWeWant = when {
                gamepad2.a -> {
                    PixelWeWant.Green
                }
                gamepad2.b -> {
                    PixelWeWant.White
                }
                gamepad2.x -> {
                    PixelWeWant.Purple
                }
                gamepad2.y -> {
                    PixelWeWant.Yellow
                }
                else -> {
                    PixelWeWant.Unknown
                }
            }

//            val isLayerRisingEdge = gamepad2.dpad_left && !previousGamepad2State.dpad_left

            val isAnyColorButtonRisingEdge = isAnyColorButtonPressed && !previousIsAnyColorButtonPressed

            if (isAnyColorButtonRisingEdge) {
                numberOfTimesColorButtonPressed += 1

                when (numberOfTimesColorButtonPressed) {
                    1 -> {
                        previousDesiredPixelLightPattern.copy(pixel1 = newOne)
                    }
                    2 -> {
                        previousDesiredPixelLightPattern.copy(pixel2 = newOne)
                    }
                    else -> {
                        previousDesiredPixelLightPattern
                    }
                }
            } else {
                previousDesiredPixelLightPattern
            }
        } else {
            numberOfTimesColorButtonPressed = 0
            previousDesiredPixelLightPattern
        }
        previousIsAnyColorButtonPressed = isAnyColorButtonPressed

        telemetry.addLine("desiredPixelLightPattern: $desiredPixelLightPattern")

        /** not controls */

        val rollerActualState = collectorSystem.getCurrentState(previousRobotState.collectorSystemState)

        //Previous state
        previousRobotState = RobotState(
                positionAndRotation = PositionAndRotation(),
                collectorSystemState = CollectorSystem.CollectorState(
                        collectorState = inputCollectorStateSystem,
                        extendoPosition = extendoState,
                        transferRollersState = autoRollerState,
                        transferLeftSensorState = rollerActualState.transferLeftSensorState,
                        transferRightSensorState = rollerActualState.transferLeftSensorState
                ),
                depoState = RobotTwoAuto.DepoState(
                        liftPosition = liftPosition,
                        armPos = armPosition,
                        leftClawPosition = leftClawPosition,
                        rightClawPosition = rightClawPosition,
                ))
//                hardware.getActualState(RobotTwoAuto.ActualWorld(previousRobotState, 0), arm, odometryLocalizer, collectorSystem).actualRobot
//                .copy(depoState = RobotTwoAuto.DepoState(   armPos = armPosition,
//                                                            liftPosition = liftPosition,
//                                                            leftClawPosition = leftClawPosition,
//                                                            rightClawPosition = rightClawPosition))
        previousDesiredPixelLightPattern = desiredPixelLightPattern
        previousGamepad1State.copy(gamepad1)
        previousGamepad2State.copy(gamepad2)
        telemetry.update()
    }

    enum class PixelWeWant {
        White,
        Green,
        Purple,
        Yellow,
        Unknown,
    }

    data class BothPixelsWeWant(val pixel1: PixelWeWant, val pixel2: PixelWeWant)
//    enum class PixelHandlerState {
//        None,
//        White,
//        Purple,
//        Green,
//        Yellow
//    }
//    data class TwoPixelHandlerState(val leftSide: PixelHandlerState, val rightSide: PixelHandlerState)
//    private val emptyPixelHandler = TwoPixelHandlerState(PixelHandlerState.None, PixelHandlerState.None)
//
//    private fun transferState(): TwoPixelHandlerState {
//        //TODO Put logic here and sensors on the robot
//        return TwoPixelHandlerState(PixelHandlerState.None, PixelHandlerState.None)
//    }
//    private fun clawState(): TwoPixelHandlerState {
//        //TODO Put logic here and sensors on the robot
//        return TwoPixelHandlerState(PixelHandlerState.None, PixelHandlerState.None)
//    }

}