package us.brainstormz.localizer.aprilTagLocalization

class AprilTagFieldErrors {

    data class FieldError(
            val BlueAllianceError: AverageAprilTagLocalizationError,
            val RedAllianceError: AverageAprilTagLocalizationError
    )

    val zeroes = AverageAprilTagLocalizationError(
            xInches = 0.0,
            yInches = 0.0,
            hDegrees = 0.0
    )

    val LeftFieldRedAllianceBackboardAverageErrors = AverageAprilTagLocalizationError(
            xInches = -0.25,
            yInches = 3.325,
            hDegrees = 0.0,
    )

    val LeftFieldBlueAllianceBackboardAverageErrors = AverageAprilTagLocalizationError(
            xInches = 0.04375,
            yInches = 3.675,
            hDegrees = 0.0, //0
    )

    val leftField = FieldError(
            BlueAllianceError = LeftFieldBlueAllianceBackboardAverageErrors,
            RedAllianceError = LeftFieldRedAllianceBackboardAverageErrors
    )





    val RightFieldRedAllianceBackboardAverageErrors = AverageAprilTagLocalizationError(
            xInches = -0.0,
            yInches = 0.0,
            hDegrees = 0.0,
    )

    val RightFieldBlueAllianceBackboardAverageErrors = AverageAprilTagLocalizationError(
            xInches = 0.0,
            yInches = 0.0,
            hDegrees = 0.0, //0
    )

    val rightField = FieldError(
            BlueAllianceError = zeroes,
            RedAllianceError = zeroes
    )






    val noOffsets = FieldError(
            RedAllianceError = zeroes,
            BlueAllianceError = zeroes
    )
}