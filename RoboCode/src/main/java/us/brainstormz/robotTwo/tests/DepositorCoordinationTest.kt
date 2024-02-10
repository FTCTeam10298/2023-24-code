package us.brainstormz.robotTwo.tests

import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.localizer.PhoHardware
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Wrist

class DepoTest {
    val telemetry = PrintlnTelemetry()
    val lift = Lift(telemetry)
    val arm = Arm()
    val wrist = Wrist(left= Claw(), right = Claw(), telemetry= telemetry)
    val depoManager = DepoManager(lift= lift, arm= arm, wrist= wrist, telemetry= telemetry)

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
                                        wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                        targetType = DepoManager.DepoTargetType.GoingHome
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.In.angleDegrees,
                                        liftPositionTicks = Lift.LiftPositions.Down.ticks,
                                        wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                                        isLiftLimitActivated = false
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget(
                                liftPosition = Lift.LiftPositions.Down,
                                armPosition = Arm.Positions.In,
                                wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
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
                                        wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                        targetType = DepoManager.DepoTargetType.GoingOut
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.In.angleDegrees,
                                        liftPositionTicks = Lift.LiftPositions.Down.ticks,
                                        isLiftLimitActivated = false,
                                        wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget(
                                liftPosition = inputLiftPosition,
                                armPosition = Arm.Positions.ClearLiftMovement,
                                wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
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
                                        wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                        targetType = DepoManager.DepoTargetType.GoingOut
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees,
                                        liftPositionTicks = inputLiftPosition.ticks,
                                        isLiftLimitActivated = false,
                                        wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget(
                                liftPosition = inputLiftPosition,
                                armPosition = Arm.Positions.Out,
                                wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
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
                                        wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                        targetType = DepoManager.DepoTargetType.GoingOut
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                        liftPositionTicks = inputLiftPosition.ticks,
                                        isLiftLimitActivated = false,
                                        wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget(
                                liftPosition = inputLiftPosition,
                                armPosition = Arm.Positions.Out,
                                wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
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
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                    targetType = DepoManager.DepoTargetType.GoingOut
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    liftPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                    isLiftLimitActivated = false,
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.SetLine3,
                            armPosition = Arm.Positions.Out,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the depo will wait to move until the claws have retracted",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(
                                    depo= RobotTwoTeleOp.DepoInput.Down
                            ),
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.SetLine3,
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    liftPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                    isLiftLimitActivated = false,
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.SetLine3,
                            armPosition = Arm.Positions.Out,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the depo will wait to retract the claws until the arm is at the right place",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(
                                    depo= RobotTwoTeleOp.DepoInput.Down
                            ),
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.SetLine3,
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                    targetType = DepoManager.DepoTargetType.GoingOut
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    liftPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                    armAngleDegrees = Arm.Positions.In.angleDegrees,
                                    isLiftLimitActivated = false,
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.SetLine3,
                            armPosition = Arm.Positions.Out,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the depo will retract the claws, after it has moved out for the purpose of retracting them",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(
                                    depo= RobotTwoTeleOp.DepoInput.Down
                            ),
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.SetLine3,
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    liftPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    isLiftLimitActivated = false,
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.SetLine3,
                            armPosition = Arm.Positions.Out,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
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
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    liftPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                    isLiftLimitActivated = false,
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.ClearForArmToMove,
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
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
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees - 10,
                                    liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks,
                                    isLiftLimitActivated = false,
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.ClearForArmToMove,
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
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
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees,
                                    liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks,
                                    isLiftLimitActivated = false,
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.Down,
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
    )

    val staticTests: List<AllDataForTest> = listOf(
            AllDataForTest(
                    testName= "Testing whether the depo will do nothing with no inputs and in the in position",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput,
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.Down,
                                    armPosition = Arm.Positions.In,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees,
                                    liftPositionTicks = Lift.LiftPositions.Down.ticks,
                                    isLiftLimitActivated = false,
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.Down,
                            armPosition = Arm.Positions.In,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the depo will do nothing with no inputs and one tick off from the in position",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput,
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.Down,
                                    armPosition = Arm.Positions.In,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees + 1,
                                    liftPositionTicks = Lift.LiftPositions.Down.ticks + 1,
                                    isLiftLimitActivated = false,
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.Down,
                            armPosition = Arm.Positions.In,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the depo will do nothing with no inputs and one tick beyond the allowed position error",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput,
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.Down,
                                    armPosition = Arm.Positions.In,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees + 5.0+1,
                                    liftPositionTicks = Lift.LiftPositions.Down.ticks + 100+ 1,
                                    isLiftLimitActivated = false,
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.Down,
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),

    )

    val specificTest: List<AllDataForTest> = listOf(
//            AllDataForTest(
//                    testName= "Testing whether the depo will do nothing with no inputs and in the in position",
//                    input= FullyManageDepoTestInputs(
//                            target = RobotTwoTeleOp.noInput,
//                            previousDepoTarget = DepoTarget(
//                                    liftPosition = Lift.LiftPositions.ClearForArmToMove,
//                                    armPosition = Arm.Positions.ClearLiftMovement,
//                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
//                                    targetType = DepoManager.DepoTargetType.GoingHome
//                            ),
//                            actualDepo = DepoManager.ActualDepo(
//                                    armAngleDegrees = 257.12,
//                                    liftPositionTicks = 357,
//                                    isLiftLimitActivated = false,
//                                    wristAngles = Wrist.ActualWrist(1.0, 1.0),
//                            ),
//                            handoffIsReady = false
//                    ),
//                    expectedOutput= DepoTarget(
//                            liftPosition = Lift.LiftPositions.Down,
//                            armPosition = Arm.Positions.In,
//                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
//                            targetType = DepoManager.DepoTargetType.GoingHome
//                    )
//            ),
            AllDataForTest(
                    testName= "Testing whether the depo will do nothing with depo input and in the in position",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(depo = RobotTwoTeleOp.DepoInput.Down),
                            previousDepoTarget = DepoTarget(
                                    liftPosition = Lift.LiftPositions.ClearForArmToMove,
                                    armPosition = Arm.Positions.ClearLiftMovement,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees,
                                    liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks,
                                    isLiftLimitActivated = false,
                                    wristAngles = Wrist.ActualWrist(1.0, 1.0),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget(
                            liftPosition = Lift.LiftPositions.Down,
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
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
    val tests = depoTest.specificTest//allMovingOutTests + depoTest.movingInTests + depoTest.staticTests + depoTest.specificTest

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

