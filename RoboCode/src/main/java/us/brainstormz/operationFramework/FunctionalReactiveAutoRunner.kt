package us.brainstormz.operationFramework

class FunctionalReactiveAutoRunner<TargetState, ActualState> {
    var previousTargetState: TargetState? = null
        private set
    var previousActualState: ActualState? = null
        private set
    fun loop(   actualStateGetter: (previousActualState: ActualState?)->ActualState,
                targetStateFetcher:  (previousTargetState: TargetState?, actualState: ActualState, previousActualState: ActualState?)->TargetState,
                stateFulfiller: (targetState: TargetState, previousTargetState: TargetState?, actualState: ActualState)->Unit ) {
        val actualState = actualStateGetter(previousActualState)
        val targetState = targetStateFetcher(previousTargetState, actualState, previousActualState)
        stateFulfiller(targetState, previousTargetState, actualState)
        previousTargetState = targetState
        previousActualState = actualState
    }
}