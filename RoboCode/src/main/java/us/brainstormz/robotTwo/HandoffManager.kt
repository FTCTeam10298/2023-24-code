package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Transfer.*
import us.brainstormz.robotTwo.subsystems.Wrist

class HandoffManager(
        private val collectorManager: CollectorManager,
        private val depoManager: DepoManager,
        private val wrist: Wrist,
        private val lift: Lift,
        private val extendo: Extendo,
        private val arm: Arm,
        private val transfer: Transfer,
        private val telemetry: Telemetry) {

    enum class Extendo {
        Out,
        Retracted
    }
    enum class Depo {
        TakePixelsUp,
        NoPixelsUp,
        Retracted
    }
    data class HandoffConstrainingInputs(
            val extendo: Extendo,
            val depo: Depo
    )
    data class HandoffCoordinatedOutput(
            val extendo: Extendo,
            val latches: Latches,

            val depo: Depo,
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


    private fun checkIfActualRobotAllowsForHandoff(actualRobot: ActualRobot): Boolean {
        val liftIsDown = actualRobot.depoState.lift.limitSwitchIsActivated
        val extendoIsIn = actualRobot.collectorSystemState.extendo.limitSwitchIsActivated
        val armIsAtHandoffPosition = arm.checkIfArmIsAtTarget(Arm.Positions.In, actualRobot.depoState.armAngleDegrees)
        return liftIsDown && armIsAtHandoffPosition && extendoIsIn
    }

    private fun checkIfInputAllowsForHandoff(handoffInput: HandoffConstrainingInputs): Boolean {
        val liftIsDown = handoffInput.depo == Depo.Retracted
        val extendoIsIn = handoffInput.extendo == Extendo.Retracted
        return liftIsDown && extendoIsIn
    }

    private enum class PixelController {
        Depo,
        Collector,
        Both,
        NoPixel,
    }
    private fun determinePixelControllerForSinglePixel(side: Side, transferSensorState: TransferState, actualWristAngles: Wrist.ActualWrist, previousTransferTarget: TransferTarget): PixelController {
        val pixelIsDetected = transferSensorState.getBySide(side).hasPixelBeenSeen

        return if (pixelIsDetected) {
            val clawIsGripping = wrist.getClawBySide(side).isClawAtAngle(Claw.ClawTarget.Gripping, actualWristAngles.getBySide(side))
            val latchIsClosed = transfer.checkIfLatchHasActuallyAchievedTarget(side, LatchPositions.Closed, previousTransferTarget)

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
            val latch: HandoffCoordinatedOutput.PixelHolder,
            val claw: HandoffCoordinatedOutput.PixelHolder
    )

    fun coordinateHandoff(handoffInput: HandoffConstrainingInputs, actualRobot: ActualRobot, transferSensorState: TransferState, previousTransferTarget: TransferTarget, ): HandoffCoordinatedOutput {

        val actualRobotAllowsForHandoff = checkIfActualRobotAllowsForHandoff(actualRobot)
        val inputAllowsForHandoff = checkIfInputAllowsForHandoff(handoffInput)
        val liftWantsToGoUpWithoutPixels = handoffInput.depo == Depo.NoPixelsUp
        val startHandoff = inputAllowsForHandoff && actualRobotAllowsForHandoff && !liftWantsToGoUpWithoutPixels

        val finalPixelController = if (startHandoff) {
            PixelController.Depo
        } else {
            PixelController.Collector
        }

        val actualController = { side: Side ->
            determinePixelControllerForSinglePixel(side, transferSensorState, actualRobot.depoState.wristAngles, previousTransferTarget)
        }

        val bothActualControllersAreAtFinal = Side.entries.fold(true) { acc, side ->
            val controller = actualController(side)

            val noPixelToControl = PixelController.NoPixel == controller
            val controllerIsAtFinal = finalPixelController == controller
            acc && (controllerIsAtFinal || noPixelToControl)
        }


        fun determineOutputFromController(targetPixelController: PixelController): OneSideCoordinatedExtremeties = when (targetPixelController) {
            PixelController.Depo -> OneSideCoordinatedExtremeties(
                    latch = HandoffCoordinatedOutput.PixelHolder.Released,
                    claw = HandoffCoordinatedOutput.PixelHolder.Holding,
            )
            PixelController.Both -> OneSideCoordinatedExtremeties(
                    latch = HandoffCoordinatedOutput.PixelHolder.Holding,
                    claw = HandoffCoordinatedOutput.PixelHolder.Holding
            )
            else -> OneSideCoordinatedExtremeties(
                    latch = HandoffCoordinatedOutput.PixelHolder.Holding,
                    claw = HandoffCoordinatedOutput.PixelHolder.Released,
            )
        }


        return if (bothActualControllersAreAtFinal) {
            val outputs = {side: Side ->
                val controller = actualController(side)
                determineOutputFromController(controller)
            }
            val left = outputs(Side.Left)
            val right = outputs(Side.Right)

            HandoffCoordinatedOutput(
                    extendo = handoffInput.extendo,
                    depo = handoffInput.depo,

                    latches = HandoffCoordinatedOutput.Latches(
                            left = left.latch,
                            right = right.latch
                    ),
                    wrist = HandoffCoordinatedOutput.Wrist(
                        left = left.claw,
                        right = right.claw
                    )
            )
        } else {
            fun resolveControllerDifference(finalController: PixelController, actualController: PixelController): PixelController {
                val controlByTarget = if (finalController == PixelController.Depo) {
                    PixelController.Depo
                } else {
                    PixelController.Collector
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
                                finalController = finalPixelController,
                                actualController = actualController(side)
                        )
                )
            }

            val left = outputs(Side.Left)
            val right = outputs(Side.Right)

            //Extensions must be in
            HandoffCoordinatedOutput(
                    extendo = Extendo.Retracted,
                    depo = Depo.Retracted,

                    latches = HandoffCoordinatedOutput.Latches(
                            left = left.latch,
                            right = right.latch
                    ),
                    wrist = HandoffCoordinatedOutput.Wrist(
                            left = left.claw,
                            right = right.claw
                    )
            )
        }
    }

    data class HandoffTarget(
            val collector: CollectorTarget,
            val depo: DepoTarget
    )
    fun manageHandoff(): HandoffTarget {
        val coordinatedForHandoff = coordinateHandoff()

        val coordinatedCollector = collectorManager.coordinateCollector()
        val coordinatedDepo = depoManager.fullyManageDepo()

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