package us.brainstormz.robotTwo.tests

import android.graphics.Path.Op
import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.FauxOpenCvAbstraction
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.Autonomous
import us.brainstormz.robotTwo.CollectorSystem
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.RobotTwoAuto
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

fun main() {
    val tester = AutoTest()

    val gamepadDpad_down = tester.getChangedGamepad { it.dpad_down = true}
    println("gamepadDpad_down: $gamepadDpad_down")

    val test = listOf(
            tester.emptyWorld.copy(actualGamepad2 = gamepadDpad_down),
//            tester.emptyWorld.copy(actualGamepad2 = Gamepad()),
//            tester.emptyWorld.copy(actualGamepad2 = Gamepad()),
    )

    val output = tester.runTest(test, numberOfTimesToRunInitLoop = 5)
    println("\noutput: \n$output")
}

class AutoTest {
    fun runTest(testSteps: List<ActualWorld>, numberOfTimesToRunInitLoop: Int): List<Pair<ActualWorld, RobotTwoAuto.AutoTargetWorld>> {
        val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
        val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
        val program = RobotTwoAuto(opmode.telemetry)
        val openCvAbstraction = FauxOpenCvAbstraction(opmode)

        program.init(hardware, openCvAbstraction)

        for (i in 1..numberOfTimesToRunInitLoop) {
            program.init_loop(hardware, openCvAbstraction, gamepad1 = Gamepad())
        }

        val results:List<Pair<ActualWorld, RobotTwoAuto.AutoTargetWorld>> = testSteps.mapIndexed() { index, actualWorld ->
            //Set Inputs
            hardware.actualRobot = actualWorld.actualRobot

            //Start
            program.start(hardware, openCvAbstraction)

            //Run Once
            program.loop(hardware)

            //Get result
            val result = program.functionalReactiveAutoRunner.previousTargetState!!
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
                            lift = Lift.ActualLift(
                                    currentPositionTicks =  0,
                                    limitSwitchIsActivated =  false),
                            wristAngles = Wrist.ActualWrist(leftClawAngleDegrees = 0.0, rightClawAngleDegrees = 0.0),
                    ),
                    collectorSystemState = CollectorSystem.ActualCollector(
                            extendo = SlideSubsystem.ActualSlideSubsystem(
                                    currentPositionTicks = 0,
                                    ticksMovedSinceReset = 0,
                                    limitSwitchIsActivated = false,
                                    zeroPositionOffsetTicks = 0,
                                    currentAmps = 0.0),
                            leftRollerAngleDegrees = 0.0,
                            rightRollerAngleDegrees = 0.0,
                            leftTransferState = emptySensorReading,
                            rightTransferState = emptySensorReading,
                    ),
            ),
            timestampMilis = 0,
    )
    fun getChangedGamepad(originalGamepad: Gamepad = Gamepad(), gamepadChange: (gamepad: Gamepad)->Unit): Gamepad {
        gamepadChange(originalGamepad)
        return originalGamepad
    }
}