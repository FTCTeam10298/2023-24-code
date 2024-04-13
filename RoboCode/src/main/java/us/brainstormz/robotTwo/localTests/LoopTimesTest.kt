package us.brainstormz.robotTwo.localTests

import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.utils.DeltaTimeMeasurer

fun getHeapInfoAsString(env: Runtime): String {
    return """
    |   Max Heap Size = maxMemory() = ${env.maxMemory()}
    |   Current Heap Size = totalMemory() = ${env.totalMemory()}
    |   Available in Current Heap = freeMemory() = ${env.freeMemory()} //current heap will extend if no more freeMemory to a maximum of maxMemory
    |   Currently Used Heap = ${env.totalMemory() - env.freeMemory()}
    |   Unassigned Heap = ${env.maxMemory() - env.totalMemory()}
    |   Currently Totally Available Heap Space = ${env.maxMemory() - env.totalMemory() + env.freeMemory()} //available=unassigned + free
    """.trimMargin()
}

data class PeakData(
        val loopTimeMillis: Long,
        val timeSinceStartMillis: Long,
        val memoryData: String,
        val previousLoopMemoryData: String,
)
var previousLoopMemoryData = ""
fun main() {
    val env = Runtime.getRuntime()


    val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
    val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
    val teleop = RobotTwoTeleOp(opmode.telemetry)

    teleop.initRobot(hardware)

    val loopTimeMeasurer = DeltaTimeMeasurer()
    val peaks = mutableListOf<PeakData>()

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

        val memoryData = getHeapInfoAsString(env)
        if (loopTime == peakTime) {
            peaks.add(PeakData(
                    loopTimeMillis = loopTime,
                    timeSinceStartMillis = timeSinceStartMillis,
                    memoryData = memoryData,
                    previousLoopMemoryData= previousLoopMemoryData))
        }

        println("peak time: ${peakTime}")

        previousLoopMemoryData = memoryData
    } while (timeSinceStartMillis < timeToRunLoopMillis)

    println("\n\nnumber of peaks: ${peaks.size}")
    println("peaks: ${peaks.fold("") {acc, it ->
        acc + "\n\n" + it.toString()
    }}")
    println("highest peak time: ${loopTimeMeasurer.peakDeltaTime()}")
}