package us.brainstormz.operationFramework

class FunctionalReactiveAutoRunner<TargetState, ActualState> {
    var previousTargetState: TargetState? = null
        private set
    var previousActualState: ActualState? = null
        private set
    fun loop(   actualStateGetter: (previousActualState: ActualState?)->ActualState,
                targetStateFetcher:  (previousTargetState: TargetState?, actualState: ActualState, previousActualState: ActualState?)->TargetState,
                stateFulfiller: (targetState: TargetState, previousTargetState: TargetState?, actualState: ActualState)->Unit ) {
        val actualState = measured("get actual state"){ actualStateGetter(previousActualState) }
        val targetState = measured("get target state"){ targetStateFetcher(previousTargetState, actualState, previousActualState) }
        measured("fulfill state"){ stateFulfiller(targetState, previousTargetState, actualState) }
        previousTargetState = targetState
        previousActualState = actualState
    }

    private inline fun now() = System.currentTimeMillis()
}

val mStack = mutableListOf<String>()
inline fun <T>measured(name:String, fn:()->T):T{
    val start = System.currentTimeMillis()
    mStack.add(name)
    val tag = "[MEASURES] [$start] [${mStack.joinToString("/")}]"
    println("$tag Started")
    val r = fn()
    mStack.removeLastOrNull()
    val end = System.currentTimeMillis()
    val duration = end - start
    println("$tag Finished in $duration millis (ended $end)")
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
