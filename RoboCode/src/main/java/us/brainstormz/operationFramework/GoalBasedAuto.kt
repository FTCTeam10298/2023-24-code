package us.brainstormz.operationFramework

class GoalBasedAuto {

    interface ScoringStrategy {
        val steps: List<RobotState>
    }

    interface RobotState
    interface ExtraRobotState
    open class TargetWorld(val targetRobot: RobotState, val targetExtraRobot: ExtraRobotState)
    class ActualWorld(val scoringStrategy: ScoringStrategy, val actualRobot: RobotState, val actualExtraRobot: ExtraRobotState, val timestampMilis: Long): TargetWorld(actualRobot, actualExtraRobot)

    private val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    fun loop(
        actualStateGetter: ()->ActualWorld,
        strategySetter: (actualWorld: ActualWorld, previousActualWorld: ActualWorld?, currentTimeMilis: Long) -> ScoringStrategy,
        nextStepFinder: (scoringStrategy: ScoringStrategy, actualWorld: ActualWorld, previousTargetWorld: TargetWorld?, currentTimeMilis: Long) -> TargetWorld,
        stateFulfiller: (targetWorld: TargetWorld, actualWorld: ActualWorld)->Unit
    ) {
        functionalReactiveAutoRunner.loop(
            actualStateGetter = actualStateGetter,
            targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                val scoringStrategy = strategySetter( actualState, previousActualState, System.currentTimeMillis())
                nextStepFinder(scoringStrategy, actualState, previousTargetState, System.currentTimeMillis())
            },
            stateFulfiller = stateFulfiller
        )
    }
}