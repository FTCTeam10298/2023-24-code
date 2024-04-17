package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Transfer.*
import us.brainstormz.robotTwo.subsystems.Wrist

class HandoffManager(
        private val collectorManager: CollectorManager,
        private val depoManager: DepoManager,
        private val extendo: Extendo,
        private val wrist: Wrist,
        private val arm: Arm,
        private val lift: Lift,
        private val transfer: Transfer,
        private val telemetry: Telemetry) {



    // ??? Seems like this should be different
    enum class TargetPixelControlState {
        ControlledByDepositor,
        ControlledByBoth,
        ControlledByCollector,
    }
    data class TargetPixelControlStates(
        override val left: TargetPixelControlState,
        override val right: TargetPixelControlState
    ): Side.ThingWithSides<TargetPixelControlState> {
        constructor(both: TargetPixelControlState): this(both, both)
    }


    enum class ActualSlideStates {
        NotReady,
        PartiallyIn,
        ReadyToHandoff
    }

    enum class ExtendoCoordinationStates {
        NotReady,
        ReadyToHandoff
    }
    enum class DepoCoordinationStates {
        NotReady,
        PotentiallyBlockingExtendoMovement,  // might not be needed
        ReadyToHandoff
    }

    data class HandoffConstraints(
            val depo: DepoCoordinationStates,
            val targetPixelControlStates: TargetPixelControlStates
    )
    enum class ExtendoHandoffControlDecision {
        HandoffPosition, DriverControlledPosition
    }


    enum class DepoHandoffControlDecision {
        HandoffPosition,
        DriverControlledPosition
    }

    data class HandoffCoordinated(
            val extendo: ExtendoHandoffControlDecision,
            val latches: HandoffSidedOutput,

            val depo: DepoHandoffControlDecision,
            val wrist: HandoffSidedOutput,

            val handoffCompleted: Boolean
    ) {

        enum class PixelHolder {
            Holding,
            Released,
        }

        data class SidedPixelHolders(
            override val left: PixelHolder,
            override val right: PixelHolder
        ): Side.ThingWithSides<PixelHolder>

        enum class HandoffCommand {
            Holding,
            Released,
            Passthrough
        }

        data class HandoffSidedOutput(
                override val left: HandoffCommand,
                override val right: HandoffCommand
        ): Side.ThingWithSides<HandoffCommand>
    }

    enum class PixelOwner {
        Depo,
        Collector,
        Both,
        NoPixel,
    }


    data class OneSideCoordinatedExtremeties(
            val latch: HandoffCoordinated.PixelHolder,
            val claw: HandoffCoordinated.PixelHolder
    )

    private fun detectActualOwner(
            side: Side,
            actualExtendo: ActualSlideStates,
            actualLatches: HandoffCoordinated.SidedPixelHolders,
            actualDepo: ActualSlideStates,
            actualWrist: HandoffCoordinated.SidedPixelHolders,
            transferSensorState: TransferSensorState
    ): PixelOwner {
        val pixelIsDetectedBySensor = transferSensorState.getBySide(side).hasPixelBeenSeen
        val latchIsClosed = HandoffCoordinated.PixelHolder.Holding == actualLatches.getBySide(side)

        val clawIsGripping = HandoffCoordinated.PixelHolder.Holding == actualWrist.getBySide(side)

        val extendoIsIn = actualExtendo != ActualSlideStates.NotReady
//        val extendoIsIn = actualExtendo == ActualSlideStates.ReadyToHandoff
        val liftIsInHandoffPosition = actualDepo == ActualSlideStates.ReadyToHandoff
        val liftIsIn = actualDepo != ActualSlideStates.NotReady
        val bothSlidesAreIn = liftIsIn && extendoIsIn

        return when {
            bothSlidesAreIn && latchIsClosed && clawIsGripping -> PixelOwner.Both
            clawIsGripping && ((pixelIsDetectedBySensor && !latchIsClosed && bothSlidesAreIn) || (!pixelIsDetectedBySensor && !liftIsInHandoffPosition)) -> PixelOwner.Depo
            pixelIsDetectedBySensor && latchIsClosed -> PixelOwner.Collector
//            pixelIsDetectedBySensor && latchIsClosed && !clawIsGripping -> PixelOwner.Collector
            else -> PixelOwner.NoPixel
        }
    }

    private fun decidePixelDestination(
        side: Side,
        actualController: PixelOwner,
        inputConstraints: HandoffConstraints,
    ): PixelOwner {
        val thereIsAPixel = actualController != PixelOwner.NoPixel
        return if (thereIsAPixel) {
                val pixelHandoffDesire = inputConstraints.targetPixelControlStates.getBySide(side)

                when (pixelHandoffDesire) {
                    TargetPixelControlState.ControlledByDepositor -> PixelOwner.Depo
                    TargetPixelControlState.ControlledByBoth -> PixelOwner.Both
                    TargetPixelControlState.ControlledByCollector -> PixelOwner.Collector
                }
        } else {
            PixelOwner.NoPixel
        }
    }

    fun getNextTargetOwner(
        finalOwner: PixelOwner,
        currentOwner: PixelOwner,
        actualExtendo: ActualSlideStates,
        actualDepo: ActualSlideStates): PixelOwner {
        val nextOwnerInAirlockProcess = if (currentOwner == finalOwner) {
            finalOwner
        } else {
            when (currentOwner) {
                PixelOwner.Depo, PixelOwner.Collector -> PixelOwner.Both
                PixelOwner.Both -> finalOwner
                PixelOwner.NoPixel -> PixelOwner.NoPixel
            }
        }

        val freezeIfExtendoAndDepoArentReady = if (actualExtendo != ActualSlideStates.ReadyToHandoff) {
            when (currentOwner) {
                PixelOwner.Both -> {
                    when (actualDepo) {
                        ActualSlideStates.NotReady -> PixelOwner.Depo
                        ActualSlideStates.PartiallyIn -> PixelOwner.Depo
                        ActualSlideStates.ReadyToHandoff -> PixelOwner.Collector
                    }
                }
                else -> currentOwner
            }
        } else {
            nextOwnerInAirlockProcess
        }

        return freezeIfExtendoAndDepoArentReady
    }

    fun latchFromTargetController(targetPixelOwner: PixelOwner): HandoffCoordinated.HandoffCommand = when (targetPixelOwner) {
        PixelOwner.Depo -> HandoffCoordinated.HandoffCommand.Released
        PixelOwner.Both -> HandoffCoordinated.HandoffCommand.Holding
        PixelOwner.Collector -> HandoffCoordinated.HandoffCommand.Holding
        PixelOwner.NoPixel -> HandoffCoordinated.HandoffCommand.Holding
    }

    fun clawFromTargetController(targetPixelOwner: PixelOwner): HandoffCoordinated.HandoffCommand = when (targetPixelOwner) {
        PixelOwner.Depo -> HandoffCoordinated.HandoffCommand.Holding
        PixelOwner.Both -> HandoffCoordinated.HandoffCommand.Holding
        PixelOwner.Collector -> HandoffCoordinated.HandoffCommand.Released
        PixelOwner.NoPixel -> HandoffCoordinated.HandoffCommand.Released
    }

    fun coordinateHandoff(
            inputConstraints: HandoffConstraints,
            physicalExtendoReadiness: ActualSlideStates,
            actualLatches: HandoffCoordinated.SidedPixelHolders,
            actualDepo: ActualSlideStates,
            actualWrist: HandoffCoordinated.SidedPixelHolders,
            transferSensorState: TransferSensorState,
    ): HandoffCoordinated {

        telemetry.addLine("\ninputConstraints: $inputConstraints")
        telemetry.addLine("\nactualExtendo: $physicalExtendoReadiness")
        telemetry.addLine("\nactualLatches: $actualLatches")
        telemetry.addLine("\nactualDepo: $actualDepo")
        telemetry.addLine("\nactualWrist: $actualWrist")
        telemetry.addLine("\ntransferSensorState: $transferSensorState")

        data class PixelHandoffState(
            val currentOwner:PixelOwner,
            val targetOwner:PixelOwner,
            val finalOwner:PixelOwner,
        ){
            val pixelIsWithFinalOwner = currentOwner == finalOwner
        }
        fun determinePixelHandoffState(side: Side): PixelHandoffState {
            val currentOwner = detectActualOwner(
                    side = side,
                    actualExtendo = physicalExtendoReadiness,
                    actualLatches = actualLatches,
                    actualDepo = actualDepo,
                    actualWrist = actualWrist,
                    transferSensorState = transferSensorState
            )

            val finalOwner = decidePixelDestination(
                    side = side,
                    currentOwner,
                    inputConstraints,
            )
            val targetPixelController = getNextTargetOwner(
                    finalOwner = finalOwner,
                    currentOwner = currentOwner,
                    actualExtendo = physicalExtendoReadiness,
                    actualDepo = actualDepo
            )

            telemetry.addLine("\n$side pixel, actual controller: $currentOwner, final controller: $finalOwner, intermediate target: $targetPixelController")


            return PixelHandoffState(
                currentOwner,
                targetPixelController,
                finalOwner)
        }

        val leftPixelStatus = determinePixelHandoffState(Side.Left)
        val rightPixelStatus = determinePixelHandoffState(Side.Right)

        val bothPixelsAreWithFinalOwner = leftPixelStatus.pixelIsWithFinalOwner && rightPixelStatus.pixelIsWithFinalOwner

        val depoIsNotOut = actualDepo != ActualSlideStates.NotReady
        val doHandoff = Side.entries.fold(false) { acc, side ->
            acc || inputConstraints.targetPixelControlStates.getBySide(side) != TargetPixelControlState.ControlledByCollector
        }
        val eitherPixelTargetIsBoth = leftPixelStatus.targetOwner == PixelOwner.Both || rightPixelStatus.targetOwner == PixelOwner.Both

//        val tryingToHandoff = (eitherPixelTargetIsBoth && depoIsNotOut) || doHandoff
//        val doneWithManagingThings = bothPixelsAreWithFinalOwner
//        val handoffCanRelinquishControlOfEverything = tryingToHandoff && doneWithManagingThings
        val handoffCanRelinquishControlOfEverything = bothPixelsAreWithFinalOwner && !(eitherPixelTargetIsBoth || (depoIsNotOut && doHandoff))

        return if (handoffCanRelinquishControlOfEverything) {
            HandoffCoordinated(
                    extendo = ExtendoHandoffControlDecision.DriverControlledPosition,
                    depo = DepoHandoffControlDecision.DriverControlledPosition,

                    latches = HandoffCoordinated.HandoffSidedOutput(
                        left = HandoffCoordinated.HandoffCommand.Passthrough,
                        right = HandoffCoordinated.HandoffCommand.Passthrough
                    ),
                    wrist = HandoffCoordinated.HandoffSidedOutput(
                        left = HandoffCoordinated.HandoffCommand.Passthrough,
                        right = HandoffCoordinated.HandoffCommand.Passthrough
                    ),
                    handoffCompleted = true
            )
        } else {
            val onePixelWantsToBeWithDepo = leftPixelStatus.targetOwner == PixelOwner.Depo || rightPixelStatus.targetOwner == PixelOwner.Depo

            val latches = HandoffCoordinated.HandoffSidedOutput(
                    left = latchFromTargetController(leftPixelStatus.targetOwner),
                    right = latchFromTargetController(rightPixelStatus.targetOwner)
            )

            return if (onePixelWantsToBeWithDepo) {
                HandoffCoordinated(
                        extendo = ExtendoHandoffControlDecision.HandoffPosition,
                        depo = DepoHandoffControlDecision.DriverControlledPosition,

                        latches = latches,
                        wrist = HandoffCoordinated.HandoffSidedOutput(
                                left = HandoffCoordinated.HandoffCommand.Passthrough,
                                right = HandoffCoordinated.HandoffCommand.Passthrough
                        ),
                        handoffCompleted = true
                )
            } else {


                HandoffCoordinated(
                        extendo = ExtendoHandoffControlDecision.HandoffPosition,
                        depo = DepoHandoffControlDecision.HandoffPosition,

                        latches = latches,
                        wrist = HandoffCoordinated.HandoffSidedOutput(
                                left = clawFromTargetController(leftPixelStatus.targetOwner),
                                right = clawFromTargetController(rightPixelStatus.targetOwner)
                        ),
                        handoffCompleted = false
                )
            }
        }
    }

    data class CollectorDepositorTarget(
            val collector: CollectorTarget,
            val depo: DepoTarget,
            val handoffCompleted: Boolean
    )
    fun manageHandoff(
        wristInput: RobotTwoTeleOp.WristInput,
        depoInput: RobotTwoTeleOp.DepoInput,
        extendoInput: RobotTwoTeleOp.ExtendoInput,
        collectorTarget: CollectorTarget,
        previousTargetWorld: TargetWorld,
        actualWorld: ActualWorld,
        doingHandoff:Boolean,
    ): CollectorDepositorTarget {

        fun deriveLatchPositionFromPixelHolder(pixelHolder: HandoffCoordinated.HandoffCommand, driverInputLatchPosition: LatchPositions): LatchPositions {
            return when(pixelHolder) {
                HandoffCoordinated.HandoffCommand.Holding -> LatchPositions.Closed
                HandoffCoordinated.HandoffCommand.Released -> LatchPositions.Open
                HandoffCoordinated.HandoffCommand.Passthrough -> driverInputLatchPosition
            }
        }
        fun deriveClawTargetFromPixelHolder(claw: HandoffCoordinated.HandoffCommand, clawInput: RobotTwoTeleOp.ClawInput): RobotTwoTeleOp.ClawInput {
            return when(claw) {
                HandoffCoordinated.HandoffCommand.Holding -> RobotTwoTeleOp.ClawInput.Hold
                HandoffCoordinated.HandoffCommand.Released -> RobotTwoTeleOp.ClawInput.Drop
                HandoffCoordinated.HandoffCommand.Passthrough -> clawInput
            }
        }

        fun getPixelHolderFromIsHolding(isHolding: Boolean): HandoffCoordinated.PixelHolder {
            return if (isHolding) {
                HandoffCoordinated.PixelHolder.Holding
            } else {
                HandoffCoordinated.PixelHolder.Released
            }
        }

        fun getLatchesFromRule(checkIfIsHolding: (side:Side)->Boolean): HandoffCoordinated.SidedPixelHolders {
            return HandoffCoordinated.SidedPixelHolders(
                    left = getPixelHolderFromIsHolding(checkIfIsHolding(Side.Left)),
                    right = getPixelHolderFromIsHolding(checkIfIsHolding(Side.Right))
            )
        }
        fun getWristFromRule(checkIfIsHolding: (side:Side)->Boolean): HandoffCoordinated.SidedPixelHolders {
            return HandoffCoordinated.SidedPixelHolders(
                    left = getPixelHolderFromIsHolding(checkIfIsHolding(Side.Left)),
                    right = getPixelHolderFromIsHolding(checkIfIsHolding(Side.Right))
            )
        }

        val areDriversRetractingExtendo = when (extendoInput) {
            RobotTwoTeleOp.ExtendoInput.RetractManual, RobotTwoTeleOp.ExtendoInput.RetractSetAmount -> ExtendoCoordinationStates.ReadyToHandoff
            else -> ExtendoCoordinationStates.NotReady
        }
        val driverDepositorHandoffReadiness = when (depoInput) {
            RobotTwoTeleOp.DepoInput.Manual -> DepoCoordinationStates.ReadyToHandoff
            RobotTwoTeleOp.DepoInput.NoInput -> DepoCoordinationStates.ReadyToHandoff
            RobotTwoTeleOp.DepoInput.Down -> DepoCoordinationStates.ReadyToHandoff
            else -> DepoCoordinationStates.NotReady
        }

        fun determineTargetPixelControlState(
            side: Side,
            doingHandoff: Boolean,
            inputDepo: DepoCoordinationStates,
            actualExtendo: ActualSlideStates,
            actualDepo: ActualSlideStates
        ): TargetPixelControlState {
            val liftInputIsDown = driverDepositorHandoffReadiness != DepoCoordinationStates.NotReady
            val extendoInputIsIn = areDriversRetractingExtendo == ExtendoCoordinationStates.ReadyToHandoff
            val inputAllowsForHandoff = liftInputIsDown && extendoInputIsIn

            val actualDepoAndExtendoAreInPositionForHandoff = actualExtendo == ActualSlideStates.ReadyToHandoff && actualDepo == ActualSlideStates.ReadyToHandoff

            return when(doingHandoff){
                true -> TargetPixelControlState.ControlledByDepositor
                false -> if(inputAllowsForHandoff && actualDepoAndExtendoAreInPositionForHandoff){
                    TargetPixelControlState.ControlledByBoth
                } else {
                    if (inputDepo == DepoCoordinationStates.NotReady) {
                        TargetPixelControlState.ControlledByDepositor
                    } else {
                        TargetPixelControlState.ControlledByCollector
                    }
                }
            }
        }

        val physicalExtendoReadiness = when {
            actualWorld.actualRobot.collectorSystemState.extendo.limitSwitchIsActivated -> ActualSlideStates.ReadyToHandoff
            actualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks <= Extendo.ExtendoPositions.InPastBatteryBox.ticks && !actualWorld.actualRobot.collectorSystemState.extendo.limitSwitchIsActivated -> ActualSlideStates.PartiallyIn
            else -> ActualSlideStates.NotReady
        }

        val armIsOutIsh = actualWorld.actualRobot.depoState.armAngleDegrees <= Arm.Positions.InsideTheBatteryBox.angleDegrees
        val actualDepo = when {
            actualWorld.actualRobot.depoState.lift.limitSwitchIsActivated && arm.checkIfArmIsAtTarget(Arm.Positions.In, actualWorld.actualRobot.depoState.armAngleDegrees)-> ActualSlideStates.ReadyToHandoff
            !lift.isLiftAbovePosition(Lift.LiftPositions.ClearForArmToMove.ticks, actualWorld.actualRobot.depoState.lift.currentPositionTicks) && !armIsOutIsh -> ActualSlideStates.PartiallyIn
            else -> ActualSlideStates.NotReady
        }

        val handoffCoordinated = coordinateHandoff(
                inputConstraints = HandoffConstraints(
                    depo = driverDepositorHandoffReadiness,
                    targetPixelControlStates = TargetPixelControlStates(
                        left = determineTargetPixelControlState(Side.Left, doingHandoff, driverDepositorHandoffReadiness, physicalExtendoReadiness, actualDepo),
                        right = determineTargetPixelControlState(Side.Right, doingHandoff, driverDepositorHandoffReadiness, physicalExtendoReadiness, actualDepo),
                    )
                ),
                physicalExtendoReadiness =  physicalExtendoReadiness,
                actualLatches = getLatchesFromRule {side ->
                    transfer.checkIfLatchHasActuallyAchievedTarget(side, LatchPositions.Closed, actualWorld.timestampMilis, previousTargetWorld.targetRobot.collectorTarget.latches)
                },
                actualDepo = actualDepo,
                actualWrist = getWristFromRule { latchSide ->
                    val clawSide = latchSide.otherSide()
                    wrist.getBySide(clawSide).isClawAtAngle(Claw.ClawTarget.Gripping, actualWorld.actualRobot.depoState.wristAngles.getBySide(clawSide))
                },
                transferSensorState = collectorTarget.transferSensorState,
        )

        val depoDriverInput = RobotTwoTeleOp.noInput.copy(
                depo = when (handoffCoordinated.depo) {
                    DepoHandoffControlDecision.HandoffPosition -> RobotTwoTeleOp.DepoInput.Down
                    DepoHandoffControlDecision.DriverControlledPosition -> depoInput
                },
                wrist = RobotTwoTeleOp.WristInput(
                        right = deriveClawTargetFromPixelHolder(handoffCoordinated.wrist.left, wristInput.right),
                        left = deriveClawTargetFromPixelHolder(handoffCoordinated.wrist.right, wristInput.left),
                ),
        )

        telemetry.addLine("\nhandoffCoordinated: $handoffCoordinated")

        val driversAreRetractingExtendo = extendoInput == RobotTwoTeleOp.ExtendoInput.RetractManual || extendoInput == RobotTwoTeleOp.ExtendoInput.RetractSetAmount
        val driversAreExtendoExtendo = extendoInput == RobotTwoTeleOp.ExtendoInput.ExtendManual

        val extendoPassedThroughFromHandoff = if (driversAreRetractingExtendo) {
            collectorTarget.extendo
        } else {
            if (!actualWorld.actualRobot.depoState.lift.limitSwitchIsActivated && depoDriverInput.depo == RobotTwoTeleOp.DepoInput.Down && !driversAreExtendoExtendo) {
                Extendo.ExtendoTarget(
                        targetPosition = Extendo.ExtendoPositions.OutFarEnoughToCompletelyClearDepo,
                        movementMode = DualMovementModeSubsystem.MovementMode.Position,
                )
            } else {
                val realTarget = if (handoffCoordinated.extendo == ExtendoHandoffControlDecision.HandoffPosition) {
                    Extendo.ExtendoTarget(
                            targetPosition = Extendo.ExtendoPositions.Min,
                            movementMode = DualMovementModeSubsystem.MovementMode.Position,
                    )
                } else {
                    collectorTarget.extendo
                }

                val extendoTargetIsIn = realTarget.targetPosition == Extendo.ExtendoPositions.Min && realTarget.movementMode == DualMovementModeSubsystem.MovementMode.Position

                val extendoIsSuperCloseToZero = actualWorld.actualRobot.collectorSystemState.extendo.currentPositionTicks <= 300
                val extendoIsntAtLimit = !actualWorld.actualRobot.collectorSystemState.extendo.limitSwitchIsActivated

                if (extendoTargetIsIn && extendoIsSuperCloseToZero && extendoIsntAtLimit) {
                    Extendo.ExtendoTarget(
                            power = -extendo.findResetPower,
                            movementMode = DualMovementModeSubsystem.MovementMode.Power,
                            targetPosition = Extendo.ExtendoPositions.Min
                    )
                } else {
                    realTarget
                }
            }
        }

        val coordinatedCollector = collectorManager.coordinateCollector(
                timestampMillis = actualWorld.timestampMilis,
                uncoordinatedTarget = collectorTarget.copy(
                        extendo = extendoPassedThroughFromHandoff,
                        latches = TransferTarget(
                                left = LatchTarget(deriveLatchPositionFromPixelHolder(handoffCoordinated.latches.left, collectorTarget.latches.left.target), 0),
                                right= LatchTarget(deriveLatchPositionFromPixelHolder(handoffCoordinated.latches.right, collectorTarget.latches.right.target), 0)
                        ),
                ),
                previousTargetWorld = previousTargetWorld,
        )

        val coordinatedDepo = depoManager.fullyManageDepo(
                target = depoDriverInput,
                previousTarget = previousTargetWorld.targetRobot.depoTarget,
                actualWorld = actualWorld,
                handoffCompleted = handoffCoordinated.handoffCompleted
        )

        val armShouldHoldCurrentPositionBecauseWereTransferring = !handoffCoordinated.handoffCompleted && actualWorld.actualRobot.collectorSystemState.extendo.limitSwitchIsActivated && handoffCoordinated.depo == DepoHandoffControlDecision.DriverControlledPosition
        val coordinatedDepoWithArm = coordinatedDepo.copy(
            armPosition = if (armShouldHoldCurrentPositionBecauseWereTransferring) {
                Arm.ArmTarget(
                        targetPosition = Arm.VariableArmTarget(actualWorld.actualRobot.depoState.armAngleDegrees),
                        movementMode = DualMovementModeSubsystem.MovementMode.Position,
                        power = 0.0
                )
            } else {
                coordinatedDepo.armPosition
            }
        )

        return CollectorDepositorTarget(
                collector = coordinatedCollector,
                depo = coordinatedDepoWithArm,
                handoffCompleted = handoffCoordinated.handoffCompleted
        )
    }

    //Brody, while robot is backing up turn on extendo motors to hold position
    //drop down mapped to right joystick y. it auto goes up when intake is off or ejecting
}
