package us.brainstormz.pid

import com.qualcomm.robotcore.util.Range


/**
 * Sets pidf coefficients/multipliers.
 * @param p proportional coefficient
 * @param i integral coefficient
 * @param d derivative coefficient
 * @param f feed-forward coefficient
 * */
data class PID(val kp: Double = 0.0, val ki: Double = 0.0, val kd: Double = 0.0, val kf: Double = 0.0) {

    var p: Double = 0.0
    var i: Double = 0.0
    var d: Double = 0.0
    var f: Double = 0.0

    var iRange = -1.0..1.0

    private var deltaTimeMs: Long = 1
    private var lastTimeMs: Long = System.currentTimeMillis()
    private var lastError: Double = 0.0

    fun pidVals(): Double = p + i + d + f

    /**
     * Calculates pidf in a loop.
     * @param target the target value for the controller
     * @param feedback the current value
     * @return the calculated value for the us.us.brainstormz.pid
     */
    fun calcPID(target: Double, feedback: Double): Double {
        val error: Double = target - feedback

        return calcPID(error)
    }

    fun calcPID(error: Double): Double {
        if (deltaTimeMs < 1)
            deltaTimeMs = 1

        p = kp * error
        i += ki * (error * deltaTimeMs.toDouble())
        d = kd * (error - lastError) / deltaTimeMs.toDouble()

        i = i.coerceIn(iRange)

        lastError = error

        deltaTimeMs = System.currentTimeMillis() - lastTimeMs
        lastTimeMs = System.currentTimeMillis()

        return pidVals()
    }

    override fun toString(): String {
        return "Proportional: $p\n Integral: $i\n Derivative: $d"
    }
}
