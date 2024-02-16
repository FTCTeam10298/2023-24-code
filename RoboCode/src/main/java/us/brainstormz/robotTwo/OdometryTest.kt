package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import java.lang.Thread.sleep


//gabe this class is your example
@Autonomous
class OdometryTrackingTest: OpMode() {
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
    var currentTargetStartTimeMilis: Long = 0
    data class PositionDataPoint(val target: PositionAndRotation, val timeToSuccessMilis: Long, val finalPosition: PositionAndRotation)
    val positionData = mutableListOf<PositionDataPoint>()
    override fun loop() {
//        localizer.recalculatePositionAndRotation()

        val currentPosition = drivetrain.getPosition()
        telemetry.addLine("rr current position: $currentPosition")

//        val driveMotors = mapOf<String, DcMotor>(
//                "left front" to hardware.lFDrive,
//                "right front" to hardware.rFDrive,
//                "left back" to hardware.lBDrive,
//                "right back" to hardware.rBDrive
//        )
//        driveMotors.forEach{ (name, motor) ->
//            telemetry.addLine("name: $name")
//            telemetry.update()
//            motor.power = 0.5
//            sleep(2000)
//            motor.power = 0.0
//        }
//
//        movement.setSpeedAll(vY= 0.0, vX = 0.5, vA = 0.0, minPower = -1.0, maxPower = 1.0)


        telemetry.addLine("currentTarget: $currentTarget")
        drivetrain.actuateDrivetrain(Drivetrain.DrivetrainTarget(currentTarget), currentPosition)

        val isAtTarget = drivetrain.isRobotAtPosition(currentPosition= currentPosition, targetPosition = currentTarget)
        if (isAtTarget) {
            val index = positions.indexOf(currentTarget)
            if (index != (positions.size - 1)) {

                val timeToComplete = System.currentTimeMillis() - currentTargetStartTimeMilis
                positionData.add(PositionDataPoint(currentTarget, timeToComplete, currentPosition))

                val distanceInches = 20
                currentTarget = PositionAndRotation(Math.random() * distanceInches, Math.random() * distanceInches)//,(Math.random() * 360*2)-360)//positions[positions.indexOf(currentTarget) + 1]
                currentTargetStartTimeMilis = System.currentTimeMillis()
            }
            sleep(500)
        }

        telemetry.addLine("\n\npositionData: \n$positionData")

        telemetry.update()
    }

}