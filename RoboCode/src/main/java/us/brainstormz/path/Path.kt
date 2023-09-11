package us.brainstormz.path

import us.brainstormz.localizer.PositionAndRotation

interface Path {
    fun length():Double
    fun positionAt(distance:Double): PositionAndRotation
}