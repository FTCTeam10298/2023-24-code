package us.brainstormz.robotTwo

import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Transfer

class CollectorSystem(
//        private val intake: Intake,
        private val transfer: Transfer,
        private val extendo: Extendo,
        private val telemetry: Telemetry) {

    data class CollectorState(
            val collectorState: Intake.CollectorPowers,
            val extendoPosition: Extendo.ExtendoPositions,
            val transferRollersState: Transfer.RollerState,
            val transferLeftSensorState: Transfer.TransferHalfState,
            val transferRightSensorState: Transfer.TransferHalfState,
    )


    data class ActualCollector(
            val extendoPositionTicks: Int,
            val extendoCurrentAmps: Double,
            val extendoLimitIsActivated: Boolean,
            val leftRollerAngleDegrees: Double,
            val rightRollerAngleDegrees: Double,
            val leftTransferState: Transfer.SensorReading,
            val rightTransferState: Transfer.SensorReading,
    )

    fun getCurrentState(hardware: RobotTwoHardware): ActualCollector {

        return ActualCollector(
                extendoPositionTicks= extendo.getExtendoPositionTicks(hardware),
                extendoCurrentAmps = hardware.extendoMotorMaster.getCurrent(CurrentUnit.AMPS),
                extendoLimitIsActivated = extendo.getExtendoLimitIsActivated(hardware),
                leftRollerAngleDegrees= transfer.getFlapAngleDegrees(Transfer.Side.Left, hardware),
                rightRollerAngleDegrees= transfer.getFlapAngleDegrees(Transfer.Side.Right, hardware),
                leftTransferState= transfer.getSensorReading(hardware.leftTransferSensor),
                rightTransferState= transfer.getSensorReading(hardware.rightTransferSensor),
        )
    }
}