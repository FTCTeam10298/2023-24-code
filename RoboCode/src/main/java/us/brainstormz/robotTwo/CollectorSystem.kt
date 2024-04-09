package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.utils.measured
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer

class CollectorSystem(
        private val intake: Intake,
        private val transfer: Transfer,
        private val extendo: Extendo,
        private val telemetry: Telemetry) {


    data class ActualCollector(
            val extendo: SlideSubsystem.ActualSlideSubsystem,
            val transferState: Transfer.ActualTransfer,
    )

    fun getCurrentState(hardware: RobotTwoHardware, previousActualWorld: ActualWorld?): ActualCollector = measured("collector-state"){
        val collectorReadStartTimeMilis = System.currentTimeMillis()

        val actualCollector = ActualCollector(
                extendo= extendo.getActualSlideSubsystem(hardware, previousActualWorld?.actualRobot?.collectorSystemState?.extendo),
                transferState= transfer.getActualTransfer(hardware),
        )

        val collectorReadEndTimeMilis = System.currentTimeMillis()
        val timeToReadCollector = collectorReadEndTimeMilis-collectorReadStartTimeMilis
        telemetry.addLine("timeToReadCollector: $timeToReadCollector")
        actualCollector
    }

    fun coordinateCollector(uncoordinatedTarget: CollectorTarget, previousTargetWorld: TargetWorld): CollectorTarget {

        val actuallyIsIntaking = Intake.CollectorPowers.Intake == previousTargetWorld.targetRobot.collectorTarget.intakeNoodles
        val coordinatedLatchTarget = if (actuallyIsIntaking) {
            Transfer.TransferTarget(
                    leftLatchTarget = Transfer.LatchTarget(Transfer.LatchPositions.Closed, System.currentTimeMillis()),
                    rightLatchTarget = Transfer.LatchTarget(Transfer.LatchPositions.Closed, System.currentTimeMillis())
            )
        } else {
            uncoordinatedTarget.latches
        }

        val eitherLatchIsOpen = Transfer.Side.entries.fold(true) { acc, side ->
            acc && Transfer.LatchPositions.Open == previousTargetWorld.targetRobot.collectorTarget.latches.getBySide(side).target
        }
        val coordinatedIntakeTarget = if (eitherLatchIsOpen && uncoordinatedTarget.intakeNoodles == Intake.CollectorPowers.Intake) {
            Intake.CollectorPowers.Off
        } else {
            uncoordinatedTarget.intakeNoodles
        }

        return uncoordinatedTarget.copy(
                latches = coordinatedLatchTarget,
                intakeNoodles = coordinatedIntakeTarget
        )
    }
}