package us.brainstormz.robotTwo.tests

import us.brainstormz.localizer.PhoHardware
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

class DepoTest {
    val telemetry = PhoHardware.PhoTelemetry()
    val lift = Lift(telemetry)
    val arm = Arm()
    val wrist = Wrist(left= Claw(Transfer.Side.Left), right = Claw(Transfer.Side.Right))
    val depoManager = DepoManager(lift= lift, arm= arm, wrist= wrist)

    data class FullyManageDepoTestInputs(val target: RobotTwoTeleOp.DriverInput, val previousDepoTarget: DepoTarget, val actualDepo: DepoManager.ActualDepo, val handoffIsReady: Boolean)

    data class AllDataForTest(val testName: String, val input: FullyManageDepoTestInputs, val expectedOutput: DepoTarget)

    fun testDepo(dataForTest: AllDataForTest): Pair<Boolean, DepoTarget> {
        val output = depoManager.fullyManageDepo(dataForTest.input.target, dataForTest.input.previousDepoTarget, dataForTest.input.actualDepo, dataForTest.input.handoffIsReady)
        val testPassed = output == dataForTest.expectedOutput
        return testPassed to output
    }



    val movingOutTests: (RobotTwoTeleOp.DepoInput)->List<DepoTest.AllDataForTest> = {input ->
        val inputLiftPosition = lift.getGetLiftTargetFromDepoTarget(input)
        listOf(
                DepoTest.AllDataForTest(
                        testName= "Testing whether the depo will wait for the claws to grip before moving",
                        input= DepoTest.FullyManageDepoTestInputs(
                                target = RobotTwoTeleOp.noInput.copy(
                                        depo= input
                                ),
                                previousDepoTarget = DepoTarget(
                                        liftPosition = Lift.LiftPositions.Down,
                                        armPosition = Arm.Positions.In,
                                        wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Retracted),
                                        targetType = DepoManager.DepoTargetType.GoingHome
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.In.angleDegrees,
                                        liftPositionTicks = Lift.LiftPositions.Down.ticks,
                                        isLiftLimitActivated = false
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget(
                                liftPosition = Lift.LiftPositions.Down,
                                armPosition = Arm.Positions.In,
                                wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Gripping),
                                targetType = DepoManager.DepoTargetType.GoingOut
                        )
                ),
                DepoTest.AllDataForTest(
                        testName= "Testing whether the depo will start moving once the claws are gripping",
                        input= DepoTest.FullyManageDepoTestInputs(
                                target = RobotTwoTeleOp.noInput.copy(
                                        depo= input
                                ),
                                previousDepoTarget = DepoTarget(
                                        liftPosition = Lift.LiftPositions.Down,
                                        armPosition = Arm.Positions.In,
                                        wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Gripping),
                                        targetType = DepoManager.DepoTargetType.GoingOut
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.In.angleDegrees,
                                        liftPositionTicks = Lift.LiftPositions.Down.ticks,
                                        isLiftLimitActivated = false
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget(
                                liftPosition = inputLiftPosition,
                                armPosition = Arm.Positions.ClearLiftMovement,
                                wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Gripping),
                                targetType = DepoManager.DepoTargetType.GoingOut
                        )
                ),
                DepoTest.AllDataForTest(
                        testName= "Testing whether the depo will reach the correct position once it's started moving",
                        input= DepoTest.FullyManageDepoTestInputs(
                                target = RobotTwoTeleOp.noInput.copy(
                                        depo= input
                                ),
                                previousDepoTarget = DepoTarget(
                                        liftPosition = inputLiftPosition,
                                        armPosition = Arm.Positions.ClearLiftMovement,
                                        wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Gripping),
                                        targetType = DepoManager.DepoTargetType.GoingOut
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees,
                                        liftPositionTicks = inputLiftPosition.ticks,
                                        isLiftLimitActivated = false
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget(
                                liftPosition = inputLiftPosition,
                                armPosition = Arm.Positions.Out,
                                wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Gripping),
                                targetType = DepoManager.DepoTargetType.GoingOut
                        )
                ),
                DepoTest.AllDataForTest(
                        testName= "Testing whether the depo will stay out once it's gotten there",
                        input= DepoTest.FullyManageDepoTestInputs(
                                target = RobotTwoTeleOp.noInput.copy(
                                        depo= input
                                ),
                                previousDepoTarget = DepoTarget(
                                        liftPosition = inputLiftPosition,
                                        armPosition = Arm.Positions.Out,
                                        wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Gripping),
                                        targetType = DepoManager.DepoTargetType.GoingOut
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                        liftPositionTicks = inputLiftPosition.ticks,
                                        isLiftLimitActivated = false
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget(
                                liftPosition = inputLiftPosition,
                                armPosition = Arm.Positions.Out,
                                wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Gripping),
                                targetType = DepoManager.DepoTargetType.GoingOut
                        )
                ),
        )
    }

