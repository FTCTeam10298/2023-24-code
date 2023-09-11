package interfacing.localization

import locationTracking.PosAndRot

interface Localizer {
    fun currentPositionAndRotation(): PosAndRot
    fun recalculatePositionAndRotation()
    fun setPositionAndRotation(x: Double? = null, y: Double? = null, r: Double? = null)
}