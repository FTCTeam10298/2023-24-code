import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.CollectorManager
import us.brainstormz.robotTwo.CollectorTarget
import us.brainstormz.robotTwo.CompleteSnapshot
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.TargetRobot
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.blankGamepad
import us.brainstormz.robotTwo.localTests.TeleopTest
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

class CompleteSnapshotTest {
    @Test
    fun `reads and writes`(){
        // given
        val emptyWorld = ActualWorld(
                actualGamepad1 = blankGamepad(),
                actualGamepad2 = blankGamepad(),
                actualRobot = ActualRobot(
                        positionAndRotation = PositionAndRotation(),
                        depoState = DepoManager.ActualDepo(
                                armAngleDegrees = 0.0,
                                lift = SlideSubsystem.ActualSlideSubsystem(
                                        currentPositionTicks = 0,
                                        limitSwitchIsActivated = false),
                                wristAngles = Wrist.ActualWrist(leftClawAngleDegrees = 0.0, rightClawAngleDegrees = 0.0),
                        ),
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(
                                        currentPositionTicks = 0,
                                        ticksMovedSinceReset = 0,
                                        limitSwitchIsActivated = false,
                                        zeroPositionOffsetTicks = 0,
                                        currentAmps = 0.0),
//                                leftRollerAngleDegrees = 0.0,
//                                rightRollerAngleDegrees = 0.0,
                                transferState = Transfer.ActualTransfer(
                                        left = TeleopTest.emptySensorReading,
                                        right = TeleopTest.emptySensorReading
                                ),
                        ),
                        neopixelState = Neopixels.HalfAndHalfTarget().compileStripState()
                ),
                timestampMilis = 0,
                timeOfMatchStartMillis = 0
        )
        val initialTarget = TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(), DualMovementModeSubsystem.MovementMode.Power, Drivetrain.DrivetrainPower()),
                        depoTarget = RobotTwoTeleOp.initDepoTarget,
                        collectorTarget = CollectorTarget(
                                intakeNoodles = Intake.CollectorPowers.Off,
                                dropDown= Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                                timeOfEjectionStartMillis = 0,
                                timeOfTransferredMillis = 0,
                                transferSensorState = Transfer.TransferSensorState(
                                        left = RobotTwoTeleOp.initSensorState,
                                        right = RobotTwoTeleOp.initSensorState
                                ),
                                latches = Transfer.TransferTarget(
                                        left = RobotTwoTeleOp.initLatchTarget,
                                        right = RobotTwoTeleOp.initLatchTarget
                                ),
                                extendo = Extendo.ExtendoTarget(Extendo.ExtendoPositions.Min, 0.0, DualMovementModeSubsystem.MovementMode.Position),
                        ),
                        hangPowers = RobotTwoHardware.HangPowers.Holding,
                        launcherPosition = RobotTwoHardware.LauncherPosition.Holding,
                        lights = RobotTwoTeleOp.LightTarget(
                                pattern = RobotTwoTeleOp.BothPixelsWeWant(RobotTwoTeleOp.PixelColor.Unknown, RobotTwoTeleOp.PixelColor.Unknown),
                                stripTarget = Neopixels.HalfAndHalfTarget().compileStripState()
                        ),
                ),
                doingHandoff = false,
                driverInput = RobotTwoTeleOp.noInput,
                autoInput = RobotTwoTeleOp.teleopAutoState,
                gamepad1Rumble = null
        )
        val snapshot = CompleteSnapshot(
                actualWorld = emptyWorld,
                null,
                initialTarget,
                null
        )
        // when
        val json = snapshot.toJson()
        val foo:CompleteSnapshot = jacksonObjectMapper().readValue(json)

        println(json)

        // then
        Assert.assertEquals(snapshot.copy(targetWorld = snapshot.targetWorld), foo)
    }
}