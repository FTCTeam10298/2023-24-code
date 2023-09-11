package us.brainstormz.utils

import locationTracking.PosAndRot
import kotlin.math.*

object MathHelps {
    fun scaleBetween(value: Double, range1: ClosedRange<Double>, range2: ClosedRange<Double>): Double {
        val range1Total = range1.endInclusive - range1.start
        val range2Total = range2.endInclusive - range2.start

        return (((value - range1.start) * range2Total) / range1Total) + range2.start
    }
    fun posOrNeg(num: Double): Int {
        return when {
            num > 0 -> 1
            num < 0 -> -1
            else -> 0
        }
    }
    fun wrap360(degrees: Double): Double {
//        return if (degrees < 0)
//            360 - degrees
//        else
           return degrees.mod(360.0)
    }
    fun smallestHeadingChange(currentHeading: Double, targetHeading: Double): Double {

        val absCurrentHeading = abs(currentHeading)
        val absHeadingToMouse = abs(targetHeading)


        // console.log("degreesToMouse: " + headingToMouse)
        // console.log("current angle : " + currentHeading)

        // # 1 & 2
        return when {
            getSign(currentHeading) != getSign(targetHeading) -> {
                 if ((abs(currentHeading) + abs(targetHeading)) < (360 - absCurrentHeading - absHeadingToMouse)) {
                    // #1
                    // console.log("#1")

                    -getSign(currentHeading) * (absCurrentHeading + absHeadingToMouse)
                } else {
                    // #2
                    // console.log("#2")

                    getSign(currentHeading) * (360 - absCurrentHeading - absHeadingToMouse)
                }
            }
            absCurrentHeading != absHeadingToMouse -> {
                // console.log("#3")
                 targetHeading - currentHeading
            }
            absCurrentHeading == absHeadingToMouse ->  0.0
            else -> 0.0
        }

    }

    fun getSign(valu: Double):Int {
        return when {
            valu > 0 -> 1
            valu < 0 -> -1
            else -> 0
        }
    }
    fun calcVector(x: Double, y: Double, magnitude: Double, radians: Double): Point2D {
        val newX = (magnitude * cos(radians)) + x
        val newY = (magnitude * sin(radians)) + y
        return Point2D(newX, newY)
    }
}

open class Point3D(val x: Double, val y: Double, val z: Double) {

    constructor() : this(0.0, 0.0, 0.0)

    operator fun plus(point: Point3D): Point3D {
        return Point3D(point.x + this.x, point.y + this.y, point.z + this.z)
    }

    operator fun minus(point: Point3D): Point3D {
        return Point3D(point.x - this.x, point.y - this.y, point.z - this.z)
    }

    fun pow(i: Int): Point3D {
        return Point3D(this.x.pow(i), this.y.pow(i), this.z.pow(i))
    }

    operator fun times(d: Double): Point3D {
        val x = this.x * d
        val y = this.y * d
        val z = this.z * d
        return Point3D(x, y, z)
    }

    fun copy(x: Double, y: Double, z: Double): Point3D = Point3D(this.x + x, this.y + y, this.z + z)

    fun toPoint2D(): Point2D = Point2D(this.x, this.y)

    fun magnitude(): Double {
        return this.x.pow(2) + y.pow(2) + z.pow(2)
    }
    fun normalized(): Point3D {
        val m = this.magnitude()
        return if (m > 0) {
            this / m
        } else
            Point3D()
    }

    operator fun div(i: Double): Point3D {
        return Point3D(this.x / i, this.y / i, this.z / i)
    }

    override fun toString(): String {
        return "($x, $y, $z)"
    }

    fun distanceTo(p1: PosAndRot): Double =
            sqrt((p1.x - x).pow(2.0) +
                         (p1.y - y).pow(2.0) * 1.0)

    fun equals(other: Point3D): Boolean {
        return this.x == other.x && this.y == other.y && this.z == other.z
    }
}

class Point2D(x: Double, y: Double): Point3D(x, y, 0.0) {
    fun toPoint3D(): Point3D = Point3D(this.x, this.y, this.z)
}