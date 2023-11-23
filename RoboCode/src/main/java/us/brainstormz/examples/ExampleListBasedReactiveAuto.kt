package us.brainstormz.examples

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.DcMotor
import us.brainstormz.OldIterativeOpMode
import us.brainstormz.RobotStateManager

class ExampleListBasedReactiveAuto: OpMode() {
    val hardware = ExampleHardware()

    open class ExampleTargetRobot(val driveMotorPositions: List<Int>)
    class ExampleActualState(driveMotorPositions: List<Int>, val timestampMilis: Long): ExampleTargetRobot(driveMotorPositions)

    override fun init() {
        hardware.init(hardwareMap)
    }

    private val targets: List<ExampleTargetRobot> = listOf(
        ExampleTargetRobot(driveMotorPositions = listOf(0,0,0,0)),
        ExampleTargetRobot(driveMotorPositions = listOf(1,1,1,1)),
        ExampleTargetRobot(driveMotorPositions = listOf(10,10,10,10))
    )

    private val robotStateManager = RobotStateManager<ExampleTargetRobot, ExampleActualState>()
    override fun loop() {
        robotStateManager.loop(
            actualStateGetter = {
                ExampleActualState(driveMotorPositions= hardware.driveMotors.map { it.currentPosition }, timestampMilis= System.currentTimeMillis())
            },
            targetStateFetcher = { previousTargetState, actualState ->
                if (previousTargetState == null) {
                    targets.first()
                } else {
                    val targetIsReached = previousTargetState == actualState
                    val atTheEndOfTheList = previousTargetState != targets.last()
                    if (targetIsReached && atTheEndOfTheList) {
                        val indexOfPrevious = targets.indexOf(previousTargetState)
                        targets[indexOfPrevious+1]
                    } else {
                        previousTargetState
                    }
                }
            },
            stateFulfiller = { targetState, actualState ->
                targetState.driveMotorPositions.forEachIndexed {i, it ->
                    val targetOverActual = it.toDouble() / actualState.driveMotorPositions[i]
                    hardware.driveMotors[i].power = 1 - targetOverActual
                }
            },
        )
    }
}