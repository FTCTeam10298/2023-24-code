package us.brainstormz.robotTwo.tests

import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.CollectorSystem
import us.brainstormz.robotTwo.CollectorTarget
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.TargetRobot
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.tests.TeleopTest.getChangedGamepad

fun main() {
    val steps = listOf(
            TeleopTest.emptyWorld,
            TeleopTest.emptyWorld.copy(
                    actualGamepad2= getChangedGamepad { it.dpad_up = true }
            ),
            TeleopTest.emptyWorld.copy(
                    actualRobot = TeleopTest.emptyWorld.actualRobot.copy(
                        depoState = TeleopTest.emptyWorld.actualRobot.depoState.copy(
                            liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks + 1
                        )
                    )
            ),
            TeleopTest.emptyWorld.copy(
                    actualGamepad2= getChangedGamepad { it.dpad_down = true },
                    actualRobot = TeleopTest.emptyWorld.actualRobot.copy(
                            depoState = TeleopTest.emptyWorld.actualRobot.depoState.copy(
                                    liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks + 1,
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees
                            )
                    )
            ),
            TeleopTest.emptyWorld.copy(
                    actualRobot = TeleopTest.emptyWorld.actualRobot.copy(
                            depoState = TeleopTest.emptyWorld.actualRobot.depoState.copy(
                                    liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks + 1,
                                    armAngleDegrees = Arm.Positions.In.angleDegrees
                            )
                    )
            ),
    )
    val expectedOutput = TeleopTest.emptyTarget.copy(
            targetRobot = TeleopTest.emptyTarget.targetRobot.copy(
                    depoTarget = TeleopTest.emptyTarget.targetRobot.depoTarget.copy(
                            liftPosition = Lift.LiftPositions.Transfer,
                            armPosition = Arm.Positions.In,
                    ),
            )
    )

    val result = TeleopTest.runTest(steps)
    val normalEquality = { output: TargetWorld ->
        output.targetRobot.depoTarget.armPosition == expectedOutput.targetRobot.depoTarget.armPosition
    }
    val testPass = TeleopTest.didTestPass(result = result, normalEquality)

    println(result)
    println("Test passed? $testPass")
}


object TeleopTest {
    fun runTest(testSteps: List<ActualWorld>): List<Pair<ActualWorld, TargetWorld>> {
        val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
        val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
        val teleop = RobotTwoTeleOp(opmode.telemetry)

        teleop.init(hardware)

        val results:List<Pair<ActualWorld, TargetWorld>> = testSteps.mapIndexed() {index, actualWorld ->
            //Set Inputs
            hardware.actualRobot = actualWorld.actualRobot

            //Run Once
            teleop.loop(gamepad1 = actualWorld.actualGamepad1, gamepad2 = actualWorld.actualGamepad2, hardware)

            //Get result
            val result = teleop.functionalReactiveAutoRunner.previousTargetState!!
            actualWorld to result
        }

        return results
    }
    fun didTestPass(result: List<Pair<ActualWorld, TargetWorld>>, passCondition: (TargetWorld)->Boolean): Boolean {// List<Pair<Boolean, Pair<ActualWorld, TargetWorld>>> {
        return passCondition(result.last().second)
    }

    val emptySensorReading = Transfer.SensorReading(0, 0, 0 ,0)
    val emptyWorld = ActualWorld(
            actualGamepad1= Gamepad(),
            actualGamepad2 = Gamepad(),
            actualRobot = ActualRobot(
                    positionAndRotation = PositionAndRotation(),
                    depoState = DepoManager.ActualDepo(
                            armAngleDegrees = 0.0,
                            liftPositionTicks = 0,
                            isLiftLimitActivated = false
                    ),
                    collectorSystemState = CollectorSystem.ActualCollector(
                            extendoPositionTicks = 0,
                            extendoCurrentAmps = 0.0,
                            leftRollerAngleDegrees = 0.0,
                            rightRollerAngleDegrees = 0.0,
                            leftTransferState = emptySensorReading,
                            rightTransferState = emptySensorReading,
                    ),
            ),
            timestampMilis = 0,
    )
    val noInput = RobotTwoTeleOp.DriverInput(
            driveVelocity = PositionAndRotation(),
            depo = RobotTwoTeleOp.DepoInput.NoInput,
            leftClaw = RobotTwoTeleOp.ClawInput.NoInput,
            rightClaw = RobotTwoTeleOp.ClawInput.NoInput,
            collector = RobotTwoTeleOp.CollectorInput.NoInput,
            rollers = RobotTwoTeleOp.RollerInput.NoInput,
            extendo = RobotTwoTeleOp.ExtendoInput.NoInput,
            handoff = RobotTwoTeleOp.HandoffInput.NoInput,
            hang = RobotTwoTeleOp.HangInput.NoInput,
            launcher = RobotTwoTeleOp.LauncherInput.NoInput,
            bumperMode = RobotTwoTeleOp.Gamepad1BumperMode.Collector,
            lightInput = RobotTwoTeleOp.LightInput.NoInput
    )
    val emptyTarget = TargetWorld(
            targetRobot = TargetRobot(
                    positionAndRotation = PositionAndRotation(),
                    depoTarget = DepoTarget(
                            liftPosition = Lift.LiftPositions.Manual,
                            armPosition = Arm.Positions.Manual,
                            leftClawPosition = Claw.ClawTarget.Gripping,
                            rightClawPosition = Claw.ClawTarget.Gripping,
                    ),
                    collectorTarget = CollectorTarget(
                            intakeNoodles = Intake.CollectorPowers.Off,
                            rollers = Transfer.RollerState(Transfer.RollerPowers.Off, Transfer.RollerPowers.Off, Transfer.DirectorState.Off),
                            extendoPositions = Extendo.ExtendoPositions.Manual,
                    ),
                    hangPowers = RobotTwoHardware.HangPowers.Holding,
                    launcherPosition = RobotTwoHardware.LauncherPosition.Holding,
                    lights = RobotTwoTeleOp.LightTarget(
                            targetColor = RobotTwoTeleOp.PixelColor.Unknown,
                            pattern = RobotTwoTeleOp.BothPixelsWeWant(RobotTwoTeleOp.PixelColor.Unknown, RobotTwoTeleOp.PixelColor.Unknown),
                            timeOfColorChangeMilis = 0L
                    ),
            ),
            isLiftEligableForReset = false,
            doingHandoff = false,
            driverInput = noInput,
            isTargetReached = {_, _ -> false}
    )

    fun getChangedGamepad(originalGamepad: Gamepad = Gamepad(), gamepadChange: (gamepad: Gamepad)->Unit): Gamepad {
        gamepadChange(originalGamepad)
        return originalGamepad
    }
}