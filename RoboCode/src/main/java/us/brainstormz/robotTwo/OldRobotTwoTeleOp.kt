package us.brainstormz.robotTwo

import com.outoftheboxrobotics.photoncore.Photon
import com.qualcomm.hardware.lynx.LynxModule
import com.qualcomm.hardware.rev.RevBlinkinLedDriver
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.robotcore.external.Telemetry
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit
import us.brainstormz.hardwareClasses.MecanumDriveTrain
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.robotTwo.RobotTwoHardware.LeftClawPosition
import us.brainstormz.robotTwo.RobotTwoHardware.RightClawPosition
import us.brainstormz.robotTwo.RobotTwoHardware.RobotState
import us.brainstormz.utils.DeltaTimeMeasurer
import kotlin.math.abs
import kotlin.math.absoluteValue


@Photon
@TeleOp(group = "!")
class OldRobotTwoTeleOp: OpMode() {

    private val hardware = RobotTwoHardware(telemetry, this)
    val movement = MecanumDriveTrain(hardware)
    private lateinit var arm: Arm
    private lateinit var collectorSystem: CollectorSystem
    private lateinit var lift: Lift
    private lateinit var handoffManager: HandoffManager

    private lateinit var odometryLocalizer: RRTwoWheelLocalizer

    private val initialRobotState = RobotState(
            positionAndRotation = PositionAndRotation(),
            collectorSystemState = CollectorSystem.CollectorState(
                    collectorState = CollectorSystem.CollectorPowers.Off,
                    extendoPosition = CollectorSystem.ExtendoPositions.Manual,
                    transferRollersState = CollectorSystem.RollerState(CollectorSystem.RollerPowers.Off, CollectorSystem.RollerPowers.Off, CollectorSystem.DirectorState.Off),
                    transferLeftSensorState = CollectorSystem.TransferHalfState(false, 0),
                    transferRightSensorState = CollectorSystem.TransferHalfState(false, 0)
            ),
            depoState = RobotTwoAuto.DepoState(
                    liftPosition = Lift.LiftPositions.Manual,
                    armPos = Arm.Positions.Manual,
                    leftClawPosition = LeftClawPosition.Retracted,
                    rightClawPosition = RightClawPosition.Retracted,
            ),
    )

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)

        arm = Arm(  encoder= hardware.armEncoder,
                    armServo1= hardware.armServo1,
                    armServo2= hardware.armServo2, telemetry)
        collectorSystem = CollectorSystem(  extendoMotorMaster= hardware.extendoMotorMaster,
                                extendoMotorSlave= hardware.extendoMotorSlave,
                                collectorServo1 = hardware.collectorServo1,
                                collectorServo2 = hardware.collectorServo2,
                                rightTransferServo=hardware.rightTransferServo,
                                leftTransferServo= hardware.leftTransferServo,
                                transferDirectorServo= hardware.transferDirectorServo,
                                leftTransferPixelSensor= hardware.leftTransferSensor,
                                rightTransferPixelSensor= hardware.rightTransferSensor,
                                leftRollerEncoder= hardware.leftRollerEncoder,
                                rightRollerEncoder= hardware.rightRollerEncoder,
                                telemetry= telemetry)
        lift = Lift(liftMotor1 = hardware.liftMotorMaster,
                    liftMotor2 = hardware.liftMotorSlave,
                    liftLimit = hardware.liftMagnetLimit)
        handoffManager = HandoffManager(
                collectorSystem,
                lift,
                arm,
                telemetry)

        odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)

        // Bulk encoder reads ----------------------------------------------------------------------
        // From ConceptMotorBulkRead.java, see that file for details

        // Important Step 2: Get access to a list of Expansion Hub Modules to enable changing caching methods.
//        allHubs = hardwareMap.getAll(LynxModule::class.java)

        // Important Step 3: Option B. Set all Expansion hubs to use the MANUAL Bulk Caching mode
        for (module in hardware.allHubs) {
            module.bulkCachingMode = LynxModule.BulkCachingMode.MANUAL
        }
    }

    private val loopTimeMeasurer = DeltaTimeMeasurer()
    private val handoffTimeMeasurer = DeltaTimeMeasurer()
    private val extendoTimeMeasurer = DeltaTimeMeasurer()
    private val liftTimeMeasurer = DeltaTimeMeasurer()
    private val armTimeMeasurer = DeltaTimeMeasurer()
    private val collectorTimeMeasurer = DeltaTimeMeasurer()
    private val clawsTimeMeasurer = DeltaTimeMeasurer()
    private val driveTimeMeasurer = DeltaTimeMeasurer()
    private val planeTimeMeasurer = DeltaTimeMeasurer()
    private val hangTimeMeasurer = DeltaTimeMeasurer()
    private val lightsTimeMeasurer = DeltaTimeMeasurer()
    private val telemetryTimeMeasurer = DeltaTimeMeasurer()
    private val miscTimeMeasurer = DeltaTimeMeasurer()

    private var numberOfTimesColorButtonPressed: Int = 0
    private var previousDesiredPixelLightPattern: BothPixelsWeWant = BothPixelsWeWant(leftPixel = PixelColor.Unknown, rightPixel = PixelColor.Unknown)
    private var previousIsAnyColorButtonPressed: Boolean = false
    private var timeWhenCurrentColorStartedBeingDisplayedMilis = 0L
    private var previousPixelToBeDisplayed = previousDesiredPixelLightPattern.leftPixel

    var wereBothPixelsInPreviously = false

    var wereWeDoingHandoffLastLoop = false

    var previousIsLiftEligableForReset = false

    private var previousGamepad1State: Gamepad = Gamepad()
    private var previousGamepad2State: Gamepad = Gamepad()

    enum class Sensors(val returnType: Any) {
        ArmEncoder(Double),
        LiftMagnetLimit(Boolean),
        LeftRollerEncoder(Double),
        RightRollerEncoder(Double),
    }
