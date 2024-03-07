package us.brainstormz.utils

import kotlin.random.Random

val mStack = mutableListOf<String>()
inline fun logMeasure(m:String) = println("[MEASURES] [${System.currentTimeMillis()}] [${mStack.joinToString("/")}] $m")
inline fun <T>measured(name:String, fn:()->T):T{
    val start = System.currentTimeMillis()
    mStack.add(name)

    logMeasure("Started")
    val r = fn()
    val end = System.currentTimeMillis()
    val duration = end - start
    logMeasure("Ended - Duration $duration millis (end $end)")
    if(duration > 300){
        logMeasure("SLOOOOOW ^")
    }
    mStack.removeLastOrNull()
    return r
}



fun main() {

    fun test(name:String, fn:(()->Unit)? = null){
        measured(name) {
            Thread.sleep(Random.nextLong(0, 100))
            if (fn != null) fn()
        }
    }

    while (true){
        test("top-level"){
            test("detail-a"){
                test("sub-detail-foo")
            }
            test("detail-b")
        }
    }
}
