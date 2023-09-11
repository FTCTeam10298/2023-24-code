package interfacing

import locationTracking.PosAndRot
import interfacing.localization.World
import interfacing.path.Path

interface PathFinder {
    fun calculatePath(world: World, from: PosAndRot, to: PosAndRot): Path
}