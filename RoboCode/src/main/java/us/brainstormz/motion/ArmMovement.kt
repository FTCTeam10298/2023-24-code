package us.brainstormz.motion

import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.HardwareMap
import us.brainstormz.hardwareClasses.HardwareClass
import us.brainstormz.pid.PID
import kotlin.math.cos

class ArmMovement(private val kp: Double, private val ki: Double, private val kd: Double, private val Kcos: Double) {

    val pid = PID(kp, ki, kd)

    fun calcPower(current: Double, target: Double): Double {
        val gravityComp = cos(target) * Kcos

        return pid.calcPID(target, current) + gravityComp //+ Kv * (referenceSpeed) + Ka * referenceAccel;
    }

}

fun main() {
    val armMovement = ArmMovement(0.00001, 0.0, 0.0, 1.0)


    val power = armMovement.calcPower(1.0, 180.0)
    println(power)
}

@TeleOp
class armMovementTest: OpMode() {

    val hardware = armMap()
    val armMovement = ArmMovement(0.00001, 0.0, 0.0, 1.0)

    override fun init() {
        hardware.init(hardwareMap)
        telemetry.addLine("hi there")
        telemetry.addLine(hardware.arm.currentPosition.toString())
    }

    override fun loop() {

        telemetry.addLine(hardware.arm.currentPosition.toString())
        val power = armMovement.calcPower(hardware.arm.currentPosition.toDouble(), 0.0)

        hardware.arm.power = power

    }
}

class armMap: HardwareClass {
    override lateinit var hwMap: HardwareMap
    lateinit var arm: DcMotorEx
    lateinit var allHubs: List<LynxModule>

    override fun init(ahwMap: HardwareMap) {
        hwMap = ahwMap

        arm = hwMap["arm"] as DcMotorEx
//        allHubs = hwMap.getAll(LynxModule::class.java)
//        val ctrlHub = SmartLynxModule(allHubs[0])
//
//        arm = ctrlHub.getMotor(0)
    }

}