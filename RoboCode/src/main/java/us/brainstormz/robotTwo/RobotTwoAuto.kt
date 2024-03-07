package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.RobotTwoHardware.UnchangingRobotAttributes.alliance
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.robotTwo.RobotTwoPropDetector.PropColors
import us.brainstormz.robotTwo.RobotTwoPropDetector.PropPosition
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Claw.ClawTarget
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Extendo.ExtendoPositions
import us.brainstormz.robotTwo.subsystems.Intake.CollectorPowers
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Transfer.TransferTarget
import us.brainstormz.robotTwo.subsystems.Transfer.DirectorState
import us.brainstormz.robotTwo.subsystems.Transfer.RollerPowers
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.DeltaTimeMeasurer
import kotlin.math.abs
import kotlin.math.absoluteValue

class RobotTwoAuto(private val telemetry: Telemetry, private val aprilTagPipeline: AprilTagPipeline) {
    
    data class AutoTargetWorld(
            val targetRobot: RobotState,
            val getNextTask: (targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld) -> TargetWorld,
            val timeTargetStartedMilis: Long = 0L
    ) {
        val asTargetWorld: TargetWorld = TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(
                                    targetPosition = targetRobot.positionAndRotation,
                                    power = targetRobot.drivePower,
                                    movementMode = targetRobot.movementMode
                                ),
                        depoTarget = DepoTarget(
                                armPosition = Arm.ArmTarget(targetRobot.depoState.armPos, MovementMode.Position, 0.0),
                                lift= Lift.TargetLift(
                                        targetPosition = targetRobot.depoState.liftPosition,
                                        power = 0.0,
                                        movementMode = MovementMode.Position
                                ),
                                wristPosition = Wrist.WristTargets(
                                        left= targetRobot.depoState.leftClawPosition,
                                        right= targetRobot.depoState.rightClawPosition),
                                targetType = DepoManager.DepoTargetType.GoingOut,
                        ),
                        collectorTarget = CollectorTarget(
                                extendo = SlideSubsystem.TargetSlideSubsystem(
                                        targetPosition = targetRobot.collectorSystemState.extendoPosition,
                                        power = 0.0,
                                        movementMode = MovementMode.Position
                                ),
                                intakeNoodles = targetRobot.collectorSystemState.collectorState,
                                rollers = targetRobot.collectorSystemState.transferRollersState.toRealTransferTarget(),
                                transferState = Transfer.TransferState(
                                        left = RobotTwoTeleOp.initTransferHalfState,
                                        right = RobotTwoTeleOp.initTransferHalfState
                                ),
                                timeOfEjectionStartMilis = timeTargetStartedMilis),
                        hangPowers = RobotTwoHardware.HangPowers.Holding,
                        launcherPosition = RobotTwoHardware.LauncherPosition.Holding,
                        lights = RobotTwoTeleOp.LightTarget(),
//                                RobotTwoTeleOp.LightTarget(
//                                targetColor = RevBlinkinLedDriver.BlinkinPattern.BLUE,
//                                pattern = RobotTwoTeleOp.BothPixelsWeWant(RobotTwoTeleOp.PixelColor.Unknown, RobotTwoTeleOp.PixelColor.Unknown),
//                                timeOfColorChangeMilis = timeTargetStartedMilis
//                        ),
                ),
                driverInput = RobotTwoTeleOp.noInput,
                isLiftEligableForReset = false,
                doingHandoff = false,
                getNextTask = getNextTask,
                timeTargetStartedMilis = timeTargetStartedMilis,
                gamepad1Rumble = null
        )
    }

    data class MovementPIDSet(val x: PID, val y: PID, val r: PID)
    data class RobotState(
            val positionAndRotation: PositionAndRotation,
            val movementMode: MovementMode = MovementMode.Position,
            val drivePower: Drivetrain.DrivetrainPower = Drivetrain.DrivetrainPower(),
            val movementPIDs: MovementPIDSet? = null,
            val depoState: DepoState,
            val collectorSystemState: CollectorState
    )
    data class CollectorState(
            val collectorState: CollectorPowers,
            val extendoPosition: ExtendoPositions,
            val transferRollersState: TransferTarget,
    )
    data class TransferTarget(val left: RollerPowers, val right: RollerPowers, val directorState: DirectorState) {
        fun toRealTransferTarget(): Transfer.TransferTarget {
            return TransferTarget(
                    leftServoCollect = Transfer.RollerTarget(
                            left,
                            0L
                    ),
                    rightServoCollect = Transfer.RollerTarget(
                            right,
                            0L
                    ),
                    directorState
            )
        }
    }

    data class DepoState(
            val armPos: Arm.Positions,
            val liftPosition: Lift.LiftPositions,
            val leftClawPosition: ClawTarget,
            val rightClawPosition: ClawTarget,
    )

    private fun hasTimeElapsed(timeToElapseMilis: Long, targetWorld: TargetWorld): Boolean {
        val taskStartedTimeMilis = targetWorld.timeTargetStartedMilis
        val timeSinceTargetStarted = System.currentTimeMillis() - taskStartedTimeMilis
        return timeSinceTargetStarted >= timeToElapseMilis
    }

    private fun isRobotAtPosition(targetWorld: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld, precisionInches: Double = drivetrain.precisionInches, precisionDegrees: Double = drivetrain.precisionDegrees): Boolean {
        return drivetrain.checkIfDrivetrainIsAtPosition(targetWorld.targetRobot.drivetrainTarget.targetPosition, previousWorld = previousActualState, actualWorld = actualState, precisionInches = precisionInches, precisionDegrees = precisionDegrees)
    }

    private fun isRobotAtAngle(targetWorld: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld): Boolean {
        val rotationErrorDegrees = actualState.actualRobot.positionAndRotation.r - targetWorld.targetRobot.drivetrainTarget.targetPosition.r
        return rotationErrorDegrees.absoluteValue <= 3.0
    }

    /** Generic functions */
    enum class RelativePropPosition {
        NextToTruss,
        Center,
        AwayFromTruss,
    }

    private fun getRelativePropPosition(startPosition: StartPosition, propPosition: PropPosition): RelativePropPosition =
            when (startPosition) {
                StartPosition.Backboard -> when (propPosition) {
                    PropPosition.Left -> RelativePropPosition.NextToTruss
                    PropPosition.Center -> RelativePropPosition.Center
                    PropPosition.Right -> RelativePropPosition.AwayFromTruss
                }
                StartPosition.Audience -> when (propPosition) {
                    PropPosition.Left -> RelativePropPosition.AwayFromTruss
                    PropPosition.Center -> RelativePropPosition.Center
                    PropPosition.Right -> RelativePropPosition.NextToTruss
                }
            }

    private fun purplePlacement(startPosition: StartPosition, absolutePropPosition: PropPosition): List<TargetWorld> {
        val propPosition = getRelativePropPosition(startPosition, absolutePropPosition)

        val rotationPolarity = when (absolutePropPosition) {
            PropPosition.Left -> +1
            else -> -1
        }

        val depositingPosition = when (propPosition) {
            RelativePropPosition.NextToTruss    -> startPosition.redStartPosition.copy(
                    x= startPosition.redStartPosition.x + 18,
                    r= startPosition.redStartPosition.r + (40.0*rotationPolarity))

            RelativePropPosition.Center         -> startPosition.redStartPosition.copy(
                    x= startPosition.redStartPosition.x + 5.0)

            RelativePropPosition.AwayFromTruss  -> startPosition.redStartPosition.copy(
                    x= startPosition.redStartPosition.x + 11,
                    r= startPosition.redStartPosition.r + (35.0*rotationPolarity))
        }

        val extendoPosition = when (propPosition) {
            RelativePropPosition.NextToTruss -> ExtendoPositions.PurpleFarSidePosition
            RelativePropPosition.Center -> ExtendoPositions.PurpleCenterPosition
            RelativePropPosition.AwayFromTruss -> ExtendoPositions.PurpleCloseSidePosition
        }

        val depositingMoveAroundTrussWaypoint = depositingPosition.copy(r= startPosition.redStartPosition.r)

        val lineUpForDeposit = when (propPosition) {
            RelativePropPosition.Center -> listOf(
                    AutoTargetWorld(
                            targetRobot = RobotState(
                                    collectorSystemState = CollectorState(CollectorPowers.Off, extendoPosition, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                    positionAndRotation = depositingPosition,
                                    depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                            ),
                            getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for extendo")
                                val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState, precisionInches = 1.0)
                                val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                                val taskIsDone = (isExtendoAtPosition && isRobotAtPosition) || hasTimeElapsed(4000, targetState)
                                nextTargetFromCondition(taskIsDone, targetState)
                            },
                    ).asTargetWorld,
            )
            else -> listOf(
                    AutoTargetWorld(
                            targetRobot = RobotState(
                                    collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                    positionAndRotation = depositingMoveAroundTrussWaypoint,
                                    depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                            ),
                            getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for drivetrain to go around truss")
                                val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState, precisionInches = 1.0)
                                val taskIsDone = isRobotAtPosition || hasTimeElapsed(4000, targetState)
                                nextTargetFromCondition(taskIsDone, targetState)
                            },
                    ).asTargetWorld,
                    AutoTargetWorld(
                            targetRobot = RobotState(
                                    collectorSystemState = CollectorState(CollectorPowers.Off, extendoPosition, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                    positionAndRotation = depositingPosition,
                                    depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                            ),
                            getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for extendo and rotation")
                                val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState, precisionInches = 1.0, precisionDegrees = 2.0)
                                val taskIsDone = isRobotAtPosition || hasTimeElapsed(4000, targetState)
                                nextTargetFromCondition(taskIsDone, targetState)
                            },
                    ).asTargetWorld,
                    AutoTargetWorld(
                            targetRobot = RobotState(
                                    collectorSystemState = CollectorState(CollectorPowers.Off, extendoPosition, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                    positionAndRotation = depositingPosition,
                                    depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                            ),
                            getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for extendo")
                                val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                                val taskIsDone = isExtendoAtPosition || hasTimeElapsed(4000, targetState)
                                nextTargetFromCondition(taskIsDone, targetState)
                            },
                    ).asTargetWorld,
            )
        }

        val depositPixel = listOf(
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, extendoPosition, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for eject")
                            val taskIsDone = hasTimeElapsed(200, targetState)
                            nextTargetFromCondition(taskIsDone, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for extendo to retract")
                            val isExtendoABitAwayFromThePurple = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks - 80)
                            val taskIsDone = isExtendoABitAwayFromThePurple || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                            nextTargetFromCondition(taskIsDone, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for extendo to retract 2 electric boogaloo")
                            val isExtendoHalfwayIn = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks / 2)
                            val taskIsDone = isExtendoHalfwayIn || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                            nextTargetFromCondition(taskIsDone, targetState)
                        },).asTargetWorld,
        )

        return lineUpForDeposit + depositPixel
    }

    private fun getYellowDepositingPosition(propPosition: PropPosition): PositionAndRotation = when (propPosition) {
        PropPosition.Left -> PositionAndRotation(x= -30.5, y= -55.0, r= 0.0)
        PropPosition.Center -> PositionAndRotation(x= -37.0, y= -55.0, r= 0.0)
        PropPosition.Right -> PositionAndRotation(x= -42.0, y= -55.0, r= 0.0)
    }


    private val aprilTagLineupXPID = PID(
            "x",
            kp = 0.12,
            ki = 1.2E-6,
            kd = 1.0
    )

    private fun yellowPlacement(propPosition: PropPosition, startPosition: StartPosition, alliance: RobotTwoHardware.Alliance): List<TargetWorld> {
        val depositingPosition = getYellowDepositingPosition(propPosition)
//        return when (startPosition) {
//            StartPosition.Backboard -> {
        return listOf(
                        AutoTargetWorld(
                                targetRobot = RobotState(
                                        collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                        positionAndRotation = depositingPosition,
                                        depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                                ),
                                getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                    val xError = actualState.actualRobot.positionAndRotation.x - targetState.targetRobot.drivetrainTarget.targetPosition.x
                                    val yError = actualState.actualRobot.positionAndRotation.y - targetState.targetRobot.drivetrainTarget.targetPosition.y
                                    val isRobotAtPosition = abs(xError) < 1.5 && abs(yError) < 3.0//targetStateisRobotAtPosition(targetState, actualState, previousActualState, precisionInches = 2.0, precisionDegrees = 5.0)
//                                    val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState, precisionInches = 2.0, precisionDegrees = 5.0)
                                    nextTargetFromCondition(isRobotAtPosition || hasTimeElapsed(1000, targetState), targetState)
                                },).asTargetWorld,
                        AutoTargetWorld(
                                targetRobot = RobotState(
                                        collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                        positionAndRotation = depositingPosition,
                                        depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                                ),
                                getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                    val taskIsDone = lift.isLiftAtPosition(targetState.targetRobot.depoTarget.lift.targetPosition.ticks, actualState.actualRobot.depoState.lift.currentPositionTicks) || hasTimeElapsed(500, targetState)
                                    nextTargetFromCondition(taskIsDone, targetState)
                                },).asTargetWorld,
                        AutoTargetWorld(
                                targetRobot = RobotState(
                                        collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                        positionAndRotation = depositingPosition,
                                        depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                                ),
                                getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                    val taskIsDone = arm.isArmAtAngle(targetState.targetRobot.depoTarget.armPosition.targetPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees) || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                                    nextTargetFromCondition(taskIsDone, targetState)
                                },).asTargetWorld,
                        AutoTargetWorld(
                                targetRobot = RobotState(
                                        collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                        positionAndRotation = depositingPosition,
                                        movementMode = MovementMode.Power,
                                        drivePower = Drivetrain.DrivetrainPower(y= -0.2, x= 0.0, r= 0.0),
                                        depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                                ),
                                getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                    val taskIsDone = actualState.actualRobot.depoState.wristAngles == Wrist.ActualWrist(ClawTarget.Retracted.angleDegrees, ClawTarget.Retracted.angleDegrees) || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                                    nextTargetFromCondition(taskIsDone, targetState)
                                },).asTargetWorld,
                )
