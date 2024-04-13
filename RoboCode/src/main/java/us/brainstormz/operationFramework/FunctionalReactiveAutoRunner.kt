package us.brainstormz.operationFramework

import us.brainstormz.utils.measured

class FunctionalReactiveAutoRunner<TargetState, ActualState> {
    var whenLastRun:Long? = null
    var previousTargetState: TargetState? = null
        private set
    var previousActualState: ActualState? = null
        private set
    fun loop(   actualStateGetter: (previousActualState: ActualState?)->ActualState,
                targetStateFetcher:  (actualState: ActualState, previousActualState: ActualState?, previousTargetState: TargetState?)->TargetState,
                stateFulfiller: (targetState: TargetState, previousTargetState: TargetState?, actualState: ActualState, previousActualState: ActualState?)->Unit ) {
        whenLastRun = now()
        val actualState = measured("get actual state"){ actualStateGetter(previousActualState) }
        val targetState = measured("get target state"){ targetStateFetcher(actualState, previousActualState, previousTargetState) }
        measured("fulfill state"){ stateFulfiller(targetState, previousTargetState, actualState, previousActualState) }
        previousTargetState = targetState
        previousActualState = actualState
    }

    private inline fun now() = System.currentTimeMillis()

    fun hackSetForTest(previousTargetState: TargetState){
        this.previousTargetState = previousTargetState
    }
}
