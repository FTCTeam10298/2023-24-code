//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package org.firstinspires.ftc.vision.apriltag

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit
import org.firstinspires.ftc.vision.VisionProcessor
import org.openftc.apriltag.AprilTagDetectorJNI

abstract class AprilTagProcessor : VisionProcessor {
    abstract fun setDecimation(var1: Float)
    abstract fun setPoseSolver(var1: PoseSolver?)
    abstract val perTagAvgPoseSolveTime: Int
    abstract val detections: ArrayList<AprilTagDetection?>?
    abstract val freshDetections: ArrayList<AprilTagDetection?>?

    enum class PoseSolver(val code: Int) {
        APRILTAG_BUILTIN(-1),
        OPENCV_ITERATIVE(0),
        OPENCV_SOLVEPNP_EPNP(1),
        OPENCV_IPPE(6),
        OPENCV_IPPE_SQUARE(7),
        OPENCV_SQPNP(8)
    }

    class Builder {
        private var fx = 0.0
        private var fy = 0.0
        private var cx = 0.0
        private var cy = 0.0
        private var tagFamily: TagFamily?
        private var tagLibrary: AprilTagLibrary?
        private var outputUnitsLength: DistanceUnit
        private var outputUnitsAngle: AngleUnit
        private var threads = 3
        private var drawAxes = false
        private var drawCube = false
        private var drawOutline = true
        private var drawTagId = true

        init {
            tagFamily = TagFamily.TAG_36h11
            tagLibrary = AprilTagGameDatabase.getCurrentGameTagLibrary()
            outputUnitsLength = DistanceUnit.INCH
            outputUnitsAngle = AngleUnit.DEGREES
        }

        fun setLensIntrinsics(fx: Double, fy: Double, cx: Double, cy: Double): Builder {
            this.fx = fx
            this.fy = fy
            this.cx = cx
            this.cy = cy
            return this
        }

        fun setTagFamily(tagFamily: TagFamily?): Builder {
            this.tagFamily = tagFamily
            return this
        }

        fun setTagLibrary(tagLibrary: AprilTagLibrary?): Builder {
            this.tagLibrary = tagLibrary
            return this
        }

        fun setOutputUnits(distanceUnit: DistanceUnit, angleUnit: AngleUnit): Builder {
            outputUnitsLength = distanceUnit
            outputUnitsAngle = angleUnit
            return this
        }

        fun setDrawAxes(drawAxes: Boolean): Builder {
            this.drawAxes = drawAxes
            return this
        }

        fun setDrawCubeProjection(drawCube: Boolean): Builder {
            this.drawCube = drawCube
            return this
        }

        fun setDrawTagOutline(drawOutline: Boolean): Builder {
            this.drawOutline = drawOutline
            return this
        }

        fun setDrawTagID(drawTagId: Boolean): Builder {
            this.drawTagId = drawTagId
            return this
        }

        fun setNumThreads(threads: Int): Builder {
            this.threads = threads
            return this
        }

        fun build(): AprilTagProcessor {
            return if (tagLibrary == null) {
                throw RuntimeException("Cannot create AprilTagProcessor without setting tag library!")
            } else if (tagFamily == null) {
                throw RuntimeException("Cannot create AprilTagProcessor without setting tag family!")
            } else {
                AprilTagProcessorImpl(fx, fy, cx, cy, outputUnitsLength, outputUnitsAngle, tagLibrary, drawAxes, drawCube, drawOutline, drawTagId, tagFamily, threads)
            }
        }
    }

    enum class TagFamily(val ATLibTF: AprilTagDetectorJNI.TagFamily) {
        TAG_36h11(AprilTagDetectorJNI.TagFamily.TAG_36h11),
        TAG_25h9(AprilTagDetectorJNI.TagFamily.TAG_25h9),
        TAG_16h5(AprilTagDetectorJNI.TagFamily.TAG_16h5),
        TAG_standard41h12(AprilTagDetectorJNI.TagFamily.TAG_standard41h12)
    }

    companion object {
        const val THREADS_DEFAULT = 3
        fun easyCreateWithDefaults(): AprilTagProcessor {
            return Builder().build()
        }
    }
}
