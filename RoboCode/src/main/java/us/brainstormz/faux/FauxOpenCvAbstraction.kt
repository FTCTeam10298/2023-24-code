package us.brainstormz.faux

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.HardwareMap
import org.opencv.core.Mat
import us.brainstormz.openCvAbstraction.OpenCvAbstraction

class FauxOpenCvAbstraction(opmode: OpMode): OpenCvAbstraction(opmode) {
    override fun init(hardwareMap: HardwareMap) {
        println("initializing FauxOpenCvAbstraction")
    }

    override fun start() {
        println("starting FauxOpenCvAbstraction")
    }

    override fun stop() {
        println("stopping FauxOpenCvAbstraction")
    }

    override fun onNewFrame(function: (Mat) -> Mat) {
        println("onNewFrame FauxOpenCvAbstraction")
    }
}