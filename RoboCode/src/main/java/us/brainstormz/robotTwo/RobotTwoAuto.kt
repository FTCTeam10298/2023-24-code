package us.brainstormz.robotTwo

import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.robotTwo.RobotTwoHardware.UnchangingRobotAttributes.alliance
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.robotTwo.RobotTwoHardware.RobotState
import us.brainstormz.threeDay.PropColors
import us.brainstormz.threeDay.PropDetector
import us.brainstormz.threeDay.PropPosition
import us.brainstormz.utils.LoopTimeMeasurer
import us.brainstormz.robotTwo.CollectorSystem.*

@Autonomous(group = "!")
class RobotTwoAuto: OpMode() {

    private fun hasTimeElapsed(timeToElapseMilis: Long, targetWorld: TargetWorld): Boolean {
        val taskStartedTimeMilis = targetWorld.timeTargetStartedMilis
        val timeSinceTargetStarted = System.currentTimeMillis() - taskStartedTimeMilis
        return timeSinceTargetStarted >= timeToElapseMilis
    }

    private fun isRobotAtPosition(targetWorld: TargetWorld, actualState: ActualWorld): Boolean {
        return mecanumMovement.isRobotAtPosition(currentPosition = actualState.actualRobot.positionAndRotation, targetPosition = targetWorld.targetRobot.positionAndRotation)
    }

