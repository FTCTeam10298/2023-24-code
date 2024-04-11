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

    private lateinit var localizer: RRTwoWheelLocalizer
    private lateinit var drivetrain: Drivetrain

    override fun init() {
        hardware.init(hardwareMap)

        localizer = RRTwoWheelLocalizer(hardware, hardware.inchesPerTick)
        drivetrain = Drivetrain(hardware, localizer, telemetry)
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

        if (!(atTarget && newTarget == PositionAndRotation())) {
            drivetrain.actuateDrivetrain(Drivetrain.DrivetrainTarget(newTarget), Drivetrain.DrivetrainTarget(previousTarget), currentPosition)
        } else {
            drivetrain.powerDrivetrain(Drivetrain.DrivetrainPower())
            telemetry.addLine("drivetrain power 0")
        }

        telemetry.addLine("currentPosition: $currentPosition")
        telemetry.addLine("targetPosition: $newTarget")
        val deltaPosition = currentPosition - PositionAndRotation()
        telemetry.addLine("deltaPosition: $deltaPosition")
        telemetry.update()


        previousInchesMovedSinceStart = newInchesMovedSinceStart
        previousTarget = newTarget
        lastActualPosition = currentPosition
    }
}