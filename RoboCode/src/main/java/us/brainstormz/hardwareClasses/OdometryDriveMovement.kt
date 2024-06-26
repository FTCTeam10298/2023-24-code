package us.brainstormz.hardwareClasses

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.*

class OdometryDriveMovement(private val console: TelemetryConsole, private val hardware: MecOdometry, private val opmode: LinearOpMode): DriveMovement, OdometryDriveTrain(hardware, console) {

    enum class State {
        Running,
        Done
    }

    fun reset() {
        // Start by setting all speeds and error values to 0 and moving into the next state
        drivePowerZero()
    }

    fun goToPosition(
        target: PositionAndRotation,
        maxPower: Double,
        distancePIDX: PID,
        distancePIDY: PID,
        anglePID: PID,
        distanceMin: Double,
        angleDegMin: Double,
    ): State {

        // Find the error in distance for X
        val distanceErrorX = target.x - localizer.currentPositionAndRotation().x

        // Find there error in distance for Y
        val distanceErrorY = target.y - localizer.currentPositionAndRotation().y

        // Find the error in angle
        var tempAngleError = target.r - localizer.currentPositionAndRotation().r

        while (tempAngleError > Math.PI)
            tempAngleError -= Math.PI * 2

        while (tempAngleError < -Math.PI)
            tempAngleError += Math.PI * 2

        val angleError: Double = tempAngleError

        // Find the error in distance
        val distanceError = hypot(distanceErrorX, distanceErrorY)

        // Check to see if we've reached the desired position already
        if (abs(distanceError) <= distanceMin &&
                abs(angleError) <= Math.toRadians(angleDegMin)) {
            return State.Done
        }

        // Calculate the error in x and y and use the PID to find the error in angle
        val speedX: Double = distancePIDX.calcPID(
                target = target,
                error = sin(localizer.currentPositionAndRotation().r) * distanceErrorY + cos(localizer.currentPositionAndRotation().r) * -distanceErrorX)
        val speedY: Double = distancePIDY.calcPID(
                target = target,
                error = cos(localizer.currentPositionAndRotation().r) * distanceErrorY + sin(localizer.currentPositionAndRotation().r) * distanceErrorX)
        val speedA: Double = anglePID.calcPID(
                target = target,
                error = angleError)

        console.display(5, "Target Robot X, Error X: ${target.x}, $distanceErrorX")
        console.display(6, "Target Robot Y, Error Y: ${target.y}, $distanceErrorY")
        console.display(7, "Target Robot A, Error A: ${Math.toDegrees(target.r)}, ${Math.toDegrees(angleError)}")
        console.display(8, "Global Coordinate X, Y, A: ${localizer.currentPositionAndRotation().x}, ${localizer.currentPositionAndRotation().y}, ${Math.toDegrees(localizer.currentPositionAndRotation().r)}")
        console.display(9, "X P, I, D in, P, I, D out: ${distancePIDX.kp}, ${distancePIDX.ki}, ${distancePIDX.kd}, ${distancePIDX.p}, ${distancePIDX.i}, ${distancePIDX.d}")
        console.display(10, "Y P, I, D in, P, I, D out: ${distancePIDY.kp}, ${distancePIDY.ki}, ${distancePIDY.kd}, ${distancePIDY.p}, ${distancePIDY.i}, ${distancePIDY.d}")
        console.display(11, "A P, I, D in, P, I, D out: ${anglePID.kp}, ${anglePID.ki}, ${anglePID.kd}, ${anglePID.p}, ${anglePID.i}, ${anglePID.d}")
        console.display(12, "Speed X, Speed Y, Speed A: $speedX, $speedY, $speedA")
        console.display(13, "Raw L, Raw C, Raw R: ${hardware.lOdom.getCurrentPosition()}, ${hardware.cOdom.getCurrentPosition()}, ${hardware.rOdom.getCurrentPosition()}")

        setSpeedAll(speedX, speedY, speedA, 0.0, maxPower)

        return State.Running
    }

