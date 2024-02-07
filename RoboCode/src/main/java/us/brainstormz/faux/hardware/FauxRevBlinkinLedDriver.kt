package us.brainstormz.faux.hardware

import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.hardware.PwmControl
import com.qualcomm.robotcore.hardware.ServoController
import com.qualcomm.robotcore.hardware.ServoControllerEx
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType

class FauxRevBlinkinLedDriver: FauxActionTracker, RevBlinkinLedDriver(FauxServoControllerEx(), 0) {
    override val printSignature: String = "RevBlinkinLedDriver"

    override fun setPattern(pattern: BlinkinPattern?) {
        printInput(pattern.toString())
    }
}

class FauxServoControllerEx(): PhoDevice(), ServoControllerEx {
    override val printSignature: String
        get() = TODO("Not yet implemented")

    override fun pwmEnable() {
        TODO("Not yet implemented")
    }

    override fun pwmDisable() {
        TODO("Not yet implemented")
    }

    override fun getPwmStatus(): ServoController.PwmStatus {
        TODO("Not yet implemented")
    }

    override fun setServoPosition(servo: Int, position: Double) {
        TODO("Not yet implemented")
    }

    override fun getServoPosition(servo: Int): Double {
        TODO("Not yet implemented")
    }

    override fun setServoPwmRange(servo: Int, range: PwmControl.PwmRange) {
        TODO("Not yet implemented")
    }

    override fun getServoPwmRange(servo: Int): PwmControl.PwmRange {
        TODO("Not yet implemented")
    }

    override fun setServoPwmEnable(servo: Int) {
        TODO("Not yet implemented")
    }

    override fun setServoPwmDisable(servo: Int) {
        TODO("Not yet implemented")
    }

    override fun isServoPwmEnabled(servo: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun setServoType(servo: Int, servoType: ServoConfigurationType?) {
        TODO("Not yet implemented")
    }

}