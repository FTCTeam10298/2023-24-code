package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
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
    enum class HandoffReadiness {
        FinishHandoff,
        StartHandoff,
        DontHandoff,
    }
    data class HandoffPixelsToLift(
            override val left: HandoffReadiness,
            override val right: HandoffReadiness
    ): Side.ThingWithSides<HandoffReadiness> {
        constructor(both: HandoffReadiness): this(both, both)
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
            val extendo: ExtendoCoordinationStates,
            val depo: DepoCoordinationStates,
            val handoffPixelsToLift: HandoffPixelsToLift
    )
    data class HandoffCoordinated(
            val extendo: ExtendoCoordinationStates,
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

    enum class PixelController {
        Depo,
        Collector,
        Both,
        NoPixel,
    }


    data class OneSideCoordinatedExtremeties(
            val latch: HandoffCoordinated.PixelHolder,
            val claw: HandoffCoordinated.PixelHolder
    )

    fun detectActualController(
            side: Side,
            actualExtendo: ExtendoCoordinationStates,
            actualLatches: HandoffCoordinated.SidedPixelHolders,
            actualDepo: DepoCoordinationStates,
            actualWrist: HandoffCoordinated.SidedPixelHolders,
            transferSensorState: TransferSensorState
    ): PixelController {
        val pixelIsDetectedBySensor = transferSensorState.getBySide(side).hasPixelBeenSeen
        val latchIsClosed = HandoffCoordinated.PixelHolder.Holding == actualLatches.getBySide(side)

        val clawIsGripping = HandoffCoordinated.PixelHolder.Holding == actualWrist.getBySide(side)

        val extendoIsIn = actualExtendo == ExtendoCoordinationStates.ReadyToHandoff
        val liftIsIn = actualDepo == DepoCoordinationStates.ReadyToHandoff
        val bothSlidesAreIn = liftIsIn && extendoIsIn

        return when {
            bothSlidesAreIn && pixelIsDetectedBySensor && latchIsClosed && clawIsGripping -> PixelController.Both
            pixelIsDetectedBySensor && latchIsClosed -> PixelController.Collector
            clawIsGripping && ((pixelIsDetectedBySensor && !latchIsClosed && bothSlidesAreIn) || (!pixelIsDetectedBySensor && !bothSlidesAreIn)) -> PixelController.Depo
            else -> PixelController.NoPixel
        }
    }

    fun decideFinalController(
            side: Side,
            actualController: PixelController,
            inputConstraints: HandoffConstraints
    ): PixelController {
        val thereIsAPixel = actualController != PixelController.NoPixel
        return if (thereIsAPixel) {
            val pixelHanoffDesire = inputConstraints.handoffPixelsToLift.getBySide(side)

            when (pixelHanoffDesire) {
                HandoffReadiness.FinishHandoff -> PixelController.Depo
                HandoffReadiness.StartHandoff -> PixelController.Both
                HandoffReadiness.DontHandoff -> {
                    when (actualController) {
                        PixelController.Depo -> {
                            when (inputConstraints.depo) {
                                DepoCoordinationStates.ReadyToHandoff -> PixelController.Collector
                                DepoCoordinationStates.PotentiallyBlockingExtendoMovement -> PixelController.Depo
                                DepoCoordinationStates.NotReady -> PixelController.Both
                            }
                        }
                        else -> PixelController.Collector
                    }
                }
            }
        } else {
            PixelController.NoPixel
        }
    }

    fun resolveControllerDifference(finalController: PixelController, actualController: PixelController, actualExtendo: ExtendoCoordinationStates, actualDepo: DepoCoordinationStates): PixelController {
        val airlockLogic = if (actualController == finalController) {
            finalController
        } else {
            when (actualController) {
                PixelController.Depo -> PixelController.Both
                PixelController.Collector -> PixelController.Both
                PixelController.Both -> finalController
                PixelController.NoPixel -> PixelController.NoPixel
            }
        }

        val extendoIsIn = actualExtendo == ExtendoCoordinationStates.ReadyToHandoff
        val depoIsIn = actualDepo == DepoCoordinationStates.ReadyToHandoff
        val legalMove = if (extendoIsIn && depoIsIn) {
            airlockLogic
        } else {
            actualController
        }
        return legalMove
    }

    fun latchFromTargetController(targetPixelController: PixelController): HandoffCoordinated.PixelHolder = when (targetPixelController) {
        PixelController.Depo -> HandoffCoordinated.PixelHolder.Released
        PixelController.Both -> HandoffCoordinated.PixelHolder.Holding
        PixelController.Collector -> HandoffCoordinated.PixelHolder.Holding
        PixelController.NoPixel -> HandoffCoordinated.PixelHolder.Holding
    }

    fun clawFromTargetController(targetPixelController: PixelController): HandoffCoordinated.PixelHolder = when (targetPixelController) {
        PixelController.Depo -> HandoffCoordinated.PixelHolder.Holding
        PixelController.Both -> HandoffCoordinated.PixelHolder.Holding
        PixelController.Collector -> HandoffCoordinated.PixelHolder.Released
        PixelController.NoPixel -> HandoffCoordinated.PixelHolder.Released
    }

    fun coordinateHandoff(
            inputConstraints: HandoffConstraints,
            physicalExtendoReadiness: ExtendoCoordinationStates,
            actualLatches: HandoffCoordinated.SidedPixelHolders,
            actualDepo: DepoCoordinationStates,
            actualWrist: HandoffCoordinated.SidedPixelHolders,
            transferSensorState: TransferSensorState
    ): HandoffCoordinated {

        telemetry.addLine("\ninputConstraints: $inputConstraints")
        telemetry.addLine("\nactualExtendo: $physicalExtendoReadiness")
        telemetry.addLine("\nactualLatches: $actualLatches")
        telemetry.addLine("\nactualDepo: $actualDepo")
        telemetry.addLine("\nactualWrist: $actualWrist")
        telemetry.addLine("\ntransferSensorState: $transferSensorState")

        fun deriveTargetController(side: Side): Pair<PixelController, Boolean> {
            val actualController = detectActualController(
                    side = side,
                    actualExtendo = physicalExtendoReadiness,
                    actualLatches = actualLatches,
                    actualDepo = actualDepo,
                    actualWrist = actualWrist,
                    transferSensorState = transferSensorState
            )

            val finalController = decideFinalController(
                    side = side,
                    actualController,
                    inputConstraints,
            )
            val targetPixelController = resolveControllerDifference(
                    finalController = finalController,
                    actualController = actualController,
                    actualExtendo = physicalExtendoReadiness,
                    actualDepo = actualDepo
            )

            telemetry.addLine("\n$side pixel, actual controller: $actualController, final controller: $finalController, intermediate target: $targetPixelController")

            val controllerIsAtFinalState = actualController == finalController

            return targetPixelController to controllerIsAtFinalState
        }

        val left = deriveTargetController(Side.Left)
        val right = deriveTargetController(Side.Right)


        val latches = HandoffCoordinated.SidedPixelHolders(
                left = latchFromTargetController(left.first),
                right = latchFromTargetController(right.first)
        )
        val wrist = HandoffCoordinated.SidedPixelHolders(
                left = clawFromTargetController(left.first),
                right = clawFromTargetController(right.first)
        )

        val bothActualControllersAreAtFinal = left.second && right.second

        return if (bothActualControllersAreAtFinal) {
            HandoffCoordinated(
                    extendo = inputConstraints.extendo,
                    depo = inputConstraints.depo,

                    latches = latches,
                    wrist = wrist
            )
        } else {
            HandoffCoordinated(
                    extendo = ExtendoCoordinationStates.ReadyToHandoff,
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
    fun manageHandoff(handoffInput: RobotTwoTeleOp.HandoffInput, wristInput: RobotTwoTeleOp.WristInput, depoInput: RobotTwoTeleOp.DepoInput, extendoInput: RobotTwoTeleOp.ExtendoInput, collectorTarget: CollectorTarget, previousTargetWorld: TargetWorld, actualWorld: ActualWorld): CollectorDepositorTarget {

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

        val driverExtendoHandoffReadiness = when (extendoInput) {
            RobotTwoTeleOp.ExtendoInput.RetractManual, RobotTwoTeleOp.ExtendoInput.RetractSetAmount, RobotTwoTeleOp.ExtendoInput.NoInput -> ExtendoCoordinationStates.ReadyToHandoff
            else -> ExtendoCoordinationStates.NotReady
        }
        val driverDepositorHandoffReadiness = when (depoInput) {
            RobotTwoTeleOp.DepoInput.Manual -> DepoCoordinationStates.ReadyToHandoff
            RobotTwoTeleOp.DepoInput.NoInput -> DepoCoordinationStates.ReadyToHandoff
            RobotTwoTeleOp.DepoInput.Down -> DepoCoordinationStates.ReadyToHandoff
            else -> DepoCoordinationStates.NotReady
        }

        fun deriveHandoffReadiness(handoffInput: RobotTwoTeleOp.HandoffInput, clawInput: RobotTwoTeleOp.ClawInput): HandoffReadiness {
            val liftInputIsDown = driverDepositorHandoffReadiness == DepoCoordinationStates.ReadyToHandoff
            val extendoInputIsIn = driverExtendoHandoffReadiness == ExtendoCoordinationStates.ReadyToHandoff
            val inputAllowsForHandoff = liftInputIsDown && extendoInputIsIn

            return when (handoffInput) {
                RobotTwoTeleOp.HandoffInput.Handoff -> HandoffReadiness.FinishHandoff
                RobotTwoTeleOp.HandoffInput.NoInput -> {
                    when (clawInput) {
                        RobotTwoTeleOp.ClawInput.Drop -> HandoffReadiness.DontHandoff
                        RobotTwoTeleOp.ClawInput.Hold -> HandoffReadiness.StartHandoff
                        RobotTwoTeleOp.ClawInput.NoInput -> {
                            val automaticallyHandoff = inputAllowsForHandoff
                            if (automaticallyHandoff) {
                                HandoffReadiness.StartHandoff
                            } else {
                                HandoffReadiness.DontHandoff
                            }
                        }
                    }

                }
            }
        }

        val handoffCoordinated = coordinateHandoff(
                inputConstraints = HandoffConstraints(
                        extendo = driverExtendoHandoffReadiness,
                        depo = driverDepositorHandoffReadiness,
                        handoffPixelsToLift = HandoffPixelsToLift(
                                left = deriveHandoffReadiness(handoffInput, wristInput.left),
                                right = deriveHandoffReadiness(handoffInput, wristInput.right)
                        )
                ),
                physicalExtendoReadiness =  when {
                    actualWorld.actualRobot.collectorSystemState.extendo.limitSwitchIsActivated -> ExtendoCoordinationStates.ReadyToHandoff
                    else -> ExtendoCoordinationStates.NotReady
                },
                actualLatches = getLatchesFromRule {side ->
                    transfer.checkIfLatchHasActuallyAchievedTarget(side, LatchPositions.Closed, actualWorld.timestampMilis, previousTargetWorld.targetRobot.collectorTarget.latches)
                },
                actualDepo = when {
                    actualWorld.actualRobot.depoState.lift.limitSwitchIsActivated && arm.checkIfArmIsAtTarget(Arm.Positions.In, actualWorld.actualRobot.depoState.armAngleDegrees)-> DepoCoordinationStates.ReadyToHandoff
                    !lift.isLiftAbovePosition(Lift.LiftPositions.ClearForArmToMove.ticks, actualWorld.actualRobot.depoState.lift.currentPositionTicks) -> DepoCoordinationStates.PotentiallyBlockingExtendoMovement
                    else -> DepoCoordinationStates.NotReady
                },
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
                        extendo = if (handoffCoordinated.extendo == ExtendoCoordinationStates.ReadyToHandoff) {
                            Extendo.ExtendoTarget(
                                    targetPosition = Extendo.ExtendoPositions.Min
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
                previousDepoTarget = previousTargetWorld.targetRobot.depoTarget,
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
