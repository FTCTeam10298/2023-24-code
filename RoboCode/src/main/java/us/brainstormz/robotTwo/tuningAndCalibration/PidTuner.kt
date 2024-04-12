package us.brainstormz.robotTwo.tuningAndCalibration

import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.CollectorManager
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.SerializableGamepad
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.ConfigServer
import us.brainstormz.utils.ConfigServerTelemetry
import us.brainstormz.utils.DeltaTimeMeasurer
import java.lang.Thread.sleep

fun exponentialIncrements(from:Long, init: Long) = generateSequence(from){ x -> Math.max(init, x) * 2}
fun exponentialIncrements(from:Long, to:Long, init: Long = 1) = exponentialIncrements(from, init).takeWhile { it <= to }

val rIncrements: Sequence<Long> = Sequence { listOf<Long>(0, 40, 90, 40, 0).listIterator() }

fun incrementSequence():List<PositionAndRotation>{

    return exponentialIncrements(from = 0, to = 30).flatMap{ y->
            exponentialIncrements(from = 0, to = 60).flatMap{ x ->
                rIncrements.map{ r->
                PositionAndRotation(
                        y= y.toDouble(),
                        x= x.toDouble(),
                        r= r.toDouble())
            }
        }
    }.toList()
}
fun main() {

    val s = incrementSequence().toList()

    println("${s.size} steps:")
    s.forEach{
        println("  ${it}")
    }
}
data class PidConfig(
        val name:String,
        val kp:Double,
        val ki:Double,
        val kd:Double,
        val min:Double,
){
    constructor(p:PID):this(name = p.name, kp = p.kp, ki = p.ki, kd = p.kd, min = p.min)
    fun toPID() = PID("adjusted $name", kp = kp, ki = ki, kd = kd, min = min)
}

data class PidTuningAdjusterConfig (
        val timeDelayMillis:Int,
        val boxSizeInchesY:Int,
        val boxSizeInchesX:Int,
        val x: PidConfig,
        val y: PidConfig,
        val r: PidConfig,
)

//fun main() {
//
//    val fauxTelemetry = PrintlnTelemetry()
//    val hardware = FauxRobotTwoHardware(FauxOpMode(fauxTelemetry), fauxTelemetry);
//    val pidTuner = PidTuner(hardware, fauxTelemetry)
//
//    hardware.actualRobot = emptyWorld.actualRobot
//    pidTuner.init()
//
//
//    val loopStartTime = System.currentTimeMillis()
//    println("loopStartTime: $loopStartTime")
//    for (i in 0..10) {
//        pidTuner.loop(Gamepad())
//        hardware.actualRobot = emptyWorld.actualRobot.copy(positionAndRotation = pidTuner.functionalReactiveAutoRunner.previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition!!)
//    }
//    val loopEndTime = System.currentTimeMillis()
//    println("loopEndTime: $loopEndTime")
//    val timeSpendTotal = loopEndTime-loopStartTime
//    println("timeSpendTotal: $timeSpendTotal")
//}


class PidTuner(private val hardware: RobotTwoHardware, telemetry: Telemetry) {

    private val configServerTelemetry = ConfigServerTelemetry()
    private val multipleTelemetry = MultipleTelemetry(telemetry, configServerTelemetry)

    val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
    private val drivetrain = Drivetrain(hardware, odometryLocalizer, multipleTelemetry)

    private val transfer = Transfer(multipleTelemetry)
    private val extendo = Extendo(multipleTelemetry)

    private val intake = Intake()
    private val collectorSystem = CollectorManager(transfer= transfer, extendo= extendo, intake = intake, telemetry= multipleTelemetry)
    private val arm = Arm()
    private val lift = Lift(multipleTelemetry)
    private val wrist = Wrist(left = Claw(multipleTelemetry), right = Claw(multipleTelemetry), telemetry= multipleTelemetry)
    private val depoManager = DepoManager(arm= arm, lift= lift, wrist= wrist, telemetry= multipleTelemetry)
    private lateinit var config: PidTuningAdjusterConfig
    private var previousQueue = ""
    private lateinit var configServer: ConfigServer

