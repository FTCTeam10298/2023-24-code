package us.brainstormz.robotTwo

import com.qualcomm.hardware.rev.RevBlinkinLedDriver
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
import kotlin.math.absoluteValue

class RobotTwoAuto(private val telemetry: Telemetry) {

    data class AutoTargetWorld(
            val targetRobot: RobotState,
            val isTargetReached: (targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld)-> Boolean,
            val timeTargetStartedMilis: Long = 0L
    ) {
        val asTargetWorld: TargetWorld = TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(targetRobot.positionAndRotation),
                        depoTarget = DepoTarget(
                                armPosition = targetRobot.depoState.armPos,
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
                        lights = RobotTwoTeleOp.LightTarget(
                                targetColor = RevBlinkinLedDriver.BlinkinPattern.BLUE,
                                pattern = RobotTwoTeleOp.BothPixelsWeWant(RobotTwoTeleOp.PixelColor.Unknown, RobotTwoTeleOp.PixelColor.Unknown),
                                timeOfColorChangeMilis = timeTargetStartedMilis
                        ),
                ),
                driverInput = RobotTwoTeleOp.noInput,
                isLiftEligableForReset = false,
                doingHandoff = false,
                isTargetReached = isTargetReached,
                timeTargetStartedMilis = timeTargetStartedMilis,
                gamepad1Rumble = null
        )
    }

    data class MovementPIDSet(val x: PID, val y: PID, val r: PID)
    data class RobotState(
            val positionAndRotation: PositionAndRotation,
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
        val sideRotation: Double = startPosition.redStartPosition.r + (40.0 * rotationPolarity)

        val depositingPosition = when (propPosition) {
            RelativePropPosition.NextToTruss    -> startPosition.redStartPosition.copy(x= startPosition.redStartPosition.x + 18, r= sideRotation)
            RelativePropPosition.Center         -> startPosition.redStartPosition.copy(x= startPosition.redStartPosition.x + 5.0)
            RelativePropPosition.AwayFromTruss  -> startPosition.redStartPosition.copy(x= startPosition.redStartPosition.x + 13, r= sideRotation)
        }

        val extendoPosition = when (propPosition) {
            RelativePropPosition.Center -> ExtendoPositions.PurpleCenterPosition
            else -> ExtendoPositions.PurpleSidePosition
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
                            isTargetReached = { targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for extendo")
                                val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState, precisionInches = 1.0)
                                val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                                (isExtendoAtPosition && isRobotAtPosition) || hasTimeElapsed(4000, targetState)
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
                            isTargetReached = { targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for drivetrain to go around truss")
                                val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState, precisionInches = 1.0)
                                isRobotAtPosition || hasTimeElapsed(4000, targetState)
                            },
                    ).asTargetWorld,
                    AutoTargetWorld(
                            targetRobot = RobotState(
                                    collectorSystemState = CollectorState(CollectorPowers.Off, extendoPosition, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                    positionAndRotation = depositingPosition,
                                    depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                            ),
                            isTargetReached = { targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for extendo and rotation")
                                val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState, precisionInches = 1.0, precisionDegrees = 2.0)
                                isRobotAtPosition || hasTimeElapsed(4000, targetState)
                            },
                    ).asTargetWorld,
                    AutoTargetWorld(
                            targetRobot = RobotState(
                                    collectorSystemState = CollectorState(CollectorPowers.Off, extendoPosition, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                    positionAndRotation = depositingPosition,
                                    depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                            ),
                            isTargetReached = { targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for extendo")
                                val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                                isExtendoAtPosition || hasTimeElapsed(4000, targetState)
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
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for eject")
                            hasTimeElapsed(200, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for extendo to retract")
                            val isExtendoABitAwayFromThePurple = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks - 80)
                            isExtendoABitAwayFromThePurple || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for extendo to retract 2 electric boogaloo")
                            val isExtendoHalfwayIn = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks / 2)
                            isExtendoHalfwayIn || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                        },).asTargetWorld,
        )

        return lineUpForDeposit + depositPixel
    }

    private fun getYellowDepositingPosition(propPosition: PropPosition): PositionAndRotation = when (propPosition) {
        PropPosition.Left -> PositionAndRotation(x= -29.0, y= -55.0, r= 0.0)
        PropPosition.Center -> PositionAndRotation(x= -37.0, y= -55.0, r= 0.0)
        PropPosition.Right -> PositionAndRotation(x= -42.0, y= -55.0, r= 0.0)
    }

    private fun yellowPlacement(propPosition: PropPosition): List<TargetWorld> {
        val depositingPosition = getYellowDepositingPosition(propPosition)

        return listOf(
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for go to board")
                            val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState)
                            val isCollectorRetracted = extendo.isExtendoAtPosition(ExtendoPositions.Min.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                            (isRobotAtPosition && isCollectorRetracted) || hasTimeElapsed(3000, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            lift.isLiftAtPosition(targetState.targetRobot.depoTarget.lift.targetPosition.ticks, actualState.actualRobot.depoState.lift.currentPositionTicks) || hasTimeElapsed(1000, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            arm.isArmAtAngle(targetState.targetRobot.depoTarget.armPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees) || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            actualState.actualRobot.depoState.wristAngles == Wrist.ActualWrist(ClawTarget.Retracted.angleDegrees, ClawTarget.Retracted.angleDegrees) || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                        },).asTargetWorld,
        )
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
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = isTargetReached,).asTargetWorld


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
                            isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for robot to navigate around purple pixel")
                                drivetrain.rotationPID = drivetrain.rotationOnlyPID
                                val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState)
                                isRobotAtPosition || hasTimeElapsed(4000, targetState)
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
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for go to board")
                            val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState)
                            drivetrain.rotationPID = if (isRobotAtPosition)
                                drivetrain.rotationOnlyPID
                            else
                                drivetrain.rotationWithOtherAxisPID
                            val isCollectorRetracted = extendo.isExtendoAtPosition(ExtendoPositions.Min.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                            (isRobotAtPosition && isCollectorRetracted) || hasTimeElapsed(3000, targetState)
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
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState, previousActualState)
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
                isTargetReached = ::isRobotAtPosition))

        val driveToBackboard = listOf(
                navigatingTargetSetup(targetPosition =
                    PositionAndRotation(x = redDistanceFromCenterlineInches, y = -36.0, r = 0.0)
                ) { targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                    actualState.actualRobot.positionAndRotation.y <= targetState.targetRobot.drivetrainTarget.targetPosition.y
                },
                navigatingTargetSetup(
                        targetPosition = getYellowDepositingPosition(propPosition),
                        isTargetReached = ::isRobotAtPosition
                ),
        )

        return navigateAroundSpike + driveToBackboard
    }


    private val audienceSideParkPosition = PositionAndRotation(x= -10.0, y= -55.0, r= -10.0)
    private val audienceSidePark: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = PositionAndRotation(x= -10.0, y= -45.0, r= 0.0),
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState, previousActualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, TransferTarget(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideParkPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState, previousActualState)
                    },).asTargetWorld,
    )

    /** Path assembly */
    data class PathPreAssembled(val purplePlacementPath: (PropPosition)->List<TargetWorld>, val driveToBoardPath: (PropPosition)->List<TargetWorld>, val yellowDepositPath: (PropPosition)->List<TargetWorld>, val parkPath: List<TargetWorld>) {
        fun assemblePath(propPosition: PropPosition): List<TargetWorld> {
            return  purplePlacementPath(propPosition) +
                    driveToBoardPath(propPosition) //+
//                    yellowDepositPath(propPosition) +
//                    parkPath
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
                        yellowDepositPath = { yellowPlacement(it) },
                        parkPath = backboardSidePark
                )
            }
            StartPosition.Audience -> {
                PathPreAssembled(
                        purplePlacementPath = { purplePlacement(StartPosition.Audience, it) },
                        driveToBoardPath = ::audienceSideNavigateToBackboard,
                        yellowDepositPath = { yellowPlacement(it) },
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
    private fun nextTargetState(
            previousTargetState: TargetWorld?,
            actualState: ActualWorld,
            previousActualState: ActualWorld?): TargetWorld {
        return if (previousTargetState == null) {
            getNextTargetFromList()
        } else {
            val isTargetReached = previousTargetState.isTargetReached(previousTargetState!!, actualState, previousActualState ?: actualState)
            telemetry.addLine("isTargetReached: $isTargetReached")

            when {
                isTargetReached && autoListIterator.hasNext()-> {
                    getNextTargetFromList()
                }
                else -> {
                    previousTargetState
                }
            }
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
                hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK)
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

        hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK)
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
                        transfer= transfer,
                        extendoOverridePower = 0.0,
                        armOverridePower = 0.0
                )

                hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK)
            }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()
        telemetry.addLine("loop time: $loopTime milis")
        println("loopTime: ${loopTimeMeasurer.peakDeltaTime()}")

        telemetry.addLine("average loop time: ${loopTimeMeasurer.getAverageLoopTimeMillis()}")

        telemetry.update()
    }
}