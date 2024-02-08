package us.brainstormz.robotTwo

import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Lift

class DepoManager(
        private val arm: Arm,
        private val lift: Lift,
        private val leftClaw: Claw,
        private val rightClaw: Claw) {
    private val claws: List<Claw> = listOf(leftClaw, rightClaw)

    data class ActualDepo(
            val armAngleDegrees: Double,
            val liftPositionTicks: Int,
            val isLiftLimitActivated: Boolean
//        val leftClawAngleDegrees: Double,
//        val rightClawAngleDegrees: Double,
    )


    enum class DepoTargetType {
        GoingHome,
        GoingOut,
        Manual
//        Interrupted
    }

    fun getDepoTargetTypeFromDepoInput(depoInput: RobotTwoTeleOp.DepoInput): DepoTargetType? {
        return when (depoInput) {
            RobotTwoTeleOp.DepoInput.SetLine1 -> DepoTargetType.GoingOut
            RobotTwoTeleOp.DepoInput.SetLine2 -> DepoTargetType.GoingOut
            RobotTwoTeleOp.DepoInput.SetLine3 -> DepoTargetType.GoingOut
            RobotTwoTeleOp.DepoInput.Down -> DepoTargetType.GoingHome
            else -> null
        }
    }

    fun coordinateDepo(depoInput: RobotTwoTeleOp.DepoInput, previousTargetDepo: DepoTarget, actualDepo: ActualDepo): DepoTarget {
        val depoTargetType = getDepoTargetTypeFromDepoInput(depoInput) ?: return initDepoTarget

        val clawTarget: Claw.ClawTarget = when (depoTargetType) {
            DepoTargetType.GoingOut -> Claw.ClawTarget.Gripping
            DepoTargetType.GoingHome -> Claw.ClawTarget.Retracted
            else -> return initDepoTarget
        }
        val bothClawsAreAtTarget = claws.fold(true) {acc, claw ->
            acc && claw.isClawAtPosition(clawTarget, previousTargetDepo)
        }

        val armTarget: Arm.Positions = if (bothClawsAreAtTarget) {
            when (depoTargetType) {
                DepoTargetType.GoingOut -> Arm.Positions.Out
                DepoTargetType.GoingHome -> Arm.Positions.ClearLiftMovement
                else -> return initDepoTarget
            }
        } else {
            previousTargetDepo.armPosition
        }
        val armIsAtTarget = arm.isArmAtAngle(armTarget.angleDegrees, actualDepo.armAngleDegrees)


        val liftTarget: Lift.LiftPositions = if (bothClawsAreAtTarget) {
            if (armIsAtTarget) {
                when (depoTargetType) {
                    DepoTargetType.GoingOut -> lift.getGetLiftTargetFromDepoTarget(depoInput)
                    DepoTargetType.GoingHome -> Lift.LiftPositions.Transfer
                    else -> return initDepoTarget
                }
            } else {
                //Waiting for arm
                when (depoTargetType) {
                    DepoTargetType.GoingOut -> previousTargetDepo.liftPosition
                    DepoTargetType.GoingHome -> Lift.LiftPositions.ClearForArmToMove
                    else -> return initDepoTarget
                }
            }
        } else {
            previousTargetDepo.liftPosition
        }


        return DepoTarget(
                liftPosition = liftTarget,
                armPosition = armTarget,
                leftClawPosition = clawTarget,
                rightClawPosition = clawTarget,
                targetType = depoTargetType
        )
    }

    fun checkIfArmAndLiftAreAtTarget(target: DepoTarget, actualDepo: ActualDepo): Boolean {
        val liftIsAtTarget = target.liftPosition.ticks == actualDepo.liftPositionTicks
        val armIsAtTarget = target.armPosition.angleDegrees == actualDepo.armAngleDegrees
        return liftIsAtTarget && armIsAtTarget
    }

    fun getDepoState(hardware: RobotTwoHardware): ActualDepo {
        return ActualDepo(
                armAngleDegrees = arm.getArmAngleDegrees(hardware),
                liftPositionTicks = lift.getCurrentPositionTicks(hardware),
                isLiftLimitActivated = lift.isLimitSwitchActivated(hardware)
        )
    }

    val initDepoTarget = DepoTarget(
            liftPosition = Lift.LiftPositions.Nothing,
            armPosition = Arm.Positions.Manual,
            leftClawPosition = Claw.ClawTarget.Gripping,
            rightClawPosition = Claw.ClawTarget.Gripping,
            targetType = DepoTargetType.GoingHome
    )
}