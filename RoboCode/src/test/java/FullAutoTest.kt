import android.util.Size
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.openCvAbstraction.OpenCvAbstraction
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.CompleteSnapshot
import us.brainstormz.robotTwo.RobotTwoAuto
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initialPreviousTargetState
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.localTests.FauxRobotTwoHardware
import us.brainstormz.robotTwo.localTests.TeleopTest.Companion.emptyWorld
import us.brainstormz.robotTwo.onRobotTests.AprilTagPipeline
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem

class FullAutoTest {


//    @Test
//    fun `robot goes to target position`(){
//        val snapshot: CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":239.8181818181818,"lift":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":0,"ticksMovedSinceReset":0,"currentAmps":0.0},"wristAngles":{"leftClawAngleDegrees":358.43636363636364,"rightClawAngleDegrees":342.5090909090909,"left":358.43636363636364,"right":342.5090909090909}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":0,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.0049804687,"green":0.008105469,"blue":0.0109375,"alpha":0.023535157,"asList":[0.0049804687,0.008105469,0.0109375,0.023535157]},"right":{"red":0.0048828125,"green":0.008300781,"blue":0.012011719,"alpha":0.024804687,"asList":[0.0048828125,0.008300781,0.012011719,0.024804687]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1713002827940,"timeOfMatchStartMillis":1713002826385},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":239.92727272727274,"lift":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":0,"ticksMovedSinceReset":0,"currentAmps":0.0},"wristAngles":{"leftClawAngleDegrees":358.43636363636364,"rightClawAngleDegrees":339.45454545454544,"left":358.43636363636364,"right":339.45454545454544}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":0,"ticksMovedSinceReset":0,"currentAmps":0.0},"transferState":{"left":{"red":0.0049804687,"green":0.008105469,"blue":0.0109375,"alpha":0.023535157,"asList":[0.0049804687,0.008105469,0.0109375,0.023535157]},"right":{"red":0.0048828125,"green":0.008203126,"blue":0.011914062,"alpha":0.024707032,"asList":[0.0048828125,0.008203126,0.011914062,0.024707032]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1713002827879,"timeOfMatchStartMillis":1713002826385},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Retracted","right":"Retracted","asMap":{"Left":"Retracted","Right":"Retracted"},"bothOrNull":"Retracted"},"targetType":"GoingHome"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Collector","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoColor","depo":"NoInput","depoScoringHeightAdjust":0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":0.0}},"doingHandoff":false,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":10.0,"y":10.0,"r":0.0},"movementMode":"Position","power":{"x":0.0,"y":0.0,"r":0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Retracted","right":"Retracted","asMap":{"Left":"Retracted","Right":"Retracted"},"bothOrNull":"Retracted"},"targetType":"GoingHome"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Collector","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoColor","depo":"NoInput","depoScoringHeightAdjust":0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":0.0}},"doingHandoff":false,"timeTargetStartedMilis":1713002826385,"gamepad1Rumble":"Throb"}}""")
//
//        val actualWorld = snapshot.previousActualWorld!!
//        val previousTarget = snapshot.previousActualTarget!!
//
//        var prevNow = System.currentTimeMillis()
//        fun now(): Long {
//            val newNow = prevNow+1000
//            prevNow = newNow
//            return newNow
//        }
//
//        // when
//        val loop = getLoopFunction(actualWorld, now(), previousTarget)
//        loop(actualWorld.copy(
//            actualRobot = actualWorld.actualRobot.copy(
//                positionAndRotation = PositionAndRotation(0.0, 0.0)
//            )
//        ), now())
//        val newTarget =
//            loop(actualWorld.copy(
//                actualRobot = actualWorld.actualRobot.copy(
//                    positionAndRotation = PositionAndRotation(0.0, 0.0)
//                )
//            ), now())
//
//        // then
//        assertEqualsJson(
//            PositionAndRotation(10.0, 10.0),
//            newTarget.targetRobot.drivetrainTarget.targetPosition)
//    }

    @Test
    fun `robot goes to target positionasdf`(){
        val actualWorld = emptyWorld
        val previousTarget = initialPreviousTargetState

        var prevNow = System.currentTimeMillis()
        fun now(): Long {
            val newNow = prevNow+1000
            prevNow = newNow
            return newNow
        }

        // when
        val loop = getLoopFunction(actualWorld, now(), previousTarget)
        for (i in 0..45) {
            loop(actualWorld.copy(
                    actualRobot = actualWorld.actualRobot.copy(
                            positionAndRotation = PositionAndRotation(0.0, 0.0)
                    )
            ), now())
        }
        val newTarget =
        loop(actualWorld.copy(
                actualRobot = actualWorld.actualRobot.copy(
                        positionAndRotation = PositionAndRotation(0.0, 0.0)
                )
        ), now())

//         then
        assertEqualsJson(
            PositionAndRotation(10.0),
            newTarget.targetRobot.drivetrainTarget.targetPosition)
    }


    fun <T>assertEqualsJson(expected:T, actual:T){
        val writer = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        Assert.assertEquals(writer.writeValueAsString(expected), writer.writeValueAsString(actual))
        Assert.assertEquals(expected, actual)
    }

    fun getLoopFunction(initActualWorld:ActualWorld, initNow:Long, previousTargetWorld: TargetWorld? = null): (actual: ActualWorld, now: Long) -> TargetWorld {
        val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
        val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
        val auto = RobotTwoAuto(
            opmode.telemetry,
//            aprilTagPipeline = AprilTagPipeline("Webcam 1", Size(1, 1))
        )
//        val openCv = OpenCvAbstraction(opmode)

        auto.init(hardware,)

//        auto.initLoop(hardware, initActualWorld.actualGamepad1)


        //Set Inputs
        hardware.actualRobot = initActualWorld.actualRobot
        auto.getTime = { initNow }
        previousTargetWorld?.let { previousTarget ->
            auto.functionalReactiveAutoRunner.hackSetForTest(previousTarget)
        }

        auto.start(hardware)

        return { actualWorld, now ->
            //Set Inputs
            hardware.actualRobot = actualWorld.actualRobot
            auto.getTime = { now }

            //Run
            auto.loop(gamepad1 = actualWorld.actualGamepad1, hardware = hardware)

            //Get result
            auto.functionalReactiveAutoRunner.previousTargetState!!
        }
    }
    fun runTest(onlyActualWorld:ActualWorld, onlyNow:Long): TargetWorld {
        return getLoopFunction(onlyActualWorld, onlyNow)(onlyActualWorld, onlyNow)
    }
}