package us.brainstormz.robotTwo.onRobotTests

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.subsystems.Drivetrain
import kotlin.math.hypot

@TeleOp
class TrackingOverMovementTest: OpMode() {
    private val hardware: RobotTwoHardware = RobotTwoHardware(telemetry= telemetry, opmode = this)
    private val localizer = RRTwoWheelLocalizer(hardware, hardware.inchesPerTick)
    private val drivetrain = Drivetrain(hardware, localizer, telemetry)

    override fun init() {
        hardware.init(hardwareMap)
    }

    private val inchesToMoveToAccumulateError: Double = 12*12.0
    private val movementRectangleXInches = 20
    private val movementRectangleYInches = 20

    private var previousTarget = PositionAndRotation()

    private var previousInchesMovedSinceStart = 0.0
    private var lastActualPosition = PositionAndRotation()

    override fun loop() {
        localizer.recalculatePositionAndRotation()
        val currentPosition = localizer.currentPositionAndRotation()

        val inchesMovedSinceLastLoop = hypot(currentPosition.x - lastActualPosition.x, currentPosition.y - lastActualPosition.y)
        val newInchesMovedSinceStart = previousInchesMovedSinceStart + inchesMovedSinceLastLoop

        val atTarget = drivetrain.isRobotAtPosition(previousTarget, currentPosition, precisionInches = 5.0, precisionDegrees = 10.0)
        val newTarget = if (atTarget) {
            val doneWithErrorAccumulationPhase = newInchesMovedSinceStart >= inchesToMoveToAccumulateError

            if (doneWithErrorAccumulationPhase) {
                PositionAndRotation()
            } else {
                PositionAndRotation(
                        x = movementRectangleXInches * Math.random(),
                        y = movementRectangleYInches * Math.random(),
                        r = 180 * Math.random()
                )
            }
        } else {
            previousTarget
        }

        drivetrain.actuateDrivetrain(Drivetrain.DrivetrainTarget(newTarget), Drivetrain.DrivetrainTarget(previousTarget), currentPosition)

        previousInchesMovedSinceStart = newInchesMovedSinceStart
        previousTarget = newTarget
        lastActualPosition = currentPosition
    }
}