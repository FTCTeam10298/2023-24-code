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
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
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
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(false)
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
    fun `handoff will not finish when it can finish but HandoffPixelsToLift is false`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(false)
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
    fun `handoff will finish when it can finish and HandoffPixelsToLift is true`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(true)
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
    fun `when both slides are in, gripping claw will stay at both, released claw will start handoff`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(false)
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
                        right = PixelHolder.Holding
                ),
                depo = Slides.Retracted,
                wrist = Wrist(
                        left = PixelHolder.Holding,
                        right = PixelHolder.Holding,
                )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by both slides and extendo wants to go out, lift will let go, extendo slide won't move`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Out,
                        depo = Slides.Retracted,
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(false)
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
                params.transferSensorState
        )

        // then
        val expectedOutput = HandoffCoordinated(
                extendo = Slides.Retracted,
                latches = Latches(
                        left = PixelHolder.Holding,
                        right = PixelHolder.Holding
                ),
                depo = Slides.Retracted,
                wrist = Wrist(
                        left = PixelHolder.Released,
                        right = PixelHolder.Released,
                )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by both slides and lift wants to go out without pixels, lift will let got but will wait to move`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Retracted,
                        depo = Slides.Out,
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(false)
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
                params.transferSensorState
        )

        // then
        val expectedOutput = HandoffCoordinated(
                extendo = Slides.Retracted,
                latches = Latches(
                        left = PixelHolder.Holding,
                        right = PixelHolder.Holding
                ),
                depo = Slides.Retracted,
                wrist = Wrist(
                        left = PixelHolder.Released,
                        right = PixelHolder.Released,
                )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by both slides and lift wants to go out and take pixels, extendo will let got, lift will wait to move`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Retracted,
                        depo = Slides.Out,
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(true)
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
                params.transferSensorState
        )

        // then
        val expectedOutput = HandoffCoordinated(
                extendo = Slides.Retracted,
                latches = Latches(
                        left = PixelHolder.Released,
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

    @Test
    fun `when pixel is controlled by both slides and extendo wants to go out but lift wants to take pixels, lift will let go, extendo will wait to move`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Out,
                        depo = Slides.Retracted,
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(true)
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
                params.transferSensorState
        )

        // then
        val expectedOutput = HandoffCoordinated(
                extendo = Slides.Retracted,
                latches = Latches(
                        left = PixelHolder.Holding,
                        right = PixelHolder.Holding
                ),
                depo = Slides.Retracted,
                wrist = Wrist(
                        left = PixelHolder.Released,
                        right = PixelHolder.Released,
                )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by extendo and extendo wants to go out, extendo will move`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Out,
                        depo = Slides.Retracted,
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(false)
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
                params.inputConstraints,
                params.actualState,
                params.transferSensorState
        )

        // then
        val expectedOutput = HandoffCoordinated(
                extendo = Slides.Out,
                latches = Latches(
                        left = PixelHolder.Holding,
                        right = PixelHolder.Holding
                ),
                depo = Slides.Retracted,
                wrist = Wrist(
                        left = PixelHolder.Released,
                        right = PixelHolder.Released,
                )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by lift and lift wants to go out with pixels, lift will move and take pixels`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Retracted,
                        depo = Slides.Out,
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(true)
                ),
                actualState = HandoffCoordinated(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,

                        latches = Latches(
                                left = PixelHolder.Released,
                                right = PixelHolder.Released
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
                params.transferSensorState
        )

        // then
        val expectedOutput = HandoffCoordinated(
                extendo = Slides.Retracted,
                latches = Latches(
                        left = PixelHolder.Released,
                        right = PixelHolder.Released
                ),
                depo = Slides.Out,
                wrist = Wrist(
                        left = PixelHolder.Holding,
                        right = PixelHolder.Holding,
                )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by lift and lift wants to go out without pixels, lift will not move and pixels will un-handoff`() {
        // given
        val testSubject = createHandoffManager()
        val params = HandoffCoordinationParams(
                inputConstraints = HandoffConstraints(
                        extendo = Slides.Retracted,
                        depo = Slides.Out,
                        handoffPixelsToLift = HandoffConstraints.HandoffPixelsToLift(false)
                ),
                actualState = HandoffCoordinated(
                        extendo = Slides.Retracted,
                        depo = Slides.Retracted,

                        latches = Latches(
                                left = PixelHolder.Released,
                                right = PixelHolder.Released
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
                params.transferSensorState
        )

        // then
        val expectedOutput = HandoffCoordinated(
                extendo = Slides.Retracted,
                latches = Latches(
                        left = PixelHolder.Holding,
                        right = PixelHolder.Holding
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
