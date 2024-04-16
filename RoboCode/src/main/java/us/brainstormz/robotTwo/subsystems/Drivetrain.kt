package us.brainstormz.robotTwo.subsystems

import kotlinx.serialization.Serializable
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.utils.measured
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.RobotTwoHardware
import java.math.BigDecimal
import kotlin.math.cos
import kotlin.math.sin

class Drivetrain(hardware: RobotTwoHardware, localizer: Localizer, private val telemetry: Telemetry): DualMovementModeSubsystem, MecanumMovement(localizer, hardware, telemetry) {

    @Serializable
    data class DrivetrainPower(val x: Double = 0.0, val y: Double = 0.0, val r: Double = 0.0) {
        constructor() : this(0.0, 0.0, 0.0)
        operator fun plus(other: DrivetrainPower): DrivetrainPower {
            return DrivetrainPower(
                    x = x + other.x,
                    y = y + other.y,
                    r = r + other.r
            )
        }
    }

    @Serializable
    data class DrivetrainTarget(
            override val targetPosition: PositionAndRotation,
            override val movementMode: DualMovementModeSubsystem.MovementMode,
            override val power: DrivetrainPower): DualMovementModeSubsystem.TargetMovementSubsystem {
        constructor(targetPosition: PositionAndRotation): this(targetPosition, DualMovementModeSubsystem.MovementMode.Position, DrivetrainPower())
        constructor(power: DrivetrainPower): this(PositionAndRotation(), DualMovementModeSubsystem.MovementMode.Power, power)
    }

    fun getPosition(): PositionAndRotation = measured("drivetrain getPosition"){
        localizer.recalculatePositionAndRotation()
        localizer.currentPositionAndRotation()
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

        telemetry.addLine("distanceErrorX: $distanceErrorX")
        telemetry.addLine("distanceErrorY: $distanceErrorY")
        telemetry.addLine("angleError: $angleError")

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
//            xInchesPerMili = 0.25 / 1000.0,
//            yInchesPerMili = 0.25 / 1000.0,
//            rDegreesPerMili = 5 / 1000.0
            xInchesPerMilli = 1.toBigDecimal() / 1000.toBigDecimal(),
            yInchesPerMilli = 1.toBigDecimal() / 1000.toBigDecimal(),
            rDegreesPerMilli = 10.toBigDecimal() / 1000.toBigDecimal()
    )
    fun checkIfDrivetrainIsAtPosition(targetPosition: PositionAndRotation, actualWorld: ActualWorld, previousWorld: ActualWorld, precisionInches: Double, precisionDegrees: Double): Boolean {
        val isRobotCurrentlyAtTarget = isRobotAtPosition(
                currentPosition= actualWorld.actualRobot.positionAndRotation,
                targetPosition= targetPosition,
                precisionInches= precisionInches,
                precisionDegrees= precisionDegrees)
        val willRobotStayAtTarget = getVelocity(actualWorld, previousWorld).checkIfIsLessThanOrEqualTo(maxVelocityToStayAtPosition)
        return isRobotCurrentlyAtTarget && willRobotStayAtTarget
    }

    data class DriveVelocity(val xInchesPerMilli: BigDecimal, val yInchesPerMilli: BigDecimal, val rDegreesPerMilli: BigDecimal) {
        fun checkIfIsLessThanOrEqualTo(other: DriveVelocity): Boolean {
            val xIsLess = xInchesPerMilli <= other.xInchesPerMilli

            val yIsLess = yInchesPerMilli <= other.yInchesPerMilli

            val rIsLess = rDegreesPerMilli <= other.rDegreesPerMilli

            return xIsLess && yIsLess && rIsLess
        }
    }

    fun getVelocity(actualPosition: PositionAndRotation, actualTimeMilis: Long, previousPosition: PositionAndRotation, previousTimeMilis: Long): DriveVelocity {
        val deltaX = actualPosition.x - previousPosition.x
        val deltaY = actualPosition.y - previousPosition.y
        val deltaR = actualPosition.r - previousPosition.r
        val deltaTimeMillis = actualTimeMilis - previousTimeMilis

        return DriveVelocity(
                xInchesPerMilli = deltaX.toBigDecimal() / deltaTimeMillis.toBigDecimal(),
                yInchesPerMilli = deltaY.toBigDecimal() / deltaTimeMillis.toBigDecimal(),
                rDegreesPerMilli = deltaR.toBigDecimal() / deltaTimeMillis.toBigDecimal()
        )
    }

    fun getVelocity(actualWorld: ActualWorld, previousWorld: ActualWorld): DriveVelocity {
        return getVelocity(actualWorld.actualRobot.positionAndRotation, actualWorld.timestampMilis, previousWorld.actualRobot.positionAndRotation, previousWorld.timestampMilis)
    }

    fun checkIfRobotIsMoving(actualWorld: ActualWorld, previousWorld: ActualWorld): Boolean {
        return getVelocity(actualWorld, previousWorld).checkIfIsLessThanOrEqualTo(maxVelocityToStayAtPosition)
    }
}