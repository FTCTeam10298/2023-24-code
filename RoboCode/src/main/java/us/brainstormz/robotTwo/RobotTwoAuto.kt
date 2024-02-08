package us.brainstormz.robotTwo

import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.Gamepad
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.robotTwo.RobotTwoHardware.UnchangingRobotAttributes.alliance
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.threeDay.PropColors
import us.brainstormz.threeDay.PropDetector
import us.brainstormz.threeDay.PropPosition
import us.brainstormz.robotTwo.CollectorSystem.*
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Extendo.ExtendoPositions
import us.brainstormz.robotTwo.subsystems.Intake.CollectorPowers
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Transfer.RollerState
import us.brainstormz.robotTwo.subsystems.Transfer.DirectorState
import us.brainstormz.robotTwo.subsystems.Transfer.RollerPowers
import us.brainstormz.robotTwo.subsystems.Transfer.TransferHalfState
import us.brainstormz.utils.DeltaTimeMeasurer


@Autonomous(group = "!")
class RobotTwoAuto: OpMode() {

    data class AutoTargetWorld(
            val targetRobot: RobotState,
            val isTargetReached: (targetState: AutoTargetWorld, actualState: ActualWorld)-> Boolean,
            val myJankFlagToInjectPurplePlacement: Boolean = false,
            val timeTargetStartedMilis: Long = 0L
    )
    data class RobotState(
            val positionAndRotation: PositionAndRotation,
            val depoState: DepoState,
            val collectorSystemState: CollectorState
    )
    data class DepoState(
            val armPos: Arm.Positions,
            val liftPosition: Lift.LiftPositions,
            val leftClawPosition: RobotTwoHardware.LeftClawPosition,
            val rightClawPosition: RobotTwoHardware.RightClawPosition,
    )

    private fun hasTimeElapsed(timeToElapseMilis: Long, targetWorld: AutoTargetWorld): Boolean {
        val taskStartedTimeMilis = targetWorld.timeTargetStartedMilis
        val timeSinceTargetStarted = System.currentTimeMillis() - taskStartedTimeMilis
        return timeSinceTargetStarted >= timeToElapseMilis
    }

    private fun isRobotAtPosition(targetWorld: AutoTargetWorld, actualState: ActualWorld): Boolean {
        return mecanumMovement.isRobotAtPosition(currentPosition = actualState.actualRobot.positionAndRotation, targetPosition = targetWorld.targetRobot.positionAndRotation)
    }

