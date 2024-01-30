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
        DropPurple(0.2),
        ReverseDropPurple(-0.2)
    }

    enum class RollerPowers(val power: Double) {
        Off(0.0),
        GoToHoldingPosition(0.0),
        Intake(1.0),
        Eject(-1.0),
    }

    enum class DirectorState(val power: Double) {
        Left(-1.0),
        Right(1.0),
        Off(0.0)
    }

    data class RollerState(val leftServoCollect: RollerPowers, val rightServoCollect: RollerPowers, val directorState: DirectorState)
    data class TransferHalfState(val hasPixelBeenSeen: Boolean, val timeOfSeeingMilis: Long)

    enum class Side {
        Left,
        Right
    }

    data class CollectorState(
            val collectorState: CollectorPowers,
            val extendoPosition: ExtendoPositions,
            val transferRollersState: RollerState,
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

    private val leftFlapTransferReadyAngleDegrees = 20.0
    private val rightFlapTransferReadyAngleDegrees = 305.0
    private val leftFlapKp = 0.3
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
            (encoderReader.getPositionDegrees() * 2).mod(360.0)

    fun isFlapAtAngle(currentAngleDegrees: Double, angleToCheckDegrees: Double, flapAngleToleranceDegrees: Double = 5.0): Boolean {
        val maxAcceptedAngle = (angleToCheckDegrees + flapAngleToleranceDegrees).mod(360.0)
        val minAcceptedAngle = (angleToCheckDegrees - flapAngleToleranceDegrees).mod(360.0)
        val angleTolerance = (minAcceptedAngle..maxAcceptedAngle)
        return currentAngleDegrees in angleTolerance
    }

    fun getPowerToMoveFlapToAngle(flap: Side, targetAngleDegrees: Double): Double {
        val encoder = when (flap) {
            Side.Left -> leftEncoderReader
            Side.Right -> rightEncoderReader
        }

        val currentAngle = getFlapAngleDegrees(encoder)
        val angleErrorDegrees = (currentAngle - targetAngleDegrees).mod(360.0)

        val proportionalConstant = when (flap) {
            Side.Left -> leftFlapKp
            Side.Right -> rightFlapKp
        }
        val power = abs((angleErrorDegrees / 360) * proportionalConstant)

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
    val extraTransferRollingTimeMilis = 0
    fun getAutoPixelSortState(isCollecting: Boolean): RollerState {
        //Detection:
        val isLeftSeeingPixel = isPixelIn(leftTransferPixelSensor, Side.Left)
        val timeOfSeeingLeftPixelMilis = when {
            !previousLeftTransferState.hasPixelBeenSeen && isLeftSeeingPixel-> System.currentTimeMillis()
            !isLeftSeeingPixel -> 0
            else -> previousLeftTransferState.timeOfSeeingMilis
        }
        val leftTransferState = TransferHalfState(isLeftSeeingPixel, timeOfSeeingLeftPixelMilis)

        val isRightSeeingPixel = isPixelIn(rightTransferPixelSensor, Side.Right)
        val timeOfSeeingRightPixelMilis = when {
            !previousRightTransferState.hasPixelBeenSeen && isRightSeeingPixel-> System.currentTimeMillis()
            !isRightSeeingPixel -> 0
            else -> previousRightTransferState.timeOfSeeingMilis
        }
        val rightTransferState = TransferHalfState(isRightSeeingPixel, timeOfSeeingRightPixelMilis)


        //Should collect
        val timeSinceLeftSeen = System.currentTimeMillis() - leftTransferState.timeOfSeeingMilis
        val shouldLeftServoCollect = when {
            (!leftTransferState.hasPixelBeenSeen && isCollecting) || timeSinceLeftSeen < extraTransferRollingTimeMilis -> RollerPowers.Intake
            else -> RollerPowers.Off
        }
        val timeSinceRightSeen = System.currentTimeMillis() - rightTransferState.timeOfSeeingMilis
        val shouldRightServoCollect = when {
            !leftTransferState.hasPixelBeenSeen -> RollerPowers.Off
            (!rightTransferState.hasPixelBeenSeen && isCollecting) || timeSinceRightSeen < extraTransferRollingTimeMilis -> RollerPowers.Intake
            else -> RollerPowers.Off
        }

        val directorState = when {
            !isCollecting -> DirectorState.Off
            !leftTransferState.hasPixelBeenSeen -> DirectorState.Left
            !rightTransferState.hasPixelBeenSeen -> DirectorState.Right
            else -> DirectorState.Off
        }

        previousRightTransferState = rightTransferState
        previousLeftTransferState = leftTransferState
        return RollerState(   leftServoCollect= shouldLeftServoCollect,
                                rightServoCollect= shouldRightServoCollect,
                                directorState= directorState)
    }

    fun runRollers(transferState: RollerState) {
        leftTransferServo.power = getRollerPowerBasedOnState(Side.Left, transferState.leftServoCollect)
        rightTransferServo.power = getRollerPowerBasedOnState(Side.Right, transferState.rightServoCollect)
        transferDirectorServo.power = transferState.directorState.power
    }

    fun getRollerPowerBasedOnState(side: Side, rollerState: RollerPowers): Double {
        val flapTransferReadyAngleDegrees = when (side) {
            Side.Left -> leftFlapTransferReadyAngleDegrees
            Side.Right -> rightFlapTransferReadyAngleDegrees
        }
        return if (rollerState == RollerPowers.Off) {
            getPowerToMoveFlapToAngle(side, flapTransferReadyAngleDegrees)
//            0.0
        } else {
            rollerState.power
        }
    }

    val leftAlphaDetectionThreshold = 800
    val rightAlphaDetectionThreshold = 600
    fun isPixelIn(colorSensor: ColorSensor, side: Side): Boolean {
        val alpha = colorSensor.alpha()

        val alphaDetectionThreshold = when (side) {
            Side.Left -> leftAlphaDetectionThreshold
            Side.Right -> rightAlphaDetectionThreshold
        }

        return alpha > alphaDetectionThreshold
    }

    data class RGBValue(val red: Double, val green: Double, val blue: Double) {
        constructor(red: Int, green: Int, blue: Int): this(red.toDouble(), green.toDouble(), blue.toDouble())
    }
    data class RGBRange(val low: RGBValue, val high: RGBValue) {
        enum class RBGColor {
            Red,
            Green,
            Blue
        }
        fun getRangeForColor(color: RBGColor): ClosedRange<Double> =
                when (color) {
                    RBGColor.Red -> low.red..high.red
                    RBGColor.Green -> low.green..high.green
                    RBGColor.Blue -> low.blue..high.blue
                }
        fun contains(color: RGBValue): Boolean {
            val isRedGood = color.red in getRangeForColor(RBGColor.Red)
            val isGreenGood = color.green in getRangeForColor(RBGColor.Green)
            val isBlueGood = color.blue in getRangeForColor(RBGColor.Blue)
            return isRedGood && isGreenGood && isBlueGood
        }
    }
    data class SensorColorInformation(val purple: RGBRange, val green: RGBRange, val white: RGBRange, val yellow: RGBRange) {
        fun mappedValues(): Map<RobotTwoTeleOp.PixelColor, RGBRange> {
            return mapOf(
                    RobotTwoTeleOp.PixelColor.Purple to purple,
                    RobotTwoTeleOp.PixelColor.Green to green,
                    RobotTwoTeleOp.PixelColor.White to white,
                    RobotTwoTeleOp.PixelColor.Yellow to yellow
            )
        }
    }

    val leftSensorColorInformation = SensorColorInformation(
            purple = RGBRange(
                    RGBValue(76, 2, 171),
                    RGBValue(198, 73, 247)
            ),
            green = RGBRange(
                    RGBValue(2, 207, 2),
                    RGBValue(66, 250, 151)
            ),
            white = RGBRange(
                    RGBValue(199, 199, 199),
                    RGBValue(225, 225, 225)
            ),
            yellow = RGBRange(
                    RGBValue(217, 128, 0),
                    RGBValue(225, 225, 128)
            ),
    )
    val rightSensorColorInformation = leftSensorColorInformation//SensorColorInformation()

    fun getColorInSide(colorSensor: ColorSensor, side: Side): RobotTwoTeleOp.PixelColor {
        val sensorMultiplier: Double = (1.0/8.0)

        val rgbValue = RGBValue(colorSensor.red() *sensorMultiplier, colorSensor.green() *sensorMultiplier, colorSensor.blue() *sensorMultiplier)
        val findWhichColor = leftSensorColorInformation.mappedValues().entries.firstOrNull {entry ->
            entry.value.contains(rgbValue)
        }

        telemetry.addLine("\n$side side:")
        telemetry.addLine("rgbValue: $rgbValue")
        telemetry.addLine("leftSensorColorInformation: $leftSensorColorInformation")

        return findWhichColor?.key ?: RobotTwoTeleOp.PixelColor.Unknown
//        return when {
//            (red + blue) < green -> RobotTwoTeleOp.PixelWeWant.Green
//            red > 2500 && green > 2500 && blue > 2500 -> RobotTwoTeleOp.PixelWeWant.White
//            green > ((blue*2) - 50) && red > green -> RobotTwoTeleOp.PixelWeWant.Yellow
//            blue > green && blue > red && green > red -> RobotTwoTeleOp.PixelWeWant.Purple
//            else -> RobotTwoTeleOp.PixelWeWant.Unknown
//        }
//        return alpha > alphaDetectionThreshold
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
        AllTheWayInTarget(-10),
        Min(0),
        Manual(0),
        ClearTransfer(230),
        CloserBackboardPixelPosition(500),
        MidBackboardPixelPosition(1000),
        FarBackboardPixelPosition(1750),
        Max(2000),
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

    private fun getRollerPowerState(power: Double) = RollerPowers.entries.firstOrNull { it ->
        power == it.power
    } ?: RollerPowers.Off

    private fun getDirectorPowerState(power: Double): CollectorSystem.DirectorState = CollectorSystem.DirectorState.entries.firstOrNull { it ->
        power == it.power
    } ?: CollectorSystem.DirectorState.Off

    private fun getTransferHalfState(half: Side, previousState: TransferHalfState): TransferHalfState {
        val sensor = when (half) {
            Side.Left -> leftTransferPixelSensor
            Side.Right -> rightTransferPixelSensor
        }
        val isSeeingPixel = isPixelIn(sensor, half)

        val timeOfSeeingPixelMilis = when {
            !previousState.hasPixelBeenSeen && isSeeingPixel-> System.currentTimeMillis()
//            !isSeeingPixel -> 0
            else -> previousState.timeOfSeeingMilis
        }
        return TransferHalfState(isSeeingPixel, timeOfSeeingPixelMilis)
    }

    private val defaultPreviousState = TransferHalfState(false, 0)
    fun getCurrentState(previousState: CollectorState?): CollectorState {
        val collectorPowerState: CollectorPowers = getCollectorPowerState(collectorServo1.power)
        val extendoPosition = ExtendoPositions.Min
        val transferRollersState = RollerState(
                leftServoCollect = getRollerPowerState(leftTransferServo.power),
                rightServoCollect = getRollerPowerState(rightTransferServo.power),
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