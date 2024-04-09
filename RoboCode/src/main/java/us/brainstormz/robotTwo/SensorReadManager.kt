package us.brainstormz.robotTwo

import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.localTests.FauxRobotTwoHardware

class SensorReadManager(private val sensorReads: List<(RobotTwoHardware) -> Unit>) {


    private val defaultAverageSensorReadTimeMilis: Long = 10L
    private val indexOfLastItemInAllReads = sensorReads.size-1
    private var indexOfLastReadSensor: Int? = null
    fun manageSensorReads(hardware: RobotTwoHardware, maximumReadTimeMilis: Long = 50) {
        val maxNumberOfReads: Int = (maximumReadTimeMilis.toDouble() / defaultAverageSensorReadTimeMilis.toDouble()).toInt()

        //indexOfLastRedSensor = 9
        //indexOfLastItemInAllReads = 10
        //maxNumberOfReads = 5
        //ranges = indexOfLastRedSensor..indexOfLastItemInAllReads + 0..2

        //indexOfLastRedSensor = 0
        //indexOfLastItemInAllReads = 10
        //maxNumberOfReads = 5
        //ranges = indexOfLastRedSensor..5 + 0..2

        val indexOfFirstRead = indexOfLastReadSensor?.plus(1) ?: 0
        val lastIndexInRangeOne = if (indexOfFirstRead+maxNumberOfReads > indexOfLastItemInAllReads) {
            indexOfLastItemInAllReads
        } else {
            indexOfFirstRead+maxNumberOfReads
        }
        val firstRange:IntRange = indexOfFirstRead..lastIndexInRangeOne
        val numberOfReadsInFirstRange = firstRange.toList().size
        val secondRange:IntRange = 0..maxNumberOfReads-numberOfReadsInFirstRange

        val rangeOfSensorsToRead: List<Int> = firstRange + if (numberOfReadsInFirstRange != maxNumberOfReads) {
            secondRange
        } else {
            emptyList()
        }

        val listOfSensorReads = sensorReads.filterIndexed { i, it -> rangeOfSensorsToRead.contains(i) }
        listOfSensorReads.forEach { it(hardware) }

        indexOfLastReadSensor = rangeOfSensorsToRead.last()
    }
}


fun main() {
    val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
    val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)

    val reads:List<(RobotTwoHardware)->Unit> = (0..9).mapIndexed {i, it -> {println("read$i")} }
    val readManager = SensorReadManager(reads)

    val numberOfLoops = 10
    for (i in 1..numberOfLoops) {
        println("start loop")
        readManager.manageSensorReads(hardware)
    }
}
