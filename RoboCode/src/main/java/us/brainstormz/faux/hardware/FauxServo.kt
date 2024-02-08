package us.brainstormz.faux.hardware

import com.qualcomm.robotcore.hardware.CRServo
import com.qualcomm.robotcore.hardware.Servo
import com.qualcomm.robotcore.hardware.ServoController

class FauxServo: Servo, FauxDevice() {
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
        printInput("setDirection $direction")
    }

    override fun getDirection(): Servo.Direction {
        printInput("Not yet implemented")
        return Servo.Direction.FORWARD
    }

    override fun setPosition(position: Double) {
        printInput("setPosition $position")
    }

    override fun getPosition(): Double {
        printInput("Not yet implemented")
        return 0.0
    }

    override fun scaleRange(min: Double, max: Double) {
        printInput("Not yet implemented")
    }

}

class FauxCRServo: FauxDcMotorSimple(), CRServo {
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