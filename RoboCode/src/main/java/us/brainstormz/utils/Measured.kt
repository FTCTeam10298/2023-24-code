package us.brainstormz.utils

val mStack = mutableListOf<String>()
inline fun tag(time:Long) = "[MEASURES] [$time] [${mStack.joinToString("/")}]"
inline fun <T>measured(name:String, fn:()->T):T{
    val start = System.currentTimeMillis()
    mStack.add(name)

    println("${tag(start)} Started")
    val r = fn()
    mStack.removeLastOrNull()
    val end = System.currentTimeMillis()
    val duration = end - start
    println("${tag(end)} Ended - Duration $duration millis")
    if(duration > 300){
        println("[MEASURES] SLOOOOOW ^")
    }
    return r
}



fun main() {

    fun test(name:String, fn:(()->Unit)? = null){
        measured(name) {
            if (fn != null) fn()
        }
    }

    while (true){
        test("top-level"){
            test("detail a"){
                test("sub detail foo")
            }
            test("detail b")
        }
    }
}
