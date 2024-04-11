import com.qualcomm.robotcore.hardware.Gamepad
import org.junit.Assert
import org.junit.Test
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.CollectorManager
import us.brainstormz.robotTwo.CollectorTarget
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initialPreviousTargetState
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.noInput
import us.brainstormz.robotTwo.TargetRobot
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.localTests.FauxRobotTwoHardware
import us.brainstormz.robotTwo.localTests.TeleopTest
import us.brainstormz.robotTwo.localTests.TeleopTest.Companion.emptyWorld
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.ColorReading
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

class TeleOpHandoffTest {

    @Test
    fun `when slides are in handoff starts`() {
        //given
        val telemetry = PrintlnTelemetry()
        val teleop = RobotTwoTeleOp(
                telemetry = telemetry
        )

        val driverInput = noInput
        val actualWorld = emptyWorld.copy(
                actualRobot = emptyWorld.actualRobot.copy(
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(Extendo.ExtendoPositions.Min.ticks, true, 0, 0, 0.0),
                                transferState = Transfer.ActualTransfer(
                                        left = ColorReading(1f, 1f, 1f, 1f),
                                        right = ColorReading(1f, 1f, 1f, 1f),
                                )
                        ),
                        depoState = DepoManager.ActualDepo(
                                armAngleDegrees = Arm.Positions.In.angleDegrees,
                                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.Down.ticks, true, 0, 0, 0.0),
                                wristAngles = Wrist.ActualWrist(Claw.ClawTarget.Gripping.angleDegrees, Claw.ClawTarget.Gripping.angleDegrees)
                        )
                ),
                timestampMilis = System.currentTimeMillis()
        )
        val previousActualWorld = emptyWorld
        val previousTargetWorld = initialPreviousTargetState

        //when
        val actualOutput = teleop.getTargetWorld(
                driverInput = driverInput,
                actualWorld = actualWorld,
                previousActualWorld = previousActualWorld,
                previousTargetState = previousTargetWorld
        )

        //then
        val expectedOutput = previousTargetWorld.copy(
                targetRobot = previousTargetWorld.targetRobot.copy(
                        collectorTarget = previousTargetWorld.targetRobot.collectorTarget.copy(
                                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = Extendo.ExtendoPositions.Min),
                                intakeNoodles = Intake.CollectorPowers.Eject,
                                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                                transferSensorState = Transfer.TransferSensorState(
                                        left = Transfer.SensorState(hasPixelBeenSeen = true, actualWorld.timestampMilis),
                                        right = Transfer.SensorState(hasPixelBeenSeen = true, actualWorld.timestampMilis),
                                ),
                                latches = Transfer.TransferTarget(
                                        left = Transfer.LatchTarget(
                                                target = Transfer.LatchPositions.Closed, actualWorld.timestampMilis
                                        ),
                                        right = Transfer.LatchTarget(
                                                target = Transfer.LatchPositions.Closed, actualWorld.timestampMilis
                                        ),
                                ),
                                timeOfTransferredMillis = actualWorld.timestampMilis,
                                timeOfEjectionStartMilis = actualWorld.timestampMilis
                        ),
                        depoTarget = DepoTarget(
                                armPosition = Arm.ArmTarget(Arm.Positions.In),
                                lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                wristPosition = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                                targetType = DepoManager.DepoTargetType.GoingHome
                        ),
                        lights = actualOutput.targetRobot.lights
                ),
                driverInput = actualOutput.driverInput,
                doingHandoff = actualOutput.doingHandoff,
                gamepad1Rumble = actualOutput.gamepad1Rumble
        )

        Assert.assertEquals(expectedOutput.toString(), actualOutput.toString())
    }

    @Test
    fun `when depo is in, extendo is out, and drivers retract extendo it retracts`() {
        //given
        val telemetry = PrintlnTelemetry()
        val teleop = RobotTwoTeleOp(
                telemetry = telemetry
        )

        val driverInput = noInput.copy(
                extendo = RobotTwoTeleOp.ExtendoInput.RetractManual,
                extendoManualPower = -1.0
        )
        val actualWorld = emptyWorld.copy(
                actualRobot = emptyWorld.actualRobot.copy(
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(Extendo.ExtendoPositions.Max.ticks, false, 0, Extendo.ExtendoPositions.Max.ticks, 0.0),
                                transferState = Transfer.ActualTransfer(
                                        left = ColorReading(1f, 1f, 1f, 1f),
                                        right = ColorReading(1f, 1f, 1f, 1f),
                                )
                        ),
                        depoState = DepoManager.ActualDepo(
                                armAngleDegrees = Arm.Positions.In.angleDegrees,
                                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.Down.ticks, true, 0, 0, 0.0),
                                wristAngles = emptyWorld.actualRobot.depoState.wristAngles
                        )
                ),
                timestampMilis = System.currentTimeMillis()
        )
        val previousActualWorld = emptyWorld
        val previousTargetWorld = initialPreviousTargetState

        //when
        val actualOutput = teleop.getTargetWorld(
                driverInput = driverInput,
                actualWorld = actualWorld,
                previousActualWorld = previousActualWorld,
                previousTargetState = previousTargetWorld
        )

        //then
        val expectedOutput = previousTargetWorld.copy(
                targetRobot = previousTargetWorld.targetRobot.copy(
                        collectorTarget = previousTargetWorld.targetRobot.collectorTarget.copy(
                                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = Extendo.ExtendoPositions.Manual),
                                intakeNoodles = Intake.CollectorPowers.Eject,
                                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                                transferSensorState = Transfer.TransferSensorState(
                                        left = Transfer.SensorState(hasPixelBeenSeen = true, actualWorld.timestampMilis),
                                        right = Transfer.SensorState(hasPixelBeenSeen = true, actualWorld.timestampMilis),
                                ),
                                latches = Transfer.TransferTarget(
                                        left = Transfer.LatchTarget(
                                                target = Transfer.LatchPositions.Closed, 0
                                        ),
                                        right = Transfer.LatchTarget(
                                                target = Transfer.LatchPositions.Closed, 0
                                        ),
                                ),
                                timeOfTransferredMillis = actualWorld.timestampMilis,
                                timeOfEjectionStartMilis = actualWorld.timestampMilis
                        ),
                        depoTarget = DepoTarget(
                                armPosition = Arm.ArmTarget(Arm.Positions.In),
                                lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                wristPosition = Wrist.WristTargets(Claw.ClawTarget.Retracted),
                                targetType = DepoManager.DepoTargetType.GoingHome
                        ),
                        lights = actualOutput.targetRobot.lights
                ),
                driverInput = actualOutput.driverInput,
                doingHandoff = actualOutput.doingHandoff,
                gamepad1Rumble = actualOutput.gamepad1Rumble
        )

        Assert.assertEquals(expectedOutput.toString(), actualOutput.toString())
