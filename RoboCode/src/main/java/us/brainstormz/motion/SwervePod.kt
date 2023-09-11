package us.brainstormz.motion

import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.hardwareClasses.DiffySwervePod

class SwervePod(override val motor1: DcMotor,
                     override val motor2: DcMotor,
                     override val rEncoder: DcMotor) : DiffySwervePod {
    data class MoveParams(private val turnDegrees: Double,
                          val turnPower: Double,
                          val driveRotations: Double,
                          val drivePower: Double) {val turnDegreesLimited = turnDegrees.coerceIn(-180.0..180.0)}
    fun calculateMotorTargets(turn: Double, drive: Double): Pair<Double, Double> {
        val power1 = drive + turn
        val power2 = drive - turn

        return Pair(power1, power2)
    }
    val accuracy = 10

    fun movePod(podParams: MoveParams,
                turnBeforeDriving: Boolean = true) {
        var notAtTarget = true
        while (notAtTarget) {
            notAtTarget = !startMovingPod(podParams, turnBeforeDriving)
        }
    }

    fun startMovingPod(podParams: MoveParams,
                       turnBeforeDriving: Boolean = true): Boolean {
        val turnDegreesWrapped = podParams.turnDegreesLimited.mod(90.0)
        val driveRotations = podParams.driveRotations

        val motorTargets = calculateMotorTargets(turnDegreesWrapped, driveRotations)

        motor1.mode = DcMotor.RunMode.RUN_TO_POSITION
        motor2.mode = DcMotor.RunMode.RUN_TO_POSITION

        motor1.targetPosition = motorTargets.first.toInt()
        motor2.targetPosition = motorTargets.second.toInt()

        movePower(podParams.turnPower, podParams.drivePower)


        val motor1Error = motor1.currentPosition - motor1.targetPosition
        val motor2Error = motor2.currentPosition - motor2.targetPosition
        val accuracy = -accuracy..accuracy
        return motor1Error in accuracy && motor2Error in accuracy
    }

    fun movePower(turnPower: Double,
                  drivePower: Double) {
        val motorPowers = calculateMotorTargets(turnPower, drivePower)

        val powerRange = -1.0..1.0
        motor1.power = motorPowers.first.coerceIn(powerRange)
        motor2.power = motorPowers.second.coerceIn(powerRange)
    }
}