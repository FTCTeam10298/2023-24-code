package us.brainstormz.localizer.aprilTagLocalization

import us.brainstormz.localizer.PointInXInchesAndYInches
import us.brainstormz.localizer.PredeterminedFieldPoints
import kotlin.math.abs

data class FourPoints(
        val first: PointInXInchesAndYInches,
        val second: PointInXInchesAndYInches,
        val third: PointInXInchesAndYInches,
        val fourth: PointInXInchesAndYInches
)

data class PointInXInchesAndYInches(val xInches: Double, val yInches: Double)

fun getDelta(calculated:Double, actual:Double) = actual - abs(calculated)
fun findErrorOfFourPoints(allianceSide: ReusableAprilTagFieldLocalizer.AllianceSide,
                          fourPointsPredictedMeasurement: FourPoints): FourPoints {
    val predeterminedFieldPoints = PredeterminedFieldPoints()

    val predeterminedFieldPointsRedOrBlue = when (allianceSide) {
        ReusableAprilTagFieldLocalizer.AllianceSide.Blue -> predeterminedFieldPoints.Blue
        ReusableAprilTagFieldLocalizer.AllianceSide.Red -> predeterminedFieldPoints.Red
    }

    val realWorldMeasurementOfFirst = predeterminedFieldPointsRedOrBlue.first
    val realWorldMeasurementOfSecond = predeterminedFieldPointsRedOrBlue.second
    val realWorldMeasurementOfThird = predeterminedFieldPointsRedOrBlue.third
    val realWorldMeasurementOfFourth = predeterminedFieldPointsRedOrBlue.fourth

    val predictedMeasurementOfFirst = fourPointsPredictedMeasurement.first
    val predictedMeasurementOfSecond = fourPointsPredictedMeasurement.second
    val predictedMeasurementOfThird = fourPointsPredictedMeasurement.third
    val predictedMeasurementOfFourth = fourPointsPredictedMeasurement.fourth

    val differenceBetweenActualMeasurementAndPredictionFirst = PointInXInchesAndYInches(
            xInches = realWorldMeasurementOfFirst.xInches - abs(predictedMeasurementOfFirst.xInches),
            yInches = realWorldMeasurementOfFirst.yInches - abs(predictedMeasurementOfFirst.yInches)

    )

    val differenceBetweenActualMeasurementAndPredictionSecond = PointInXInchesAndYInches(
            xInches = realWorldMeasurementOfSecond.xInches - abs(predictedMeasurementOfSecond.xInches),
            yInches = realWorldMeasurementOfSecond.yInches - abs(predictedMeasurementOfSecond.yInches)
    )

    val differenceBetweenActualMeasurementAndPredictionThird = PointInXInchesAndYInches(
            xInches = realWorldMeasurementOfThird.xInches - abs(predictedMeasurementOfThird.xInches),
            yInches = realWorldMeasurementOfThird.yInches - abs(predictedMeasurementOfThird.yInches)
    )

    val differenceBetweenActualMeasurementAndPredictionFourth = PointInXInchesAndYInches(
            xInches = realWorldMeasurementOfFourth.xInches - abs(predictedMeasurementOfFourth.xInches),
            yInches = realWorldMeasurementOfFourth.yInches - abs(predictedMeasurementOfFourth.yInches)
    )



    return (FourPoints(
            first = differenceBetweenActualMeasurementAndPredictionFirst,
            second = differenceBetweenActualMeasurementAndPredictionSecond,
            third = differenceBetweenActualMeasurementAndPredictionThird,
            fourth = differenceBetweenActualMeasurementAndPredictionFourth
    ))

}

class CalculateAprilTagCalibration {


}