package us.brainstormz.utils

class LoopTimeMeasurer {
    private var previousMarkedTime: Long? = null
    var peakDeltaTime = 0L

    fun measureTimeSinceLastCallMilis(): Long {
        val currentTimeMark = System.currentTimeMillis()

        val deltaTime = currentTimeMark - (previousMarkedTime ?: currentTimeMark)

        previousMarkedTime = currentTimeMark

        if (deltaTime > peakDeltaTime) {
            peakDeltaTime = deltaTime
        }

        return deltaTime
    }
}