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



    enum class HandoffReadyness {
        FinishHandoff,
        StartHandoff,
        DontHandoff
    }
    data class HandoffPixelsToLift(
            override val left: HandoffReadyness,
            override val right: HandoffReadyness
    ): Side.ThingWithSides<HandoffReadyness> {
        constructor(both: HandoffReadyness): this(both, both)
    }


    enum class ExtendoCoordinationStates {
        NotReady,
        ReadyToHandoff
    }

    enum class DepoCoordinationStates {
        NotReady,
        PotentiallyBlockingExtendoMovement,
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

    private enum class PixelController {
        Depo,
        Collector,
        Both,
        NoPixel,
    }

    private fun determineActualPixelController(side: Side, actualLatches: HandoffCoordinated.SidedPixelHolders, actualWrist: HandoffCoordinated.SidedPixelHolders, transferSensorState: TransferSensorState): PixelController {
        val pixelIsDetectedBySensor = transferSensorState.getBySide(side).hasPixelBeenSeen
        val latchIsClosed = HandoffCoordinated.PixelHolder.Holding == actualLatches.getBySide(side)
        val controlledByCollector = latchIsClosed && pixelIsDetectedBySensor

        val clawIsGripping = HandoffCoordinated.PixelHolder.Holding == actualWrist.getBySide(side)

        return when {
            clawIsGripping && controlledByCollector -> PixelController.Both
            clawIsGripping -> PixelController.Depo
            controlledByCollector -> PixelController.Collector
            else -> PixelController.NoPixel
        }
    }

    private data class OneSideCoordinatedExtremeties(
            val latch: HandoffCoordinated.PixelHolder,
            val claw: HandoffCoordinated.PixelHolder
    )

    fun coordinateHandoff(
            inputConstraints: HandoffConstraints,
            actualExtendo: ExtendoCoordinationStates,
            actualLatches: HandoffCoordinated.SidedPixelHolders,
            actualDepo: DepoCoordinationStates,
            actualWrist: HandoffCoordinated.SidedPixelHolders,
            transferSensorState: TransferSensorState
    ): HandoffCoordinated {

        telemetry.addLine("\ninputConstraints: $inputConstraints")
//        telemetry.addLine("actualState: $actualState")
        telemetry.addLine("transferSensorState: $transferSensorState")

        val getActualController = { side: Side ->
            determineActualPixelController(side, actualLatches, actualWrist, transferSensorState)
        }

        val getFinalPixelController = { side: Side ->
            val actualController = getActualController(side)

            val thereIsAPixel = actualController != PixelController.NoPixel
            if (thereIsAPixel) {
                val pixelHanoffDesire = inputConstraints.handoffPixelsToLift.getBySide(side)

                when (pixelHanoffDesire) {
                    HandoffReadyness.FinishHandoff -> PixelController.Depo
                    HandoffReadyness.StartHandoff -> PixelController.Both
                    HandoffReadyness.DontHandoff -> {
                        actualController
                    }
                }


//                val pixelIsStillInTransfer = actualState.depo != DepoCoordinationStates.NotReady
//                if (pixelIsStillInTransfer) {

//                    when {
//                        liftWantsThePixel && actualRobotAllowsForHandoff -> PixelController.Depo
//                        autoStartHandoff -> PixelController.Both
//                        else -> PixelController.Collector
//                    }

//                } else {
//                    actualController
//                }
            } else {
                PixelController.NoPixel
            }
        }


        val bothActualControllersAreAtFinal = Side.entries.fold(true) { acc, side ->
            val controller = getActualController(side)

            val controllerIsAtFinal = getFinalPixelController(side) == controller
            acc && controllerIsAtFinal
        }

        fun determineOutputFromController(side: Side, targetPixelController: PixelController): OneSideCoordinatedExtremeties = when (targetPixelController) {
            PixelController.Depo -> OneSideCoordinatedExtremeties(
                    latch = HandoffCoordinated.PixelHolder.Released,
                    claw = HandoffCoordinated.PixelHolder.Holding,
            )
            PixelController.Both -> OneSideCoordinatedExtremeties(
                    latch = HandoffCoordinated.PixelHolder.Holding,
                    claw = HandoffCoordinated.PixelHolder.Holding
            )
            PixelController.Collector -> OneSideCoordinatedExtremeties(
                    latch = HandoffCoordinated.PixelHolder.Holding,
                    claw = HandoffCoordinated.PixelHolder.Released,
            )
            PixelController.NoPixel -> OneSideCoordinatedExtremeties(
                    latch = HandoffCoordinated.PixelHolder.Holding,
                    claw = actualWrist.getBySide(side)
            )
        }


        return if (bothActualControllersAreAtFinal) {
            val outputs = {side: Side ->
                val controller = getActualController(side)
                determineOutputFromController(side, controller)
            }
            val left = outputs(Side.Left)
            val right = outputs(Side.Right)

            HandoffCoordinated(
                    extendo = inputConstraints.extendo,
                    depo = inputConstraints.depo,

                    latches = HandoffCoordinated.SidedPixelHolders(
                            left = left.latch,
                            right = right.latch
                    ),
                    wrist = HandoffCoordinated.SidedPixelHolders(
                        left = left.claw,
                        right = right.claw
                    )
            )
        } else {
            fun resolveControllerDifference(finalController: PixelController, actualController: PixelController): PixelController {
                val controlByTarget = if (finalController == PixelController.NoPixel) {
                    PixelController.Collector
                } else {
                    finalController
                }

                return if (actualController == finalController) {
                    controlByTarget
                } else {
                    when (actualController) {
                        PixelController.Depo -> PixelController.Both
                        PixelController.Collector -> PixelController.Both
                        PixelController.Both -> controlByTarget
                        PixelController.NoPixel -> PixelController.NoPixel
                    }
                }
            }

            val outputs = {side: Side ->
                determineOutputFromController(side,
                        targetPixelController = resolveControllerDifference(
                                finalController = getFinalPixelController(side),
                                actualController = getActualController(side)
                        )
                )
            }

            val left = outputs(Side.Left)
            val right = outputs(Side.Right)

            //Extensions must be in
            HandoffCoordinated(
                    extendo = ExtendoCoordinationStates.ReadyToHandoff,
                    depo = DepoCoordinationStates.ReadyToHandoff,

                    latches = HandoffCoordinated.SidedPixelHolders(
                            left = left.latch,
                            right = right.latch
                    ),
                    wrist = HandoffCoordinated.SidedPixelHolders(
                            left = left.claw,
                            right = right.claw
                    )
            )
        }
    }
    private fun deriveLatchPositionFromPixelHolder(pixelHolder: HandoffCoordinated.PixelHolder): LatchPositions {
        return when(pixelHolder) {
            HandoffCoordinated.PixelHolder.Holding -> LatchPositions.Closed
            HandoffCoordinated.PixelHolder.Released -> LatchPositions.Open
        }
    }
    private fun deriveClawTargetFromPixelHolder(claw: HandoffCoordinated.PixelHolder, clawInput: RobotTwoTeleOp.ClawInput): RobotTwoTeleOp.ClawInput {
        return when(claw) {
//            HandoffCoordinated.Claw.Holding ->
//            HandoffCoordinated.Claw.Released ->
//            HandoffCoordinated.Claw.Passthrough -> clawInput
            HandoffCoordinated.PixelHolder.Holding -> RobotTwoTeleOp.ClawInput.Hold
            HandoffCoordinated.PixelHolder.Released -> RobotTwoTeleOp.ClawInput.Drop
        }
    }

    data class HandoffTarget(
            val collector: CollectorTarget,
            val depo: DepoTarget
    )
    fun manageHandoff(handoffInput: RobotTwoTeleOp.HandoffInput, wristInput: RobotTwoTeleOp.WristInput, depoInput: RobotTwoTeleOp.DepoInput, extendoInput: RobotTwoTeleOp.ExtendoInput, collectorTarget: CollectorTarget, previousTargetWorld: TargetWorld, actualWorld: ActualWorld): HandoffTarget {

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

        val inputExtendo = when (extendoInput) {
            RobotTwoTeleOp.ExtendoInput.RetractManual -> ExtendoCoordinationStates.ReadyToHandoff
            RobotTwoTeleOp.ExtendoInput.RetractSetAmount -> ExtendoCoordinationStates.ReadyToHandoff
            else -> ExtendoCoordinationStates.NotReady
        }
        val inputDepo = when (depoInput) {
            RobotTwoTeleOp.DepoInput.Manual -> DepoCoordinationStates.ReadyToHandoff
            RobotTwoTeleOp.DepoInput.NoInput -> DepoCoordinationStates.ReadyToHandoff
            RobotTwoTeleOp.DepoInput.Down -> DepoCoordinationStates.ReadyToHandoff
            else -> DepoCoordinationStates.NotReady
        }

        fun deriveHandoffReadyness(handoffInput: RobotTwoTeleOp.HandoffInput, clawInput: RobotTwoTeleOp.ClawInput): HandoffReadyness {
            val liftInputIsDown = inputDepo == DepoCoordinationStates.ReadyToHandoff
            val extendoInputIsIn = inputExtendo == ExtendoCoordinationStates.ReadyToHandoff
            val inputAllowsForHandoff = liftInputIsDown && extendoInputIsIn

            return when (handoffInput) {
                RobotTwoTeleOp.HandoffInput.StartHandoff -> HandoffReadyness.FinishHandoff
                RobotTwoTeleOp.HandoffInput.NoInput -> {
                    when (clawInput) {
                        RobotTwoTeleOp.ClawInput.Drop -> HandoffReadyness.DontHandoff
                        RobotTwoTeleOp.ClawInput.Hold -> HandoffReadyness.StartHandoff
                        RobotTwoTeleOp.ClawInput.NoInput -> {
                            val automaticallyHandoff = inputAllowsForHandoff
                            if (automaticallyHandoff) {
                                HandoffReadyness.FinishHandoff
                            } else {
                                HandoffReadyness.DontHandoff
                            }
                        }
                    }

                }
            }
        }

        val handoffCoordinated = coordinateHandoff(
                inputConstraints = HandoffConstraints(
                        extendo = inputExtendo,
                        depo = inputDepo,
                        handoffPixelsToLift = HandoffPixelsToLift(
                                left = deriveHandoffReadyness(handoffInput, wristInput.left),
                                right = deriveHandoffReadyness(handoffInput, wristInput.right)
                        )
                ),
                actualExtendo =  when {
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
                        left = deriveClawTargetFromPixelHolder(handoffCoordinated.wrist.left, wristInput.left),
                        right = deriveClawTargetFromPixelHolder(handoffCoordinated.wrist.right, wristInput.right)
                ),
        )
        val coordinatedDepo = depoManager.fullyManageDepo(
                target = depoDriverInput,
                previousDepoTarget = previousTargetWorld.targetRobot.depoTarget,
                actualWorld = actualWorld
        )

        return HandoffTarget(
                coordinatedCollector,
                coordinatedDepo
        )
    }

    //Brody, while robot is backing up turn on extendo motors to hold position
    //drop down mapped to right joystick y. it auto goes up when intake is off or ejecting
}
