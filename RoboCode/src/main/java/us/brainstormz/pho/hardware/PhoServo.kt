package us.brainstormz.pho.hardware

import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.ServoController
import us.brainstormz.pho.hardware.PhoDcMotorSimple
import us.brainstormz.pho.hardware.PhoDevice

class PhoServo: Servo, PhoDevice() {
    override val printSignature: String = "Servo"

    override fun getController(): ServoController {
        printInput("Not yet implemented")
        return TODO()
    }

    override fun getPortNumber(): Int {
        printInput("Not yet implemented")
        return 0
    }

    override fun setDirection(direction: Servo.Direction?) {
        printInput("Not yet implemented")
    }

    override fun getDirection(): Servo.Direction {
        printInput("Not yet implemented")
        return Servo.Direction.FORWARD
    }

    override fun setPosition(position: Double) {
        printInput("Not yet implemented")
    }

    override fun getPosition(): Double {
        printInput("Not yet implemented")
        return 0.0
    }

    override fun scaleRange(min: Double, max: Double) {
        printInput("Not yet implemented")
    }

}

class PhoCRServo: PhoDcMotorSimple(), CRServo {
    override val printSignature = "CRServo"

    override fun getController(): ServoController {
        printInput("Not yet implemented")
        return TODO()
    }

    override fun getPortNumber(): Int {
        printInput("Not yet implemented")
        return 0
    }
}