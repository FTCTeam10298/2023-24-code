package us.brainstormz.localizer.aprilTagLocalization

object AprilTagFieldConfigurations {

    data class FieldConfiguration(
            val BlueAllianceOffsets: AverageAprilTagBackboardOffset,
            val RedAllianceOffsets: AverageAprilTagBackboardOffset
    )

    val zeroes = AverageAprilTagBackboardOffset(
            xInches = 0.0,
            yInches = 0.0,
            hDegrees = 0.0
    )



    val leftFieldAtWorlds = FieldConfiguration(
            BlueAllianceOffsets = AverageAprilTagBackboardOffset(
                    xInches = 0.04375,
                    yInches = 3.675,
                    hDegrees = 0.0, //0
            ),
            RedAllianceOffsets = AverageAprilTagBackboardOffset(
                    xInches = -0.25,
                    yInches = 3.325,
                    hDegrees = 0.0,
            )
    )

    val rightFieldAtWorlds = FieldConfiguration(
            BlueAllianceOffsets = AverageAprilTagBackboardOffset(
                    xInches = 0.0,
                    yInches = 0.0,
                    hDegrees = 0.0,
            ),
            RedAllianceOffsets = AverageAprilTagBackboardOffset(
                    xInches = 0.0,
                    yInches = 0.0,
                    hDegrees = 0.0,
            )
    )

    val fieldConfigurationWithoutOffsets = FieldConfiguration(
            RedAllianceOffsets = AverageAprilTagBackboardOffset(
                    xInches = 0.0,
                    yInches = 0.0,
                    hDegrees = 0.0,
            ),
            BlueAllianceOffsets = AverageAprilTagBackboardOffset(
                    xInches = 0.0,
                    yInches = 0.0,
                    hDegrees = 0.0,
            )
    )
}