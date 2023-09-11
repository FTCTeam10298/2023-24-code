package us.brainstormz.path

import us.brainstormz.localizer.PositionAndRotation

class BezierPath(val path: BezierCurve):Path {
    override fun length(): Double {
        return 0.0
    }

    override fun positionAt(distance: Double): PositionAndRotation {
        val point3D = path.calculatePoint(distance)
        return PositionAndRotation(point3D.x, point3D.y, point3D.r)
    }
}