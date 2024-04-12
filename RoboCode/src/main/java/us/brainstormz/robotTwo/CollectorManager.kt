package us.brainstormz.robotTwo

import kotlinx.serialization.Serializable
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.utils.measured
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer

class CollectorManager(
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

    fun coordinateCollector(uncoordinatedTarget: CollectorTarget, timestampMillis: Long, previousTargetWorld: TargetWorld): CollectorTarget {

        val actuallyIsIntaking = Intake.CollectorPowers.Intake == previousTargetWorld.targetRobot.collectorTarget.intakeNoodles
        val coordinatedLatchTarget = if (actuallyIsIntaking) {
            Transfer.TransferTarget(
                    left = Transfer.LatchTarget(Transfer.LatchPositions.Closed, timestampMillis),
                    right = Transfer.LatchTarget(Transfer.LatchPositions.Closed, timestampMillis)
            )
        } else {
            uncoordinatedTarget.latches
        }
        val timestampedLatchTargets = transfer.timestampTransferTargets(timestampMillis, coordinatedLatchTarget, previousTargetWorld.targetRobot.collectorTarget.latches)

        val eitherLatchIsOpen = Side.entries.fold(true) { acc, side ->
            acc && Transfer.LatchPositions.Open == previousTargetWorld.targetRobot.collectorTarget.latches.getBySide(side).target
        }
        val coordinatedIntakeTarget = if (eitherLatchIsOpen && uncoordinatedTarget.intakeNoodles == Intake.CollectorPowers.Intake) {
            Intake.CollectorPowers.Off
        } else {
            uncoordinatedTarget.intakeNoodles
        }

        return uncoordinatedTarget.copy(
                latches = timestampedLatchTargets,
                intakeNoodles = coordinatedIntakeTarget
        )
    }
}