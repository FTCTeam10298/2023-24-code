import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.Assert
import org.junit.Test
import us.brainstormz.localizer.PointInXInchesAndYInches
import us.brainstormz.localizer.PredeterminedFieldPoints
import us.brainstormz.localizer.aprilTagLocalization.AprilTagFieldConfigurations
import us.brainstormz.localizer.aprilTagLocalization.FourPoints
import us.brainstormz.localizer.aprilTagLocalization.ReusableAprilTagFieldLocalizer
import us.brainstormz.localizer.aprilTagLocalization.calculateAprilTagOffsets
import us.brainstormz.localizer.aprilTagLocalization.findErrorOfFourPoints
import us.brainstormz.localizer.aprilTagLocalization.getDelta

class AprilTagCalibratorTest {

    fun add(a:Double, b:Double) = a + b

    fun subtract(a:Double, b:Double) = a - b
    @Test
    fun `adds numbers`(){
        // given
        val x = 0.3
        val y = 0.2

        // when
        val result = add(x, y)

        // then
        Assert.assertEquals(0.5, result, 0.0)

    }

    @Test
    fun `delta for One Number`(){
        // given
        val calculated = 47.0
        val actual = 47.5

        // when
        val result = getDelta(calculated, actual)

        // then
        Assert.assertEquals(0.5, result, 0.0)

    }

    @Test
    fun deltaForFourPoints() {

        // given
        val allianceSide: ReusableAprilTagFieldLocalizer.AllianceSide = ReusableAprilTagFieldLocalizer.AllianceSide.Red
        val fourPointsAprilTagMeasurement = FourPoints(
                first = PointInXInchesAndYInches(
                        xInches = -23.34,
                        yInches = 43.9
                ),
                second = PointInXInchesAndYInches(
                        xInches = -47.48,
                        yInches = -44.45
                ),
                third = PointInXInchesAndYInches(
                        xInches = -24.44,
                        yInches = -18.46
                ),
                fourth = PointInXInchesAndYInches(
                        xInches = -47.69,
                        yInches = -18.59
                )
        )


        // when
        val actualFourPoints = findErrorOfFourPoints(
                allianceSide = allianceSide,
                fourPointsPredictedMeasurement = fourPointsAprilTagMeasurement
        )

        val field = PredeterminedFieldPoints()

        // then
        val expected = FourPoints(
                first = PointInXInchesAndYInches(
                        xInches = getDelta(fourPointsAprilTagMeasurement.first.xInches, field.Red.first.xInches),
                        yInches = getDelta(fourPointsAprilTagMeasurement.first.yInches, field.Red.first.yInches)
                ),

                second = PointInXInchesAndYInches(
                        xInches = getDelta(fourPointsAprilTagMeasurement.second.xInches, field.Red.second.xInches),
                        yInches = getDelta(fourPointsAprilTagMeasurement.second.yInches, field.Red.second.yInches)
                ),

                third = PointInXInchesAndYInches(
                        xInches = getDelta(fourPointsAprilTagMeasurement.third.xInches, field.Red.third.xInches),
                        yInches = getDelta(fourPointsAprilTagMeasurement.third.yInches, field.Red.third.yInches)
                ),

                fourth = PointInXInchesAndYInches(
                        xInches = getDelta(fourPointsAprilTagMeasurement.fourth.xInches, field.Red.fourth.xInches),
                        yInches = getDelta(fourPointsAprilTagMeasurement.fourth.yInches, field.Red.fourth.yInches)
                ),
        )

        assertJsonEquals(expected, actualFourPoints)
        Assert.assertEquals(expected, actualFourPoints)
    }

    @Test
    fun getOffsets() {

        // given
        val allianceSide: ReusableAprilTagFieldLocalizer.AllianceSide = ReusableAprilTagFieldLocalizer.AllianceSide.Red

        val fourPointsAprilTagMeasurement = FourPoints(
                first = PointInXInchesAndYInches(
                        xInches = -23.34,
                        yInches = 43.9
                ),
                second = PointInXInchesAndYInches(
                        xInches = -47.48,
                        yInches = -44.45
                ),
                third = PointInXInchesAndYInches(
                        xInches = -24.44,
                        yInches = -18.46
                ),
                fourth = PointInXInchesAndYInches(
                        xInches = -47.69,
                        yInches = -18.59
                )
        )


        val actualFourPoints = findErrorOfFourPoints(
                allianceSide = allianceSide,
                fourPointsPredictedMeasurement = fourPointsAprilTagMeasurement
        )

        // when
        val offsets = calculateAprilTagOffsets(actualFourPoints)

        // then
        Assert.assertEquals(0.0125, offsets.xInches, 0.001)
        Assert.assertEquals(4.15, offsets.yInches, 0.001)


    }

