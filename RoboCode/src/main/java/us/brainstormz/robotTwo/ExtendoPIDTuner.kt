package us.brainstormz.robotTwo

import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.ConfigServer
import us.brainstormz.utils.ConfigServerTelemetry
import us.brainstormz.utils.DeltaTimeMeasurer


data class ExtendoPidTuningAdjusterConfig (
        val timeDelayMillis:Int,
        val pid:PidConfig,
)

class ExtendoPIDTuner(private val hardware: RobotTwoHardware, telemetry: Telemetry) {

    private val configServerTelemetry = ConfigServerTelemetry()
    private val multipleTelemetry = MultipleTelemetry(telemetry, configServerTelemetry)


    val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
    private val drivetrain = Drivetrain(hardware, odometryLocalizer, multipleTelemetry)


    private val transfer = Transfer(multipleTelemetry)
    private val extendo = Extendo()

    private val collectorSystem = CollectorSystem(transfer= transfer, extendo= extendo, telemetry= multipleTelemetry)
    private val arm = Arm()
    private val lift = Lift(multipleTelemetry)
    private val wrist = Wrist(left = Claw(multipleTelemetry), right = Claw(multipleTelemetry), telemetry= multipleTelemetry)
    private val depoManager = DepoManager(arm= arm, lift= lift, wrist= wrist, telemetry= multipleTelemetry)
    private lateinit var config: ExtendoPidTuningAdjusterConfig
    private var previousQueue = ""
    private lateinit var configServer: ConfigServer

    fun init() {
        config = ExtendoPidTuningAdjusterConfig(500, PidConfig(extendo.pid))
        multipleTelemetry.addLine("Starting config server")
        configServer = ConfigServer(
                port = 8083,
                get = { jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(config) },
                update = {
                    config = jacksonObjectMapper().readValue(it)
                    extendo.pid = config.pid.toPID()
                },
                getInfoToPrint = {
                    val folded = configServerTelemetry.screenOfLines.fold("") { acc, it -> acc + it }
                    if (folded == "") {
                        previousQueue
                    } else {
                        previousQueue = folded
                        folded
                    }
                })
    }

    val routine = listOf(
            Extendo.ExtendoPositions.Min,
            Extendo.ExtendoPositions.PurpleSidePosition,
            Extendo.ExtendoPositions.PurpleCenterPosition,
    )

