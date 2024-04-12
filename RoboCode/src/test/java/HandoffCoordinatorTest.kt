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


fun createHandoffManager(): HandoffManager {

    val telemetry = PrintlnTelemetry()
    val transfer = Transfer(telemetry)
    val arm = Arm()
    val lift = Lift(telemetry)
    val wrist = Wrist(Claw(telemetry), Claw(telemetry), telemetry)
    return HandoffManager(
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
        lift = lift,
        transfer = transfer,
        telemetry = telemetry
    )
}
class HandoffCoordinatorTest {

    @Test
    fun `handoff will start when both slides are in`() {
        // given

        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(HandoffReadiness.StartHandoff)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            depo = DepoCoordinationStates.ReadyToHandoff,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when
        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            latches = SidedPixelHolders(
                PixelHolder.Holding,
                PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                PixelHolder.Holding,
                PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `handoff will not finish when it can finish but HandoffPixelsToLift is false`() {
        // given
        val handoffPixelsToLift = HandoffReadiness.DontHandoff // false??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when
        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            latches = SidedPixelHolders(
                PixelHolder.Holding,
                PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                PixelHolder.Released,
                PixelHolder.Released,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `handoff will finish when it can finish and HandoffPixelsToLift is true`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.FinishHandoff // true??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        val testSubject = createHandoffManager()

        // when
        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            latches = SidedPixelHolders(
                PixelHolder.Released,
                PixelHolder.Released
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                PixelHolder.Holding,
                PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when both slides are in, gripping claw will stay at both, released claw will start handoff`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.FinishHandoff // true??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        val testSubject = createHandoffManager()

        // when
        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Released
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by both slides and extendo wants to go out, lift will let go, extendo slide won't move`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.FinishHandoff // true??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )

        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when
        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by both slides and extendo is a little out, claws will release and extendo will retract`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.DontHandoff // false??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.DriverControlledPosition,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        val testSubject = createHandoffManager()

        // when
        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by both slides and trying to transfer and extendo is a little out, claws will release and extendo will retract`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.StartHandoff // true??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???


        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.DriverControlledPosition,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )
        val testSubject = createHandoffManager()

        // when
        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by depo and depo is down, and extendo wants to go out, extendo will grab pixels, extendo slide won't move`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.DontHandoff // false??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when
        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )


        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by depo and depo is up, and extendo wants to go out, extendo slide will move`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.DontHandoff // true??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = false // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.DriverControlledPosition,// .NotReady,
            depo = DepoCoordinationStates.NotReady,

            latches = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
        )

        // when
        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.DriverControlledPosition, // .NotReady,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by depo and depo is off the limit and pixels are past the color sensor but still partially in the transfer, and extendo wants to go out, extendo slide won't move`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.StartHandoff // true??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.NotReady,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.DriverControlledPosition, //.NotReady,
            depo = DepoCoordinationStates.PotentiallyBlockingExtendoMovement,

            latches = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
        )

        // when
        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            latches = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released
            ),
            depo = DepoCoordinationStates.NotReady,
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by both slides and lift wants to go out without pixels, lift will let got but will wait to move`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.DontHandoff // false??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.NotReady,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition , //.ReadyToHandoff,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when depo is scoring pixels handoff doesn't do anything`() {
        // given


        val handoffPixelsToLift = HandoffReadiness.DontHandoff // false??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???


        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.NotReady,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            depo = DepoCoordinationStates.NotReady,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.NotReady,
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }



    @Test
    fun `when depo has pixels, it's up a little, and extendo is in, collector stays open and lift keeps doing it's thing`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.FinishHandoff // true??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.NotReady,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            depo = DepoCoordinationStates.NotReady,

            latches = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            latches = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released
            ),
            depo = DepoCoordinationStates.NotReady,
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by both slides and lift wants to go out and take pixels, extendo will let got, lift will wait to move`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.FinishHandoff // true??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.NotReady,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            latches = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by both slides and extendo wants to go out but lift wants to take pixels, lift will let go, extendo will wait to move`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.FinishHandoff // true??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???


        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(

            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }
    //
    @Test
    fun `when pixel is controlled by extendo and extendo wants to go out, extendo will move`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.DontHandoff // false??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(

            depo = DepoCoordinationStates.ReadyToHandoff,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.DriverControlledPosition,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by lift and lift wants to go out with pixels, lift will move and take pixels`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.FinishHandoff // true??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.NotReady,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            latches = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released
            ),
            depo = DepoCoordinationStates.NotReady,
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when pixel is controlled by lift and lift wants to go out without pixels, lift will not move and pixels will un-handoff`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.DontHandoff // false??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.NotReady,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )

        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.ReadyToHandoff,
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }


    @Test
    fun `when depo is depositing pixels, handoff won't engage`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.DontHandoff // false??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.NotReady,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            depo = DepoCoordinationStates.NotReady,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.NotReady,
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }


    @Test
    fun `when both slides are in and collecotr has pixels, and drivers want both to go out, they go out`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.DontHandoff // false??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(

            depo = DepoCoordinationStates.NotReady,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            depo = DepoCoordinationStates.NotReady,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = true,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.NotReady,
            wrist = SidedPixelHolders(
                left = PixelHolder.Released,
                right = PixelHolder.Released,
            )
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }


    @Test
    fun `when depo slide would be blocking pixels, but there's no pixels, extendo can go out`() {
        // given

        val handoffPixelsToLift = HandoffReadiness.DontHandoff // false??
        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
        val doingHandoff = true // ???

        val testSubject = createHandoffManager()
        val inputConstraints = HandoffConstraints(
            depo = DepoCoordinationStates.NotReady,
            handoffPixelsToLift = HandoffPixelsToLift(handoffPixelsToLift)
        )
        val actualState = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
            depo = DepoCoordinationStates.ReadyToHandoff,

            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            wrist = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding,
            )
        )
        val transferSensorState = Transfer.TransferSensorState(
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
            Transfer.SensorState(
                hasPixelBeenSeen = false,
                timeOfSeeingMillis = 0
            ),
        )

        // when

        val actualOutput = testSubject.coordinateHandoff(
            inputConstraints = inputConstraints,
            physicalExtendoReadiness = physicalExtendoReadiness,
            actualLatches = actualState.latches,
            actualDepo = actualState.depo,
            actualWrist = actualState.wrist,
            transferSensorState = transferSensorState,
            doingHandoff = doingHandoff,
        )

        // then
        val expectedOutput = HandoffCoordinated(
            extendo = ExtendoHandoffControlDecision.DriverControlledPosition,
            latches = SidedPixelHolders(
                left = PixelHolder.Holding,
                right = PixelHolder.Holding
            ),
            depo = DepoCoordinationStates.NotReady,
            wrist = SidedPixelHolders(
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
}