//            }
//            StartPosition.Audience -> {
//
//                val awayFromBoardPosition = depositingPosition.copy(y = depositingPosition.y + 15)
//
//                fun modifyTargetWorldToLineUpToTag(targetWorld: TargetWorld, actualWorld: ActualWorld, previousActualWorld: ActualWorld): TargetWorld {
//                    if (targetWorld.targetRobot.drivetrainTarget.targetPosition.x != depositingPosition.x){
//                        drivetrain.xTranslationPID = aprilTagLineupXPID
//                    }
//
//                    val tag = aprilTagLineup.findDetection(actualWorld.aprilTagReadings, propPosition, alliance)
//                    val previousTag = aprilTagLineup.findDetection(previousActualWorld.aprilTagReadings, propPosition, alliance)
//
//                    val targetPosition = aprilTagLineup.getTargetPositionToLineupWithTag(
//                            currentPosition = targetWorld.targetRobot.drivetrainTarget.targetPosition,
//                            previousPosition = previousActualWorld.actualRobot.positionAndRotation,
//                            aprilTagReading = tag,
//                            previousAprilTagReading = previousTag
//                    )
//
//                    return targetWorld.copy(targetRobot = targetWorld.targetRobot.copy(drivetrainTarget = Drivetrain.DrivetrainTarget(targetPosition)))
//                }
//
//                listOf(
////                AutoTargetWorld(
////                        targetRobot = RobotState(
////                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
////                                positionAndRotation = awayFromBoardPosition,
////                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
////                        ),
////                        getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
////                            telemetry.addLine("Waiting for go to board")
////                            val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState, precisionInches = 2.0, precisionDegrees = 3.0)
////                            val taskIsDone = isRobotAtPosition || hasTimeElapsed(3000, targetState)
//////                            val isCollectorRetracted = extendo.isExtendoAtPosition(ExtendoPositions.Min.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
//////                            (isRobotAtPosition && isCollectorRetracted) || hasTimeElapsed(3000, targetState)
////                            nextTargetFromCondition(taskIsDone, targetState)
////                        },).asTargetWorld,
//                        AutoTargetWorld(
//                                targetRobot = RobotState(
//                                        collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
//                                        positionAndRotation = awayFromBoardPosition,
//                                        depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
//                                ),
//                                getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
//                                    val newTargetWorld = modifyTargetWorldToLineUpToTag(targetState, actualState, previousActualState)
//                                    nextTargetFromCondition(isRobotAtPosition(newTargetWorld, actualState, previousActualState, precisionInches = 1.5, precisionDegrees = 5.0) || hasTimeElapsed(5000, newTargetWorld), newTargetWorld)
//                                },).asTargetWorld,
//                        AutoTargetWorld(
//                                targetRobot = RobotState(
//                                        collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
//                                        positionAndRotation = depositingPosition,
//                                        depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
//                                ),
//                                getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
//                                    val taskIsDone = lift.isLiftAtPosition(targetState.targetRobot.depoTarget.lift.targetPosition.ticks, actualState.actualRobot.depoState.lift.currentPositionTicks) || hasTimeElapsed(1000, targetState)
//                                    nextTargetFromCondition(taskIsDone, modifyTargetWorldToLineUpToTag(targetState, actualState, previousActualState))
//                                },).asTargetWorld,
//                        AutoTargetWorld(
//                                targetRobot = RobotState(
//                                        collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
//                                        positionAndRotation = depositingPosition,
//                                        depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
//                                ),
//                                getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
//                                    val taskIsDone = arm.isArmAtAngle(targetState.targetRobot.depoTarget.armPosition.targetPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees) || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
//                                    nextTargetFromCondition(taskIsDone, modifyTargetWorldToLineUpToTag(targetState, actualState, previousActualState))
//                                },).asTargetWorld,
//                        AutoTargetWorld(
//                                targetRobot = RobotState(
//                                        collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
//                                        positionAndRotation = depositingPosition,
//                                        depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
//                                ),
//                                getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
//                                    val robotYDelta = actualState.actualRobot.positionAndRotation.y - targetState.targetRobot.drivetrainTarget.targetPosition.y
//                                    val isRobotAtBoard = robotYDelta <= -1
//                                    telemetry.addLine("isRobotAtBoard: $isRobotAtBoard")
//                                    nextTargetFromCondition(isRobotAtBoard || hasTimeElapsed(timeToElapseMilis = 5000, targetState), modifyTargetWorldToLineUpToTag(targetState, actualState, previousActualState))
//                                },).asTargetWorld,
//                        AutoTargetWorld(
//                                targetRobot = RobotState(
//                                        collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
//                                        positionAndRotation = depositingPosition,
//                                        movementMode = MovementMode.Power,
//                                        drivePower = Drivetrain.DrivetrainPower(y= -0.5, x= 0.0, r= 0.0),
//                                        depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
//                                ),
//                                getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
//                                    val taskIsDone = actualState.actualRobot.depoState.wristAngles == Wrist.ActualWrist(ClawTarget.Retracted.angleDegrees, ClawTarget.Retracted.angleDegrees) || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
//                                    nextTargetFromCondition(taskIsDone, modifyTargetWorldToLineUpToTag(targetState, actualState, previousActualState))
//                                },).asTargetWorld,
//                )
//            }
//        }

    }

    fun navigatingTargetSetup(
            targetPosition: PositionAndRotation,
            isTargetReached: (targetState: TargetWorld,
                              actualState: ActualWorld,
                              previousActualState: ActualWorld)->Boolean): TargetWorld =
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = targetPosition,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    getNextTask = { targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        nextTargetFromCondition(isTargetReached(targetState, actualState, previousActualState), targetState)
                    },).asTargetWorld


    /** Backboard side */
    private fun backboardSideNavigateToBackboard(propPosition: PropPosition): List<TargetWorld> {
        val startPosition: StartPosition = StartPosition.Backboard

        val moveAroundRightPixelPosition = startPosition.redStartPosition.copy(x= startPosition.redStartPosition.x + 10.0)
        val moveAroundDepositedPixel = when (propPosition) {
            PropPosition.Right -> listOf(
                    AutoTargetWorld(
                            targetRobot = RobotState(
                                    collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                    positionAndRotation = moveAroundRightPixelPosition,
                                    depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                            ),
                            getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for robot to navigate around purple pixel")
                                drivetrain.rotationPID = drivetrain.rotationOnlyPID
                                val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState)
                                val taskIsDone = isRobotAtPosition || hasTimeElapsed(4000, targetState)
                                nextTargetFromCondition(taskIsDone, targetState)
                            },).asTargetWorld
            )
            else -> listOf()
        }

        val depositingPosition = getYellowDepositingPosition(propPosition)
        val moveToBackboard = listOf(
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for go to board")
                            val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState)
                            drivetrain.rotationPID = if (isRobotAtPosition)
                                drivetrain.rotationOnlyPID
                            else
                                drivetrain.rotationWithOtherAxisPID
                            val isCollectorRetracted = extendo.isExtendoAtPosition(ExtendoPositions.Min.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                            val taskIsDone = (isRobotAtPosition && isCollectorRetracted) || hasTimeElapsed(2000, targetState)
                            nextTargetFromCondition(taskIsDone, targetState)
                        },).asTargetWorld,
        )

        return moveAroundDepositedPixel + moveToBackboard
    }

    private val backboardSideParkingPosition = PositionAndRotation(x= -58.0, y= -48.0, r= 0.0)
    private val backboardSidePark: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = backboardSideParkingPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        val taskIsDone = isRobotAtPosition(targetState, actualState, previousActualState)
                        nextTargetFromCondition(taskIsDone, targetState)
                    },).asTargetWorld
    )

    /** Audience side */
    private fun audienceSideNavigateToBackboard(propPosition: PropPosition): List<TargetWorld> {
        val startPosition = StartPosition.Audience.redStartPosition

        val redDistanceFromCenterlineInches = -((RobotTwoHardware.robotWidthInches/2)+1.0)

        val readyToGoAroundTrussWaypoint = PositionAndRotation(x= redDistanceFromCenterlineInches, y= 40.0, r= 0.0)

        val navigateAroundSpike = when (propPosition) {
            PropPosition.Center -> listOf(
                    navigatingTargetSetup(
                            targetPosition =
                                startPosition.copy(x= startPosition.x + 20.0, y= 50.0),
                            isTargetReached = ::isRobotAtPosition
                    ),
                    navigatingTargetSetup(
                            targetPosition =
                                startPosition.copy(x= redDistanceFromCenterlineInches - 8, y= 50.0),
                            isTargetReached = ::isRobotAtPosition
                    ),
                    navigatingTargetSetup(
                            targetPosition =
                                startPosition.copy(x= redDistanceFromCenterlineInches - 5, y = 52.0, r = 0.0),
                            isTargetReached = ::isRobotAtAngle
                    ),
            )
            else -> {
                val travelingAroundSidePixelsYPosition = startPosition.y + if (propPosition == PropPosition.Right) {
                    5
                } else {
                    0
                }

                listOf(
                        navigatingTargetSetup(
                                targetPosition =
                                    startPosition.copy(x = startPosition.x + 18.0, y = travelingAroundSidePixelsYPosition),
                                isTargetReached = ::isRobotAtPosition
                        ),
                        navigatingTargetSetup(
                                targetPosition =
                                    startPosition.copy(x = redDistanceFromCenterlineInches - 8, y = travelingAroundSidePixelsYPosition),
                                isTargetReached = ::isRobotAtPosition
                        ),
                )
            }
        } + listOf(navigatingTargetSetup(
                targetPosition = readyToGoAroundTrussWaypoint,
                isTargetReached = { targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                    val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState)

                    drivetrain.rotationPID = if (isRobotAtPosition)
                        drivetrain.rotationOnlyPID
                    else
                        drivetrain.rotationWithOtherAxisPID

                    isRobotAtPosition
                }))

        val driveToBackboard = listOf(
                navigatingTargetSetup(targetPosition =
                    PositionAndRotation(x = redDistanceFromCenterlineInches, y = -36.0, r = 0.0)
                ) { targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                    actualState.actualRobot.positionAndRotation.y <= targetState.targetRobot.drivetrainTarget.targetPosition.y
                },
                navigatingTargetSetup(
                        targetPosition = getYellowDepositingPosition(propPosition).copy(y= -45.0),
                        isTargetReached = ::isRobotAtPosition
                ),
        )

        return navigateAroundSpike + driveToBackboard
    }


    private val audienceSideParkPosition = PositionAndRotation(x= -10.0, y= -55.0, r= -10.0)
