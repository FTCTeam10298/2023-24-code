package us.brainstormz.robotTwo

import com.fasterxml.jackson.annotation.JsonIgnore
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.openftc.easyopencv.OpenCvCameraRotation
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.robotTwo.RobotTwoHardware.UnchangingRobotAttributes.alliance
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.robotTwo.RobotTwoPropDetector.PropColors
import us.brainstormz.robotTwo.RobotTwoPropDetector.PropPosition
import us.brainstormz.robotTwo.RobotTwoTeleOp.*
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initialPreviousTargetState
import us.brainstormz.robotTwo.localTests.TeleopTest
import us.brainstormz.robotTwo.onRobotTests.AprilTagPipeline
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem.*
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Extendo.ExtendoPositions
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.utils.measured
import kotlin.math.absoluteValue


class RobotTwoAuto(
    private val telemetry: Telemetry,
    private val aprilTagPipeline: AprilTagPipeline
): RobotTwo(telemetry) {

    private val blankAutoState = AutoInput(
            drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation()),
            depoInput = DepoInput.NoInput,
            handoffInput = HandoffInput.NoInput,
            wristInput = WristInput(ClawInput.NoInput, ClawInput.NoInput),
            extendoInput = ExtendoPositions.Min,
            getNextInput = { actualWorld, previousActualWorld, previousTargetWorld -> getNextTargetFromList() }
    )

    private fun hasTimeElapsed(timeToElapseMilis: Long, targetWorld: TargetWorld): Boolean {
        val taskStartedTimeMilis = targetWorld.timeTargetStartedMilis
        val timeSinceTargetStarted = System.currentTimeMillis() - taskStartedTimeMilis
        return timeSinceTargetStarted >= timeToElapseMilis
    }

    private fun isRobotAtPosition(actualState: ActualWorld, previousActualState: ActualWorld, targetWorld: TargetWorld, precisionInches: Double = drivetrain.precisionInches, precisionDegrees: Double = drivetrain.precisionDegrees): Boolean {
        return drivetrain.checkIfDrivetrainIsAtPosition(targetWorld.targetRobot.drivetrainTarget.targetPosition, previousWorld = previousActualState, actualWorld = actualState, precisionInches = precisionInches, precisionDegrees = precisionDegrees)
    }


    private val autoStateList: List<AutoInput> = listOf(
            blankAutoState.copy(
                    drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation()),
                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                        nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                    }
            ),
            blankAutoState.copy(
                    drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(x = 10.0)),
                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                        nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                    }
            ),
            blankAutoState.copy(
                    drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(10.0, 10.0)),
                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                        nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                    }
            ),
            blankAutoState.copy(
                    drivetrainTarget = Drivetrain.DrivetrainTarget(PositionAndRotation(10.0, 10.0, 90.0)),
                    getNextInput = { actualWorld, previousActualWorld, targetWorld ->
                        nextTargetFromCondition(isRobotAtPosition(actualWorld, previousActualWorld, targetWorld), targetWorld)
                    }
            ),
    )



    fun getTargetWorldFromAutoInput(autoInput: AutoInput, actualWorld: ActualWorld, previousActualWorld: ActualWorld, previousTargetWorld: TargetWorld): TargetWorld {

        /**Handoff*/
        val transferState = transfer.getTransferState(
                actualWorld = actualWorld,
                previousTransferState = previousTargetWorld.targetRobot.collectorTarget.transferSensorState,
        )

        val transferLeftSensorState = transfer.checkIfPixelIsTransferred(transferState.left)
        val transferRightSensorState = transfer.checkIfPixelIsTransferred(transferState.right)
        val areBothPixelsIn = transferLeftSensorState && transferRightSensorState

        val previousTransferLeftSensorState = transfer.checkIfPixelIsTransferred(previousTargetWorld.targetRobot.collectorTarget.transferSensorState.left)
        val previousTransferRightSensorState = transfer.checkIfPixelIsTransferred(previousTargetWorld.targetRobot.collectorTarget.transferSensorState.right)
        val wereBothPixelsInPreviously = previousTransferLeftSensorState && previousTransferRightSensorState

        val theRobotJustCollectedTwoPixels = areBothPixelsIn && !wereBothPixelsInPreviously

        val uncoordinatedCollectorTarget = CollectorTarget(
                intakeNoodles = Intake.CollectorPowers.Off,
                dropDown = Dropdown.DropdownTarget(Dropdown.DropdownPresets.Up),
                timeOfEjectionStartMilis = 0,
                timeOfTransferredMillis = 0,
                transferSensorState = transferState,
                latches = Transfer.TransferTarget(
                        left = Transfer.LatchTarget(Transfer.LatchPositions.Closed, 0),
                        right = Transfer.LatchTarget(Transfer.LatchPositions.Closed, 0),),
                extendo = Extendo.ExtendoTarget(autoInput.extendoInput),
        )

        val extendoDriverInput = when (autoInput.extendoInput) {
            ExtendoPositions.Min -> ExtendoInput.RetractManual
            else -> ExtendoInput.ExtendManual
        }

        val doingHandoff = autoInput.handoffInput == HandoffInput.Handoff

        val handoffTarget = handoffManager.manageHandoff(
                handoffInput = autoInput.handoffInput,
                wristInput = autoInput.wristInput,
                depoInput = autoInput.depoInput,
                extendoInput = extendoDriverInput,
                collectorTarget = uncoordinatedCollectorTarget,
                previousTargetWorld = previousTargetWorld,
                actualWorld = actualWorld,
                doingHandoff = doingHandoff,
        )

        return TargetWorld(
                targetRobot = TargetRobot(
                        drivetrainTarget = autoInput.drivetrainTarget,
                        depoTarget = handoffTarget.depo,
                        collectorTarget = handoffTarget.collector,
                        hangPowers = RobotTwoHardware.HangPowers.Holding,
                        launcherPosition = RobotTwoHardware.LauncherPosition.Holding,
                        lights = LightTarget(),
                ),
                doingHandoff = doingHandoff,
                driverInput = initialPreviousTargetState.driverInput,
                autoInput = autoInput,
                timeTargetStartedMilis = if (autoInput != previousTargetWorld.autoInput) {
                    actualWorld.timestampMilis
                } else {
                    previousTargetWorld.timeTargetStartedMilis
                },
                gamepad1Rumble = null
        )
    }


    data class AutoInput (
            val drivetrainTarget: Drivetrain.DrivetrainTarget,
            val depoInput: DepoInput,
            val handoffInput: HandoffInput,
            val wristInput: WristInput,
            val extendoInput: ExtendoPositions,
            @get:JsonIgnore
            val getNextInput: (actualWorld: ActualWorld, previousActualWorld: ActualWorld, targetWorld: TargetWorld) -> AutoInput
    )

    private fun nextTargetFromCondition(condition: Boolean, targetWorld: TargetWorld): AutoInput {
        return if (condition) {
            getNextTargetFromList()
        } else {
            targetWorld.autoInput ?: getNextTargetFromList()
        }
    }

    private fun getNextTargetFromList(): AutoInput {
        return if (autoListIterator.hasNext()) {
            autoListIterator.next()
        } else {
            autoStateList.last()
        }
    }

    private lateinit var autoListIterator: ListIterator<AutoInput>
    private fun nextTargetState(actualState: ActualWorld, previousActualState: ActualWorld?, previousTargetState: TargetWorld?): AutoInput {
        return if (previousTargetState != null && previousActualState != null) {
            previousTargetState.autoInput?.getNextInput?.invoke(actualState, previousActualState, previousTargetState)
                    ?: getNextTargetFromList()
        } else {
            getNextTargetFromList()
        }
    }


    private fun flipRedPositionToBlue(positionAndRotation: PositionAndRotation): PositionAndRotation {
        return positionAndRotation.copy(x= -positionAndRotation.x, r= -positionAndRotation.r)
    }

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
        initRobot(
            hardware = hardware,
            localizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        )


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

//        drivetrain.localizer.setPositionAndRotation(startPositionAndRotation)
        drivetrain.localizer.setPositionAndRotation(PositionAndRotation())
    }


    fun loop(hardware: RobotTwoHardware, gamepad1: SerializableGamepad) = measured("main loop"){

        runRobot(
                targetStateFetcher = { actualWorld, previousActualWorld, previousTargetWorld ->

                    val newInput = nextTargetState(
                            actualWorld,
                            previousActualWorld,
                            previousTargetWorld
                    )

                    val previousActualWorld = previousActualWorld ?: TeleopTest.emptyWorld
                    val previousTargetWorld: TargetWorld = previousTargetWorld ?: initialPreviousTargetState
                    val targetWorld = getTargetWorldFromAutoInput(
                            autoInput = newInput,
                            actualWorld = actualWorld,
                            previousActualWorld = previousActualWorld,
                            previousTargetWorld = previousTargetWorld
                    )

                    targetWorld
                },
                gamepad1 = gamepad1,
                gamepad2 = gamepad1,
                hardware = hardware
        )
    }
}