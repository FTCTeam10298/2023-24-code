package us.brainstormz.pathexecution

import android.os.Build
import androidx.annotation.RequiresApi
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.Movement
import us.brainstormz.path.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

class MecanumPathExecutor(private val movement: Movement, var granularity: Int = 10):PathExecutor {

    override fun doPath(path: Path): Future<Boolean> {

        for(distance in 0..granularity) {
            val distanceDouble = (distance / granularity).toDouble()
            val targetPosition = path.positionAt(distanceDouble)

            var atTarget = false
            while (!atTarget)
                atTarget = movement.moveTowardTarget(targetPosition)

        }

        return CompletableFuture()
    }
}