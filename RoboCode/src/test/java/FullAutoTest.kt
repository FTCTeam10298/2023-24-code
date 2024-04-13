import android.util.Size
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Assert
import org.junit.Test
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoAuto
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initialPreviousTargetState
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.localTests.FauxRobotTwoHardware
import us.brainstormz.robotTwo.localTests.TeleopTest.Companion.emptyWorld
import us.brainstormz.robotTwo.onRobotTests.AprilTagPipeline
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem

class FullAutoTest {


    @Test
    fun `robot goes to target position`(){
        val actualWorld = emptyWorld.copy(
//            actualRobot = emptyWorld.actualRobot.copy(
//                depoState = emptyWorld.actualRobot.depoState.copy(
//                    wristAngles = Wrist.ActualWrist(
//                        Claw.ClawTarget.Gripping.angleDegrees,
//                        Claw.ClawTarget.Gripping.angleDegrees
//                    ),
//                    lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.SetLine3.ticks, false, 0, 0, 0.0),
//                    armAngleDegrees = Arm.Positions.Out.angleDegrees
//                ),
//                collectorSystemState = emptyWorld.actualRobot.collectorSystemState.copy(
//                    transferState = Transfer.ActualTransfer(
//                        ColorReading(1f, 1f, 1f, 1f),
//                        ColorReading(1f, 1f, 1f, 1f),
//                    )
//                )
//            ),
//            actualGamepad1 = emptyWorld.actualGamepad1.copy(
//                dpad_up = true
//            )
        )

        val previousTarget = initialPreviousTargetState.copy(
//            targetRobot = initialPreviousTargetState.targetRobot.copy(
//                depoTarget = initialPreviousTargetState.targetRobot.depoTarget.copy(
//                    wristPosition = Wrist.WristTargets(
//                        Claw.ClawTarget.Gripping,
//                        Claw.ClawTarget.Gripping
//                    )
//                ),
//                collectorTarget = initialPreviousTargetState.targetRobot.collectorTarget.copy(
//                    transferSensorState = Transfer.TransferSensorState(
//                        Transfer.SensorState(true, 0),
//                        Transfer.SensorState(true, 0),
//                    )
//                )
//            ),
//            doingHandoff = true
        )

        val now = actualWorld.timestampMilis + 1

        // when
        val newTarget = runTest(actualWorld, previousTarget, now)

        // then
        assertEqualsJson(
            DualMovementModeSubsystem.MovementMode.Position,
            newTarget.targetRobot.drivetrainTarget.movementMode)
    }


    fun <T>assertEqualsJson(expected:T, actual:T){
        val writer = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        Assert.assertEquals(writer.writeValueAsString(expected), writer.writeValueAsString(actual))
        Assert.assertEquals(expected, actual)
    }

    fun runTest(actualWorld:ActualWorld, previousTarget:TargetWorld, now:Long): TargetWorld {
        val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
        val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
        val auto = RobotTwoAuto(
            opmode.telemetry,
            aprilTagPipeline = AprilTagPipeline("Webcam 1", Size(1, 1))
        )
        val openCv = OpenCvAbstraction(opmode)

        auto.init(hardware, openCv)

        auto.initLoop(hardware, openCv, actualWorld.actualGamepad1)

        //Set Inputs
        hardware.actualRobot = actualWorld.actualRobot
        auto.getTime = {now}
        auto.functionalReactiveAutoRunner.hackSetForTest(previousTarget)

        auto.start(hardware, openCv)

        //Run Once
        auto.loop(gamepad1 = actualWorld.actualGamepad1, hardware = hardware)

        //Get result
        return auto.functionalReactiveAutoRunner.previousTargetState!!
    }
}