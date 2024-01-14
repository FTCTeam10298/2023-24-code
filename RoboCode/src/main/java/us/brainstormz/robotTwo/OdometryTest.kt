package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.roadrunner.RoadRunnerTwoDeadWheelLocalizer

@Autonomous
class OdometryTest: OpMode() {
    val hardware = RobotTwoHardware(opmode= this, telemetry= telemetry)
    lateinit var localizer: RRTwoWheelLocalizer
    override fun init() {
        hardware.init(hardwareMap)
        localizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
    }
    override fun loop() {
        val perpPos = hardware.perpendicularOdom.getCurrentPosition()
        val parPos = hardware.parallelOdom.getCurrentPosition()
        val imuYawDegrees = hardware.imu.robotYawPitchRollAngles.getYaw(AngleUnit.DEGREES)
        telemetry.addLine("perpPos: $perpPos")
        telemetry.addLine("parPos: $parPos")
        telemetry.addLine("imuYawDegrees: $imuYawDegrees")

        localizer.recalculatePositionAndRotation()
        val currentPosition = localizer.currentPositionAndRotation()
        telemetry.addLine("rr current position: $currentPosition")

        telemetry.update()
    }
}