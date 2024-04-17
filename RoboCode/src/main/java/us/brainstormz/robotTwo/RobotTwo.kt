package us.brainstormz.robotTwo

import StatsDumper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.qualcomm.hardware.lynx.LynxModule
import org.firstinspires.ftc.robotcontroller.internal.FtcRobotControllerActivity
import org.firstinspires.ftc.robotcore.external.Telemetry
import us.brainstormz.faux.FauxLocalizer
import us.brainstormz.localizer.Localizer
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initialPreviousTargetState
import us.brainstormz.robotTwo.RobotTwoTeleOp.*
import us.brainstormz.robotTwo.localTests.TeleopTest
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist
import us.brainstormz.utils.DeltaTimeMeasurer
import us.brainstormz.utils.measured
import java.io.File
import java.lang.Exception

abstract class RobotTwo(private val telemetry: Telemetry) {
    val intake = Intake()
    val dropdown = Dropdown()
    val transfer = Transfer(telemetry)
    val extendo = Extendo(telemetry)
    val leftClaw: Claw = Claw(telemetry)
    val rightClaw: Claw = Claw(telemetry)
    val wrist = Wrist(leftClaw, rightClaw, telemetry= telemetry)
    val arm: Arm = Arm()
    val lift: Lift = Lift(telemetry)
    val collectorSystem: CollectorManager = CollectorManager(transfer= transfer, extendo= extendo, intake = intake, telemetry= telemetry)
    val depoManager: DepoManager = DepoManager(arm= arm, lift= lift, wrist= wrist, telemetry= telemetry)
    val handoffManager: HandoffManager = HandoffManager(collectorSystem, depoManager, extendo, wrist, arm, lift, transfer, telemetry)
    lateinit var drivetrain: Drivetrain

    lateinit var stateDumper: StateDumper
    lateinit var statsDumper: StatsDumper
    fun initRobot(hardware: RobotTwoHardware, localizer: Localizer) {
        FtcRobotControllerActivity.instance?.let{ controller ->
            statsDumper = StatsDumper(reportingIntervalMillis = 1000, controller)
            statsDumper.start()
        }
        stateDumper = StateDumper(reportingIntervalMillis = 1000, functionalReactiveAutoRunner)
        stateDumper.start()

        drivetrain = Drivetrain(hardware, localizer, telemetry)

        for (module in hardware.allHubs) {
            module.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
        }
    }
    var getTime:()->Long = {System.currentTimeMillis()}

    val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    val loopTimeMeasurer = DeltaTimeMeasurer()

    fun getActualState(previousActualState:ActualWorld?, gamepad1: SerializableGamepad, gamepad2: SerializableGamepad, hardware: RobotTwoHardware):ActualWorld{
        val (currentGamepad1, currentGamepad2)  = measured("gamepad copies"){
            gamepad1 to gamepad2
        }

        val actualRobot = hardware.getActualState(
            drivetrain= drivetrain,
            depoManager = depoManager,
            collectorSystem = collectorSystem,
            previousActualWorld= previousActualState,
        )
        telemetry.addLine("lift: ${actualRobot.depoState.lift}")

        return ActualWorld(
            actualRobot = actualRobot,
            actualGamepad1 = currentGamepad1,
            actualGamepad2 = currentGamepad2,
            timestampMilis = getTime(),
            timeOfMatchStartMillis = previousActualState?.timeOfMatchStartMillis ?: getTime()
        )
    }

    fun runRobot(
        targetStateFetcher: (actualState: ActualWorld, previousActualState: ActualWorld?, previousTargetState: TargetWorld?)->TargetWorld,
        gamepad1: SerializableGamepad,
        gamepad2: SerializableGamepad,
        hardware: RobotTwoHardware
    ) = measured("main loop"){

        measured("clear bulk cache"){
            for (hub in hardware.allHubs) {
                hub.clearBulkCache()
            }
        }

        functionalReactiveAutoRunner.loop(
            actualStateGetter = {getActualState(it, gamepad1, gamepad2, hardware)},
            targetStateFetcher = targetStateFetcher,
            stateFulfiller = { targetState, previousTargetState, actualState, previousActualState ->
                if (actualState.actualGamepad1.touchpad && previousActualState?.actualGamepad1?.touchpad == false) {
                    saveStateSnapshot(actualState, previousActualState, targetState, previousTargetState)
                }

                val previousActualState = previousActualState ?: actualState
                measured("actuate robot"){
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
                }
                measured("rumble"){
                    if (targetState.gamepad1Rumble != null) {
                        if (!gamepad1.isRumbling) {
                            gamepad1.runRumbleEffect(targetState.gamepad1Rumble.effect)
                        }
                        if (!gamepad2.isRumbling) {
                            gamepad2.runRumbleEffect(targetState.gamepad1Rumble.effect)
                        }
                    }
                }
            }
        )
        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMillis()

        measured("telemetry"){
            telemetry.addLine("loop time: $loopTime milis")
            telemetry.addLine("peak loop time: ${loopTimeMeasurer.peakDeltaTime()} milis")

            measured("expensiveTelemetryLines-addLine"){
                stateDumper.lines().forEach(telemetry::addLine)
            }

            telemetry.update()
        }
    }

    fun <T> saveSomething(somethingToSave: T) {
        Thread {
            try {
                val file = File("/storage/emulated/0/Download/something$numberOfSnapshotsMade.json")
                file.createNewFile()
                if (file.exists() && file.isFile) {
                    numberOfSnapshotsMade++

                    val jsonEncoded = jacksonObjectMapper().writeValueAsString(somethingToSave)

                    println("SAVING something $numberOfSnapshotsMade: ${jsonEncoded}")
                    file.printWriter().use {
                        it.print(jsonEncoded)
                    }
                }
            } catch (e: Exception) {
                println("oopsie poopsie")
            }
        }.start()
    }

    private var numberOfSnapshotsMade = 0
    fun saveStateSnapshot(actualWorld: ActualWorld, previousActualWorld: ActualWorld?, targetWorld: TargetWorld, previousActualTarget: TargetWorld?) {
        Thread {

            val file = File("/storage/emulated/0/Download/stateSnapshot$numberOfSnapshotsMade.json")
            file.createNewFile()
            if (file.exists() && file.isFile) {
                numberOfSnapshotsMade++

                val jsonEncoded = NewCompleteSnapshot(
                        actualWorld = actualWorld,
                        previousActualWorld = previousActualWorld,
                        targetWorld = targetWorld,
                        previousActualTarget = previousActualTarget,
                ).toJson()

                println("SAVING SNAPSHOT $numberOfSnapshotsMade: ${jsonEncoded}")
                file.printWriter().use {
                    it.print(jsonEncoded)
                }
            }

        }.start()
    }
}


data class NewCompleteSnapshot(
        val actualWorld: ActualWorld,
        val previousActualWorld: ActualWorld?,
        val targetWorld: TargetWorld,
        val previousActualTarget: TargetWorld?,
){
    fun toJson() = jacksonObjectMapper().writeValueAsString(this)
}

data class CompleteSnapshot(
    val actualWorld: ActualWorld,
    val previousActualWorld: ActualWorld?,
    val targetWorld: TargetWorld,
    val previousActualTarget: TargetWorld?,
){
    fun toJson() = jacksonObjectMapper().writeValueAsString(this)
}