    private val targetWorldToBeReplacedWithInjection = TargetWorld( targetRobot = RobotState(collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)), positionAndRotation = PositionAndRotation(), depoState = DepoState(Arm.Positions.In, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)),
                                                                    isTargetReached = {targetState: TargetWorld?, actualState: ActualWorld ->
                                                                        println("This had better not run")
                                                                        false
                                                                    },
                                                                    myJankFlagToInjectPurplePlacement = true)

    //Backboard side
    private val backboarkSidePurplePixelPlacementLeftPosition = PositionAndRotation(y= -36.0, x= -34.0, r= 0.0)
    private val backboarkSidePurplePixelPlacementCenterPosition = PositionAndRotation(y= -36.0, x= -34.0, r= 0.0)
    private val backboarkSidePurplePixelPlacementRightPosition = PositionAndRotation(y= -36.0, x= -34.0, r= 0.0)
    private val placingOnBackboardPosition = PositionAndRotation(y= -54.5, x= -36.0, r= 0.0)
    private val parkingPosition = PositionAndRotation(y= -48.0, x= -58.0, r= 0.0)
    private val backBoardAuto: List<TargetWorld> = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = isRobotAtPosition(targetState, actualState)
                        isRobotAtPosition
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.FarBackboardPixelPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = isRobotAtPosition(targetState, actualState)
                        val isCollectorAtPosition = collectorSystem.isExtendoAtPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks)
                        isRobotAtPosition&& isCollectorAtPosition
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.DropPurple, ExtendoPositions.FarBackboardPixelPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(timeToElapseMilis = 300, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.ReverseDropPurple, ExtendoPositions.FarBackboardPixelPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(timeToElapseMilis = 200, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = isRobotAtPosition(targetState, actualState)
                        val isCollectorRetracted = collectorSystem.isExtendoAtPosition(ExtendoPositions.Min.ticks)
                        isRobotAtPosition && isCollectorRetracted
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                          hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        lift.isLiftAtPosition(targetState.targetRobot.depoState.liftPosition.ticks)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardPosition,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoState.armPos.angleDegrees) || hasTimeElapsed(timeToElapseMilis = 3000, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardPosition,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(timeToElapseMilis = 500, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardPosition,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoState.armPos.angleDegrees)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardPosition,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        lift.isLiftAtPosition(targetState.targetRobot.depoState.liftPosition.ticks)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = parkingPosition,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),

//            targetWorldToBeReplacedWithInjection,
    )

    private val redBackboardPurplePixelPlacement: List<TargetWorld> = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = PositionAndRotation(),
                            depoState = DepoState(Arm.Positions.In, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = mecanumMovement.isRobotAtPosition(currentPosition = actualState.actualRobot.positionAndRotation, targetPosition = targetState?.targetRobot?.positionAndRotation ?: PositionAndRotation())
                        telemetry.addLine("isRobotAtPosition: $isRobotAtPosition")
                        isRobotAtPosition
                    },),
    )


    //Audience side
    private val audienceSideDepositPurpleCenter = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 6.5)
    private val audienceSideNavigateAroundTapeWaypoint1 = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 15.0, y=50.0)
    val redDistanceFromCenterlineInches = -((RobotTwoHardware.robotWidthInches/2)+1)
    private val audienceSideNavigateAroundTapeWaypoint2 = audienceSideNavigateAroundTapeWaypoint1.copy(x= redDistanceFromCenterlineInches-5)
    private val audienceSideNavigateUnderTrussWaypoint1 = audienceSideNavigateAroundTapeWaypoint2.copy(x= audienceSideNavigateAroundTapeWaypoint2.x - 2, r= 0.0)
    private val audienceSideNavigateUnderTrussWaypoint2 = audienceSideNavigateUnderTrussWaypoint1.copy(x= redDistanceFromCenterlineInches)
    private val audienceSideNavigateUnderTrussWaypoint3 = PositionAndRotation(x= redDistanceFromCenterlineInches, y=-30.0, r= 0.0)
    private val audienceSideParkPosition = PositionAndRotation(x= -10.0, y= -47.0, r= 0.0)
    private val audienceAuto: List<TargetWorld> = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AudiencePurpleMiddlePosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoAtPosition = collectorSystem.isExtendoAtPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.ReverseDropPurple, ExtendoPositions.AudiencePurpleMiddlePosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoHalfwayIn = collectorSystem.getExtendoPositionTicks() <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState) && hasTimeElapsed(500, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint3,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        actualState.actualRobot.positionAndRotation.y <= 5.0
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint3,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardPosition,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardPosition,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(1000, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideParkPosition,
                            depoState = DepoState(Arm.Positions.GoodEnoughForLiftToGoDown, Lift.LiftPositions.WaitForArmToMove, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoState.armPos.angleDegrees)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideParkPosition,
                            depoState = DepoState(Arm.Positions.GoodEnoughForLiftToGoDown, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            )


    private fun getPurplePixelPlacementRoutineForRedAlliance(
            startPosition: StartPosition): List<TargetWorld> {

        val redInjected = when (startPosition) {
            StartPosition.Backboard -> {
                redBackboardPurplePixelPlacement
            }
            StartPosition.Audience -> {
                TODO()
            }
        }

        return redInjected
    }

    private fun injectPurplePlacementIntoSidedAuto(
            sidedAuto: List<TargetWorld>,
            startPosition: StartPosition): List<TargetWorld> {

        val injectPointIndex = sidedAuto.indexOfFirst {targetWorld -> targetWorld.myJankFlagToInjectPurplePlacement}
        return if (injectPointIndex != -1) {
            val listToInject = getPurplePixelPlacementRoutineForRedAlliance(startPosition)

            val injectedList = sidedAuto.subList(0, injectPointIndex) + listToInject + sidedAuto.subList(injectPointIndex + 1, sidedAuto.size)

            injectedList
        } else {
            sidedAuto
        }
    }
    
    private fun calcAutoTargetStateList(
            alliance: RobotTwoHardware.Alliance,
            startPosition: StartPosition,
            propPosition: PropPosition
    ): List<TargetWorld> {

        val startPosAccounted = when (startPosition) {
            StartPosition.Backboard -> backBoardAuto
            StartPosition.Audience -> audienceAuto
        }

//        val purplePixelAccounted: List<TargetWorld> = injectPurplePlacementIntoSidedAuto(startPosAccounted, startPosition)
        val purplePixelAccounted = startPosAccounted.map { targetWorld ->
            if (targetWorld.targetRobot.collectorSystemState.extendoPosition == ExtendoPositions.FarBackboardPixelPosition) {
                val newExtendoPosition = when (propPosition) {
                    PropPosition.Left -> ExtendoPositions.FarBackboardPixelPosition
                    PropPosition.Center -> ExtendoPositions.MidBackboardPixelPosition
                    PropPosition.Right -> ExtendoPositions.CloserBackboardPixelPosition
                }
                val newRobotPosition = when (propPosition) {
                    PropPosition.Left -> backboarkSidePurplePixelPlacementLeftPosition
                    PropPosition.Center -> backboarkSidePurplePixelPlacementCenterPosition
                    PropPosition.Right -> backboarkSidePurplePixelPlacementRightPosition
                }
                targetWorld.copy(targetRobot = targetWorld.targetRobot.copy(
                        collectorSystemState = targetWorld.targetRobot.collectorSystemState.copy(extendoPosition = newExtendoPosition),
                        positionAndRotation = newRobotPosition
                ))
            } else {
                targetWorld
            }
        }


        val allianceAccounted = when (alliance) {
            RobotTwoHardware.Alliance.Red -> purplePixelAccounted
            RobotTwoHardware.Alliance.Blue -> flipRedAutoToBlue(purplePixelAccounted)
        }

        return allianceAccounted
    }

    private fun flipRedAutoToBlue(auto: List<TargetWorld>): List<TargetWorld> {
        return auto.map { targetWorld ->
            val flippedBluePosition = flipRedPositionToBlue(targetWorld.targetRobot.positionAndRotation)
            targetWorld.copy(targetRobot = targetWorld.targetRobot.copy(positionAndRotation = flippedBluePosition))
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

    data class DepoState(
            val armPos: Arm.Positions,
            val liftPosition: Lift.LiftPositions,
            val leftClawPosition: RobotTwoHardware.LeftClawPosition,
            val rightClawPosition: RobotTwoHardware.RightClawPosition,
    )

    data class TargetWorld(
            val targetRobot: RobotTwoHardware.RobotState,
            val isTargetReached: (previousTargetState: TargetWorld, actualState: ActualWorld) -> Boolean,
            val myJankFlagToInjectPurplePlacement: Boolean = false,
            val timeTargetStartedMilis: Long = 0)
    class ActualWorld(val actualRobot: RobotState,
                      val timestampMilis: Long)

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

    private fun runMenuWizard(): WizardResults {
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

    private val hardware = RobotTwoHardware(telemetry, this)

    private lateinit var mecanumMovement: MecanumMovement
    private lateinit var collectorSystem: CollectorSystem
    private lateinit var arm: Arm
    private lateinit var lift: Lift

    private var startPosition: StartPosition = StartPosition.Backboard

    private val opencv: OpenCvAbstraction = OpenCvAbstraction(this)
    private var propDetector: PropDetector? = null

    override fun init() {
        hardware.init(hardwareMap)

        val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        mecanumMovement = MecanumMovement(odometryLocalizer, hardware, telemetry)

        collectorSystem = CollectorSystem(  extendoMotorMaster= hardware.extendoMotorMaster,
                extendoMotorSlave= hardware.extendoMotorSlave,
                collectorServo1 = hardware.collectorServo1,
                collectorServo2 = hardware.collectorServo2,
                rightTransferServo=hardware. rightTransferServo,
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

        arm = Arm(  encoder= hardware.armEncoder,
                armServo1= hardware.armServo1,
                armServo2= hardware.armServo2, telemetry)


        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"), nextMenu = "startingPos", firstMenu = true)
        wizard.newMenu("startingPos", "What side of the truss are we on?", listOf("Audience", "Backboard"))


        opencv.init(hardwareMap)
        opencv.internalCamera = false
        opencv.cameraName = "Webcam 1"
        opencv.cameraOrientation = OpenCvCameraRotation.UPSIDE_DOWN
    }

    private fun runCamera() {
        alliance = wizardResults.alliance
        startPosition = wizardResults.startPosition

        val propColor: PropColors = when (alliance) {
            RobotTwoHardware.Alliance.Blue -> PropColors.Blue
            RobotTwoHardware.Alliance.Red -> PropColors.Red
        }
        propDetector = PropDetector(telemetry, propColor)
        opencv.onNewFrame(propDetector!!::processFrame)
    }

    private var propPosition: PropPosition = PropPosition.Left
    private var wizardResults = WizardResults(RobotTwoHardware.Alliance.Red, StartPosition.Backboard)
    override fun init_loop() {
        wizardResults = runMenuWizard()
        if (wizardWasChanged) {
            runCamera()
            hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE)
        }
    }

    override fun start() {
        propPosition = propDetector?.propPosition ?: propPosition
        opencv.stop()

        val startPositionAndRotation: PositionAndRotation = when (alliance) {
            RobotTwoHardware.Alliance.Red -> startPosition.redStartPosition
            RobotTwoHardware.Alliance.Blue -> flipRedPositionToBlue(startPosition.redStartPosition)
        }

        mecanumMovement.localizer.setPositionAndRotation(startPositionAndRotation)

        autoStateList = calcAutoTargetStateList(alliance, startPosition, propPosition)
        autoListIterator = autoStateList.listIterator()

        hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE)
    }

    private val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    private val loopTimeMeasurer = LoopTimeMeasurer()
    override fun loop() {

        functionalReactiveAutoRunner.loop(
            actualStateGetter = { previousActualState ->
                hardware.getActualState(previousActualState, arm, mecanumMovement.localizer, collectorSystem)
            },
            targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                nextTargetState(previousTargetState, actualState, previousActualState)
            },
            stateFulfiller = { targetState, actualState ->
                telemetry.addLine("target position: ${targetState.targetRobot.positionAndRotation}")
                telemetry.addLine("current position: ${mecanumMovement.localizer.currentPositionAndRotation()}")

                mecanumMovement.moveTowardTarget(targetState.targetRobot.positionAndRotation)
                collectorSystem.moveExtendoToPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks)
                collectorSystem.spinCollector(targetState.targetRobot.collectorSystemState.collectorState.power)
                lift.moveLiftToPosition(targetState.targetRobot.depoState.liftPosition.ticks)
                arm.moveArmTowardPosition(targetState.targetRobot.depoState.armPos.angleDegrees)
                hardware.rightClawServo.position = targetState.targetRobot.depoState.rightClawPosition.position
                hardware.leftClawServo.position = targetState.targetRobot.depoState.leftClawPosition.position
            }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMilis()
        telemetry.addLine("loop time: $loopTime milis")

        telemetry.update()
    }
}