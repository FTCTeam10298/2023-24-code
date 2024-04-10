package us.brainstormz.robotTwo.localTests

import com.qualcomm.robotcore.hardware.Gamepad
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.robotTwo.ActualRobot
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.CollectorManager
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.subsystems.ColorReading
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.Neopixels
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

//fun main() {
////    val armTests = listOf(
////            TeleopTest.emptyWorld,
////            TeleopTest.emptyWorld.copy(
////                    actualGamepad2= getChangedGamepad(TeleopTest.emptyWorld.actualGamepad2) { it.dpad_up = true }
////            ),
////            TeleopTest.emptyWorld.copy(
////                    actualRobot = TeleopTest.emptyWorld.actualRobot.copy(
////                        depoState = TeleopTest.emptyWorld.actualRobot.depoState.copy(
////                            liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks + 1
////                        )
////                    )
////            ),
////            TeleopTest.emptyWorld.copy(
////                    actualGamepad2= getChangedGamepad { it.dpad_down = true },
////                    actualRobot = TeleopTest.emptyWorld.actualRobot.copy(
////                            depoState = TeleopTest.emptyWorld.actualRobot.depoState.copy(
////                                    liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks + 1,
////                                    armAngleDegrees = Arm.Positions.Out.angleDegrees
////                            )
////                    )
////            ),
////            TeleopTest.emptyWorld.copy(
////                    actualRobot = TeleopTest.emptyWorld.actualRobot.copy(
////                            depoState = TeleopTest.emptyWorld.actualRobot.depoState.copy(
////                                    liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks + 1,
////                                    armAngleDegrees = Arm.Positions.GoodEnoughForLiftToGoDown.angleDegrees + 1
////                            )
////                    )
////            ),
////    )
////    val clawTests = listOf(
////            TeleopTest.emptyWorld,
////            TeleopTest.emptyWorld.copy(
////                    actualGamepad2= getChangedGamepad { it.left_bumper = true }
////            ),
//////            TeleopTest.emptyWorld,
//////            TeleopTest.emptyWorld.copy(
//////                    actualGamepad2= getChangedGamepad(TeleopTest.emptyWorld.actualGamepad2) { it.left_bumper = true }
//////            ),
////    )
//    val actualRobotThatWantsToWaitForTheArmBeforeMovingLiftDown =
//            TeleopTest.emptyWorld.actualRobot.copy(
//                    depoState = TeleopTest.emptyWorld.actualRobot.depoState.copy(
//                            liftPositionTicks = Lift.LiftPositions.ClearForArmToMove.ticks + 100,
//                            armAngleDegrees = Arm.Positions.GoodEnoughForLiftToGoDown.angleDegrees - 10
//                    )
//            )
//    val liftManualTest = listOf(
//            TeleopTest.emptyWorld,
//            TeleopTest.emptyWorld.copy(
//                    actualGamepad2= getChangedGamepad { it.right_stick_y = -1.0f },
//                    actualRobot = actualRobotThatWantsToWaitForTheArmBeforeMovingLiftDown
//            ),
//            TeleopTest.emptyWorld.copy(
//                    actualGamepad2= getChangedGamepad { it.right_stick_y = -1.0f },
//                    actualRobot = actualRobotThatWantsToWaitForTheArmBeforeMovingLiftDown
//            )
//    )
//    val expectedOutput = TeleopTest.emptyTarget.copy(
//            targetRobot = TeleopTest.emptyTarget.targetRobot.copy(
//                    depoTarget = TeleopTest.emptyTarget.targetRobot.depoTarget.copy(
//                            liftPosition = Lift.LiftPositions.Manual
//                    ),
//            )
//    )
//
//    val result = TeleopTest.runTest(liftManualTest)
//    val normalEquality = { output: TargetWorld ->
//        output.targetRobot.depoTarget.liftPosition == expectedOutput.targetRobot.depoTarget.liftPosition
//    }
//    val testPass = TeleopTest.didTestPass(result = result, normalEquality)
//
//    println(result)
//    println("Test passed? $testPass")
//}

