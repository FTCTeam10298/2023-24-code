package us.brainstormz.utils

class AverageAccumulator(
        private var numMeasurements:Long = 0,
        private var total:Long = 0,
){
    fun addMeasurment(quantity:Long){
        this.numMeasurements += 1
        this.total += quantity
    }

    fun getAverage(): Long {
        if (numMeasurements == 0L) {
            return 0
        }
        return total/numMeasurements
    }

    fun getNumMeasurements() = this.numMeasurements
}

fun main() {
    println("press enter to start")
    Utils.consoleLines().first()
    val m = DeltaTimeMeasurer()

    while(true){
        m.measureTimeSinceLastCallMillis()
        println(m.getAverageLoopTimeMillis())
        println(m.getNumMeasurements())
    }

}


class DeltaTimeMeasurer {
    private var timeBegin: Long? = null
    private var peakDeltaTime = 0L
    private var deltaTime = 0L
    private val avg = AverageAccumulator()

    fun getNumMeasurements() = avg.getNumMeasurements()

    fun getLastMeasuredDT() = deltaTime

    fun getPeakDeltaTime() = this.peakDeltaTime

    fun getAverageLoopTimeMillis() = avg.getAverage()

    fun measureTimeSinceLastCallMillis(): Long {
        val deltaTime = endMeasureDT()
        beginMeasureDT()
        return deltaTime
    }

    fun beginMeasureDT() {
        timeBegin = System.currentTimeMillis()
    }

    fun endMeasureDT(): Long {
        val timeEnd = System.currentTimeMillis()

        deltaTime = timeEnd - (timeBegin ?: timeEnd)

        if (deltaTime > peakDeltaTime && avg.getNumMeasurements() > 1) {
            peakDeltaTime = deltaTime
        }

        avg.addMeasurment(deltaTime)

        return deltaTime
    }

}