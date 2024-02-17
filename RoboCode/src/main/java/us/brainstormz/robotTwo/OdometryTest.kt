package us.brainstormz.robotTwo

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.OdometryMovementTest.PIDTuningValues.timeDelayMilis
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import java.lang.Thread.sleep

@Autonomous
class PIDTuningTest: OpMode() {
    val hardware = RobotTwoHardware(opmode= this, telemetry= telemetry)
    lateinit var localizer: RRTwoWheelLocalizer
    override fun init() {
        hardware.init(hardwareMap)
        localizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        localizer.setPositionAndRotation(PositionAndRotation(0.0, 0.0, 0.0))
    }
    override fun loop() {
        val perpPos = hardware.perpendicularEncoder.getPositionAndVelocity().position
        val parPos = hardware.parallelEncoder.getPositionAndVelocity().position
        val imuYawDegrees = hardware.imu.robotYawPitchRollAngles.getYaw(AngleUnit.DEGREES)
        telemetry.addLine("perpPos: $perpPos")
        telemetry.addLine("parPos: $parPos")
        telemetry.addLine("imuYawDegrees: $imuYawDegrees")

        localizer.recalculatePositionAndRotation()
        val currentPosition = localizer.currentPositionAndRotation()
        telemetry.addLine("current position: $currentPosition")
        telemetry.addLine("Road runner internal current position: ${localizer.pose}")

        telemetry.update()
    }
}

@Autonomous
class OdometryMovementTest: OpMode() {
    val hardware = RobotTwoHardware(opmode= this, telemetry= telemetry)
//    lateinit var localizer: RRTwoWheelLocalizer
//    lateinit var movement: MecanumMovement
    lateinit var drivetrain: Drivetrain

//    val console = TelemetryConsole(telemetry)
//    val wizard = TelemetryWizard(console, null)


    override fun init() {
//        wizard.newMenu("testType", "What test to run?", listOf("Drive motor", "Movement directions", "Full odom movement"), firstMenu = true)
//        wizard.summonWizardBlocking(gamepad1)

        hardware.init(hardwareMap)
        drivetrain = Drivetrain(hardware, RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick), telemetry)
    }

    val positions = listOf<PositionAndRotation>(
            PositionAndRotation(y= 10.0, x= 0.0, r= 0.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
            PositionAndRotation(y= 0.0, x= 10.0, r= 0.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
            PositionAndRotation(y= 10.0, x= 0.0, r= 90.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
            PositionAndRotation(y= 0.0, x= 10.0, r= 90.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
    )
    var currentTarget: PositionAndRotation = positions.first()
    var previousTarget = currentTarget
    var currentTargetStartTimeMilis: Long = 0
    data class PositionDataPoint(val target: PositionAndRotation, val timeToSuccessMilis: Long, val finalPosition: PositionAndRotation)
    val positionData = mutableListOf<PositionDataPoint>()

    var currentTargetEndTimeMilis:Long = 0
    override fun loop() {

        val currentPosition = drivetrain.getPosition()
        telemetry.addLine("rr current position: $currentPosition")

        val ypid = PIDTuningValues.getYTranslationPID()
        val xpid = PIDTuningValues.getXTranslationPID()
        val rpid = PIDTuningValues.getRotationPID()
        telemetry.addLine("ypid: $ypid")
        telemetry.addLine("xpid: $xpid")
        telemetry.addLine("rpid: $rpid")

        telemetry.addLine("currentTarget: $currentTarget")
        drivetrain.actuateDrivetrain(
                Drivetrain.DrivetrainTarget(currentTarget),
                Drivetrain.DrivetrainTarget(previousTarget),
                currentPosition,
                yTranslationPID = ypid,
                xTranslationPID =  xpid,
                rotationPID = rpid)

        previousTarget = currentTarget
        val isAtTarget = drivetrain.isRobotAtPosition(currentPosition= currentPosition, targetPosition = currentTarget, precisionInches = 1.0, precisionDegrees = 3.0)
        if (isAtTarget) {
            if (currentTargetEndTimeMilis == 0L)
                currentTargetEndTimeMilis = System.currentTimeMillis()

            val timeSinceEnd = System.currentTimeMillis() - currentTargetEndTimeMilis
            if (timeSinceEnd > timeDelayMilis) {
                val index = positions.indexOf(currentTarget)
                if (index != (positions.size - 1)) {
                    val timeToComplete = System.currentTimeMillis() - currentTargetStartTimeMilis
                    positionData.add(PositionDataPoint(currentTarget, timeToComplete, currentPosition))

                    currentTarget = positions[index+1]
//                    val distanceInches = 20
//                    currentTarget = PositionAndRotation(Math.random() * distanceInches, Math.random() * distanceInches,(Math.random() * 360*2)-360)//positions[positions.indexOf(currentTarget) + 1]
                } else {
                    currentTarget = positions.first()
                }
            }
        } else {
            currentTargetEndTimeMilis = 0
        }

        telemetry.addLine("\n\npositionData: \n$positionData")

        telemetry.update()
    }

    @Config
    object PIDTuningValues {
        @JvmField
        var timeDelayMilis: Int = 2000

        @JvmField
        var yp = 0.1
        @JvmField
        var yi = 0.0//00002
        fun getYTranslationPID(): PID {
            return PID(kp= yp, ki= yi)
        }
        @JvmField
        var xp = 0.3
        @JvmField
        var xi = 0.0//00003
        fun getXTranslationPID(): PID {
            return PID(kp= xp, ki= xi)
        }
        @JvmField
        var rp = 1.2
        @JvmField
        var ri = 1.0
        fun getRotationPID(): PID {
            return PID(kp= rp, ki= ri)
        }
    }

}