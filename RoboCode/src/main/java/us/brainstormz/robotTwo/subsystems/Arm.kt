package us.brainstormz.robotTwo.subsystems

import us.brainstormz.operationFramework.Subsystem
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.AxonEncoderReader
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.math.cos

class Arm: Subsystem, DualMovementModeSubsystem {

    data class ArmTarget(
            override val targetPosition: Positions,
            override val movementMode: DualMovementModeSubsystem.MovementMode,
            override val power: Double): DualMovementModeSubsystem.TargetMovementSubsystem {
                constructor(targetPosition: Positions): this(targetPosition= targetPosition, movementMode= DualMovementModeSubsystem.MovementMode.Position, power = 0.0)
            }

    enum class Positions(val angleDegrees:Double) {
        TooFarIn(285.0),
        ClearLiftMovement(240.0),
        In(238.0),
        TransferringTarget(230.0),
        InsideTheBatteryBox(225.0),
        Inish(230.0),
        AutoInitPosition(120.0),
        OkToDropPixels(90.0),
        OutButUnderTwelve(50.0),
        Out(60.0),
        DroppingWithHighPrecision(65.0),
//        Manual(0.0),
    }


    private val outPid = PID("arm/outPid", kp= 0.0026, kd = 0.01)
    private val outHoldingConstant = 0.08
    private val inPid = PID("arm/inPid", kp= 0.0017, ki = 0.00000018)//, kd = 0.000055)
    private val inHoldingConstant = 0.035
    val weightHorizontalDegrees = 235
    val holdingConstantAngleOffset = weightHorizontalDegrees - 180

    fun isArmAtAngle(angleToCheckDegrees: Double, currentAngleDegrees: Double, armPositioningToleranceDegrees: Double = 5.0): Boolean {
        val minAcceptableAngle = angleToCheckDegrees - armPositioningToleranceDegrees
        val maxAcceptableAngle = angleToCheckDegrees + armPositioningToleranceDegrees
        val acceptableRange = minAcceptableAngle..maxAcceptableAngle
        return currentAngleDegrees in acceptableRange
    }

    fun checkIfArmIsAtTarget(armTarget: Arm.Positions, actualArmAngleDegrees: Double): Boolean {
        return when (armTarget) {
            Arm.Positions.ClearLiftMovement -> actualArmAngleDegrees < Arm.Positions.TooFarIn.angleDegrees && actualArmAngleDegrees >= Arm.Positions.ClearLiftMovement.angleDegrees
            Arm.Positions.In -> actualArmAngleDegrees >= (Arm.Positions.In.angleDegrees)
            Arm.Positions.Out -> actualArmAngleDegrees <= (Arm.Positions.OkToDropPixels.angleDegrees + 2)
            else -> isArmAtAngle(armTarget.angleDegrees, actualArmAngleDegrees)
        }
    }

//    fun moveArmTowardPosition(targetPosition: Double) {
//        val power = calcPowerToReachTarget(targetPosition)
//        powerArm(power)
//    }

    override fun powerSubsystem(power: Double, hardware: RobotTwoHardware) {
        hardware.armServo1.power = power
        hardware.armServo2.power = power
    }
    fun getArmAngleDegrees(hardware: RobotTwoHardware): Double {
        //20
        //180
        val encoderReader: AxonEncoderReader = AxonEncoderReader(hardware.armEncoder, -341 + 90.0)
        return  encoderReader.getPositionDegrees()
    }

    private val armAngleMidpointDegrees = 150.0
    fun calcPowerToReachTarget(targetDegrees: Double, currentDegrees: Double): Double {
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

        println("arm pid name: ${pid.name}")
        println("arm target: ${targetDegrees}")
        println("arm error: ${errorDegrees}")
        println("arm pid v: ${pid.v}")
        val pidPower = pid.calcPID(target = targetDegrees, error = errorDegrees)
        println("arm pidPower: $pidPower")
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
//    fun getArmAngleDegrees(): Double {
//        return encoderReader.getPositionDegrees()
//    }

//    fun getArmState(): Arm.Positions = Arm.Positions.entries.firstOrNull { it ->
//        getArmAngleDegrees() == it.angleDegrees
//    } ?: Arm.Positions.In

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