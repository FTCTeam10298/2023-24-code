package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.utils.DeltaTimeMeasurer

class CollectorSystem(
//        private val intake: Intake,
        private val transfer: Transfer,
        private val extendo: Extendo,
        private val telemetry: Telemetry) {

    val collectorLoopTimeMeasurer = DeltaTimeMeasurer()

    data class ActualCollector(
            val extendo: SlideSubsystem.ActualSlideSubsystem,
            val transferState: Transfer.ActualTransfer,
            val leftRollerAngleDegrees: Double,
            val rightRollerAngleDegrees: Double,
    )

    fun getCurrentState(hardware: RobotTwoHardware, previousActualWorld: ActualWorld?): ActualCollector {
        collectorLoopTimeMeasurer.beginMeasureDT()

        val actualCollector = ActualCollector(
                extendo= extendo.getActualSlideSubsystem(hardware, previousActualWorld?.actualRobot?.collectorSystemState?.extendo),
                leftRollerAngleDegrees= transfer.getFlapAngleDegrees(Transfer.Side.Left, hardware),
                rightRollerAngleDegrees= transfer.getFlapAngleDegrees(Transfer.Side.Right, hardware),
                transferState= transfer.getActualTransfer(hardware),
        )

        collectorLoopTimeMeasurer.endMeasureDT()
        return actualCollector
    }
}