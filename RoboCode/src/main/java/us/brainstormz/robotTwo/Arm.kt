package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.AnalogInput
import com.qualcomm.robotcore.hardware.CRServo
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.pid.PID
import kotlin.math.cos

class Arm(private val encoder: AnalogInput, private val armServo1: CRServo, private val armServo2: CRServo, private val telemetry: Telemetry) {
    enum class Positions(val angleDegrees:Double) {
        Manual(0.0),
        LiftIsGoingHome(255.0),
        In(248.0),
        TransferringTarget(238.0),
//        Horizontal(180.0),
        Out(60.0)
    }

    private val encoderReader: AxonEncoderReader = AxonEncoderReader(encoder, 7.0-40)

    private val outPid = PID(kp= 0.0026, kd = 0.01)
    private val outHoldingConstant = 0.08
    private val inPid = PID(kp= 0.0015, ki = 0.00000035, kd = 0.000055)
    private val inHoldingConstant = 0.035
    val weightHorizontalDegrees = 235
    val holdingConstantAngleOffset = weightHorizontalDegrees - 180

    fun isArmAtAngle(angleToCheckDegrees: Double, armPositioningToleranceDegrees: Double = 5.0): Boolean {
        val minAcceptableAngle = angleToCheckDegrees - armPositioningToleranceDegrees
        val maxAcceptableAngle = angleToCheckDegrees + armPositioningToleranceDegrees
        val acceptableRange = minAcceptableAngle..maxAcceptableAngle
        return getArmAngleDegrees() in acceptableRange
    }

    fun moveArmTowardPosition(targetPosition: Double) {
        telemetry.addLine("Powering arm toward: $targetPosition")
        val power = calcPowerToReachTarget(targetPosition)
        powerArm(power)
    }

    fun powerArm(power: Double) {
        armServo1.power = power
        armServo2.power = power
    }

    private var previousIsisArmTargetInOfMidpoint = false
    private val armAngleMidpointDegrees = 150.0
    fun calcPowerToReachTarget(targetDegrees: Double): Double {
        val currentDegrees = getArmAngleDegrees()
        val errorDegrees = (targetDegrees - currentDegrees) % 360

        val isArmTargetInOfMidpoint = targetDegrees > armAngleMidpointDegrees
        val pid: PID = when (isArmTargetInOfMidpoint) {
            true -> inPid
            false -> outPid
        }
        val holdingConstant: Double = when (isArmTargetInOfMidpoint) {
            true -> inHoldingConstant
            false -> outHoldingConstant
        }

        if (isArmTargetInOfMidpoint && !previousIsisArmTargetInOfMidpoint) {
            //Target just changed to in
            inPid.reset()
        }
        if (!isArmTargetInOfMidpoint && previousIsisArmTargetInOfMidpoint) {
            //Target just changed to in
            outPid.reset()
        }

//        telemetry.addLine("previousIsisArmTargetInOfMidpoint: $previousIsisArmTargetInOfMidpoint")
//        telemetry.addLine("isArmTargetInOfMidpoint: $isArmTargetInOfMidpoint")
        previousIsisArmTargetInOfMidpoint = isArmTargetInOfMidpoint

        val pidPower = pid.calcPID(errorDegrees)
        val gravityCompPower = (holdingConstant * cos(Math.toRadians(currentDegrees - holdingConstantAngleOffset)))
        val power = pidPower + gravityCompPower
//        telemetry.addLine("Arm currentDegrees: $currentDegrees")
//        telemetry.addLine("Arm raw currentDegrees: ${encoderReader.getRawPositionDegrees()}")
//        telemetry.addLine("Arm encoder voltage: ${encoder.voltage}")
//        telemetry.addLine("Arm encoder MAX voltage: ${encoder.maxVoltage}")
//        telemetry.addLine("Arm targetDegrees: $targetDegrees")
//        telemetry.addLine("Arm errorDegrees no wrap: ${targetDegrees - currentDegrees}")
//        telemetry.addLine("Arm errorDegrees: $errorDegrees")
//        telemetry.addLine("Arm pidPower: $pidPower")
//        telemetry.addLine("Arm gravityCompPower: $gravityCompPower")
//        telemetry.addLine("Arm power: $power")
        return power
    }

    /** 0 angle is where the flat face of the claws is facing parallel to the ground */
    fun getArmAngleDegrees(): Double {
        return encoderReader.getPositionDegrees()
    }

    fun getArmState(): Arm.Positions = Arm.Positions.entries.firstOrNull { it ->
        getArmAngleDegrees() == it.angleDegrees
    } ?: Arm.Positions.In

}

//@Autonomous
//class ArmTest: OpMode() {
//    private val hardware = RobotTwoHardware(telemetry, this)
//    val movement = MecanumDriveTrain(hardware)
//    private lateinit var arm: Arm
//
//    override fun init() {
//        /** INIT PHASE */
//        hardware.init(hardwareMap)
//        arm = Arm(
//                encoder= hardware.armEncoder,
//                armServo1= hardware.armServo1,
//                armServo2= hardware.armServo2, telemetry)
//    }
//
//    override fun loop() {
//
//        arm.powerArm(arm.holdingConstant * cos(Math.toRadians(arm.getArmAngleDegrees() - arm.holdingConstantAngleOffset)))
//
//        telemetry.addLine("power: ${hardware.armServo1.power}")
//        telemetry.addLine("angle: ${arm.getArmAngleDegrees()}")
//        telemetry.addLine("voltage: ${hardware.armEncoder.voltage}")
//        telemetry.update()
//    }
//}