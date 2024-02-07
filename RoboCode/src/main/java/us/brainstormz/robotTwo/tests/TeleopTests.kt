package us.brainstormz.robotTwo.tests

import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.RobotTwoTeleOp

fun main() {
    val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
    val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
    val teleop = RobotTwoTeleOp(hardware, opmode.telemetry)


    //Init
    val gamepad1 = Gamepad()

    val gamepad2 = Gamepad()

    //Action 1
    teleop.init()
    teleop.loop(gamepad1, gamepad2)

    val initState = teleop.functionalReactiveAutoRunner.previousTargetState

    //Change
    gamepad1.y = true

    //Action 2
    teleop.loop(gamepad1, gamepad2)

    //Result
    val finalState = teleop.functionalReactiveAutoRunner.previousTargetState

    println("initState: $initState")
    println("finalState: $finalState")

    val areTheyTheSame = initState==finalState
    println("areTheyTheSame: $areTheyTheSame")
}
