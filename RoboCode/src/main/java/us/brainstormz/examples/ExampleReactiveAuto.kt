package us.brainstormz.examples

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.RobotStateManager

class ExampleReactiveAuto : OpMode(){
    val hardware = ExampleHardware()

    open class ExampleTargetRobot(val driveMotorPositions: List<Int>)
    class ExampleActualState(driveMotorPositions: List<Int>, val timestampMilis: Long): ExampleTargetRobot(driveMotorPositions)

    override fun init() {
        hardware.init(hardwareMap)
    }

    private val robotStateManager = RobotStateManager<ExampleTargetRobot, ExampleActualState>()
    override fun loop() {
        robotStateManager.loop(
            actualStateGetter = {
                ExampleActualState(driveMotorPositions= hardware.driveMotors.map { it.currentPosition }, timestampMilis= System.currentTimeMillis())
            },
            targetStateFetcher = { previousTargetState, actualState ->
                ExampleTargetRobot(driveMotorPositions = previousTargetState?.driveMotorPositions?.map { it + 1 } ?: listOf(0, 0, 0, 0))
            },
            stateFulfiller = { targetState, actualState ->
                targetState.driveMotorPositions.forEachIndexed { i, it ->
                    hardware.driveMotors[i].targetPosition = it
                }
            }
        )
    }

}