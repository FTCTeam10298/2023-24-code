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

    fun peakDeltaTime() = this.peakDeltaTime

    fun getAverageLoopTimeMillis() = avg.getAverage()

    fun measureTimeSinceLastCallMillis(): Long {
        val deltaTime = endMeasureDT()
        avg.addMeasurment(deltaTime)
        beginMeasureDT()
        return deltaTime
    }

    private fun beginMeasureDT() {
        timeBegin = System.currentTimeMillis()
    }

    private fun endMeasureDT(): Long {
        val timeEnd = System.currentTimeMillis()

        deltaTime = timeEnd - (timeBegin ?: timeEnd)

        if (deltaTime > peakDeltaTime) {
            peakDeltaTime = deltaTime
        }

        return deltaTime
    }

}