//        Assert.assertEquals(expectedOutput, actualOutput)
    }


    @Test
    fun `when depo and extendo are in, and drivers extend extendo it extends`() {
        //given
        val telemetry = PrintlnTelemetry()
        val teleop = RobotTwoTeleOp(
                telemetry = telemetry
        )

        val driverInput = noInput.copy(
                extendo = RobotTwoTeleOp.ExtendoInput.ExtendManual,
                extendoManualPower = 1.0
        )
        val actualWorld = emptyWorld.copy(
                actualRobot = emptyWorld.actualRobot.copy(
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(Extendo.ExtendoPositions.Min.ticks, true, 0, 0, 0.0),
                                transferState = Transfer.ActualTransfer(
                                        left = ColorReading(1f, 1f, 1f, 1f),
                                        right = ColorReading(1f, 1f, 1f, 1f),
                                )
                        ),
                        depoState = DepoManager.ActualDepo(
                                armAngleDegrees = Arm.Positions.In.angleDegrees,
                                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.Down.ticks, true, 0, 0, 0.0),
                                wristAngles = Wrist.ActualWrist(
                                        leftClawAngleDegrees = Claw.ClawTarget.Retracted.angleDegrees,
                                        rightClawAngleDegrees = Claw.ClawTarget.Retracted.angleDegrees,
                                )
                        )
                ),
                timestampMilis = System.currentTimeMillis()
        )
        val previousActualWorld = emptyWorld
        val previousTargetWorld = initialPreviousTargetState

        //when
        val actualOutput = teleop.getTargetWorld(
                driverInput = driverInput,
                actualWorld = actualWorld,
                previousActualWorld = previousActualWorld,
                previousTargetState = previousTargetWorld
        )

        //then
        val expectedOutput = previousTargetWorld.copy(
                targetRobot = previousTargetWorld.targetRobot.copy(
                        collectorTarget = previousTargetWorld.targetRobot.collectorTarget.copy(
                                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = Extendo.ExtendoPositions.Manual),
                                intakeNoodles = Intake.CollectorPowers.Eject,
                                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                                transferSensorState = Transfer.TransferSensorState(
                                        left = Transfer.SensorState(hasPixelBeenSeen = true, actualWorld.timestampMilis),
                                        right = Transfer.SensorState(hasPixelBeenSeen = true, actualWorld.timestampMilis),
                                ),
                                latches = Transfer.TransferTarget(
                                        left = Transfer.LatchTarget(
                                                target = Transfer.LatchPositions.Closed, 0
                                        ),
                                        right = Transfer.LatchTarget(
                                                target = Transfer.LatchPositions.Closed, 0
                                        ),
                                ),
                                timeOfTransferredMillis = actualWorld.timestampMilis,
                                timeOfEjectionStartMilis = actualWorld.timestampMilis
                        ),
                        depoTarget = DepoTarget(
                                armPosition = Arm.ArmTarget(Arm.Positions.In),
                                lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                wristPosition = Wrist.WristTargets(Claw.ClawTarget.Retracted),
                                targetType = DepoManager.DepoTargetType.GoingHome
                        ),
                        lights = actualOutput.targetRobot.lights
                ),
                driverInput = actualOutput.driverInput,
                doingHandoff = actualOutput.doingHandoff,
                gamepad1Rumble = actualOutput.gamepad1Rumble
        )

        println("expected : $expectedOutput")
        println("actual   : $actualOutput")

        Assert.assertEquals(expectedOutput.toString(), actualOutput.toString())
