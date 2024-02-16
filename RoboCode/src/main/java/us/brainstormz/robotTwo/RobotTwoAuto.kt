package us.brainstormz.robotTwo

import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
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
import us.brainstormz.robotTwo.subsystems.Transfer.TransferHalfState
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.DeltaTimeMeasurer

class RobotTwoAuto(private val telemetry: Telemetry) {

    data class AutoTargetWorld(
            val targetRobot: RobotState,
            val isTargetReached: (targetState: TargetWorld, actualState: ActualWorld)-> Boolean,
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
                                targetColor = RobotTwoTeleOp.PixelColor.Unknown,
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

    data class RobotState(
            val positionAndRotation: PositionAndRotation,
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

    private fun isRobotAtPosition(targetWorld: TargetWorld, actualState: ActualWorld): Boolean {
        return drivetrain.isRobotAtPosition(currentPosition = actualState.actualRobot.positionAndRotation, targetPosition = targetWorld.targetRobot.drivetrainTarget.targetPosition)
    }

    private val placingOnBackboardCenter = PositionAndRotation(x= -36.0, y= -55.0, r= 0.0)
    private val placingOnBackboardLeft = PositionAndRotation(x= -28.0, y= -55.0, r= 0.0)
    private val placingOnBackboardRight = PositionAndRotation(x= -40.0, y= -55.0, r= 0.0)

    /** Backboard side */
    private val backboarkSidePurplePixelPlacementLeftPosition = PositionAndRotation(x= -34.0, y= -36.0, r= 0.0)
    private val backboarkSidePurplePixelPlacementCenterPosition = PositionAndRotation(x= -34.0, y= -36.0, r= 0.0)
    private val backboarkSidePurplePixelPlacementRightPosition = PositionAndRotation(x= -34.0, y= -36.0, r= 0.0)
    private val parkingPosition = PositionAndRotation(x= -58.0, y= -48.0, r= 0.0)
    private val backBoardAuto: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = isRobotAtPosition(targetState, actualState)
                        isRobotAtPosition
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.FarBackboardPixelPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = isRobotAtPosition(targetState, actualState)
                        val isCollectorAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                        isRobotAtPosition&& isCollectorAtPosition
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.DropPurple, ExtendoPositions.FarBackboardPixelPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(timeToElapseMilis = 300, targetState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.ReverseDropPurple, ExtendoPositions.FarBackboardPixelPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(timeToElapseMilis = 200, targetState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = isRobotAtPosition(targetState, actualState)
                        val isCollectorRetracted = extendo.isExtendoAtPosition(ExtendoPositions.Min.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                        isRobotAtPosition && isCollectorRetracted
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                          hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        lift.isLiftAtPosition(targetState.targetRobot.depoTarget.lift.targetPosition.ticks, actualState.actualRobot.depoState.lift.currentPositionTicks)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoTarget.armPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees) || hasTimeElapsed(timeToElapseMilis = 3000, targetState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(timeToElapseMilis = 500, targetState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoTarget.armPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        lift.isLiftAtPosition(targetState.targetRobot.depoTarget.lift.targetPosition.ticks, actualState.actualRobot.depoState.lift.currentPositionTicks)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = parkingPosition,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,

//            targetWorldToBeReplacedWithInjection,
    )


    private val redBackboardPurplePixelPlacement: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = PositionAndRotation(),
                            depoState = DepoState(Arm.Positions.In, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = drivetrain.isRobotAtPosition(currentPosition = actualState.actualRobot.positionAndRotation, targetPosition = targetState.targetRobot.drivetrainTarget.targetPosition)
                        telemetry.addLine("isRobotAtPosition: $isRobotAtPosition")
                        isRobotAtPosition
                    },).asTargetWorld
    )


    /** Audience side */
    val redDistanceFromCenterlineInches = -((RobotTwoHardware.robotWidthInches/2)+0.5)
    private val audienceSideNavigateUnderTrussWaypoint1 = PositionAndRotation(x= redDistanceFromCenterlineInches, y=-30.0, r= 0.0)
    private val audienceSideParkPosition = PositionAndRotation(x= -10.0, y= -47.0, r= 0.0)

    private val audienceSideDepositPurpleLeft = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 1.0, r= StartPosition.Audience.redStartPosition.r+18.0)
    private val audienceSideNavigateBetweenTapeWaypoint1 = StartPosition.Audience.redStartPosition.copy(x= -14.0, y= StartPosition.Audience.redStartPosition.y + 1)//, r= StartPosition.Audience.redStartPosition.r+5)
    private val audienceSideNavigateBetweenTapeWaypoint2 = StartPosition.Audience.redStartPosition.copy(x= -14.0, y= StartPosition.Audience.redStartPosition.y + 1, r= 0.0)//, r= StartPosition.Audience.redStartPosition.r+5)
    private val audienceSideLeftPurple: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoABitAwayFromThePurple = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks - 80)
                        isExtendoABitAwayFromThePurple
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoHalfwayIn = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState) && hasTimeElapsed(500, targetState)
                    },).asTargetWorld,
            )

    private val audienceSideDepositPurpleCenter = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 6.5)
    private val audienceSideNavigateAroundTapeWaypoint1 = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 15.0, y=50.0)
    private val audienceSideNavigateAroundTapeWaypoint2 = audienceSideNavigateAroundTapeWaypoint1.copy(x= redDistanceFromCenterlineInches-10)
    private val audienceSideNavigateAroundTapeWaypoint3 = PositionAndRotation(x= redDistanceFromCenterlineInches-10, y= 50.0, r= 0.0)
    private val audienceSideNavigateAroundTapeWaypoint4 = PositionAndRotation(x= redDistanceFromCenterlineInches, y= 50.0, r= 0.0)
    private val audienceSideCenterPurple: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoABitAwayFromThePurple = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks - 80)
                        isExtendoABitAwayFromThePurple
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoHalfwayIn = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        drivetrain.isRobotAtPosition(precisionInches = 3.0, precisionDegrees = 3.0, currentPosition = actualState.actualRobot.positionAndRotation, targetPosition = targetState.targetRobot.drivetrainTarget.targetPosition)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint3,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,

            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint4,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint4,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState) && hasTimeElapsed(500, targetState)
                    },).asTargetWorld,
    )

    private val audienceSideDepositPurpleRight = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x + 1.0, r= StartPosition.Audience.redStartPosition.r-18.0)
    private val audienceSideRightPurple: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoABitAwayFromThePurple = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks - 80)
                        isExtendoABitAwayFromThePurple
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoHalfwayIn = actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState) && hasTimeElapsed(500, targetState)
                    },).asTargetWorld,
    )

    private val audienceSidePurpleMapToProp: Map<PropPosition, List<TargetWorld>> = mapOf(
            PropPosition.Left to audienceSideLeftPurple,
            PropPosition.Center to audienceSideCenterPurple,
            PropPosition.Right to audienceSideRightPurple
    )


    /** Audience Drive To Board */
    private val audienceDriveToBoard: List<TargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Down, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        actualState.actualRobot.positionAndRotation.y <= 5.0
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint1,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Gripping, ClawTarget.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
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
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardLeft,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
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
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
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
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = placingOnBackboardRight,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
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
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoTarget.armPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees)
                    },).asTargetWorld,
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off)),
                            positionAndRotation = audienceSideParkPosition,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.Down, ClawTarget.Retracted, ClawTarget.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },).asTargetWorld,
    )



    data class PathPreAssembled(val purplePlacementPath: Map<PropPosition, List<TargetWorld>>, val driveToBoardPath: List<TargetWorld>, val yellowDepositPath: Map<PropPosition, List<TargetWorld>>, val parkPath: List<TargetWorld>) {
        fun assemblePath(propPosition: PropPosition): List<TargetWorld> {
            return purplePlacementPath[propPosition]!! + driveToBoardPath + yellowDepositPath[propPosition]!! + parkPath
        }
    }
    private fun calcAutoTargetStateList(
            alliance: RobotTwoHardware.Alliance,
            startPosition: StartPosition,
            propPosition: PropPosition
    ): List<TargetWorld> {


        val redPath: PathPreAssembled = when (startPosition) {
            StartPosition.Backboard -> {
                PathPreAssembled(
                        purplePlacementPath = mapOf(propPosition to emptyList()),
                        driveToBoardPath = backBoardAuto,
                        yellowDepositPath = mapOf(propPosition to emptyList()),
                        parkPath = listOf()
                )
            }
            StartPosition.Audience -> {
                PathPreAssembled(
                        purplePlacementPath = audienceSidePurpleMapToProp,
                        driveToBoardPath = audienceDriveToBoard,
                        yellowDepositPath = audienceSideYellowMapToProp,
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
            val isTargetReached = previousTargetState.isTargetReached(previousTargetState!!, actualState)
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
    private var wizardWasChanged = false
    data class WizardResults(val alliance: RobotTwoHardware.Alliance, val startPosition: StartPosition)

    private fun runMenuWizard(gamepad1: Gamepad): WizardResults {
        val isWizardDone = wizard.summonWizard(gamepad1)
        return if (isWizardDone) {
            wizardWasChanged = true
            WizardResults(
                    alliance = when (wizard.wasItemChosen("alliance", "Red")) {
                        true -> RobotTwoHardware.Alliance.Red
                        false -> RobotTwoHardware.Alliance.Blue
                    },
                    startPosition = when (wizard.wasItemChosen("startingPos", "Audience")) {
                        true -> StartPosition.Audience
                        false -> StartPosition.Backboard
                    }
            )
        } else {
            wizardResults
        }
    }

    private lateinit var drivetrain: Drivetrain

    private val intake = Intake()
    private val transfer = Transfer(telemetry)
    private val extendo = Extendo()

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

    private fun runCamera(opencv: OpenCvAbstraction) {
        alliance = wizardResults.alliance
        startPosition = wizardResults.startPosition

        val propColor: PropColors = when (alliance) {
            RobotTwoHardware.Alliance.Blue -> PropColors.Blue
            RobotTwoHardware.Alliance.Red -> PropColors.Red
        }
        propDetector = RobotTwoPropDetector(telemetry, propColor)
        opencv.onNewFrame(propDetector!!::processFrame)
    }

    private var propPosition: PropPosition = PropPosition.Left
    private var wizardResults = WizardResults(RobotTwoHardware.Alliance.Red, StartPosition.Backboard)
    fun init_loop(hardware: RobotTwoHardware, opencv: OpenCvAbstraction, gamepad1: Gamepad) {
        wizardResults = runMenuWizard(gamepad1)
        if (wizardWasChanged) {
            runCamera(opencv)
            hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE)
        }
    }

    fun start(hardware: RobotTwoHardware, opencv: OpenCvAbstraction) {
        propPosition = propDetector?.propPosition ?: propPosition
        opencv.stop()

        val startPositionAndRotation: PositionAndRotation = when (alliance) {
            RobotTwoHardware.Alliance.Red -> startPosition.redStartPosition
            RobotTwoHardware.Alliance.Blue -> flipRedPositionToBlue(startPosition.redStartPosition)
        }

        drivetrain.localizer.setPositionAndRotation(startPositionAndRotation)

        autoStateList = calcAutoTargetStateList(alliance, startPosition, PropPosition.Center)
        autoListIterator = autoStateList.listIterator()

        hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE)
    }

    val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    private val loopTimeMeasurer = DeltaTimeMeasurer()
    fun loop(hardware: RobotTwoHardware, gamepad1: Gamepad) {
        functionalReactiveAutoRunner.loop(
            actualStateGetter = { previousActualState ->
                val currentGamepad1 = Gamepad()
                currentGamepad1.copy(gamepad1)
                ActualWorld(
                        actualRobot = hardware.getActualState(drivetrain.localizer, collectorSystem, depoManager, previousActualState),
                        actualGamepad1 = currentGamepad1,
                        actualGamepad2 = currentGamepad1,
                        timestampMilis = System.currentTimeMillis()
                )
            },
            targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                nextTargetState(previousTargetState, actualState, previousActualState)
            },
            stateFulfiller = { targetState, actualState ->
                telemetry.addLine("target position: ${targetState.targetRobot.drivetrainTarget.targetPosition}")
                telemetry.addLine("current position: ${drivetrain.localizer.currentPositionAndRotation()}")

                val universalTargetWorld: TargetWorld = targetState

                hardware.actuateRobot(
                        universalTargetWorld,
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

                hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE)//targetState.targetRobot.lights.targetColor.blinkinPattern)

//                mecanumMovement.moveTowardTarget(targetState.targetRobot.drivetrainTarget.targetPosition)
//                extendo.powerSubsystem(extendo.calcPowerToMoveExtendo(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot), hardware)
//                intake.powerSubsystem(targetState.targetRobot.collectorSystemState.collectorState.power, hardware)
//                lift.powerSubsystem(lift.calculatePowerToMoveToPosition(targetState.targetRobot.depoTarget.lift.targetPosition.ticks, actualState.actualRobot.depoState.lift.currentPositionTicks), hardware)
//                arm.powerSubsystem(arm.calcPowerToReachTarget(targetState.targetRobot.depoTarget.armPosition.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees), hardware)
//
//
//                hardware.rightClawServo.power = TODO()// targetState.targetRobot.depoState.rightClawPosition.position
//                hardware.leftClawServo.power = TODO()//targetState.targetRobot.depoState.leftClawPosition.position
            }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMilis()
        telemetry.addLine("loop time: $loopTime milis")

        telemetry.update()
    }
}