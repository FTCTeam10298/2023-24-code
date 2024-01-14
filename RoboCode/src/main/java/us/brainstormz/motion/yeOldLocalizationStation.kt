package us.brainstormz.motion

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.util.Range
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.hardwareClasses.*
import us.brainstormz.localizer.RRLocalizer
import us.brainstormz.pid.PID
import us.brainstormz.telemetryWizard.TelemetryConsole
import kotlin.math.*

class OdometryDriveMovement(private val console: TelemetryConsole, private val hardware: ThreeWheelOdometry, private val opmode: LinearOpMode) {

    private val rrLocalizer = RRLocalizer(hardware)
    private val drive = MecanumDriveTrain(hardware as MecanumHardware)

    enum class State {
        Running,
        Done
    }

    fun reset() {
        // Start by setting all speeds and error values to 0 and moving into the next state
        drive.drivePowerZero()
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
        val distanceErrorX = target.x - rrLocalizer.currentPositionAndRotation().x

        // Find there error in distance for Y
        val distanceErrorY = target.y - rrLocalizer.currentPositionAndRotation().y

        // Find the error in angle
        var tempAngleError = target.r - rrLocalizer.currentPositionAndRotation().r

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
        val speedX: Double = distancePIDX.calcPID(sin(rrLocalizer.currentPositionAndRotation().r) * distanceErrorY + cos(rrLocalizer.currentPositionAndRotation().r) * -distanceErrorX)
        val speedY: Double = distancePIDY.calcPID(cos(rrLocalizer.currentPositionAndRotation().r) * distanceErrorY + sin(rrLocalizer.currentPositionAndRotation().r) * distanceErrorX)
        val speedA: Double = anglePID.calcPID(angleError)

        console.display(5, "Target Robot X, Error X: ${target.x}, $distanceErrorX")
        console.display(6, "Target Robot Y, Error Y: ${target.y}, $distanceErrorY")
        console.display(7, "Target Robot A, Error A: ${Math.toDegrees(target.r)}, ${Math.toDegrees(angleError)}")
        console.display(8, "Global PositionAndRotation X, Y, A: ${rrLocalizer.currentPositionAndRotation().x}, ${rrLocalizer.currentPositionAndRotation().y}, ${Math.toDegrees(rrLocalizer.currentPositionAndRotation().r)}")
//        console.display(9, "X P, I, D in, P, I, D out: ${distancePIDX.k_p}, ${distancePIDX.k_i}, ${distancePIDX.k_d}, ${distancePIDX.p}, ${distancePIDX.i}, ${distancePIDX.d}")
//        console.display(10, "Y P, I, D in, P, I, D out: ${distancePIDY.k_p}, ${distancePIDY.k_i}, ${distancePIDY.k_d}, ${distancePIDY.p}, ${distancePIDY.i}, ${distancePIDY.d}")
//        console.display(11, "A P, I, D in, P, I, D out: ${anglePID.k_p}, ${anglePID.k_i}, ${anglePID.k_d}, ${anglePID.p}, ${anglePID.i}, ${anglePID.d}")
        console.display(12, "Speed X, Speed Y, Speed A: $speedX, $speedY, $speedA")
        console.display(13, "Raw L, Raw C, Raw R: ${hardware.lOdom.getCurrentPosition()}, ${hardware.cOdom.getCurrentPosition()}, ${hardware.rOdom.getCurrentPosition()}")

        setSpeedAll(speedX, speedY, speedA, 0.0, maxPower)

        return State.Running
    }

