package us.brainstormz.localizer

import com.acmerobotics.roadrunner.Pose2d
import us.brainstormz.hardwareClasses.TwoWheelImuOdometry
import us.brainstormz.roadrunner.RoadRunnerTwoDeadWheelLocalizer
import us.brainstormz.utils.measured
import java.util.LinkedList
import kotlin.math.PI

/** This class reconciles our coordinate system (the official one) with RR's */
class RRTwoWheelLocalizer(hardware: TwoWheelImuOdometry, inchesPerTick: Double): Localizer {
    val roadRunnerLocalizer = RoadRunnerTwoDeadWheelLocalizer(
            hardware.parallelEncoder,
            hardware.perpendicularEncoder,
            hardware.imu,
            hardware.parallelOdomOffsetFromCenterInch.x / inchesPerTick,
            hardware.perpendicularOdomOffsetFromCenterInch.y / inchesPerTick,
            inchesPerTick)

    override fun currentPositionAndRotation(): PositionAndRotation {
        val (x, y) = pose.position
        val headingDegrees = Math.toDegrees(pose.heading.toDouble())

        //rr switches x and y
        return PositionAndRotation(x= -y, y= x, r= headingDegrees)
    }

    val defaultInitPose = Pose2d(0.0, 0.0, 0.0)
    var pose: Pose2d = defaultInitPose
    private val poseHistory = LinkedList<Pose2d>()
    override fun recalculatePositionAndRotation() {
        val twist = measured("odomentry-update"){
            roadRunnerLocalizer.update()
        }
        pose = pose.plus(twist.value())
        poseHistory.add(pose)
        while (poseHistory.size > 100) {
            poseHistory.removeFirst()
        }
//        return twist.velocity().value()
    }

    override fun setPositionAndRotation(newPosition: PositionAndRotation) {
        pose = Pose2d(positionY= -newPosition.x, positionX= newPosition.y, heading= Math.toRadians(newPosition.r))
    }
}