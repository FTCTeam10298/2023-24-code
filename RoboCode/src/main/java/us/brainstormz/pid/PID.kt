package us.brainstormz.pid

/**
 * Sets pidf coefficients/multipliers.
 * @param p proportional coefficient
 * @param i integral coefficient
 * @param d derivative coefficient
 * @param f feed-forward coefficient
 * */
class PID(val name:String, val kp: Double = 0.0, val ki: Double = 0.0, val kd: Double = 0.0,
          val limits:ClosedFloatingPointRange<Double> = -1.0..1.0,
          now:Long = System.currentTimeMillis()) {

    var p: Double = 0.0
    var i: Double = 0.0
    var d: Double = 0.0

    private fun reset(now:Long = System.currentTimeMillis()) {
        deltaTimeMs = 0
        lastTimeMs = now
        lastError = 0.0
        i = 0.0
    }
    private var deltaTimeMs: Long = 1
    private var lastTimeMs: Long = now
    private var lastError: Double = 0.0

    private var lastTarget:Any? = null
    private fun log(m:String) = println("[PID/$name] $m")
    fun calcPID(target:Any, error: Double, now:Long = System.currentTimeMillis()): Double {

        if(lastTarget==null){
            log("setting initial target: $target")
            lastTarget = target
        } else if(lastTarget!=target){
            log("resetting due to target change (changed from $lastTarget to $target)")
            lastTarget = target
            reset(now)
        }

        if(lastTimeMs>now){
            throw Exception("No time travel allowed")
        }
        if (deltaTimeMs < 1)
            deltaTimeMs = 1

        p = kp * error
        i += ki * (error * deltaTimeMs.toDouble())
        d = kd * (error - lastError) / deltaTimeMs.toDouble()

        i = i.coerceIn(limits)

        lastError = error

        deltaTimeMs = now - lastTimeMs
        lastTimeMs = now

        return p + i + d
    }

    override fun toString(): String {
        return "Proportional: $kp\n Integral: $ki\n Derivative: $kd"
    }
}
