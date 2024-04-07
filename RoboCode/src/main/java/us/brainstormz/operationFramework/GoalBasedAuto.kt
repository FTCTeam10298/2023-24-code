package us.brainstormz.operationFramework


class GoalBasedAuto<ScoringStrategy,
                    TargetWorld,
                    ActualWorld: TargetWorld >{

//    interface ScoringStrategy{
//        val steps: List<ScoringStep>
//    }
//
//    interface ScoringStep {
//        fun successCondition(actualWorld: ActualWorld): Boolean
//    }
//    interface RobotState
//    interface NonRobotState
//    interface TargetWorld {
//        val targetRobot: RobotState
//        val targetExtraRobot: NonRobotState
//    }
//    interface ActualWorld: TargetWorld {
//        val actualRobot: RobotState
//        val actualExtraRobot: NonRobotState
//        val timestampMilis: Long
//    }

    private val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    fun loop(
        actualStateGetter: (ActualWorld?)->ActualWorld,
        strategySetter: (actualWorld: ActualWorld, previousActualWorld: ActualWorld?, currentTimeMilis: Long) -> ScoringStrategy,
        nextStepFinder: (scoringStrategy: ScoringStrategy, actualWorld: ActualWorld, previousTargetWorld: TargetWorld?, currentTimeMilis: Long) -> TargetWorld,
        stateFulfiller: (targetWorld: TargetWorld, previousTargetWorld: TargetWorld?, actualWorld: ActualWorld, previousActualWorld: ActualWorld?)->Unit
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