    /**
     * Executes goToPosition in LinearOpMode. Uses a while loop to continue updating position and
     * error to drive.
     * @param target The target PositionAndRotation to drive to.
     * @param maxPower The maximum power allowed on the drive motors.
     * @param distancePIDX The PID for the x-y error.
     * @param anglePID The PID for the theta error.
     *
     * @param distanceMin The minimum allowed distance away from the target to terminate.
     * @param angleDegMin The minimum allowed angle away from the target to terminate.
     * @param reset The current State of the robot.
     * @return The new State of the robot.
     */
    fun doGoToPosition(
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

        // Correct degree input to radians as expected by PositionAndRotation-based code
        target.r = Math.toRadians(target.r)

        var state = State.Running
        while (state != State.Done && opmode.opModeIsActive()) {
            state = goToPosition(target, maxPower, distancePIDX, distancePIDY, anglePID, distanceMin, angleDegMin)
            rrLocalizer.recalculatePositionAndRotation()
        }

        drive.drivePowerZero()
        rrLocalizer.recalculatePositionAndRotation()
    }

    /** works */
    fun setSpeedAll(vX: Double, vY: Double, vA: Double, minPower: Double, maxPower: Double) {

        // Calculate theoretical values for motor powers using transformation matrix
//        movement.driveSetPower((y + x - r),
//                               (y - x + r),
//                               (y - x - r),
//                               (y + x + r))
        var fl = vY + vX - vA
        var fr = vY - vX + vA
        var bl = vY - vX - vA
        var br = vY + vX + vA

        // Find the largest magnitude of power and the average magnitude of power to scale down to
        // maxPower and up to minPower
        var max = abs(fl)
        max = max.coerceAtLeast(abs(fr))
        max = max.coerceAtLeast(abs(bl))
        max = max.coerceAtLeast(abs(br))
        val ave = (abs(fl) + abs(fr) + abs(bl) + abs(br)) / 4
        if (max > maxPower) {
            fl *= maxPower / max
            bl *= maxPower / max
            br *= maxPower / max
            fr *= maxPower / max
        } else if (ave < minPower) {
            fl *= minPower / ave
            bl *= minPower / ave
            br *= minPower / ave
            fr *= minPower / ave
        }

        // Range clip just to be safe
        fl = Range.clip(fl, -1.0, 1.0)
        fr = Range.clip(fr, -1.0, 1.0)
        bl = Range.clip(bl, -1.0, 1.0)
        br = Range.clip(br, -1.0, 1.0)

        console.telemetry.addLine("Powers: $fl, $bl, $fr, $br" )
        println("Powers: $fl, $bl, $fr, $br")

        // Set powers
        drive.driveSetPower(-fl, fr, -bl, br)
    }
    /**
     * Executes DoGoToPosition with PIDs optimized.
     * @param target The target PositionAndRotation to drive to.
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
            PID(0.05, 0.01, 0.0),
            PID(0.06, 0.01, 0.0),
            PID(0.05, 0.01, 0.0),
            0.5,
            0.1,
            true,
            opmode
        )
    }

    /**
     * Executes DoGoToPosition with set PIDs optimized for straight driving.
     * @param target The target PositionAndRotation to drive to.
     * @param maxPower The maximum power allowed on the drive motors.
     * @param distanceMin The minimum allowed distance away from the target to terminate.
     */
    fun straightGoToPosition(
        target: PositionAndRotation,
        maxPower: Double,
        distanceMin: Double
    ) {
        doGoToPosition(
            target,
            maxPower,
            PID(0.047, 0.03, 0.01),
            PID(0.047, 0.03, 0.01),
            PID(0.05, 0.01, 0.0),
            distanceMin,
            0.4,
            true,
            opmode
        )
    }

    /**
     * Executes DoGoToPosition with set PIDs optimized for straight driving.
     * @param target The target PositionAndRotation to drive to.
     * @param maxPower The maximum power allowed on the drive motors.
     * @param angleDegMin The minimum allowed distance away from the target to terminate.
     */
     fun turnGoToPosition(
        target: PositionAndRotation,
        maxPower: Double,
        angleDegMin: Double
    ) {
        doGoToPosition(
            target,
            maxPower,
            PID(0.05, 0.01, 0.0),
            PID(0.05, 0.01, 0.0),
            PID(0.4, 0.05, 0.01),
            0.5,
            angleDegMin,
            true,
            opmode
        )
    }

}