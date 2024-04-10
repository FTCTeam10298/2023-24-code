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

    enum class Slides {
        Out,
        Retracted
    }
    data class HandoffConstraints(
            val extendo: Slides,
            val depo: Slides,
            val handoffPixelsToLift: HandoffPixelsToLift
    ) {
        data class HandoffPixelsToLift(
            override val left: Boolean,
            override val right: Boolean
        ): Side.ThingWithSides<Boolean> {
            constructor(both: Boolean): this(both, both)
        }
    }
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


    private fun checkIfActualRobotAllowsForHandoff(actualDepo: DepoManager.ActualDepo, actualCollector: CollectorManager.ActualCollector): Boolean {
        val liftIsDown = actualDepo.lift.limitSwitchIsActivated
        val extendoIsIn = actualCollector.extendo.limitSwitchIsActivated
        val armIsAtHandoffPosition = arm.checkIfArmIsAtTarget(Arm.Positions.In, actualDepo.armAngleDegrees)
        return liftIsDown && armIsAtHandoffPosition && extendoIsIn
    }

    private enum class PixelController {
        Depo,
        Collector,
        Both,
        NoPixel,
    }

    //  .wrist.getClawBySide(side).isClawAtAngle(Claw.ClawTarget.Gripping, actualWristAngles.getBySide(side))
    //transfer.checkIfLatchHasActuallyAchievedTarget(side, LatchPositions.Closed, previousTransferTarget)
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

        val liftIsDown = actualState.depo == Slides.Retracted
        val extendoIsIn = actualState.extendo == Slides.Retracted
        val actualRobotAllowsForHandoff = liftIsDown && extendoIsIn

        val liftInputIsDown = inputConstraints.depo == Slides.Retracted
        val extendoInputIsIn = inputConstraints.extendo == Slides.Retracted
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
                    extendo = Slides.Retracted,
                    depo = Slides.Retracted,

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

    private fun correctSlideTargetWithHandoff(originalSlideTarget: SlideSubsystem.SlideTargetPosition, handoffSlideTarget: Slides): SlideSubsystem.SlideTargetPosition {
        return if (handoffSlideTarget == Slides.Retracted) {
            SlideSubsystem.VariableTargetPosition(0)
        } else {
            originalSlideTarget
        }
    }
    private fun deriveLatchPositionFromPixelHolder(pixelHolder: HandoffCoordinated.PixelHolder): LatchPositions {
        return when(pixelHolder) {
            HandoffCoordinated.PixelHolder.Holding -> LatchPositions.Closed
            HandoffCoordinated.PixelHolder.Released -> LatchPositions.Open
        }
    }

    data class HandoffTarget(
            val collector: CollectorTarget,
            val depo: DepoTarget
    )
    fun manageHandoff(depoInput: RobotTwoTeleOp.DepoInput, collectorTarget: CollectorTarget, previousTargetWorld: TargetWorld, actualWorld: ActualWorld, previousActualWorld: ActualWorld): HandoffTarget {
        val handoffCoordinated = coordinateHandoff(
                inputConstraints = ,
                actualState = ,
                transferSensorState = collectorTarget.transferSensorState,
        )

        val coordinatedCollector = collectorManager.coordinateCollector(
                uncoordinatedTarget = collectorTarget.copy(
                        extendo = SlideSubsystem.TargetSlideSubsystem(
                                correctSlideTargetWithHandoff(
                                        collectorTarget.extendo.targetPosition,
                                        handoffCoordinated.extendo
                                )),
                        latches = TransferTarget(
                                leftLatchTarget = LatchTarget(deriveLatchPositionFromPixelHolder(handoffCoordinated.latches.left), 0),
                                rightLatchTarget =LatchTarget(deriveLatchPositionFromPixelHolder(handoffCoordinated.latches.right), 0)
                        ),
                ),
                previousTargetWorld = previousTargetWorld,
        )

        val depoDriverInput = RobotTwoTeleOp.noInput.copy(
                depo = when (handoffCoordinated.depo) {
                    Slides.Out -> {
                        if (depoInput == RobotTwoTeleOp.DepoInput.NoInput) {
                            RobotTwoTeleOp.DepoInput.Down
                        } else {
                            depoInput
                        }
                    }
                    Slides.Retracted -> RobotTwoTeleOp.DepoInput.Down
                },
                wrist = handoffCoordinated.wrist,
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



    //Latches need to close before claws release
    //Brody, while robot is backing up turn on extendo motors to hold position
    //drop down mapped to right joystick y. it auto goes up when intake is off or ejecting

//    fun getHandoffState(actualRobot: ActualRobot, previousTargetWorld: TargetWorld): SideIsActivelyHandingOff {
//        val liftIsAtAHeightWhereLatchesCouldConflict = !lift.isLiftAbovePosition(targetPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks, actualLiftPositionTicks = actualRobot.depoState.lift.currentPositionTicks)
//        val armIsInAPositionWhereLatchesCouldConflict = actualRobot.depoState.armAngleDegrees > Arm.Positions.InsideTheBatteryBox.angleDegrees
//
//        return if (liftIsAtAHeightWhereLatchesCouldConflict && armIsInAPositionWhereLatchesCouldConflict) {
//            fun checkIfIsActivelyHandingOffPerSide(side: Side): Boolean {
//                val claw = wrist.clawsAsMap[side]!!
//                val clawIsClosed = claw.isClawAtAngle(Claw.ClawTarget.Gripping, actualRobot.depoState.wristAngles.getBySide(side))
////                val extendoIsIn = extendo.isSlideSystemAllTheWayIn(actualRobot.collectorSystemState.extendo)
//
//                return clawIsClosed// && extendoIsIn
//            }
//
//            SideIsActivelyHandingOff(
//                    left = checkIfIsActivelyHandingOffPerSide(Side.Left),
//                    right = checkIfIsActivelyHandingOffPerSide(Side.Right)
//            )
//        } else {
//            SideIsActivelyHandingOff(
//                    left = false,
//                    right = false
//            )
//        }
//    }
//
//    fun checkIfHandoffIsReadyToStart(actualWorld: ActualWorld, previousActualWorld: ActualWorld): Boolean {
//        val isExtendoAllTheWayIn = extendo.isSlideSystemAllTheWayIn(actualWorld.actualRobot.collectorSystemState.extendo)
//        val extendoIsMovingInOrNotAtAll = extendo.getVelocityTicksPerMili(actualWorld, previousActualWorld) <= 0
//        val extendoIsReady = extendoIsMovingInOrNotAtAll && isExtendoAllTheWayIn
//
//        val liftExtensionIsAllTheWayDown = lift.isSlideSystemAllTheWayIn(actualWorld.actualRobot.depoState.lift)//10 >= actualWorld.actualRobot.depoState.lift.currentPositionTicks//actualWorld.actualRobot.depoState.isLiftLimitActivated
//        telemetry.addLine("isExtendoAllTheWayIn: $isExtendoAllTheWayIn")
//        telemetry.addLine("liftExtensionIsAllTheWayDown: $liftExtensionIsAllTheWayDown")
//
//        val isArmReadyToTransfer = arm.checkIfArmIsAtTarget(Arm.Positions.In, actualWorld.actualRobot.depoState.armAngleDegrees)
//        telemetry.addLine("isArmReadyToTransfer: $isArmReadyToTransfer")
//
//        val readyToHandoff = extendoIsReady && liftExtensionIsAllTheWayDown && isArmReadyToTransfer
//        return readyToHandoff
//    }
//
//    fun checkIfLatchHasSecuredPixelsFromClaw(side: Side, actualWorld: ActualWorld, previousTransferTarget: TransferTarget): Boolean {
//        val latchTarget = previousTransferTarget.getBySide(side)
//
//        val latchIsClosed = LatchPositions.Closed == latchTarget.target
//
//        val timeSinceLatchChangedTarget = actualWorld.timestampMilis - latchTarget.timeTargetChangedMillis
//        val timeLatchHasToBeClosedToSecurePixelMillis = 500
//        val latchTargetChangeWasLongEnoughAgo = timeSinceLatchChangedTarget >= timeLatchHasToBeClosedToSecurePixelMillis
//
//        return latchIsClosed && latchTargetChangeWasLongEnoughAgo
//    }
}
