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
    enum class ExtendoHandoffControlDecision{
        HandoffPosition, DriverControlledPosition
    }
    data class HandoffCoordinated(
            val extendo: ExtendoHandoffControlDecision,
            val latches: SidedPixelHolders,

            val depo: DepoCoordinationStates,
            val wrist: SidedPixelHolders,
    ) {
        enum class PixelHolder {
            Holding,
            Released
        }

        data class SidedPixelHolders(
                override val left: PixelHolder,
                override val right: PixelHolder
        ): Side.ThingWithSides<PixelHolder>

        enum class Claw {
            Holding,
            Released,
            Passthrough
        }
        data class Wrist(
                override val left: Claw,
                override val right: Claw
        ): Side.ThingWithSides<Claw>
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
            actualExtendo: ExtendoCoordinationStates,
            actualLatches: HandoffCoordinated.SidedPixelHolders,
            actualDepo: DepoCoordinationStates,
            actualWrist: HandoffCoordinated.SidedPixelHolders,
            transferSensorState: TransferSensorState
    ): PixelOwner {
        val pixelIsDetectedBySensor = transferSensorState.getBySide(side).hasPixelBeenSeen
        val latchIsClosed = HandoffCoordinated.PixelHolder.Holding == actualLatches.getBySide(side)

        val clawIsGripping = HandoffCoordinated.PixelHolder.Holding == actualWrist.getBySide(side)

        val extendoIsIn = actualExtendo == ExtendoCoordinationStates.ReadyToHandoff
        val liftIsIn = actualDepo != DepoCoordinationStates.NotReady
        val bothSlidesAreIn = liftIsIn && extendoIsIn

        return when {
            bothSlidesAreIn && pixelIsDetectedBySensor && latchIsClosed && clawIsGripping -> PixelOwner.Both
            pixelIsDetectedBySensor && latchIsClosed -> PixelOwner.Collector
            clawIsGripping && ((pixelIsDetectedBySensor && !latchIsClosed && bothSlidesAreIn) || (!pixelIsDetectedBySensor && !bothSlidesAreIn)) -> PixelOwner.Depo
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

    fun getNextTargetOwner(finalOwner: PixelOwner, currentOwner: PixelOwner, actualExtendo: ExtendoCoordinationStates, actualDepo: DepoCoordinationStates): PixelOwner {
        val nextOwnerInAirlockProcess = if (currentOwner == finalOwner) {
            finalOwner
        } else {
            when (currentOwner) {
                PixelOwner.Depo -> PixelOwner.Both
                PixelOwner.Collector -> PixelOwner.Both
                PixelOwner.Both -> finalOwner
                PixelOwner.NoPixel -> PixelOwner.NoPixel
            }
        }
        return nextOwnerInAirlockProcess
    }

    fun latchFromTargetController(targetPixelOwner: PixelOwner): HandoffCoordinated.PixelHolder = when (targetPixelOwner) {
        PixelOwner.Depo -> HandoffCoordinated.PixelHolder.Released
        PixelOwner.Both -> HandoffCoordinated.PixelHolder.Holding
        PixelOwner.Collector -> HandoffCoordinated.PixelHolder.Holding
        PixelOwner.NoPixel -> HandoffCoordinated.PixelHolder.Holding
    }

    fun clawFromTargetController(targetPixelOwner: PixelOwner): HandoffCoordinated.PixelHolder = when (targetPixelOwner) {
        PixelOwner.Depo -> HandoffCoordinated.PixelHolder.Holding
        PixelOwner.Both -> HandoffCoordinated.PixelHolder.Holding
        PixelOwner.Collector -> HandoffCoordinated.PixelHolder.Released
        PixelOwner.NoPixel -> HandoffCoordinated.PixelHolder.Released
    }

    fun coordinateHandoff(
            inputConstraints: HandoffConstraints,
            physicalExtendoReadiness: ExtendoCoordinationStates,
            actualLatches: HandoffCoordinated.SidedPixelHolders,
            actualDepo: DepoCoordinationStates,
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

        val latches = HandoffCoordinated.SidedPixelHolders(
                left = latchFromTargetController(leftPixelStatus.targetOwner),
                right = latchFromTargetController(rightPixelStatus.targetOwner)
        )
        val wrist = HandoffCoordinated.SidedPixelHolders(
                left = clawFromTargetController(leftPixelStatus.targetOwner),
                right = clawFromTargetController(rightPixelStatus.targetOwner)
        )

        val bothPixelsAreWithFinalOwner = leftPixelStatus.pixelIsWithFinalOwner && rightPixelStatus.pixelIsWithFinalOwner

        return if (bothPixelsAreWithFinalOwner) {
            HandoffCoordinated(
                    extendo = ExtendoHandoffControlDecision.DriverControlledPosition,
                    depo = inputConstraints.depo,

                    latches = latches,
                    wrist = wrist
            )
        } else {
            HandoffCoordinated(
                    extendo = ExtendoHandoffControlDecision.HandoffPosition,
                    depo = DepoCoordinationStates.ReadyToHandoff,

                    latches = latches,
                    wrist = wrist
            )
        }
    }

    data class CollectorDepositorTarget(
            val collector: CollectorTarget,
            val depo: DepoTarget
    )
    fun manageHandoff(handoffInput: RobotTwoTeleOp.HandoffInput, wristInput: RobotTwoTeleOp.WristInput, depoInput: RobotTwoTeleOp.DepoInput, extendoInput: RobotTwoTeleOp.ExtendoInput, collectorTarget: CollectorTarget, previousTargetWorld: TargetWorld, actualWorld: ActualWorld, doingHandoff:Boolean): CollectorDepositorTarget {

        fun deriveLatchPositionFromPixelHolder(pixelHolder: HandoffCoordinated.PixelHolder): LatchPositions {
            return when(pixelHolder) {
                HandoffCoordinated.PixelHolder.Holding -> LatchPositions.Closed
                HandoffCoordinated.PixelHolder.Released -> LatchPositions.Open
            }
        }
        fun deriveClawTargetFromPixelHolder(claw: HandoffCoordinated.PixelHolder, clawInput: RobotTwoTeleOp.ClawInput): RobotTwoTeleOp.ClawInput {
            return when(claw) {
//            HandoffCoordinated.Claw.Holding ->
//            HandoffCoordinated.Claw.Released ->
//            HandoffCoordinated.Claw.Passthrough -> clawInput
                HandoffCoordinated.PixelHolder.Holding -> RobotTwoTeleOp.ClawInput.Hold
                HandoffCoordinated.PixelHolder.Released -> RobotTwoTeleOp.ClawInput.Drop
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
            doingHandoff: Boolean,
            actualExtendo: ExtendoCoordinationStates,
            actualDepo: DepoCoordinationStates
        ): TargetPixelControlState {
            val liftInputIsDown = driverDepositorHandoffReadiness == DepoCoordinationStates.ReadyToHandoff
            val extendoInputIsIn = areDriversRetractingExtendo == ExtendoCoordinationStates.ReadyToHandoff
            val inputAllowsForHandoff = liftInputIsDown && extendoInputIsIn

            val actualDepoAndExtendoAreInPositionForHandoff = actualExtendo == ExtendoCoordinationStates.ReadyToHandoff && actualDepo == DepoCoordinationStates.ReadyToHandoff

            return when(doingHandoff){
                true -> TargetPixelControlState.ControlledByDepositor
                false -> if(inputAllowsForHandoff && actualDepoAndExtendoAreInPositionForHandoff){
                    TargetPixelControlState.ControlledByBoth
                }else{
                    TargetPixelControlState.ControlledByCollector
                }
            }
        }

        val physicalExtendoReadiness = when {
            actualWorld.actualRobot.collectorSystemState.extendo.limitSwitchIsActivated -> ExtendoCoordinationStates.ReadyToHandoff
            else -> ExtendoCoordinationStates.NotReady
        }

        val actualDepo = when {
            actualWorld.actualRobot.depoState.lift.limitSwitchIsActivated && arm.checkIfArmIsAtTarget(Arm.Positions.In, actualWorld.actualRobot.depoState.armAngleDegrees)-> DepoCoordinationStates.ReadyToHandoff
            !lift.isLiftAbovePosition(Lift.LiftPositions.ClearForArmToMove.ticks, actualWorld.actualRobot.depoState.lift.currentPositionTicks) -> DepoCoordinationStates.PotentiallyBlockingExtendoMovement
            else -> DepoCoordinationStates.NotReady
        }

        val handoffCoordinated = coordinateHandoff(
                inputConstraints = HandoffConstraints(
                    depo = driverDepositorHandoffReadiness,
                    targetPixelControlStates = TargetPixelControlStates(
                        left = determineTargetPixelControlState(doingHandoff, physicalExtendoReadiness, actualDepo),
                        right = determineTargetPixelControlState(doingHandoff, physicalExtendoReadiness, actualDepo),
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

        telemetry.addLine("\nhandoffCoordinated: $handoffCoordinated")

        val coordinatedCollector = collectorManager.coordinateCollector(
                timestampMillis = actualWorld.timestampMilis,
                uncoordinatedTarget = collectorTarget.copy(
                        extendo = if (handoffCoordinated.extendo == ExtendoHandoffControlDecision.HandoffPosition) {
                            Extendo.ExtendoTarget(
                                targetPosition = Extendo.ExtendoPositions.Min,
                                movementMode = DualMovementModeSubsystem.MovementMode.Position,
                            )
                        } else {
                            collectorTarget.extendo
                        },
                        latches = TransferTarget(
                                left = LatchTarget(deriveLatchPositionFromPixelHolder(handoffCoordinated.latches.left), 0),
                                right= LatchTarget(deriveLatchPositionFromPixelHolder(handoffCoordinated.latches.right), 0)
                        ),
                ),
                previousTargetWorld = previousTargetWorld,
        )

        val depoDriverInput = RobotTwoTeleOp.noInput.copy(
                depo = when (handoffCoordinated.depo) {
                    DepoCoordinationStates.ReadyToHandoff -> RobotTwoTeleOp.DepoInput.Down
                    else -> {
                        when (depoInput) {
                            RobotTwoTeleOp.DepoInput.Manual -> RobotTwoTeleOp.DepoInput.Down
                            RobotTwoTeleOp.DepoInput.NoInput -> RobotTwoTeleOp.DepoInput.Down
                            else -> depoInput
                        }
                    }
                },
                wrist = RobotTwoTeleOp.WristInput(
                        left = deriveClawTargetFromPixelHolder(handoffCoordinated.wrist.right, wristInput.right),
                        right = deriveClawTargetFromPixelHolder(handoffCoordinated.wrist.left, wristInput.left)
                ),
        )
        val coordinatedDepo = depoManager.fullyManageDepo(
                target = depoDriverInput,
                previousTarget = previousTargetWorld.targetRobot.depoTarget,
                actualWorld = actualWorld
        )

        return CollectorDepositorTarget(
                coordinatedCollector,
                coordinatedDepo
        )
    }

    //Brody, while robot is backing up turn on extendo motors to hold position
    //drop down mapped to right joystick y. it auto goes up when intake is off or ejecting
}