//    var current: PeriodicSupplier<Map<Sensors, Any>> = PeriodicSupplier(FutureSupplier {
//        mapOf(
//                Sensors.ArmEncoder  to hardware.armEncoder.voltage
//        )
//                                                                            }, 100)

    enum class Gamepad1BumperMode {
        Collector,
        Claws
    }
    var previousGamepadOneBumperMode = Gamepad1BumperMode.Collector
    val twoBeatRumble = Gamepad.RumbleEffect.Builder().addStep(1.0, 1.0, 500).addStep(0.0, 0.0, 100).addStep(1.0, 1.0, 500).build()

    private var previousRobotState = initialRobotState
    override fun loop() {
        /** TELE-OP PHASE */

        val loopTime = loopTimeMeasurer.measureTimeSinceLastCallMilis()
        telemetry.addLine("Loop time (Current): $loopTime ms")
        telemetry.addLine("Loop time (Peak): ${loopTimeMeasurer.peakDeltaTime} ms")

        // Important Step 4: If you are using MANUAL mode, you must clear the BulkCache once per control cycle
        for (module in hardware.allHubs) {
            module.clearBulkCache()
        }

        // FIXME:
        //Spit out extra pixels
        //Wait to retract depo during driver 1 until 500milis after claws retract
        //use green color amount in threshold measuring
        //figure out smashing issue
        //arm goes down too much and gets stuck

        miscTimeMeasurer.endMeasureDT()

        // Handoff related inputs ------------------------------------------------------------------
        handoffTimeMeasurer.beginMeasureDT()
        val isHandoffButtonPressed = (gamepad2.a && !gamepad2.dpad_left) || (gamepad1.a && !gamepad1.start)

        val extendoTriggerActivation = 0.1
        val rightTrigger: Boolean = gamepad1.right_trigger > extendoTriggerActivation
        val leftTrigger: Boolean = gamepad1.left_trigger > extendoTriggerActivation

        val liftOverrideStickValue = gamepad2.right_stick_y.toDouble()
        val areLiftManualControlsActive = liftOverrideStickValue.absoluteValue > 0.2

        val depoGamepad2Input: Lift.LiftPositions? = when {
            gamepad2.dpad_up-> {
                Lift.LiftPositions.SetLine3
            }
            gamepad2.dpad_down -> {
                Lift.LiftPositions.Transfer
            }
            gamepad2.dpad_right && !previousGamepad2State.dpad_right -> {
                if (previousRobotState.depoState.liftPosition !== Lift.LiftPositions.SetLine1) {
                    Lift.LiftPositions.SetLine1
                } else {
                    Lift.LiftPositions.SetLine2
                }
            }
            else -> null
        }
        val depoGamepad1Input: Lift.LiftPositions? = when {
            gamepad1.dpad_up-> {
                Lift.LiftPositions.SetLine3
            }
            gamepad1.dpad_left -> {
                Lift.LiftPositions.SetLine2
            }
            gamepad1.dpad_down -> {
                Lift.LiftPositions.SetLine1
            }
            else -> null
        }

        val depoInput = depoGamepad2Input ?: depoGamepad1Input

        val armOverrideStickValue = gamepad2.right_stick_x.toDouble()
        val isArmManualOverrideActive = armOverrideStickValue.absoluteValue >= 0.2


        // Gamepad1 Bumper Mode --------------------------------------------------------------------
        val gamepad1DpadIsActive = depoGamepad1Input != null
        val liftTargetIsDown = previousRobotState.depoState.liftPosition.ticks <= Lift.LiftPositions.Min.ticks
        val bothClawsAreRetracted = hardware.leftClawServo.position == LeftClawPosition.Retracted.position && hardware.rightClawServo.position == RightClawPosition.Retracted.position
        val gamepadOneBumperMode: Gamepad1BumperMode  = when {
            gamepad1DpadIsActive -> {
                Gamepad1BumperMode.Claws
            }
            !gamepad1DpadIsActive && (bothClawsAreRetracted || liftTargetIsDown) -> {
                Gamepad1BumperMode.Collector
            }
            else -> {
                previousGamepadOneBumperMode
            }
        }


        // Handoff ---------------------------------------------------------------------------------
        val transferSensorState = collectorSystem.getCurrentState(previousRobotState.collectorSystemState)
        val areBothPixelsIn = transferSensorState.transferLeftSensorState.hasPixelBeenSeen && transferSensorState.transferRightSensorState.hasPixelBeenSeen
        val theRobotJustCollectedTwoPixels = areBothPixelsIn && !wereBothPixelsInPreviously

        val weWantToStartHandoff = isHandoffButtonPressed || theRobotJustCollectedTwoPixels

        val inputsConflictWithTransfer = rightTrigger || (depoInput != null)

        telemetry.addLine("\nHANDOFF:")
        val doHandoffSequence = when {
            inputsConflictWithTransfer -> {
                telemetry.addLine("Canceled due to conflicting inputs")
                false
            }
            weWantToStartHandoff -> {
                telemetry.addLine("Starting handoff")
                true
            }
            else -> {
                telemetry.addLine("Doing the same thing as last time")
                wereWeDoingHandoffLastLoop
            }
        }
        telemetry.addLine("Are we doing handoff: $doHandoffSequence")
        wereWeDoingHandoffLastLoop= doHandoffSequence
        telemetry.addLine()

        val previousBothClawState = when (previousRobotState.depoState.rightClawPosition) {
            RightClawPosition.Retracted -> HandoffManager.ClawStateFromHandoff.Retracted
            RightClawPosition.Gripping -> HandoffManager.ClawStateFromHandoff.Gripping
        }
        val handoffState = handoffManager.getHandoffState(previousBothClawState, RevBlinkinLedDriver.BlinkinPattern.BLUE)

        val aClawWasPreviouslyRetracted = previousRobotState.depoState.rightClawPosition.position == RightClawPosition.Retracted.position || hardware.leftClawServo.position == LeftClawPosition.Retracted.position
        val bothClawsAreGripping = hardware.leftClawServo.position == LeftClawPosition.Gripping.position && hardware.rightClawServo.position == RightClawPosition.Gripping.position
        if (doHandoffSequence && bothClawsAreGripping && aClawWasPreviouslyRetracted) {
            gamepad2.rumble(1.0, 1.0, 800)
            gamepad1.rumble(1.0, 1.0, 800)
        }

        handoffTimeMeasurer.endMeasureDT()

        // Extendo ---------------------------------------------------------------------------------
        extendoTimeMeasurer.beginMeasureDT()
        val extendoState: CollectorSystem.ExtendoPositions = when {
            rightTrigger && leftTrigger -> {
                hardware.extendoMotorMaster.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
                hardware.extendoMotorMaster.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
                CollectorSystem.ExtendoPositions.Min
            }
            leftTrigger -> {
                CollectorSystem.ExtendoPositions.Manual
            }
            rightTrigger -> {
                CollectorSystem.ExtendoPositions.Manual
            }
            theRobotJustCollectedTwoPixels -> {
                CollectorSystem.ExtendoPositions.Min
            }
            doHandoffSequence -> {
                when (handoffState.collectorState) {
                    HandoffManager.ExtendoStateFromHandoff.MoveIn -> {
                        CollectorSystem.ExtendoPositions.AllTheWayInTarget
                    }
                    HandoffManager.ExtendoStateFromHandoff.MoveOutOfTheWay -> {
                        CollectorSystem.ExtendoPositions.ClearTransfer
                    }
                }
            }
            areBothPixelsIn && !wereBothPixelsInPreviously -> {
                CollectorSystem.ExtendoPositions.Min
            }
            else -> {
                previousRobotState.collectorSystemState.extendoPosition
            }
        }
        if (extendoState != CollectorSystem.ExtendoPositions.Manual){
            if (extendoState == CollectorSystem.ExtendoPositions.AllTheWayInTarget) {
                collectorSystem.powerExtendo(-0.5)
            } else {
                collectorSystem.moveExtendoToPosition(extendoState.ticks)
            }
        } else {
            val areTriggersOn = rightTrigger || leftTrigger

            val power = if (areTriggersOn) {
                val triggerPower: Double = (gamepad1.right_trigger.toDouble() - gamepad1.left_trigger.toDouble())
                if (hardware.extendoMotorMaster.currentPosition <= CollectorSystem.ExtendoPositions.Min.ticks) {
                    triggerPower.coerceIn(-0.4..0.4)
                } else {
                    triggerPower
                }
            } else {
                0.0
            }

            collectorSystem.powerExtendo(power)
        }

        extendoTimeMeasurer.endMeasureDT()

        // Lift ------------------------------------------------------------------------------------
        liftTimeMeasurer.beginMeasureDT()
        val liftPosition: Lift.LiftPositions = if (areLiftManualControlsActive) {
            Lift.LiftPositions.Manual
        } else {
            when {
                previousGamepadOneBumperMode == Gamepad1BumperMode.Claws && gamepadOneBumperMode == Gamepad1BumperMode.Collector -> {
                    Lift.LiftPositions.Transfer
                }
                doHandoffSequence -> {
                    when (handoffState.liftState) {
                        HandoffManager.LiftStateFromHandoff.MoveDown -> Lift.LiftPositions.Transfer
                        HandoffManager.LiftStateFromHandoff.None -> Lift.LiftPositions.Nothing
                    }
                }
                else -> {
                    depoInput ?: previousRobotState.depoState.liftPosition
                }
            }
        }

        val liftTargetIsBelowSafeArm = liftPosition.ticks <= Lift.LiftPositions.ClearForArmToMove.ticks
        val liftActualPositionIsAboveSafeArm = hardware.liftMotorMaster.currentPosition >= Lift.LiftPositions.ClearForArmToMove.ticks
        val armIsFarEnoughIn = arm.getArmAngleDegrees() >= Arm.Positions.GoodEnoughForLiftToGoDown.angleDegrees
        val armIsTooFarIn = arm.getArmAngleDegrees() >= Arm.Positions.TooFarIn.angleDegrees
        val liftNeedsToWaitForTheArm = liftTargetIsBelowSafeArm && ((!armIsFarEnoughIn && liftActualPositionIsAboveSafeArm) || armIsTooFarIn)
        telemetry.setDisplayFormat(Telemetry.DisplayFormat.HTML)
        telemetry.addLine("\nliftNeedsToWaitForTheArm: $liftNeedsToWaitForTheArm")
        telemetry.addLine("armIsTooFarIn: $armIsTooFarIn")

        val liftPower = when {
            liftPosition == Lift.LiftPositions.Manual -> {
                -liftOverrideStickValue
            }
            liftPosition == Lift.LiftPositions.Nothing -> {
                0.0
            }
            liftNeedsToWaitForTheArm -> {
                lift.calculatePowerToMoveToPosition(Lift.LiftPositions.WaitForArmToMove.ticks)
            }
            liftPosition == Lift.LiftPositions.Transfer && lift.getCurrentPositionTicks() <= Lift.LiftPositions.Transfer.ticks -> {
//                if (!lift.isLimitSwitchActivated() && !lift.isLiftDrawingTooMuchCurrent()) {
//                    -0.3
//                } else {
//                    0.0
//                }
                0.0
            }
            else -> {
                lift.calculatePowerToMoveToPosition(liftPosition.ticks)
            }
        }
        lift.powerLift(liftPower)

        val liftTargetHasntChanged = liftPosition == previousRobotState.depoState.liftPosition
        val isLiftEligableForReset = lift.isLimitSwitchActivated() && liftTargetHasntChanged
        if (isLiftEligableForReset && !previousIsLiftEligableForReset) {
            hardware.liftMotorMaster.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
            hardware.liftMotorMaster.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

            gamepad1.rumble(1.0, 1.0, 1200)
        }
        previousIsLiftEligableForReset = isLiftEligableForReset

        telemetry.addLine("Lift position: ${lift.getCurrentPositionTicks()}")
        telemetry.addLine("Lift target: ${liftPosition}, ticks: ${liftPosition.ticks}")

        liftTimeMeasurer.endMeasureDT()

        // Arm -------------------------------------------------------------------------------------
        armTimeMeasurer.beginMeasureDT()
        val liftIsBelowFreeArmLevel = hardware.liftMotorMaster.currentPosition <= Lift.LiftPositions.ClearForArmToMove.ticks
        val armIsInish = arm.getArmAngleDegrees() >= Arm.Positions.Inish.angleDegrees

        val depositorShouldGoAllTheWayIn = liftPosition.ticks <= Lift.LiftPositions.ClearForArmToMove.ticks

        val liftPositionsWhereArmShouldBeOut = listOf(Lift.LiftPositions.SetLine1, Lift.LiftPositions.SetLine2, Lift.LiftPositions.SetLine3)

        val armWasManualControlLastTime = previousRobotState.depoState.armPos == Arm.Positions.Manual

        val armPosition: Arm.Positions = if (isArmManualOverrideActive || armWasManualControlLastTime && liftTargetHasntChanged) {
            Arm.Positions.Manual
        } else {
            when  {
                liftPosition == Lift.LiftPositions.Manual || liftPosition == Lift.LiftPositions.Nothing-> {
                    previousRobotState.depoState.armPos
                }
                doHandoffSequence -> {
                    telemetry.addLine("Using the transfer to decide where to move")
                    handoffState.armState
                }
                liftIsBelowFreeArmLevel  -> {
                    if (armIsInish) {
                        Arm.Positions.ClearLiftMovement
                    } else {
                        Arm.Positions.AutoInitPosition
                    }
                }
                depositorShouldGoAllTheWayIn && !liftIsBelowFreeArmLevel-> {
                    Arm.Positions.ClearLiftMovement
                }
                liftPosition in liftPositionsWhereArmShouldBeOut -> {
                    Arm.Positions.Out
                }
                else -> {
                    previousRobotState.depoState.armPos
                }
            }
        }
        if (armPosition == Arm.Positions.Manual) {
            arm.powerArm(armOverrideStickValue)
            telemetry.addLine("Arm power (manual override): $armOverrideStickValue")
        } else {
            arm.moveArmTowardPosition(armPosition.angleDegrees)
            telemetry.addLine("Arm target: $armPosition, angle: ${armPosition.angleDegrees}")
        }

        armTimeMeasurer.endMeasureDT()

        // Collector -------------------------------------------------------------------------------
        collectorTimeMeasurer.beginMeasureDT()
        fun nextPosition(isDirectionPositive: Boolean): CollectorSystem.CollectorPowers {
            val intakePowerOptions = mapOf(
                    1 to CollectorSystem.CollectorPowers.Intake,
                    0 to CollectorSystem.CollectorPowers.Off,
                    -1 to CollectorSystem.CollectorPowers.Eject
            )
            val previousPowerInt: Int = previousRobotState.collectorSystemState.collectorState.power.toInt()

            val valueToChangeBy = if (isDirectionPositive) {
                1
            } else {
                -1
            }
            val nonRangedChange = previousPowerInt + valueToChangeBy
            val newPowerOption =if (nonRangedChange !in -1..1) {
                0
            } else {
                nonRangedChange
            }

            return intakePowerOptions[newPowerOption] ?: CollectorSystem.CollectorPowers.Off
        }

        val inputCollectorStateSystem = if (gamepadOneBumperMode == Gamepad1BumperMode.Collector) {
            when {
                gamepad1.right_bumper && !previousGamepad1State.right_bumper -> {
                    nextPosition(true)
                }

                gamepad1.left_bumper && !previousGamepad1State.left_bumper -> {
                    nextPosition(false)
                }

                theRobotJustCollectedTwoPixels -> {
                    CollectorSystem.CollectorPowers.Off
                }

                else -> {
                    previousRobotState.collectorSystemState.collectorState
                }
            }
        } else {
            previousRobotState.collectorSystemState.collectorState
        }

        val actualCollectorState = collectorSystem.getCollectorState(inputCollectorStateSystem)
        collectorSystem.spinCollector(actualCollectorState.power)

        val autoRollerState = collectorSystem.getAutoPixelSortState(isCollecting = actualCollectorState == CollectorSystem.CollectorPowers.Intake)
        val rollerState = when {
            gamepad1.right_stick_button || gamepad1.left_stick_button -> {
                val leftEject = if (gamepad1.left_stick_button) {
                    CollectorSystem.RollerPowers.Eject
                } else {
                    CollectorSystem.RollerPowers.Off
                }
                val rightEject = if (gamepad1.right_stick_button) {
                    CollectorSystem.RollerPowers.Eject
                } else {
                    CollectorSystem.RollerPowers.Off
                }
                CollectorSystem.RollerState(leftServoCollect = leftEject,
                        rightServoCollect = rightEject,
                        directorState = CollectorSystem.DirectorState.Off)
            }
            gamepad1.b ->
                CollectorSystem.RollerState(leftServoCollect = CollectorSystem.RollerPowers.Intake,
                        rightServoCollect = CollectorSystem.RollerPowers.Intake,
                        directorState = CollectorSystem.DirectorState.Off)
            gamepad1.left_bumper ->
                autoRollerState.copy(directorState = CollectorSystem.DirectorState.Right)

//            gamepad1.dpad_down ->
//                CollectorSystem.RollerState(leftServoCollect = CollectorSystem.RollerPowers.Eject,
//                        rightServoCollect = CollectorSystem.RollerPowers.Eject,
//                        directorState = CollectorSystem.DirectorState.Off)
            else -> {
                autoRollerState
            }
        }
        collectorSystem.runRollers(rollerState)

        collectorTimeMeasurer.endMeasureDT()

        // Claws -----------------------------------------------------------------------------------
        clawsTimeMeasurer.beginMeasureDT()
        val isTheLiftGoingDown = liftPosition.ticks <= Lift.LiftPositions.ClearForArmToMove.ticks
        val wasTheLiftGoindDownBefore = previousRobotState.depoState.liftPosition.ticks <= Lift.LiftPositions.ClearForArmToMove.ticks
        val armIsIn = armPosition.angleDegrees >= Arm.Positions.GoodEnoughForLiftToGoDown.angleDegrees
        val isTheExtendoGoingIn = extendoState.ticks <= CollectorSystem.ExtendoPositions.Min.ticks
        val wasTheCollectorGoingInBefore = previousRobotState.collectorSystemState.extendoPosition.ticks <= CollectorSystem.ExtendoPositions.Min.ticks
        val extendoIsIn = collectorSystem.getExtendoPositionTicks() <= CollectorSystem.ExtendoPositions.Min.ticks
        val shouldTheClawsRetractForLift = isTheLiftGoingDown && !wasTheLiftGoindDownBefore && armIsIn
        val shouldTheClawsRetractForExtendo = isTheLiftGoingDown && !extendoIsIn//isTheExtendoGoingIn && !wasTheCollectorGoingInBefore
        val shouldClawsRetract = shouldTheClawsRetractForLift || shouldTheClawsRetractForExtendo

        val areGamepad1ClawControlsActive = gamepadOneBumperMode == Gamepad1BumperMode.Claws

        val gamepad1LeftClawToggle = areGamepad1ClawControlsActive && gamepad1.left_bumper && !previousGamepad1State.left_bumper
        val gamepad2LeftClawToggle = gamepad2.left_bumper && !previousGamepad2State.left_bumper
        val leftClawPosition: LeftClawPosition = if (gamepad2LeftClawToggle || gamepad1LeftClawToggle) {
            when (previousRobotState.depoState.leftClawPosition) {
                LeftClawPosition.Gripping -> LeftClawPosition.Retracted
                LeftClawPosition.Retracted -> LeftClawPosition.Gripping
            }
        } else if (doHandoffSequence) {
            when (handoffState.clawPosition) {
                HandoffManager.ClawStateFromHandoff.Gripping -> {
                    LeftClawPosition.Gripping
//                    if (transferSensorState.transferLeftSensorState.hasPixelBeenSeen) {
//                        LeftClawPosition.Gripping
//                    } else {
//                        LeftClawPosition.Retracted
//                    }
                }
                HandoffManager.ClawStateFromHandoff.Retracted -> LeftClawPosition.Retracted
            }
        } else if (shouldClawsRetract) {
            LeftClawPosition.Retracted
        } else {
            previousRobotState.depoState.leftClawPosition
        }
        hardware.leftClawServo.position = leftClawPosition.position

        val gamepad1RightClawToggle = areGamepad1ClawControlsActive && gamepad1.right_bumper && !previousGamepad1State.right_bumper
        val gamepad2RightClawToggle = gamepad2.right_bumper && !previousGamepad2State.right_bumper
        val rightClawPosition: RightClawPosition = if (gamepad2RightClawToggle || gamepad1RightClawToggle) {
            when (previousRobotState.depoState.rightClawPosition) {
                RightClawPosition.Gripping -> RightClawPosition.Retracted
                RightClawPosition.Retracted -> RightClawPosition.Gripping
            }
        } else if (doHandoffSequence) {
            when (handoffState.clawPosition) {
                HandoffManager.ClawStateFromHandoff.Gripping -> {
                    RightClawPosition.Gripping
//                    if (transferSensorState.transferRightSensorState.hasPixelBeenSeen) {
//                        RightClawPosition.Gripping
//                    } else {
//                        RightClawPosition.Retracted
//                    }
                }
                HandoffManager.ClawStateFromHandoff.Retracted -> RightClawPosition.Retracted
            }
        } else if (shouldClawsRetract) {
            RightClawPosition.Retracted
        } else {
            previousRobotState.depoState.rightClawPosition
        }
        hardware.rightClawServo.position = rightClawPosition.position

        clawsTimeMeasurer.endMeasureDT()

        // DRONE DRIVE -----------------------------------------------------------------------------
        driveTimeMeasurer.beginMeasureDT()
        val yInput = -gamepad1.left_stick_y.toDouble()
        val xInput = gamepad1.left_stick_x.toDouble()
        val rInput = gamepad1.right_stick_x.toDouble()

        // Strafe without turing for depositing
        val xSlowDowMultiplier = 1.0
        val driver2XInput = if (xInput == 0.0) {
            (gamepad2.left_trigger - gamepad2.right_trigger) * xSlowDowMultiplier
        } else {
            0.0
        }
        val ySlowDowMultiplier: Double = (2.0)/(3.0)
        val driver2YInput = if (yInput in -0.1..0.1) {
            gamepad2.left_stick_y.toDouble() * ySlowDowMultiplier
        } else {
            0.0
        }

        val isAtTheEndOfExtendo = hardware.extendoMotorMaster.currentPosition >= CollectorSystem.ExtendoPositions.Max.ticks || hardware.extendoMotorMaster.getCurrent(CurrentUnit.AMPS) > 6.0 // FIXME: Amp reading may be slow, actual limit switch will be better anyways
        val extendoCompensationPower = if (isAtTheEndOfExtendo && yInput == 0.0) {
            gamepad1.right_trigger.toDouble()
        } else {
            0.0
        }

        val y = yInput + extendoCompensationPower + driver2YInput
        val x = xInput + driver2XInput
        val r = -rInput * abs(rInput)
        movement.driveSetPower((y + x - r),
                (y - x + r),
                (y - x - r),
                (y + x + r))

        driveTimeMeasurer.endMeasureDT()

        // Plane Launcher --------------------------------------------------------------------------
        planeTimeMeasurer.beginMeasureDT()
        hardware.launcherServo.position = if ((gamepad2.y && !gamepad2.dpad_left) || gamepad1.y) {
            RobotTwoHardware.LauncherPosition.Released.position
        } else {
            RobotTwoHardware.LauncherPosition.Holding.position
        }
        planeTimeMeasurer.endMeasureDT()

        // Hang ------------------------------------------------------------------------------------
        hangTimeMeasurer.beginMeasureDT()
        hardware.hangReleaseServo.power = if (gamepad1.x || (gamepad2.left_stick_button && gamepad2.right_stick_button)) {
            RobotTwoHardware.HangPowers.Release.power
        } else {
            RobotTwoHardware.HangPowers.Holding.power
        }
        hangTimeMeasurer.endMeasureDT()

        // Light Control ---------------------------------------------------------------------------
        lightsTimeMeasurer.beginMeasureDT()
        val isAnyColorButtonPressed: Boolean = gamepad2.a || gamepad2.b || gamepad2.x || gamepad2.y

        val desiredPixelLightPattern: BothPixelsWeWant = if (gamepad2.dpad_left) {
            val newOne: PixelColor = when {
                gamepad2.a -> {
                    PixelColor.Green
                }
                gamepad2.b -> {
                    PixelColor.White
                }
                gamepad2.x -> {
                    PixelColor.Purple
                }
                gamepad2.y -> {
                    PixelColor.Yellow
                }
                else -> {
                    PixelColor.Unknown
                }
            }

//            val isLayerRisingEdge = gamepad2.dpad_left && !previousGamepad2State.dpad_left
            val isAnyColorButtonRisingEdge = isAnyColorButtonPressed && !previousIsAnyColorButtonPressed

            if (isAnyColorButtonRisingEdge) {
                numberOfTimesColorButtonPressed += 1

                when (numberOfTimesColorButtonPressed) {
                    1 -> {
                        previousDesiredPixelLightPattern.copy(leftPixel = newOne)
                    }
                    2 -> {
                        previousDesiredPixelLightPattern.copy(rightPixel = newOne)
                    }
                    else -> {
                        previousDesiredPixelLightPattern
                    }
                }
            } else {
                previousDesiredPixelLightPattern
            }
        } else {
            numberOfTimesColorButtonPressed = 0

            if (liftPosition.ticks <= Lift.LiftPositions.Min.ticks && previousRobotState.depoState.liftPosition.ticks >= Lift.LiftPositions.Min.ticks && bothClawsAreRetracted) {
                BothPixelsWeWant(PixelColor.Unknown, PixelColor.Unknown)
            } else {
                previousDesiredPixelLightPattern
            }
        }
        previousIsAnyColorButtonPressed = isAnyColorButtonPressed
        telemetry.addLine("desiredPixelLightPattern: $desiredPixelLightPattern")

//        val colorDetectedInLeftSide = collectorSystem.getColorInSide(hardware.leftTransferSensor, CollectorSystem.Side.Left)
//        val colorDetectedInRightSide = collectorSystem.getColorInSide(hardware.rightTransferSensor, CollectorSystem.Side.Right)
//        telemetry.addLine("colorDetectedInLeftSide: ${colorDetectedInLeftSide}")
//        telemetry.addLine("colorDetectedInRightSide: ${colorDetectedInRightSide}")


        // Light actuation -------------------------------------------------------------------------
        fun getLightPatternFromPixelColor(pixelWeWant: PixelColor): RevBlinkinLedDriver.BlinkinPattern {
            return when (pixelWeWant) {
                PixelColor.Unknown -> RevBlinkinLedDriver.BlinkinPattern.BLUE
                PixelColor.White -> RevBlinkinLedDriver.BlinkinPattern.WHITE
                PixelColor.Green -> RevBlinkinLedDriver.BlinkinPattern.GREEN
                PixelColor.Purple -> RevBlinkinLedDriver.BlinkinPattern.BLUE_VIOLET
                PixelColor.Yellow -> RevBlinkinLedDriver.BlinkinPattern.YELLOW
            }
        }

        val timeToDisplayColorMilis = 1000
        val timeSinceCurrentColorWasDisplayedMilis = System.currentTimeMillis() - timeWhenCurrentColorStartedBeingDisplayedMilis
        val isTimeToChangeColor = timeSinceCurrentColorWasDisplayedMilis >= timeToDisplayColorMilis
        val isCurrentColorObsolete = previousPixelToBeDisplayed !in desiredPixelLightPattern.toList()
        val currentPixelToBeDisplayed: PixelColor = when {
            isTimeToChangeColor || isCurrentColorObsolete -> {
                timeWhenCurrentColorStartedBeingDisplayedMilis = System.currentTimeMillis()
                desiredPixelLightPattern.toList().firstOrNull { color ->
                    color != previousPixelToBeDisplayed
                } ?: desiredPixelLightPattern.leftPixel
            }
             else -> {
                previousPixelToBeDisplayed
            }
        }
        previousPixelToBeDisplayed = currentPixelToBeDisplayed

        val timeOfSeeing = listOf(transferSensorState.transferRightSensorState.timeOfSeeingMilis, transferSensorState.transferLeftSensorState.timeOfSeeingMilis).maxOfOrNull { time -> time } ?: 0
        val timeSinceSeeing = System.currentTimeMillis() - timeOfSeeing
        val timeToShowPixelLights = 1500
        val doneWithThePixelCollectedLights = timeSinceSeeing >= timeToShowPixelLights
        //val isTransferCollected = transferSensorState.transferRightSensorState.hasPixelBeenSeen && transferSensorState.transferLeftSensorState.hasPixelBeenSeen
        telemetry.addLine("doneWithThePixelCollectedLights: $doneWithThePixelCollectedLights")
        telemetry.addLine("timeSinceSeeing: $timeSinceSeeing")
        telemetry.addLine("timeOfSeeing: $timeOfSeeing")

        val colorToDisplay = if (areBothPixelsIn && !doneWithThePixelCollectedLights) {
            gamepad1.runRumbleEffect(twoBeatRumble)
            RevBlinkinLedDriver.BlinkinPattern.LARSON_SCANNER_RED
        } else {
            getLightPatternFromPixelColor(currentPixelToBeDisplayed)
        }

        hardware.lights.setPattern(colorToDisplay)

        lightsTimeMeasurer.endMeasureDT()

        // FIXME: Some of these may add to loop latency, consider disabling for comp
        telemetryTimeMeasurer.beginMeasureDT()
        telemetry.addLine("Arm raw angle: ${arm.encoderReader.getRawPositionDegrees()}")
        telemetry.addLine("Arm actual angle: ${arm.getArmAngleDegrees()}")
        telemetry.addLine("Lift actual position: ${lift.getCurrentPositionTicks()}")
        telemetry.addLine("Extendo actual position: ${hardware.extendoMotorMaster.currentPosition}")
        telemetry.addLine("Left flap angle: ${collectorSystem.leftEncoderReader.getPositionDegrees()}")
        telemetry.addLine("Right flap angle: ${collectorSystem.rightEncoderReader.getPositionDegrees()}")

        telemetry.addLine("handoffTimeMeasurer:   ${handoffTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("extendoTimeMeasurer:   ${extendoTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("liftTimeMeasurer:      ${liftTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("armTimeMeasurer:       ${armTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("collectorTimeMeasurer: ${collectorTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("clawsTimeMeasurer:     ${clawsTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("driveTimeMeasurer:     ${driveTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("planeTimeMeasurer:     ${planeTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("hangTimeMeasurer:      ${hangTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("lightsTimeMeasurer:    ${lightsTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("telemetryTimeMeasurer: ${telemetryTimeMeasurer.getLastMeasuredDT()}ms")
        telemetry.addLine("miscTimeMeasurer:      ${miscTimeMeasurer.getLastMeasuredDT()}ms")

        // Update telemetry display at end of loop cycle
        telemetry.update()
        telemetryTimeMeasurer.endMeasureDT()

        /** not controls */

        // Previous robot state --------------------------------------------------------------------
        miscTimeMeasurer.beginMeasureDT()
        previousRobotState = RobotState(
                positionAndRotation = PositionAndRotation(),
                collectorSystemState = CollectorSystem.CollectorState(
                        collectorState = inputCollectorStateSystem,
                        extendoPosition = extendoState,
                        transferRollersState = autoRollerState,
                        transferLeftSensorState = transferSensorState.transferLeftSensorState,
                        transferRightSensorState = transferSensorState.transferRightSensorState
                ),
                depoState = RobotTwoAuto.DepoState(
                        liftPosition = liftPosition,
                        armPos = armPosition,
                        leftClawPosition = leftClawPosition,
                        rightClawPosition = rightClawPosition,
                ))
//                hardware.getActualState(RobotTwoAuto.ActualWorld(previousRobotState, 0), arm, odometryLocalizer, collectorSystem).actualRobot
//                .copy(depoState = RobotTwoAuto.DepoState(   armPos = armPosition,
//                                                            liftPosition = liftPosition,
//                                                            leftClawPosition = leftClawPosition,
//                                                            rightClawPosition = rightClawPosition))
        previousDesiredPixelLightPattern = desiredPixelLightPattern
        previousGamepadOneBumperMode = gamepadOneBumperMode
        previousGamepad1State.copy(gamepad1)
        previousGamepad2State.copy(gamepad2)
        wereBothPixelsInPreviously = areBothPixelsIn
    }

    enum class PixelColor {
        White,
        Green,
        Purple,
        Yellow,
        Unknown,
    }

    data class BothPixelsWeWant(val leftPixel: PixelColor, val rightPixel: PixelColor) {
        fun toList():List<PixelColor> {
            return listOf(leftPixel, rightPixel)
        }
        fun toPair():Pair<PixelColor, PixelColor> {
            return leftPixel to rightPixel
        }
    }
//    enum class PixelHandlerState {
//        None,
//        White,
//        Purple,
//        Green,
//        Yellow
//    }
//    data class TwoPixelHandlerState(val leftSide: PixelHandlerState, val rightSide: PixelHandlerState)
//    private val emptyPixelHandler = TwoPixelHandlerState(PixelHandlerState.None, PixelHandlerState.None)
//
//    private fun transferState(): TwoPixelHandlerState {
//        //TODO Put logic here and sensors on the robot
//        return TwoPixelHandlerState(PixelHandlerState.None, PixelHandlerState.None)
//    }
//    private fun clawState(): TwoPixelHandlerState {
//        //TODO Put logic here and sensors on the robot
//        return TwoPixelHandlerState(PixelHandlerState.None, PixelHandlerState.None)
//    }

}