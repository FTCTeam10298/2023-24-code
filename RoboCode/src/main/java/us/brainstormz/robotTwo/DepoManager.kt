package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.robotTwo.subsystems.Wrist.WristTargets

class DepoManager(
        private val arm: Arm,
        private val lift: Lift,
        private val wrist: Wrist,
        private val telemetry: Telemetry,
) {

//    private val claws: List<Claw> = listOf(leftClaw, rightClaw)

    data class ActualDepo(
            val armAngleDegrees: Double,
            val liftPositionTicks: Int,
            val isLiftLimitActivated: Boolean,
            val wristAngles: Wrist.ActualWrist
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
        //close applicable claws otherwise keep the same claw pos
        //If the depo is going out, then if handoff is yes then close applicable claws otherwise keep the same claw pos
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
                wristPosition = WristTargets(both= clawTarget),
                targetType = depoTargetType
        )
    }

    fun coordinateArmLiftAndClaws(finalDepoTarget: DepoTarget, previousTargetDepo: DepoTarget, actualDepo: ActualDepo): DepoTarget {
        //If the depo is going out, then if handoff is yes then close applicable claws otherwise keep the same claw pos

        val eitherClawIsGripping = !wrist.wristIsAtPosition(WristTargets(both= Claw.ClawTarget.Retracted), actualDepo.wristAngles)

        val wristTarget = when (finalDepoTarget.targetType) {
            DepoTargetType.GoingOut -> {
                finalDepoTarget.wristPosition
            }
            DepoTargetType.GoingHome -> {
                //If depo is going in then go out and either claw is gripping, drop then come in.
                val armIsOkToDrop = actualDepo.armAngleDegrees <= Arm.Positions.OkToDropPixels.angleDegrees + 2
                if (!armIsOkToDrop && eitherClawIsGripping) {
                    telemetry.addLine("arm Is not Out && either Claw Is Gripping")
                    previousTargetDepo.wristPosition
                } else {
                    WristTargets(Claw.ClawTarget.Retracted)
                }
            }
            else -> return previousTargetDepo
        }

        val bothClawsAreAtTarget = wrist.wristIsAtPosition(finalDepoTarget.wristPosition, actualDepo.wristAngles)
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
                        telemetry.addLine("arm is clearing lift because depo is either going in and aren't there or are going out and aren't past the wiring box")
                        Arm.Positions.ClearLiftMovement
                    }
                }
            }
        } else {
            when (finalDepoTarget.targetType) {
                DepoTargetType.GoingHome -> {
                    val liftIsAboveClear = actualDepo.liftPositionTicks > Lift.LiftPositions.ClearForArmToMove.ticks
                    if (eitherClawIsGripping && liftIsAboveClear) {
                        //If depo is going in and either claw is gripping then go out, drop then come in.
                        Arm.Positions.Out
                    } else {
                        telemetry.addLine("arm is clearing lift because the claws are gripping and we need to go out and close them before going back in")
                        Arm.Positions.ClearLiftMovement
                    }
                }
                DepoTargetType.GoingOut -> {
                    previousTargetDepo.armPosition
                }
                else -> {
                    previousTargetDepo.armPosition
                }
            }
        }


        val armIsAtTarget = when (armTarget) {
            Arm.Positions.ClearLiftMovement -> actualDepo.armAngleDegrees < Arm.Positions.TooFarIn.angleDegrees && actualDepo.armAngleDegrees >= Arm.Positions.ClearLiftMovement.angleDegrees
            Arm.Positions.In -> actualDepo.armAngleDegrees <= (Arm.Positions.In.angleDegrees + 2)
            else -> arm.isArmAtAngle(armTarget.angleDegrees, actualDepo.armAngleDegrees)
        }
        telemetry.addLine("armIsAtTarget: $armIsAtTarget")

        val liftTarget: Lift.LiftPositions = if (bothClawsAreAtTarget) {
            if (armIsAtTarget) {
                finalDepoTarget.liftPosition
            } else {
                //Waiting for arm
                when (finalDepoTarget.targetType) {
                    DepoTargetType.GoingOut -> {
                        finalDepoTarget.liftPosition
                    }
                    DepoTargetType.GoingHome -> {
                        telemetry.addLine("lift is waiting for the arm")
                        Lift.LiftPositions.ClearForArmToMove
                    }
                    else -> {
                        telemetry.addLine("lift is confused af (asinine and futile)")
                        previousTargetDepo.liftPosition
                    }
                }
            }
        } else {
            telemetry.addLine("lift is waiting for the claws")
            previousTargetDepo.liftPosition
        }

        return DepoTarget(
                liftPosition = liftTarget,
                armPosition = armTarget,
                wristPosition = wristTarget,
                targetType = finalDepoTarget.targetType
        )
    }

    fun fullyManageDepo(target: RobotTwoTeleOp.DriverInput, previousDepoTarget: DepoTarget, actualDepo: ActualDepo, handoffIsReady: Boolean): DepoTarget {
        telemetry.addLine("\nDepo manager: ")

        val depoInput = target.depo
        val wristInput = WristTargets(left= target.leftClaw.toClawTarget() ?: previousDepoTarget.wristPosition.left, right= target.rightClaw.toClawTarget()?:previousDepoTarget.wristPosition.right)

        val finalDepoTarget = getFinalDepoTarget(depoInput) ?: previousDepoTarget

        val movingArmAndLiftTarget = coordinateArmLiftAndClaws(finalDepoTarget, previousDepoTarget, actualDepo)

        val armAndLiftAreAtFinalRestingPlace: Boolean = checkIfArmAndLiftAreAtTarget(finalDepoTarget, actualDepo)
        val wristPosition: WristTargets = if (armAndLiftAreAtFinalRestingPlace) {
            val depoIsDepositing: Boolean = movingArmAndLiftTarget.targetType == DepoTargetType.GoingOut
            if (depoIsDepositing) {
                //Driver control
                wristInput
            } else {
                if (handoffIsReady) {
                    val firstLoopOfHandoff = target.handoff == RobotTwoTeleOp.HandoffInput.StartHandoff//Make this code actually work
                    if (firstLoopOfHandoff) {
                        //start griping the claws
                        WristTargets(Claw.ClawTarget.Gripping)
                    } else {
                        //then manual after
                        wristInput
                    }
                } else {
                    //When it isn't handing off then keep claws retracted
                    WristTargets(Claw.ClawTarget.Retracted)
                }
            }
        } else {
            //When going in/out keep the claws retracted/griping so that pixels can't get dropped
            movingArmAndLiftTarget.wristPosition
        }

        return movingArmAndLiftTarget.copy(wristPosition = wristPosition)
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
                isLiftLimitActivated = lift.isLimitSwitchActivated(hardware),
                wristAngles = wrist.getWristActualState(hardware)
        )
    }

}