import org.junit.Assert
import org.junit.Test
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Wrist

class DepoCoordinatorTest {

    fun createDepoManager(): DepoManager {
        val telemetry = PrintlnTelemetry()
        val arm = Arm()
        val wrist = Wrist(Claw(telemetry), Claw(telemetry), telemetry)
        val depoManager = DepoManager(
                arm = arm,
                lift = Lift(telemetry),
                wrist = wrist,
                telemetry = telemetry
        )
        return depoManager
    }

    @Test
    fun `when claws are gripping and depo is in handoff position, nothing moves`() {
        // given
        val testSubject = createDepoManager()

        val finalDepoTarget = DepoTarget(
                armPosition = Arm.ArmTarget(Arm.Positions.In),
                lift = Lift.TargetLift(Lift.LiftPositions.Down),
                wristPosition = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                targetType = DepoManager.DepoTargetType.GoingHome
        )
        val previousTargetDepo = finalDepoTarget
        val actualDepo = DepoManager.ActualDepo(
                armAngleDegrees = Arm.Positions.In.angleDegrees,
                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.Down.ticks, true, 0 ,0  ,0.0),
                wristAngles = Wrist.ActualWrist(leftClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees, rightClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees)
        )

        // when
        val actualOutput = testSubject.coordinateArmLiftAndClaws(
                finalDepoTarget = finalDepoTarget,
                previousTargetDepo = previousTargetDepo,
                actualDepo = actualDepo
        )

        // then
        val expectedOutput = DepoTarget(
                armPosition = Arm.ArmTarget(Arm.Positions.In),
                lift = Lift.TargetLift(Lift.LiftPositions.Down),
                wristPosition = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                targetType = DepoManager.DepoTargetType.GoingHome
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }

    @Test
    fun `when claws are open but told to close and depo is in handoff position and told to stay, depo starts going out to close claws`() {
        // given
        val testSubject = createDepoManager()

        val finalDepoTarget = DepoTarget(
                armPosition = Arm.ArmTarget(Arm.Positions.In),
                lift = Lift.TargetLift(Lift.LiftPositions.Down),
                wristPosition = Wrist.WristTargets(Claw.ClawTarget.Retracted),
                targetType = DepoManager.DepoTargetType.GoingHome
        )
        val previousTargetDepo = finalDepoTarget
        val actualDepo = DepoManager.ActualDepo(
                armAngleDegrees = Arm.Positions.In.angleDegrees,
                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.Down.ticks, true, 0 ,0  ,0.0),
                wristAngles = Wrist.ActualWrist(leftClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees, rightClawAngleDegrees = Claw.ClawTarget.Gripping.angleDegrees)
        )

        // when
        val actualOutput = testSubject.coordinateArmLiftAndClaws(
                finalDepoTarget = finalDepoTarget,
                previousTargetDepo = previousTargetDepo,
                actualDepo = actualDepo
        )

        // then
        val expectedOutput = DepoTarget(
                armPosition = Arm.ArmTarget(Arm.Positions.ClearLiftMovement),
                lift = Lift.TargetLift(Lift.LiftPositions.Down),
                wristPosition = Wrist.WristTargets(Claw.ClawTarget.Gripping),
                targetType = DepoManager.DepoTargetType.GoingHome
        )
        Assert.assertEquals(expectedOutput, actualOutput)
    }
}