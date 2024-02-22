package us.brainstormz.robotTwo

import com.acmerobotics.dashboard.telemetry.MultipleTelemetry
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
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
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.robotTwo.tests.FauxRobotTwoHardware
import us.brainstormz.robotTwo.tests.TeleopTest.Companion.emptyWorld
import us.brainstormz.utils.ConfigServer
import us.brainstormz.utils.ConfigServerTelemetry
import us.brainstormz.utils.DeltaTimeMeasurer
import java.lang.Thread.sleep

data class PidConfig(
        val name:String,
        val kp:Double,
        val ki:Double,
        val kd:Double,
){
    constructor(p:PID):this(name = p.name, kp = p.kp, ki = p.ki, kd = p.kd)
    fun toPID() = PID("adjusted $name", kp = kp, ki = ki, kd = kd)
}

data class PidTuningAdjusterConfig (
        val timeDelayMillis:Int,
        val boxSizeInchesY:Int,
        val boxSizeInchesX:Int,
        val x:PidConfig,
        val y:PidConfig,
        val r:PidConfig,
)

fun main() {

    val fauxTelemetry = PrintlnTelemetry()
    val hardware = FauxRobotTwoHardware(FauxOpMode(fauxTelemetry), fauxTelemetry);
    val pidTuner = PidTuner(hardware, fauxTelemetry)

    hardware.actualRobot = emptyWorld.actualRobot
    pidTuner.init()


    val loopStartTime = System.currentTimeMillis()
    println("loopStartTime: $loopStartTime")
    for (i in 0..10) {
        pidTuner.loop(Gamepad())
        hardware.actualRobot = emptyWorld.actualRobot.copy(positionAndRotation = pidTuner.functionalReactiveAutoRunner.previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition!!)
    }
    val loopEndTime = System.currentTimeMillis()
    println("loopEndTime: $loopEndTime")
    val timeSpendTotal = loopEndTime-loopStartTime
    println("timeSpendTotal: $timeSpendTotal")
}


class PidTuner(private val hardware: RobotTwoHardware, telemetry: Telemetry) {

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
    private lateinit var config: PidTuningAdjusterConfig
    private var previousQueue = ""
    private lateinit var configServer: ConfigServer

    fun init() {
        config = PidTuningAdjusterConfig(
                timeDelayMillis = 500,
                boxSizeInchesY = 40,
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

    val routine = listOf<PositionAndRotation>(
            PositionAndRotation(y= 10.0, x= 0.0, r= 0.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
            PositionAndRotation(y= 0.0, x= 10.0, r= 0.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
            PositionAndRotation(y= 10.0, x= 0.0, r= 90.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
            PositionAndRotation(y= 0.0, x= 10.0, r= 90.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 90.0),
            PositionAndRotation(y= 0.0, x= 0.0, r= 0.0),
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
                    val isAtTarget = drivetrain.checkIfDrivetrainIsAtPosition(targetPosition = previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition ?: PositionAndRotation(), actualWorld = actualState, previousWorld = previousActualState?:actualState)
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
                                currentTask to currentIndex
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
                        RevBlinkinLedDriver.BlinkinPattern.BLUE
                    } else {
                        RevBlinkinLedDriver.BlinkinPattern.RED
                    }

                    previousTargetState?.copy(targetRobot = previousTargetState.targetRobot.copy(drivetrainTarget = Drivetrain.DrivetrainTarget(targetPosition = newTargetPosition.first ?: PositionAndRotation(), power = Drivetrain.DrivetrainPower(y=newTargetPosition.second?.toDouble()?:0.0,x= 0.0, r=0.0), movementMode = DualMovementModeSubsystem.MovementMode.Position), lights = previousTargetState.targetRobot.lights.copy(targetColor = ligths)))
                            ?: RobotTwoTeleOp.initialPreviousTargetState
                },
                stateFulfiller = { targetState, previousTargetState, actualState ->
                    multipleTelemetry.addLine("target position: ${targetState.targetRobot.drivetrainTarget.targetPosition}")
                    multipleTelemetry.addLine("current position: ${drivetrain.localizer.currentPositionAndRotation()}")

                    drivetrain.actuateDrivetrain(
                            target = targetState.targetRobot.drivetrainTarget,
                            previousTarget = previousTargetState?.targetRobot?.drivetrainTarget ?: targetState.targetRobot.drivetrainTarget,
                            actualPosition = actualState.actualRobot.positionAndRotation,
                    )

                    hardware.lights.setPattern(targetState.targetRobot.lights.targetColor)
                }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()
        multipleTelemetry.addLine("loopTime: $loopTime")

        multipleTelemetry.addLine("average loopTime: ${loopTimeMeasurer.getAverageLoopTimeMillis()}")

//        sleep(104-84)

        multipleTelemetry.update()
    }

    fun stop() {
        configServer.stop()
    }
}

@TeleOp
class PidTunerOpMode: OpMode() {
    private lateinit var pidTuner: PidTuner
    private val hardware: RobotTwoHardware= RobotTwoHardware(telemetry= telemetry, opmode = this)

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