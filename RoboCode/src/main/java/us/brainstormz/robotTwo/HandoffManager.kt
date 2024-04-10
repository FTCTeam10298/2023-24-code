package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.ColorReading
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Transfer.*
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.SlideSubsystem

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
    data class HandoffConstrainingInputs(
            val extendo: Slides,
            val depo: Slides
    )
    data class HandoffCoordinatedOutput(
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

    private fun checkIfInputAllowsForHandoff(handoffInput: HandoffConstrainingInputs): Boolean {
        val liftIsDown = handoffInput.depo == Slides.Retracted
        val extendoIsIn = handoffInput.extendo == Slides.Retracted
        return liftIsDown && extendoIsIn
    }

    private enum class PixelController {
        Depo,
        Collector,
        Both,
        NoPixel,
    }
    private fun determinePixelControllerForSinglePixel(side: Side, transferSensorState: TransferSensorState, actualWristAngles: Wrist.ActualWrist, previousTransferTarget: TransferTarget): PixelController {
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

    fun coordinateHandoff(handoffInput: HandoffConstrainingInputs, actualCollector: CollectorManager.ActualCollector, actualDepo: DepoManager.ActualDepo, transferSensorState: TransferSensorState, previousTransferTarget: TransferTarget, ): HandoffCoordinatedOutput {

        val actualRobotAllowsForHandoff = checkIfActualRobotAllowsForHandoff(actualDepo, actualCollector)
        val inputAllowsForHandoff = checkIfInputAllowsForHandoff(handoffInput)
        val startHandoff = inputAllowsForHandoff && actualRobotAllowsForHandoff

        val finalPixelController = { side: Side ->
            if (startHandoff) {
                PixelController.Depo
            } else {
                PixelController.Collector
            }
        }

        val actualController = { side: Side ->
            determinePixelControllerForSinglePixel(side, transferSensorState, actualDepo.wristAngles, previousTransferTarget)
        }

        val bothActualControllersAreAtFinal = Side.entries.fold(true) { acc, side ->
            val controller = actualController(side)

            val noPixelToControl = PixelController.NoPixel == controller
            val controllerIsAtFinal = finalPixelController(side) == controller
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
                                finalController = finalPixelController(side),
                                actualController = actualController(side)
                        )
                )
            }

            val left = outputs(Side.Left)
            val right = outputs(Side.Right)

            //Extensions must be in
            HandoffCoordinatedOutput(
                    extendo = Slides.Retracted,
                    depo = Slides.Retracted,

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

//    data class HandoffTarget(
//            val collector: CollectorTarget,
//            val depo: DepoTarget
//    )
//    fun manageHandoff(): HandoffTarget {
//        val coordinatedForHandoff = coordinateHandoff()
//
//        val coordinatedCollector = collectorManager.coordinateCollector()
//        val coordinatedDepo = depoManager.fullyManageDepo()
//
//        return HandoffTarget(
//                coordinatedCollector,
//                coordinatedDepo
//        )
//    }



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


fun main() {
    val telemetry = PrintlnTelemetry()
    val transfer = Transfer(telemetry)
    val arm = Arm()
    val wrist = Wrist(Claw(telemetry), Claw(telemetry), telemetry)
    val handoffManager = HandoffManager(
            collectorManager = CollectorManager(
                    intake = Intake(),
                    transfer = transfer,
                    extendo = Extendo(telemetry),
                    telemetry = telemetry
            ),
            depoManager = DepoManager(
                    arm = arm,
                    lift = Lift(telemetry),
                    wrist = wrist,
                    telemetry = telemetry
            ),
            wrist = wrist,
            arm = arm,
            transfer = transfer,
            telemetry = telemetry
    )

    data class HandoffCoordinationTest(
            val testHypothesis: String,
            val expectedOutput: HandoffManager.HandoffCoordinatedOutput,
            val constrainingInputs: HandoffManager.HandoffConstrainingInputs,
            val actualCollector: CollectorManager.ActualCollector,
            val actualDepo: DepoManager.ActualDepo,
            val transferSensorState: TransferSensorState,
            val previousTransferTarget: TransferTarget,
            val actualOutput: HandoffManager.HandoffCoordinatedOutput? = null
    ) {
        val testPassed = expectedOutput == actualOutput
    }
    val tests = listOf(
            HandoffCoordinationTest(
                    testHypothesis = "handoff will start when both slides are in",
                    expectedOutput = HandoffManager.HandoffCoordinatedOutput(
                            extendo = HandoffManager.Slides.Retracted,
                            latches = HandoffManager.HandoffCoordinatedOutput.Latches(
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding,
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding
                            ),
                            depo = HandoffManager.Slides.Retracted,
                            wrist = HandoffManager.HandoffCoordinatedOutput.Wrist(
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding,
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding,
                            )
                    ),
                    constrainingInputs = HandoffManager.HandoffConstrainingInputs(
                            extendo = HandoffManager.Slides.Retracted,
                            depo = HandoffManager.Slides.Retracted,
                    ),
                    actualCollector = CollectorManager.ActualCollector(
                        extendo = SlideSubsystem.ActualSlideSubsystem(
                                0,
                                true,
                                0,
                                0,
                                0.0
                        ),
                        transferState = ActualTransfer(
                                ColorReading(1f, 1f, 1f, 1f),
                                ColorReading(1f, 1f, 1f, 1f),
                        ),
                    ),
                    actualDepo = DepoManager.ActualDepo(
                        armAngleDegrees = Arm.Positions.In.angleDegrees,
                            lift = SlideSubsystem.ActualSlideSubsystem(
                                    0,
                                    true,
                                    0,
                                    0,
                                    0.0
                            ),
                            wristAngles = Wrist.ActualWrist(
                                    Claw.ClawTarget.Retracted.angleDegrees,
                                    Claw.ClawTarget.Retracted.angleDegrees,
                            )
                    ),
                    transferSensorState = TransferSensorState(
                            SensorState(
                                    hasPixelBeenSeen = true,
                                    timeOfSeeingMilis = 0
                            ),
                            SensorState(
                                    hasPixelBeenSeen = true,
                                    timeOfSeeingMilis = 0
                            ),
                    ),
                    previousTransferTarget = TransferTarget(
                            LatchTarget(
                                    target = LatchPositions.Closed,
                                    timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis*2L
                            ),
                            LatchTarget(
                                    target = LatchPositions.Closed,
                                    timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis*2L
                            ),
                    ),
            ),
            HandoffCoordinationTest(
                    testHypothesis = "handoff will finish when both slides are in and both controllers are holding pixel",
                    expectedOutput = HandoffManager.HandoffCoordinatedOutput(
                            extendo = HandoffManager.Slides.Retracted,
                            latches = HandoffManager.HandoffCoordinatedOutput.Latches(
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Released,
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Released
                            ),
                            depo = HandoffManager.Slides.Retracted,
                            wrist = HandoffManager.HandoffCoordinatedOutput.Wrist(
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding,
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding,
                            )
                    ),
                    constrainingInputs = HandoffManager.HandoffConstrainingInputs(
                            extendo = HandoffManager.Slides.Retracted,
                            depo = HandoffManager.Slides.Retracted,
                    ),
                    actualCollector = CollectorManager.ActualCollector(
                            extendo = SlideSubsystem.ActualSlideSubsystem(
                                    0,
                                    true,
                                    0,
                                    0,
                                    0.0
                            ),
                            transferState = ActualTransfer(
                                    ColorReading(1f, 1f, 1f, 1f),
                                    ColorReading(1f, 1f, 1f, 1f),
                            ),
                    ),
                    actualDepo = DepoManager.ActualDepo(
                            armAngleDegrees = Arm.Positions.In.angleDegrees,
                            lift = SlideSubsystem.ActualSlideSubsystem(
                                    0,
                                    true,
                                    0,
                                    0,
                                    0.0
                            ),
                            wristAngles = Wrist.ActualWrist(
                                    Claw.ClawTarget.Gripping.angleDegrees,
                                    Claw.ClawTarget.Gripping.angleDegrees,
                            )
                    ),
                    transferSensorState = TransferSensorState(
                            SensorState(
                                    hasPixelBeenSeen = true,
                                    timeOfSeeingMilis = 0
                            ),
                            SensorState(
                                    hasPixelBeenSeen = true,
                                    timeOfSeeingMilis = 0
                            ),
                    ),
                    previousTransferTarget = TransferTarget(
                            LatchTarget(
                                    target = LatchPositions.Closed,
                                    timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis*2L
                            ),
                            LatchTarget(
                                    target = LatchPositions.Closed,
                                    timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis*2L
                            ),
                    ),
            ),
            HandoffCoordinationTest(
                    testHypothesis = "when both slides are in gripping claw will finish handoff, released claw will start handoff",
                    expectedOutput = HandoffManager.HandoffCoordinatedOutput(
                            extendo = HandoffManager.Slides.Retracted,
                            latches = HandoffManager.HandoffCoordinatedOutput.Latches(
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Released,
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding
                            ),
                            depo = HandoffManager.Slides.Retracted,
                            wrist = HandoffManager.HandoffCoordinatedOutput.Wrist(
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding,
                                    HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding,
                            )
                    ),
                    constrainingInputs = HandoffManager.HandoffConstrainingInputs(
                            extendo = HandoffManager.Slides.Retracted,
                            depo = HandoffManager.Slides.Retracted,
                    ),
                    actualCollector = CollectorManager.ActualCollector(
                            extendo = SlideSubsystem.ActualSlideSubsystem(
                                    0,
                                    true,
                                    0,
                                    0,
                                    0.0
                            ),
                            transferState = ActualTransfer(
                                    ColorReading(1f, 1f, 1f, 1f),
                                    ColorReading(1f, 1f, 1f, 1f),
                            ),
                    ),
                    actualDepo = DepoManager.ActualDepo(
                            armAngleDegrees = Arm.Positions.In.angleDegrees,
                            lift = SlideSubsystem.ActualSlideSubsystem(
                                    0,
                                    true,
                                    0,
                                    0,
                                    0.0
                            ),
                            wristAngles = Wrist.ActualWrist(
                                    Claw.ClawTarget.Retracted.angleDegrees,
                                    Claw.ClawTarget.Gripping.angleDegrees,
                            )
                    ),
                    transferSensorState = TransferSensorState(
                            SensorState(
                                    hasPixelBeenSeen = true,
                                    timeOfSeeingMilis = 0
                            ),
                            SensorState(
                                    hasPixelBeenSeen = true,
                                    timeOfSeeingMilis = 0
                            ),
                    ),
                    previousTransferTarget = TransferTarget(
                            LatchTarget(
                                    target = LatchPositions.Closed,
                                    timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis*2L
                            ),
                            LatchTarget(
                                    target = LatchPositions.Closed,
                                    timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis*2L
                            ),
                    ),
            )
    )

    val finishedTests = tests.map { test ->
        test.copy (
                actualOutput = handoffManager.coordinateHandoff(
                        test.constrainingInputs,
                        test.actualCollector,
                        test.actualDepo,
                        test.transferSensorState,
                        test.previousTransferTarget
                )
        )
    }

    finishedTests.forEach {test ->

        val testAsString = test.toString()
        println(testAsString.replace(", actualOutput", ", \n    actualOutput").replace(", expectedOutput", ", \n    expectedOutput"))
        println("testPassed: ${test.testPassed}\n\n")
    }
}