    fun init() {
        config = PidTuningAdjusterConfig(
                timeDelayMillis = 500,
                boxSizeInchesY = 20,
                boxSizeInchesX =  20,
                x = PidConfig(drivetrain.xTranslationPID),
                y = PidConfig(drivetrain.yTranslationPID),
                r = PidConfig(drivetrain.rotationPID)
        )
        multipleTelemetry.addLine("Starting config server")
        configServer = ConfigServer(
                port = 8083,
                get = { jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(config) },
                update = {
                    config = jacksonObjectMapper().readValue(it)
                    drivetrain.xTranslationPID = config.x.toPID()
                    drivetrain.yTranslationPID = config.y.toPID()
                    drivetrain.rotationPID = config.r.toPID()
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

    val sequence = incrementSequence()
    val allThreeDimensionsOver20InchesTest = listOf(
            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
            PositionAndRotation(y= 20.0, x= 20.0, r= 90.0),
            PositionAndRotation(y= 0.0, x= 20.0, r= 0.0),
            PositionAndRotation(y= 20.0, x= 0.0, r= -90.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),//dummy
    )
    val routine = sequence//allThreeDimensionsOver20InchesTest
    val routine2 = listOf<PositionAndRotation>(
            // X Test
            PositionAndRotation(y= 10.0, x= 0.0, r= 0.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0), // burner

//             Y Test
//            PositionAndRotation(y= 0.0, x= 10.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0), // burner

//            // R Test
//            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 180.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 270.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0), // burner

//            // Full Test
//            PositionAndRotation(y= 0.0, x= 10.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 5.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 10.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 5.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 10.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 120.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 180.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 270.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
//            PositionAndRotation(y= 5.0, x= 5.0, r= 180.0),
//            PositionAndRotation(y= 10.0, x= 10.0, r= 180.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 270.0),
//            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0), // burner
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
                            actualGamepad1 = SerializableGamepad(currentGamepad1),
                            actualGamepad2 = SerializableGamepad(currentGamepad1),
                            timestampMilis = System.currentTimeMillis()
                    )
                },
                targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                    multipleTelemetry.addLine("previousTargetState: $previousTargetState")
                    val isAtTarget = drivetrain.checkIfDrivetrainIsAtPosition(targetPosition = previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition ?: PositionAndRotation(), actualWorld = actualState, previousWorld = previousActualState?:actualState, precisionInches= drivetrain.precisionInches, precisionDegrees= drivetrain.precisionDegrees)
                    multipleTelemetry.addLine("isAtTarget: $isAtTarget")
                    val newTargetPosition = if (isAtTarget) {
                        val timeSinceEnd = actualState.timestampMilis - timeOfTargetDone
                        println("timeSinceEnd: $timeSinceEnd")
                        val currentTask = previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition
                        val currentIndex = previousTargetState?.targetRobot?.drivetrainTarget?.power?.y ?: 0.0
                        val isTimeToGetNextTask = timeSinceEnd > config.timeDelayMillis
                        println("config.timeDelayMillis: ${config.timeDelayMillis}")
                        if (isTimeToGetNextTask) {
                            println("next task")
//                            PositionAndRotation(Math.random() * config.boxSizeInchesX, Math.random() * config.boxSizeInchesY,(Math.random() * 360*2)-360)//positions[positions.indexOf(currentTarget) + 1]
                            val nextTaskIndex = currentIndex.toInt() + 1
                            if (nextTaskIndex < routine.size-1) {
                                routine[nextTaskIndex] to nextTaskIndex
                            } else {
                                routine.first() to 0
                            }
                        } else {
                            currentTask to currentIndex
                        }
                    } else {
                        timeOfTargetDone = actualState.timestampMilis
                        previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition to previousTargetState?.targetRobot?.drivetrainTarget?.power?.y
                    }

                    println("newTargetPosition: $newTargetPosition")

                    val ligths = if (isAtTarget) {
                        Neopixels.HalfAndHalfTarget(Neopixels.NeoPixelColors.Blue, Neopixels.NeoPixelColors.Blue)
                    } else {
                        Neopixels.HalfAndHalfTarget(Neopixels.NeoPixelColors.Red, Neopixels.NeoPixelColors.Red)
                    }

                    previousTargetState?.copy(targetRobot = previousTargetState.targetRobot.copy(drivetrainTarget = Drivetrain.DrivetrainTarget(targetPosition = newTargetPosition.first ?: PositionAndRotation(), power = Drivetrain.DrivetrainPower(y=newTargetPosition.second?.toDouble()?:0.0,x= 0.0, r=0.0), movementMode = DualMovementModeSubsystem.MovementMode.Position), lights = RobotTwoTeleOp.LightTarget(ligths.compileStripState())))
                            ?: RobotTwoTeleOp.initialPreviousTargetState
                },
                stateFulfiller = { targetState, previousTargetState, actualState, previousActualState ->
                    val previousActualState = previousActualState ?: actualState
                    multipleTelemetry.addLine("target position: ${targetState.targetRobot.drivetrainTarget.targetPosition}")
                    multipleTelemetry.addLine("current position: ${drivetrain.localizer.currentPositionAndRotation()}")

                    printPID(drivetrain.rotationPID)
                    printPID(drivetrain.yTranslationPID)
                    printPID(drivetrain.xTranslationPID)

//                    multipleTelemetry.addLine("")

                    drivetrain.actuateDrivetrain(
                            target = targetState.targetRobot.drivetrainTarget,
                            previousTarget = previousTargetState?.targetRobot?.drivetrainTarget ?: targetState.targetRobot.drivetrainTarget,
                            actualPosition = actualState.actualRobot.positionAndRotation,
                    )

                    hardware.neopixelSystem.writeQuickly(targetState.targetRobot.lights.stripTarget, previousTargetState?.targetRobot?.lights?.stripTarget ?: RobotTwoTeleOp.BothPixelsWeWant().toStripState(), hardware.neopixelDriver)
                }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()
        multipleTelemetry.addLine("loopTime: $loopTime")

        multipleTelemetry.addLine("average loopTime: ${loopTimeMeasurer.getAverageLoopTimeMillis()}")

        sleep(104-84)

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
class PidTunerOpMode: OpMode() {
    private lateinit var pidTuner: PidTuner
    private val hardware: RobotTwoHardware = RobotTwoHardware(telemetry= telemetry, opmode = this)

    override fun init() {
        hardware.init(hardwareMap)
        pidTuner = PidTuner(hardware, telemetry)
        pidTuner.init()
    }

    override fun loop() {
        pidTuner.loop(gamepad1)
    }

    override fun stop() {
        pidTuner.stop()
    }
}


//@Autonomous
//class OdometryMovementTest: OpMode() {
//    val hardware = RobotTwoHardware(opmode= this, telemetry= telemetry)
//    //    lateinit var localizer: RRTwoWheelLocalizer
////    lateinit var movement: MecanumMovement
//    lateinit var drivetrain: Drivetrain
//
////    val console = TelemetryConsole(telemetry)
////    val wizard = TelemetryWizard(console, null)
//
//
//    override fun init() {
////        wizard.newMenu("testType", "What test to run?", listOf("Drive motor", "Movement directions", "Full odom movement"), firstMenu = true)
////        wizard.summonWizardBlocking(gamepad1)
//
//        hardware.init(hardwareMap)
//        drivetrain = Drivetrain(hardware, RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick), telemetry)
//    }
//
//    var currentTarget: PositionAndRotation = positions.first()
//    var previousTarget = currentTarget
//    var currentTargetStartTimeMilis: Long = 0
//    data class PositionDataPoint(val target: PositionAndRotation, val timeToSuccessMilis: Long, val finalPosition: PositionAndRotation)
//    val positionData = mutableListOf<PositionDataPoint>()
//
//    var currentTargetEndTimeMilis:Long = 0
//    override fun loop() {
//
//        val currentPosition = drivetrain.getPosition()
//        telemetry.addLine("rr current position: $currentPosition")
//
//        telemetry.addLine("ypid: $ypid")
//        telemetry.addLine("xpid: $xpid")
//        telemetry.addLine("rpid: $rpid")
//
//        telemetry.addLine("currentTarget: $currentTarget")
//        drivetrain.actuateDrivetrain(
//                Drivetrain.DrivetrainTarget(currentTarget),
//                Drivetrain.DrivetrainTarget(previousTarget),
//                currentPosition,
//                yTranslationPID = ypid,
//                xTranslationPID =  xpid,
//                rotationPID = rpid)
//
////        previousTarget = currentTarget.copy()
////        val isAtTarget = drivetrain.checkIfDrivetrainIsAtPosition(currentPosition, targetPosition = currentTarget, precisionInches = 1.0, precisionDegrees = 3.0)
////        if (isAtTarget) {
////            if (currentTargetEndTimeMilis == 0L)
////                currentTargetEndTimeMilis = System.currentTimeMillis()
////
////            val timeSinceEnd = System.currentTimeMillis() - currentTargetEndTimeMilis
////            if (timeSinceEnd > timeDelayMilis) {
////                val index = positions.indexOf(currentTarget)
////                if (index != (positions.size - 1)) {
////                    val timeToComplete = System.currentTimeMillis() - currentTargetStartTimeMilis
////                    positionData.add(PositionDataPoint(currentTarget, timeToComplete, currentPosition))
////
//////                    currentTarget = positions[index+1]
////                    val distanceInches = 20
////                    currentTarget = PositionAndRotation(Math.random() * distanceInches, Math.random() * distanceInches,(Math.random() * 360*2)-360)//positions[positions.indexOf(currentTarget) + 1]
////                } else {
//////                    currentTarget = positions.first()
////                }
////            }
////        } else {
////            currentTargetEndTimeMilis = 0
////        }
//
//        telemetry.addLine("\n\npositionData: \n$positionData")
//
//        telemetry.update()
//    }
//}