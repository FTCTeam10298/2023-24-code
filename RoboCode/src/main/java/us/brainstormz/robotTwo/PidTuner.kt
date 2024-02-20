package us.brainstormz.robotTwo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.ConfigServer
import us.brainstormz.utils.DeltaTimeMeasurer
import java.lang.Thread.sleep


fun main() {
    var v = PidConfig(1.0, 1.0, 1.0)
    ConfigServer(
            port = 8083,
            get = { jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(v) },
            update = {v = jacksonObjectMapper().readValue(it) },)
}

data class PidConfig(
        val kp:Double,
        val ki:Double,
        val kd:Double,
){
    constructor(p:PID):this(kp = p.kp, ki = p.ki, kd = p.kd)

    fun toPID() = PID(kp = kp, ki = ki, kd = kd)
}


data class PidTuningAdjusterConfig (
        val timeDelayMillis:Int,
        val boxSizeInchesY:Int,
        val boxSizeInchesX:Int,
        val x:PidConfig,
        val y:PidConfig,
        val r:PidConfig,
)

@TeleOp
class PidTuner: OpMode() {
    private val hardware: RobotTwoHardware= RobotTwoHardware(telemetry= telemetry, opmode = this)

    private lateinit var drivetrain: Drivetrain

    private val transfer = Transfer(telemetry)
    private val extendo = Extendo()

    private lateinit var collectorSystem: CollectorSystem
    private lateinit var arm: Arm
    private lateinit var lift: Lift
    private lateinit var wrist: Wrist
    private lateinit var depoManager: DepoManager
    private lateinit var config: PidTuningAdjusterConfig

    override fun init() {
        hardware.init(hardwareMap)
        val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        drivetrain = Drivetrain(hardware, odometryLocalizer, telemetry)

        collectorSystem = CollectorSystem(transfer= transfer, extendo= extendo, telemetry= telemetry)
        lift = Lift(telemetry)
        arm = Arm()
        wrist = Wrist(left = Claw(telemetry), right = Claw(telemetry), telemetry= telemetry)
        depoManager = DepoManager(arm= arm, lift= lift, wrist= wrist, telemetry= telemetry)


        config = PidTuningAdjusterConfig(
            timeDelayMillis = 500,
            boxSizeInchesY = 40,
            boxSizeInchesX =  20,
            x = PidConfig(drivetrain.xTranslationPID),
            y = PidConfig(drivetrain.yTranslationPID),
            r = PidConfig(drivetrain.rotationPID)
        )
        ConfigServer(
                port = 8083,
                get = { jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(config) },
                update = {
                    config = jacksonObjectMapper().readValue(it)
                    drivetrain.xTranslationPID = config.x.toPID()
                    drivetrain.yTranslationPID = config.y.toPID()
                    drivetrain.rotationPID = config.r.toPID()
         },)
    }

    val loopTimeMeasurer = DeltaTimeMeasurer()
    var timeOfTargetDone = 0L
    val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    override fun loop() {
        functionalReactiveAutoRunner.loop(
                actualStateGetter = { previousActualState ->
                    val currentGamepad1 = Gamepad()
                    currentGamepad1.copy(gamepad1)
                    ActualWorld(
                            actualRobot = hardware.getActualState(drivetrain, collectorSystem, depoManager, previousActualState),
                            actualGamepad1 = currentGamepad1,
                            actualGamepad2 = currentGamepad1,
                            timestampMilis = System.currentTimeMillis()
                    )
                },
                targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                    telemetry.addLine("previousTargetState: $previousTargetState")
                    val isAtTarget = drivetrain.checkIfDrivetrainIsAtPosition(targetPosition = previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition ?: PositionAndRotation(), actualWorld = actualState, previousWorld = previousActualState?:actualState)
                    telemetry.addLine("isAtTarget: $isAtTarget")
                    val newTargetPosition = if (isAtTarget) {
                        val timeSinceEnd = actualState.timestampMilis - timeOfTargetDone
                        if (timeSinceEnd > config.timeDelayMillis) {
                            PositionAndRotation(Math.random() * config.boxSizeInchesX, Math.random() * config.boxSizeInchesY,(Math.random() * 360*2)-360)//positions[positions.indexOf(currentTarget) + 1]
                        } else {
                            previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition
                        }
                    } else {
                        timeOfTargetDone = actualState.timestampMilis
                        previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition
                    }

                    val ligths = if (isAtTarget) {
                        RevBlinkinLedDriver.BlinkinPattern.BLUE
                    } else {
                        RevBlinkinLedDriver.BlinkinPattern.RED
                    }

                    previousTargetState?.copy(targetRobot = previousTargetState.targetRobot.copy(drivetrainTarget = Drivetrain.DrivetrainTarget(targetPosition = newTargetPosition?: PositionAndRotation()), lights = previousTargetState.targetRobot.lights.copy(targetColor = ligths)))
                            ?: RobotTwoTeleOp.initialPreviousTargetState
                },
                stateFulfiller = { targetState, previousTargetState, actualState ->
                    telemetry.addLine("target position: ${targetState.targetRobot.drivetrainTarget.targetPosition}")
                    telemetry.addLine("current position: ${drivetrain.localizer.currentPositionAndRotation()}")

                    drivetrain.actuateDrivetrain(
                            target = targetState.targetRobot.drivetrainTarget,
                            previousTarget = previousTargetState?.targetRobot?.drivetrainTarget ?: targetState.targetRobot.drivetrainTarget,
                            actualPosition = actualState.actualRobot.positionAndRotation,
                    )

                    hardware.lights.setPattern(targetState.targetRobot.lights.targetColor)
                }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()
        telemetry.addLine("loopTime: $loopTime")

        telemetry.addLine("average loopTime: ${loopTimeMeasurer.getAverageLoopTimeMillis()}")

        sleep(104-84)

        telemetry.update()
    }
}