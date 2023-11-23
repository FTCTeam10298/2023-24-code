package us.brainstormz.operationFramework

class FunctionalReactiveAutoRunner<TargetState, ActualState: TargetState> {
    private var previousTargetState: TargetState? = null
    private var previousActualState: ActualState? = null
    fun loop(   actualStateGetter: ()->ActualState,
                targetStateFetcher:  (previousTargetState: TargetState?, actualState: ActualState, previousActualState: ActualState?)->TargetState,
                stateFulfiller: (targetState: TargetState, actualState: ActualState)->Unit ) {
        val actualState = actualStateGetter()
        val targetState = targetStateFetcher(previousTargetState, actualState, previousActualState)
        stateFulfiller(targetState, actualState)
        previousTargetState = targetState
        previousActualState = actualState
    }
}