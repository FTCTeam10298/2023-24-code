package us.brainstormz.robotTwo.subsystems

import com.qualcomm.robotcore.hardware.NormalizedColorSensor
import kotlinx.serialization.Serializable
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.utils.measured
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.Side

@Serializable
data class ColorReading(val red: Float, val green: Float, val blue: Float, val alpha: Float) {
    val asList = listOf(red, green, blue, alpha)
}
fun readColor(sensor: NormalizedColorSensor): ColorReading = measured("read-color"){
    val normalized = sensor
    val c = normalized.normalizedColors
    println("Read color from a ${sensor.javaClass}")
    ColorReading(
        red= c.red,
        green= c.green,
        blue= c.blue,
        alpha = c.alpha,
    )
}

class Transfer(private val telemetry: Telemetry) {
    enum class LatchPositions(val position: Double) {
        Closed(0.0),
        Open(0.35),
    }

    fun checkIfPixelIsTransferred(transferHalfState: SensorState): Boolean {
        return transferHalfState.hasPixelBeenSeen
    }

    @Serializable
    data class ActualTransfer(val left: ColorReading, val right: ColorReading)

    fun getActualTransfer(hardware: RobotTwoHardware): ActualTransfer {
        return ActualTransfer(
                left = hardware.leftTransferLowerSensorWrapped.read(),
                right = hardware.rightTransferLowerSensorWrapped.read(),
        )
    }

    fun timestampTransferTargets(timestampMillis: Long, transferTarget: TransferTarget, previousTransferTarget: TransferTarget): TransferTarget {
        return TransferTarget(
                left = getLatchTarget(Side.Left, timestampMillis, transferTarget, previousTransferTarget),
                right = getLatchTarget(Side.Right, timestampMillis, transferTarget, previousTransferTarget),
        )
    }
    fun getLatchTarget(side: Side, timestampMillis: Long, targetPosition: TransferTarget, previousTransferTarget: TransferTarget): LatchTarget {
        val previousLatchTarget = previousTransferTarget.getBySide(side)

        val latchTarget = targetPosition.getBySide(side)

        return latchTarget.copy(
                timeTargetChangedMillis= if (latchTarget.target != previousLatchTarget.target) {
                    timestampMillis
                } else {
                    previousLatchTarget.timeTargetChangedMillis
                }
        )
    }

    val timeSinceTargetChangeToAchieveTargetMillis = 500
    fun checkIfLatchHasActuallyAchievedTarget(side: Side, latchTarget: LatchPositions, timestampMilis: Long, previousTransferTarget: TransferTarget): Boolean {
        val previousLatchTarget = previousTransferTarget.getBySide(side)

        val timeSinceTargetChangeMillis = timestampMilis - previousLatchTarget.timeTargetChangedMillis
        val beenEnoughTimeSinceTargetChange = timeSinceTargetChangeMillis >= timeSinceTargetChangeToAchieveTargetMillis

        val targetMatches = latchTarget == previousLatchTarget.target
        return targetMatches && beenEnoughTimeSinceTargetChange
    }

    @Serializable
    data class SensorState(val hasPixelBeenSeen: Boolean, val timeOfSeeingMillis: Long)
    @Serializable
    data class TransferSensorState(
            override val left: SensorState,
            override val right: SensorState): Side.ThingWithSides<SensorState>

    fun getTransferState(actualWorld: ActualWorld, previousTransferState: TransferSensorState): TransferSensorState {

        fun getSensorState(actualReading: ColorReading, previousSensorState: SensorState): SensorState {
            val isSeeingPixel = isPixelIn(actualReading)
            val timeOfSeeingRightPixelMilis = when {
                !previousSensorState.hasPixelBeenSeen && isSeeingPixel -> actualWorld.timestampMilis
                !isSeeingPixel -> 0
                else -> previousSensorState.timeOfSeeingMillis
            }
            return SensorState(isSeeingPixel, timeOfSeeingRightPixelMilis)
        }

        return TransferSensorState(
                left = getSensorState(actualWorld.actualRobot.collectorSystemState.transferState.left, previousTransferState.left),
                right = getSensorState(actualWorld.actualRobot.collectorSystemState.transferState.right, previousTransferState.right)
        )
    }