    /**
     * Executes goToPosition in LinearOpMode. Uses a while loop to continue updating position and
     * error to drive.
     * @param target The target Coordinate to drive to.
     * @param maxPower The maximum power allowed on the drive motors.
     * @param distancePIDX The PID for the x-y error.
     * @param anglePID The PID for the theta error.
     *
     * @param distanceMin The minimum allowed distance away from the target to terminate.
     * @param angleDegMin The minimum allowed angle away from the target to terminate.
     * @param reset The current State of the robot.
     * @return The new State of the robot.
     */
    override fun doGoToPosition(
        target: PositionAndRotation,
        maxPower: Double,
        distancePIDX: PID,
        distancePIDY: PID,
        anglePID: PID,
        distanceMin: Double,
        angleDegMin: Double,
        reset: Boolean,
        opmode: LinearOpMode
    ) {
        if (reset)
            reset()

        // Correct degree input to radians as expected by coordinate-based code
        target.r = Math.toRadians(target.r)

        var state = State.Running
        while (state != State.Done && opmode.opModeIsActive()) {
            state = goToPosition(target, maxPower, distancePIDX, distancePIDY, anglePID, distanceMin, angleDegMin)
            localizer.recalculatePositionAndRotation()
        }

        drivePowerZero()
        localizer.recalculatePositionAndRotation()
    }

    /**
     * Executes DoGoToPosition with PIDs optimized.
     * @param target The target Coordinate to drive to.
     * @param maxPower The maximum power allowed on the drive motors.
     * @param distanceMin The minimum allowed distance away from the target to terminate.
     * @param opmode The LinearOpMode that this call is in. Used to tell if opModeIsActive
     * so that stopping mid-loop doesn't cause an error.
     */
    fun fineTunedGoToPos(
            target: PositionAndRotation
    ) {
        doGoToPosition(
                target,
                1.0,
                PID("x",0.05, 0.01, 0.0),
                PID("y",0.06, 0.01, 0.0),
                PID("angle", 0.05, 0.01, 0.0),
                0.5,
                0.1,
                true,
                opmode
        )
    }

    /**
     * Executes DoGoToPosition with set PIDs optimized for straight driving.
     * @param target The target Coordinate to drive to.
     * @param maxPower The maximum power allowed on the drive motors.
     * @param distanceMin The minimum allowed distance away from the target to terminate.
     */
    override fun straightGoToPosition(
        target: PositionAndRotation,
        maxPower: Double,
        distanceMin: Double
    ) {
        doGoToPosition(
                target,
                maxPower,
                PID("straight/x", 0.047, 0.03, 0.01),
                PID("straight/y", 0.047, 0.03, 0.01),
                PID("straight/angle", 0.05, 0.01, 0.0),
                distanceMin,
                0.4,
                true,
                opmode
        )
    }

    /**
     * Executes DoGoToPosition with set PIDs optimized for straight driving.
     * @param target The target Coordinate to drive to.
     * @param maxPower The maximum power allowed on the drive motors.
     * @param angleDegMin The minimum allowed distance away from the target to terminate.
     */
    override fun turnGoToPosition(
        target: PositionAndRotation,
        maxPower: Double,
        angleDegMin: Double
    ) {
        doGoToPosition(
                target,
                maxPower,
                PID("turn/x", 0.05, 0.01, 0.0),
                PID("turn/y", 0.05, 0.01, 0.0),
                PID("turn/a", 0.4, 0.05, 0.01),
                0.5,
                angleDegMin,
                true,
                opmode
        )
    }


    override fun driveRobotTime(ms: Int, power: Double) {
        TODO("Not yet implemented")
    }

    override fun driveRobotDistanceToObject(power: Double, inches: Double, smartAccel: Boolean) {
        TODO("Not yet implemented")
    }

    override fun driveRobotPosition(power: Double, inches: Double, smartAccel: Boolean) {
        TODO("Not yet implemented")
    }

    override fun driveRobotTurn(power: Double, degree: Double, smartAccel: Boolean) {
        TODO("Not yet implemented")
    }

    override fun driveRobotStrafe(power: Double, inches: Double, smartAccel: Boolean) {
        TODO("Not yet implemented")
    }

    override fun driveSidewaysTime(time: Double, power: Double) {
        TODO("Not yet implemented")
    }

    override fun driveRobotHug(power: Double, inches: Double, hugLeft: Boolean) {
        TODO("Not yet implemented")
    }

    override fun driveRobotArc(power: Double, inches: Double, difference: Double) {
        TODO("Not yet implemented")
    }

    override fun driveRobotArcStrafe(power: Double, inches: Double, difference: Double) {
        TODO("Not yet implemented")
    }

}
