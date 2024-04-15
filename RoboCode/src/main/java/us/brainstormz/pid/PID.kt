package us.brainstormz.pid

import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sign

/**
 * Sets pidf coefficients/multipliers.
 * @param p proportional coefficient
 * @param i integral coefficient
 * @param d derivative coefficient
 * @param f feed-forward coefficient
 * */
class PID(val name:String, val kp: Double = 0.0, val ki: Double = 0.0, val kd: Double = 0.0,
          val limits:ClosedFloatingPointRange<Double> = -1.0..1.0,
          val min:Double = 0.0,
          now:Long = System.currentTimeMillis()) {

    var p: Double = 0.0
    var i: Double = 0.0
    var d: Double = 0.0


    var ap: Double = 0.0
    var ai: Double = 0.0
    var ad: Double = 0.0

    var v:Double = 0.0
    var vMin = 0.0

    var deltaTimeMs:Long = 0

    fun reset(now:Long = System.currentTimeMillis()) {
//        deltaTimeMs = 0
        lastTimeMs = now
        lastError = 0.0
        i = 0.0
    }
//    private var deltaTimeMs: Long = 1
    private var lastTimeMs: Long = now
    var lastError: Double = 0.0

    private var lastTarget:Any? = null
    private fun log(m:String) = println("[PID/$name] $m")


    fun applyMin(v:Double):Double = v.sign  * (min + (v.absoluteValue * (1.0 -min)))

    fun calcPID(target:Any, error: Double, now:Long = System.currentTimeMillis()): Double {

        if (lastTarget == null) {
            log("setting initial target: $target")
            reset(now)
            lastTarget = target
        } else if (lastTarget != target) {
            log("resetting due to target change (changed from $lastTarget to $target)")
            lastTarget = target
            reset(now)
        }

        if (lastTimeMs > now) {
            throw Exception("No time travel allowed")
        }

        val deltaTimeMs = now - lastTimeMs
        val dt = deltaTimeMs.toDouble()

        p =  error
        i += error * dt
        d = if (dt == 0.0) {
            0.0
        } else {
            (error - lastError) / dt
        }

        this.lastError = error
        this.lastTimeMs = now
        this.deltaTimeMs = deltaTimeMs

        ap = (kp * p)
        ai = (ki * i)
        ad = (kd * d)

        v = ap + ai + ad

        vMin = applyMin(v)

        return vMin
    }

    fun fancyP(inputP: Double): Double {
        var absFancyP = abs(inputP)
        if (absFancyP <= 0.067)
            absFancyP *= 3.0
        else if (absFancyP <= 0.2)
            absFancyP = 0.2
        return absFancyP * inputP.sign
    }

    override fun toString(): String {
        return "Proportional: $kp\n Integral: $ki\n Derivative: $kd"
    }
}

data class PIConfig (
        val kp: Double,
        val ki: Double,
        val kd: Double,
)
data class PIDState(
        val timeMs:Long,
        val p:Double,
        val i:Double,
        val d:Double,
        val error:Double,
        val v:Double,
)

data class TargetedPIDState(
    val target:Any,
    val state:PIDState,
)

fun doTargetedPid(now:Long, target: Any, error:Double, config:PIConfig, prev:TargetedPIDState?, default:Double = 0.0):PIDState{

    val stateToUse = prev?.let{
        if(it.target!=target){
            null
        }else{
            it
        }
    }

    return doPid(
            now = now,
            error = error,
            config = config,
            prev = stateToUse?.state,
            default = default)
}

fun doPid(now:Long, error:Double, config:PIConfig, prev:PIDState?, default:Double = 0.0):PIDState{
    return prev?.let{prev ->
        val deltaTimeMs = now - prev.timeMs
        val dt = deltaTimeMs.toDouble()

        val p =  error
        val i = prev.i + (error * dt)
        val d = if (dt == 0.0) {
            0.0
        } else {
            (error - prev.error) / dt
        }


        val ap = (config.kp * p)
        val ai = (config.ki * i)
        val ad = (config.kd * d)

        val v = ap + ai + ad

        PIDState(
                timeMs = now,
                p = p,
                i = i,
                d = d,
                error = error,
                v = v
        )
    } ?: PIDState(
            timeMs = now,
            p = 0.0,
            i = 0.0,
            d = 0.0,
            error = error,
            v = default,
    )
}