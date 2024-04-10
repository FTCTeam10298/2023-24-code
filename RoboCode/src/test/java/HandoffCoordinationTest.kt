import org.junit.Assert
import org.junit.Test
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.CollectorManager
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.HandoffManager
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.ColorReading
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist


class HandoffCoordinatorTest {

    @Test
    fun `handoff will start when both slides are in`() {
        // given
        val (testSubject, transfer) = createHandoffManager()
        val params = HandoffCoordinationParams(
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
                        transferState = Transfer.ActualTransfer(
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
                transferSensorState = Transfer.TransferSensorState(
                        Transfer.SensorState(
                                hasPixelBeenSeen = true,
                                timeOfSeeingMilis = 0
                        ),
                        Transfer.SensorState(
                                hasPixelBeenSeen = true,
                                timeOfSeeingMilis = 0
                        ),
                ),
                previousTransferTarget = Transfer.TransferTarget(
                        Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed,
                                timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis * 2L
                        ),
                        Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed,
                                timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis * 2L
                        ),
                ),
        )


        // when
        val actualOutput = testSubject.coordinateHandoff(
                params.constrainingInputs,
                params.actualCollector,
                params.actualDepo,
                params.transferSensorState,
                params.previousTransferTarget
        )

        // then
        val expectedOutput = HandoffManager.HandoffCoordinatedOutput(
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
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `handoff will finish when both slides are in and both controllers are holding pixel`() {
        // given
        val (testSubject, transfer) = createHandoffManager()
        val params = HandoffCoordinationParams(
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
                        transferState = Transfer.ActualTransfer(
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
                                leftClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees,
                                rightClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees,
                        )
                ),
                transferSensorState = Transfer.TransferSensorState(
                        Transfer.SensorState(
                                hasPixelBeenSeen = true,
                                timeOfSeeingMilis = 0
                        ),
                        Transfer.SensorState(
                                hasPixelBeenSeen = true,
                                timeOfSeeingMilis = 0
                        ),
                ),
                previousTransferTarget = Transfer.TransferTarget(
                        Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed,
                                timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis * 2L
                        ),
                        Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed,
                                timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis * 2L
                        ),
                ),
        )

        // when
        val actualOutput = testSubject.coordinateHandoff(
                params.constrainingInputs,
                params.actualCollector,
                params.actualDepo,
                params.transferSensorState,
                params.previousTransferTarget
        )

        // then
        val expectedOutput = HandoffManager.HandoffCoordinatedOutput(
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
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when both slides are in gripping claw will finish handoff, released claw will start handoff`() {
        // given
        val (testSubject, transfer) = createHandoffManager()
        val params = HandoffCoordinationParams(
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
                        transferState = Transfer.ActualTransfer(
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
                                leftClawAngleDegrees = Claw.ClawTarget.Retracted.angleDegrees,
                                rightClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees,
                        )
                ),
                transferSensorState = Transfer.TransferSensorState(
                        Transfer.SensorState(
                                hasPixelBeenSeen = true,
                                timeOfSeeingMilis = 0
                        ),
                        Transfer.SensorState(
                                hasPixelBeenSeen = true,
                                timeOfSeeingMilis = 0
                        ),
                ),
                previousTransferTarget = Transfer.TransferTarget(
                        leftLatchTarget = Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed,
                                timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis * 2L
                        ),
                        rightLatchTarget = Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed,
                                timeTargetChangedMillis = transfer.timeSinceTargetChangeToAchieveTargetMillis * 2L
                        ),
                ),
        )

        // when
        val actualOutput = testSubject.coordinateHandoff(
                params.constrainingInputs,
                params.actualCollector,
                params.actualDepo,
                params.transferSensorState,
                params.previousTransferTarget
        )

        // then
        val expectedOutput = HandoffManager.HandoffCoordinatedOutput(
                extendo = HandoffManager.Slides.Retracted,
                latches = HandoffManager.HandoffCoordinatedOutput.Latches(
                        left = HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding,
                        right = HandoffManager.HandoffCoordinatedOutput.PixelHolder.Released
                ),
                depo = HandoffManager.Slides.Retracted,
                wrist = HandoffManager.HandoffCoordinatedOutput.Wrist(
                        left = HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding,
                        right = HandoffManager.HandoffCoordinatedOutput.PixelHolder.Holding,
                )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    data class HandoffCoordinationParams(
//        val expectedOutput: HandoffManager.HandoffCoordinatedOutput,
            val constrainingInputs: HandoffManager.HandoffConstrainingInputs,
            val actualCollector: CollectorManager.ActualCollector,
            val actualDepo: DepoManager.ActualDepo,
            val transferSensorState: Transfer.TransferSensorState,
            val previousTransferTarget: Transfer.TransferTarget,
    )

    fun createHandoffManager(): Pair<HandoffManager, Transfer> {

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
        return handoffManager to transfer
    }
}
