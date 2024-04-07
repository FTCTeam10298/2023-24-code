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
}