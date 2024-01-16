package us.brainstormz.localizer

//import com.acmerobotics.roadrunner.geometry.Pose2d
import us.brainstormz.hardwareClasses.ThreeWheelOdometry

//import us.brainstormz.paddieMatrick.PaddieMatrickHardware
//import us.brainstormz.roadRunner.drive.SampleMecanumDrive

class RRLocalizer(hardware: ThreeWheelOdometry): Localizer {
    //val roadRunner = SampleMecanumDrive(hardware.hwMap)

    override fun currentPositionAndRotation(): PositionAndRotation {
        TODO()
//        val (x, y, heading) = roadRunner.poseEstimate
//        //rr switches x and y
//        return PositionAndRotation(x= y, y= x, r= Math.toDegrees(heading))
    }

    override fun recalculatePositionAndRotation() {
        //roadRunner.update()
    }

    override fun setPositionAndRotation(newPosition: PositionAndRotation) {
        //roadRunner.localizer.poseEstimate = Pose2d(y ?:0.0, x ?:0.0, Math.toRadians(r ?:0.0))
    }

//    override fun startNewMovement() {
//        print("startNewMovement does nothing")
//    }
}

/*class Foo(private val hardware: PaddieMatrickHardware, private val telemetry:Telemetry){
    val localizer = RRLocalizer(hardware)

    fun loop(){
        localizer.recalculatePositionAndRotation()
        telemetry.addLine("current pos: ${localizer.currentPositionAndRotation()}")
        telemetry.update()
    }
}

@Autonomous
class RRLocalizerTest: OpMode() {
    var foo:Foo? = null
    val hardware = PaddieMatrickHardware()
    val movement = MecanumDriveTrain(hardware)

    override fun init() {
        hardware.init(hardwareMap)
        foo = Foo(hardware, telemetry)
        hardware.lFDrive.zeroPowerBehavior = ZeroPowerBehavior.FLOAT
        hardware.rFDrive.zeroPowerBehavior =  ZeroPowerBehavior.FLOAT
        hardware.lBDrive.zeroPowerBehavior =  ZeroPowerBehavior.FLOAT
        hardware.rBDrive.zeroPowerBehavior =  ZeroPowerBehavior.FLOAT
    }

    override fun loop() {
        hardware.lFDrive.zeroPowerBehavior = ZeroPowerBehavior.FLOAT
        hardware.rFDrive.zeroPowerBehavior =  ZeroPowerBehavior.FLOAT
        hardware.lBDrive.zeroPowerBehavior =  ZeroPowerBehavior.FLOAT
        hardware.rBDrive.zeroPowerBehavior =  ZeroPowerBehavior.FLOAT
        foo?.loop()
    }
}*/