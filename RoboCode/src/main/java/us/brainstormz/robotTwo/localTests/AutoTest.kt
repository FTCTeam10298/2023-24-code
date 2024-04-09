package us.brainstormz.robotTwo.localTests

import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.FauxOpenCvAbstraction
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.onRobotTests.AprilTagPipeline
import us.brainstormz.robotTwo.RobotTwoAuto
import us.brainstormz.robotTwo.TargetWorld

fun main() {
    val tester = AutoTest()

    val gamepadDpad_down = tester.getChangedGamepad { it.dpad_down = true}
    println("gamepadDpad_down: $gamepadDpad_down")

    val test = listOf(
            TeleopTest.emptyWorld.copy(actualGamepad2 = gamepadDpad_down),
//            tester.emptyWorld.copy(actualGamepad2 = Gamepad()),
//            tester.emptyWorld.copy(actualGamepad2 = Gamepad()),
    )

    val output = tester.runTest(test, numberOfTimesToRunInitLoop = 5)
    println("\noutput: \n$output")
}

class AutoTest {
    fun runTest(testSteps: List<ActualWorld>, numberOfTimesToRunInitLoop: Int): List<Pair<ActualWorld, TargetWorld>> {
        val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
        val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
        val program = RobotTwoAuto(opmode.telemetry, AprilTagPipeline(hardware.backCameraName, hardware.backCameraResolution))
        val openCvAbstraction = FauxOpenCvAbstraction(opmode)

        program.init(hardware, openCvAbstraction)

        for (i in 1..numberOfTimesToRunInitLoop) {
            program.init_loop(hardware, openCvAbstraction, gamepad1 = Gamepad())
        }

        val results:List<Pair<ActualWorld, TargetWorld>> = testSteps.mapIndexed() { index, actualWorld ->
            //Set Inputs
            hardware.actualRobot = actualWorld.actualRobot

            //Start
            program.start(hardware, openCvAbstraction)

            //Run Once
            for (i in 1..numberOfTimesToRunInitLoop) {
                program.loop(hardware, gamepad1 = Gamepad())
            }

            //Get result
            val result = program.functionalReactiveAutoRunner.previousTargetState!!
            actualWorld to result
        }

        return results
    }
    fun didTestPass(result: List<Pair<ActualWorld, TargetWorld>>, passCondition: (TargetWorld)->Boolean): Boolean {// List<Pair<Boolean, Pair<ActualWorld, TargetWorld>>> {
        return passCondition(result.last().second)
    }
    fun getChangedGamepad(originalGamepad: Gamepad = Gamepad(), gamepadChange: (gamepad: Gamepad)->Unit): Gamepad {
        gamepadChange(originalGamepad)
        return originalGamepad
    }
}