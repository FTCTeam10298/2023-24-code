package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.utils.measured
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*
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
            val lift: SlideSubsystem.ActualSlideSubsystem,
//            val liftPositionTicks: Int,
//            val liftZeroPositionOffsetTicks: Int,
//            val isLiftLimitActivated: Boolean,
            val wristAngles: Wrist.ActualWrist
    )

    enum class ArmInput {
        In,
        Out
    }
    data class WristInput(
            val left: Claw.ClawTarget,
            val right: Claw.ClawTarget
    )

    enum class DepoTargetType {
        GoingHome,
        GoingOut,
        Manual
//        Interrupted
    }

    fun getDepoTargetTypeFromDepoInput(depoInput: RobotTwoTeleOp.DepoInput): DepoTargetType? {
        return when (depoInput) {
            RobotTwoTeleOp.DepoInput.Preset1 -> DepoTargetType.GoingOut
            RobotTwoTeleOp.DepoInput.Preset2 -> DepoTargetType.GoingOut
            RobotTwoTeleOp.DepoInput.Preset3 -> DepoTargetType.GoingOut
            RobotTwoTeleOp.DepoInput.Preset4 -> DepoTargetType.GoingOut
            RobotTwoTeleOp.DepoInput.YellowPlacement -> DepoTargetType.GoingOut
            RobotTwoTeleOp.DepoInput.ScoringHeightAdjust -> DepoTargetType.GoingOut
            RobotTwoTeleOp.DepoInput.Down -> DepoTargetType.GoingHome
            else -> null
        }
    }

