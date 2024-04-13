package us.brainstormz.localizer

import FourPoints

data class PointInXInchesAndYInches(val xInches: Double, val yInches: Double)



val b1 = PointInXInchesAndYInches(
        xInches = 47.5,
        yInches = 47.625
)

val b2 = PointInXInchesAndYInches(
        xInches = 23.625,
        yInches = 47.625
)

val b3 = PointInXInchesAndYInches(
        xInches = 47.5,
        yInches = 23.875
)

val b4 = PointInXInchesAndYInches(
        xInches = 23.75,
        yInches = 23.875
)



val r1 = PointInXInchesAndYInches(
        xInches = 23.875,
        yInches = 47.625
)

val r2 = PointInXInchesAndYInches(
        xInches = 47.625,
        yInches = 47.625
)

val r3 = PointInXInchesAndYInches(
        xInches = 23.875,
        yInches = 23.375
)

val r4 = PointInXInchesAndYInches(
        xInches = 47.625,
        yInches = 23.375
)


class PredeterminedFieldPoints {
    val Red = FourPoints(
            first = r1,
            second = r2,
            third = r3,
            fourth = r4
    )

    val Blue = FourPoints(
            first = b1,
            second = b2,
            third = b3,
            fourth = b4
    )

}