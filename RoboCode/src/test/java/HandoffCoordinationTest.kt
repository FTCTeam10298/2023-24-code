import org.junit.Assert
import org.junit.Test
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.CollectorManager
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.HandoffManager
import us.brainstormz.robotTwo.HandoffManager.*
import us.brainstormz.robotTwo.HandoffManager.HandoffCoordinated.*
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
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,
                ),
                actualState = HandoffCoordinated(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,

                        latches = Latches(
                                left = PixelHolder.Holding,
                                right = PixelHolder.Holding
                        ),
                        wrist = Wrist(
                                left = PixelHolder.Released,
                                right = PixelHolder.Released,
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
        )


        // when
        val actualOutput = testSubject.coordinateHandoff(
                inputConstraints = params.inputConstraints,
                actualState = params.actualState,
                transferSensorState = params.transferSensorState,
        )

        // then
        val expectedOutput = HandoffCoordinated(
                extendo = Slides.Retracted,
                latches = Latches(
                        PixelHolder.Holding,
                        PixelHolder.Holding
                ),
                depo = Slides.Retracted,
                wrist = Wrist(
                        PixelHolder.Holding,
                        PixelHolder.Holding,
                )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `handoff will finish when both slides are in and both controllers are holding pixel`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,
                ),
                actualState = HandoffCoordinated(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,

                        latches = Latches(
                                left = PixelHolder.Holding,
                                right = PixelHolder.Holding
                        ),
                        wrist = Wrist(
                                left = PixelHolder.Holding,
                                right = PixelHolder.Holding,
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
        )

        // when
        val actualOutput = testSubject.coordinateHandoff(
                params.inputConstraints,
                params.actualState,
                params.transferSensorState,
        )

        // then
        val expectedOutput = HandoffCoordinated(
                extendo = Slides.Retracted,
                latches = Latches(
                        PixelHolder.Released,
                        PixelHolder.Released
                ),
                depo = Slides.Retracted,
                wrist = Wrist(
                        PixelHolder.Holding,
                        PixelHolder.Holding,
                )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when both slides are in gripping claw will finish handoff, released claw will start handoff`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,
                ),

                actualState = HandoffCoordinated(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,

                        latches = Latches(
                                left = PixelHolder.Holding,
                                right = PixelHolder.Holding
                        ),
                        wrist = Wrist(
                                left = PixelHolder.Released,
                                right = PixelHolder.Holding,
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
        )

        // when
        val actualOutput = testSubject.coordinateHandoff(
                params.inputConstraints,
                params.actualState,
                params.transferSensorState
        )

        // then
        val expectedOutput = HandoffCoordinated(
                extendo = Slides.Retracted,
                latches = Latches(
                        left = PixelHolder.Holding,
                        right = PixelHolder.Released
                ),
                depo = Slides.Retracted,
                wrist = Wrist(
                        left = PixelHolder.Holding,
                        right = PixelHolder.Holding,
                )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    data class HandoffCoordinationParams(
            val inputConstraints: HandoffConstraints,
            val actualState: HandoffCoordinated,
            val transferSensorState: Transfer.TransferSensorState,
    )

    fun createHandoffManager(): HandoffManager {

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
        return handoffManager
    }
}
