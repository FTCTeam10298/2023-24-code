package us.brainstormz.robotTwo.subsystems

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoHardware
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class Drivetrain(hardware: RobotTwoHardware, localizer: Localizer, private val telemetry: Telemetry): DualMovementModeSubsystem, MecanumMovement(localizer, hardware, telemetry) {
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

    fun actuateDrivetrain(target: DrivetrainTarget,
                          previousTarget: DrivetrainTarget,
                          actualPosition: PositionAndRotation,
                          ) {
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
//        setSpeedAll(vX = power.x, vY= power.y, vA= power.r, minPower = 0.0, maxPower = 0.0)
    }

    fun calcPowerToTarget(target: PositionAndRotation,
                          actual: PositionAndRotation,
                          ): DrivetrainPower {
        val angleRad = Math.toRadians(actual.r)

        val distanceErrorX = target.x - actual.x
        val distanceErrorY = target.y - actual.y

        var tempAngleError = Math.toRadians(target.r) - angleRad

        while (tempAngleError > Math.PI)
            tempAngleError -= Math.PI * 2

        while (tempAngleError < -Math.PI)
            tempAngleError += Math.PI * 2

        val angleError: Double = tempAngleError

        val speedX: Double = xTranslationPID.calcPID(
                target = target,
                error = sin(angleRad) * distanceErrorY + cos(angleRad) * distanceErrorX)
        val speedY: Double = yTranslationPID.calcPID(
                target = target,
                error = cos(angleRad) * distanceErrorY + sin(angleRad) * -distanceErrorX)
        val speedA: Double = rotationPID.calcPID(
                target = target,
                error = angleError)

        return DrivetrainPower(speedX, speedY, speedA)
    }

    val maxVelocityToStayAtPosition = DriveVelocity(
            xInchesPerMili = 0.25 / 1000.0,
            yInchesPerMili = 0.25 / 1000.0,
            rDegreesPerMili = 3 / 1000.0
    )
    fun checkIfDrivetrainIsAtPosition(targetPosition: PositionAndRotation, actualWorld: ActualWorld, previousWorld: ActualWorld, precisionInches: Double, precisionDegrees: Double): Boolean {
        val isRobotCurrentlyAtTarget = isRobotAtPosition(
                currentPosition= actualWorld.actualRobot.positionAndRotation,
                targetPosition= targetPosition,
                precisionInches= precisionInches,
                precisionDegrees= precisionDegrees)
        val willRobotStayAtTarget = getVelocity(actualWorld, previousWorld).checkIfIsLessThan(maxVelocityToStayAtPosition)
        return isRobotCurrentlyAtTarget && willRobotStayAtTarget
    }

    data class DriveVelocity(val xInchesPerMili: Double, val yInchesPerMili: Double, val rDegreesPerMili: Double) {
        fun checkIfIsLessThan(other: DriveVelocity): Boolean {
            val xIsLess = xInchesPerMili < other.xInchesPerMili
            val yIsLess = yInchesPerMili < other.yInchesPerMili
            val rIsLess = rDegreesPerMili < other.rDegreesPerMili
            return xIsLess && yIsLess && rIsLess
        }
    }
    fun getVelocity(actualPosition: PositionAndRotation, actualTimeMilis: Long, previousPosition: PositionAndRotation, previousTimeMilis: Long): DriveVelocity {
        val deltaPosition = actualPosition - previousPosition
        val deltaTime = actualTimeMilis - previousTimeMilis

        return DriveVelocity(
                xInchesPerMili = deltaPosition.x / deltaTime,
                yInchesPerMili = deltaPosition.y / deltaTime,
                rDegreesPerMili = deltaPosition.r / deltaTime
        )
    }

    fun getVelocity(actualWorld: ActualWorld, previousWorld: ActualWorld): DriveVelocity {
        return getVelocity(actualWorld.actualRobot.positionAndRotation, actualWorld.timestampMilis, previousWorld.actualRobot.positionAndRotation, previousWorld.timestampMilis)
    }
}