    val movingInTests: List<AllDataForTest> = listOf(
            AllDataForTest(
                    testName= "Testing whether the depo will wait for the claws to release before moving",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(
                                    depo= RobotTwoTeleOp.DepoInput.Down
                            ),
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.SetLine3,
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Gripping),
                                    targetType = DepoManager.DepoTargetType.GoingOut
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    liftPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                    isLiftLimitActivated = false
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.SetLine3,
                            armPosition = Arm.Positions.Out,
                            wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the lift will start moving in while the arm moves in",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(
                                    depo= RobotTwoTeleOp.DepoInput.Down
                            ),
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.SetLine3,
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    liftPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                    isLiftLimitActivated = false
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.ClearForArmToMove,
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the lift will stop and wait for the arm",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(
                                    depo= RobotTwoTeleOp.DepoInput.Down
                            ),
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.ClearForArmToMove,
                                    armPosition = Arm.Positions.ClearLiftMovement,
                                    wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees - 10,
                                    liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks,
                                    isLiftLimitActivated = false
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.ClearForArmToMove,
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the lift will go after waiting for arm",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(
                                    depo= RobotTwoTeleOp.DepoInput.Down
                            ),
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.ClearForArmToMove,
                                    armPosition = Arm.Positions.ClearLiftMovement,
                                    wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees,
                                    liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks,
                                    isLiftLimitActivated = false
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.Down,
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristPositions(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
    )


//    val movingOutToSetline1WithOneFrameOfInput:(RobotTwoTeleOp.DepoInput) -> List<DepoTest.AllDataForTest> = {it ->
//        movingOutTests(it).mapIndexed {index, testdata -> testdata.copy(
//                testName = "One Frame Of Input " + testdata.testName,
//                input = testdata.input.copy(
//                        target = testdata.input.target.copy(
//                                depo = if (index == 0) {
//                                    testdata.input.target.depo
//                                } else {
//                                    RobotTwoTeleOp.DepoInput.NoInput
//                                }
//                        )
//                )
//            )
//        }
//    }
}

//Things to test
//moving to an out position from an in position
//moving to an in position from an out position
fun main() {

    val depoTest = DepoTest()
    val allMovingOutTests = listOf(RobotTwoTeleOp.DepoInput.SetLine1, RobotTwoTeleOp.DepoInput.SetLine2,RobotTwoTeleOp.DepoInput.SetLine3).fold(listOf<DepoTest.AllDataForTest>()) { acc, it -> acc + depoTest.movingOutTests(it) }
    val tests = allMovingOutTests + depoTest.movingInTests

    fun boolToPassFail(result: Boolean): String {
        return when (result) {
            true -> "passed"
            false -> "failed"
        }
    }

    val allTestsPassed = tests.fold(true) { acc, testData ->

        val result = depoTest.testDepo(testData)

        val passFail = boolToPassFail(result.first)
        println("Test \"${testData.testName}\" finished. It $passFail. Results:")
        println("\nInput: \n${testData.input}")
        println("\nExpected output: \n${testData.expectedOutput}")
        println("\nActual output: \n${result.second}")
        println("\nTest $passFail")

        acc && result.first
    }

    println("\n\nAll tests: ${boolToPassFail(allTestsPassed)}")
}

