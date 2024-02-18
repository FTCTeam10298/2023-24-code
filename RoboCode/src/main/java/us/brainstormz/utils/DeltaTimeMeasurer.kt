package us.brainstormz.utils

class DeltaTimeMeasurer {
    private var timeBegin: Long? = null
    var peakDeltaTime = 0L
    var deltaTime = 0L

    var loopTimeAverageStorage = mutableListOf<Long>()
    fun getAverageLoopTimeMilis(): Long {
        return loopTimeAverageStorage.sum()/loopTimeAverageStorage.size
    }

    fun measureTimeSinceLastCallMilis(): Long {
        val deltaTime = endMeasureDT()
        loopTimeAverageStorage.add(deltaTime)
        beginMeasureDT()
        return deltaTime
    }

    fun beginMeasureDT() {
        timeBegin = System.currentTimeMillis()
    }

    fun endMeasureDT(): Long {
        val timeEnd = System.currentTimeMillis()

        deltaTime = timeEnd - (timeBegin ?: timeEnd)

        if (deltaTime > peakDeltaTime) {
            peakDeltaTime = deltaTime
        }

        return deltaTime
    }

    fun getLastMeasuredDT(): Long {
        return deltaTime
    }
}