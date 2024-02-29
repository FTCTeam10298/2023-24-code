package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer

class CollectorSystem(
//        private val intake: Intake,
        private val transfer: Transfer,
        private val extendo: Extendo,
        private val telemetry: Telemetry) {

    data class CollectorState(
            val collectorState: Intake.CollectorPowers,
            val extendoPosition: Extendo.ExtendoPositions,
            val transferRollersState: Transfer.TransferTarget,
    )


    data class ActualCollector(
            val extendo: SlideSubsystem.ActualSlideSubsystem,
            val transferState: Transfer.ActualTransfer,
            val leftRollerAngleDegrees: Double,
            val rightRollerAngleDegrees: Double,
    )

    fun getCurrentState(hardware: RobotTwoHardware, previousActualWorld: ActualWorld?): ActualCollector {
        val collectorReadStartTimeMilis = System.currentTimeMillis()
        val actualCollector= ActualCollector(
                extendo= extendo.getActualSlideSubsystem(hardware, previousActualWorld?.actualRobot?.collectorSystemState?.extendo),
                leftRollerAngleDegrees= transfer.getFlapAngleDegrees(Transfer.Side.Left, hardware),
                rightRollerAngleDegrees= transfer.getFlapAngleDegrees(Transfer.Side.Right, hardware),
                transferState= transfer.getActualTransfer(hardware),
        )
        val collectorReadEndTimeMilis = System.currentTimeMillis()
        val timeToReadCollector = collectorReadEndTimeMilis-collectorReadStartTimeMilis
        telemetry.addLine("timeToReadCollector: $timeToReadCollector")
        return actualCollector
    }
}