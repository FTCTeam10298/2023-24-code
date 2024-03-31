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

class ReusableAprilTagFieldLocalizer(private val aprilTagLocalization:AprilTagLocalizationFunctions, private val averageError: AverageAprilTagLocalizationError){

    fun getFieldPositionsForTag(detection: AprilTagDetection):FieldRelativePointInSpace? {
        return returnAprilTagInFieldCentricCoords(detection)?.FieldRelativePointInSpace
    }

    private fun returnFieldCentricCoordsInJamesFieldCoords(anyOldTag: FieldRelativePointInSpace): FieldRelativePointInSpace {

        return FieldRelativePointInSpace(
                xInches = anyOldTag.xInches - averageError.xInches,
                yInches = -(anyOldTag.yInches) - averageError.yInches,
                headingDegrees = (360 - abs(anyOldTag.headingDegrees)) - averageError.hDegrees //have the angle decrease
        )
    }

    fun returnAprilTagInFieldCentricCoords(aprilTag: AprilTagDetection): AprilTagAndData? {
        //go look in the FTC documentation, you absolutely need to understand the FTC AprilTag Coordinate System

        //you need to look at the diagram to get this.

        //getting l

        if (aprilTag != null) {
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

            val thisTagInFieldCentricCoords = aprilTagLocalization.getCameraPositionOnField(aprilTagID = aprilTag.id, thisTagInTagCentricCoords)

            val resultPositionInJamesFieldCoords = returnFieldCentricCoordsInJamesFieldCoords(thisTagInFieldCentricCoords)

            return AprilTagAndData(aprilTag, representationOfAprilTag!!, resultPositionInTagCentric, resultPositionInJamesFieldCoords)
        }
        else return null
    }

    data class AprilTagAndData(val AprilTag: AprilTagDetection, val CamRelativePointInSpace: CameraRelativePointInSpace?,
                               val TagRelativePointInSpace: TagRelativePointInSpace?, val FieldRelativePointInSpace: FieldRelativePointInSpace?)

}
