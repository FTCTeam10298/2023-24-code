package us.brainstormz.robotTwo

import com.acmerobotics.dashboard.config.Config
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.motion.MecanumMovement.Companion.defaultRotationPID
import us.brainstormz.motion.MecanumMovement.Companion.defaultXTranslationPID
import us.brainstormz.motion.MecanumMovement.Companion.defaultYTranslationPID
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
import us.brainstormz.utils.DeltaTimeMeasurer
import java.lang.Thread.sleep


@Config
object PidTuningAdjuster {
    @JvmField
    var timeDelayMilis: Int = 500

    @JvmField
    var yBoxSizeInches = 40

    @JvmField
    var xBoxSizeInches = 20

    @JvmField
    var yp = defaultYTranslationPID.kp
    @JvmField
    var yi = defaultYTranslationPID.ki
    @JvmField
    var yd = defaultYTranslationPID.kd
    @JvmField
    var yf = defaultYTranslationPID.kf
    fun getYTranslationPID(): PID {
        return PID(kp= yp, ki= yi, kd= yd, kf= yf)
    }
    @JvmField
    var xp = defaultXTranslationPID.kp
    @JvmField
    var xi = defaultXTranslationPID.ki
    @JvmField
    var xd = defaultXTranslationPID.kd
    @JvmField
    var xf = defaultXTranslationPID.kf
    fun getXTranslationPID(): PID {
        return PID(kp= xp, ki= xi, kd= xd, kf= xf)
    }
    @JvmField
    var rp = defaultRotationPID.kp
    @JvmField
    var ri = defaultRotationPID.ki
    @JvmField
    var rd = defaultRotationPID.kd
    @JvmField
    var rf = defaultRotationPID.kf
    fun getRotationPID(): PID {
        return PID(kp= rp, ki= ri, kd= rd, kf= rf)
    }
}

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
                        if (timeSinceEnd > timeDelayMilis) {
                            PositionAndRotation(Math.random() * xBoxSizeInches, Math.random() * yBoxSizeInches,(Math.random() * 360*2)-360)//positions[positions.indexOf(currentTarget) + 1]
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
                            xTranslationPID = getXTranslationPID(),
                            yTranslationPID = getYTranslationPID(),
                            rotationPID = getRotationPID())

                    hardware.lights.setPattern(targetState.targetRobot.lights.targetColor)
                }
        )

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMilis()

//        telemetry.addLine("xpid: \n${getXTranslationPID()}")
//        telemetry.addLine("ypid: \n${getYTranslationPID()}")
//        telemetry.addLine("rpid: \n${getRotationPID()}")

        telemetry.addLine("loopTime: $loopTime")

        telemetry.addLine("average loopTime: ${loopTimeMeasurer.getAverageLoopTimeMilis()}")

        sleep(104-84)

        telemetry.update()
    }
}