import com.qualcomm.robotcore.hardware.Gamepad
import org.junit.Assert
import org.junit.Test
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.CollectorManager
import us.brainstormz.robotTwo.CollectorTarget
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.HandoffManager
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initialPreviousTargetState
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.ColorReading
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

class HandoffManagerTest {

    val createPreviousTargetStateChangeTransferAndIntake = {leftLatch: Transfer.LatchPositions, rightLatch: Transfer.LatchPositions, intake: Intake.CollectorPowers ->
        initialPreviousTargetState.copy(
                targetRobot = initialPreviousTargetState.targetRobot.copy(
                        collectorTarget = initialPreviousTargetState.targetRobot.collectorTarget.copy(
                            latches = Transfer.TransferTarget(
                                    leftLatchTarget = Transfer.LatchTarget(
                                            target = leftLatch, 0),
                                    rightLatchTarget = Transfer.LatchTarget(
                                            target = rightLatch, 0)
                            ),
                            intakeNoodles = intake
                    )
                )
        )
    }

    @Test
    fun `when both slides are and and both pixels are controlled by both slides nothing changes`() {
        // given
        val testSubject = createHandoffManager()

        val handoff = HandoffManager.HandoffPixelsToLift(false)
        val depoInput = RobotTwoTeleOp.DepoInput.Down
        val collector = CollectorTarget(
                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = Extendo.ExtendoPositions.Min),
                timeOfEjectionStartMilis = 0,
                timeOfTransferredMillis = 0,
                intakeNoodles = Intake.CollectorPowers.Off,
                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                transferSensorState = Transfer.TransferSensorState(
                        left = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                        right = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                ),
                latches = Transfer.TransferTarget(
                        leftLatchTarget = Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed, 0
                        ),
                        rightLatchTarget = Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed, 0
                        ),
                )
        )
        val previousTargetWorld = createPreviousTargetStateChangeTransferAndIntake(
                Transfer.LatchPositions.Closed,
                Transfer.LatchPositions.Closed,
                Intake.CollectorPowers.Off
        )
        val actualWorld = ActualWorld(
                actualRobot = ActualRobot(
                        positionAndRotation = PositionAndRotation(),
                        depoState = DepoManager.ActualDepo(
                                armAngleDegrees = Arm.Positions.In.angleDegrees,
                                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.Down.ticks, true, 0, 0, 0.0),
                                wristAngles = Wrist.ActualWrist(
                                        leftClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees,
                                        rightClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees
                                )
                        ),
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(Extendo.ExtendoPositions.Min.ticks, true, 0, 0, 0.0),
                                transferState = Transfer.ActualTransfer(
                                        left = ColorReading(1f, 1f, 1f, 1f),
                                        right = ColorReading(1f, 1f, 1f, 1f),
                                )
                        ),
                        neopixelState = Neopixels.HalfAndHalfTarget().compileStripState()
                ),
                timestampMilis = 0,
                actualGamepad1 = Gamepad(),
                actualGamepad2 = Gamepad()
        )

        // when
        val actualOutput = testSubject.manageHandoff(
                handoff = handoff,
                depoInput = depoInput,
                collectorTarget = collector,
                previousTargetWorld = previousTargetWorld,
                actualWorld = actualWorld,
        )

        // then
        val expectedOutput = HandoffManager.HandoffTarget(
                collector = collector,
//            collector = collector.copy(
//                    extendo = SlideSubsystem.TargetSlideSubsystem(Extendo.ExtendoPositions.Min),
//                    latches = Transfer.TransferTarget(
//                            leftLatchTarget = Transfer.LatchTarget(
//                                    target = Transfer.LatchPositions.Closed, 0
//                            ),
//                            rightLatchTarget = Transfer.LatchTarget(
//                                    target = Transfer.LatchPositions.Closed, 0
//                            ),
//                    )
//            ),
                depo = DepoTarget(
                        armPosition = Arm.ArmTarget(Arm.Positions.In),
                        lift = Lift.TargetLift(Lift.LiftPositions.Down),
                        wristPosition = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                        targetType = DepoManager.DepoTargetType.GoingHome
                )
        )


        println("\nexpected: $expectedOutput")
        println("actual: $actualOutput")

        Assert.assertTrue(expectedOutput.toString() == actualOutput.toString())
    }


    @Test
    fun `handoff will start when both slides are in`() {
        // given
        val testSubject = createHandoffManager()

        val handoff = HandoffManager.HandoffPixelsToLift(false)
        val depoInput = RobotTwoTeleOp.DepoInput.Down
        val collector = CollectorTarget(
                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = Extendo.ExtendoPositions.Min),
                timeOfEjectionStartMilis = 0,
                timeOfTransferredMillis = 0,
                intakeNoodles = Intake.CollectorPowers.Off,
                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                transferSensorState = Transfer.TransferSensorState(
                        left = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                        right = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                ),
                latches = Transfer.TransferTarget(
                        leftLatchTarget = Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed, 0
                        ),
                        rightLatchTarget = Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed, 0
                        ),
                )
        )
        val previousTargetWorld = createPreviousTargetStateChangeTransferAndIntake(
                Transfer.LatchPositions.Closed,
                Transfer.LatchPositions.Closed,
                Intake.CollectorPowers.Off
        )
        val actualWorld = ActualWorld(
                actualRobot = ActualRobot(
                        positionAndRotation = PositionAndRotation(),
                        depoState = DepoManager.ActualDepo(
                                armAngleDegrees = Arm.Positions.In.angleDegrees,
                                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.Down.ticks, true, 0, 0, 0.0),
                                wristAngles = Wrist.ActualWrist(
                                        leftClawAngleDegrees = Claw.ClawTarget.Retracted.angleDegrees,
                                        rightClawAngleDegrees = Claw.ClawTarget.Retracted.angleDegrees
                                )
                        ),
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(Extendo.ExtendoPositions.Min.ticks, true, 0, 0, 0.0),
                                transferState = Transfer.ActualTransfer(
                                        left = ColorReading(1f, 1f, 1f, 1f),
                                        right = ColorReading(1f, 1f, 1f, 1f),
                                )
                        ),
                        neopixelState = Neopixels.HalfAndHalfTarget().compileStripState()
                ),
                timestampMilis = 0,
                actualGamepad1 = Gamepad(),
                actualGamepad2 = Gamepad()
        )

        // when
        val actualOutput = testSubject.manageHandoff(
                handoff = handoff,
                depoInput = depoInput,
                collectorTarget = collector,
                previousTargetWorld = previousTargetWorld,
                actualWorld = actualWorld,
        )

        // then
        val expectedOutput = HandoffManager.HandoffTarget(
                collector = collector,
                depo = DepoTarget(
                        armPosition = Arm.ArmTarget(Arm.Positions.In),
                        lift = Lift.TargetLift(Lift.LiftPositions.Down),
                        wristPosition = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                        targetType = DepoManager.DepoTargetType.GoingHome
                )
        )


        println("\nexpected: $expectedOutput")
        println("actual: $actualOutput")

        Assert.assertTrue(expectedOutput.toString() == actualOutput.toString())
    }



    @Test
    fun `when pixel is controlled by extendo and extendo wants to go out, extendo will move`() {
        // given
        val testSubject = createHandoffManager()

        val handoff = HandoffManager.HandoffPixelsToLift(false)
        val depoInput = RobotTwoTeleOp.DepoInput.Down
        val collector = CollectorTarget(
                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = Extendo.ExtendoPositions.PurpleCenterPosition),
                timeOfEjectionStartMilis = 0,
                timeOfTransferredMillis = 0,
                intakeNoodles = Intake.CollectorPowers.Off,
                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                transferSensorState = Transfer.TransferSensorState(
                        left = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                        right = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                ),
                latches = Transfer.TransferTarget(
                        leftLatchTarget = Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed, 0
                        ),
                        rightLatchTarget = Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Closed, 0
                        ),
                )
        )
        val previousTargetWorld = createPreviousTargetStateChangeTransferAndIntake(
                Transfer.LatchPositions.Closed,
                Transfer.LatchPositions.Closed,
                Intake.CollectorPowers.Off
        )
        val actualWorld = ActualWorld(
                actualRobot = ActualRobot(
                        positionAndRotation = PositionAndRotation(),
                        depoState = DepoManager.ActualDepo(
                                armAngleDegrees = Arm.Positions.In.angleDegrees,
                                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.Down.ticks, true, 0, 0, 0.0),
                                wristAngles = Wrist.ActualWrist(
                                        leftClawAngleDegrees = Claw.ClawTarget.Retracted.angleDegrees,
                                        rightClawAngleDegrees = Claw.ClawTarget.Retracted.angleDegrees
                                )
                        ),
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(Extendo.ExtendoPositions.Min.ticks, true, 0, 0, 0.0),
                                transferState = Transfer.ActualTransfer(
                                        left = ColorReading(1f, 1f, 1f, 1f),
                                        right = ColorReading(1f, 1f, 1f, 1f),
                                )
                        ),
                        neopixelState = Neopixels.HalfAndHalfTarget().compileStripState()
                ),
                timestampMilis = 0,
                actualGamepad1 = Gamepad(),
                actualGamepad2 = Gamepad()
        )

        // when
        val actualOutput = testSubject.manageHandoff(
                handoff = handoff,
                depoInput = depoInput,
                collectorTarget = collector,
                previousTargetWorld = previousTargetWorld,
                actualWorld = actualWorld,
        )

        // then
        val expectedOutput = HandoffManager.HandoffTarget(
                collector = collector,
                depo = DepoTarget(
                        armPosition = Arm.ArmTarget(Arm.Positions.In),
                        lift = Lift.TargetLift(Lift.LiftPositions.Down),
                        wristPosition = Wrist.WristTargets(Claw.ClawTarget.Retracted),
                        targetType = DepoManager.DepoTargetType.GoingHome
                )
        )


        println("\nexpected: $expectedOutput")
        println("actual: $actualOutput")

        Assert.assertTrue(expectedOutput.toString() == actualOutput.toString())
    }

    @Test
    fun `when pixel is controlled by depo and depo wants to go out, depo will move`() {
        // given
        val testSubject = createHandoffManager()

        val handoff = HandoffManager.HandoffPixelsToLift(true)
        val depoInput = RobotTwoTeleOp.DepoInput.Preset3
        val collector = CollectorTarget(
                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = Extendo.ExtendoPositions.Min),
                timeOfEjectionStartMilis = 0,
                timeOfTransferredMillis = 0,
                intakeNoodles = Intake.CollectorPowers.Off,
                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                transferSensorState = Transfer.TransferSensorState(
                        left = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                        right = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                ),
                latches = Transfer.TransferTarget(
                        leftLatchTarget = Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Open, 0
                        ),
                        rightLatchTarget = Transfer.LatchTarget(
                                target = Transfer.LatchPositions.Open, 0
                        ),
                )
        )
        val previousTargetWorld = createPreviousTargetStateChangeTransferAndIntake(
                Transfer.LatchPositions.Open,
                Transfer.LatchPositions.Open,
                Intake.CollectorPowers.Off
        )
        val actualWorld = ActualWorld(
                actualRobot = ActualRobot(
                        positionAndRotation = PositionAndRotation(),
                        depoState = DepoManager.ActualDepo(
                                armAngleDegrees = Arm.Positions.In.angleDegrees,
                                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.Down.ticks, true, 0, 0, 0.0),
                                wristAngles = Wrist.ActualWrist(
                                        leftClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees,
                                        rightClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees
                                )
                        ),
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(Extendo.ExtendoPositions.Min.ticks, true, 0, 0, 0.0),
                                transferState = Transfer.ActualTransfer(
                                        left = ColorReading(1f, 1f, 1f, 1f),
                                        right = ColorReading(1f, 1f, 1f, 1f),
                                )
                        ),
                        neopixelState = Neopixels.HalfAndHalfTarget().compileStripState()
                ),
                timestampMilis = 0,
                actualGamepad1 = Gamepad(),
                actualGamepad2 = Gamepad()
        )

        // when
        val actualOutput = testSubject.manageHandoff(
                handoff = handoff,
                depoInput = depoInput,
                collectorTarget = collector,
                previousTargetWorld = previousTargetWorld,
                actualWorld = actualWorld,
        )

        // then
        val expectedOutput = HandoffManager.HandoffTarget(
                collector = collector,
                depo = DepoTarget(
                        armPosition = Arm.ArmTarget(Arm.Positions.ClearLiftMovement),
                        lift = Lift.TargetLift(Lift.LiftPositions.SetLine2),
                        wristPosition = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                        targetType = DepoManager.DepoTargetType.GoingOut
                )
        )


        println("\nexpected : $expectedOutput")
        println(  "actual   : $actualOutput")

        Assert.assertTrue(expectedOutput.toString() == actualOutput.toString())
    }
}