//    var jankSave = WristTargets(Claw.ClawTarget.Gripping)
    fun getFinalDepoTarget(depoInput: RobotTwoTeleOp.DepoInput, depoScoringHeightAdjust: Double, wristInput: WristTargets, previousWristTarget: WristTargets, previousDepoTargetType: DepoTargetType, actualLift: SlideSubsystem.ActualSlideSubsystem, actualArmAngleDegrees: Double): DepoTarget? {

        //close applicable claws otherwise keep the same claw pos
        //If the depo is going out, then if handoff is yes then close applicable claws otherwise keep the same claw pos
        val depoTargetType = getDepoTargetTypeFromDepoInput(depoInput) ?: return null

//        if (previousDepoTargetType != depoTargetType) {
//            jankSave = WristTargets(previousWristTarget.left, previousWristTarget.right)
//        }

        val wristTarget: WristTargets =
                when (depoTargetType) {
                    DepoTargetType.GoingOut -> wristInput
                    DepoTargetType.GoingHome -> {
//                        val depoIsAlreadyDown = !lift.isLiftAbovePosition(Lift.LiftPositions.ClearForArmToMove.ticks, actualLift.currentPositionTicks) && actualArmAngleDegrees >= Arm.Positions.OkToDropPixels.angleDegrees;
//                        if (depoIsAlreadyDown) {
//                            wristInput
//                        } else {
//                            WristTargets(Claw.ClawTarget.Retracted)
//                        }
                        WristTargets(Claw.ClawTarget.Retracted)
                    }
                    else -> return null
                }

        val armTarget: Arm.Positions =
                when (depoTargetType) {
                    DepoTargetType.GoingOut -> Arm.Positions.Out
                    DepoTargetType.GoingHome -> Arm.Positions.In
                    else -> return null
                }

        val liftTarget: SlideSubsystem.SlideTargetPosition =
                when (depoTargetType) {
                    DepoTargetType.GoingOut -> lift.getGetLiftTargetFromDepoTarget(depoInput, depoScoringHeightAdjust)
                    DepoTargetType.GoingHome -> Lift.LiftPositions.Down
                    else -> return null
                }

        return DepoTarget(
                lift = Lift.TargetLift(liftTarget, movementMode = MovementMode.Position),
                armPosition = Arm.ArmTarget(armTarget, movementMode = MovementMode.Position, 0.0),
                wristPosition = wristTarget,
                targetType = depoTargetType
        )
    }

    fun checkIfArmIsAtTarget(armTarget: Arm.ArmAngle, actualArmAngleDegrees: Double): Boolean {
        return if (armTarget is Arm.Positions) {
            when (armTarget) {
                Arm.Positions.ClearLiftMovement -> actualArmAngleDegrees < Arm.Positions.TooFarIn.angleDegrees && actualArmAngleDegrees >= Arm.Positions.ClearLiftMovement.angleDegrees
                Arm.Positions.In -> actualArmAngleDegrees >= (Arm.Positions.In.angleDegrees)
                Arm.Positions.Out -> actualArmAngleDegrees <= (Arm.Positions.OkToDropPixels.angleDegrees + 2)
                else -> arm.isArmAtAngle(armTarget.angleDegrees, actualArmAngleDegrees)
            }
        } else {
            armTarget.angleDegrees == actualArmAngleDegrees
        }
    }

    fun coordinateArmLiftAndClaws(finalDepoTarget: DepoTarget, previousTargetDepo: DepoTarget, actualDepo: ActualDepo, handoffCompleted: Boolean): DepoTarget {
        //If the depo is going out, then if handoff is yes then close applicable claws otherwise keep the same claw pos

        val eitherClawIsGripping = !wrist.wristIsAtPosition(WristTargets(both= Claw.ClawTarget.Retracted), actualDepo.wristAngles)

        val armAndLiftAreAtFinalRestingPlace: Boolean = checkIfArmAndLiftAreAtTarget(finalDepoTarget, actualDepo)

        val wristTarget = when (finalDepoTarget.targetType) {
            DepoTargetType.GoingOut -> {
                val armIsOutEnoughToDrop = actualDepo.armAngleDegrees <= Arm.Positions.OkToDropPixels.angleDegrees
                if (armIsOutEnoughToDrop) {
                    finalDepoTarget.wristPosition
                } else {
                    wrist.getWristTargetsFromActualWrist(actualDepo.wristAngles)
                }
            }
            DepoTargetType.GoingHome -> {
                if (armAndLiftAreAtFinalRestingPlace) {
                    finalDepoTarget.wristPosition
                } else {
                    //If depo is going in then go out and either claw is gripping, drop then come in.

                    val armIsOkToDrop = actualDepo.armAngleDegrees <= Arm.Positions.OkToDropPixels.angleDegrees + 2
                    if (!armIsOkToDrop && eitherClawIsGripping) {
                        val gripIfClawIsntFullyReleased = { side: Side ->
                            val claw = wrist.getBySide(side)
                            val clawIsFullyRetracted = claw.isClawAtAngle(Claw.ClawTarget.Retracted, actualDepo.wristAngles.getBySide(side))
                            if (clawIsFullyRetracted) {
                                Claw.ClawTarget.Retracted
                            } else {
                                Claw.ClawTarget.Gripping
                            }
                        }
                        WristTargets(
                                left = gripIfClawIsntFullyReleased(Side.Left),
                                right = gripIfClawIsntFullyReleased(Side.Right)
                        )
                    } else {
                        WristTargets(Claw.ClawTarget.Retracted)
                    }
                }
            }
            else -> return previousTargetDepo
        }

        val bothClawsAreAtIntermediateTarget = wrist.wristIsAtPosition(wristTarget, actualDepo.wristAngles)
        val bothClawsAreAtFinalTarget = wrist.wristIsAtPosition(finalDepoTarget.wristPosition, actualDepo.wristAngles)
        val liftIsAtFinalRestingPlace = lift.isLiftAtPosition(finalDepoTarget.lift.targetPosition.ticks, actualDepo.lift.currentPositionTicks)

        val clawsArentMoving = wristTarget.asMap.entries.fold(true) {acc, (side, claw) ->
            acc && previousTargetDepo.wristPosition.getBySide(side) == claw
        }
        telemetry.addLine("clawsArentMoving: $clawsArentMoving")
        telemetry.addLine("wristTarget: ${wristTarget.asMap}")
        telemetry.addLine("finalWristPosition: ${finalDepoTarget.wristPosition.asMap}")

        val armTarget: Arm.ArmAngle = if (bothClawsAreAtFinalTarget) {
            when (liftIsAtFinalRestingPlace) {
                true -> {
                    finalDepoTarget.armPosition.targetPosition
                }
                false -> {
                    val liftIsAtOrAboveClear = actualDepo.lift.currentPositionTicks >= Lift.LiftPositions.ClearForArmToMove.ticks
                    val depoTargetIsOut = finalDepoTarget.targetType == DepoTargetType.GoingOut
                    val armIsOut = actualDepo.armAngleDegrees < Arm.Positions.InsideTheBatteryBox.angleDegrees
                    if (depoTargetIsOut && (liftIsAtOrAboveClear || armIsOut)) {
                        finalDepoTarget.armPosition.targetPosition
                    } else {
                        telemetry.addLine("arm is clearing lift because depo is either going in and aren't there or are going out and aren't past the wiring box")
                        Arm.Positions.ClearLiftMovement
                    }
                }
            }
        } else {
            val liftIsAboveClear = actualDepo.lift.currentPositionTicks >= Lift.LiftPositions.ClearForArmToMove.ticks
            when (finalDepoTarget.targetType) {
                DepoTargetType.GoingHome -> {

                    when {
                        armAndLiftAreAtFinalRestingPlace -> finalDepoTarget.armPosition.targetPosition
                        eitherClawIsGripping && liftIsAboveClear -> {
                            //If depo is going in and either claw is gripping then go out, drop then come in.
                            Arm.Positions.Out
                        }
                        else -> {
                            telemetry.addLine("arm is clearing lift because the claws are gripping and we need to go out and close them before going back in")
                            Arm.Positions.ClearLiftMovement
                        }
                    }
                }
                DepoTargetType.GoingOut -> {
                    if (clawsArentMoving && liftIsAboveClear) {
                        finalDepoTarget.armPosition.targetPosition
                    } else {
                        previousTargetDepo.armPosition.targetPosition
                    }
                }
                else -> {
                    previousTargetDepo.armPosition.targetPosition
                }
            }
        }


        val armIsAtIntermediateTarget = checkIfArmIsAtTarget(armTarget, actualDepo.armAngleDegrees)
        val armIsAtFinalTarget = checkIfArmIsAtTarget(finalDepoTarget.armPosition.targetPosition, actualDepo.armAngleDegrees)
        telemetry.addLine("armIsAtTarget: $armIsAtIntermediateTarget")

        val liftTarget: SlideSubsystem.SlideTargetPosition =
                if (bothClawsAreAtIntermediateTarget) {
                    if (armIsAtIntermediateTarget && (armIsAtFinalTarget || finalDepoTarget.targetType == DepoTargetType.GoingHome)) {
                        finalDepoTarget.lift.targetPosition
                    } else {
                        val goToFinalAnyway = when (finalDepoTarget.targetType) {
                            DepoTargetType.GoingHome -> {
                                false
//                                val armIsInsideOfBatteryBox = actualDepo.armAngleDegrees <= Arm.Positions.InsideTheBatteryBox.angleDegrees
//                                val liftIsAlreadyDecentlyFarDown = actualDepo.lift.currentPositionTicks < Lift.LiftPositions.ClearForArmToMove.ticks/2
//
//                                !eitherClawIsGripping && (liftIsAlreadyDecentlyFarDown && !armIsInsideOfBatteryBox)
                            }
                            else -> {
                                //When going out arm doesn't have to be at position just out enough
                                val liftTargetIsAboveArmClearanceHeight = finalDepoTarget.lift.targetPosition.ticks > Lift.LiftPositions.ClearForArmToMove.ticks
                                val armIsOutEnough = actualDepo.armAngleDegrees <= (Arm.Positions.InsideTheBatteryBox.angleDegrees-10)

                                liftTargetIsAboveArmClearanceHeight || armIsOutEnough
                            }
                        }

                        if (goToFinalAnyway) {
                            finalDepoTarget.lift.targetPosition
                        } else {
                            telemetry.addLine("lift is waiting for the arm")
                            Lift.LiftPositions.TargetClearForArmToMove
                        }
                    }
                } else {
                    telemetry.addLine("lift is waiting for the claws")
//                    SlideSubsystem.VariableTargetPosition(actualDepo.lift.currentPositionTicks)
                    previousTargetDepo.lift.targetPosition
                }

        return DepoTarget(
                lift = Lift.TargetLift(liftTarget, movementMode = MovementMode.Position),
                armPosition = Arm.ArmTarget(armTarget),
                wristPosition = wristTarget,
                targetType = finalDepoTarget.targetType
        )
    }

    fun fullyManageDepo(target: RobotTwoTeleOp.DriverInput, previousTarget: DepoTarget, actualWorld: ActualWorld, handoffCompleted: Boolean): DepoTarget {
        val actualDepo: ActualDepo = actualWorld.actualRobot.depoState
        telemetry.addLine("\nDepo manager: ")

        val depoInput = target.depo
        val wristInput = WristTargets(left= target.wrist.left.toClawTarget() ?: previousTarget.wristPosition.left, right= target.wrist.right.toClawTarget()?:previousTarget.wristPosition.right)

        val finalDepoTarget = getFinalDepoTarget(
                depoInput,
                target.depoScoringHeightAdjust,
                wristInput,
                previousTarget.wristPosition,
                previousTarget.targetType,
                SlideSubsystem.ActualSlideSubsystem(actualDepo.lift),
                actualDepo.armAngleDegrees
        ) ?: previousTarget

        val movingArmAndLiftTarget = coordinateArmLiftAndClaws(finalDepoTarget, previousTarget, actualDepo, handoffCompleted)

        val armAndLiftAreAtFinalRestingPlace: Boolean = checkIfArmAndLiftAreAtTarget(finalDepoTarget, actualDepo)
        val wristPosition: WristTargets =
                when (movingArmAndLiftTarget.targetType) {
                    DepoTargetType.GoingHome -> {
                        if (armAndLiftAreAtFinalRestingPlace) {
                            wristInput
                        } else {
                            telemetry.addLine("keeping wrist closed because arm and lift aren't ready")
                            //When going in/out keep the claws retracted/griping so that pixels can't get dropped
                            movingArmAndLiftTarget.wristPosition
                        }
                    }
                    DepoTargetType.GoingOut -> {
                        if (checkIfArmIsAtTarget(finalDepoTarget.armPosition.targetPosition, actualDepo.armAngleDegrees)) {
                            wristInput
                        } else {
                            //When going in/out keep the claws retracted/griping so that pixels can't get dropped
                            movingArmAndLiftTarget.wristPosition
                        }
                    }
                    else -> previousTarget.wristPosition
                }


        val liftWithFindReset = movingArmAndLiftTarget.lift
//        val liftWithFindReset = if (movingArmAndLiftTarget.lift.targetPosition == Lift.LiftPositions.Down && actualDepo.lift.currentPositionTicks <= Lift.LiftPositions.Down.ticks && actualDepo.armAngleDegrees >= Arm.Positions.InsideTheBatteryBox.angleDegrees) {
//            val liftIsSuperCloseToZero = actualWorld.actualRobot.depoState.lift.currentPositionTicks <= 100
//            val liftIsntAtLimit = !actualWorld.actualRobot.depoState.lift.limitSwitchIsActivated
//
//            if (liftIsSuperCloseToZero && liftIsntAtLimit) {
//                Lift.TargetLift(power = -lift.findResetPower, movementMode = MovementMode.Power)
//            } else {
//                movingArmAndLiftTarget.lift
//            }
//        } else {
//            movingArmAndLiftTarget.lift
//        }

        return movingArmAndLiftTarget.copy(wristPosition = wristPosition, lift = liftWithFindReset)
    }

    fun checkIfArmAndLiftAreAtTarget(target: DepoTarget, actualDepo: ActualDepo): Boolean {
        val liftIsAtTarget = lift.isLiftAtPosition(target.lift.targetPosition.ticks, actualDepo.lift.currentPositionTicks)
        val armIsAtTarget = checkIfArmIsAtTarget(target.armPosition.targetPosition, actualDepo.armAngleDegrees)//arm.isArmAtAngle(target.armPosition.angleDegrees, actualDepo.armAngleDegrees)
        return liftIsAtTarget && armIsAtTarget
    }

    fun getDepoState(hardware: RobotTwoHardware, previousActualWorld: ActualWorld?): ActualDepo = measured("dep get depo state"){
        val readStartTimeMilis = System.currentTimeMillis()
        val actualDepo =  ActualDepo(
                armAngleDegrees = arm.getArmAngleDegrees(hardware),
                lift = lift.getActualSlideSubsystem(hardware, previousActualWorld?.actualRobot?.depoState?.lift),
//                liftPositionTicks = lift.getCurrentPositionTicks(hardware),
//                liftZeroPositionOffsetTicks = ,
//                isLiftLimitActivated = lift.isLimitSwitchActivated(hardware),
                wristAngles = wrist.getWristActualState(hardware)
        )
        val readEndTimeMilis = System.currentTimeMillis()
        val timeToRead = readEndTimeMilis-readStartTimeMilis
        telemetry.addLine("timeToRead Depo: $timeToRead")
        actualDepo
    }

}