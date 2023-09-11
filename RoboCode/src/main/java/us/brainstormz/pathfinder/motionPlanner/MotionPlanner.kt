package motionPlanner
//
//import code.BezierPath
//
//
//interface MotionPlanner {
//
//    data class ComponentState(val pieceStates: List<Any>)
//    data class WorldState(val worldMap: Nothing, val componentStates: List<ComponentState>)
//    data class MotionPlan(val plan: BezierPath)
//
//    fun plan(currentState: ComponentState, currentWorldState: WorldState, targetState: ComponentState): MotionPlan {
//        return MotionPlan((currentState - targetState) / currentWorldState)
//    }
//    fun interperetPlan(currentState: ComponentState, plan: MotionPlan): ComponentState {
//        return plan / currentState
//    }
//}