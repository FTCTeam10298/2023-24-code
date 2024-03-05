package us.brainstormz.faux

import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.utils.printToLogcat


class FauxLocalizer: Localizer {
    override fun currentPositionAndRotation(): PositionAndRotation {
        printToLogcat("FauxLocalizer: currentPositionAndRotation")
        return PositionAndRotation()
    }

    override fun recalculatePositionAndRotation() {
        printToLogcat("FauxLocalizer: recalculatePositionAndRotation")
    }

    override fun setPositionAndRotation(newPosition: PositionAndRotation) {
        printToLogcat("FauxLocalizer: setPositionAndRotation= $newPosition")

    }
}