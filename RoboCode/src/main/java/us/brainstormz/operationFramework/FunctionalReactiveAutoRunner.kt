package us.brainstormz.operationFramework

class FunctionalReactiveAutoRunner<TargetState, ActualState> {
    var previousTargetState: TargetState? = null
        private set
    var previousActualState: ActualState? = null
        private set
    fun loop(   actualStateGetter: (previousActualState: ActualState?)->ActualState,
                targetStateFetcher:  (previousTargetState: TargetState?, actualState: ActualState, previousActualState: ActualState?)->TargetState,
                stateFulfiller: (targetState: TargetState, actualState: ActualState)->Unit ) {
        val actualState = actualStateGetter(previousActualState)
        val targetState = targetStateFetcher(previousTargetState, actualState, previousActualState)
        stateFulfiller(targetState, actualState)
        previousTargetState = targetState
        previousActualState = actualState
    }
}