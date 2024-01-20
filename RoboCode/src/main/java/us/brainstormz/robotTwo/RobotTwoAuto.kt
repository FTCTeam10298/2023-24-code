package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.motion.MecanumMovement
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.robotTwo.RobotTwoHardware.UnchangingRobotAttributes.alliance
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard
import us.brainstormz.robotTwo.RobotTwoHardware.RobotState

@Autonomous
class RobotTwoAuto: OpMode() {

    private val backBoardAuto: List<TargetWorld> = listOf(
            TargetWorld(
                    targetRobot = RobotState(
                            collectorState = Collector.CollectorState(Collector.CollectorPowers.Off, RobotTwoHardware.ExtendoPositions.Min, Collector.TransferState(Collector.CollectorPowers.Off, Collector.CollectorPowers.Off, Collector.DirectorState.Off), Collector.TransferHalfState(false, 0), Collector.TransferHalfState(false, 0)),
                            positionAndRotation = PositionAndRotation(y= -12.0, r= -90.0),
                            depoState = DepoState(Arm.Positions.In, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld?, actualState: ActualWorld ->
                        val isRobotAtPosition = mecanumMovement.isRobotAtPosition(currentPosition = actualState.actualRobot.positionAndRotation, targetPosition = targetState?.targetRobot?.positionAndRotation ?: PositionAndRotation())
                        telemetry.addLine("isRobotAtPosition: $isRobotAtPosition")
                        isRobotAtPosition
                    }),
            TargetWorld(
                    targetRobot = RobotState(
                            collectorState = Collector.CollectorState(Collector.CollectorPowers.Off, RobotTwoHardware.ExtendoPositions.Min, Collector.TransferState(Collector.CollectorPowers.Off, Collector.CollectorPowers.Off, Collector.DirectorState.Off), Collector.TransferHalfState(false, 0), Collector.TransferHalfState(false, 0)),
                            positionAndRotation = PositionAndRotation(),
                            depoState = DepoState(Arm.Positions.In, Lift.LiftPositions.Min, RobotTwoHardware.LeftClawPosition.Retracted, RobotTwoHardware.RightClawPosition.Retracted)
                    ),
                    isTargetReached = {targetState: TargetWorld?, actualState: ActualWorld ->
                        val isRobotAtPosition = mecanumMovement.isRobotAtPosition(currentPosition = actualState.actualRobot.positionAndRotation, targetPosition = targetState?.targetRobot?.positionAndRotation ?: PositionAndRotation())
                        telemetry.addLine("isRobotAtPosition: $isRobotAtPosition")
                        isRobotAtPosition
                    })
    )

    private val audienceAuto: List<TargetWorld> = listOf(

    )
    
    private fun calcAutoTargetStateList(
            alliance: RobotTwoHardware.Alliance,
            startPosition: StartPosition,
    ): List<TargetWorld> {

        val startPosAccounted = when (startPosition) {
            StartPosition.Backboard -> backBoardAuto
            StartPosition.Audience -> audienceAuto
        }

        val allianceAccounted = when (alliance) {
            RobotTwoHardware.Alliance.Red -> startPosAccounted
            RobotTwoHardware.Alliance.Blue -> flipRedAutoToBlue(startPosAccounted)
        }


        return allianceAccounted
    }

    private fun flipRedAutoToBlue(auto: List<TargetWorld>): List<TargetWorld> {
        TODO()
    }

    private lateinit var autoStateList: List<TargetWorld>
    private lateinit var autoListIterator: ListIterator<TargetWorld>
    private fun nextTargetState(
            previousTargetState: TargetWorld?,
            actualState: ActualWorld,
            previousActualState: ActualWorld?): TargetWorld {
        return when {
            previousTargetState == null -> {
                autoListIterator.next()
            }
            previousTargetState.isTargetReached(previousTargetState, actualState) && autoListIterator.hasNext()-> {
                autoListIterator.next()
            }
            else -> {
                previousTargetState
            }
        }
    }

    data class DepoState(
            val armPos: Arm.Positions,
            val liftPosition: Lift.LiftPositions,
            val leftClawPosition: RobotTwoHardware.LeftClawPosition,
            val rightClawPosition: RobotTwoHardware.RightClawPosition,
    )

    open class TargetWorld(
            val targetRobot: RobotTwoHardware.RobotState,
            val isTargetReached: (previousTargetState: TargetWorld?, actualState: ActualWorld) -> Boolean)
    class ActualWorld(val actualRobot: RobotState,
                      val timestampMilis: Long)

    enum class StartPosition {
        Backboard,
        Audience
    }

    private val console = TelemetryConsole(telemetry)
    private val wizard = TelemetryWizard(console, null)
    data class WizardResults(val alliance: RobotTwoHardware.Alliance, val startPosition: StartPosition)
    private fun runMenuWizard(): WizardResults {
        val isWizardDone = wizard.summonWizard(gamepad1)
        return if (isWizardDone) {
            WizardResults(
                    alliance = when (wizard.wasItemChosen("alliance", "Red")) {
                        true -> RobotTwoHardware.Alliance.Red
                        false -> RobotTwoHardware.Alliance.Blue
                    },
                    startPosition = when (wizard.wasItemChosen("startingPos", "Audience")) {
                        true -> StartPosition.Audience
                        false -> StartPosition.Backboard
                    }
            )
        } else {
            wizardResults
        }
    }

    private val hardware = RobotTwoHardware(telemetry, this)

    private lateinit var mecanumMovement: MecanumMovement

    private lateinit var collector: Collector

    private lateinit var arm: Arm

    private lateinit var startPosition: StartPosition

    override fun init() {
        hardware.init(hardwareMap)

        val odometryLocalizer = RRTwoWheelLocalizer(hardware= hardware, inchesPerTick= hardware.inchesPerTick)
        mecanumMovement = MecanumMovement(odometryLocalizer, hardware, telemetry)

        collector = Collector(  extendoMotorMaster= hardware.extendoMotorMaster,
                extendoMotorSlave= hardware.extendoMotorSlave,
                collectorServo1 = hardware.collectorServo1,
                collectorServo2 = hardware.collectorServo2,
                rightTransferServo=hardware. rightTransferServo,
                leftTransferServo= hardware.leftTransferServo,
                transferDirectorServo= hardware.transferDirectorServo,
                leftTransferPixelSensor= hardware.leftTransferSensor,
                rightTransferPixelSensor= hardware.rightTransferSensor,
                leftRollerEncoder= hardware.leftRollerEncoder,
                rightRollerEncoder= hardware.rightRollerEncoder,
                telemetry= telemetry)


        arm = Arm(  encoder= hardware.armEncoder,
                armServo1= hardware.armServo1,
                armServo2= hardware.armServo2, telemetry)


        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"), nextMenu = "startingPos", firstMenu = true)
        wizard.newMenu("startingPos", "What side of the truss are we on?", listOf("Audience", "Backboard"))
    }

    private var wizardResults = WizardResults(RobotTwoHardware.Alliance.Red, StartPosition.Backboard)
    override fun init_loop() {
        wizardResults = runMenuWizard()
    }

    override fun start() {
        alliance = wizardResults.alliance
        startPosition = wizardResults.startPosition

        val startPositionAndRotation: PositionAndRotation = when (alliance) {
            RobotTwoHardware.Alliance.Red -> {
                when (startPosition) {
                    StartPosition.Backboard -> PositionAndRotation(x = -(72.0 - ((hardware.robotLengthInches/2) + hardware.tabCutoffCompensationInches)), y= -12.0, r= -90.0)
                    StartPosition.Audience -> TODO()
                }
            }
            RobotTwoHardware.Alliance.Blue -> {
                TODO()
            }
        }
        mecanumMovement.localizer.setPositionAndRotation(startPositionAndRotation)

        autoStateList = calcAutoTargetStateList(alliance, startPosition)
        autoListIterator = autoStateList.listIterator()
    }

    private val functionalReactiveAutoRunner = FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>()
    override fun loop() {
        functionalReactiveAutoRunner.loop(
            actualStateGetter = { previousActualState ->
                hardware.getActualState(previousActualState, arm, mecanumMovement.localizer, collector)
            },
            targetStateFetcher = { previousTargetState, actualState, previousActualState ->
                nextTargetState(previousTargetState, actualState, previousActualState)
            },
            stateFulfiller = { targetState, actualState ->
                telemetry.addLine("target position: ${targetState.targetRobot.positionAndRotation}")
                telemetry.addLine("current position: ${mecanumMovement.localizer.currentPositionAndRotation()}")
                mecanumMovement.moveTowardTarget(targetState.targetRobot.positionAndRotation)
            }
        )
        telemetry.update()
    }
}