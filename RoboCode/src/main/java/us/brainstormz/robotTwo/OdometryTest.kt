package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
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
        localizer.recalculatePositionAndRotation()
        val currentPosition = localizer.currentPositionAndRotation()
        telemetry.addLine("currentPosition: $currentPosition")
        telemetry.update()
    }
}