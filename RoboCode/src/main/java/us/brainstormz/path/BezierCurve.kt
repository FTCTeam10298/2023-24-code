package us.brainstormz.path

import us.brainstormz.localizer.PositionAndRotation
import kotlin.math.pow

class BezierCurve(val ctrlPoints: List<PositionAndRotation>)/*: Collection<Point3D> by listOf(*ctrlPoints.toTypedArray())*/ {

    fun calculatePoint(t: Double): PositionAndRotation {
        val n = ctrlPoints.size - 1
        val t = t.coerceIn(0.1, 1.0)

        return ctrlPoints.fold(PositionAndRotation()) { acc, it ->
            val i = ctrlPoints.indexOf(it)

            val c = n.factorial() / (i.factorial() * ((n-i).factorial()))
            acc + (it * ((1-t).pow(n-i)) * (t.pow(i) * c))
        }
    }

    fun getPointOnSegment(curveIndex: Double, t: Double): PositionAndRotation {
        return calculatePoint(curveIndex) * (t - 1) + calculatePoint(curveIndex + 1) * t
    }
}

fun Int.factorial(): Double {
    var factorial: Long=1

    for(i in 1..this){
        factorial*=i.toLong()
    }

    return factorial.toDouble()
}


//fun interpolate(segmentPoints: List<Point3D>, scale: Double) : BezierCurve {
//    val controlPoints = mutableListOf(Point3D())
//
//    if (segmentPoints.size < 2)
//        return BezierCurve(listOf(Point3D()))
//
//    for (i in segmentPoints.indices) {
//        when (i) {
//            0 -> {// is first
//                val p1: Point3D = segmentPoints[i]
//                val p2: Point3D = segmentPoints[i + 1]
//
//                val tangent: Point3D = (p2 - p1)
//                val q1: Point3D = p1 + tangent * scale
//
//                controlPoints.add(p1)
//                controlPoints.add(q1)
//            }
//            segmentPoints.lastIndex -> {  //last
//                val p0: Point3D = segmentPoints[i - 1]
//                val p1: Point3D = segmentPoints[i]
//                val tangent: Point3D = (p1 - p0)
//                val q0: Point3D = p1 - tangent * scale
//
//                controlPoints.add(q0)
//                controlPoints.add(p1)
//            }
//            else -> { // middle
//                val p0: Point3D = segmentPoints[i - 1]
//                val p1: Point3D = segmentPoints[i]
//                val p2: Point3D = segmentPoints[i + 1]
//                val tangent: Point3D = (p2 - p0).normalized()
//                val q0: Point3D = p1 - tangent * scale * (p1 - p0).magnitude()
//                val q1: Point3D = p1 + tangent * scale * (p2 - p1).magnitude()
//
//                controlPoints.add(q0)
//                controlPoints.add(p1)
//                controlPoints.add(q1)
//            }
//        }
//    }
//
//    return BezierCurve(controlPoints)
//}