////
//// Source code recreated from a .class file by IntelliJ IDEA
//// (powered by FernFlower decompiler)
////
//
//package us.brainstormz.localizer.aprilTagLocalization.aprilTagAnalysis;
//
//import java.util.ArrayList;
//import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
//import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
//import org.firstinspires.ftc.vision.VisionProcessor;
//import org.firstinspires.ftc.vision.apriltag.AprilTagGameDatabase;
//import org.firstinspires.ftc.vision.apriltag.AprilTagLibrary;
//import us.brainstormz.localizer.aprilTagLocalization.aprilTagAnalysis.AprilTagProcessorImpl;
//import org.openftc.apriltag.AprilTagDetection;
//import org.openftc.apriltag.AprilTagDetectorJNI;
//
//public abstract class AprilTagProcessor implements VisionProcessor {
//    public static final int THREADS_DEFAULT = 3;
//
//    public AprilTagProcessor() {
//    }
//
//    public static AprilTagProcessor easyCreateWithDefaults() {
//        return (new Builder()).build();
//    }
//
//    public abstract void setDecimation(float var1);
//
//    public abstract void setPoseSolver(PoseSolver var1);
//
//    public abstract int getPerTagAvgPoseSolveTime();
//
//    public abstract ArrayList<AprilTagDetection> getDetections();
//
//    public abstract ArrayList<AprilTagDetection> getFreshDetections();
//
//    public static enum PoseSolver {
//        APRILTAG_BUILTIN(-1),
//        OPENCV_ITERATIVE(0),
//        OPENCV_SOLVEPNP_EPNP(1),
//        OPENCV_IPPE(6),
//        OPENCV_IPPE_SQUARE(7),
//        OPENCV_SQPNP(8);
//
//        final int code;
//
//        private PoseSolver(int code) {
//            this.code = code;
//        }
//    }
//
//    public static class Builder {
//        private double fx;
//        private double fy;
//        private double cx;
//        private double cy;
//        private TagFamily tagFamily;
//        private AprilTagLibrary tagLibrary;
//        private DistanceUnit outputUnitsLength;
//        private AngleUnit outputUnitsAngle;
//        private int threads;
//        private boolean drawAxes;
//        private boolean drawCube;
//        private boolean drawOutline;
//        private boolean drawTagId;
//
//        public Builder() {
//            this.tagFamily = AprilTagProcessor.TagFamily.TAG_36h11;
//            this.tagLibrary = AprilTagGameDatabase.getCurrentGameTagLibrary();
//            this.outputUnitsLength = DistanceUnit.INCH;
//            this.outputUnitsAngle = AngleUnit.DEGREES;
//            this.threads = 3;
//            this.drawAxes = false;
//            this.drawCube = false;
//            this.drawOutline = true;
//            this.drawTagId = true;
//        }
//
//        public Builder setLensIntrinsics(double fx, double fy, double cx, double cy) {
//            this.fx = fx;
//            this.fy = fy;
//            this.cx = cx;
//            this.cy = cy;
//            return this;
//        }
//
//        public Builder setTagFamily(TagFamily tagFamily) {
//            this.tagFamily = tagFamily;
//            return this;
//        }
//
//        public Builder setTagLibrary(AprilTagLibrary tagLibrary) {
//            this.tagLibrary = tagLibrary;
//            return this;
//        }
//
//        public Builder setOutputUnits(DistanceUnit distanceUnit, AngleUnit angleUnit) {
//            this.outputUnitsLength = distanceUnit;
//            this.outputUnitsAngle = angleUnit;
//            return this;
//        }
//
//        public Builder setDrawAxes(boolean drawAxes) {
//            this.drawAxes = drawAxes;
//            return this;
//        }
//
//        public Builder setDrawCubeProjection(boolean drawCube) {
//            this.drawCube = drawCube;
//            return this;
//        }
//
//        public Builder setDrawTagOutline(boolean drawOutline) {
//            this.drawOutline = drawOutline;
//            return this;
//        }
//
//        public Builder setDrawTagID(boolean drawTagId) {
//            this.drawTagId = drawTagId;
//            return this;
//        }
//
//        public Builder setNumThreads(int threads) {
//            this.threads = threads;
//            return this;
//        }
//
//        public AprilTagProcessor build() {
//            if (this.tagLibrary == null) {
//                throw new RuntimeException("Cannot create AprilTagProcessor without setting tag library!");
//            } else if (this.tagFamily == null) {
//                throw new RuntimeException("Cannot create AprilTagProcessor without setting tag family!");
//            } else {
//                return new AprilTagProcessorImpl(this.fx, this.fy, this.cx, this.cy, this.outputUnitsLength, this.outputUnitsAngle, this.tagLibrary, this.drawAxes, this.drawCube, this.drawOutline, this.drawTagId, this.tagFamily, this.threads);
//            }
//        }
//    }
//
//    public static enum TagFamily {
//        TAG_36h11(org.openftc.apriltag.AprilTagDetectorJNI.TagFamily.TAG_36h11),
//        TAG_25h9(org.openftc.apriltag.AprilTagDetectorJNI.TagFamily.TAG_25h9),
//        TAG_16h5(org.openftc.apriltag.AprilTagDetectorJNI.TagFamily.TAG_16h5),
//        TAG_standard41h12(org.openftc.apriltag.AprilTagDetectorJNI.TagFamily.TAG_standard41h12);
//
//        final AprilTagDetectorJNI.TagFamily ATLibTF;
//
//        private TagFamily(AprilTagDetectorJNI.TagFamily ATLibTF) {
//            this.ATLibTF = ATLibTF;
//        }
//    }
//}
