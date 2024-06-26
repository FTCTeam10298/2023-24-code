package us.brainstormz.localizer

import kotlinx.serialization.Serializable
import kotlin.math.hypot

/**
 * Creates a Coordinate with given values. All alternate constructors assume 0 for unstated variables.
 * @param x X position
 * @param y Y position
 * @param r Angle, in degrees
 * Matches the official coordinate system
 */
@Serializable
data class PositionAndRotation(var x: Double = 0.0, var y: Double = 0.0, var r: Double = 0.0) {

    /**
     * Sets the parameters of the Coordinate.
     * @param x The x value that we want to set.
     * @param y The y value that we want to set.
     * @param r The angle that we want to set in degrees
     */
    fun setCoordinate(x: Double? = null, y: Double? = null, r: Double? = null) {
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

    fun addCoordinate(x: Double = 0.0, y: Double = 0.0, r: Double = 0.0) {
        setCoordinate(this.x + x, this.y + y, this.r + r)
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
     * Gives the absolute value of the distance between the given Coordinate and the current Coordinate.
     * @param positionAndRotation Coordinate to compare
     * @return distance from current Coordinate
     */
    fun distance(positionAndRotation: PositionAndRotation): Double {
        return hypot(positionAndRotation.x - x, positionAndRotation.y - y)
    }

    /**
     * Gives the absolute value of the distance between the X and Y values and the current Coordinate.
     * @param targetX X
     * @param targetY Y
     * @return distance from current Coordinate
     */
    fun distance(targetX: Double, targetY: Double): Double {
        return hypot(targetX - x, targetY - y)
    }

    /**
     * Gives the error of the angle from the given angle and the current Coordinate.
     * @param targetA angle to compare
     * @return angle error from current Coordinate
     */
    fun theta(targetA: Double): Double {
        return wrapAngle(targetA - r)
    }

    /**
     * Gives the error of the angle from the given Coordinate and the current Coordinate.
     * @param positionAndRotation Coordinate to compare
     * @return angle error from current Coordinate
     */
    fun theta(positionAndRotation: PositionAndRotation): Double {
        return theta(positionAndRotation.r)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is PositionAndRotation) {
            equals(other)
        } else {
            false
        }
    }

    fun equals(other: PositionAndRotation): Boolean {
        return other.x == this.x && other.y == this.y && other.r == this.r
    }

    override fun toString(): String {
        return "X: $x Y: $y Angle: $r"
    }

    override fun hashCode(): Int = x.hashCode() + y.hashCode() + r.hashCode()
    operator fun times(n: Double): PositionAndRotation = PositionAndRotation(n*x, n*y, n*r)

    /**
     * Don't use, bad!
     */
    operator fun compareTo(other: PositionAndRotation): Int {
        val asdf = this - other

//        return when {
//            asdf == 0.0 -> 0
//            asdf > 0 -> 1
//            asdf < 0 -> -1
//            else -> 1
//        }
        return 0
    }

    operator fun minus(other: PositionAndRotation): PositionAndRotation =
            PositionAndRotation(this.x - other.x,this.y - other.y, this.r - other.r)

    operator fun plus(other: PositionAndRotation): PositionAndRotation =
            PositionAndRotation(this.x + other.x,this.y + other.y, this.r + other.r)

}
