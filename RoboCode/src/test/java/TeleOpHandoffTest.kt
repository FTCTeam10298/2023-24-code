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

        Assert.assertTrue(expectedOutput.toString() == actualOutput.toString())
//        Assert.assertEquals(expectedOutput, actualOutput)
    }

}