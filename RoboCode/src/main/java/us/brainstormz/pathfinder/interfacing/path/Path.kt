package interfacing.path

import locationTracking.PosAndRot

interface Path {
    fun length():Double
    fun positionAt(distance:Double): PosAndRot
}