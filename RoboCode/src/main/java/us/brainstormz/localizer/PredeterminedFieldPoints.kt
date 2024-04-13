package us.brainstormz.localizer

data class FieldRelativePointInSpaceNoHeading(val xInches: Double, val yInches: Double)

data class FourPoints(
        val first: FieldRelativePointInSpaceNoHeading,
        val second: FieldRelativePointInSpaceNoHeading,
        val third: FieldRelativePointInSpaceNoHeading,
        val fourth: FieldRelativePointInSpaceNoHeading
)

val b1 = FieldRelativePointInSpaceNoHeading(
        xInches = 47.5,
        yInches = 47.625
)

val b2 = FieldRelativePointInSpaceNoHeading(
        xInches = 23.625,
        yInches = 47.625
)

val b3 = FieldRelativePointInSpaceNoHeading(
        xInches = 47.5,
        yInches = 23.875
)

val b4 = FieldRelativePointInSpaceNoHeading(
        xInches = 23.75,
        yInches = 23.875
)



val r1 = FieldRelativePointInSpaceNoHeading(
        xInches = 23.875,
        yInches = 47.625
)

val r2 = FieldRelativePointInSpaceNoHeading(
        xInches = 47.625,
        yInches = 47.625
)

val r3 = FieldRelativePointInSpaceNoHeading(
        xInches = 23.875,
        yInches = 23.375
)

val r4 = FieldRelativePointInSpaceNoHeading(
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