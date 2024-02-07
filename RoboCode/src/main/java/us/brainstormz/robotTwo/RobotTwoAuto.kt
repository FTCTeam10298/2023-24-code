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

    private val placingOnBackboardCenter = PositionAndRotation(x= -36.0, y= -55.0, r= 0.0)
    private val placingOnBackboardLeft = PositionAndRotation(x= -28.0, y= -55.0, r= 0.0)
    private val placingOnBackboardRight = PositionAndRotation(x= -40.0, y= -55.0, r= 0.0)

    /** Backboard side */
    private val backboarkSidePurplePixelPlacementLeftPosition = PositionAndRotation(x= -34.0, y= -36.0, r= 0.0)
    private val backboarkSidePurplePixelPlacementCenterPosition = PositionAndRotation(x= -34.0, y= -36.0, r= 0.0)
    private val backboarkSidePurplePixelPlacementRightPosition = PositionAndRotation(x= -34.0, y= -36.0, r= 0.0)
    private val parkingPosition = PositionAndRotation(x= -58.0, y= -48.0, r= 0.0)
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
                            positionAndRotation = placingOnBackboardCenter,
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
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                          hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        lift.isLiftAtPosition(targetState.targetRobot.depoState.liftPosition.ticks)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoState.armPos.angleDegrees) || hasTimeElapsed(timeToElapseMilis = 3000, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(timeToElapseMilis = 500, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoState.armPos.angleDegrees)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
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


    /** Audience side */
    val redDistanceFromCenterlineInches = -((RobotTwoHardware.robotWidthInches/2)+0.5)
    private val audienceSideNavigateUnderTrussWaypoint1 = PositionAndRotation(x= redDistanceFromCenterlineInches, y=-30.0, r= 0.0)
    private val audienceSideParkPosition = PositionAndRotation(x= -10.0, y= -47.0, r= 0.0)

    private val audienceSideDepositPurpleLeft = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 1.0, r= StartPosition.Audience.redStartPosition.r+18.0)
    private val audienceSideNavigateBetweenTapeWaypoint1 = StartPosition.Audience.redStartPosition.copy(x= -14.0, y= StartPosition.Audience.redStartPosition.y + 1)//, r= StartPosition.Audience.redStartPosition.r+5)
    private val audienceSideNavigateBetweenTapeWaypoint2 = StartPosition.Audience.redStartPosition.copy(x= -14.0, y= StartPosition.Audience.redStartPosition.y + 1, r= 0.0)//, r= StartPosition.Audience.redStartPosition.r+5)
    private val audienceSideLeftPurple: List<TargetWorld> = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoAtPosition = collectorSystem.isExtendoAtPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoABitAwayFromThePurple = collectorSystem.getExtendoPositionTicks() <= (ExtendoPositions.Max.ticks - 80)
                        isExtendoABitAwayFromThePurple
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoHalfwayIn = collectorSystem.getExtendoPositionTicks() <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState) && hasTimeElapsed(500, targetState)
                    },),
            )

    private val audienceSideDepositPurpleCenter = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 6.5)
    private val audienceSideNavigateAroundTapeWaypoint1 = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 15.0, y=50.0)
    private val audienceSideNavigateAroundTapeWaypoint2 = audienceSideNavigateAroundTapeWaypoint1.copy(x= redDistanceFromCenterlineInches-10)
    private val audienceSideNavigateAroundTapeWaypoint3 = PositionAndRotation(x= redDistanceFromCenterlineInches-10, y= 50.0, r= 0.0)
    private val audienceSideNavigateAroundTapeWaypoint4 = PositionAndRotation(x= redDistanceFromCenterlineInches, y= 50.0, r= 0.0)
    private val audienceSideCenterPurple: List<TargetWorld> = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoAtPosition = collectorSystem.isExtendoAtPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoABitAwayFromThePurple = collectorSystem.getExtendoPositionTicks() <= (ExtendoPositions.Max.ticks - 80)
                        isExtendoABitAwayFromThePurple
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoHalfwayIn = collectorSystem.getExtendoPositionTicks() <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
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
                        mecanumMovement.isRobotAtPosition(precisionInches = 3.0, precisionDegrees = 3.0, currentPosition = actualState.actualRobot.positionAndRotation, targetPosition = targetState.targetRobot.positionAndRotation)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint3,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),

            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint4,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint4,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState) && hasTimeElapsed(500, targetState)
                    },),
    )

    private val audienceSideDepositPurpleRight = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x + 1.0, r= StartPosition.Audience.redStartPosition.r-18.0)
    private val audienceSideRightPurple: List<TargetWorld> = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoAtPosition = collectorSystem.isExtendoAtPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoABitAwayFromThePurple = collectorSystem.getExtendoPositionTicks() <= (ExtendoPositions.Max.ticks - 80)
                        isExtendoABitAwayFromThePurple
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        val isExtendoHalfwayIn = collectorSystem.getExtendoPositionTicks() <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState) && hasTimeElapsed(500, targetState)
                    },),
    )

    private val audienceSidePurpleMapToProp: Map<PropPosition, List<TargetWorld>> = mapOf(
            PropPosition.Left to audienceSideLeftPurple,
            PropPosition.Center to audienceSideCenterPurple,
            PropPosition.Right to audienceSideRightPurple
    )


    /** Audience Drive To Board */
    private val audienceDriveToBoard: List<TargetWorld> = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        actualState.actualRobot.positionAndRotation.y <= 5.0
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint1,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            )


    /** Audience Drop Yellow */
    private val audienceSideDepositLeft = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardLeft,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardLeft,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(500, targetState)
                    },),
    )

    private val audienceSideDepositCenter = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(1000, targetState)
                    },),
    )
    private val audienceSideDepositRight = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardRight,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardRight,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(500, targetState)
                    },),
    )

    private val audienceSideYellowMapToProp: Map<PropPosition, List<TargetWorld>> = mapOf(
            PropPosition.Left to audienceSideDepositLeft,
            PropPosition.Center to audienceSideDepositCenter,
            PropPosition.Right to audienceSideDepositRight
    )

    private val audienceSidePark = listOf(
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
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideParkPosition,
                            depoState = DepoState(Arm.Positions.GoodEnoughForLiftToGoDown, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
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
                        purplePlacementPath = mapOf(),
                        driveToBoardPath = backBoardAuto,
                        yellowDepositPath = mapOf(),
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

        autoStateList = calcAutoTargetStateList(alliance, startPosition, PropPosition.Center)
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