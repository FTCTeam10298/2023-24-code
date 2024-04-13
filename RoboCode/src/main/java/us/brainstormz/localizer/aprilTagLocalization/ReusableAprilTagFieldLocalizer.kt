package us.brainstormz.localizer.aprilTagLocalization

import CameraRelativePointInSpace
import FieldRelativePointInSpace
import TagRelativePointInSpace
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection
import returnCamCentricCoordsInTagCentricCoordsPartDeux
import kotlin.math.abs

//Go to a bunch of points, look at the best AprilTag, then find your error, find the median average error, and subtract it. That should clean up your data.
data class AverageAprilTagLocalizationError (
        val xInches: Double,
        val yInches: Double,
        val hDegrees: Double
)

class ReusableAprilTagFieldLocalizer(private val aprilTagLocalization:AprilTagLocalizationFunctions,
                                     private val averageErrorRedSide: AverageAprilTagLocalizationError,
                                     private val averageErrorBlueSide: AverageAprilTagLocalizationError){

    fun getFieldPositionsForTag(detection: AprilTagDetection):FieldRelativePointInSpace? {
        return returnAprilTagInFieldCentricCoords(detection)?.FieldRelativePointInSpace
    }

    private fun returnFieldCentricCoordsInJamesFieldCoords(anyOldTag: FieldRelativePointInSpace, allianceSide: AllianceSide): FieldRelativePointInSpace {

        return if (allianceSide == AllianceSide.Red) {
            FieldRelativePointInSpace(
                    xInches = anyOldTag.xInches - averageErrorRedSide.xInches,
                    yInches = -(anyOldTag.yInches) - averageErrorRedSide.yInches,
                    headingDegrees = (360 - abs(anyOldTag.headingDegrees)) - averageErrorRedSide.hDegrees //have the angle decrease
            )
        }
        else { //the side's blue
            FieldRelativePointInSpace(
                    xInches = anyOldTag.xInches - averageErrorBlueSide.xInches,
                    yInches = -(anyOldTag.yInches) - averageErrorBlueSide.yInches,
                    headingDegrees = (360 - abs(anyOldTag.headingDegrees)) - averageErrorBlueSide.hDegrees //have the angle decrease
            )
        }


    }

    fun returnAprilTagInFieldCentricCoords(aprilTag: AprilTagDetection): AprilTagAndData {

        var isTheMeasurementAccurateToOneInch = true

        //go look in the FTC documentation, you absolutely need to understand the FTC AprilTag Coordinate System

        //you need to look at the diagram to get this.

        //getting l

        val thisAprilTagPose = aprilTag.ftcPose
        val representationOfAprilTag = CameraRelativePointInSpace(
                xInches=thisAprilTagPose.x,
                yInches=thisAprilTagPose.y,
                yawDegrees=thisAprilTagPose.yaw)

        val thisTagInTagCentricCoords = returnCamCentricCoordsInTagCentricCoordsPartDeux(representationOfAprilTag)
        val resultPositionInTagCentric = TagRelativePointInSpace(
                thisTagInTagCentricCoords.xInches,
                thisTagInTagCentricCoords.yInches,
                thisTagInTagCentricCoords.headingDegrees)

        val alliancePositionOfTag: AllianceSide = when(aprilTag.id) {
            1, 2, 3 -> AllianceSide.Blue
            4, 5, 6 -> AllianceSide.Red

            //this should never happen, but we have some weirdness
            else -> AllianceSide.Blue
        }

        val thisTagInFieldCentricCoords = aprilTagLocalization.getCameraPositionOnField(aprilTagID = aprilTag.id, thisTagInTagCentricCoords, alliancePositionOfTag)

        val resultPositionInJamesFieldCoords = returnFieldCentricCoordsInJamesFieldCoords(thisTagInFieldCentricCoords, alliancePositionOfTag)


        //we're only ever on the negative y-side, so we take the negative to get the absolute value of our y.

        val yMinimum = 10

        val xMinimum = -1000

        val xMaximum = 1000

        val areWeInAnAccurateXPosition = java.lang.Boolean.valueOf(xMinimum <= -resultPositionInJamesFieldCoords.yInches ||
                -resultPositionInJamesFieldCoords.yInches <= xMaximum)

        if (-resultPositionInJamesFieldCoords.yInches <= yMinimum && areWeInAnAccurateXPosition){
            isTheMeasurementAccurateToOneInch = false
        }


        return AprilTagAndData(AprilTag = aprilTag,
                CamRelativePointInSpace = representationOfAprilTag,
                TagRelativePointInSpace = resultPositionInTagCentric,
                FieldRelativePointInSpace =  resultPositionInJamesFieldCoords,
                AllianceSide = alliancePositionOfTag,
                valueHasOneInchAccuracy = isTheMeasurementAccurateToOneInch)
    }

    enum class AllianceSide{
        Red,
        Blue
    }


    data class AprilTagAndData(val AprilTag: AprilTagDetection, val CamRelativePointInSpace: CameraRelativePointInSpace?,
                               val TagRelativePointInSpace: TagRelativePointInSpace?, val FieldRelativePointInSpace: FieldRelativePointInSpace?,
                               val AllianceSide: AllianceSide, val valueHasOneInchAccuracy: Boolean)

}
