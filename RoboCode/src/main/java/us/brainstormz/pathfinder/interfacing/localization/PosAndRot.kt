package locationTracking

import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

/**
 * Creates a PositionAndRotation with given values. All alternate constructors assume 0 for unstated variables.
 * @param x X position
 * @param y Y position
 * @param r Angle, in radians
 */
open class PosAndRot(var x: Double = 0.0, var y: Double = 0.0, var r: Double = 0.0) {

    /**
     * Sets the parameters of the PositionAndRotation.
     * @param x The x value that we want to set.
     * @param y The y value that we want to set.
     * @param r The angle that we want to set in degrees
     */
    fun setPositionAndRotation(x: Double? = null, y: Double? = null, r: Double? = null) {
        if (x != null)
            this.x = x

        if (y != null)
            this.y = y

        if (r != null) {
//            var rAdjusted: Double = r
//
//            while (abs(rAdjusted) > 180)
//                rAdjusted += (Math.PI/* * 2*/)

//            this.r = rAdjusted
            this.r = r
        }
    }

    fun addPositionAndRotation(x: Double = 0.0, y: Double = 0.0, r: Double = 0.0) {
        setPositionAndRotation(this.x + x, this.y + y, this.r + r)
    }

    /**
     * Wraps the angle around so that the robot doesn't unnecessarily turn over 180 degrees.
     * @param angle The angle to wrap.
     * @return The wrapped angle.
     */
    fun wrapAngle(angle: Double): Double {
        return angle % (2 * Math.PI)
    }

    /**
     * Gives the absolute value of the distance between the given PositionAndRotation and the current PositionAndRotation.
     * @param PosAndRot PositionAndRotation to compare
     * @return distance from current PositionAndRotation
     */
    fun distance(PosAndRot: PosAndRot): Double {
        return hypot(PosAndRot.x - x, PosAndRot.y - y)
    }

    /**
     * Gives the absolute value of the distance between the X and Y values and the current PositionAndRotation.
     * @param targetX X
     * @param targetY Y
     * @return distance from current PositionAndRotation
     */
    fun distance(targetX: Double, targetY: Double): Double {
        return hypot(targetX - x, targetY - y)
    }

    /**
     * Gives the error of the angle from the given angle and the current PositionAndRotation.
     * @param targetA angle to compare
     * @return angle error from current PositionAndRotation
     */
    fun theta(targetA: Double): Double {
        return wrapAngle(targetA - r)
    }

    /**
     * Gives the error of the angle from the given PositionAndRotation and the current PositionAndRotation.
     * @param PosAndRot PositionAndRotation to compare
     * @return angle error from current PositionAndRotation
     */
    fun theta(PosAndRot: PosAndRot): Double {
        return theta(PosAndRot.r)
    }

    fun direction(PosAndRot: PosAndRot): Double {
        return Math.atan2(PosAndRot.y-this.y, PosAndRot.x-this.x)
    }

    fun rotateAround(origin: PosAndRot, angle: Double): PosAndRot =
        PosAndRot(
            cos(angle) * (this.x - origin.x) - sin(angle) * (this.y - origin.y) + origin.x,
            sin(angle) * (this.x - origin.x) + cos(angle) * (this.y - origin.y) + origin.y)


    fun coordinateAlongLine(distance: Double, p2: PosAndRot): PosAndRot {

        val d = this.distance(p2)

        return PosAndRot(
            this.x + ((distance / d) * (p2.x - this.x)),
            this.y + ((distance / d) * (p2.y - this.y))
        )
    }

    override fun equals(other: Any?): Boolean {
        return super.equals(other)
    }

//    override fun toString(): String {
//        return "X: $x\nY: $y\nAngle: $r"
//    }


    override fun toString(): String {
        return "(x: $x, y: $y, angle: $r)"
    }

    override fun hashCode(): Int = x.hashCode() + y.hashCode() + r.hashCode()

    fun copy(x: Double, y: Double, r: Double): PosAndRot {
        val thisPlaceHolder = this
        thisPlaceHolder.addPositionAndRotation(x, y, r)
        return thisPlaceHolder
    }

    operator fun plus(n: Double): PosAndRot {
        return PosAndRot(this.x + n, this.y + n, this.r + n)
    }

    operator fun plus(n: PosAndRot): PosAndRot {
        return PosAndRot(this.x + n.x, this.y + n.y, this.r + n.r)
    }
    operator fun times(n: Double): PosAndRot {
        return PosAndRot(this.x * n, this.y * n, this.r * n)
    }

    operator fun compareTo(n: PosAndRot): Int {
        val avgN = n.x + n.y + n.r / 3
        val avgThis = this.x + this.y + this.r / 3

        val difference = avgThis - avgN

        return when{
            difference == 0.0 -> 0
            difference > 0 -> 1
            else -> -1
        }
    }

    operator fun div(n: Int): PosAndRot {
        return PosAndRot(x / n, y / n, r / n)
    }

    operator fun minus(n: PosAndRot): PosAndRot {
        return plus(n = n * -1.0)
    }
}
