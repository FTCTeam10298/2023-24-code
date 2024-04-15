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


    val garageFieldAtHome = FieldConfiguration(
            BlueAllianceOffsets = AverageAprilTagBackboardOffset(
                    xInches = -0.29375,
                    yInches = 3.235,
                    hDegrees = 0.0, //0
            ),
            RedAllianceOffsets = AverageAprilTagBackboardOffset(
                    xInches = 0.0125,
                    yInches = 4.15,
                    hDegrees = 0.0,
            )
    )

    //TODO: delete
    val garageFieldAtHomeButPotatoBot = FieldConfiguration(
            BlueAllianceOffsets = AverageAprilTagBackboardOffset(
                    xInches = 0.27875,
                    yInches = 3.1625,
                    hDegrees = 0.0, //0
            ),
            RedAllianceOffsets = AverageAprilTagBackboardOffset(
                    xInches = 0.0125,
                    yInches = 4.15,
                    hDegrees = 0.0,
            )
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

    val fieldConfigurationNoOffsets = FieldConfiguration(
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