//        Assert.assertEquals(expectedOutput, actualOutput)
//        val expectedCt = expectedOutput.targetRobot.collectorTarget
//        val actualCt = actualOutput.targetRobot.collectorTarget
//        Assert.assertEquals(
//                expectedOutput.targetRobot.collectorTarget.copy(
//
////                timeOfEjectionStartMilis = actualCt.timeOfEjectionStartMilis,
////        timeOfTransferredMillis = actualCt.timeOfTransferredMillis,
////        intakeNoodles = actualCt.intakeNoodles,
////        dropDown = actualCt.dropDown,
////        transferSensorState = actualCt.transferSensorState,
////        latches = actualCt.latches,
//                ),
//                actualCt)
//        Assert.assertEquals(expectedOutput.copy(
//                targetRobot = expectedOutput.targetRobot.copy(
////                        drivetrainTarget = actualOutput.targetRobot.drivetrainTarget,
////                        depoTarget = actualOutput.targetRobot.depoTarget,
//                        collectorTarget = actualOutput.targetRobot.collectorTarget,
////                        hangPowers = actualOutput.targetRobot.hangPowers,
////                        launcherPosition = actualOutput.targetRobot.launcherPosition,
////                        lights = actualOutput.targetRobot.lights,
//                ),
//                getNextTask = actualOutput.getNextTask,
//        ), actualOutput)
    }

    @Test
    fun `when depo is up and extendo is out, and drivers extend extendo it extends`() {
        //given
        val telemetry = PrintlnTelemetry()
        val teleop = RobotTwoTeleOp(
                telemetry = telemetry
        )

        val driverInput = noInput.copy(
                extendo = RobotTwoTeleOp.ExtendoInput.ExtendManual,
                extendoManualPower = 0.1
        )
        val actualWorld = emptyWorld.copy(
                actualRobot = emptyWorld.actualRobot.copy(
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(Extendo.ExtendoPositions.Min.ticks + 20, false, 0, 0, 0.0),
                                transferState = Transfer.ActualTransfer(
                                        left = ColorReading(1f, 1f, 1f, 1f),
                                        right = ColorReading(1f, 1f, 1f, 1f),
                                )
                        ),
                        depoState = DepoManager.ActualDepo(
                                armAngleDegrees = Arm.Positions.In.angleDegrees+2,
                                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.Down.ticks + 20, false, 0, 0, 0.0),
                                wristAngles = Wrist.ActualWrist(
                                        leftClawAngleDegrees = Claw.ClawTarget.Retracted.angleDegrees,
                                        rightClawAngleDegrees = Claw.ClawTarget.Retracted.angleDegrees,
                                )
                        )
                ),
                timestampMilis = System.currentTimeMillis()
        )
        val previousActualWorld = emptyWorld.copy(
                actualRobot = emptyWorld.actualRobot.copy(
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(Extendo.ExtendoPositions.Min.ticks + 20, false, 0, 0, 0.0),
                                transferState = Transfer.ActualTransfer(
                                        left = ColorReading(1f, 1f, 1f, 1f),
                                        right = ColorReading(1f, 1f, 1f, 1f),
                                )
                        )
                )
        )
        val previousTargetWorld = initialPreviousTargetState.copy(
                targetRobot = initialPreviousTargetState.targetRobot.copy(
                        collectorTarget = initialPreviousTargetState.targetRobot.collectorTarget.copy(
                                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = Extendo.ExtendoPositions.Manual),
                                intakeNoodles = Intake.CollectorPowers.Off,
                                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                                transferSensorState = Transfer.TransferSensorState(
                                        left = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                                        right = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                                ),
                        )
                )
        )

        //when
        val actualOutput = teleop.getTargetWorld(
                driverInput = driverInput,
                actualWorld = actualWorld,
                previousActualWorld = previousActualWorld,
                previousTargetState = previousTargetWorld
        )

        //then
        val expectedOutput = previousTargetWorld.copy(
                targetRobot = previousTargetWorld.targetRobot.copy(
                        collectorTarget = previousTargetWorld.targetRobot.collectorTarget.copy(
                                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = Extendo.ExtendoPositions.Manual),
                                intakeNoodles = Intake.CollectorPowers.Off,
                                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                                transferSensorState = Transfer.TransferSensorState(
                                        left = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                                        right = Transfer.SensorState(hasPixelBeenSeen = true, 0),
                                ),
                                latches = Transfer.TransferTarget(
                                        left = Transfer.LatchTarget(
                                                target = Transfer.LatchPositions.Closed, 0
                                        ),
                                        right = Transfer.LatchTarget(
                                                target = Transfer.LatchPositions.Closed, 0
                                        ),
                                ),
                                timeOfTransferredMillis = 0,
                                timeOfEjectionStartMilis = 0
                        ),
                        depoTarget = DepoTarget(
                                armPosition = Arm.ArmTarget(Arm.Positions.In),
                                lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                wristPosition = Wrist.WristTargets(Claw.ClawTarget.Retracted),
                                targetType = DepoManager.DepoTargetType.GoingHome
                        ),
                        lights = actualOutput.targetRobot.lights
                ),
                driverInput = actualOutput.driverInput,
                doingHandoff = actualOutput.doingHandoff,
                gamepad1Rumble = actualOutput.gamepad1Rumble
        )

