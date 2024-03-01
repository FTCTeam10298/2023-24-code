package us.brainstormz.robotTwo.tests

import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.utils.DeltaTimeMeasurer

fun main() {
    val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
    val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
    val teleop = RobotTwoTeleOp(opmode.telemetry)

    teleop.init(hardware)

    val loopTimeMeasurer = DeltaTimeMeasurer()
    val peaks = mutableListOf<Pair<Long, Long>>()

    val timeToRunLoopMillis = 150000
//    val timeToRunLoopMillis = 10000
    val timeStarted = System.currentTimeMillis()
    do {
        val actualWorld = TeleopTest.emptyWorld

        //Set Inputs
        hardware.actualRobot = actualWorld.actualRobot

        //Run
        teleop.loop(gamepad1 = actualWorld.actualGamepad1, gamepad2 = actualWorld.actualGamepad2, hardware)


        val timeSinceStartMillis = System.currentTimeMillis() - timeStarted

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()
        val peakTime = loopTimeMeasurer.peakDeltaTime()

        if (loopTime == peakTime) {
            peaks.add(peakTime to timeSinceStartMillis)
        }

        println("peak time: ${peakTime}")

    } while (timeSinceStartMillis < timeToRunLoopMillis)

    println("\n\nnumber of peaks: ${peaks.size}")
    println("peaks: ${peaks}")
    println("highest peak time: ${loopTimeMeasurer.peakDeltaTime()}")
}