    private val targetWorldToBeReplacedWithInjection = AutoTargetWorld( targetRobot = RobotState(collectorSystemState = CollectorState(Intake.CollectorPowers.Off, Extendo.ExtendoPositions.Min, Transfer.RollerState(Transfer.RollerPowers.Off, Transfer.RollerPowers.Off, Transfer.DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)), positionAndRotation = PositionAndRotation(), depoState = DepoState(Arm.Positions.In, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)),
                                                                    isTargetReached = {targetState: AutoTargetWorld?, actualState: ActualWorld ->
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
    private val backBoardAuto: List<AutoTargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = isRobotAtPosition(targetState, actualState)
                        isRobotAtPosition
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.FarBackboardPixelPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = isRobotAtPosition(targetState, actualState)
                        val isCollectorAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks, actualState.actualRobot.collectorSystemState.extendoPositionTicks)
                        isRobotAtPosition&& isCollectorAtPosition
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.DropPurple, ExtendoPositions.FarBackboardPixelPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(timeToElapseMilis = 300, targetState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.ReverseDropPurple, ExtendoPositions.FarBackboardPixelPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = backboarkSidePurplePixelPlacementLeftPosition,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(timeToElapseMilis = 200, targetState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isRobotAtPosition = isRobotAtPosition(targetState, actualState)
                        val isCollectorRetracted = extendo.isExtendoAtPosition(ExtendoPositions.Min.ticks, actualState.actualRobot.collectorSystemState.extendoPositionTicks)
                        isRobotAtPosition && isCollectorRetracted
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                          hasTimeElapsed(timeToElapseMilis = 1000, targetState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        lift.isLiftAtPosition(targetState.targetRobot.depoState.liftPosition.ticks, actualState.actualRobot)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoState.armPos.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees) || hasTimeElapsed(timeToElapseMilis = 3000, targetState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.Out, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(timeToElapseMilis = 500, targetState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoState.armPos.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        lift.isLiftAtPosition(targetState.targetRobot.depoState.liftPosition.ticks, actualState.actualRobot)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = parkingPosition,
                            depoState = DepoState(Arm.Positions.ClearLiftMovement, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),

//            targetWorldToBeReplacedWithInjection,
    )


    private val redBackboardPurplePixelPlacement: List<AutoTargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = PositionAndRotation(),
                            depoState = DepoState(Arm.Positions.In, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
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
    private val audienceSideLeftPurple: List<AutoTargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks, actualState.actualRobot.collectorSystemState.extendoPositionTicks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isExtendoABitAwayFromThePurple = actualState.actualRobot.collectorSystemState.extendoPositionTicks <= (ExtendoPositions.Max.ticks - 80)
                        isExtendoABitAwayFromThePurple
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleLeft,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isExtendoHalfwayIn = actualState.actualRobot.collectorSystemState.extendoPositionTicks <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState) && hasTimeElapsed(500, targetState)
                    },),
            )

    private val audienceSideDepositPurpleCenter = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 6.5)
    private val audienceSideNavigateAroundTapeWaypoint1 = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x+ 15.0, y=50.0)
    private val audienceSideNavigateAroundTapeWaypoint2 = audienceSideNavigateAroundTapeWaypoint1.copy(x= redDistanceFromCenterlineInches-10)
    private val audienceSideNavigateAroundTapeWaypoint3 = PositionAndRotation(x= redDistanceFromCenterlineInches-10, y= 50.0, r= 0.0)
    private val audienceSideNavigateAroundTapeWaypoint4 = PositionAndRotation(x= redDistanceFromCenterlineInches, y= 50.0, r= 0.0)
    private val audienceSideCenterPurple: List<AutoTargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks, actualState.actualRobot.collectorSystemState.extendoPositionTicks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isExtendoABitAwayFromThePurple = actualState.actualRobot.collectorSystemState.extendoPositionTicks <= (ExtendoPositions.Max.ticks - 80)
                        isExtendoABitAwayFromThePurple
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleCenter,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isExtendoHalfwayIn = actualState.actualRobot.collectorSystemState.extendoPositionTicks <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        mecanumMovement.isRobotAtPosition(precisionInches = 3.0, precisionDegrees = 3.0, currentPosition = actualState.actualRobot.positionAndRotation, targetPosition = targetState.targetRobot.positionAndRotation)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint3,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),

            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint4,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateAroundTapeWaypoint4,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState) && hasTimeElapsed(500, targetState)
                    },),
    )

    private val audienceSideDepositPurpleRight = StartPosition.Audience.redStartPosition.copy(x= StartPosition.Audience.redStartPosition.x + 1.0, r= StartPosition.Audience.redStartPosition.r-18.0)
    private val audienceSideRightPurple: List<AutoTargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isExtendoAtPosition = extendo.isExtendoAtPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks, actualState.actualRobot.collectorSystemState.extendoPositionTicks)
                        isExtendoAtPosition && isRobotAtPosition(targetState, actualState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.AudiencePurpleCenterPosition, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.AutoInitPosition, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(200, targetState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.EjectDraggedPixelPower, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isExtendoABitAwayFromThePurple = actualState.actualRobot.collectorSystemState.extendoPositionTicks <= (ExtendoPositions.Max.ticks - 80)
                        isExtendoABitAwayFromThePurple
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideDepositPurpleRight,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        val isExtendoHalfwayIn = actualState.actualRobot.collectorSystemState.extendoPositionTicks <= (ExtendoPositions.Max.ticks / 2)
                        isExtendoHalfwayIn
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Eject, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateBetweenTapeWaypoint2,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState) && hasTimeElapsed(500, targetState)
                    },),
    )

    private val audienceSidePurpleMapToProp: Map<PropPosition, List<AutoTargetWorld>> = mapOf(
            PropPosition.Left to audienceSideLeftPurple,
            PropPosition.Center to audienceSideCenterPurple,
            PropPosition.Right to audienceSideRightPurple
    )


    /** Audience Drive To Board */
    private val audienceDriveToBoard: List<AutoTargetWorld> = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint1,
                            depoState = DepoState(Arm.Positions.OutButUnderTwelve, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        actualState.actualRobot.positionAndRotation.y <= 5.0
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideNavigateUnderTrussWaypoint1,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            )


    /** Audience Drop Yellow */
    private val audienceSideDepositLeft = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardLeft,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardLeft,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(500, targetState)
                    },),
    )

    private val audienceSideDepositCenter = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardCenter,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(1000, targetState)
                    },),
    )
    private val audienceSideDepositRight = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardRight,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Gripping, RobotTwoHardware.RightClawPosition.Gripping)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = placingOnBackboardRight,
                            depoState = DepoState(Arm.Positions.DroppingWithHighPrecision, Lift.LiftPositions.BackboardBottomRow, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        hasTimeElapsed(500, targetState)
                    },),
    )

    private val audienceSideYellowMapToProp: Map<PropPosition, List<AutoTargetWorld>> = mapOf(
            PropPosition.Left to audienceSideDepositLeft,
            PropPosition.Center to audienceSideDepositCenter,
            PropPosition.Right to audienceSideDepositRight
    )

    private val audienceSidePark = listOf(
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.Min, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideParkPosition,
                            depoState = DepoState(Arm.Positions.GoodEnoughForLiftToGoDown, Lift.LiftPositions.WaitForArmToMove, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        arm.isArmAtAngle(targetState.targetRobot.depoState.armPos.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees)
                    },),
            AutoTargetWorld(
                    targetRobot = RobotState(
                            collectorSystemState = CollectorState(CollectorPowers.Off, ExtendoPositions.AllTheWayInTarget, RollerState(RollerPowers.Off, RollerPowers.Off, DirectorState.Off), TransferHalfState(false, 0), TransferHalfState(false, 0)),
                            positionAndRotation = audienceSideParkPosition,
                            depoState = DepoState(Arm.Positions.GoodEnoughForLiftToGoDown, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: AutoTargetWorld, actualState: ActualWorld ->
                        isRobotAtPosition(targetState, actualState)
                    },),
    )



    data class PathPreAssembled(val purplePlacementPath: Map<PropPosition, List<AutoTargetWorld>>, val driveToBoardPath: List<AutoTargetWorld>, val yellowDepositPath: Map<PropPosition, List<AutoTargetWorld>>, val parkPath: List<AutoTargetWorld>) {
        fun assemblePath(propPosition: PropPosition): List<AutoTargetWorld> {
            return purplePlacementPath[propPosition]!! + driveToBoardPath + yellowDepositPath[propPosition]!! + parkPath
        }
    }
    private fun calcAutoTargetStateList(
            alliance: RobotTwoHardware.Alliance,
            startPosition: StartPosition,
            propPosition: PropPosition
    ): List<AutoTargetWorld> {


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

    private fun mirrorRedAutoToBlue(auto: List<AutoTargetWorld>): List<AutoTargetWorld> {
        return auto.map { targetWorld ->
            val flippedBluePosition = flipRedPositionToBlue(targetWorld.targetRobot.positionAndRotation)
            targetWorld.copy(targetRobot = targetWorld.targetRobot.copy(positionAndRotation = flippedBluePosition))
        }
    }
    private fun flipRedPositionToBlue(positionAndRotation: PositionAndRotation): PositionAndRotation {
        return positionAndRotation.copy(x= -positionAndRotation.x, r= -positionAndRotation.r)
    }

    private fun getNextTargetFromList(): AutoTargetWorld {
        return autoListIterator.next().copy(timeTargetStartedMilis = System.currentTimeMillis())
    }


    private lateinit var autoStateList: List<AutoTargetWorld>
    private lateinit var autoListIterator: ListIterator<AutoTargetWorld>
    private fun nextTargetState(
            previousTargetState: AutoTargetWorld?,
            actualState: ActualWorld,
            previousActualState: ActualWorld?): AutoTargetWorld {
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

    private val intake = Intake()
    private val transfer = Transfer()
    private val extendo = Extendo()

    private lateinit var collectorSystem: CollectorSystem
    private lateinit var arm: Arm
    private lateinit var lift: Lift
    private lateinit var depoManager: DepoManager

    private var startPosition: StartPosition = StartPosition.Backboard

    private val opencv: OpenCvAbstraction = OpenCvAbstraction(this)
    private var propDetector: PropDetector? = null

    override fun init() {
        hardware.init(hardwareMap)

        val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        mecanumMovement = MecanumMovement(odometryLocalizer, hardware, telemetry)

        collectorSystem = CollectorSystem(transfer= transfer, extendo= extendo, telemetry= telemetry)
        lift = Lift(telemetry)
        arm = Arm()
        depoManager = DepoManager(arm= arm, lift= lift, leftClaw = Claw(Transfer.Side.Left), rightClaw = Claw(Transfer.Side.Right))

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

    private val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<AutoTargetWorld, ActualWorld>()
    private val loopTimeMeasurer = DeltaTimeMeasurer()
    override fun loop() {

        functionalReactiveAutoRunner.loop(
            actualStateGetter = { previousActualState ->
                ActualWorld(
                        actualRobot = hardware.getActualState(mecanumMovement.localizer, collectorSystem, depoManager),
                        actualGamepad1 = Gamepad(),
                        actualGamepad2 = Gamepad(),
                        timestampMilis = System.currentTimeMillis()
                )
            },
            targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                nextTargetState(previousTargetState, actualState, previousActualState)
            },
            stateFulfiller = { targetState, actualState ->
                telemetry.addLine("target position: ${targetState.targetRobot.positionAndRotation}")
                telemetry.addLine("current position: ${mecanumMovement.localizer.currentPositionAndRotation()}")

                mecanumMovement.moveTowardTarget(targetState.targetRobot.positionAndRotation)
                extendo.powerSubsystem(extendo.calcPowerToMoveExtendo(targetState.targetRobot.collectorSystemState.extendoPosition.ticks, actualState.actualRobot), hardware)
                intake.powerSubsystem(targetState.targetRobot.collectorSystemState.collectorState.power, hardware)
                lift.powerSubsystem(lift.calculatePowerToMoveToPosition(targetState.targetRobot.depoState.liftPosition.ticks, actualState.actualRobot), hardware)
                arm.powerSubsystem(arm.calcPowerToReachTarget(targetState.targetRobot.depoState.armPos.angleDegrees, actualState.actualRobot.depoState.armAngleDegrees), hardware)
                hardware.rightClawServo.position = targetState.targetRobot.depoState.rightClawPosition.position
                hardware.leftClawServo.position = targetState.targetRobot.depoState.leftClawPosition.position
            }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMilis()
        telemetry.addLine("loop time: $loopTime milis")

        telemetry.update()
    }
}