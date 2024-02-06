package us.brainstormz.utils

class DeltaTimeMeasurer {
    private var timeBegin: Long? = null
    var peakDeltaTime = 0L

    fun measureTimeSinceLastCallMilis(): Long {
        val deltaTime = endMeasureDT()
        beginMeasureDT()
        return deltaTime
    }

    fun beginMeasureDT() {
        timeBegin = System.currentTimeMillis()
    }

    fun endMeasureDT(): Long {
        val timeEnd = System.currentTimeMillis()

        val deltaTime = timeEnd - (timeBegin ?: timeEnd)

        if (deltaTime > peakDeltaTime) {
            peakDeltaTime = deltaTime
        }

        return deltaTime
    }
}