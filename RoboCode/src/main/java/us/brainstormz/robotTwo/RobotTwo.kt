package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.DeltaTimeMeasurer

fun main() {
    val startTimeMillis = System.currentTimeMillis()
    val thread2 = Thread {
        do {
            println("Thread1 is going")
            val timeSinceStart = System.currentTimeMillis() - startTimeMillis
        } while (timeSinceStart < 600)
    }
    thread2.start()

    do {
        println("Thread1 is going")
        val timeSinceStart = System.currentTimeMillis() - startTimeMillis
    } while (timeSinceStart < 500)

    thread2.join()
}


abstract class RobotTwoOpMode: OpMode() {
    private val hardware = RobotTwoHardware(telemetry, this)
    private lateinit var robot: Robot

    abstract class RobotLogicThread: Thread("RobotLogicThread") {
        abstract fun fetchTargetState(
                previousTargetState: TargetWorld,
                actualState: ActualWorld,
                previousActualState: ActualWorld): TargetWorld
        override fun run() {

        }
    }
    abstract val logicThread: RobotLogicThread

    override fun init() {
        hardware.init(hardwareMap)

        robot = Robot(telemetry, hardware)
    }

    override fun loop() {
        robot.loop(
                gamepad1 = gamepad1,
                gamepad2 = gamepad2,
                targetStateFetcher = logicThread::fetchTargetState
        )
    }

    override fun stop() {

    }
}

class Robot(private val telemetry: Telemetry, private val hardware: RobotTwoHardware) {
    val intake = Intake()
    val dropdown = Dropdown()
    val transfer = Transfer(telemetry)
    val extendo = Extendo(telemetry)
    val collectorSystem: CollectorManager = CollectorManager(transfer= transfer, extendo= extendo, intake = intake, telemetry= telemetry)
    val leftClaw: Claw = Claw(telemetry)
    val rightClaw: Claw = Claw(telemetry)
    val wrist = Wrist(leftClaw, rightClaw, telemetry= telemetry)
    val arm: Arm = Arm()
    val lift: Lift = Lift(telemetry)
    val depoManager: DepoManager = DepoManager(arm= arm, lift= lift, wrist= wrist, telemetry= telemetry)
    val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
    val drivetrain = Drivetrain(hardware, odometryLocalizer, telemetry)

    val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    private val loopTimeMeasurer = DeltaTimeMeasurer()
    fun loop(gamepad1: Gamepad, gamepad2: Gamepad, targetStateFetcher: (previousTargetState: TargetWorld, actualState: ActualWorld, previousActualState: ActualWorld)->TargetWorld) {
        for (hub in hardware.allHubs) {
            hub.clearBulkCache()
        }
        functionalReactiveAutoRunner.loop(
                actualStateGetter = { previousActualState ->
                    val currentGamepad1 = Gamepad()
                    currentGamepad1.copy(gamepad1)
                    val currentGamepad2 = Gamepad()
                    currentGamepad2.copy(gamepad2)
                    ActualWorld(
                            actualRobot = hardware.getActualState(drivetrain= drivetrain, depoManager = depoManager, collectorSystem = collectorSystem, previousActualWorld= previousActualState),
                            actualGamepad1 = currentGamepad1,
                            actualGamepad2 = currentGamepad2,
                            timestampMilis = System.currentTimeMillis()
                    )
                },
                targetStateFetcher = { previousTargetState: TargetWorld?, actualState: ActualWorld, previousActualState: ActualWorld? ->
                    val previousActualState = previousActualState ?: actualState
                    val previousTargetState: TargetWorld = previousTargetState ?: RobotTwoTeleOp.initialPreviousTargetState
                    targetStateFetcher(previousTargetState, actualState, previousActualState)
                },
                stateFulfiller = { targetState, previousTargetState, actualState, previousActualState ->
                    val previousActualState = previousActualState ?: actualState

                    telemetry.addLine("\ntargetState: $targetState")
                    hardware.actuateRobot(
                            targetState,
                            previousTargetState ?: targetState,
                            actualState,
                            previousActualWorld = previousActualState,
                            drivetrain = drivetrain,
                            wrist= wrist,
                            arm= arm,
                            lift= lift,
                            extendo= extendo,
                            intake= intake,
                            dropdown = dropdown,
                            transfer= transfer
                    )
                    if (targetState.gamepad1Rumble != null && !gamepad1.isRumbling) {
                        gamepad1.runRumbleEffect(targetState.gamepad1Rumble.effect)
                    }
                }
        )
        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()
        telemetry.addLine("loop time: $loopTime milis")
        telemetry.addLine("peak loop time: ${loopTimeMeasurer.peakDeltaTime()} milis")

        telemetry.update()
    }
}