    @Serializable
    data class LatchTarget(val target: LatchPositions, val timeTargetChangedMillis: Long)
    @Serializable
    data class TransferTarget(
            override val left: LatchTarget,
            override val right: LatchTarget): Side.ThingWithSides<LatchTarget>

    fun powerSubsystem(transferState: TransferTarget, hardware: RobotTwoHardware, actualRobot: ActualRobot) {
        hardware.leftTransferServo.power = transferState.left.target.position
        hardware.rightTransferServo.power = transferState.right.target.position
    }

    /*
    No Pixel
    03-07 08:17:27.104  9258  9416 I System.out: multiple ColorReading(red=72, green=117, blue=145, alpha=336)
03-07 08:17:27.104  9258  9416 I System.out: single ColorReading(red=1, green=2, blue=3, alpha=8)
03-07 08:17:27.104  9258  9416 I System.out: normalized OtherColorReading(red=0.0070312503, green=0.011425781, blue=0.014160156, alpha=0.032812502)
03-07 08:17:27.191  9258  9416 I System.out: multiple ColorReading(red=72, green=123, blue=150, alpha=347)
03-07 08:17:27.192  9258  9416 I System.out: single ColorReading(red=1, green=3, blue=3, alpha=8)
03-07 08:17:27.192  9258  9416 I System.out: normalized OtherColorReading(red=0.0070312503, green=0.012011719, blue=0.0146484375, alpha=0.03388672)
03-07 08:17:27.219  9258  9416 I System.out: multiple ColorReading(red=72, green=117, blue=145, alpha=336)
03-07 08:17:27.219  9258  9416 I System.out: single ColorReading(red=1, green=2, blue=3, alpha=8)
03-07 08:17:27.219  9258  9416 I System.out: normalized OtherColorReading(red=0.0070312503, green=0.011425781, blue=0.014160156, alpha=0.032812502)
03-07 08:17:27.303  9258  9416 I System.out: multiple ColorReading(red=72, green=123, blue=150, alpha=347)
03-07 08:17:27.303  9258  9416 I System.out: single ColorReading(red=1, green=3, blue=3, alpha=8)
03-07 08:17:27.303  9258  9416 I System.out: normalized OtherColorReading(red=0.0070312503, green=0.012011719, blue=0.0146484375, alpha=0.03388672)
03-07 08:17:27.330  9258  9416 I System.out: multiple ColorReading(red=72, green=117, blue=145, alpha=336)
03-07 08:17:27.330  9258  9416 I System.out: single ColorReading(red=1, green=2, blue=3, alpha=8)
03-07 08:17:27.330  9258  9416 I System.out: normalized OtherColorReading(red=0.0070312503, green=0.011425781, blue=0.014160156, alpha=0.032812502)
03-07 08:17:27.446  9258  9416 I System.out: multiple ColorReading(red=72, green=123, blue=150, alpha=347)
03-07 08:17:27.446  9258  9416 I System.out: single ColorReading(red=1, green=3, blue=3, alpha=8)
03-07 08:17:27.446  9258  9416 I System.out: normalized OtherColorReading(red=0.0070312503, green=0.012011719, blue=0.0146484375, alpha=0.03388672)
03-07 08:17:27.474  9258  9416 I System.out: multiple ColorReading(red=72, green=117, blue=145, alpha=336)

2 white pixels

03-07 08:18:45.661  9258  9416 I System.out: single ColorReading(red=30, green=37, blue=30, alpha=97)
03-07 08:18:45.661  9258  9416 I System.out: normalized OtherColorReading(red=0.12011719, green=0.1459961, blue=0.11816406, alpha=0.38242188)
03-07 08:18:45.686  9258  9416 I System.out: multiple ColorReading(red=1297, green=1586, blue=1305, alpha=4187)
03-07 08:18:45.686  9258  9416 I System.out: single ColorReading(red=32, green=39, blue=32, alpha=104)
03-07 08:18:45.686  9258  9416 I System.out: normalized OtherColorReading(red=0.12666015, green=0.15488282, blue=0.1274414, alpha=0.40888673)
03-07 08:18:45.755  9258  9416 I System.out: multiple ColorReading(red=1231, green=1495, blue=1211, alpha=3917)
03-07 08:18:45.755  9258  9416 I System.out: single ColorReading(red=30, green=37, blue=30, alpha=97)
03-07 08:18:45.755  9258  9416 I System.out: normalized OtherColorReading(red=0.12001953, green=0.14589845, blue=0.11816406, alpha=0.38222656)
03-07 08:18:45.780  9258  9416 I System.out: multiple ColorReading(red=1297, green=1586, blue=1306, alpha=4187)
03-07 08:18:45.780  9258  9416 I System.out: single ColorReading(red=32, green=39, blue=32, alpha=104)
03-07 08:18:45.780  9258  9416 I System.out: normalized OtherColorReading(red=0.12666015, green=0.15488282, blue=0.12753907, alpha=0.40888673)
03-07 08:18:45.908  9258  9416 I System.out: multiple ColorReading(red=1230, green=1495, blue=1210, alpha=3917)
03-07 08:18:45.909  9258  9416 I System.out: single ColorReading(red=30, green=37, blue=30, alpha=97)
03-07 08:18:45.909  9258  9416 I System.out: normalized OtherColorReading(red=0.12011719, green=0.14589845, blue=0.11826172, alpha=0.38242188)
03-07 08:18:45.936  9258  9416 I System.out: multiple ColorReading(red=1297, green=1586, blue=1306, alpha=4187)
03-07 08:18:45.936  9258  9416 I System.out: single ColorReading(red=32, green=39, blue=32, alpha=104)
03-07 08:18:45.936  9258  9416 I System.out: normalized OtherColorReading(red=0.12666015, green=0.15488282, blue=0.12753907, alpha=0.40888673)
03-07 08:18:46.028  9258  9416 I System.out: multiple ColorReading(red=1231, green=1495, blue=1211, alpha=3918)
03-07 08:18:46.028  9258  9416 I System.out: single ColorReading(red=30, green=37, blue=30, alpha=97)
03-07 08:18:46.028  9258  9416 I System.out: normalized OtherColorReading(red=0.12021484, green=0.1459961, blue=0.11826172, alpha=0.3826172)
03-07 08:18:46.058  9258  9416 I System.out: multiple ColorReading(red=1297, green=1586, blue=1306, alpha=4188)
03-07 08:18:46.058  9258  9416 I System.out: single ColorReading(red=32, green=39, blue=32, alpha=104)


Greens
03-07 08:21:34.749  9258  9416 I System.out: single ColorReading(red=9, green=18, blue=9, alpha=37)
03-07 08:21:34.749  9258  9416 I System.out: normalized OtherColorReading(red=0.037695315, green=0.07109375, blue=0.035449218, alpha=0.14658204)
03-07 08:21:34.832  9258  9416 I System.out: multiple ColorReading(red=359, green=694, blue=344, alpha=1388)
03-07 08:21:34.832  9258  9416 I System.out: single ColorReading(red=8, green=17, blue=8, alpha=34)
03-07 08:21:34.832  9258  9416 I System.out: normalized OtherColorReading(red=0.035058595, green=0.06777344, blue=0.03359375, alpha=0.13554688)
03-07 08:21:34.861  9258  9416 I System.out: multiple ColorReading(red=386, green=728, blue=363, alpha=1501)
03-07 08:21:34.861  9258  9416 I System.out: single ColorReading(red=9, green=18, blue=9, alpha=37)
03-07 08:21:34.861  9258  9416 I System.out: normalized OtherColorReading(red=0.037695315, green=0.07109375, blue=0.035546876, alpha=0.14658204)
03-07 08:21:34.953  9258  9416 I System.out: multiple ColorReading(red=359, green=694, blue=345, alpha=1387)
03-07 08:21:34.954  9258  9416 I System.out: single ColorReading(red=8, green=17, blue=8, alpha=34)
03-07 08:21:34.954  9258  9416 I System.out: normalized OtherColorReading(red=0.035058595, green=0.067675784, blue=0.03359375, alpha=0.13544922)
03-07 08:21:34.980  9258  9416 I System.out: multiple ColorReading(red=386, green=728, blue=363, alpha=1501)
03-07 08:21:34.980  9258  9416 I System.out: single ColorReading(red=9, green=18, blue=9, alpha=37)
03-07 08:21:34.980  9258  9416 I System.out: normalized OtherColorReading(red=0.037695315, green=0.070898436, blue=0.035351563, alpha=0.14658204)
03-07 08:21:35.128  9258  9416 I System.out: multiple ColorReading(red=360, green=693, blue=344, alpha=1387)
03-07 08:21:35.129  9258  9416 I System.out: single ColorReading(red=9, green=17, blue=8, alpha=34)
03-07 08:21:35.129  9258  9416 I System.out: normalized OtherColorReading(red=0.03515625, green=0.067675784, blue=0.03359375, alpha=0.13544922)
03-07 08:21:35.156  9258  9416 I System.out: multiple ColorReading(red=386, green=727, blue=363, alpha=1502)
03-07 08:21:35.156  9258  9416 I System.out: single ColorReading(red=9, green=18, blue=9, alpha=37)
03-07 08:21:35.156  9258  9416 I System.out: normalized OtherColorReading(red=0.037695315, green=0.0709961, blue=0.035449218, alpha=0.14667968)
03-07 08:21:35.244  9258  9416 I System.out: multiple ColorReading(red=359, green=693, blue=344, alpha=1387)
03-07 08:21:35.244  9258  9416 I System.out: single ColorReading(red=8, green=17, blue=8, alpha=34)
03-07 08:21:35.244  9258  9416 I System.out: normalized OtherColorReading(red=0.035058595, green=0.067675784, blue=0.03359375, alpha=0.13544922)
03-07 08:21:35.268  9258  9416 I System.out: multiple ColorReading(red=386, green=728, blue=363, alpha=1502)




Yellows

03-07 08:22:17.113  9258  9416 I System.out: multiple ColorReading(red=823, green=753, blue=378, alpha=1944)
03-07 08:22:17.113  9258  9416 I System.out: single ColorReading(red=20, green=18, blue=9, alpha=48)
03-07 08:22:17.113  9258  9416 I System.out: normalized OtherColorReading(red=0.0803711, green=0.07353516, blue=0.03691406, alpha=0.18984376)
03-07 08:22:17.139  9258  9416 I System.out: multiple ColorReading(red=850, green=811, blue=405, alpha=2068)
03-07 08:22:17.140  9258  9416 I System.out: single ColorReading(red=21, green=20, blue=10, alpha=51)
03-07 08:22:17.140  9258  9416 I System.out: normalized OtherColorReading(red=0.08300781, green=0.07919922, blue=0.03955078, alpha=0.20185547)
03-07 08:22:17.215  9258  9416 I System.out: multiple ColorReading(red=823, green=753, blue=378, alpha=1944)
03-07 08:22:17.215  9258  9416 I System.out: single ColorReading(red=20, green=18, blue=9, alpha=48)
03-07 08:22:17.215  9258  9416 I System.out: normalized OtherColorReading(red=0.0803711, green=0.07353516, blue=0.03691406, alpha=0.18984376)
03-07 08:22:17.243  9258  9416 I System.out: multiple ColorReading(red=851, green=811, blue=405, alpha=2067)
03-07 08:22:17.243  9258  9416 I System.out: single ColorReading(red=21, green=20, blue=10, alpha=51)
03-07 08:22:17.245  9258  9416 I System.out: normalized OtherColorReading(red=0.08300781, green=0.07919922, blue=0.03955078, alpha=0.20185547)
03-07 08:22:17.326  9258  9416 I System.out: multiple ColorReading(red=823, green=754, blue=377, alpha=1944)
03-07 08:22:17.326  9258  9416 I System.out: single ColorReading(red=20, green=18, blue=9, alpha=48)
03-07 08:22:17.326  9258  9416 I System.out: normalized OtherColorReading(red=0.0803711, green=0.07353516, blue=0.036816407, alpha=0.18984376)
03-07 08:22:17.352  9258  9416 I System.out: multiple ColorReading(red=851, green=811, blue=405, alpha=2067)
03-07 08:22:17.352  9258  9416 I System.out: single ColorReading(red=21, green=20, blue=10, alpha=51)
03-07 08:22:17.352  9258  9416 I System.out: normalized OtherColorReading(red=0.08310547, green=0.07919922, blue=0.03955078, alpha=0.20185547)
03-07 08:22:17.452  9258  9416 I System.out: multiple ColorReading(red=823, green=753, blue=377, alpha=1944)
03-07 08:22:17.452  9258  9416 I System.out: single ColorReading(red=20, green=18, blue=9, alpha=48)
03-07 08:22:17.453  9258  9416 I System.out: normalized OtherColorReading(red=0.0803711, green=0.07353516, blue=0.036816407, alpha=0.18984376)
03-07 08:22:17.477  9258  9416 I System.out: multiple ColorReading(red=851, green=811, blue=405, alpha=2067)
03-07 08:22:17.477  9258  9416 I System.out: single ColorReading(red=21, green=20, blue=10, alpha=51)





Purples

03-07 08:22:50.855  9258  9416 I System.out: normalized OtherColorReading(red=0.0703125, green=0.07900391, blue=0.085839845, alpha=0.23085938)
03-07 08:22:50.882  9258  9416 I System.out: multiple ColorReading(red=834, green=967, blue=1087, alpha=2886)
03-07 08:22:50.883  9258  9416 I System.out: single ColorReading(red=20, green=24, blue=27, alpha=72)
03-07 08:22:50.885  9258  9416 I System.out: normalized OtherColorReading(red=0.081445314, green=0.0944336, blue=0.10615235, alpha=0.28183594)
03-07 08:22:50.999  9258  9416 I System.out: multiple ColorReading(red=720, green=809, blue=879, alpha=2364)
03-07 08:22:50.999  9258  9416 I System.out: single ColorReading(red=18, green=20, blue=21, alpha=59)
03-07 08:22:50.999  9258  9416 I System.out: normalized OtherColorReading(red=0.0703125, green=0.07900391, blue=0.08574219, alpha=0.23076172)
03-07 08:22:51.026  9258  9416 I System.out: multiple ColorReading(red=834, green=968, blue=1087, alpha=2887)
03-07 08:22:51.026  9258  9416 I System.out: single ColorReading(red=20, green=24, blue=27, alpha=72)
03-07 08:22:51.027  9258  9416 I System.out: normalized OtherColorReading(red=0.081445314, green=0.0944336, blue=0.10615235, alpha=0.28183594)
03-07 08:22:51.119  9258  9416 I System.out: multiple ColorReading(red=721, green=810, blue=879, alpha=2365)
03-07 08:22:51.119  9258  9416 I System.out: single ColorReading(red=18, green=20, blue=21, alpha=59)
03-07 08:22:51.119  9258  9416 I System.out: normalized OtherColorReading(red=0.0703125, green=0.07900391, blue=0.08574219, alpha=0.23085938)
03-07 08:22:51.149  9258  9416 I System.out: multiple ColorReading(red=834, green=968, blue=1087, alpha=2886)
03-07 08:22:51.149  9258  9416 I System.out: single ColorReading(red=20, green=24, blue=27, alpha=72)
03-07 08:22:51.149  9258  9416 I System.out: normalized OtherColorReading(red=0.081445314, green=0.0944336, blue=0.106054686, alpha=0.28183594)
03-07 08:22:51.221  9258  9416 I System.out: multiple ColorReading(red=721, green=809, blue=878, alpha=2364)
03-07 08:22:51.221  9258  9416 I System.out: single ColorReading(red=18, green=20, blue=21, alpha=59)
03-07 08:22:51.221  9258  9416 I System.out: normalized OtherColorReading(red=0.0703125, green=0.07900391, blue=0.08574219, alpha=0.23085938)
03-07 08:22:51.246  9258  9416 I System.out: multiple ColorReading(red=834, green=967, blue=1087, alpha=2887)
03-07 08:22:51.246  9258  9416 I System.out: single ColorReading(red=20, green=24, blue=27, alpha=72)
03-07 08:22:51.247  9258  9416 I System.out: normalized OtherColorReading(red=0.081445314, green=0.0944336, blue=0.10615235, alpha=0.28173828)
03-07 08:22:51.377  9258  9416 I System.out: multiple ColorReading(red=720, green=809, blue=878, alpha=2364)
03-07 08:22:51.377  9258  9416 I System.out: single ColorReading(red=18, green=20, blue=21, alpha=59)
03-07 08:22:51.377  9258  9416 I System.out: normalized OtherColorReading(red=0.0703125, green=0.07900391, blue=0.08574219, alpha=0.23085938)
03-07 08:22:51.403  9258  9416 I System.out: multiple ColorReading(red=834, green=968, blue=1087, alpha=2886)
03-07 08:22:51.403  9258  9416 I System.out: single ColorReading(red=20, green=24, blue=27, alpha=72)
03-07 08:22:51.404  9258  9416 I System.out: normalized OtherColorReading(red=0.08134766, green=0.0944336, blue=0.10615235, alpha=0.28173828)
03-07 08:22:51.493  9258  9416 I System.out: multiple ColorReading(red=720, green=809, blue=878, alpha=2363)
03-07 08:22:51.493  9258  9416 I System.out: single ColorReading(red=18, green=20, blue=21, alpha=59)
03-07 08:22:51.493  9258  9416 I System.out: normalized OtherColorReading(red=0.0703125, green=0.07900391, blue=0.08574219, alpha=0.23076172)

     */
    // TODO: Fix these
    /*
nothing - OtherColorReading(red=0.0070312503, green=0.012011719, blue=0.0146484375, alpha=0.03388672)
white   - OtherColorReading(red=0.12021484,   green=0.1459961,   blue=0.11826172,   alpha=0.3826172)
purple  - OtherColorReading(red=0.0703125,    green=0.07900391,  blue=0.085839845,  alpha=0.23085938)
yellow  - OtherColorReading(red=0.0803711,    green=0.07353516,  blue=0.03691406,   alpha=0.18984376)
green   - OtherColorReading(red=0.037695315,  green=0.07109375,  blue=0.035449218,  alpha=0.14658204)
     */
    val upperNothingReading = ColorReading(red=0.0070312503f, green=0.012011719f, blue=0.0146484375f, alpha=0.03388672f)
//    val upperNothingReading = ColorReading(red= 207, green= 336, blue= 473, alpha= 990)
//    val lowerNothingReading = ColorReading(red= 41, green= 69, blue= 106, alpha= 219)
    private fun isPixelIn(reading: ColorReading): Boolean {
        val doesEachColorChannelPass = reading.asList.mapIndexed {i, it ->
            it > (upperNothingReading.asList[i] * 2)
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