//    private val audienceSideParkPosition = PositionAndRotation(x= -16.5/2, y= -(17 + 11/16)/2.0, r= 0.0)
    private val audienceSidePark: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = PositionAndRotation(x= -10.0, y= -45.0, r= 0.0),
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        val taskIsDone = isRobotAtPosition(targetState, actualState, previousActualState)
                        nextTargetFromCondition(taskIsDone, targetState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideParkPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    getNextTask = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        val taskIsDone = isRobotAtPosition(targetState, actualState, previousActualState)
                        nextTargetFromCondition(taskIsDone, targetState)
                    },).asTargetWorld,
    )

    /** Path assembly */
    data class PathPreAssembled(val purplePlacementPath: (PropPosition)->List<TargetWorld>, val driveToBoardPath: (PropPosition)->List<TargetWorld>, val yellowDepositPath: (PropPosition)->List<TargetWorld>, val parkPath: List<TargetWorld>) {
        fun assemblePath(propPosition: PropPosition): List<TargetWorld> {
            return  purplePlacementPath(propPosition) +
                    driveToBoardPath(propPosition) +
                    yellowDepositPath(propPosition) +
                    parkPath
        }
    }

    enum class StartPosition(val redStartPosition: PositionAndRotation) {
        Backboard(PositionAndRotation(  x = RobotTwoHardware.redStartingXInches,
                y= -12.0,
                r= RobotTwoHardware.redStartingRDegrees)),
        Audience(PositionAndRotation(   x = RobotTwoHardware.redStartingXInches,
                y= 36.0,
                r= RobotTwoHardware.redStartingRDegrees))
    }

    private fun calcAutoTargetStateList(
            alliance: RobotTwoHardware.Alliance,
            startPosition: StartPosition,
            propPosition: PropPosition
    ): List<TargetWorld> {

//        strafe as soon as claws closed (don't wait for arm/lift)
//        place purple before moving much

        val redPath: PathPreAssembled = when (startPosition) {
            StartPosition.Backboard -> {
                PathPreAssembled(
                        purplePlacementPath = { purplePlacement(StartPosition.Backboard, it) },
                        driveToBoardPath = ::backboardSideNavigateToBackboard,
                        yellowDepositPath = { yellowPlacement(it, StartPosition.Backboard, alliance) },
                        parkPath = backboardSidePark
                )
            }
            StartPosition.Audience -> {
                PathPreAssembled(
                        purplePlacementPath = { purplePlacement(StartPosition.Audience, it) },
                        driveToBoardPath = ::audienceSideNavigateToBackboard,
                        yellowDepositPath = { yellowPlacement(it, StartPosition.Audience, alliance) },
                        parkPath = audienceSidePark
                )
            }
        }

        val allianceIsColorBlue = alliance == RobotTwoHardware.Alliance.Blue
        val propPositionSwappedToMatchBlue = when (propPosition) {
            PropPosition.Left -> PropPosition.Right
            PropPosition.Center -> PropPosition.Center  
            PropPosition.Right -> PropPosition.Left
        }
        val adjustedPropPosition = if (allianceIsColorBlue) {
            propPositionSwappedToMatchBlue
        } else {
            propPosition
        }

        val allianceMirroredAndAsList = if (allianceIsColorBlue) {
            mirrorRedAutoToBlue(redPath.assemblePath(adjustedPropPosition))
        } else {
            redPath.assemblePath(adjustedPropPosition)
        }

        return allianceMirroredAndAsList
    }

    private fun mirrorRedAutoToBlue(auto: List<TargetWorld>): List<TargetWorld> {
        return auto.map { targetWorld ->
            val flippedBluePosition = flipRedPositionToBlue(targetWorld.targetRobot.drivetrainTarget.targetPosition)
            targetWorld.copy(targetRobot = targetWorld.targetRobot.copy(drivetrainTarget = targetWorld.targetRobot.drivetrainTarget.copy(targetPosition = flippedBluePosition)))
        }
    }
    private fun flipRedPositionToBlue(positionAndRotation: PositionAndRotation): PositionAndRotation {
        return positionAndRotation.copy(x= -positionAndRotation.x, r= -positionAndRotation.r)
    }

    private fun getNextTargetFromList(): TargetWorld {
        return autoListIterator.next().copy(timeTargetStartedMilis = System.currentTimeMillis())
    }

    private lateinit var autoStateList: List<TargetWorld>
    private lateinit var autoListIterator: ListIterator<TargetWorld>
    private fun nextTargetState(previousTargetState: TargetWorld?, actualState: ActualWorld, previousActualState: ActualWorld?): TargetWorld {
        return if (previousTargetState == null) {
            getNextTargetFromList()
        } else {
            when {
                autoListIterator.hasNext()-> {
                    previousTargetState.getNextTask(previousTargetState, actualState, previousActualState ?: actualState) ?: previousTargetState
                }
                else -> {
                    previousTargetState
                }
            }
        }
    }

    private fun nextTargetFromCondition(condition: Boolean, previousTargetState: TargetWorld) : TargetWorld {
        return if (condition) {
            getNextTargetFromList()
        } else {
            previousTargetState
        }
    }



    private val console = TelemetryConsole(telemetry)
    private val wizard = TelemetryWizard(console, null)
    data class WizardResults(val alliance: RobotTwoHardware.Alliance, val startPosition: StartPosition)

    private fun getMenuWizardResults(gamepad1: Gamepad): WizardResults {
        return WizardResults(
                alliance = when (wizard.wasItemChosen("alliance", "Red")) {
                    true -> RobotTwoHardware.Alliance.Red
                    false -> RobotTwoHardware.Alliance.Blue
                },
                startPosition = when (wizard.wasItemChosen("startingPos", "Audience")) {
                    true -> StartPosition.Audience
                    false -> StartPosition.Backboard
                }
        )
    }

    private val aprilTagLineup = AprilTagLineup()

    private lateinit var drivetrain: Drivetrain

    private val intake = Intake()
    private val transfer = Transfer(telemetry)
    private val extendo = Extendo(telemetry)

    private lateinit var collectorSystem: CollectorSystem
    private lateinit var arm: Arm
    private lateinit var lift: Lift
    private lateinit var wrist: Wrist
    private lateinit var depoManager: DepoManager

    private var startPosition: StartPosition = StartPosition.Backboard

    private var propDetector: RobotTwoPropDetector? = null

    fun init(hardware: RobotTwoHardware, opencv: OpenCvAbstraction) {
        val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        drivetrain = Drivetrain(hardware, odometryLocalizer, telemetry)

        collectorSystem = CollectorSystem(transfer= transfer, extendo= extendo, telemetry= telemetry)
        lift = Lift(telemetry)
        arm = Arm()
        wrist = Wrist(left = Claw(telemetry), right = Claw(telemetry), telemetry= telemetry)
        depoManager = DepoManager(arm= arm, lift= lift, wrist= wrist, telemetry= telemetry)

        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"), nextMenu = "startingPos", firstMenu = true)
        wizard.newMenu("startingPos", "What side of the truss are we on?", listOf("Audience", "Backboard"))

        opencv.internalCamera = false
        opencv.cameraName = "Webcam 1"
        opencv.cameraOrientation = OpenCvCameraRotation.UPRIGHT
    }

    private fun runCamera(opencv: OpenCvAbstraction, wizardResults: WizardResults) {
        val propColor: PropColors = when (wizardResults.alliance) {
            RobotTwoHardware.Alliance.Blue -> PropColors.Blue
            RobotTwoHardware.Alliance.Red -> PropColors.Red
        }
        propDetector = RobotTwoPropDetector(telemetry, propColor)
        opencv.onNewFrame(propDetector!!::processFrame)
    }

    private var wizardResults: WizardResults? = null
    fun init_loop(hardware: RobotTwoHardware, opencv: OpenCvAbstraction, gamepad1: Gamepad) {
        if (wizardResults == null) {
            val isWizardDone = wizard.summonWizard(gamepad1)
            if (isWizardDone) {
                wizardResults = getMenuWizardResults(gamepad1)

                alliance = wizardResults!!.alliance
                startPosition = wizardResults!!.startPosition

                runCamera(opencv, wizardResults!!)
//                hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK)
            }
        } else {
            telemetry.addLine("propPosition? = ${propDetector?.propPosition}")
            telemetry.addLine("wizardResults = ${wizardResults}")
        }
    }

    fun start(hardware: RobotTwoHardware, opencv: OpenCvAbstraction) {
        val propPosition = propDetector?.propPosition ?: PropPosition.Right
        opencv.stop()

        val startPositionAndRotation: PositionAndRotation = when (alliance) {
            RobotTwoHardware.Alliance.Red -> startPosition.redStartPosition
            RobotTwoHardware.Alliance.Blue -> flipRedPositionToBlue(startPosition.redStartPosition)
        }

        drivetrain.localizer.setPositionAndRotation(startPositionAndRotation)

        autoStateList = calcAutoTargetStateList(alliance, startPosition, propPosition)
        autoListIterator = autoStateList.listIterator()

//        hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK)
    }

    val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    private val loopTimeMeasurer = DeltaTimeMeasurer()
    fun loop(hardware: RobotTwoHardware, gamepad1: Gamepad) {
        functionalReactiveAutoRunner.loop(
            actualStateGetter = { previousActualState ->
                val currentGamepad1 = Gamepad()
                currentGamepad1.copy(gamepad1)
                ActualWorld(
                        actualRobot = hardware.getActualState(drivetrain, collectorSystem, depoManager, previousActualState),
                        aprilTagReadings = aprilTagPipeline.detections(),
                        actualGamepad1 = currentGamepad1,
                        actualGamepad2 = currentGamepad1,
                        timestampMilis = System.currentTimeMillis()
                )
            },
            targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                nextTargetState(previousTargetState, actualState, previousActualState)
            },
            stateFulfiller = { targetState, previousTargetState, actualState ->
                telemetry.addLine("target position: ${targetState.targetRobot.drivetrainTarget.targetPosition}")
                telemetry.addLine("current position: ${drivetrain.localizer.currentPositionAndRotation()}")

                val universalTargetWorld: TargetWorld = targetState

                hardware.actuateRobot(
                        universalTargetWorld,
                        previousTargetState?: targetState,
                        actualState,
                        drivetrain = drivetrain,
                        wrist= wrist,
                        arm= arm,
                        lift= lift,
                        extendo= extendo,
                        intake= intake,
                        transfer= transfer
                )

//                hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK)
            }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()
        telemetry.addLine("loop time: $loopTime milis")
        println("loopTime: ${loopTimeMeasurer.peakDeltaTime()}")

        telemetry.addLine("average loop time: ${loopTimeMeasurer.getAverageLoopTimeMillis()}")

        telemetry.update()
    }
}