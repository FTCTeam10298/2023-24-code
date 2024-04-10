package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

class HandoffManager(
        private val collectorSystem: CollectorSystem,
        private val wrist: Wrist,
        private val lift: Lift,
        private val extendo: Extendo,
        private val arm: Arm,
        private val transfer: Transfer,
        private val telemetry: Telemetry) {

    data class SideIsActivelyHandingOff(
            val left: Boolean,
            val right: Boolean
    ) {
        fun getBySide(side: Transfer.Side): Boolean {
            return when (side) {
                Transfer.Side.Left -> left
                Transfer.Side.Right -> right
            }
        }
    }

    data class HandoffTarget(
            val collectorTarget: CollectorTarget,
            val depoTarget: DepoTarget,
    )

    private fun getTransferTargetBasedOnRule(rule: (side: Transfer.Side)->Transfer.LatchTarget): Transfer.TransferTarget {
        return Transfer.TransferTarget(
                leftLatchTarget = rule(Transfer.Side.Left),
                rightLatchTarget = rule(Transfer.Side.Right)
        )
    }
    private fun getWristTargetBasedOnRule(rule: (side: Transfer.Side)->Claw.ClawTarget): Wrist.WristTargets {
        return Wrist.WristTargets(
                left = rule(Transfer.Side.Left),
                right = rule(Transfer.Side.Right)
        )
    }

    fun coordinateHandoff(actualRobot: ActualRobot, uncoordinatedHandoffTarget: HandoffTarget, previousHandoffTarget: HandoffTarget): HandoffTarget {

        val liftIsDown = actualRobot.depoState.lift.limitSwitchIsActivated
        val extendoIsIn = actualRobot.collectorSystemState.extendo.limitSwitchIsActivated
        val armIsAtHandoffPosition = arm.checkIfArmIsAtTarget(Arm.Positions.In, actualRobot.depoState.armAngleDegrees)
        val startHandoff = liftIsDown && armIsAtHandoffPosition && extendoIsIn

        return if (startHandoff) {
            fun gripClawIfPixelInSide(side: Transfer.Side): Claw.ClawTarget {
                val transferSensorState = uncoordinatedHandoffTarget.collectorTarget.transferState.getBySide(side.otherSide())
                val pixelIsInSide = transferSensorState.hasPixelBeenSeen
                return if (pixelIsInSide) {
                    Claw.ClawTarget.Gripping
                } else {
                    previousHandoffTarget.depoTarget.wristPosition.getClawTargetBySide(side)
                }
            }

            fun openLatchIfClawHasControl(side: Transfer.Side): Transfer.LatchTarget {
                val matchingClaw = wrist.getClawBySide(side.otherSide())
                val matchingClawActualAngle = actualRobot.depoState.wristAngles.getBySide(side.otherSide())
                val clawIsGripping = matchingClaw.isClawAtAngle(Claw.ClawTarget.Gripping, matchingClawActualAngle)
                val latchTarget = if (clawIsGripping) {
                    Transfer.LatchPositions.Open
                } else {
                    Transfer.LatchPositions.Closed
                }
                return transfer.getLatchTarget(side, latchTarget, previousHandoffTarget.collectorTarget.latches)
            }

            HandoffTarget(
                    collectorTarget = uncoordinatedHandoffTarget.collectorTarget.copy(
                            latches = getTransferTargetBasedOnRule(::openLatchIfClawHasControl),
                            extendo = SlideSubsystem.TargetSlideSubsystem(Extendo.ExtendoPositions.Min, DualMovementModeSubsystem.MovementMode.Position),
                    ),
                    depoTarget = uncoordinatedHandoffTarget.depoTarget.copy(
                            lift = Lift.TargetLift(targetPosition = Lift.LiftPositions.Down),
                            armPosition = Arm.ArmTarget(targetPosition = Arm.Positions.In),
                            wristPosition = getWristTargetBasedOnRule(::gripClawIfPixelInSide),
                    ),
            )
        } else {
            //Handle ending the handoff

            fun retractClawIfTransferHasPixel(side: Transfer.Side): Claw.ClawTarget {
                val latchHasActuallyAchievedTarget = transfer.checkIfLatchHasActuallyAchievedTarget(side.otherSide(), Transfer.LatchPositions.Closed, previousHandoffTarget.collectorTarget.latches)

                return if (latchHasActuallyAchievedTarget) {
                    Claw.ClawTarget.Retracted
                } else {
                    previousHandoffTarget.depoTarget.wristPosition.getClawTargetBySide(side)
                }
            }

            val bothLatchesAreClosed = Transfer.Side.entries.fold(true) {acc, it ->
                acc && transfer.checkIfLatchHasActuallyAchievedTarget(it, Transfer.LatchPositions.Closed, previousHandoffTarget.collectorTarget.latches)
            }

            if (bothLatchesAreClosed) {
                HandoffTarget(
                        collectorTarget = uncoordinatedHandoffTarget.collectorTarget.copy(
                                latches = getTransferTargetBasedOnRule{transfer.getLatchTarget(it, Transfer.LatchPositions.Closed, previousHandoffTarget.collectorTarget.latches)},
                                extendo = SlideSubsystem.TargetSlideSubsystem(Extendo.ExtendoPositions.Min, DualMovementModeSubsystem.MovementMode.Position),
                        ),
                        depoTarget = uncoordinatedHandoffTarget.depoTarget.copy(
                                lift = Lift.TargetLift(targetPosition = Lift.LiftPositions.Down),
                                armPosition = Arm.ArmTarget(targetPosition = Arm.Positions.In),
                                wristPosition = getWristTargetBasedOnRule(::retractClawIfTransferHasPixel),
                        ),
                )
            } else {
                HandoffTarget(
                        collectorTarget = uncoordinatedHandoffTarget.collectorTarget.copy(
                                latches = getTransferTargetBasedOnRule{transfer.getLatchTarget(it, Transfer.LatchPositions.Closed, previousHandoffTarget.collectorTarget.latches)}
                        ),
                        depoTarget = uncoordinatedHandoffTarget.depoTarget.copy(
                                wristPosition = getWristTargetBasedOnRule(::retractClawIfTransferHasPixel),
                        ),
                )
            }
        }
    }


    //Latches need to close before claws release
    //Brody, while robot is backing up turn on extendo motors to hold position
    //drop down mapped to right joystick y. it auto goes up when intake is off or ejecting

    fun getHandoffState(actualRobot: ActualRobot, previousTargetWorld: TargetWorld): SideIsActivelyHandingOff {
        val liftIsAtAHeightWhereLatchesCouldConflict = !lift.isLiftAbovePosition(targetPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks, actualLiftPositionTicks = actualRobot.depoState.lift.currentPositionTicks)
        val armIsInAPositionWhereLatchesCouldConflict = actualRobot.depoState.armAngleDegrees > Arm.Positions.InsideTheBatteryBox.angleDegrees

        return if (liftIsAtAHeightWhereLatchesCouldConflict && armIsInAPositionWhereLatchesCouldConflict) {
            fun checkIfIsActivelyHandingOffPerSide(side: Transfer.Side): Boolean {
                val claw = wrist.clawsAsMap[side]!!
                val clawIsClosed = claw.isClawAtAngle(Claw.ClawTarget.Gripping, actualRobot.depoState.wristAngles.getBySide(side))
//                val extendoIsIn = extendo.isSlideSystemAllTheWayIn(actualRobot.collectorSystemState.extendo)

                return clawIsClosed// && extendoIsIn
            }

            SideIsActivelyHandingOff(
                    left = checkIfIsActivelyHandingOffPerSide(Transfer.Side.Left),
                    right = checkIfIsActivelyHandingOffPerSide(Transfer.Side.Right)
            )
        } else {
            SideIsActivelyHandingOff(
                    left = false,
                    right = false
            )
        }
    }

    fun checkIfHandoffIsReadyToStart(actualWorld: ActualWorld, previousActualWorld: ActualWorld): Boolean {
        val isExtendoAllTheWayIn = extendo.isSlideSystemAllTheWayIn(actualWorld.actualRobot.collectorSystemState.extendo)
        val extendoIsMovingInOrNotAtAll = extendo.getVelocityTicksPerMili(actualWorld, previousActualWorld) <= 0
        val extendoIsReady = extendoIsMovingInOrNotAtAll && isExtendoAllTheWayIn

        val liftExtensionIsAllTheWayDown = lift.isSlideSystemAllTheWayIn(actualWorld.actualRobot.depoState.lift)//10 >= actualWorld.actualRobot.depoState.lift.currentPositionTicks//actualWorld.actualRobot.depoState.isLiftLimitActivated
        telemetry.addLine("isExtendoAllTheWayIn: $isExtendoAllTheWayIn")
        telemetry.addLine("liftExtensionIsAllTheWayDown: $liftExtensionIsAllTheWayDown")

        val isArmReadyToTransfer = arm.checkIfArmIsAtTarget(Arm.Positions.In, actualWorld.actualRobot.depoState.armAngleDegrees)
        telemetry.addLine("isArmReadyToTransfer: $isArmReadyToTransfer")

        val readyToHandoff = extendoIsReady && liftExtensionIsAllTheWayDown && isArmReadyToTransfer
        return readyToHandoff
    }

    fun checkIfLatchHasSecuredPixelsFromClaw(side: Transfer.Side, actualWorld: ActualWorld, previousTransferTarget: Transfer.TransferTarget): Boolean {
        val latchTarget = previousTransferTarget.getBySide(side)

        val latchIsClosed = Transfer.LatchPositions.Closed == latchTarget.target

        val timeSinceLatchChangedTarget = actualWorld.timestampMilis - latchTarget.timeTargetChangedMillis
        val timeLatchHasToBeClosedToSecurePixelMillis = 500
        val latchTargetChangeWasLongEnoughAgo = timeSinceLatchChangedTarget >= timeLatchHasToBeClosedToSecurePixelMillis

        return latchIsClosed && latchTargetChangeWasLongEnoughAgo
    }
}