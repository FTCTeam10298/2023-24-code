package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Transfer.*
import us.brainstormz.robotTwo.subsystems.Wrist

class HandoffManager(
        private val collectorManager: CollectorManager,
        private val depoManager: DepoManager,
        private val wrist: Wrist,
        private val arm: Arm,
        private val transfer: Transfer,
        private val telemetry: Telemetry) {



    data class HandoffPixelsToLift(
            override val left: Boolean,
            override val right: Boolean
    ): Side.ThingWithSides<Boolean> {
        constructor(both: Boolean): this(both, both)
    }


    enum class Slides {
        NotReady,
        ReadyToHandoff
    }
    data class HandoffConstraints(
            val extendo: Slides,
            val depo: Slides,
            val handoffPixelsToLift: HandoffPixelsToLift
    )
    data class HandoffCoordinated(
            val extendo: Slides,
            val latches: Latches,

            val depo: Slides,
            val wrist: Wrist,
    ) {
        enum class PixelHolder {
            Holding,
            Released
        }

        data class Latches(
                override val left: PixelHolder,
                override val right: PixelHolder
        ): Side.ThingWithSides<PixelHolder>

        data class Wrist(
                override val left: PixelHolder,
                override val right: PixelHolder
        ): Side.ThingWithSides<PixelHolder>
    }

    private enum class PixelController {
        Depo,
        Collector,
        Both,
        NoPixel,
    }

    private fun determinePixelControllerForSinglePixel(side: Side, actualState: HandoffCoordinated, transferSensorState: TransferSensorState): PixelController {
        val pixelIsDetected = transferSensorState.getBySide(side).hasPixelBeenSeen

        return if (pixelIsDetected) {
            val clawIsGripping = HandoffCoordinated.PixelHolder.Holding == actualState.wrist.getBySide(side)
            val latchIsClosed = HandoffCoordinated.PixelHolder.Holding == actualState.latches.getBySide(side)

            when {
                clawIsGripping && latchIsClosed -> PixelController.Both
                clawIsGripping -> PixelController.Depo
                else -> PixelController.Collector
            }
        } else {
            PixelController.NoPixel
        }
    }

    private data class OneSideCoordinatedExtremeties(
            val latch: HandoffCoordinated.PixelHolder,
            val claw: HandoffCoordinated.PixelHolder
    )

    fun coordinateHandoff(inputConstraints: HandoffConstraints, actualState: HandoffCoordinated, transferSensorState: TransferSensorState): HandoffCoordinated {

        val liftIsDown = actualState.depo == Slides.ReadyToHandoff
        val extendoIsIn = actualState.extendo == Slides.ReadyToHandoff
        val actualRobotAllowsForHandoff = liftIsDown && extendoIsIn

        val liftInputIsDown = inputConstraints.depo == Slides.ReadyToHandoff
        val extendoInputIsIn = inputConstraints.extendo == Slides.ReadyToHandoff
        val inputAllowsForHandoff = liftInputIsDown && extendoInputIsIn

        val handoffAllowedToStart = inputAllowsForHandoff && actualRobotAllowsForHandoff

        val finalPixelController = { side: Side ->
            val liftWantsThePixel = inputConstraints.handoffPixelsToLift.getBySide(side) && extendoInputIsIn
            when {
                liftWantsThePixel -> PixelController.Depo
                handoffAllowedToStart -> PixelController.Both
                else -> PixelController.Collector
            }
        }

        val actualController = { side: Side ->
            determinePixelControllerForSinglePixel(side, actualState, transferSensorState)
        }


        val bothActualControllersAreAtFinal = Side.entries.fold(true) { acc, side ->
            val controller = actualController(side)

            val noPixelToControl = PixelController.NoPixel == controller
            val controllerIsAtFinal = finalPixelController(side) == controller
            acc && (controllerIsAtFinal || noPixelToControl)
        }


        fun determineOutputFromController(targetPixelController: PixelController): OneSideCoordinatedExtremeties = when (targetPixelController) {
            PixelController.Depo -> OneSideCoordinatedExtremeties(
                    latch = HandoffCoordinated.PixelHolder.Released,
                    claw = HandoffCoordinated.PixelHolder.Holding,
            )
            PixelController.Both -> OneSideCoordinatedExtremeties(
                    latch = HandoffCoordinated.PixelHolder.Holding,
                    claw = HandoffCoordinated.PixelHolder.Holding
            )
            else -> OneSideCoordinatedExtremeties(
                    latch = HandoffCoordinated.PixelHolder.Holding,
                    claw = HandoffCoordinated.PixelHolder.Released,
            )
        }


        return if (bothActualControllersAreAtFinal) {
            val outputs = {side: Side ->
                val controller = actualController(side)
                determineOutputFromController(controller)
            }
            val left = outputs(Side.Left)
            val right = outputs(Side.Right)

            HandoffCoordinated(
                    extendo = inputConstraints.extendo,
                    depo = inputConstraints.depo,

                    latches = HandoffCoordinated.Latches(
                            left = left.latch,
                            right = right.latch
                    ),
                    wrist = HandoffCoordinated.Wrist(
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
                determineOutputFromController(
                        targetPixelController = resolveControllerDifference(
                                finalController = finalPixelController(side),
                                actualController = actualController(side)
                        )
                )
            }

            val left = outputs(Side.Left)
            val right = outputs(Side.Right)

            //Extensions must be in
            HandoffCoordinated(
                    extendo = Slides.ReadyToHandoff,
                    depo = Slides.ReadyToHandoff,

                    latches = HandoffCoordinated.Latches(
                            left = left.latch,
                            right = right.latch
                    ),
                    wrist = HandoffCoordinated.Wrist(
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
    private fun deriveClawTargetFromPixelHolder(pixelHolder: HandoffCoordinated.PixelHolder): RobotTwoTeleOp.ClawInput {
        return when(pixelHolder) {
            HandoffCoordinated.PixelHolder.Holding -> RobotTwoTeleOp.ClawInput.Hold
            HandoffCoordinated.PixelHolder.Released -> RobotTwoTeleOp.ClawInput.Drop
        }
    }

    data class HandoffTarget(
            val collector: CollectorTarget,
            val depo: DepoTarget
    )
    fun manageHandoff(handoff: HandoffPixelsToLift, depoInput: RobotTwoTeleOp.DepoInput, collectorTarget: CollectorTarget, previousTargetWorld: TargetWorld, actualWorld: ActualWorld, previousActualWorld: ActualWorld): HandoffTarget {

        fun getPixelHolderFromIsHolding(isHolding: Boolean): HandoffCoordinated.PixelHolder {
            return if (isHolding) {
                HandoffCoordinated.PixelHolder.Holding
            } else {
                HandoffCoordinated.PixelHolder.Released
            }
        }

        fun getLatchesFromRule(checkIfIsHolding: (side:Side)->Boolean): HandoffCoordinated.Latches {
            return HandoffCoordinated.Latches(
                    left = getPixelHolderFromIsHolding(checkIfIsHolding(Side.Left)),
                    right = getPixelHolderFromIsHolding(checkIfIsHolding(Side.Right))
            )
        }
        fun getWristFromRule(checkIfIsHolding: (side:Side)->Boolean): HandoffCoordinated.Wrist {
            return HandoffCoordinated.Wrist(
                    left = getPixelHolderFromIsHolding(checkIfIsHolding(Side.Left)),
                    right = getPixelHolderFromIsHolding(checkIfIsHolding(Side.Right))
            )
        }
        val handoffCoordinated = coordinateHandoff(
                inputConstraints = HandoffConstraints(
                        extendo = when (collectorTarget.extendo.targetPosition) {
                            Extendo.ExtendoPositions.Min -> Slides.ReadyToHandoff
                            else -> Slides.NotReady
                        },
                        depo = when (depoInput) {
                            RobotTwoTeleOp.DepoInput.Manual -> Slides.ReadyToHandoff
                            RobotTwoTeleOp.DepoInput.NoInput -> Slides.ReadyToHandoff
                            RobotTwoTeleOp.DepoInput.Down -> Slides.ReadyToHandoff
                            else -> Slides.NotReady
                        },
                        handoffPixelsToLift = handoff,
                ),

                actualState = HandoffCoordinated(
                        extendo =  when {
                            actualWorld.actualRobot.collectorSystemState.extendo.limitSwitchIsActivated -> Slides.ReadyToHandoff
                            else -> Slides.NotReady
                        },
                        latches = getLatchesFromRule {side ->
                            transfer.checkIfLatchHasActuallyAchievedTarget(side, LatchPositions.Closed, previousTargetWorld.targetRobot.collectorTarget.latches)
                        },
                        depo = when {
                            actualWorld.actualRobot.depoState.lift.limitSwitchIsActivated && arm.checkIfArmIsAtTarget(Arm.Positions.In, actualWorld.actualRobot.depoState.armAngleDegrees)-> Slides.ReadyToHandoff
                            else -> Slides.NotReady
                        },
                        wrist = getWristFromRule {side ->
                            wrist.getClawBySide(side).isClawAtAngle(Claw.ClawTarget.Gripping, actualWorld.actualRobot.depoState.wristAngles.getBySide(side))
                        }
                ),
                transferSensorState = collectorTarget.transferSensorState,
        )

        val coordinatedCollector = collectorManager.coordinateCollector(
                uncoordinatedTarget = collectorTarget.copy(
                        extendo = SlideSubsystem.TargetSlideSubsystem(
                                targetPosition = if (handoffCoordinated.extendo == Slides.ReadyToHandoff) {
                                    Extendo.ExtendoPositions.Min
                                } else {
                                    collectorTarget.extendo.targetPosition
                                }
                        ),
                        latches = TransferTarget(
                                leftLatchTarget = LatchTarget(deriveLatchPositionFromPixelHolder(handoffCoordinated.latches.left), 0),
                                rightLatchTarget =LatchTarget(deriveLatchPositionFromPixelHolder(handoffCoordinated.latches.right), 0)
                        ),
                ),
                previousTargetWorld = previousTargetWorld,
        )

        val depoDriverInput = RobotTwoTeleOp.noInput.copy(
                depo = when (handoffCoordinated.depo) {
                    Slides.NotReady -> {
                        when (depoInput) {
                            RobotTwoTeleOp.DepoInput.Manual -> RobotTwoTeleOp.DepoInput.Down
                            RobotTwoTeleOp.DepoInput.NoInput -> RobotTwoTeleOp.DepoInput.Down
                            else -> depoInput
                        }
                    }
                    Slides.ReadyToHandoff -> RobotTwoTeleOp.DepoInput.Down
                },
                wrist = RobotTwoTeleOp.WristInput(
                        left = deriveClawTargetFromPixelHolder(handoffCoordinated.wrist.left),
                        right = deriveClawTargetFromPixelHolder(handoffCoordinated.wrist.right)
                ),
        )
        val coordinatedDepo = depoManager.fullyManageDepo(
                target = depoDriverInput,
                previousDepoTarget = previousTargetWorld.targetRobot.depoTarget,
                actualWorld = actualWorld,
                previousActualWorld = previousActualWorld
        )

        return HandoffTarget(
                coordinatedCollector,
                coordinatedDepo
        )
    }

    //Brody, while robot is backing up turn on extendo motors to hold position
    //drop down mapped to right joystick y. it auto goes up when intake is off or ejecting
}
