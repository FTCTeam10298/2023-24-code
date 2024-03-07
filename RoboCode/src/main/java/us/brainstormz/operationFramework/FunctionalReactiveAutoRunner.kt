package us.brainstormz.operationFramework

import us.brainstormz.utils.measured

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
