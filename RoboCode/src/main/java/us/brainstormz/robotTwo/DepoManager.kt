package us.brainstormz.robotTwo

class DepoManager(val arm: Arm, val lift: Lift) {//, val leftClaw: Claw, val rightClaw: Claw) {

    data class ActualDepo(
            val armAngleDegrees: Double,
            val liftPositionTicks: Int
//        val leftClawAngleDegrees: Double,
//        val rightClawAngleDegrees: Double,
    )

    fun getDepoState(hardware: RobotTwoHardware): ActualDepo {
        return ActualDepo(
                armAngleDegrees = arm.getArmAngleDegrees(hardware),
                liftPositionTicks = lift.getCurrentPositionTicks(),
        )
    }
}