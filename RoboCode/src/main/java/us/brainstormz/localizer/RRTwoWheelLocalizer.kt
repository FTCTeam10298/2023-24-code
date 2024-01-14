package us.brainstormz.localizer

import com.acmerobotics.roadrunner.Pose2d
import us.brainstormz.hardwareClasses.TwoWheelImuOdometry
import us.brainstormz.roadrunner.RoadRunnerTwoDeadWheelLocalizer
import java.util.LinkedList
import kotlin.math.PI

class RRTwoWheelLocalizer(hardware: TwoWheelImuOdometry, inchesPerTick: Double): Localizer {

    val roadRunnerLocalizer = RoadRunnerTwoDeadWheelLocalizer(hardware, inchesPerTick)
    override fun currentPositionAndRotation(): PositionAndRotation {
        val (x, y) = pose.position
        val heading = pose.heading.real
        //rr switches x and y
        return PositionAndRotation(x= y, y= x, r= Math.toDegrees(heading))
    }

    val defaultInitPose = Pose2d(0.0, 0.0, 0.0)
    var pose: Pose2d = defaultInitPose
    private val poseHistory = LinkedList<Pose2d>()
    override fun recalculatePositionAndRotation() {
        val twist = roadRunnerLocalizer.update()
        pose = pose.plus(twist.value())
        poseHistory.add(pose)
        while (poseHistory.size > 100) {
            poseHistory.removeFirst()
        }
//        return twist.velocity().value()
    }

    override fun setPositionAndRotation(x: Double?, y: Double?, r: Double?) {
        pose = Pose2d(y ?:0.0, x ?:0.0, Math.toRadians(r ?:0.0))
    }

}