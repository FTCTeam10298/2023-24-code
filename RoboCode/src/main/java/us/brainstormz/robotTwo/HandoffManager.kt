package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

class HandoffManager(
        private val collectorSystem: CollectorSystem,
        private val wrist: Wrist,
        private val lift: Lift,
        private val extendo: Extendo,
        private val arm: Arm,
        private val telemetry: Telemetry) {

    data class SideIsActivelyHandingOff(
            val left: Boolean,
            val right: Boolean
    ) {
        fun getBySide(side: Transfer.Side): Boolean {
            return when (side) {
                Transfer.Side.Left -> left
                Transfer.Side.Right -> right
            }
        }
    }
//    data class HandoffState(
//            val handoffStarted: SideIsActivelyHandingOff
//    )

    fun getHandoffState(actualRobot: ActualRobot, previousTargetWorld: TargetWorld): SideIsActivelyHandingOff {

        val liftIsAtAHeightWhereLatchesCouldConflict = !lift.isLiftAbovePosition(targetPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks, actualLiftPositionTicks = actualRobot.depoState.lift.currentPositionTicks)
        val armIsInAPositionWhereLatchesCouldConflict = actualRobot.depoState.armAngleDegrees > Arm.Positions.InsideTheBatteryBox.angleDegrees

        return if (liftIsAtAHeightWhereLatchesCouldConflict && armIsInAPositionWhereLatchesCouldConflict) {
            fun checkIfIsActivelyHandingOffPerSide(side: Transfer.Side): Boolean {
                val claw = wrist.clawsAsMap[side]!!
                val clawIsClosed = claw.isClawAtAngle(Claw.ClawTarget.Gripping, actualRobot.depoState.wristAngles.getBySide(side))
                return clawIsClosed
            }

            SideIsActivelyHandingOff(
                    left = checkIfIsActivelyHandingOffPerSide(Transfer.Side.Left),
                    right = checkIfIsActivelyHandingOffPerSide(Transfer.Side.Right)
            )
        } else {
            SideIsActivelyHandingOff(
                    left = false,
                    right = false
            )
        }
    }

    fun checkIfHandoffIsReadyToStart(actualWorld: ActualWorld, previousActualWorld: ActualWorld): Boolean {
        val isExtendoAllTheWayIn = extendo.isSlideSystemAllTheWayIn(actualWorld.actualRobot.collectorSystemState.extendo)
        val extendoIsMovingInOrNotAtAll = extendo.getVelocityTicksPerMili(actualWorld, previousActualWorld) <= 0
        val extendoIsReady = extendoIsMovingInOrNotAtAll && isExtendoAllTheWayIn

        val liftExtensionIsAllTheWayDown = lift.isSlideSystemAllTheWayIn(actualWorld.actualRobot.depoState.lift)//10 >= actualWorld.actualRobot.depoState.lift.currentPositionTicks//actualWorld.actualRobot.depoState.isLiftLimitActivated
        telemetry.addLine("isExtendoAllTheWayIn: $isExtendoAllTheWayIn")
        telemetry.addLine("liftExtensionIsAllTheWayDown: $liftExtensionIsAllTheWayDown")

        val isArmReadyToTransfer = arm.checkIfArmIsAtTarget(Arm.Positions.In, actualWorld.actualRobot.depoState.armAngleDegrees)
        telemetry.addLine("isArmReadyToTransfer: $isArmReadyToTransfer")

        val readyToHandoff = extendoIsReady && liftExtensionIsAllTheWayDown && isArmReadyToTransfer
        return readyToHandoff
    }

    fun checkIfLatchesShouldOpenForHandoff(actualWorld: ActualWorld, previousActualWorld: ActualWorld): Boolean {
        val isExtendoAllTheWayIn = extendo.isSlideSystemAllTheWayIn(actualWorld.actualRobot.collectorSystemState.extendo)
        val extendoIsMovingInOrNotAtAll = extendo.getVelocityTicksPerMili(actualWorld, previousActualWorld) <= 0
        val extendoIsReady = extendoIsMovingInOrNotAtAll && isExtendoAllTheWayIn

        val isArmReadyToTransfer = arm.checkIfArmIsAtTarget(Arm.Positions.In, actualWorld.actualRobot.depoState.armAngleDegrees)

        val handOffIsHappening = extendoIsReady && isArmReadyToTransfer

        val handoffIsReadyToStart = checkIfHandoffIsReadyToStart(actualWorld, previousActualWorld)
        return handoffIsReadyToStart && handOffIsHappening
    }

}