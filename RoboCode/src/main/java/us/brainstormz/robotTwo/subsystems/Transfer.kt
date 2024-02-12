package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.hardware.ColorSensor
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.AxonEncoderReader
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp
import kotlin.math.abs

class Transfer(private val telemetry: Telemetry) {
    enum class RollerPowers(val power: Double) {
        Off(0.0),
        GoToHoldingPosition(0.0),
        Intake(1.0),
        Eject(-1.0),
    }

    enum class DirectorState(val power: Double) {
        Left(1.0),
        Right(-1.0),
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
    private val leftFlapKp = 0.22
    private val rightFlapKp = 0.31


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

    val leftAlphaDetectionThreshold = 600
    val rightAlphaDetectionThreshold = 600
    fun isPixelIn(reading: SensorReading, side: Side): Boolean {
        telemetry.addLine("$side reading: $reading")
        val alpha = reading.alpha

        val alphaDetectionThreshold = when (side) {
            Side.Left -> leftAlphaDetectionThreshold
            Side.Right -> rightAlphaDetectionThreshold
        }

        return alpha > alphaDetectionThreshold
    }

    val rightWhiteReading = SensorReading(red= 1113, green= 1068, blue= 967, alpha= 3014)
    val leftWhiteReading = SensorReading(red= 9364, green= 10240, blue= 9907, alpha= 10240)
    private val rgbChannelRange: IntRange = 0..255
//    private val whiteRGB = RGBValue(255, 255, 255)
    private fun getRGBFromSensorReading(reading: SensorReading, whiteReading: SensorReading): RGBValue {
        val conversionFactors = whiteReading.rgbAsMap.map { (color, colorChannelOfWhite) ->
            //e.g. readingToRGBCoversion = red= 9364 * red= 255
            //9364 * x = 225
            //x = 255/9364
            val conversionFactor = (255.0)/colorChannelOfWhite.toDouble()

            color to conversionFactor
        }.toMap()

        val convertedChannels = reading.rgbAsMap.map {(color, colorChannelOfReading) ->
            val conversionFactorForThisChannel = conversionFactors[color]!!
            val convertedChannel = colorChannelOfReading * conversionFactorForThisChannel
            color to convertedChannel
        }.toMap()

        val rangeLimited = convertedChannels.map {(color, colorChannelOfReading) ->
            color to colorChannelOfReading.coerceIn(rgbChannelRange.first.toDouble(), rgbChannelRange.last.toDouble())
        }.toMap()

        return RGBValue(rangeLimited[RGB.Red]!!, rangeLimited[RGB.Green]!!, rangeLimited[RGB.Blue]!!)
    }

    fun findColorFromReading(reading: SensorReading, side: Side): RobotTwoTeleOp.PixelColor {
        val whiteReading = when (side) {
            Side.Left -> leftWhiteReading
            Side.Right -> rightWhiteReading
        }
        val readingAsRGBColor = getRGBFromSensorReading(reading, whiteReading)
        println("readingAsRGBColor: $readingAsRGBColor")

        val allPossibleColors = pixelColorsToRGB.asMap.filter { (pixelColor, rgbRange) ->
            rgbRange.contains(readingAsRGBColor)
        }
        val pixelColor = allPossibleColors.keys.firstOrNull() ?: RobotTwoTeleOp.PixelColor.Unknown
        return pixelColor
    }

    enum class RGB {
        Red,
        Green,
        Blue
    }

    data class SensorReading(val red: Int, val green: Int, val blue: Int, val alpha: Int) {
        val asList = listOf(red, green, blue, alpha)
        val rgbAsMap = mapOf(RGB.Red to red, RGB.Green to green, RGB.Blue to blue)
    }
    fun getSensorReading(sensor: ColorSensor): SensorReading {
        return SensorReading(
                red= sensor.red(),
                green= sensor.green(),
                blue= sensor.blue(),
                alpha = sensor.alpha()
        )
    }

    data class RGBValue(val red: Double, val green: Double, val blue: Double) {
        constructor(red: Int, green: Int, blue: Int): this(red.toDouble(), green.toDouble(), blue.toDouble())
        val asMap = mapOf(RGB.Red to red, RGB.Green to green, RGB.Blue to blue)
    }

    data class RGBRange(val low: RGBValue, val high: RGBValue) {
        fun getRangeForColor(color: RGB): ClosedRange<Double> = low.asMap[color]!!..high.asMap[color]!!
//                when (color) {
//                    RGB.Red -> low.red..high.red
//                    RGB.Green -> low.green..high.green
//                    RGB.Blue -> low.blue..high.blue
//                }

        fun contains(color: RGBValue): Boolean {
            return color.asMap.toList().fold(true) {acc, (color, value) ->
                val rangeForColor = getRangeForColor(color)
                val colorIsInRange = rangeForColor.contains(value)
                println("value: $value in rangeForColor: $rangeForColor is $colorIsInRange")

                acc && colorIsInRange
            }
        }
    }

    data class PixelColorsToRGB(val purple: RGBRange, val green: RGBRange, val white: RGBRange, val yellow: RGBRange) {
        val asMap: Map<RobotTwoTeleOp.PixelColor, RGBRange> = mapOf(
                        RobotTwoTeleOp.PixelColor.Purple to purple,
                        RobotTwoTeleOp.PixelColor.Green to green,
                        RobotTwoTeleOp.PixelColor.White to white,
                        RobotTwoTeleOp.PixelColor.Yellow to yellow
        )
    }

    private val pixelColorsToRGB = PixelColorsToRGB(
            purple = RGBRange(
                    RGBValue(76, 2, 171),
                    RGBValue(198, 73, 247)
            ),
            green = RGBRange(
                    RGBValue(2, 207, 2),
                    RGBValue(66, 250, 151)
            ),
            white = RGBRange(
                    RGBValue(140,140, 140),
                    RGBValue(255, 255, 255)
            ),
            yellow = RGBRange(
                    RGBValue(217, 128, 0),
                    RGBValue(255, 255, 128)
            ),
    )
}

fun main() {
    val transfer = Transfer(PrintlnTelemetry())

    val actualRightReadings = Transfer.Side.Right to mapOf(
            RobotTwoTeleOp.PixelColor.White to Transfer.SensorReading(red = 624, green= 619, blue= 539, alpha= 1717)
    )

    val actualLeftReadings = Transfer.Side.Right to mapOf(
            RobotTwoTeleOp.PixelColor.White to Transfer.SensorReading(red = 4290, green= 4992, blue= 4262, alpha= 10240)
    )

    val allTests = listOf(actualRightReadings, actualLeftReadings)
    val resultOfAllTests = allTests.fold(true) {acc, (side, map) ->
        acc && map.toList().fold(true) {acc, (expectedPixelColor, inputReading) ->
            println("\n\ninputReading: $inputReading")
            println("side: $side")
            println("expectedPixelColor: $expectedPixelColor\n")

            val outputPixelColor = transfer.findColorFromReading(inputReading, side)
            println("\noutputPixelColor: $outputPixelColor")

            val pixelWasIdentifiedCorrectly = expectedPixelColor == outputPixelColor
            println("\npixelWasIdentifiedCorrectly: $pixelWasIdentifiedCorrectly")

            acc && pixelWasIdentifiedCorrectly
        }
    }
    println("\n\nresultOfAllTests: $resultOfAllTests")
}