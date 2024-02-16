package us.brainstormz.faux

import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation


class FauxLocalizer: Localizer {
    override fun currentPositionAndRotation(): PositionAndRotation {
        println("FauxLocalizer: currentPositionAndRotation")
        return PositionAndRotation()
    }

    override fun recalculatePositionAndRotation() {
        println("FauxLocalizer: recalculatePositionAndRotation")
    }

    override fun setPositionAndRotation(newPosition: PositionAndRotation) {
        println("FauxLocalizer: setPositionAndRotation= $newPosition")

    }
}