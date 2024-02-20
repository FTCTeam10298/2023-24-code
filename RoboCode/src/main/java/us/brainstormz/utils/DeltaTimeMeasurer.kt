package us.brainstormz.utils

class DeltaTimeMeasurer {
    private var timeBegin: Long? = null
    private var peakDeltaTime = 0L
    private var deltaTime = 0L

    private val loopTimeAverageStorage = mutableListOf<Long>()

    fun peakDeltaTime() = this.peakDeltaTime

    fun getAverageLoopTimeMillis(): Long {
        return loopTimeAverageStorage.sum()/loopTimeAverageStorage.size
    }

    fun measureTimeSinceLastCallMillis(): Long {
        val deltaTime = endMeasureDT()
        loopTimeAverageStorage.add(deltaTime)
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