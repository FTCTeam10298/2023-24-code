package posePlanner

import locationTracking.PosAndRot
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class PosePlanner {
    var aStar = AStar(PosAndRot(), PosAndRot(), listOf(), this)
    var hitPoints = listOf<PosAndRot>()

    val preLoadedPaths = listOf(BezierPath())
    val undesirableAreas: List<UndesirableArea> = listOf()
    var obstructions: List<Obstruction> = listOf()

    val hitRadius = 1.0

    /**
    PATH GEN
     */
    fun generatePath(start: PosAndRot, target: PosAndRot): List<PosAndRot> {
        println("\n\n\nNew Path=\n")

        val aStar = AStar(start, target, obstructions, this)

        this.aStar = aStar
        hitPoints = listOf()

        var currentNode = AStarPoint(start)

        while (currentNode.point != target){
            val hitbox = Hitbox(Hitbox().createHitbox(Line(currentNode.point, target)))

            /**Choose*/
            currentNode = aStar.cheapestNode()
            println("\nNew Node: ${currentNode.point}\n")

            val collision = hitbox.collides(obstructions)
            println("Collision ${collision?.first}\n")

            if (collision == null) {
                currentNode = AStarPoint(target, 0.0, 0.0, currentNode, 0.0)
                break
            }

            val adjustedCollision =
                collision.first.coordinateAlongLine(.2, start)
            val adjustedAStar = AStarPoint(adjustedCollision, currentNode.gCost + adjustedCollision.distance(currentNode.point), adjustedCollision.distance(target), currentNode, 0.0)

            hitPoints += adjustedCollision

            /**Find Neighbors*/
            val neighbors = if (collision.first != target)
                findAround(adjustedCollision, collision.second).filter{
                    val newHitbox = Hitbox(Hitbox().createHitbox(Line(adjustedCollision, it)))
                    hitbox.collides(obstructions) == null
                }
            else
                listOf(target)
            aStar.addNeighbors(neighbors.map { AStarPoint(it, 0.0, 0.0, adjustedAStar, 0.0) })

            val trueNeighbors = aStar.openSet.filter {
                val newHitbox = Hitbox(Hitbox().createHitbox(Line(adjustedCollision, it.point)))
                hitbox.collides(obstructions) == null
            }

            /**Expand*/
            aStar.expand(adjustedAStar, trueNeighbors)
        }

        val rtn = aStar.tracePath(currentNode)
        return cutCorners(cutCorners(rtn))
    }

    private fun createHitPath(line: Line): Pair<Line, Line> {
        val rightAngleToStart = line.end.rotateAround(line.start, PI *.5)
        val startSideA = line.start.coordinateAlongLine(1.0, rightAngleToStart)
        val startSideB = line.start.coordinateAlongLine(-1.0, rightAngleToStart)

        println(rightAngleToStart)
        println("Start: ${line.start}")
        println(startSideA)
        val rightAngleToEnd = line.start.rotateAround(line.end, -PI *.5)
        val endSideA = line.end.coordinateAlongLine(1.0, rightAngleToEnd)
        val endSideB = line.end.coordinateAlongLine(-1.0, rightAngleToEnd)

        return Pair(Line(startSideA, endSideA), Line(startSideB, endSideB))
    }

    private fun findAround(current: PosAndRot, obstruction: Obstruction): List<PosAndRot> {
        val edges = obstruction.poly.getLines()

        val centroid = obstruction.poly.centroid()
        var collisionEdge = edges.first()
        edges.forEach {
            val intersect = it.lineIntersection(Line(current, centroid))
            if (intersect != null && ((collisionEdge.start + collisionEdge.end) / 2).distance(current) > intersect.distance(current))
            collisionEdge = it
        }

        val farthestPoint = obstruction.poly.points.minByOrNull { it.distance(current) }!!
        val movedPoints = listOf(collisionEdge.start, collisionEdge.end).map {
            it.coordinateAlongLine(-.2, centroid).coordinateAlongLine(-.2, farthestPoint)
        }

        return movedPoints
    }



    private fun cutCorners(path: List<PosAndRot>): List<PosAndRot> {
        println("\n")
        println(path)

        val efficientPath: MutableList<PosAndRot> = path.toMutableList()

        var current = path.first()

        while (efficientPath.indexOf(current) + 2 < efficientPath.size) {
            val next = efficientPath[efficientPath.indexOf(current) + 1]
            val nextNext = efficientPath[efficientPath.indexOf(current) + 2]

            println(efficientPath.size)

            val throughAllDistance = current.distance(next) + next.distance(nextNext)
            val directDistance = current.distance(nextNext)

            val newHitbox = Hitbox(Hitbox().createHitbox(Line(current, nextNext)))
            current = if ((directDistance <= throughAllDistance) && newHitbox.collides(obstructions) == null) {

                println(efficientPath.remove(next))
                println(next)
                current
            } else {
                next
            }
        }

        return efficientPath
    }

//    fun firstIntersection(l: Line, obstructions: List<Obstruction>): Pair<PosAndRot, Obstruction>? {
//        var result: PosAndRot? = null
//        var intersect: Obstruction? = null
//        obstructions.forEach {
//            val newIntersect = it.poly.intersection(l)
//            if (newIntersect != null)
//                if (result == null || newIntersect.first.distance(l.start) < result!!.distance(l.start)){
//                    result = newIntersect.first
//                    intersect = it
//                }
//        }
//
//        return if (intersect != null)
//            result!! to intersect!!
//        else
//            null
//    }


    /**
    PATH FOLLOWER
     */

//    broke needs work
//    ill fix it later

//
//    private var currentPath: List<BezierCurve>? = null
//    private var currentCurve: BezierCurve? = null
//    private var d = 0.0
//    private val granularity = 0.1
//
//    fun getPositionAndRotation(current: PositionAndRotation, target: PositionAndRotation): PositionAndRotation {
//
////        determine path
//        if (currentPath == null)
//            currentPath = isPathPreLoaded(current.toPoint3D(), target.toPoint3D())
//                        ?: generatePath(current, target)
//
////        determine current curve
//        if (d == 1.0) {
//            val currentPathcurves = currentPath!!.curves
//            currentCurve = currentPathcurves.firstOrNull {
//                it == currentCurve
//            } ?: currentPathcurves.iterator().next()
//            d = 0.0
//        }
//
////        determine d
//        d = calculateD()
//
////        return
//        val nextPoint = currentCurve!!.calculatePoint(d)
//        return nextPoint.toPositionAndRotation()
//    }
//
//    private fun isPathPreLoaded(start: Point3D, end: Point3D): BezierPath? =
//        preLoadedPaths.firstOrNull {
//            it.curves.last().calculatePoint(1.0) == end
//        }
//
//    private fun calculateD(): Double {
//        return d + granularity
//    }

    val pointToPositionAndRotation = Point3D(0.0, 0.0, 0.0)
    private fun Point3D.toPositionAndRotation(): PosAndRot {
        val adjusted = this.copy(x = pointToPositionAndRotation.x,
                                 y = pointToPositionAndRotation.y,
                                 z = pointToPositionAndRotation.z)
        return PosAndRot(x = adjusted.x,
                          y = adjusted.y,
                          r = adjusted.z)
    }
    private fun PosAndRot.toPoint3D(): Point3D {
        val adjusted = this.copy(x = pointToPositionAndRotation.x,
                                 y = pointToPositionAndRotation.y,
                                 r = pointToPositionAndRotation.z)
        return Point3D(x = adjusted.x,
                       y = adjusted.y,
                       z = adjusted.r)
    }

}
