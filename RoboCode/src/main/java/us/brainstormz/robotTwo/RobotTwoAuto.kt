package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.pid.PID
import us.brainstormz.robotTwo.RobotTwoHardware.UnchangingRobotAttributes.alliance
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.robotTwo.RobotTwoPropDetector.PropColors
import us.brainstormz.robotTwo.RobotTwoPropDetector.PropPosition
import us.brainstormz.robotTwo.RobotTwoTeleOp.*
import us.brainstormz.robotTwo.localTests.TeleopTest.Companion.emptyWorld
import us.brainstormz.robotTwo.onRobotTests.AprilTagPipeline
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw.ClawTarget
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*
import us.brainstormz.robotTwo.subsystems.Extendo.ExtendoPositions
import us.brainstormz.robotTwo.subsystems.Intake.CollectorPowers
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Transfer.TransferTarget
import us.brainstormz.robotTwo.subsystems.Transfer.LatchPositions
import us.brainstormz.utils.measured

class RobotTwoAuto(
    private val telemetry: Telemetry,
    private val aprilTagPipeline: AprilTagPipeline
): RobotTwo(telemetry) {






    private val autoStateList: List<AutoInput> = listOf(
        AutoInput(
            drivePosition = PositionAndRotation(),
            depo = DepoInput.NoInput,
            wrist = WristInput(ClawInput.NoInput, ClawInput.NoInput),
            collector = CollectorInput.NoInput,
            dropdown = DropdownInput.NoInput,
            leftLatch = LatchInput.NoInput,
            rightLatch = LatchInput.NoInput,
            extendo = ExtendoInput.NoInput,
            handoff = HandoffInput.NoInput,
            hang = HangInput.NoInput,
            launcher = LauncherInput.NoInput,
            getNextTask = { _, _, target -> nextTargetFromCondition(true, target) }
        )
    )






    private fun nextTargetFromCondition(condition: Boolean, previousTargetState: TargetWorld): AutoInput {
        return if (condition) {
            getNextTargetFromList()
        } else {
            /*previousTargetState.getNextTask() ?: */AutoInput(previousTargetState.driverInput) { _, _, _, -> null }
        }
    }

    private fun getNextTargetFromList(): AutoInput {
        return autoListIterator.next()
    }

    private lateinit var autoListIterator: ListIterator<AutoInput>
    private fun nextTargetState(actualState: ActualWorld, previousActualState: ActualWorld, previousTargetState: TargetWorld?): AutoInput {
        return if (previousTargetState == null) {
            getNextTargetFromList()
        } else {
            when {
                autoListIterator.hasNext()-> {


                    previousTargetState.getNextTask?.let { getNextTask ->
                        val previousInput = previousTargetState.getNextTask.invoke(actualState, previousActualState, previousTargetState)

                        previousInput
                    } ?: previousTargetState.driverInput.toAutoInput()
                }
                else -> {
                    val previousInput =previousTargetState.driverInput
                    AutoInput(
                        previousInput,
                        { _, _, _ -> previousInput.toAutoInput() }
                    )
                }
            }
        }
    }


    private fun flipRedPositionToBlue(positionAndRotation: PositionAndRotation): PositionAndRotation {
        return positionAndRotation.copy(x= -positionAndRotation.x, r= -positionAndRotation.r)
    }
    
    data class AutoInput(
        val drivePosition: PositionAndRotation,
        val depo: DepoInput,
        val wrist: WristInput,
        val collector: CollectorInput,
        val dropdown: DropdownInput,
        val extendo: ExtendoInput,
        val hang: HangInput,
        val launcher: LauncherInput,
        val handoff: HandoffInput,
        val leftLatch: LatchInput,
        val rightLatch: LatchInput,
        val getNextTask: (actualState: ActualWorld, previousActualState: ActualWorld, targetState: TargetWorld) -> AutoInput?,
    ) {
        val driverInput = DriverInput(
            driveVelocity = Drivetrain.DrivetrainPower(),
            depo = depo,
            depoScoringHeightAdjust = 0.0,
            armOverridePower = 0.0,
            wrist = wrist,
            collector = collector,
            dropdown = dropdown,
            dropdownPositionOverride = 0.0,
            leftLatch = leftLatch,
            rightLatch = rightLatch,
            extendo = extendo,
            extendoManualPower = 0.0,
            handoff = handoff,
            hang = hang,
            launcher = launcher,
            bumperMode = Gamepad1BumperMode.Collector,
            gamepad1ControlMode = GamepadControlMode.Normal,
            gamepad2ControlMode = GamepadControlMode.Normal,
            lightInput = LightInput.NoColor
        )

        constructor(
            driverInput: DriverInput,
            getNextTask: (actualState: ActualWorld, previousActualState: ActualWorld, targetState: TargetWorld) -> AutoInput?,
        ): this(
            drivePosition = PositionAndRotation(),
            depo = driverInput.depo,
            wrist = driverInput.wrist,
            collector = driverInput.collector,
            dropdown = driverInput.dropdown,
            leftLatch = driverInput.leftLatch,
            rightLatch = driverInput.rightLatch,
            extendo = driverInput.extendo,
            handoff = driverInput.handoff,
            hang = driverInput.hang,
            launcher = driverInput.launcher,
            getNextTask = getNextTask
        )
    }

    data class MovementPIDSet(val x: PID, val y: PID, val r: PID)
    data class RobotState(
            val positionAndRotation: PositionAndRotation,
            val movementMode: MovementMode = MovementMode.Position,
            val drivePower: Drivetrain.DrivetrainPower = Drivetrain.DrivetrainPower(),
            val movementPIDs: MovementPIDSet? = null,
            val depoState: DepoState,
            val collectorSystemState: CollectorState
    )
    data class CollectorState(
            val collectorState: CollectorPowers,
            val extendoPosition: ExtendoPositions,
            val transferRollersState: TransferTarget,
    )
    data class TransferTarget(val left: LatchPositions, val right: LatchPositions) {
        fun toRealTransferTarget(): Transfer.TransferTarget {
            return TransferTarget(
                    left = Transfer.LatchTarget(left, 0),
                    right = Transfer.LatchTarget(right, 0)
            )
        }
    }

    data class DepoState(
            val armPos: Arm.Positions,
            val liftPosition: Lift.LiftPositions,
            val leftClawPosition: ClawTarget,
            val rightClawPosition: ClawTarget,
    )


    enum class StartPosition(val redStartPosition: PositionAndRotation) {
        Backboard(PositionAndRotation(  x = RobotTwoHardware.redStartingXInches,
            y= -12.0,
            r= RobotTwoHardware.redStartingRDegrees)),
        Audience(PositionAndRotation(   x = RobotTwoHardware.redStartingXInches,
            y= 36.0,
            r= RobotTwoHardware.redStartingRDegrees))
    }

    private val console = TelemetryConsole(telemetry)
    private val wizard = TelemetryWizard(console, null)
    data class WizardResults(val alliance: RobotTwoHardware.Alliance, val startPosition: StartPosition, val partnerIsPlacingYellow: Boolean, val shouldDoYellow: Boolean)

    private fun getMenuWizardResults(gamepad1: Gamepad): WizardResults {
        return WizardResults(
                alliance = when (wizard.wasItemChosen("alliance", "Red")) {
                    true -> RobotTwoHardware.Alliance.Red
                    false -> RobotTwoHardware.Alliance.Blue
                },
                startPosition = when (wizard.wasItemChosen("startingPos", "Audience")) {
                    true -> StartPosition.Audience
                    false -> StartPosition.Backboard
                },
                partnerIsPlacingYellow = wizard.wasItemChosen("partnerYellow", "Yellow"),
                shouldDoYellow = wizard.wasItemChosen("doYellow", "Yellow")
        )
    }

    private var startPosition: StartPosition = StartPosition.Backboard

    private var propDetector: RobotTwoPropDetector? = null

    fun init(hardware: RobotTwoHardware, opencv: OpenCvAbstraction) {
        initRobot(hardware)

        val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)

        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"), nextMenu = "partnerYellow", firstMenu = true)
        wizard.newMenu("partnerYellow", "What will our partner be placing on the board?", listOf("Yellow", "Nothing"), nextMenu = "startingPos")
        wizard.newMenu("startingPos", "What side of the truss are we on?", listOf("Audience" to "doYellow", "Backboard" to null))
        wizard.newMenu("doYellow", "What do we do after the purple?", listOf("Yellow", "Nothing"))

        opencv.internalCamera = false
        opencv.cameraName = "Webcam 1"
        opencv.cameraOrientation = OpenCvCameraRotation.UPRIGHT
    }

    private fun runCamera(opencv: OpenCvAbstraction, wizardResults: WizardResults) {
        val propColor: PropColors = when (wizardResults.alliance) {
            RobotTwoHardware.Alliance.Blue -> PropColors.Blue
            RobotTwoHardware.Alliance.Red -> PropColors.Red
        }
        propDetector = RobotTwoPropDetector(telemetry, propColor)
        opencv.onNewFrame(propDetector!!::processFrame)
    }

    private var wizardResults: WizardResults? = null
    fun initLoop(hardware: RobotTwoHardware, opencv: OpenCvAbstraction, gamepad1: SerializableGamepad) {
        if (wizardResults == null) {
            val isWizardDone = wizard.summonWizard(gamepad1.theGamepad ?: Gamepad())
            if (isWizardDone) {
                wizardResults = getMenuWizardResults(gamepad1.theGamepad ?: Gamepad())

                alliance = wizardResults!!.alliance
                startPosition = wizardResults!!.startPosition

                runCamera(opencv, wizardResults!!)
//                hardware.lights.setPattern(RevBlinkinLedDriver.BlinkinPattern.BLACK)
            }
        } else {
            telemetry.addLine("propPosition? = ${propDetector?.propPosition}")
            telemetry.addLine("wizardResults = ${wizardResults}")
        }
    }

    fun start(hardware: RobotTwoHardware, opencv: OpenCvAbstraction) {
        val propPosition = propDetector?.propPosition ?: PropPosition.Right
        try{
            opencv.stop()
        }catch (t:Throwable){
            t.printStackTrace()
            println("WARN: OPENCV Didn't stop cleanly")
        }

        val startPositionAndRotation: PositionAndRotation = when (alliance) {
            RobotTwoHardware.Alliance.Red -> startPosition.redStartPosition
            RobotTwoHardware.Alliance.Blue -> flipRedPositionToBlue(startPosition.redStartPosition)
        }

        autoListIterator = autoStateList.listIterator()

        drivetrain.localizer.setPositionAndRotation(startPositionAndRotation)
    }


    fun loop(hardware: RobotTwoHardware, gamepad1: SerializableGamepad) = measured("main loop"){
        runRobot(
            { actual, previousActual, previousTarget ->
                val autoInput = nextTargetState(
                    actual,
                    previousActual?:emptyWorld,
                    previousTarget
                )
                val teleopTargetWorld = getTargetWorldFromDriverInput(
                    { _, _, _ -> autoInput.driverInput},
                    actual,
                    previousActual,
                    previousTarget
                )
                teleopTargetWorld.copy(
                    targetRobot = teleopTargetWorld.targetRobot.copy(
                        drivetrainTarget = Drivetrain.DrivetrainTarget(autoInput.drivePosition)
                    ),
                    getNextTask = {actual, previousActual, target ->
                        autoInput.getNextTask(actual, previousActual, target)!!
                    }
                )
            },
            gamepad1,
            gamepad1,
            hardware
        )
    }
}