package us.brainstormz.utils

class LoopTimeMeasurer {
    private var previousMarkedTime = 0L

    fun measureTimeSinceLastCallMilis(): Long {
        val currentTimeMark = System.currentTimeMillis()
        val deltaTime = currentTimeMark - previousMarkedTime
        previousMarkedTime = currentTimeMark

        return deltaTime
    }
}