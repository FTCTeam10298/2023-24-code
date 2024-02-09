package us.brainstormz.robotTwo

import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.WristPositions

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

    fun getFinalDepoTarget(depoInput: RobotTwoTeleOp.DepoInput): DepoTarget? {
        val depoTargetType = getDepoTargetTypeFromDepoInput(depoInput) ?: return null

        val clawTarget: Claw.ClawTarget =
                when (depoTargetType) {
                    DepoTargetType.GoingOut -> Claw.ClawTarget.Gripping
                    DepoTargetType.GoingHome -> Claw.ClawTarget.Retracted
                    else -> return null
                }

        val armTarget: Arm.Positions =
                when (depoTargetType) {
                    DepoTargetType.GoingOut -> Arm.Positions.Out
                    DepoTargetType.GoingHome -> Arm.Positions.In
                    else -> return null
                }

        val liftTarget: Lift.LiftPositions =
                when (depoTargetType) {
                    DepoTargetType.GoingOut -> lift.getGetLiftTargetFromDepoTarget(depoInput)
                    DepoTargetType.GoingHome -> Lift.LiftPositions.Down
                    else -> return null
                }

        return DepoTarget(
                liftPosition = liftTarget,
                armPosition = armTarget,
                leftClawPosition = clawTarget,
                rightClawPosition = clawTarget,
                targetType = depoTargetType
        )
    }

    fun coordinateArmLiftAndClaws(finalDepoTarget: DepoTarget, previousTargetDepo: DepoTarget, actualDepo: ActualDepo): DepoTarget {

        val bothClawsAreAtTarget = claws.fold(true) {acc, claw ->
            acc && claw.isClawAtPosition(finalDepoTarget.leftClawPosition, previousTargetDepo)
        }

        val liftIsAtFinalRestingPlace = lift.isLiftAtPosition(finalDepoTarget.liftPosition.ticks, actualDepo.liftPositionTicks)
        val armTarget: Arm.Positions = if (bothClawsAreAtTarget) {
            when (liftIsAtFinalRestingPlace) {
                true -> {
                    finalDepoTarget.armPosition
                }
                false -> {
                    val liftIsAtOrAboveClear = actualDepo.liftPositionTicks >= Lift.LiftPositions.ClearForArmToMove.ticks
                    val depoTargetIsOut = finalDepoTarget.targetType == DepoTargetType.GoingOut
                    if (depoTargetIsOut && liftIsAtOrAboveClear) {
                        finalDepoTarget.armPosition
                    } else {
                        Arm.Positions.ClearLiftMovement
                    }
                }
            }
        } else {
            previousTargetDepo.armPosition
        }

        val armIsAtTarget = arm.isArmAtAngle(armTarget.angleDegrees, actualDepo.armAngleDegrees) // change to a more variable past point check

        val liftTarget: Lift.LiftPositions = if (bothClawsAreAtTarget) {
            if (armIsAtTarget) {
                finalDepoTarget.liftPosition
            } else {
                //Waiting for arm
                when (finalDepoTarget.targetType) {
                    DepoTargetType.GoingOut -> {
                        finalDepoTarget.liftPosition
                    }
                    DepoTargetType.GoingHome -> Lift.LiftPositions.ClearForArmToMove
                    else -> previousTargetDepo.liftPosition
                }
            }
        } else {
            previousTargetDepo.liftPosition
        }

        return DepoTarget(
                liftPosition = liftTarget,
                armPosition = armTarget,
                leftClawPosition = finalDepoTarget.leftClawPosition,
                rightClawPosition = finalDepoTarget.rightClawPosition,
                targetType = finalDepoTarget.targetType
        )
    }

    fun fullyManageDepo(target: RobotTwoTeleOp.DriverInput, previousDepoTarget: DepoTarget, actualDepo: ActualDepo, handoffIsReady: Boolean): DepoTarget {

        val depoInput = target.depo
        val wristInput = WristPositions(left= target.leftClaw.toClawTarget()?:previousDepoTarget.leftClawPosition, right= target.rightClaw.toClawTarget()?:previousDepoTarget.rightClawPosition)

        val finalDepoTarget = getFinalDepoTarget(depoInput) ?: previousDepoTarget

        val movingArmAndLiftTarget = coordinateArmLiftAndClaws(finalDepoTarget, previousDepoTarget, actualDepo)

        val armAndLiftAreAtFinalRestingPlace: Boolean = checkIfArmAndLiftAreAtTarget(finalDepoTarget, actualDepo)
        val wristPosition: WristPositions = if (armAndLiftAreAtFinalRestingPlace) {
            val depoIsDepositing: Boolean = movingArmAndLiftTarget.targetType == DepoTargetType.GoingOut
            if (depoIsDepositing) {
                //Driver control
                wristInput
            } else {
                if (handoffIsReady) {
                    val firstLoopOfHandoff = target.handoff == RobotTwoTeleOp.HandoffInput.StartHandoff
                    if (firstLoopOfHandoff) {
                        //start griping the claws
                        WristPositions(Claw.ClawTarget.Gripping)
                    } else {
                        //then manual after
                        wristInput
                    }
                } else {
                    //When it isn't handing off then keep claws retracted
                    WristPositions(Claw.ClawTarget.Retracted)
                }
            }
        } else {
            //When going in/out keep the claws retracted/griping so that pixels can't get dropped
            WristPositions(left= movingArmAndLiftTarget.leftClawPosition, right= movingArmAndLiftTarget.rightClawPosition)
        }

        return movingArmAndLiftTarget.copy(
                leftClawPosition = wristPosition.left,
                rightClawPosition = wristPosition.right,
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

}