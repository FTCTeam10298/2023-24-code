package us.brainstormz.robotTwo.tuningAndCalibration

import us.brainstormz.pid.PID


data class ExtendoPidTuningAdjusterConfig (
        val timeDelayMillis:Int,
        val pid: PidConfig,
)

//class ExtendoPIDTuner(private val hardware: RobotTwoHardware, telemetry: Telemetry) {
//
//    private val configServerTelemetry = ConfigServerTelemetry()
//    private val multipleTelemetry = MultipleTelemetry(telemetry, configServerTelemetry)
//
//    private val robot = Robot(telemetry, hardware)
//
//    private lateinit var config: ExtendoPidTuningAdjusterConfig
//    private var previousQueue = ""
//    private lateinit var configServer: ConfigServer
//
//    fun init() {
//        config = ExtendoPidTuningAdjusterConfig(500, PidConfig(robot.extendo.pid))
//        multipleTelemetry.addLine("Starting config server")
//        configServer = ConfigServer(
//                port = 8083,
//                get = { jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(config) },
//                update = {
//                    config = jacksonObjectMapper().readValue(it)
//                    robot.extendo.pid = config.pid.toPID()
//                },
//                getInfoToPrint = {
//                    val folded = configServerTelemetry.screenOfLines.fold("") { acc, it -> acc + it }
//                    if (folded == "") {
//                        previousQueue
//                    } else {
//                        previousQueue = folded
//                        folded
//                    }
//                })
//    }
//
//    val routine = listOf(
//            Extendo.ExtendoPositions.Min,
//            Extendo.ExtendoPositions.PurpleFarSidePosition,
//            Extendo.ExtendoPositions.Min,
//            Extendo.ExtendoPositions.PurpleCloseSidePosition,
//            Extendo.ExtendoPositions.Min,
//            Extendo.ExtendoPositions.PurpleCenterPosition,
//    )
//
//    var timeOfTargetDone = 0L
//    fun loop(gamepad1: Gamepad) {
//        robot.runRobot(
//                gamepad1= gamepad1,
//                gamepad2 = gamepad1,
//                targetStateFetcher = { previousTargetState, actualState, previousActualState ->
//                    multipleTelemetry.addLine("previousTargetState: $previousTargetState")
//                    val isAtTarget = robot.extendo.isExtendoAtPosition(previousTargetState?.targetRobot?.collectorTarget?.extendo?.targetPosition?.ticks ?: 0, actualState.actualRobot.collectorSystemState.extendo.currentPositionTicks)
//                    multipleTelemetry.addLine("isAtTarget: $isAtTarget")
//                    val currentTask = previousTargetState?.targetRobot?.collectorTarget?.extendo?.targetPosition
//                    val currentIndex = previousTargetState?.targetRobot?.collectorTarget?.extendo?.power?.toInt() ?: 0
//                    val newTargetPosition: Pair<SlideSubsystem.SlideTargetPosition?, Int> = if (isAtTarget) {
//                        val timeSinceEnd = actualState.timestampMilis - timeOfTargetDone
//                        println("timeSinceEnd: $timeSinceEnd")
//                        val isTimeToGetNextTask = timeSinceEnd > config.timeDelayMillis
//                        println("config.timeDelayMillis: ${config.timeDelayMillis}")
//                        if (isTimeToGetNextTask) {
//                            println("next task")
////                            PositionAndRotation(Math.random() * config.boxSizeInchesX, Math.random() * config.boxSizeInchesY,(Math.random() * 360*2)-360)//positions[positions.indexOf(currentTarget) + 1]
//                            val nextTaskIndex = currentIndex.toInt() + 1
//                            if (nextTaskIndex <= routine.size-1) {
//                                routine[nextTaskIndex] to nextTaskIndex
//                            } else {
//                                routine.first() to 0
//                            }
//                        } else {
//                            currentTask to currentIndex
//                        }
//                    } else {
//                        timeOfTargetDone = actualState.timestampMilis
//                        currentTask to currentIndex
//                    }
//
//                    println("newTargetPosition: $newTargetPosition")
//
//                    val ligths = if (isAtTarget) {
//                        Neopixels.NeoPixelColors.Blue
//                    } else {
//                        Neopixels.NeoPixelColors.Red
//                    }
//
//                    previousTargetState?.copy(targetRobot = previousTargetState.targetRobot.copy(collectorTarget = previousTargetState.targetRobot.collectorTarget.copy(extendo= Extendo.ExtendoTarget(targetPosition = newTargetPosition.first ?: Extendo.ExtendoPositions.Min , power = newTargetPosition.second.toDouble(), movementMode = DualMovementModeSubsystem.MovementMode.Position)), lights = RobotTwoTeleOp.LightTarget(RobotTwoTeleOp.BothPixelsWeWant(), Neopixels.HalfAndHalfTarget().compileStripState())))
//                            ?: RobotTwoTeleOp.initialPreviousTargetState
//                }
//        )
//    }
//
//
//    fun stop() {
//        configServer.stop()
//    }
//}
//
fun printPID(pid: PID): String {
    fun format(d: Double) = String.format("%.5f", d)
    return """
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
                              e: ${format(Math.toDegrees(pid.lastError))}"""
}
//@TeleOp
//class ExtendoPidTunerOpMode: OpMode() {
//    private lateinit var pidTuner: ExtendoPIDTuner
//    private val hardware: RobotTwoHardware = RobotTwoHardware(telemetry= telemetry, opmode = this)
//
//    override fun init() {
//        hardware.init(hardwareMap)
//        pidTuner = ExtendoPIDTuner(hardware, telemetry)
//        pidTuner.init()
//    }
//
//    override fun loop() {
//        pidTuner.loop(gamepad1)
//    }
//
//    override fun stop() {
//        pidTuner.stop()
//    }
//}