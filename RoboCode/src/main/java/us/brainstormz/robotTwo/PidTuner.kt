package us.brainstormz.robotTwo

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.PidTuningAdjuster.getRotationPID
import us.brainstormz.robotTwo.PidTuningAdjuster.getXTranslationPID
import us.brainstormz.robotTwo.PidTuningAdjuster.getYTranslationPID
import us.brainstormz.robotTwo.PidTuningAdjuster.timeDelayMilis
import us.brainstormz.robotTwo.PidTuningAdjuster.xBoxSizeInches
import us.brainstormz.robotTwo.PidTuningAdjuster.yBoxSizeInches
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist


@Config
object PidTuningAdjuster {
    @JvmField
    var timeDelayMilis: Int = 2000

    @JvmField
    var yBoxSizeInches = 20

    @JvmField
    var xBoxSizeInches = 20

    @JvmField
    var yp = 0.1
    @JvmField
    var yi = 0.0//00002
    @JvmField
    var yd = 0.0
    fun getYTranslationPID(): PID {
        return PID(kp= yp, ki= yi, kd= yd)
    }
    @JvmField
    var xp = 0.3
    @JvmField
    var xi = 0.0//00003
    @JvmField
    var xd = 0.0
    fun getXTranslationPID(): PID {
        return PID(kp= xp, ki= xi, kd= xd)
    }
    @JvmField
    var rp = 1.2
    @JvmField
    var ri = 1.0
    @JvmField
    var rd = 0.0
    fun getRotationPID(): PID {
        return PID(kp= rp, ki= ri, kd= rd)
    }
}

@Autonomous
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

    override fun init() {
        hardware.init(hardwareMap)
        val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        drivetrain = Drivetrain(hardware, odometryLocalizer, telemetry)

        collectorSystem = CollectorSystem(transfer= transfer, extendo= extendo, telemetry= telemetry)
        lift = Lift(telemetry)
        arm = Arm()
        wrist = Wrist(left = Claw(telemetry), right = Claw(telemetry), telemetry= telemetry)
        depoManager = DepoManager(arm= arm, lift= lift, wrist= wrist, telemetry= telemetry)
    }

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
                        if (timeSinceEnd > timeDelayMilis) {
                            PositionAndRotation(Math.random() * xBoxSizeInches, Math.random() * yBoxSizeInches,(Math.random() * 360*2)-360)//positions[positions.indexOf(currentTarget) + 1]
                        } else {
                            previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition
                        }
                    } else {
                        timeOfTargetDone = actualState.timestampMilis
                        previousTargetState?.targetRobot?.drivetrainTarget?.targetPosition
                    }

                    previousTargetState?.copy(targetRobot = previousTargetState.targetRobot.copy(drivetrainTarget = Drivetrain.DrivetrainTarget(targetPosition = newTargetPosition?: PositionAndRotation())))
                            ?: RobotTwoTeleOp.initialPreviousTargetState
                },
                stateFulfiller = { targetState, previousTargetState, actualState ->
                    telemetry.addLine("target position: ${targetState.targetRobot.drivetrainTarget.targetPosition}")
                    telemetry.addLine("current position: ${drivetrain.localizer.currentPositionAndRotation()}")

                    drivetrain.actuateDrivetrain(
                            target = targetState.targetRobot.drivetrainTarget,
                            previousTarget = previousTargetState?.targetRobot?.drivetrainTarget ?: targetState.targetRobot.drivetrainTarget,
                            actualPosition = actualState.actualRobot.positionAndRotation,
                            xTranslationPID = getXTranslationPID(),
                            yTranslationPID = getYTranslationPID(),
                            rotationPID = getRotationPID())
//                    hardware.actuateRobot(
//                            targetState,
//                            previousTargetState?: targetState,
//                            actualState,
//                            drivetrain = drivetrain,
//                            wrist= wrist,
//                            arm= arm,
//                            lift= lift,
//                            extendo= extendo,
//                            intake= intake,
//                            transfer= transfer,
//                            extendoOverridePower = 0.0,
//                            armOverridePower = 0.0
//                    )

                    hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLUE)
                }
        )

        telemetry.update()
    }
}