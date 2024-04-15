package us.brainstormz.localizer

import us.brainstormz.localizer.aprilTagLocalization.CalculateAprilTagCalibration
import us.brainstormz.localizer.aprilTagLocalization.FourPoints


data class PointInXInchesAndYInches(val xInches: Double, val yInches: Double)
data class MutablePointInXInchesAndYInches(var xInches: Double, var yInches: Double)


class PredeterminedFieldPoints {
    val Red = FourPoints(
            first = PointInXInchesAndYInches(
                    xInches = 23.875,
                    yInches = 47.625
            ),
            second = PointInXInchesAndYInches(
                    xInches = 47.625,
                    yInches = 47.625
            ),
            third = PointInXInchesAndYInches(
                    xInches = 23.875,
                    yInches = 23.375
            ),
            fourth =  PointInXInchesAndYInches(
                    xInches = 47.625,
                    yInches = 23.375
            )
    )

    val Blue = FourPoints(
            first = PointInXInchesAndYInches(
                    xInches = 47.5,
                    yInches = 47.625
            ),
            second = PointInXInchesAndYInches(
                    xInches = 23.625,
                    yInches = 47.625
            ),
            third = PointInXInchesAndYInches(
                    xInches = 47.5,
                    yInches = 23.875
            ),
            fourth = PointInXInchesAndYInches(
                    xInches = 23.75,
                    yInches = 23.875
            )
    )

}