package us.brainstormz.robotTwo.subsystems

import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.robotTwo.RobotTwoHardware

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

    fun actuateDrivetrain(target: DrivetrainTarget, actualPosition: PositionAndRotation) {
        when (target.movementMode) {
            DualMovementModeSubsystem.MovementMode.Position -> {
                moveTowardTarget(target.targetPosition)
            }
            DualMovementModeSubsystem.MovementMode.Power -> {
                powerDrivetrain(target.power)
            }
        }
    }

    fun powerDrivetrain(power: DrivetrainPower) {
        setSpeedAll(vX = power.x, vY= power.y, vA= power.r, minPower = -1.0, maxPower = 1.0)
    }
}