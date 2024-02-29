package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.hardware.ColorSensor
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
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

    enum class Side {
        Left,
        Right
    }

    private val leftFlapTransferReadyAngleDegrees = 20.0
    private val rightFlapTransferReadyAngleDegrees = 305.0
    private val leftFlapKp = 0.22
    private val rightFlapKp = 0.22


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

    data class ActualTransferHalf(val upperSensor: SensorReading, val lowerSensor: SensorReading)
    data class ActualTransfer(val left: ActualTransferHalf, val right: ActualTransferHalf)

    fun getActualTransfer(hardware: RobotTwoHardware): ActualTransfer {
        return ActualTransfer(
                left = ActualTransferHalf(
                        upperSensor = getSensorReading(hardware.leftTransferSensor),
                        lowerSensor = getSensorReading(hardware.leftTransferSensor),
                ),
                right = ActualTransferHalf(
                        upperSensor = getSensorReading(hardware.rightTransferSensor),
                        lowerSensor = getSensorReading(hardware.rightTransferSensor),
                )
        )
    }


    data class SensorState(val hasPixelBeenSeen: Boolean, val timeOfSeeingMilis: Long)
    data class TransferHalfState(val upperSensor: SensorState, val lowerSensor: SensorState)
    data class TransferState(val left: TransferHalfState, val right: TransferHalfState)

    fun getTransferState(actualWorld: ActualWorld, previousTransferState: TransferState): TransferState {
        val timestampMilis = actualWorld.timestampMilis

        fun getSensorState(actualReading: SensorReading, previousSensorState: SensorState): SensorState {
            val isSeeingPixel = isPixelIn(actualReading)
            return if (isSeeingPixel) {
                SensorState(hasPixelBeenSeen = isSeeingPixel, timeOfSeeingMilis = timestampMilis)
            } else {
                previousSensorState
            }
        }

        fun getTransferHalfState(actualTransferHalfState: ActualTransferHalf, previousTransferHalfState: TransferHalfState): TransferHalfState {
            return TransferHalfState(
                    upperSensor = getSensorState(actualTransferHalfState.upperSensor, previousTransferHalfState.upperSensor),
                    lowerSensor = getSensorState(actualTransferHalfState.lowerSensor, previousTransferHalfState.lowerSensor)
            )
        }

        return TransferState(
                left = getTransferHalfState(actualWorld.actualRobot.collectorSystemState.transferState.left, previousTransferState.left),
                right = getTransferHalfState(actualWorld.actualRobot.collectorSystemState.transferState.right, previousTransferState.right)
        )
    }


    data class TransferTarget(val leftServoCollect: RollerPowers, val rightServoCollect: RollerPowers, val directorState: DirectorState)

    fun getTransferHalfSortingTarget(
            isCollecting: Boolean,
            actualTransferHalfState: TransferHalfState,
//            previousTransferHalfState: TransferHalfState,
            previousRollerTarget: RollerPowers
            ): RollerPowers {
        val upperSensorHasBeenSeen = actualTransferHalfState.upperSensor.hasPixelBeenSeen
        val lowerSensorHasBeenSeen = actualTransferHalfState.lowerSensor.hasPixelBeenSeen
        return if (isCollecting) {
            if (upperSensorHasBeenSeen) {
                RollerPowers.Off
            } else {
                if (lowerSensorHasBeenSeen) {
                    RollerPowers.Off
                } else {
                    RollerPowers.Intake
                }
            }
        } else {
            when {
                lowerSensorHasBeenSeen -> {
                    RollerPowers.Intake
                }
                upperSensorHasBeenSeen -> {
                    RollerPowers.Off
                }
                else -> {
                    previousRollerTarget
                }
            }
        }
    }

    fun getTransferSortingTarget(
            isCollecting: Boolean,
            actualTransferState: TransferState,
//            previousTransferState: TransferState,
            previousTransferTarget: TransferTarget): TransferTarget {

        val leftServoTarget = getTransferHalfSortingTarget(
                isCollecting= isCollecting,
                actualTransferHalfState = actualTransferState.left,
//                previousTransferHalfState = previousTransferState.left,
                previousRollerTarget = previousTransferTarget.leftServoCollect,
        )
        val rightServoTarget = getTransferHalfSortingTarget(
                isCollecting= isCollecting,
                actualTransferHalfState = actualTransferState.right,
//                previousTransferHalfState = actualTransferState.right,
                previousRollerTarget = previousTransferTarget.rightServoCollect,
        )

        return TransferTarget(
                leftServoCollect = leftServoTarget,
                rightServoCollect = rightServoTarget,
                directorState =
                if (leftServoTarget == RollerPowers.Intake) {
                    DirectorState.Left
                } else {
                    DirectorState.Right
                },
        )
    }

    fun powerSubsystem(transferState: TransferTarget, hardware: RobotTwoHardware, actualRobot: ActualRobot) {
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
//        return if (rollerState == RollerPowers.Off) {
//            getPowerToMoveFlapToAngle(side, flapTransferReadyAngleDegrees, actualFlapAngle)
////            0.0
//        } else {
//            rollerState.power
//        }
        return rollerState.power
    }

    val nothingReading = SensorReading(red= 73, green= 115, blue= 158, alpha= 324)
    fun isPixelIn(reading: SensorReading): Boolean {
        val doesEachColorChannelPass = reading.asList.mapIndexed {i, it ->
            it > (nothingReading.asList[i] * 2)
        }

        val numberOfPassingChannels = doesEachColorChannelPass.fold(0) {acc, it ->
            if (it) {
                acc + 1
            } else
                acc
        }

        return numberOfPassingChannels >= 3
    }

