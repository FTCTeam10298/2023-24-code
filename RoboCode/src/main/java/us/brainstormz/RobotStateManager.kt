package us.brainstormz

class RobotStateManager<TargetState, ActualState> {
    private var previousTargetState: TargetState? = null
    fun loop(   actualStateGetter: ()->ActualState,
                targetStateFetcher:  (previousTargetState: TargetState?, actualState: ActualState)->TargetState,
                stateFulfiller: (targetState: TargetState, actualState: ActualState)->Unit ) {
        val actualState = actualStateGetter()
        val targetState = targetStateFetcher(previousTargetState, actualState)
        stateFulfiller(targetState, actualState)
        previousTargetState = targetState
    }
}