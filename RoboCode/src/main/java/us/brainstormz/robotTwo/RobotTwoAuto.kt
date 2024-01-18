package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard

@Autonomous
class RobotTwoAuto: OpMode() {

    private fun getLiftPos(power: Double): RobotTwoHardware.LiftPositions = RobotTwoHardware.LiftPositions.entries.firstOrNull { it ->
        power == it.position
    } ?: RobotTwoHardware.LiftPositions.Min

    private fun actualStateReader(previousActualState: ActualWorld?): ActualWorld {

        val depoState = DepoState(
                liftPosition = getLiftPos(hardware.liftMotorMaster.currentPosition.toDouble()),

                armPos = arm.getArmState(),

                leftClawPosition = RobotTwoHardware.LeftClawPosition.entries.firstOrNull { it ->
                    hardware.leftClawServo.position == it.position
                } ?: RobotTwoHardware.LeftClawPosition.Retracted,

                rightClawPosition = RobotTwoHardware.RightClawPosition.entries.firstOrNull { it ->
                    hardware.rightClawServo.position == it.position
                } ?: RobotTwoHardware.RightClawPosition.Retracted,
        )

        val actualRobot = RobotState(
                positionAndRotation = mecanumMovement.localizer.currentPositionAndRotation(),
                collectorState = collector.getCurrentState(previousActualState?.actualRobot?.collectorState),
                depoState = depoState
        )

        return ActualWorld(
                actualRobot= actualRobot,
                System.currentTimeMillis()
        )
    }


    private fun nextTargetState(
            previousTargetState: TargetWorld?,
            actualState: ActualWorld,
            previousActualState: ActualWorld?): TargetWorld {

    }

    data class RobotState(
            val positionAndRotation: PositionAndRotation,
            val depoState: DepoState,
            val collectorState: Collector.CollectorState
    )
    data class DepoState(
            val armPos: Arm.Positions,
            val liftPosition: RobotTwoHardware.LiftPositions,
            val leftClawPosition: RobotTwoHardware.LeftClawPosition,
            val rightClawPosition: RobotTwoHardware.RightClawPosition,
    )

//    data class NonRobotState(val isPreloadScored: Boolean)

    open class TargetWorld( val targetRobot: RobotState)
//    open class TargetWorld( val targetRobot: RobotState,
//                            val targetExtraRobot: NonRobotState)
    class ActualWorld(val actualRobot: RobotState,
//                      val actualExtraRobot: NonRobotState,
                      val timestampMilis: Long)


    private val console = TelemetryConsole(telemetry)
    private val wizard = TelemetryWizard(console, null)
    private val hardware = RobotTwoHardware(telemetry, this)

    private lateinit var mecanumMovement: MecanumMovement

    private lateinit var collector: Collector

    private lateinit var arm: Arm

    override fun init() {
        hardware.init(hardwareMap)

        val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        mecanumMovement = MecanumMovement(odometryLocalizer, hardware, telemetry)

        collector = Collector(  extendoMotorMaster= hardware.extendoMotorMaster,
                extendoMotorSlave= hardware.extendoMotorSlave,
                collectorServo1 = hardware.collectorServo1,
                collectorServo2 = hardware.collectorServo2,
                rightTransferServo=hardware. rightTransferServo,
                leftTransferServo= hardware.leftTransferServo,
                transferDirectorServo= hardware.transferDirectorServo,
                leftCollectorPixelSensor= hardware.leftCollectorPixelSensor,
                rightCollectorPixelSensor= hardware.rightCollectorPixelSensor,
                telemetry= telemetry)


        arm = Arm(  encoder= hardware.armEncoder,
                armServo1= hardware.armServo1,
                armServo2= hardware.armServo2)
    }

    private val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    override fun loop() {
        functionalReactiveAutoRunner.loop(
            actualStateGetter = ::actualStateReader,
            targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                nextTargetState(previousTargetState, actualState, previousActualState)
            },
            stateFulfiller = { targetState, actualState ->
                mecanumMovement.moveTowardTarget(targetState.targetRobot.positionAndRotation)
            }
        )
    }
}