fun main() {
    val teleopTester = TeleopTest()
//    val test = listOf(
//            teleopTester.emptyWorld.copy(actualRobot = teleopTester.emptyWorld.actualRobot.copy(depoState =
//            DepoManager.ActualDepo(
//                    armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees,
//                    liftPositionTicks = 357,
//                    isLiftLimitActivated = false,
//                    wristAngles = Wrist.ActualWrist(1.0, 1.0),
//            ),
//            )),
//            teleopTester.emptyWorld.copy(actualRobot = teleopTester.emptyWorld.actualRobot.copy(depoState =
//            DepoManager.ActualDepo(
//                    armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees,
//                    liftPositionTicks = 357,
//                    isLiftLimitActivated = false,
//                    wristAngles = Wrist.ActualWrist(1.0, 1.0),
//            ),
//            )),
//    )
//    val expectedOutput = RobotTwoTeleOp.initialPreviousTargetState.copy(targetRobot = RobotTwoTeleOp.initialPreviousTargetState.targetRobot.copy(depoTarget =
//    DepoTarget(
//                            liftPosition = Lift.LiftPositions.Down,
//                            armPosition = Arm.Positions.In,
//                            wristPosition = Wrist.WristTargets(both = Claw.ClawTarget.Retracted),
//                            targetType = DepoManager.DepoTargetType.GoingHome)))
    val gamepad = teleopTester.getChangedGamepad { it.dpad_down = true}
    println("gamepad: $gamepad")
    val test = listOf(
            TeleopTest.emptyWorld.copy(actualGamepad2 = gamepad),
            TeleopTest.emptyWorld.copy(actualGamepad2 = Gamepad()),
            TeleopTest.emptyWorld.copy(actualGamepad2 = Gamepad()),
    )
//    val expectedOutput = RobotTwoTeleOp.initialPreviousTargetState.copy(driverInput = RobotTwoTeleOp.initialPreviousTargetState.driverInput.)

    val output = teleopTester.runTest(test)
//    val testPassed: Boolean = expectedOutput == output.last().second
//    println("\n\n\ntest result: $testPassed")
//    println("\nexpectedOutput: \n$expectedOutput")
    println("\noutput: \n$output")
}

class TeleopTest {

//    data class FullyManageDepoTestInputs(val target: RobotTwoTeleOp.DriverInput, val previousDepoTarget: DepoTarget, val actualDepo: DepoManager.ActualDepo, val handoffIsReady: Boolean)
//
//    data class AllDataForTest(val testName: String, val input: FullyManageDepoTestInputs, val expectedOutput: DepoTarget)

    fun runTest(testSteps: List<ActualWorld>): List<Pair<ActualWorld, TargetWorld>> {
        val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
        val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
        val teleop = RobotTwoTeleOp(opmode.telemetry)

        teleop.init(hardware)

        val results:List<Pair<ActualWorld, TargetWorld>> = testSteps.mapIndexed() {index, actualWorld ->
            //Set Inputs
            hardware.actualRobot = actualWorld.actualRobot

            //Run Once
            teleop.loop(gamepad1 = actualWorld.actualGamepad1, gamepad2 = actualWorld.actualGamepad2, hardware)

            //Get result
            val result = teleop.functionalReactiveAutoRunner.previousTargetState!!
            actualWorld to result
        }

        return results
    }
    fun didTestPass(result: List<Pair<ActualWorld, TargetWorld>>, passCondition: (TargetWorld)->Boolean): Boolean {// List<Pair<Boolean, Pair<ActualWorld, TargetWorld>>> {
        return passCondition(result.last().second)
    }
    companion object {
        val emptySensorReading = ColorReading(
            red = 0f,
            green = 0f,
            blue = 0f,
            alpha = 0f,
        )
        val emptyWorld = ActualWorld(
                actualGamepad1 = Gamepad(),
                actualGamepad2 = Gamepad(),
                actualRobot = ActualRobot(
                        positionAndRotation = PositionAndRotation(),
                        depoState = DepoManager.ActualDepo(
                                armAngleDegrees = 0.0,
                                lift = Lift.ActualLift(
                                        currentPositionTicks = 0,
                                        limitSwitchIsActivated = false),
                                wristAngles = Wrist.ActualWrist(leftClawAngleDegrees = 0.0, rightClawAngleDegrees = 0.0),
                        ),
                        collectorSystemState = CollectorManager.ActualCollector(
                                extendo = SlideSubsystem.ActualSlideSubsystem(
                                        currentPositionTicks = 0,
                                        ticksMovedSinceReset = 0,
                                        limitSwitchIsActivated = false,
                                        zeroPositionOffsetTicks = 0,
                                        currentAmps = 0.0),
//                                leftRollerAngleDegrees = 0.0,
//                                rightRollerAngleDegrees = 0.0,
                                transferState = Transfer.ActualTransfer(
                                        left = emptySensorReading,
                                        right = emptySensorReading
                                ),
                        ),
                        neopixelState = Neopixels.HalfAndHalfTarget().compileStripState()
                ),
                timestampMilis = 0,
        )
    }
    fun getChangedGamepad(originalGamepad: Gamepad = Gamepad(), gamepadChange: (gamepad: Gamepad)->Unit): Gamepad {
        gamepadChange(originalGamepad)
        return originalGamepad
    }
}