package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.localizer.RRTwoWheelLocalizer

@Autonomous
class OdometryTest: OpMode() {
    val hardware = RobotTwoHardware(opmode= this, telemetry= telemetry)
    val localizer = RRTwoWheelLocalizer(hardware= hardware)
    override fun init() {
        hardware.init(hardwareMap)
    }

    override fun loop() {
        localizer.recalculatePositionAndRotation()
        val currentPosition = localizer.currentPositionAndRotation()
        telemetry.addLine("currentPosition: $currentPosition")
        telemetry.update()
    }

}