//        println("expected : $expectedOutput")
//        println("actual   : $actualOutput")

        Assert.assertEquals(expectedOutput.toString(), actualOutput.toString())
    }

//    @Test
//    fun x() {
//
//
//        val a = initialPreviousTargetState.copy(
//                targetRobot = initialPreviousTargetState.targetRobot.copy(
//                        collectorTarget = initialPreviousTargetState.targetRobot.collectorTarget.copy(
//                                extendo = SlideSubsystem.TargetSlideSubsystem(targetPosition = Extendo.ExtendoPositions.Manual),
//                                intakeNoodles = Intake.CollectorPowers.Eject,
//                                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
//                                transferSensorState = Transfer.TransferSensorState(
//                                        left = Transfer.SensorState(hasPixelBeenSeen = true, 0),
//                                        right = Transfer.SensorState(hasPixelBeenSeen = true, 0),
//                                ),
//                                latches = Transfer.TransferTarget(
//                                        left = Transfer.LatchTarget(
//                                                target = Transfer.LatchPositions.Closed, 0
//                                        ),
//                                        right = Transfer.LatchTarget(
//                                                target = Transfer.LatchPositions.Closed, 0
//                                        ),
//                                ),
//                                timeOfTransferredMillis = 0,
//                                timeOfEjectionStartMilis = 0
//                        ),
//                        depoTarget = DepoTarget(
//                                armPosition = Arm.ArmTarget(Arm.Positions.In),
//                                lift = Lift.TargetLift(Lift.LiftPositions.Down),
//                                wristPosition = Wrist.WristTargets(Claw.ClawTarget.Retracted),
//                                targetType = DepoManager.DepoTargetType.GoingHome
//                        ),
//                ),
//        )
//        val b = a.copy(
//                targetRobot = a.targetRobot.copy(
//                        collectorTarget = a.targetRobot.collectorTarget.copy(
//                                intakeNoodles = Intake.CollectorPowers.Intake,
//                        ),
//                )
//        )
//
//
//        Assert.assertEquals(a, b)
//    }
}