//    val rightWhitePixelReading = SensorReading(red = 624, green= 619, blue= 539, alpha= 1717)//(red= 1113, green= 1068, blue= 967, alpha= 3014)
//    val leftWhitePixelReading = SensorReading(red = 4290, green= 4992, blue= 4262, alpha= 10240)//(red= 9364, green= 10240, blue= 9907, alpha= 10240)
//    private val rgbChannelRange: IntRange = 0..255
//    private fun getRGBFromSensorReading(reading: SensorReading, whiteReading: SensorReading): RGBValue {
//        val conversionFactors = whiteReading.rgbAsMap.map { (color, colorChannelOfWhite) ->
//            //e.g. readingToRGBCoversion = red= 9364 * red= 255
//            //9364 * x = 225
//            //x = 255/9364
//            val conversionFactor = (rgbChannelRange.last)/colorChannelOfWhite.toDouble()
//
//            color to conversionFactor
//        }.toMap()
//
//        val convertedChannels = reading.rgbAsMap.map {(color, colorChannelOfReading) ->
//            val conversionFactorForThisChannel = conversionFactors[color]!!
//            val convertedChannel = colorChannelOfReading * conversionFactorForThisChannel
//            color to convertedChannel
//        }.toMap()
//
//        val rangeLimited = convertedChannels.map {(color, colorChannelOfReading) ->
//            color to colorChannelOfReading.coerceIn(rgbChannelRange.first.toDouble(), rgbChannelRange.last.toDouble())
//        }.toMap()
//
//        return RGBValue(rangeLimited[RGB.Red]!!, rangeLimited[RGB.Green]!!, rangeLimited[RGB.Blue]!!)
//    }
//
//    fun findColorFromReading(reading: SensorReading, side: Side): RobotTwoTeleOp.PixelColor {
//        val whiteReading = when (side) {
//            Side.Left -> leftWhitePixelReading
//            Side.Right -> rightWhitePixelReading
//        }
//        val readingAsRGBColor = getRGBFromSensorReading(reading, whiteReading)
//        println("readingAsRGBColor: $readingAsRGBColor")
//
//        val allPossibleColors = pixelColorsToRGB.asMap.filter { (pixelColor, rgbRange) ->
//            rgbRange.contains(readingAsRGBColor)
//        }
//        val pixelColor = allPossibleColors.keys.firstOrNull() ?: RobotTwoTeleOp.PixelColor.Unknown
//        return pixelColor
//    }
//
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
//
//    data class RGBValue(val red: Double, val green: Double, val blue: Double) {
//        constructor(red: Int, green: Int, blue: Int): this(red.toDouble(), green.toDouble(), blue.toDouble())
//        val asMap = mapOf(RGB.Red to red, RGB.Green to green, RGB.Blue to blue)
//    }
//
//    data class RGBRange(val low: RGBValue, val high: RGBValue) {
//        fun getRangeForColor(color: RGB): ClosedRange<Double> = low.asMap[color]!!..high.asMap[color]!!
////                when (color) {
////                    RGB.Red -> low.red..high.red
////                    RGB.Green -> low.green..high.green
////                    RGB.Blue -> low.blue..high.blue
////                }
//
//        fun contains(color: RGBValue): Boolean {
//            return color.asMap.toList().fold(true) {acc, (color, value) ->
//                val rangeForColor = getRangeForColor(color)
//                val colorIsInRange = rangeForColor.contains(value)
//                println("value: $value in rangeForColor: $rangeForColor is $colorIsInRange")
//
//                acc && colorIsInRange
//            }
//        }
//    }
//
//    data class PixelColorsToRGB(val purple: RGBRange, val green: RGBRange, val white: RGBRange, val yellow: RGBRange) {
//        val asMap: Map<RobotTwoTeleOp.PixelColor, RGBRange> = mapOf(
//                        RobotTwoTeleOp.PixelColor.Purple to purple,
//                        RobotTwoTeleOp.PixelColor.Green to green,
//                        RobotTwoTeleOp.PixelColor.White to white,
//                        RobotTwoTeleOp.PixelColor.Yellow to yellow
//        )
//    }
//
//    private val pixelColorsToRGB = PixelColorsToRGB(
//            purple = RGBRange(
//                    RGBValue(76, 2, 171),
//                    RGBValue(198, 73, 247)
//            ),
//            green = RGBRange(
//                    RGBValue(2, 207, 2),
//                    RGBValue(66, 250, 151)
//            ),
//            white = RGBRange(
//                    RGBValue(140,140, 140),
//                    RGBValue(255, 255, 255)
//            ),
//            yellow = RGBRange(
//                    RGBValue(217, 128, 0),
//                    RGBValue(255, 255, 128)
//            ),
//    )
}

