package us.brainstormz.robotTwo.tests

import us.brainstormz.faux.PrintlnTelemetry
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
    val wrist = Wrist(left= Claw(telemetry), right = Claw(telemetry), telemetry= telemetry)
    val depoManager = DepoManager(lift= lift, arm= arm, wrist= wrist, telemetry= telemetry)

    data class FullyManageDepoTestInputs(val target: RobotTwoTeleOp.DriverInput, val previousDepoTarget: DepoTarget, val actualDepo: DepoManager.ActualDepo, val handoffIsReady: Boolean)

    data class AllDataForTest(
            val testName: String,
            val input: FullyManageDepoTestInputs,
            val expectedOutput: DepoTarget)

    fun testDepo(dataForTest: AllDataForTest): Pair<Boolean, DepoTarget> {
        val output = depoManager.fullyManageDepo(dataForTest.input.target, dataForTest.input.previousDepoTarget, dataForTest.input.actualDepo)
        val testPassed = output == dataForTest.expectedOutput
        return testPassed to output
    }


    val movingOutTests: (RobotTwoTeleOp.DepoInput)->List<AllDataForTest> = {input ->
        val inputLiftPosition = lift.getGetLiftTargetFromDepoTarget(input, 0.0)
        listOf(
                DepoTest.AllDataForTest(
                        testName= "Testing whether the depo will wait for the claws to grip before moving",
                        input= DepoTest.FullyManageDepoTestInputs(
                                target = RobotTwoTeleOp.noInput.copy(
                                        depo= input
                                ),
                                previousDepoTarget = DepoTarget( 
                                        lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                        armPosition = Arm.Positions.In,
                                        wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                        targetType = DepoManager.DepoTargetType.GoingHome
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.In.angleDegrees,
                                        lift = Lift.ActualLift(
                                                currentPositionTicks = Lift.LiftPositions.Down.ticks,
                                                limitSwitchIsActivated = false),
                                        wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget( 
                                lift = Lift.TargetLift(Lift.LiftPositions.Down),
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
                                        lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                        armPosition = Arm.Positions.In,
                                        wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                        targetType = DepoManager.DepoTargetType.GoingOut
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.In.angleDegrees,
                                        lift = Lift.ActualLift(
                                            currentPositionTicks =  Lift.LiftPositions.Down.ticks,
                                            limitSwitchIsActivated =  false),
                                        wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget( 
                                lift = Lift.TargetLift(inputLiftPosition),
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
                                        lift = Lift.TargetLift(inputLiftPosition),
                                        armPosition = Arm.Positions.ClearLiftMovement,
                                        wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                        targetType = DepoManager.DepoTargetType.GoingOut
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees,
                                        lift = Lift.ActualLift(
                                            currentPositionTicks =  inputLiftPosition.ticks,
                                            limitSwitchIsActivated =  false),
                                        wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget( 
                                lift = Lift.TargetLift(inputLiftPosition),
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
                                        lift = Lift.TargetLift(inputLiftPosition),
                                        armPosition = Arm.Positions.Out,
                                        wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                        targetType = DepoManager.DepoTargetType.GoingOut
                                ),
                                actualDepo = DepoManager.ActualDepo(
                                        armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                        lift = Lift.ActualLift(
                                            currentPositionTicks =  inputLiftPosition.ticks,
                                            limitSwitchIsActivated =  false),
                                        wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                                ),
                                handoffIsReady = false
                        ),
                        expectedOutput= DepoTarget( 
                                lift = Lift.TargetLift(inputLiftPosition),
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
                                    lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                    targetType = DepoManager.DepoTargetType.GoingOut
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
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
                                    lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
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
                                    lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                    targetType = DepoManager.DepoTargetType.GoingOut
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
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
                                    lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
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
                                    lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.SetLine3.ticks,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.ClearForArmToMove),
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
                                    lift = Lift.TargetLift(Lift.LiftPositions.ClearForArmToMove),
                                    armPosition = Arm.Positions.ClearLiftMovement,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees - 10,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.ClearForArmToMove),
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
                                    lift = Lift.TargetLift(Lift.LiftPositions.ClearForArmToMove),
                                    armPosition = Arm.Positions.ClearLiftMovement,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
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
                                    lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                    armPosition = Arm.Positions.In,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.Down.ticks,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
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
                                    lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                    armPosition = Arm.Positions.In,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees + 1,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.Down.ticks + 1,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
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
                                    lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                    armPosition = Arm.Positions.In,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees + 5.0 + 1,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.Down.ticks + 100 + 1,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.ClearForArmToMove),
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the depo will close the claws before going down",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(depo = RobotTwoTeleOp.DepoInput.Down),
                            previousDepoTarget = DepoTarget( 
                                    lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                    targetType = DepoManager.DepoTargetType.GoingOut
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees-5,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.SetLine3.ticks+110,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.SetLine3),
                            armPosition = Arm.Positions.Out,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),

    )


    val noInputOnStartTest: List<AllDataForTest> = listOf(
            AllDataForTest(
                    testName= "Testing whether the depo will stay put when the first loop starts and everything is down and there's no inputs",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput,
                            previousDepoTarget = RobotTwoTeleOp.initDepoTarget,
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.Down.ticks,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
                            armPosition = Arm.Positions.In,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the depo will stay put when the first loop starts and everything is a little off and there's no inputs",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput,
                            previousDepoTarget = RobotTwoTeleOp.initDepoTarget,
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees + 6,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.Down.ticks + 110,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the depo will stay put when the first loop starts and everything is a little off and limit switch is activated and there's no inputs",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput,
                            previousDepoTarget = RobotTwoTeleOp.initDepoTarget,
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees + 6,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.Down.ticks + 110,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the depo will stay put when the arm is a little off and there's no inputs",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput,
                            previousDepoTarget = DepoTarget( 
                                    lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                    armPosition = Arm.Positions.ClearLiftMovement,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees + 8,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.Down.ticks + 110,
                                            limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping)),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
    )

    val specificTest: List<AllDataForTest> = listOf(
//            AllDataForTest(
//                    testName= "Testing whether the depo will do nothing with no inputs and in the in position",
//                    input= FullyManageDepoTestInputs(
//                            target = RobotTwoTeleOp.noInput,
//                            previousDepoTarget = DepoTarget( depoScoringHeightAdjust= 0.0,
//                                    lift = Lift.TargetLift(Lift.LiftPositions.ClearForArmToMove),
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
//                    expectedOutput= DepoTarget( depoScoringHeightAdjust= 0.0,
//                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
//                            armPosition = Arm.Positions.In,
//                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
//                            targetType = DepoManager.DepoTargetType.GoingHome
//                    )
//            ),


            //find
            //(actualDepo = DepoManager.ActualDepo\(\n                                    armAngleDegrees = .*,\n                                    )(liftPositionTicks = .*,\n                                    isLiftLimitActivated = .*,)(\n                                    wristAngles = .*,\n                            \),)

            //Replace
            //$1lift = Lift.ActualLift(\n                                            currentPositionTicks = Lift.LiftPositions.SetLine2.ticks,\n                                            limitSwitchIsActivated = false),$3
            AllDataForTest(
                    testName= "Testing whether the depo will do nothing with depo input and in the in position",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(depo = RobotTwoTeleOp.DepoInput.Down),
                            previousDepoTarget = DepoTarget( 
                                    lift = Lift.TargetLift(Lift.LiftPositions.ClearForArmToMove),
                                    armPosition = Arm.Positions.ClearLiftMovement,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees,
                                    lift = Lift.ActualLift(
                                            currentPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks,
                                            limitSwitchIsActivated = false),
                                    wristAngles = Wrist.ActualWrist(1.0, 1.0),
                            ),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
        )


    val clawInputsTests: List<AllDataForTest> = listOf(
            AllDataForTest(
                    testName= "Testing whether the claws will drop when the depo is out",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(depo = RobotTwoTeleOp.DepoInput.NoInput, wrist = RobotTwoTeleOp.WristInput(RobotTwoTeleOp.ClawInput.Drop, RobotTwoTeleOp.ClawInput.Drop)),
                            previousDepoTarget = DepoTarget( 
                                    lift = Lift.TargetLift(Lift.LiftPositions.SetLine2),
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                                    targetType = DepoManager.DepoTargetType.GoingOut
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    lift = Lift.ActualLift(currentPositionTicks = Lift.LiftPositions.SetLine2.ticks, limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Gripping))),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.SetLine2),
                            armPosition = Arm.Positions.Out,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingOut
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the claws will open when the depo is moving in",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(depo = RobotTwoTeleOp.DepoInput.Down, wrist = RobotTwoTeleOp.WristInput(RobotTwoTeleOp.ClawInput.Hold, RobotTwoTeleOp.ClawInput.Hold)),
                            previousDepoTarget = DepoTarget( 
                                    lift = Lift.TargetLift(Lift.LiftPositions.SetLine2),
                                    armPosition = Arm.Positions.Out,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingOut
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.Out.angleDegrees,
                                    lift = Lift.ActualLift(currentPositionTicks = Lift.LiftPositions.SetLine2.ticks, limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted))),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.ClearForArmToMove),
                            armPosition = Arm.Positions.ClearLiftMovement,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the claws will open when the depo is down after being up",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(wrist = RobotTwoTeleOp.WristInput(RobotTwoTeleOp.ClawInput.Hold, RobotTwoTeleOp.ClawInput.Hold)),
                            previousDepoTarget = DepoTarget( 
                                    lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                    armPosition = Arm.Positions.In,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees,
                                    lift = Lift.ActualLift(currentPositionTicks = Lift.LiftPositions.Down.ticks,  zeroPositionOffsetTicks = 0, limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted))),
                            handoffIsReady = false
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
                            armPosition = Arm.Positions.In,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
            AllDataForTest(
                    testName= "Testing whether the claws will open when the depo is down and handoff is ready",
                    input= FullyManageDepoTestInputs(
                            target = RobotTwoTeleOp.noInput.copy(wrist = RobotTwoTeleOp.WristInput(RobotTwoTeleOp.ClawInput.Hold, RobotTwoTeleOp.ClawInput.Hold)),
                            previousDepoTarget = DepoTarget( 
                                    lift = Lift.TargetLift(Lift.LiftPositions.Down),
                                    armPosition = Arm.Positions.In,
                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
                                    targetType = DepoManager.DepoTargetType.GoingHome
                            ),
                            actualDepo = DepoManager.ActualDepo(
                                    armAngleDegrees = Arm.Positions.In.angleDegrees,
                                    lift = Lift.ActualLift(currentPositionTicks = Lift.LiftPositions.Down.ticks,  zeroPositionOffsetTicks = 0, limitSwitchIsActivated = false),
                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted))),
                            handoffIsReady = true
                    ),
                    expectedOutput= DepoTarget( 
                            lift = Lift.TargetLift(Lift.LiftPositions.Down),
                            armPosition = Arm.Positions.In,
                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Gripping),
                            targetType = DepoManager.DepoTargetType.GoingHome
                    )
            ),
    )
