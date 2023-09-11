//package us.brainstormz.examples
//
//import com.qualcomm.robotcore.hardware.HardwareMap
//import us.brainstormz.hardwareClasses.DiffySwerveHardware
//import us.brainstormz.hardwareClasses.DiffySwervePod
//import us.brainstormz.motion.SwervePod
//
//class ExampleSwerveHardware: DiffySwerveHardware {
//    override lateinit var pods: List<DiffySwervePod>
//    override lateinit var hwMap: HardwareMap
//
//    override fun init(ahwMap: HardwareMap) {
//        hwMap = ahwMap
//
//        val leftPod = SwervePod()
//        val rightPod = SwervePod()
//        pods = listOf(leftPod, rightPod)
//    }
//}