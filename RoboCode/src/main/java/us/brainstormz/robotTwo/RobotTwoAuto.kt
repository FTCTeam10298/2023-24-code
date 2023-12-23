//package us.brainstormz.robotTwo
//
//import com.qualcomm.robotcore.eventloop.opmode.OpMode
//import us.brainstormz.localizer.PositionAndRotation
//import us.brainstormz.motion.MecanumMovement
//import us.brainstormz.localizer.RRLocalizer
//import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
//import us.brainstormz.telemetryWizard.TelemetryConsole
//
//class RobotTwoAuto: OpMode() {
//
//    enum class RandomizationSteps: ScoringStep {
//        AlignToSpike {
//            override fun successCondition(actualWorld: ActualWorld): Boolean = true
//        },
//        DropOnSpike {
//            override fun successCondition(actualWorld: ActualWorld): Boolean = false
//        },
//        AlignToBackboard {
//            override fun successCondition(actualWorld: ActualWorld): Boolean = false
//        },
//        DropOnBackboard {
//            override fun successCondition(actualWorld: ActualWorld): Boolean = false
//        }
//    }
//    enum class CycleSteps: ScoringStep {
//        AlignToStack {
//            override fun successCondition(actualWorld: ActualWorld): Boolean = false
//        },
//        CollectPixel {
//            override fun successCondition(actualWorld: ActualWorld): Boolean = false
//        },
//        AlignToBackboard {
//            override fun successCondition(actualWorld: ActualWorld): Boolean = false
//        },
//        DepositPixels {
//            override fun successCondition(actualWorld: ActualWorld): Boolean = false
//        }
//    }
//    enum class ParkSteps: ScoringStep {
//        GetToPosition {
//            override fun successCondition(actualWorld: ActualWorld): Boolean = false
//        },
//        GetBotReadyForTeleOp {
//            override fun successCondition(actualWorld: ActualWorld): Boolean = false
//        }
//    }
//
//    enum class ScoringStrategies: ScoringStrategy {
//        RandomizationTasks {
//            override val steps: List<RandomizationSteps> = RandomizationSteps.values().toList()
//        },
//        Cycles {
//            override val steps: List<CycleSteps> = CycleSteps.values().toList()
//        },
//        Park {
//            override val steps: List<ParkSteps> = ParkSteps.values().toList()
//        }
//    }
//
//    private fun actualStateReader(): ActualWorld {
//
//    }
//
//    private fun strategySetter(previousTargetState: TargetWorld?, actualState: ActualWorld, previousActualState: ActualWorld?): ScoringStrategies {
//
//    }
//
//    private fun nextStepFinder(scoringStrategies: ScoringStrategies, previousTargetState: TargetWorld?, actualState: ActualWorld, previousActualState: ActualWorld?): ScoringStep {
//
//    }
//
//    private fun nextTargetState(scoringStep: ScoringStep, actualState: ActualWorld): TargetWorld {
//
//    }
//
//    data class RobotState(val positionAndRotation: PositionAndRotation)
//    data class NonRobotState(val isPreloadScored: Boolean)
//    open class TargetWorld( val targetRobot: RobotState,
//                            val targetExtraRobot: NonRobotState)
//    class ActualWorld(actualRobot: RobotState,
//                      actualExtraRobot: NonRobotState,
//                      val timestampMilis: Long): TargetWorld(actualRobot, actualExtraRobot)
//
//    private val console = TelemetryConsole(telemetry)
//
//    private val hardware = RobotTwoHardware(telemetry, this)
//    private val odometryLocalizer = RRLocalizer(hardware)
//    private val mecanumMovement = MecanumMovement(odometryLocalizer, hardware, telemetry)
//    override fun init() {
//        hardware.init(hardwareMap)
//    }
//
//    private val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
//    override fun loop() {
//        odometryLocalizer.recalculatePositionAndRotation()
//        functionalReactiveAutoRunner.loop(
//            actualStateGetter = ::actualStateReader,
//            targetStateFetcher = { previousTargetState, actualState, previousActualState ->
//                val strategy = strategySetter(previousTargetState, actualState, previousActualState)
//                val nextStep = nextStepFinder(strategy, previousTargetState, actualState, previousActualState)
//                nextTargetState(nextStep, actualState)
//            },
//            stateFulfiller = { targetState, actualState ->
//                mecanumMovement.moveTowardTarget(targetState.targetRobot.positionAndRotation)
//            }
//        )
//    }
//
//    interface ScoringStrategy{
//        val steps: List<ScoringStep>
//    }
//    interface ScoringStep {
//        fun successCondition(actualWorld: ActualWorld): Boolean
//    }
//}