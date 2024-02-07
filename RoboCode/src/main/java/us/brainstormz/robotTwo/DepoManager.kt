package us.brainstormz.robotTwo

class DepoManager(val arm: Arm, val lift: Lift) {//, val leftClaw: Claw, val rightClaw: Claw) {

    data class ActualDepo(
            val armAngleDegrees: Double,
            val liftPositionTicks: Int
//        val leftClawAngleDegrees: Double,
//        val rightClawAngleDegrees: Double,
    )

    fun getDepoState(): ActualDepo {
        return ActualDepo(
                armAngleDegrees = arm.getArmAngleDegrees(),
                liftPositionTicks = lift.getCurrentPositionTicks(),
        )
    }
}