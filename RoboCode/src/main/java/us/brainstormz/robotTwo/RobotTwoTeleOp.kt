package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.utils.DeltaTimeMeasurer

class RobotTwoTeleOp: OpMode() {

    private val hardware = RobotTwoHardware(telemetry, this)
    val movement = MecanumDriveTrain(hardware)
    private lateinit var arm: Arm
    private lateinit var collectorSystem: CollectorSystem
    private lateinit var lift: Lift
    private lateinit var handoffManager: HandoffManager

    private lateinit var odometryLocalizer: RRTwoWheelLocalizer


    override fun init() {
        TODO("Not yet implemented")
    }


    private fun

    private val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    private val loopTimeMeasurer = DeltaTimeMeasurer()
    override fun loop() {
        functionalReactiveAutoRunner.loop(
                actualStateGetter = { previousActualState ->
                    hardware.getActualState(previousActualState, arm, mecanumMovement.localizer, collectorSystem)
                },
                targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                    nextTargetState(previousTargetState, actualState, previousActualState)
                },
                stateFulfiller = { targetState, actualState ->
                    telemetry.addLine("target position: ${targetState.targetRobot.positionAndRotation}")
                    telemetry.addLine("current position: ${mecanumMovement.localizer.currentPositionAndRotation()}")
//                    mecanumMovement.moveTowardTarget(targetState.targetRobot.positionAndRotation)
                    collectorSystem.moveExtendoToPosition(targetState.targetRobot.collectorSystemState.extendoPosition.ticks)
                    collectorSystem.spinCollector(targetState.targetRobot.collectorSystemState.collectorState.power)
                    lift.moveLiftToPosition(targetState.targetRobot.depoState.liftPosition.ticks)
                    arm.moveArmTowardPosition(targetState.targetRobot.depoState.armPos.angleDegrees)
                    hardware.rightClawServo.position = targetState.targetRobot.depoState.rightClawPosition.position
                    hardware.leftClawServo.position = targetState.targetRobot.depoState.leftClawPosition.position
                }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMilis()
        telemetry.addLine("loop time: $loopTime milis")

        telemetry.update()
    }
}