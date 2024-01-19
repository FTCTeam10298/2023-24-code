package us.brainstormz.robotTwo

import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.OpMode
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
//        TargetWorld(
//                targetRobot = RobotState(
//
//                ),
//                isTargetReached = {previousTargetState: TargetWorld?, actualState: ActualWorld ->
//
//                })
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
    private val autoListIterator = autoStateList.listIterator()
    private fun nextTargetState(
            previousTargetState: TargetWorld?,
            actualState: ActualWorld,
            previousActualState: ActualWorld?): TargetWorld {
        return when {
            previousTargetState == null -> {
                autoListIterator.next()
            }
            previousTargetState.isTargetReached(previousTargetState, actualState) -> {
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
        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"), nextMenu = "startingPos", firstMenu = true)
        wizard.newMenu("startingPos", "What side of the truss are we on?", listOf("Audience", "Backboard"))
        wizard.summonWizard(gamepad1)
        return WizardResults(
                alliance = when (wizard.wasItemChosen("alliance", "Red")) {
                    true -> RobotTwoHardware.Alliance.Red
                    false -> RobotTwoHardware.Alliance.Blue
                },
                startPosition = when (wizard.wasItemChosen("startingPos", "Audience")) {
                    true -> StartPosition.Audience
                    false -> StartPosition.Backboard
                }
        )
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
                armServo2= hardware.armServo2)

        val wizardResults = runMenuWizard()
        alliance = wizardResults.alliance
        startPosition = wizardResults.startPosition

        autoStateList = calcAutoTargetStateList(alliance, startPosition)
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
                mecanumMovement.moveTowardTarget(targetState.targetRobot.positionAndRotation)
            }
        )
    }
}