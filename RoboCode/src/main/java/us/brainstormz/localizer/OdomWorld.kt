package us.brainstormz.localizer

import us.brainstormz.utils.MathHelps
import kotlin.math.hypot

class OdomWorld: World {
    var currentPos = PositionAndRotation()
    override fun currentPositionAndRotation(): PositionAndRotation = currentPos

    fun recalculateGlobalPosition(positionDelta: PositionAndRotation, previousPosition: PositionAndRotation): PositionAndRotation {
        val magnitude = hypot(positionDelta.x, positionDelta.y)
//        println("magnitude: $magnitude")
        val rotation = previousPosition.r + positionDelta.r
//        println("rotation: $rotation")
        val vectorDelta = MathHelps.calcVector(previousPosition.x, previousPosition.y, magnitude, Math.toRadians(rotation))
//        println("vector delta: $vectorDelta")
        val globalPosition = PositionAndRotation(vectorDelta.x, vectorDelta.y, rotation)
        currentPos = globalPosition
        return globalPosition
    }
}

fun main() {
    val odomWorld = OdomWorld()
    val initPos = PositionAndRotation()
    println(odomWorld.recalculateGlobalPosition(PositionAndRotation(x= 10.0, y= 10.0, r= 45.0), initPos))
}