    val loopTimeMeasurer = DeltaTimeMeasurer()
    var timeOfTargetDone = 0L
    val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    fun loop(gamepad1: Gamepad) {
        functionalReactiveAutoRunner.loop(
                actualStateGetter = { previousActualState ->
                    val currentGamepad1 = Gamepad()
                    currentGamepad1.copy(gamepad1)
                    ActualWorld(
                            actualRobot =    hardware.getActualState(drivetrain, collectorSystem, depoManager, previousActualState),
                            actualGamepad1 = currentGamepad1,
                            actualGamepad2 = currentGamepad1,
                            timestampMilis = System.currentTimeMillis()
                    )
                },
                targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                    multipleTelemetry.addLine("previousTargetState: $previousTargetState")
                    val isAtTarget = extendo.isExtendoAtPosition(previousTargetState?.targetRobot?.collectorTarget?.extendo?.targetPosition?.ticks ?: 0, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
                    multipleTelemetry.addLine("isAtTarget: $isAtTarget")
                    val currentTask = previousTargetState?.targetRobot?.collectorTarget?.extendo?.targetPosition
                    val currentIndex = previousTargetState?.targetRobot?.collectorTarget?.extendo?.power?.toInt() ?: 0
                    val newTargetPosition: Pair<SlideSubsystem.SlideTargetPosition?, Int> = if (isAtTarget) {
                        val timeSinceEnd = actualState.timestampMilis - timeOfTargetDone
                        println("timeSinceEnd: $timeSinceEnd")
                        val isTimeToGetNextTask = timeSinceEnd > config.timeDelayMillis
                        println("config.timeDelayMillis: ${config.timeDelayMillis}")
                        if (isTimeToGetNextTask) {
                            println("next task")
//                            PositionAndRotation(Math.random() * config.boxSizeInchesX, Math.random() * config.boxSizeInchesY,(Math.random() * 360*2)-360)//positions[positions.indexOf(currentTarget) + 1]
                            val nextTaskIndex = currentIndex.toInt() + 1
                            if (nextTaskIndex <= routine.size-1) {
                                routine[nextTaskIndex] to nextTaskIndex
                            } else {
                                routine.first() to 0
                            }
                        } else {
                            currentTask to currentIndex
                        }
                    } else {
                        timeOfTargetDone = actualState.timestampMilis
                        currentTask to currentIndex
                    }

                    println("newTargetPosition: $newTargetPosition")

                    val ligths = if (isAtTarget) {
                        RevBlinkinLedDriver.BlinkinPattern.BLUE
                    } else {
                        RevBlinkinLedDriver.BlinkinPattern.RED
                    }

                    previousTargetState?.copy(targetRobot = previousTargetState.targetRobot.copy(collectorTarget = previousTargetState.targetRobot.collectorTarget.copy(extendo= SlideSubsystem.TargetSlideSubsystem(targetPosition = newTargetPosition.first ?: Extendo.ExtendoPositions.Min , power = newTargetPosition.second.toDouble(), movementMode = DualMovementModeSubsystem.MovementMode.Position)), lights = previousTargetState.targetRobot.lights.copy(targetColor = ligths)))
                            ?: RobotTwoTeleOp.initialPreviousTargetState
                },
                stateFulfiller = { targetState, previousTargetState, actualState ->
                    multipleTelemetry.addLine("target position: ${targetState.targetRobot.collectorTarget.extendo.targetPosition}, ticks ${targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks}")
                    multipleTelemetry.addLine("current position: ${actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks}")

                    printPID(extendo.pid)

                    val extendoPower = extendo.calcPowerToMoveExtendo(targetState.targetRobot.collectorTarget.extendo.targetPosition.ticks, actualState.actualRobot)
                    extendo.powerSubsystem(extendoPower, hardware)

                    hardware.lights.setPattern(targetState.targetRobot.lights.targetColor)
                }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()
        multipleTelemetry.addLine("loopTime: $loopTime")

        multipleTelemetry.addLine("average loopTime: ${loopTimeMeasurer.getAverageLoopTimeMillis()}")

        Thread.sleep(104 - 84)

        multipleTelemetry.update()
    }

    private fun printPID(pid: PID) {
        fun format(d: Double) = String.format("%.5f", d)
        multipleTelemetry.addLine("""
                            "${pid.name}" pid powers, 
                             vm: ${format(pid.vMin)}
                              v: ${format(pid.v)}
                             dt: ${pid.deltaTimeMs}
                              p: ${format(pid.p)} 
                                 * ${format(pid.kp)} 
                                 = ${format(pid.ap)}
                              i: ${format(pid.i)} 
                                 * ${format(pid.ki)} 
                                 = ${format(pid.ai)}
                              d: ${format(pid.d)} 
                                 * ${format(pid.kd)} 
                                 = ${format(pid.ad)}
                              e: ${format(Math.toDegrees(pid.lastError))}""")
    }

    fun stop() {
        configServer.stop()
    }
}

@TeleOp
class ExtendoPidTunerOpMode: OpMode() {
    private lateinit var pidTuner: ExtendoPIDTuner
    private val hardware: RobotTwoHardware= RobotTwoHardware(telemetry= telemetry, opmode = this)

    override fun init() {
        hardware.init(hardwareMap)
        pidTuner = ExtendoPIDTuner(hardware, telemetry)
        pidTuner.init()
    }

    override fun loop() {
        pidTuner.loop(gamepad1)
    }

    override fun stop() {
        pidTuner.stop()
    }
}