    fun junk(){
//        data class BooleanForXAndY(val xInches: Boolean, val yInches: Boolean)
//
//        data class FourPointBooleans(
//                val first: BooleanForXAndY,
//                val second: BooleanForXAndY,
//                val third: BooleanForXAndY,
//                val fourth: BooleanForXAndY
//        )
//
//        fun getDelta(calculated:Double, actual:Double) = actual - abs(calculated)
//        val actualFourPoints = FourPoints(
//                first = PointInXInchesAndYInches(
//                        xInches = 0.0,
//                        yInches = 0.0
//                ),
//
//                second = PointInXInchesAndYInches(
//                        xInches = 0.0,
//                        yInches = 0.0
//                ),
//
//                third = PointInXInchesAndYInches(
//                        xInches = 0.0,
//                        yInches = 0.0
//                ),
//
//                fourth = PointInXInchesAndYInches(
//                        xInches = 0.0,
//                        yInches = 0.0
//                )
//        )


//        data class FourPoints(
//                val first: PointInXInchesAndYInches,
//                val second: PointInXInchesAndYInches,
//                val third: PointInXInchesAndYInches,
//                val fourth: PointInXInchesAndYInches
//        )
//
//        val expectedOutput = FourPoints(
//                first = PointInXInchesAndYInches(
//                        xInches = 0.5,
//                        yInches = -1.475
//                ),
//
//                second = PointInXInchesAndYInches(
//                        xInches = 0.0,
//                        yInches = 0.0
//                ),
//
//                third = PointInXInchesAndYInches(
//                        xInches = 0.0,
//                        yInches = 0.0
//                ),
//
//                fourth = PointInXInchesAndYInches(
//                        xInches = 0.0,
//                        yInches = 0.0
//                )
//        )
//
//        // when
////        val result = findErrorOfFourPoints(allianceSide = ReusableAprilTagFieldLocalizer.AllianceSide.Blue,
////                fourPointsPredictedMeasurement = errorsOfFourPoints)
//
//
//
////        val resultSet = FourPointBooleans(
////                first = BooleanForXAndY(
////                        xInches = (errorsOfFourPoints.first.xInches - predeterminedBlueFieldPoints.first.xInches) < acceptableError,
////                        yInches = (errorsOfFourPoints.first.yInches - predeterminedBlueFieldPoints.first.yInches) < acceptableError
////                ),
////                second = BooleanForXAndY(
////                        xInches = (errorsOfFourPoints.second.xInches - predeterminedBlueFieldPoints.second.xInches) < acceptableError,
////                        yInches = (errorsOfFourPoints.second.yInches - predeterminedBlueFieldPoints.second.yInches) < acceptableError
////                ),
////                third = BooleanForXAndY(
////                        xInches = (errorsOfFourPoints.third.xInches - predeterminedBlueFieldPoints.third.xInches) < acceptableError,
////                        yInches = (errorsOfFourPoints.third.yInches - predeterminedBlueFieldPoints.third.yInches) < acceptableError
////                ),
////                fourth = BooleanForXAndY(
////                        xInches = (errorsOfFourPoints.fourth.xInches - predeterminedBlueFieldPoints.fourth.xInches) < acceptableError,
////                        yInches = (errorsOfFourPoints.fourth.yInches - predeterminedBlueFieldPoints.fourth.yInches) < acceptableError
////                ),
////                )
//
//
//        // then
//
//        println("are they equal = $errorsOfFourPoints")
//        Assert.assertEquals(47.5, errorsOfFourPoints.first.xInches, 0.001)

    }



}


fun prettyPrint(v:Any) = jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(v)
fun assertJsonEquals(expected:Any, actual:Any){
    Assert.assertEquals(prettyPrint(expected), prettyPrint(actual))
}