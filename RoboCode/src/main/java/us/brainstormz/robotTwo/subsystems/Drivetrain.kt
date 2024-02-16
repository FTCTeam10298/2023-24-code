package us.brainstormz.robotTwo.subsystems

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class Drivetrain(hardware: RobotTwoHardware, localizer: Localizer, telemetry: Telemetry): DualMovementModeSubsystem, MecanumMovement(localizer, hardware, telemetry) {
    data class DrivetrainPower(val x: Double, val y: Double, val r: Double) {
        constructor() : this(0.0, 0.0, 0.0)
    }

    data class DrivetrainTarget(
            override val targetPosition: PositionAndRotation,
            override val movementMode: DualMovementModeSubsystem.MovementMode,
            override val power: DrivetrainPower): DualMovementModeSubsystem.TargetMovementSubsystem {
        constructor(targetPosition: PositionAndRotation): this(targetPosition, DualMovementModeSubsystem.MovementMode.Position, DrivetrainPower())
        constructor(power: DrivetrainPower): this(PositionAndRotation(), DualMovementModeSubsystem.MovementMode.Power, power)
    }

    fun getPosition(): PositionAndRotation {
        localizer.recalculatePositionAndRotation()
        return localizer.currentPositionAndRotation()
    }

    fun actuateDrivetrain(target: DrivetrainTarget, actualPosition: PositionAndRotation) {
        val power = when (target.movementMode) {
            DualMovementModeSubsystem.MovementMode.Position -> {
                calcPowerToTarget(target.targetPosition, actualPosition)
            }
            DualMovementModeSubsystem.MovementMode.Power -> {
                target.power
            }
        }
        powerDrivetrain(power)
    }

    fun powerDrivetrain(power: DrivetrainPower) {
        setSpeedAll(vX = power.x, vY= power.y, vA= power.r, minPower = -1.0, maxPower = 1.0)
    }

    fun calcPowerToTarget(target: PositionAndRotation,
                          actual: PositionAndRotation,
                          yTranslationPID: PID = defaultYTranslationPID,
                          xTranslationPID: PID = defaultXTranslationPID,
                          rotationPID: PID = defaultRotationPID): DrivetrainPower {
        val angleRad = Math.toRadians(actual.r)

        val distanceErrorX = target.x - actual.x
        val distanceErrorY = target.y - actual.y

        var tempAngleError = Math.toRadians(target.r) - angleRad

        while (tempAngleError > Math.PI)
            tempAngleError -= Math.PI * 2

        while (tempAngleError < -Math.PI)
            tempAngleError += Math.PI * 2

        val angleError: Double = tempAngleError

//        // Find the error in distance
//        val distanceError = hypot(distanceErrorX, distanceErrorY)
//
//        // Check to see if we've reached the desired position already
//        return if (abs(distanceError) <= precisionInches &&
//                abs(angleError) <= Math.toRadians(precisionDegrees)) {
//            DrivetrainPower(0.0, 0.0, 0.0)
//        } else {
            val speedX: Double = xTranslationPID.calcPID(sin(angleRad) * distanceErrorY + cos(angleRad) * distanceErrorX)
            val speedY: Double = yTranslationPID.calcPID(cos(angleRad) * distanceErrorY + sin(angleRad) * -distanceErrorX)
            val speedA: Double = rotationPID.calcPID(angleError)

            return DrivetrainPower(speedX, speedY, speedA)
//        }
    }
}