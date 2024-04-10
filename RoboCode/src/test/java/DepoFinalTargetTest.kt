import org.junit.Assert
import org.junit.Test
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Wrist

class DepoFinalTargetTest {

    @Test
    fun `when depo is handing off, nothing moves`() {
        // given
        val testSubject = createDepoManager()

        val depoInput = RobotTwoTeleOp.DepoInput.Down
        val depoScoringHeightAdjust = 0.0
        val previousWristTarget = Wrist.WristTargets(Claw.ClawTarget.Gripping)
        val previousDepoTargetType = DepoManager.DepoTargetType.GoingHome

        // when
        val actualOutput = testSubject.getFinalDepoTarget(
                depoInput = depoInput,
                depoScoringHeightAdjust = depoScoringHeightAdjust,
                previousWristTarget = previousWristTarget,
                previousDepoTargetType = previousDepoTargetType
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
}