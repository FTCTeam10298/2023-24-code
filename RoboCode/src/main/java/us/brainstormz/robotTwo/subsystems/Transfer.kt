package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.hardware.ColorSensor
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.AxonEncoderReader
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp
import kotlin.math.abs

class Transfer {
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
//    data class CollectorTargetState(
//            val collectorState: CollectorPowers,
//            val extendoPosition: RobotTwoHardware.ExtendoPositions,
//            val transferRollersState: TransferState
//    )


    private val leftFlapTransferReadyAngleDegrees = 20.0
    private val rightFlapTransferReadyAngleDegrees = 305.0
    private val leftFlapKp = 0.3
    private val rightFlapKp = 0.4


    fun getFlapAngleDegrees(side: Side, hardware: RobotTwoHardware): Double {
        val encoderReader = when (side) {
            Side.Left -> AxonEncoderReader(hardware.leftRollerEncoder, 0.0, direction = AxonEncoderReader.Direction.Forward)
            Side.Right -> AxonEncoderReader(hardware.rightRollerEncoder, 0.0, direction = AxonEncoderReader.Direction.Forward)
        }

        return (encoderReader.getPositionDegrees() * 2).mod(360.0)
    }

    fun isFlapAtAngle(currentAngleDegrees: Double, angleToCheckDegrees: Double, flapAngleToleranceDegrees: Double = 5.0): Boolean {
        val maxAcceptedAngle = (angleToCheckDegrees + flapAngleToleranceDegrees).mod(360.0)
        val minAcceptedAngle = (angleToCheckDegrees - flapAngleToleranceDegrees).mod(360.0)
        val angleTolerance = (minAcceptedAngle..maxAcceptedAngle)
        return currentAngleDegrees in angleTolerance
    }

    fun getPowerToMoveFlapToAngle(flap: Side, targetAngleDegrees: Double, currentAngleDegrees: Double): Double {
        val angleErrorDegrees = (currentAngleDegrees - targetAngleDegrees).mod(360.0)

        val proportionalConstant = when (flap) {
            Side.Left -> leftFlapKp
            Side.Right -> rightFlapKp
        }
        val power = abs((angleErrorDegrees / 360) * proportionalConstant)

        return power
    }


    var previousLeftTransferState = TransferHalfState(false, 0)
    var previousRightTransferState = TransferHalfState(false, 0)
    val extraTransferRollingTimeMilis = 0
    fun getAutoPixelSortState(isCollecting: Boolean, actualRobot: ActualRobot): RollerState {
        //Detection:
        val isLeftSeeingPixel = isPixelIn(actualRobot.collectorSystemState.leftTransferState, Side.Left)
        val timeOfSeeingLeftPixelMilis = when {
            !previousLeftTransferState.hasPixelBeenSeen && isLeftSeeingPixel-> System.currentTimeMillis()
            !isLeftSeeingPixel -> 0
            else -> previousLeftTransferState.timeOfSeeingMilis
        }
        val leftTransferState = TransferHalfState(isLeftSeeingPixel, timeOfSeeingLeftPixelMilis)

        val isRightSeeingPixel = isPixelIn(actualRobot.collectorSystemState.rightTransferState, Side.Right)
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

    fun powerSubsystem(transferState: RollerState, hardware: RobotTwoHardware, actualRobot: ActualRobot) {
        hardware.leftTransferServo.power = getRollerPowerBasedOnState(Side.Left, transferState.leftServoCollect, actualRobot)
        hardware.rightTransferServo.power = getRollerPowerBasedOnState(Side.Right, transferState.rightServoCollect, actualRobot)
        hardware.transferDirectorServo.power = transferState.directorState.power
    }

    fun getRollerPowerBasedOnState(side: Side, rollerState: RollerPowers, actualRobot: ActualRobot): Double {
        val flapTransferReadyAngleDegrees = when (side) {
            Side.Left -> leftFlapTransferReadyAngleDegrees
            Side.Right -> rightFlapTransferReadyAngleDegrees
        }
        val actualFlapAngle = when (side) {
            Side.Left -> actualRobot.collectorSystemState.leftRollerAngleDegrees
            Side.Right -> actualRobot.collectorSystemState.rightRollerAngleDegrees
        }
        return if (rollerState == RollerPowers.Off) {
            getPowerToMoveFlapToAngle(side, flapTransferReadyAngleDegrees, actualFlapAngle)
//            0.0
        } else {
            rollerState.power
        }
    }

    val leftAlphaDetectionThreshold = 300
    val rightAlphaDetectionThreshold = 200
    fun isPixelIn(reading: SensorReading, side: Side): Boolean {
        val alpha = reading.alpha

        val alphaDetectionThreshold = when (side) {
            Side.Left -> leftAlphaDetectionThreshold
            Side.Right -> rightAlphaDetectionThreshold
        }

        return alpha > alphaDetectionThreshold
    }
//    fun isPixelIn(colorSensor: ColorSensor, side: Side): Boolean {
//        val alpha = colorSensor.alpha()
//
//        val alphaDetectionThreshold = when (side) {
//            Side.Left -> leftAlphaDetectionThreshold
//            Side.Right -> rightAlphaDetectionThreshold
//        }
//
//        return alpha > alphaDetectionThreshold
//    }

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

//        telemetry.addLine("\n$side side:")
//        telemetry.addLine("rgbValue: $rgbValue")
//        telemetry.addLine("leftSensorColorInformation: $leftSensorColorInformation")

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


//    fun getTransferHalfState(half: Side, previousState: TransferHalfState): TransferHalfState {
//        val sensor = when (half) {
//            Side.Left -> leftTransferPixelSensor
//            Side.Right -> rightTransferPixelSensor
//        }
//        val isSeeingPixel = isPixelIn(getSensorReading(sensor), half)
//
//        val timeOfSeeingPixelMilis = when {
//            !previousState.hasPixelBeenSeen && isSeeingPixel-> System.currentTimeMillis()
////            !isSeeingPixel -> 0
//            else -> previousState.timeOfSeeingMilis
//        }
//        return TransferHalfState(isSeeingPixel, timeOfSeeingPixelMilis)
//    }

    //    private val defaultPreviousState = TransferHalfState(false, 0)
    data class SensorReading(val red: Int, val green: Int, val blue: Int, val alpha: Int)
    fun getSensorReading(sensor: ColorSensor): SensorReading {
        return SensorReading(
                red= sensor.red(),
                green= sensor.green(),
                blue= sensor.blue(),
                alpha = sensor.alpha()
        )
    }
}