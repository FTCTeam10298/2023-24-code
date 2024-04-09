package us.brainstormz.robotTwo.localTests

import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Drivetrain
import us.brainstormz.robotTwo.subsystems.Dropdown
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Intake
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist


fun main() {

    val telemetry = PrintlnTelemetry()
    val opmode = FauxOpMode(telemetry = telemetry)
    val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = telemetry)

    for (i in 0..1) {
        hardware.actuateRobot(
                targetState = RobotTwoTeleOp.initialPreviousTargetState.copy(
                        targetRobot = RobotTwoTeleOp.initialPreviousTargetState.targetRobot.copy(
                                collectorTarget = RobotTwoTeleOp.initialPreviousTargetState.targetRobot.collectorTarget.copy(
                                        extendo = SlideSubsystem.TargetSlideSubsystem(
                                                targetPosition = Extendo.ExtendoPositions.PurpleCenterPosition,
                                                power = 0.0,
                                                movementMode = DualMovementModeSubsystem.MovementMode.Position
                                        )
                                )
                        )
                ),
                previousTargetState = RobotTwoTeleOp.initialPreviousTargetState,
                actualState = TeleopTest.emptyWorld,
                previousActualWorld = TeleopTest.emptyWorld,
                drivetrain = Drivetrain(hardware, RRTwoWheelLocalizer(hardware, inchesPerTick = hardware.inchesPerTick), telemetry),
                wrist = Wrist(Claw(telemetry), Claw(telemetry), telemetry),
                arm = Arm(),
                lift = Lift(telemetry),
                extendo = Extendo(telemetry),
                intake = Intake(),
                dropdown = Dropdown(),
                transfer = Transfer(telemetry)
        )
    }
}