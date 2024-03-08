package us.brainstormz.utils

import kotlin.random.Random

class TLocal<V>(private val makeInitial:(t:Thread)->V){
    private val values = mutableMapOf<String, V>()
    fun get():V{
        val t = Thread.currentThread()
        val n = t.name
        return values.getOrPut(n) { makeInitial(t) }

    }
    fun set(v:V){
        val n = Thread.currentThread().name
        values[n] = v
    }
}
val mStack = TLocal{
    println("Initializing thread local for ${it.name}")
    mutableListOf<String>()
}

private val logEverything = true
fun logMeasure(m:String, critical:Boolean = false) {
    if(logEverything || critical){
        println("[MEASURES] [${Thread.currentThread().name}] [${System.currentTimeMillis()}] [${mStack.get().joinToString("/")}] $m")
    }
}
 fun <T>measured(name:String, fn:()->T):T{
    val start = System.currentTimeMillis()
    mStack.get().add(name)

    logMeasure("Started")
    val r = fn()
    val end = System.currentTimeMillis()
    val duration = end - start
    logMeasure("Ended - Duration $duration millis (end $end)")
    if(duration > 300){
        logMeasure("SLOOOOOW ^ - Duration $duration millis ")
    }
    mStack.get().removeLastOrNull()
     
    return r
}




fun main() {

    fun test(name:String, fn:(()->Unit)? = null){
        measured(name) {
            Thread.sleep(Random.nextLong(0, 100))
            if (fn != null) fn()
        }
    }
    (0..2).forEach {
        object:Thread(){
            override fun run() {

                while (true){
                    test("top-level"){
                        test("detail-a"){
                            test("sub-detail-foo")
                        }
                        test("detail-b")
                    }
                }
            }
        }.start()
    }
}
