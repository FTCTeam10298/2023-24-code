package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.ColorSensor
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.pid.PID
import kotlin.math.abs
import kotlin.math.absoluteValue

class CollectorSystem(private val extendoMotorMaster: DcMotorEx,
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

    val maxSafeCurrentAmps = 5.5
    init {
        extendoMotorMaster.setCurrentAlert(maxSafeCurrentAmps, CurrentUnit.AMPS)
    }


    enum class CollectorPowers(val power: Double) {
        Off(0.0),
        Intake(1.0),
        Eject(-1.0),
        DropPurple(0.2)
    }

    enum class DirectorState(val power: Double) {
        Left(-1.0),
        Right(1.0),
        Off(0.0)
    }

    data class TransferState(val leftServoCollect: CollectorPowers, val rightServoCollect: CollectorPowers, val directorState: DirectorState)
    data class TransferHalfState(val hasPixelBeenSeen: Boolean, val timeOfSeeingMilis: Long)

    enum class Side {
        Left,
        Right
    }

    data class CollectorState(
            val collectorState: CollectorPowers,
            val extendoPosition: RobotTwoHardware.ExtendoPositions,
            val transferRollersState: TransferState,
            val transferLeftSensorState: TransferHalfState,
            val transferRightSensorState: TransferHalfState,
    )
//    data class CollectorTargetState(
//            val collectorState: CollectorPowers,
//            val extendoPosition: RobotTwoHardware.ExtendoPositions,
//            val transferRollersState: TransferState
//    )


    val leftEncoderReader = AxonEncoderReader(leftRollerEncoder, 0.0, direction = AxonEncoderReader.Direction.Forward)
    val rightEncoderReader = AxonEncoderReader(rightRollerEncoder, 0.0, direction = AxonEncoderReader.Direction.Forward)

    private val leftFlapTransferReadyAngleDegrees = 32.0
    private val rightFlapTransferReadyAngleDegrees = 90.0
    private val leftFlapKp = 0.43
    private val rightFlapKp = 0.4

    private val acceptablePositionErrorTicks = 50
    fun isExtendoAtPosition(targetPositionTicks: Int): Boolean {
        val currentPositionTicks = extendoMotorMaster.currentPosition
        val positionErrorTicks = targetPositionTicks - currentPositionTicks
        return positionErrorTicks.absoluteValue <= acceptablePositionErrorTicks
    }

    fun isExtendoAllTheWayIn(): Boolean {
        return extendoMotorMaster.currentPosition <= 10
    }
    fun arePixelsAlignedInTransfer(): Boolean {
        val isLeftFlapAngleAcceptable = isFlapAtAngle(getFlapAngleDegrees(leftEncoderReader), leftFlapTransferReadyAngleDegrees, flapAngleToleranceDegrees = 20.0)
        val isRightFlapAngleAcceptable = isFlapAtAngle(getFlapAngleDegrees(rightEncoderReader), rightFlapTransferReadyAngleDegrees, flapAngleToleranceDegrees = 20.0)
        return isLeftFlapAngleAcceptable && isRightFlapAngleAcceptable
    }

    fun moveCollectorAllTheWayIn() {
        if (!isExtendoAllTheWayIn()) {
            powerExtendo(-0.5)
        }
    }

    fun getFlapAngleDegrees(encoderReader: AxonEncoderReader): Double =
            (encoderReader.getPositionDegrees() * 2) % 360

    fun isFlapAtAngle(currentAngleDegrees: Double, angleToCheckDegrees: Double, flapAngleToleranceDegrees: Double = 5.0): Boolean {
        val maxAcceptedAngle = angleToCheckDegrees + flapAngleToleranceDegrees
        val minAcceptedAngle = angleToCheckDegrees - flapAngleToleranceDegrees
        val angleTolerance = (minAcceptedAngle..maxAcceptedAngle)
        return currentAngleDegrees in angleTolerance
    }

    fun getPowerToMoveFlapToAngle(flap: Side, targetAngleDegrees: Double): Double {
        val encoder = when (flap) {
            Side.Left -> leftEncoderReader
            Side.Right -> rightEncoderReader
        }

        val currentAngle = getFlapAngleDegrees(encoder) % 360
        val angleErrorDegrees = currentAngle - targetAngleDegrees

        val proportionalConstant = when (flap) {
            Side.Left -> leftFlapKp
            Side.Right -> rightFlapKp
        }
        val power = abs((angleErrorDegrees / 360) * proportionalConstant)
        telemetry.addLine("$flap roller power: $power, angle: $angleErrorDegrees")

        return power
    }

    fun getCollectorState(driverInput: CollectorPowers): CollectorPowers {
        val bothTransfersAreFull = previousLeftTransferState.hasPixelBeenSeen && previousRightTransferState.hasPixelBeenSeen
        return if (bothTransfersAreFull && (driverInput == CollectorPowers.Intake)) {
            CollectorPowers.Off
        } else {
            driverInput
        }
    }

    fun spinCollector(power: Double) {
        collectorServo1.power = power
        collectorServo2.power = power
    }

    var previousLeftTransferState = TransferHalfState(false, 0)
    var previousRightTransferState = TransferHalfState(false, 0)
    val extraTransferRollingTimeMilis = 1000
    fun getAutoPixelSortState(isCollecting: Boolean): TransferState {
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

    fun runRollers(transferState: TransferState) {
        leftTransferServo.power = getRollerPowerBasedOnState(Side.Left, transferState.leftServoCollect)
        rightTransferServo.power = getRollerPowerBasedOnState(Side.Right, transferState.rightServoCollect)
        transferDirectorServo.power = transferState.directorState.power
    }

    fun getRollerPowerBasedOnState(side: Side, rollerState: CollectorPowers): Double {
        val flapTransferReadyAngleDegrees = when (side) {
            Side.Left -> leftFlapTransferReadyAngleDegrees
            Side.Right -> rightFlapTransferReadyAngleDegrees
        }
        return if (rollerState == CollectorPowers.Off) {
            getPowerToMoveFlapToAngle(side, flapTransferReadyAngleDegrees)
        } else {
            rollerState.power
        }
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

    enum class ExtendoPositions(val ticks: Int) {
        ClearTransfer(230),
        Min(0)
    }
    private val pid = PID(kp = 0.005)
    fun moveExtendoToPosition(targetPositionTicks: Int) {
        val currentPosition = extendoMotorMaster.currentPosition.toDouble()
        val positionError = targetPositionTicks - currentPosition
        val power = pid.calcPID(positionError)
        powerExtendo(power)
    }

    private fun getCollectorPowerState(power: Double) = CollectorPowers.entries.firstOrNull { it ->
        power == it.power
    } ?: CollectorPowers.Off

    private fun getDirectorPowerState(power: Double): CollectorSystem.DirectorState = CollectorSystem.DirectorState.entries.firstOrNull { it ->
        power == it.power
    } ?: CollectorSystem.DirectorState.Off

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