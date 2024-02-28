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
import us.brainstormz.robotTwo.CollectorSystem.*
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
import us.brainstormz.robotTwo.subsystems.Transfer.RollerState
import us.brainstormz.robotTwo.subsystems.Transfer.DirectorState
import us.brainstormz.robotTwo.subsystems.Transfer.RollerPowers
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.DeltaTimeMeasurer
import java.io.File

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
                                rollers = targetRobot.collectorSystemState.transferRollersState,
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


    private fun purplePlacement(startPosition: StartPosition, propPosition: PropPosition): List<TargetWorld> {
        fun sidePropPosition(rotationPolarity: Int) = startPosition.redStartPosition.copy(x= startPosition.redStartPosition.x + 18.0, r= startPosition.redStartPosition.r + (40.0*rotationPolarity))
        val depositingPosition = when (propPosition) {
            PropPosition.Left -> sidePropPosition(+1)
            PropPosition.Center -> startPosition.redStartPosition.copy(x= startPosition.redStartPosition.x + 5.0)
            PropPosition.Right -> sidePropPosition(-1)
        }
        val extendoPosition = when (propPosition) {
            PropPosition.Left -> ExtendoPositions.PurpleSidePosition
            PropPosition.Center -> ExtendoPositions.PurpleCenterPosition
            PropPosition.Right -> ExtendoPositions.PurpleSidePosition
        }

        val depositingMoveAroundTrussWaypoint = depositingPosition.copy(r= startPosition.redStartPosition.r)

        val lineUpForDeposit = when (propPosition) {
            PropPosition.Center -> listOf(
                    AutoTargetWorld(
                            targetRobot = RobotState(
                                    collectorSystemState = CollectorState(CollectorPowers.Off, extendoPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
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
                                    collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
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
                                    collectorSystemState = CollectorState(CollectorPowers.Off, extendoPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                    positionAndRotation = depositingPosition,
                                    depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                            ),
                            isTargetReached = { targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                                telemetry.addLine("Waiting for extendo")
                                val isRobotAtPosition = isRobotAtPosition(targetState, actualState, previousActualState, precisionInches = 1.0, precisionDegrees = 2.0)
                                isRobotAtPosition || hasTimeElapsed(4000, targetState)
                            },
                    ).asTargetWorld,
                    AutoTargetWorld(
                            targetRobot = RobotState(
                                    collectorSystemState = CollectorState(CollectorPowers.Off, extendoPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
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
        return lineUpForDeposit + listOf(
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, extendoPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for eject")
                            hasTimeElapsed(200, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
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
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            telemetry.addLine("Waiting for extendo to retract 2")
                            val isExtendoHalfwayIn = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks / 2)
                            isExtendoHalfwayIn || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                        },).asTargetWorld,
        )
    }

    private val placingOnBackboardLeft = PositionAndRotation(x= -28.0, y= -55.0, r= 0.0)
    private val placingOnBackboardCenter = PositionAndRotation(x= -36.0, y= -55.0, r= 0.0)
    private val placingOnBackboardRight = PositionAndRotation(x= -42.0, y= -55.0, r= 0.0)

    /** Backboard side */
    private val parkingPosition = PositionAndRotation(x= -58.0, y= -48.0, r= 0.0)

    private fun backboardSideYellow(depositingPosition: PositionAndRotation): List<TargetWorld> {

//        PropPosition.Left to backboardSideYellow(placingOnBackboardLeft),
//        PropPosition.Center to backboardSideYellow(placingOnBackboardCenter),
//        PropPosition.Right to backboardSideYellow(placingOnBackboardRight)

        return listOf(
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
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
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            lift.isLiftAtPosition(targetState.targetRobot.depoTarget.lift.targetPosition.ticks, actualState.actualRobot.depoState.lift.currentPositionTicks) || hasTimeElapsed(1000, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            arm.isArmAtAngle(targetState.targetRobot.depoTarget.armPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees) || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                        },).asTargetWorld,
                AutoTargetWorld(
                        targetRobot = RobotState(
                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                                positionAndRotation = depositingPosition,
                                depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                        ),
                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                            actualState.actualRobot.depoState.wristAngles == Wrist.ActualWrist(ClawTarget.Retracted.angleDegrees, ClawTarget.Retracted.angleDegrees) || hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                        },).asTargetWorld,
//                AutoTargetWorld(
//                        targetRobot = RobotState(
//                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
//                                positionAndRotation = depositingPosition,
//                                depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
//                        ),
//                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
//                            arm.isArmAtAngle(targetState.targetRobot.depoTarget.armPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees)
//                        },).asTargetWorld,
//                AutoTargetWorld(
//                        targetRobot = RobotState(
//                                collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
//                                positionAndRotation = depositingPosition,
//                                depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
//                        ),
//                        isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
//                            lift.isLiftAtPosition(targetState.targetRobot.depoTarget.lift.targetPosition.ticks, actualState.actualRobot.depoState.lift.currentPositionTicks)
//                        },).asTargetWorld,
        )
    }
    private val backboardSideYellowMapToProp: Map<PropPosition, List<TargetWorld>> = mapOf(
            PropPosition.Left to backboardSideYellow(placingOnBackboardLeft),
            PropPosition.Center to backboardSideYellow(placingOnBackboardCenter),
            PropPosition.Right to backboardSideYellow(placingOnBackboardRight)
    )
    private val FAKEBackboardSideYellowMapToProp: Map<PropPosition, List<TargetWorld>> = mapOf(
            PropPosition.Left to emptyList(),
            PropPosition.Center to emptyList(),
            PropPosition.Right to emptyList()
    )

    private val backboardSidePark: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = parkingPosition,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState, previousActualState)
                    },).asTargetWorld,
    )

    /** Audience side */
    val redDistanceFromCenterlineInches = -((RobotTwoHardware.robotWidthInches/2)+0.5)
    private val audienceSideNavigateUnderTrussWaypoint1 = PositionAndRotation(x= redDistanceFromCenterlineInches, y=-30.0, r= 0.0)
    private val audienceSideParkPosition = PositionAndRotation(x= -10.0, y= -47.0, r= 0.0)

    private val audienceSideDepositPurpleLeft = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x + 1.0, r= StartPosition.Audience.redStartPosition.r+22.0)
    private val audienceSideNavigateBetweenTapeWaypoint0 = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x + 5.0, y= StartPosition.Audience.redStartPosition.y + 1)//, r= StartPosition.Audience.redStartPosition.r+5)
    private val audienceSideNavigateBetweenTapeWaypoint1 = StartPosition.Audience.redStartPosition.copy(x= -14.0, y= StartPosition.Audience.redStartPosition.y + 1)//, r= StartPosition.Audience.redStartPosition.r+5)
    private val audienceSideNavigateBetweenTapeWaypoint2 = StartPosition.Audience.redStartPosition.copy(x= -14.0, y= StartPosition.Audience.redStartPosition.y + 1, r= 0.0)//, r= StartPosition.Audience.redStartPosition.r+5)
    private val audienceSideLeftPurple: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.PurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState, previousActualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.PurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        val isExtendoABitAwayFromThePurple = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks - 80)
                        isExtendoABitAwayFromThePurple
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        val isExtendoHalfwayIn = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },).asTargetWorld,
//            AutoTargetWorld(
//                    targetRobot = RobotState(
//                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
//                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint1,
//                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
//                    ),
//                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
//                        isRobotAtPosition(targetState, actualState, previousActualState)
//                    },).asTargetWorld,
//            AutoTargetWorld(
//                    targetRobot = RobotState(
//                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
//                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint2,
//                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
//                    ),
//                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
//                        isRobotAtPosition(targetState, actualState, previousActualState) && hasTimeElapsed(500, targetState)
//                    },).asTargetWorld,
            )

    private val audienceSideDepositPurpleCenter = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 6.5)
    private val audienceSideNavigateAroundTapeWaypoint1 = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 15.0, y=50.0)
    private val audienceSideNavigateAroundTapeWaypoint2 = audienceSideNavigateAroundTapeWaypoint1.copy(x= redDistanceFromCenterlineInches-10)
    private val audienceSideNavigateAroundTapeWaypoint3 = PositionAndRotation(x= redDistanceFromCenterlineInches, y= 50.0, r= 0.0)
    private val audienceSideNavigateAroundTapeWaypoint4 = PositionAndRotation(x= redDistanceFromCenterlineInches, y= 50.0, r= 0.0)

    /** Audience Drive To Board */
    private val audienceDriveToBoard: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        actualState.actualRobot.positionAndRotation.y <= 5.0
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint1,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState, previousActualState)
                    },).asTargetWorld,
            )


    /** Audience Drop Yellow */
    private val audienceSideDepositLeft = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardLeft,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState, previousActualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardLeft,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        hasTimeElapsed(500, targetState)
                    },).asTargetWorld,
    )

    private val audienceSideDepositCenter = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState, previousActualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        hasTimeElapsed(1000, targetState)
                    },).asTargetWorld,
    )
    private val audienceSideDepositRight = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardRight,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState, previousActualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardRight,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        hasTimeElapsed(500, targetState)
                    },).asTargetWorld,
    )

    private val audienceSideYellowMapToProp: Map<PropPosition, List<TargetWorld>> = mapOf(
            PropPosition.Left to audienceSideDepositLeft,
            PropPosition.Center to audienceSideDepositCenter,
            PropPosition.Right to audienceSideDepositRight
    )

    private val audienceSidePark = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideParkPosition,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.WaitForArmToMove, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoTarget.armPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideParkPosition,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld->
                        isRobotAtPosition(targetState, actualState, previousActualState)
                    },).asTargetWorld,
    )

    data class PathPreAssembled(val purplePlacementPath: (PropPosition)->List<TargetWorld>, val driveToBoardPath: List<TargetWorld>, val yellowDepositPath: Map<PropPosition, List<TargetWorld>>, val parkPath: List<TargetWorld>) {
        fun assemblePath(propPosition: PropPosition): List<TargetWorld> {
            val purplePath = purplePlacementPath(propPosition)
            val yellowPath = yellowDepositPath[propPosition] ?: emptyList()
            return purplePath + driveToBoardPath + yellowPath + parkPath
        }
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
                        driveToBoardPath = emptyList(),
                        yellowDepositPath = backboardSideYellowMapToProp,
                        parkPath = backboardSidePark
                )
            }
            StartPosition.Audience -> {
                PathPreAssembled(
                        purplePlacementPath = { purplePlacement(StartPosition.Audience, it) },
                        driveToBoardPath = emptyList(),
                        yellowDepositPath = mapOf(
                                PropPosition.Left to emptyList(),
                                PropPosition.Center to emptyList(),
                                PropPosition.Right to emptyList()
                        ),
                        parkPath = emptyList()
                )
            }
//            StartPosition.Audience -> {
//                PathPreAssembled(
//                        purplePlacementPath = audienceSidePurpleMapToProp,
//                        driveToBoardPath = audienceDriveToBoard,
//                        yellowDepositPath = audienceSideYellowMapToProp,
//                        parkPath = audienceSidePark
//                )
//            }
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


    enum class StartPosition(val redStartPosition: PositionAndRotation) {
        Backboard(PositionAndRotation(  x = RobotTwoHardware.redStartingXInches,
                                        y= -12.0,
                                        r= RobotTwoHardware.redStartingRDegrees)),
        Audience(PositionAndRotation(   x = RobotTwoHardware.redStartingXInches,
                                        y= 36.0,
                                        r= RobotTwoHardware.redStartingRDegrees))
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