//    val liftResetTest: List<AllDataForTest> = listOf(
//            AllDataForTest(
//                    testName= "Testing whether the depo will reset lift",
//                    input= FullyManageDepoTestInputs(
//                            target = RobotTwoTeleOp.noInput,
//                            previousDepoTarget = DepoTarget( 
//                                    lift = Lift.TargetLift(Lift.LiftPositions.Down),
//                                    armPosition = Arm.Positions.In,
//                                    wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
//                                    targetType = DepoManager.DepoTargetType.GoingHome
//                            ),
//                            actualDepo = DepoManager.ActualDepo(
//                                    armAngleDegrees = Arm.Positions.In.angleDegrees,
//                                    lift = Lift.ActualLift(
//                                            currentPositionTicks = Lift.LiftPositions.Down.ticks,
//                                            limitSwitchIsActivated = true),
//                                    wristAngles = wrist.getActualWristFromWristTargets(Wrist.WristTargets(Claw.ClawTarget.Retracted)),
//                            ),
//                            handoffIsReady = false
//                    ),
//                    expectedOutput= DepoTarget( 
//                            lift = Lift.TargetLift(Lift.LiftPositions.),
//                            armPosition = Arm.Positions.In,
//                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
//                            targetType = DepoManager.DepoTargetType.GoingHome
//                    )
//            ),
//    )

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
    val tests = /*depoTest.liftResetTest +*/ depoTest.noInputOnStartTest + depoTest.clawInputsTests + depoTest.specificTest + allMovingOutTests + depoTest.movingInTests + depoTest.staticTests + depoTest.specificTest

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

