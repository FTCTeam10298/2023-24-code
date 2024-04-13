//import org.junit.Assert
//import org.junit.Test
//import us.brainstormz.faux.PrintlnTelemetry
//import us.brainstormz.robotTwo.CollectorManager
//import us.brainstormz.robotTwo.DepoManager
//import us.brainstormz.robotTwo.HandoffManager
//import us.brainstormz.robotTwo.HandoffManager.*
//import us.brainstormz.robotTwo.HandoffManager.HandoffCoordinated.*
//import us.brainstormz.robotTwo.subsystems.Arm
//import us.brainstormz.robotTwo.subsystems.Claw
//import us.brainstormz.robotTwo.subsystems.Extendo
//import us.brainstormz.robotTwo.subsystems.Intake
//import us.brainstormz.robotTwo.subsystems.Lift
//import us.brainstormz.robotTwo.subsystems.Transfer
//import us.brainstormz.robotTwo.subsystems.Wrist
//
//
//fun createHandoffManager(): HandoffManager {
//
//    val telemetry = PrintlnTelemetry()
//    val transfer = Transfer(telemetry)
//    val arm = Arm()
//    val lift = Lift(telemetry)
//    val wrist = Wrist(Claw(telemetry), Claw(telemetry), telemetry)
//    return HandoffManager(
//        collectorManager = CollectorManager(
//            intake = Intake(),
//            transfer = transfer,
//            extendo = Extendo(telemetry),
//            telemetry = telemetry
//        ),
//        depoManager = DepoManager(
//            arm = arm,
//            lift = Lift(telemetry),
//            wrist = wrist,
//            telemetry = telemetry
//        ),
//        wrist = wrist,
//        arm = arm,
//        lift = lift,
//        transfer = transfer,
//        telemetry = telemetry
//    )
//}
//
//fun HandoffCommand.toPixelHolder(): PixelHolder {
//    return when (this) {
//        HandoffCommand.Holding -> PixelHolder.Holding
//        HandoffCommand.Released -> PixelHolder.Released
//        HandoffCommand.Passthrough -> PixelHolder.Released
//    }
//}
//
//fun HandoffSidedOutput.toSidedPixelHolders(): SidedPixelHolders =
//    SidedPixelHolders(
//        left = this.left.toPixelHolder(),
//        right = this.left.toPixelHolder()
//    )
//
//
//fun DepoCoordinationStates.toDepoHandoffControlDecision(): DepoHandoffControlDecision =
//    when (this) {
//        DepoCoordinationStates.NotReady -> DepoHandoffControlDecision.DriverControlledPosition
//        DepoCoordinationStates.PotentiallyBlockingExtendoMovement -> DepoHandoffControlDecision.DriverControlledPosition
//        DepoCoordinationStates.ReadyToHandoff -> DepoHandoffControlDecision.DriverControlledPosition
//    }
//
//class HandoffCoordinatorTest {
//
//    //SidedPixelHolders\(\n(.*)left \= PixelHolder\.(.*)\,\n.*right \= PixelHolder\.(.*)\n            \)
//    //HandoffSidedOutput\(\n$1left \= HandoffCommand.$2\,\n$1right \= HandoffCommand.$3\n            \)
//
//    @Test
//    fun `handoff will start when both slides are in`() {
//        // given
//
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            targetPixelControlStates = TargetPixelControlStates(TargetPixelControlState.ControlledByBoth)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            depo = DepoHandoffControlDecision.HandoffPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoHandoffControlDecision.HandoffPosition,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `handoff will not finish when it can finish but HandoffPixelsToLift is false`() {
//        // given
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // false??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoHandoffControlDecision.HandoffPosition,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `handoff will finish when it can finish and HandoffPixelsToLift is true`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByDepositor // true??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        val testSubject = createHandoffManager()
//
//        // when
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when both slides are in, gripping claw will stay at both, released claw will start handoff`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByDepositor // true??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        val testSubject = createHandoffManager()
//
//        // when
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Released
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by both slides and extendo wants to go out, lift will let go, extendo slide won't move`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByDepositor // true??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by both slides and extendo is a little out, claws will release and extendo will retract`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // false??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.DriverControlledPosition,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        val testSubject = createHandoffManager()
//
//        // when
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by both slides and trying to transfer and extendo is a little out, claws will release and extendo will retract`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByBoth // true??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.DriverControlledPosition,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//        val testSubject = createHandoffManager()
//
//        // when
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by depo and depo is down, and extendo wants to go out, extendo will grab pixels, extendo slide won't move`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // false??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by depo and depo is up, and extendo wants to go out, extendo slide will move`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // true??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = false // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.DriverControlledPosition,// .NotReady,
//            depo = DepoCoordinationStates.NotReady,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.DriverControlledPosition, // .NotReady,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by depo and depo is off the limit and pixels are past the color sensor but still partially in the transfer, and extendo wants to go out, extendo slide won't move`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByBoth // true??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.NotReady,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.DriverControlledPosition, //.NotReady,
//            depo = DepoCoordinationStates.PotentiallyBlockingExtendoMovement,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            depo = DepoCoordinationStates.NotReady,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by both slides and lift wants to go out without pixels, lift will let got but will wait to move`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // false??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.NotReady,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition , //.ReadyToHandoff,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when depo is scoring pixels handoff doesn't do anything`() {
//        // given
//
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // false??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.NotReady,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            depo = DepoCoordinationStates.NotReady,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.NotReady,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//
//
//    @Test
//    fun `when depo has pixels, it's up a little, and extendo is in, collector stays open and lift keeps doing it's thing`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByDepositor // true??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.NotReady,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            depo = DepoCoordinationStates.NotReady,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            depo = DepoCoordinationStates.NotReady,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by both slides and lift wants to go out and take pixels, extendo will let got, lift will wait to move`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByDepositor // true??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.NotReady,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by both slides and extendo wants to go out but lift wants to take pixels, lift will let go, extendo will wait to move`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByDepositor // true??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//    //
//    @Test
//    fun `when pixel is controlled by extendo and extendo wants to go out, extendo will move`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // false??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.DriverControlledPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by lift and lift wants to go out with pixels, lift will move and take pixels`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByDepositor // true??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.NotReady,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            depo = DepoCoordinationStates.NotReady,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//    @Test
//    fun `when pixel is controlled by lift and lift wants to go out without pixels, lift will not move and pixels will un-handoff`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // false??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.NotReady,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.ReadyToHandoff,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//
//    @Test
//    fun `when depo is depositing pixels, handoff won't engage`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // false??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.NotReady,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            depo = DepoCoordinationStates.NotReady,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.NotReady,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//
//    @Test
//    fun `when both slides are in and collecotr has pixels, and drivers want both to go out, they go out`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // false??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//
//            depo = DepoCoordinationStates.NotReady,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            depo = DepoCoordinationStates.NotReady,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = true,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.NotReady,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Released,
//                right = HandoffCommand.Released,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//
//    @Test
//    fun `when depo slide would be blocking pixels, but there's no pixels, extendo can go out`() {
//        // given
//
//        val handoffPixelsToLift = TargetPixelControlState.ControlledByCollector // false??
//        val physicalExtendoReadiness = ExtendoCoordinationStates.ReadyToHandoff
//        val doingHandoff = true // ???
//
//        val testSubject = createHandoffManager()
//        val inputConstraints = HandoffConstraints(
//            depo = DepoCoordinationStates.NotReady,
//            targetPixelControlStates = TargetPixelControlStates(handoffPixelsToLift)
//        )
//        val actualState = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.HandoffPosition, //.ReadyToHandoff,
//            depo = DepoCoordinationStates.ReadyToHandoff,
//
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        val transferSensorState = Transfer.TransferSensorState(
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//            Transfer.SensorState(
//                hasPixelBeenSeen = false,
//                timeOfSeeingMillis = 0
//            ),
//        )
//
//        // when
//
//        val actualOutput = testSubject.coordinateHandoff(
//            inputConstraints = inputConstraints,
//            physicalExtendoReadiness = physicalExtendoReadiness,
//            actualLatches = actualState.latches.toSidedPixelHolders(),
//            actualDepo = actualState.depo,
//            actualWrist = actualState.wrist.toSidedPixelHolders(),
//            transferSensorState = transferSensorState,
//
//        )
//
//        // then
//        val expectedOutput = HandoffCoordinated(
//            extendo = ExtendoHandoffControlDecision.DriverControlledPosition,
//            latches = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding
//            ),
//            depo = DepoCoordinationStates.NotReady,
//            wrist = HandoffSidedOutput(
//                left = HandoffCommand.Holding,
//                right = HandoffCommand.Holding,
//            )
//        )
//        Assert.assertEquals(expectedOutput, actualOutput)
//    }
//
//
//    data class HandoffCoordinationParams(
//        val inputConstraints: HandoffConstraints,
//        val actualState: HandoffCoordinated,
//        val transferSensorState: Transfer.TransferSensorState,
//    )
//}
