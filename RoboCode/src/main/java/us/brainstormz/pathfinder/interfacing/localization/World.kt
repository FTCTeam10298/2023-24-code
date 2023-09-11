package interfacing.localization

import locationTracking.PosAndRot

interface World {
    fun currentPositionAndRotation(): PosAndRot
}