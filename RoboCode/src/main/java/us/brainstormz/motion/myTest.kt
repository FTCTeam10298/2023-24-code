package us.brainstormz.motion

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.localization.ThreeTrackingWheelLocalizer
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.paddieMatrick.PaddieMatrickHardware
import us.brainstormz.roadRunner.drive.StandardTrackingWheelLocalizer
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.PI

@Autonomous(name= "OdometryOnly", group= "!")
class OdometryOnly:OpMode() {
    val hardware = PaddieMatrickHardware()

    /** Change Depending on robot */
    var localizer: StandardTrackingWheelLocalizer? = null

    override fun init() {
        hardware.init(hardwareMap)
        localizer = StandardTrackingWheelLocalizer(hardwareMap)
    }
    private fun format(v:Double) = BigDecimal(v).setScale(4, RoundingMode.HALF_UP).toString()

    var isStopping = false

    val localize = Thread {
        while(!isStopping){
            localizer?.update()
        }
    }

    val output = Thread {
        while(!isStopping) {
            telemetry.addLine("pos: ${localizer?.poseEstimate}")

            localizer?.poseEstimate?.let { position ->
                telemetry.addLine("X: ${position.x}")
                telemetry.addLine("Y: ${position.y}")
                telemetry.addLine("H: ${position.heading}")

                println("pose @ ${System.currentTimeMillis()} x ${position.x} y ${position.y} h ${position.heading}")
                println("wheel counts @ ${System.currentTimeMillis()} wheels: ${localizer?.getWheelPositions()}")
            }
            telemetry.update()

            Thread.sleep(500)
        }
    }
    override fun start() {
        localize.start()
        output.start()
    }

    override fun stop() {
        isStopping = true
    }

    override fun loop() {

    }
}


class TestableThreeTrackingWheelLocalizer: ThreeTrackingWheelLocalizer(listOf(
        Pose2d(0.0, StandardTrackingWheelLocalizer.LATERAL_DISTANCE / 2, 0.0), // left
        Pose2d(0.0, -StandardTrackingWheelLocalizer.LATERAL_DISTANCE / 2, 0.0), // right
        Pose2d(StandardTrackingWheelLocalizer.FORWARD_OFFSET, 0.0, Math.toRadians(90.0)) // front
)) {

    var wheelPositionReadings:List<Double> = listOf(0.0, 0.0, 0.0)
    override fun getWheelPositions() = wheelPositionReadings
    var wheelVolocities = listOf(0.0, 0.0, 0.0)
    override fun getWheelVelocities() = wheelVolocities
}

fun main(){
    // given
    val turnRadians = PI/2

    val ticksToInches = StandardTrackingWheelLocalizer.WHEEL_RADIUS *
                        2 *
                        Math.PI *
                        StandardTrackingWheelLocalizer.GEAR_RATIO /
                        StandardTrackingWheelLocalizer.TICKS_PER_REV //* ticks
    val trackWidth = StandardTrackingWheelLocalizer.LATERAL_DISTANCE
    val inchesInTurn = turnRadians * trackWidth/2
    val ticksInTurn = inchesInTurn / ticksToInches

    val testSubject = TestableThreeTrackingWheelLocalizer()
    testSubject.wheelPositionReadings = listOf(0.0, 0.0, 0.0) // order is left right front
    testSubject.wheelVolocities = listOf(0.0, 0.0, 0.0)
    testSubject.update()

    // when
    println("ticksInTurn $ticksInTurn")
    testSubject.wheelPositionReadings = listOf(10.0, -10.0, 0.0)
    testSubject.wheelVolocities = listOf(10.0, -10.0, 0.0)
    testSubject.update()

    // then
    val desiredOutput = Pose2d(x= 0.0, y= 0.0, heading= turnRadians)
    if (desiredOutput != testSubject.poseEstimate) {
        throw Exception("You suck. Output Pose was ${testSubject.poseEstimate}, but should've been $desiredOutput")
    }
//    Assert.assertEquals(Pose2d(x= 0.0, y= 0.0, heading= 90.0), testSubject.poseEstimate)
}