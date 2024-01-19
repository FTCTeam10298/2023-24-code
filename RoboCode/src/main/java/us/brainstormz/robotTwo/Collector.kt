package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.ColorSensor
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.potatoBot.potatoBotHardware

class Collector(private val extendoMotorMaster: DcMotorEx,
                private val extendoMotorSlave: DcMotor,
                private val collectorServo1: CRServo,
                private val collectorServo2: CRServo,
                private val rightTransferServo: CRServo,
                private val leftTransferServo: CRServo,
                private val transferDirectorServo: CRServo,
                private val leftTransferPixelSensor: ColorSensor,
                private val rightTransferPixelSensor: ColorSensor,
                leftRollerEncoder: AnalogInput,
                rightRollerEncoder: AnalogInput,
                private val telemetry: Telemetry) {

    val leftEncoderReader = AxonEncoderReader(leftRollerEncoder, 0.0)
    val rightEncoderReader = AxonEncoderReader(rightRollerEncoder, 0.0)

    enum class CollectorPowers(val power: Double) {
        Off(0.0),
        Intake(1.0),
        Eject(-1.0)
    }

    val maxSafeCurrentAmps = 5.5

    init {
        extendoMotorMaster.setCurrentAlert(maxSafeCurrentAmps, CurrentUnit.AMPS)
    }

    fun getCollectorState(driverInput: CollectorPowers): CollectorPowers {
        val bothTransfersAreFull = previousLeftTransferState.hasPixelBeenSeen && previousRightTransferState.hasPixelBeenSeen
        return if (bothTransfersAreFull && driverInput == CollectorPowers.Intake)
            CollectorPowers.Off
        else
            driverInput
    }

    fun spinCollector(power: Double) {
        collectorServo1.power = power
        collectorServo2.power = power
    }

    enum class DirectorState(val power: Double) {
        Left(1.0),
        Right(-1.0),
        Off(0.0)
    }

    data class TransferState(val leftServoCollect: CollectorPowers, val rightServoCollect: CollectorPowers, val directorState: DirectorState)
    data class TransferHalfState(val hasPixelBeenSeen: Boolean, val timeOfSeeingMilis: Long)
    var previousLeftTransferState = TransferHalfState(false, 0)
    var previousRightTransferState = TransferHalfState(false, 0)
    val extraTransferRollingTimeMilis = 3000
    fun getAutoTransferState(isCollecting: Boolean): TransferState {
        //Detection:
        val isLeftSeeingPixel = isPixelIn(leftTransferPixelSensor)
        val timeOfSeeingLeftPixelMilis = when {
            !previousLeftTransferState.hasPixelBeenSeen && isLeftSeeingPixel-> System.currentTimeMillis()
            !isLeftSeeingPixel -> 0
            else -> previousLeftTransferState.timeOfSeeingMilis
        }
        val leftTransferState = TransferHalfState(isLeftSeeingPixel, timeOfSeeingLeftPixelMilis)

        val isRightSeeingPixel = isPixelIn(rightTransferPixelSensor)
        val timeOfSeeingRightPixelMilis = when {
            !previousRightTransferState.hasPixelBeenSeen && isRightSeeingPixel-> System.currentTimeMillis()
            !isRightSeeingPixel -> 0
            else -> previousRightTransferState.timeOfSeeingMilis
        }
        val rightTransferState = TransferHalfState(isRightSeeingPixel, timeOfSeeingRightPixelMilis)


        //Should collect
        val timeSinceLeftSeen = System.currentTimeMillis() - leftTransferState.timeOfSeeingMilis
        val shouldLeftServoCollect = when {
            (!leftTransferState.hasPixelBeenSeen && isCollecting) || timeSinceLeftSeen < extraTransferRollingTimeMilis -> CollectorPowers.Intake
            else -> CollectorPowers.Off
        }
        val timeSinceRightSeen = System.currentTimeMillis() - rightTransferState.timeOfSeeingMilis
        val shouldRightServoCollect = when {
            !leftTransferState.hasPixelBeenSeen -> CollectorPowers.Off
            (!rightTransferState.hasPixelBeenSeen && isCollecting) || timeSinceRightSeen < extraTransferRollingTimeMilis -> CollectorPowers.Intake
            else -> CollectorPowers.Off
        }

        val directorState = when {
            !isCollecting -> DirectorState.Off
            !leftTransferState.hasPixelBeenSeen -> DirectorState.Left
            !rightTransferState.hasPixelBeenSeen -> DirectorState.Right
            else -> DirectorState.Off
        }

        previousRightTransferState = rightTransferState
        previousLeftTransferState = leftTransferState
        return TransferState(   leftServoCollect= shouldLeftServoCollect,
                                rightServoCollect= shouldRightServoCollect,
                                directorState= directorState)
    }

    fun runTransfer(transferState: TransferState) {
        leftTransferServo.power = transferState.leftServoCollect.power
        rightTransferServo.power = transferState.rightServoCollect.power
        transferDirectorServo.power = transferState.directorState.power
    }

    val alphaDetectionThreshold = 1000
    fun isPixelIn(colorSensor: ColorSensor): Boolean {
        val alpha = colorSensor.alpha()
        telemetry.addLine("alpha: $alpha")

        return alpha > alphaDetectionThreshold
    }

    fun powerExtendo(power: Double) {
        val allowedPower = if (extendoMotorMaster.isOverCurrent) {
            0.0
        } else {
            power
        }

        extendoMotorMaster.power = allowedPower
        extendoMotorSlave.power = allowedPower
    }

    private fun powerExtendoEncoderStops(power: Double) {
        val currentPosition = extendoMotorMaster.currentPosition.toDouble()
        val allowedPower = power
//        if (currentPosition > RobotTwoHardware.ExtendoPositions.Max.position) {
//            power.coerceAtMost(0.0)
//        } else if (currentPosition < RobotTwoHardware.ExtendoPositions.Min.position) {
//            power.coerceAtLeast(0.0)
//        } else {
//            power
//        }

        extendoMotorMaster.power = allowedPower
        extendoMotorSlave.power = allowedPower
    }

//    private fun moveExtendoTowardPosition(targetPosition: Double) {
//        val currentPosition = hardware.extendoMotorMaster.currentPosition.toDouble()
//        val power = hardware.extendoPositionPID.calcPID(targetPosition, currentPosition)
//        powerExtendo(power)
//    }

    data class CollectorState(
            val collectorState: CollectorPowers,
            val extendoPosition: RobotTwoHardware.ExtendoPositions,
            val transferRollersState: TransferState,
            val transferLeftSensorState: TransferHalfState,
            val transferRightSensorState: TransferHalfState,
    )

    private fun getCollectorPowerState(power: Double) = CollectorPowers.entries.firstOrNull { it ->
        power == it.power
    } ?: CollectorPowers.Off

    private fun getDirectorPowerState(power: Double): Collector.DirectorState = Collector.DirectorState.entries.firstOrNull { it ->
        power == it.power
    } ?: Collector.DirectorState.Off

    enum class Side {
        Left,
        Right
    }

    private fun getTransferHalfState(half: Side, previousState: TransferHalfState): TransferHalfState {
        val sensor = when (half) {
            Side.Left -> leftTransferPixelSensor
            Side.Right -> rightTransferPixelSensor
        }
        val isSeeingPixel = isPixelIn(sensor)

        val timeOfSeeingPixelMilis = when {
            !previousState.hasPixelBeenSeen && isSeeingPixel-> System.currentTimeMillis()
            !isSeeingPixel -> 0
            else -> previousState.timeOfSeeingMilis
        }
        return TransferHalfState(isSeeingPixel, timeOfSeeingPixelMilis)
    }

    private val defaultPreviousState = TransferHalfState(false, 0)
    fun getCurrentState(previousState: CollectorState?): CollectorState {
        val collectorPowerState: CollectorPowers = getCollectorPowerState(collectorServo1.power)
        val extendoPosition = RobotTwoHardware.ExtendoPositions.Min
        val transferRollersState = TransferState(
                leftServoCollect = getCollectorPowerState(leftTransferServo.power),
                rightServoCollect = getCollectorPowerState(rightTransferServo.power),
                directorState = getDirectorPowerState(transferDirectorServo.power),
        )
        val transferLeftSensorState = getTransferHalfState(Side.Left, previousState?.transferLeftSensorState ?: defaultPreviousState)
        val transferRightSensorState = getTransferHalfState(Side.Right, previousState?.transferRightSensorState ?: defaultPreviousState)
        return CollectorState(
                collectorState = collectorPowerState,
                extendoPosition = extendoPosition,
                transferRollersState = transferRollersState,
                transferLeftSensorState = transferLeftSensorState,
                transferRightSensorState = transferRightSensorState
        )
    }
}