//fun main() {
//    val transfer = Transfer(PrintlnTelemetry())
//
//    val actualRightReadings = Transfer.Side.Right to mapOf(
//            RobotTwoTeleOp.PixelColor.White to Transfer.SensorReading(red = 624, green= 619, blue= 539, alpha= 1717),
//    )
//
//    val actualLeftReadings = Transfer.Side.Left to mapOf(
//            RobotTwoTeleOp.PixelColor.White to Transfer.SensorReading(red = 4290, green= 4992, blue= 4262, alpha= 10240),
//            RobotTwoTeleOp.PixelColor.Purple to Transfer.SensorReading(red = 9364, green= 10240, blue= 9907, alpha= 10240)
//    )
//
//    val allTests = listOf(actualRightReadings, actualLeftReadings)
//    val resultOfAllTests = allTests.fold(true) {acc, (side, map) ->
//        acc && map.toList().fold(true) {acc, (expectedPixelColor, inputReading) ->
//            println("\n\ninputReading: $inputReading")
//            println("side: $side")
//            println("expectedPixelColor: $expectedPixelColor\n")
//
//            val outputPixelColor = transfer.findColorFromReading(inputReading, side)
//            println("\noutputPixelColor: $outputPixelColor")
//
//            val pixelWasIdentifiedCorrectly = expectedPixelColor == outputPixelColor
//            println("\npixelWasIdentifiedCorrectly: $pixelWasIdentifiedCorrectly")
//
//            acc && pixelWasIdentifiedCorrectly
//        }
//    }
//    println("\n\nresultOfAllTests: $resultOfAllTests")
//}