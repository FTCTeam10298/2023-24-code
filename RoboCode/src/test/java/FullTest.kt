import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.CompleteSnapshot
import us.brainstormz.robotTwo.DepoManager
import us.brainstormz.robotTwo.DepoTarget
import us.brainstormz.robotTwo.NewCompleteSnapshot
import us.brainstormz.robotTwo.RobotTwoAuto
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.RobotTwoTeleOp.Companion.initialPreviousTargetState
import us.brainstormz.robotTwo.SerializableGamepad
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.localTests.FauxRobotTwoHardware
import us.brainstormz.robotTwo.localTests.TeleopTest.Companion.emptyWorld
import us.brainstormz.robotTwo.printPretty
import us.brainstormz.robotTwo.subsystems.Arm
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.ColorReading
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Extendo
import us.brainstormz.robotTwo.subsystems.Lift
import us.brainstormz.robotTwo.subsystems.SlideSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer
import us.brainstormz.robotTwo.subsystems.Wrist

class FullTest {
    val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{
    "actualWorld": {
        "actualRobot": {
            "positionAndRotation": {
                "x": 0.0,
                "y": 0.0,
                "r": 0.0
            },
            "depoState": {
                "armAngleDegrees": 242.54545454545456,
                "lift": {
                    "currentPositionTicks": 0,
                    "limitSwitchIsActivated": true,
                    "zeroPositionOffsetTicks": 0,
                    "ticksMovedSinceReset": 0,
                    "currentAmps": 0.0
                },
                "wristAngles": {
                    "leftClawAngleDegrees": 18.836363636363615,
                    "rightClawAngleDegrees": 0.8363636363636431,
                    "left": 18.836363636363615,
                    "right": 0.8363636363636431
                }
            },
            "collectorSystemState": {
                "extendo": {
                    "currentPositionTicks": 1237,
                    "limitSwitchIsActivated": false,
                    "zeroPositionOffsetTicks": 21,
                    "ticksMovedSinceReset": 3531,
                    "currentAmps": 2.066
                },
                "transferState": {
                    "left": {
                        "red": 0.0044921874,
                        "green": 0.0078125,
                        "blue": 0.011035156,
                        "alpha": 0.022851562,
                        "asList": [
                            0.0044921874,
                            0.0078125,
                            0.011035156,
                            0.022851562
                        ]
                    },
                    "right": {
                        "red": 0.004589844,
                        "green": 0.008007812,
                        "blue": 0.012304688,
                        "alpha": 0.024414062,
                        "asList": [
                            0.004589844,
                            0.008007812,
                            0.012304688,
                            0.024414062
                        ]
                    }
                }
            },
            "neopixelState": {
                "wroteForward": true,
                "pixels": [
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    }
                ]
            }
        },
        "aprilTagReadings": [],
        "actualGamepad1": {
            "touchpad": true,
            "dpad_up": false,
            "dpad_down": false,
            "dpad_left": false,
            "dpad_right": false,
            "right_stick_x": 0.0,
            "right_stick_y": 0.0,
            "left_stick_x": 0.0,
            "left_stick_y": 0.0,
            "right_bumper": false,
            "left_bumper": false,
            "right_trigger": 0.0,
            "left_trigger": 0.0,
            "square": false,
            "a": false,
            "x": false,
            "start": false,
            "left_stick_button": false,
            "right_stick_button": false,
            "y": false,
            "b": false,
            "isRumbling": false
        },
        "actualGamepad2": {
            "touchpad": false,
            "dpad_up": false,
            "dpad_down": false,
            "dpad_left": false,
            "dpad_right": false,
            "right_stick_x": 0.0,
            "right_stick_y": 0.0,
            "left_stick_x": 0.0,
            "left_stick_y": 0.0,
            "right_bumper": false,
            "left_bumper": false,
            "right_trigger": 0.0,
            "left_trigger": 0.0,
            "square": false,
            "a": false,
            "x": false,
            "start": false,
            "left_stick_button": false,
            "right_stick_button": false,
            "y": false,
            "b": false,
            "isRumbling": false
        },
        "timestampMilis": 1712898841792
    },
    "previousActualWorld": {
        "actualRobot": {
            "positionAndRotation": {
                "x": 0.0,
                "y": 0.0,
                "r": 0.0
            },
            "depoState": {
                "armAngleDegrees": 242.54545454545456,
                "lift": {
                    "currentPositionTicks": 0,
                    "limitSwitchIsActivated": true,
                    "zeroPositionOffsetTicks": 0,
                    "ticksMovedSinceReset": 0,
                    "currentAmps": 0.01
                },
                "wristAngles": {
                    "leftClawAngleDegrees": 19.16363636363633,
                    "rightClawAngleDegrees": 0.8363636363636431,
                    "left": 19.16363636363633,
                    "right": 0.8363636363636431
                }
            },
            "collectorSystemState": {
                "extendo": {
                    "currentPositionTicks": 1269,
                    "limitSwitchIsActivated": false,
                    "zeroPositionOffsetTicks": 21,
                    "ticksMovedSinceReset": 3499,
                    "currentAmps": 4.144
                },
                "transferState": {
                    "left": {
                        "red": 0.0044921874,
                        "green": 0.0078125,
                        "blue": 0.011035156,
                        "alpha": 0.022851562,
                        "asList": [
                            0.0044921874,
                            0.0078125,
                            0.011035156,
                            0.022851562
                        ]
                    },
                    "right": {
                        "red": 0.004589844,
                        "green": 0.008007812,
                        "blue": 0.012304688,
                        "alpha": 0.02451172,
                        "asList": [
                            0.004589844,
                            0.008007812,
                            0.012304688,
                            0.02451172
                        ]
                    }
                }
            },
            "neopixelState": {
                "wroteForward": false,
                "pixels": [
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    },
                    {
                        "red": 0.0,
                        "blue": 0.0,
                        "white": 0.0,
                        "green": 0.0
                    }
                ]
            }
        },
        "aprilTagReadings": [],
        "actualGamepad1": {
            "touchpad": false,
            "dpad_up": false,
            "dpad_down": false,
            "dpad_left": false,
            "dpad_right": false,
            "right_stick_x": 0.0,
            "right_stick_y": 0.0,
            "left_stick_x": 0.0,
            "left_stick_y": 0.0,
            "right_bumper": false,
            "left_bumper": false,
            "right_trigger": 0.0,
            "left_trigger": 0.0,
            "square": false,
            "a": false,
            "x": false,
            "start": false,
            "left_stick_button": false,
            "right_stick_button": false,
            "y": false,
            "b": false,
            "isRumbling": false
        },
        "actualGamepad2": {
            "touchpad": false,
            "dpad_up": false,
            "dpad_down": false,
            "dpad_left": false,
            "dpad_right": false,
            "right_stick_x": 0.0,
            "right_stick_y": 0.0,
            "left_stick_x": 0.0,
            "left_stick_y": 0.0,
            "right_bumper": false,
            "left_bumper": false,
            "right_trigger": 0.0,
            "left_trigger": 0.0,
            "square": false,
            "a": false,
            "x": false,
            "start": false,
            "left_stick_button": false,
            "right_stick_button": false,
            "y": false,
            "b": false,
            "isRumbling": false
        },
        "timestampMilis": 1712898841748
    },
    "targetWorld": {
        "targetRobot": {
            "drivetrainTarget": {
                "targetPosition": {
                    "x": 0.0,
                    "y": 0.0,
                    "r": 0.0
                },
                "movementMode": "Power",
                "power": {
                    "x": 0.0,
                    "y": 0.0,
                    "r": -0.0
                }
            },
            "depoTarget": {
                "armPosition": {
                    "targetPosition": "In",
                    "movementMode": "Power",
                    "power": 0.0
                },
                "lift": {
                    "targetPosition": [
                        "LiftPositions",
                        "Down"
                    ],
                    "power": -0.0,
                    "movementMode": "Power"
                },
                "wristPosition": {
                    "left": "Retracted",
                    "right": "Retracted",
                    "asMap": {
                        "Left": "Retracted",
                        "Right": "Retracted"
                    },
                    "bothOrNull": "Retracted"
                },
                "targetType": "Manual"
            },
            "collectorTarget": {
                "extendo": {
                    "targetPosition": [
                        "ExtendoPositions",
                        "Min"
                    ],
                    "power": 0.0,
                    "movementMode": "Power"
                },
                "timeOfEjectionStartMilis": 0,
                "timeOfTransferredMillis": 0,
                "intakeNoodles": "Off",
                "dropDown": {
                    "targetPosition": "Up",
                    "movementMode": "Position",
                    "power": 0.0
                },
                "transferSensorState": {
                    "left": {
                        "hasPixelBeenSeen": false,
                        "timeOfSeeingMillis": 0
                    },
                    "right": {
                        "hasPixelBeenSeen": false,
                        "timeOfSeeingMillis": 0
                    }
                },
                "latches": {
                    "left": {
                        "target": "Closed",
                        "timeTargetChangedMillis": 0
                    },
                    "right": {
                        "target": "Closed",
                        "timeTargetChangedMillis": 0
                    }
                }
            },
            "hangPowers": "Holding",
            "launcherPosition": "Holding",
            "lights": {
                "pattern": {
                    "leftPixel": "Unknown",
                    "rightPixel": "Unknown",
                    "asList": [
                        "Unknown",
                        "Unknown"
                    ]
                },
                "stripTarget": {
                    "wroteForward": true,
                    "pixels": [
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        }
                    ]
                }
            }
        },
        "driverInput": {
            "bumperMode": "Collector",
            "gamepad1ControlMode": "Manual",
            "gamepad2ControlMode": "Normal",
            "lightInput": "NoInput",
            "depo": "NoInput",
            "depoScoringHeightAdjust": -0.0,
            "armOverridePower": 0.0,
            "wrist": {
                "left": "NoInput",
                "right": "NoInput",
                "bothClaws": {
                    "Left": "NoInput",
                    "Right": "NoInput"
                }
            },
            "collector": "NoInput",
            "dropdown": "NoInput",
            "dropdownPositionOverride": 0.0,
            "extendo": "NoInput",
            "extendoManualPower": 0.0,
            "hang": "NoInput",
            "launcher": "NoInput",
            "handoff": "NoInput",
            "leftLatch": "NoInput",
            "rightLatch": "NoInput",
            "driveVelocity": {
                "x": 0.0,
                "y": 0.0,
                "r": -0.0
            }
        },
        "doingHandoff": false,
        "timeTargetStartedMilis": 0,
        "gamepad1Rumble": null
    },
    "previousActualTarget": {
        "targetRobot": {
            "drivetrainTarget": {
                "targetPosition": {
                    "x": 0.0,
                    "y": 0.0,
                    "r": 0.0
                },
                "movementMode": "Power",
                "power": {
                    "x": 0.0,
                    "y": 0.0,
                    "r": -0.0
                }
            },
            "depoTarget": {
                "armPosition": {
                    "targetPosition": "In",
                    "movementMode": "Position",
                    "power": 0.0
                },
                "lift": {
                    "targetPosition": [
                        "LiftPositions",
                        "Down"
                    ],
                    "power": 0.0,
                    "movementMode": "Position"
                },
                "wristPosition": {
                    "left": "Retracted",
                    "right": "Retracted",
                    "asMap": {
                        "Left": "Retracted",
                        "Right": "Retracted"
                    },
                    "bothOrNull": "Retracted"
                },
                "targetType": "GoingHome"
            },
            "collectorTarget": {
                "extendo": {
                    "targetPosition": [
                        "ExtendoPositions",
                        "Min"
                    ],
                    "power": 0.0,
                    "movementMode": "Position"
                },
                "timeOfEjectionStartMilis": 0,
                "timeOfTransferredMillis": 0,
                "intakeNoodles": "Off",
                "dropDown": {
                    "targetPosition": "Up",
                    "movementMode": "Position",
                    "power": 0.0
                },
                "transferSensorState": {
                    "left": {
                        "hasPixelBeenSeen": false,
                        "timeOfSeeingMillis": 0
                    },
                    "right": {
                        "hasPixelBeenSeen": false,
                        "timeOfSeeingMillis": 0
                    }
                },
                "latches": {
                    "left": {
                        "target": "Closed",
                        "timeTargetChangedMillis": 0
                    },
                    "right": {
                        "target": "Closed",
                        "timeTargetChangedMillis": 0
                    }
                }
            },
            "hangPowers": "Holding",
            "launcherPosition": "Holding",
            "lights": {
                "pattern": {
                    "leftPixel": "Unknown",
                    "rightPixel": "Unknown",
                    "asList": [
                        "Unknown",
                        "Unknown"
                    ]
                },
                "stripTarget": {
                    "wroteForward": true,
                    "pixels": [
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        },
                        {
                            "red": 0.0,
                            "blue": 0.0,
                            "white": 0.0,
                            "green": 0.0
                        }
                    ]
                }
            }
        },
        "driverInput": {
            "bumperMode": "Collector",
            "gamepad1ControlMode": "Normal",
            "gamepad2ControlMode": "Normal",
            "lightInput": "NoInput",
            "depo": "NoInput",
            "depoScoringHeightAdjust": -0.0,
            "armOverridePower": 0.0,
            "wrist": {
                "left": "NoInput",
                "right": "NoInput",
                "bothClaws": {
                    "Left": "NoInput",
                    "Right": "NoInput"
                }
            },
            "collector": "NoInput",
            "dropdown": "NoInput",
            "dropdownPositionOverride": 0.0,
            "extendo": "NoInput",
            "extendoManualPower": 0.0,
            "hang": "NoInput",
            "launcher": "NoInput",
            "handoff": "NoInput",
            "leftLatch": "NoInput",
            "rightLatch": "NoInput",
            "driveVelocity": {
                "x": 0.0,
                "y": 0.0,
                "r": -0.0
            }
        },
        "doingHandoff": false,
        "timeTargetStartedMilis": 0,
        "gamepad1Rumble": null
    }
}""")

//    @Test
//    fun `extendo shouldn't be frozen`(){
//        // given
//        val snapshot: CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":240.8,"lift":{"currentPositionTicks":8,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-1592,"ticksMovedSinceReset":8,"currentAmps":0.08},"wristAngles":{"leftClawAngleDegrees":7.1636363636363285,"rightClawAngleDegrees":345.56363636363636,"left":7.1636363636363285,"right":345.56363636363636}},"collectorSystemState":{"extendo":{"currentPositionTicks":238,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-56,"ticksMovedSinceReset":336,"currentAmps":1.038},"transferState":{"left":{"red":0.0049804687,"green":0.008007812,"blue":0.010644532,"alpha":0.023144532,"asList":[0.0049804687,0.008007812,0.010644532,0.023144532]},"right":{"red":0.17695312,"green":0.20664063,"blue":0.15957032,"alpha":0.53876954,"asList":[0.17695312,0.20664063,0.15957032,0.53876954]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1713137185539,"timeOfMatchStartMillis":1713137093697},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":240.8,"lift":{"currentPositionTicks":8,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-1592,"ticksMovedSinceReset":8,"currentAmps":0.08},"wristAngles":{"leftClawAngleDegrees":7.1636363636363285,"rightClawAngleDegrees":345.56363636363636,"left":7.1636363636363285,"right":345.56363636363636}},"collectorSystemState":{"extendo":{"currentPositionTicks":238,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-56,"ticksMovedSinceReset":336,"currentAmps":1.027},"transferState":{"left":{"red":0.0049804687,"green":0.008007812,"blue":0.010644532,"alpha":0.023144532,"asList":[0.0049804687,0.008007812,0.010644532,0.023144532]},"right":{"red":0.17685547,"green":0.20664063,"blue":0.15957032,"alpha":0.5388672,"asList":[0.17685547,0.20664063,0.15957032,0.5388672]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1713137185501,"timeOfMatchStartMillis":1713137093697},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Retracted","right":"Retracted","asMap":{"Left":"Retracted","Right":"Retracted"},"bothOrNull":"Retracted"},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1713137166668}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Collector","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Down","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":false,"autoInput":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Position","power":{"x":0.0,"y":0.0,"r":0.0}},"depoInput":"NoInput","handoffInput":"NoInput","wristInput":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"extendoInput":["ExtendoPositions","Min"],"intakeInput":"NoInput","listIndex":null},"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Retracted","right":"Retracted","asMap":{"Left":"Retracted","Right":"Retracted"},"bothOrNull":"Retracted"},"targetType":"GoingHome"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","OutFarEnoughToCompletelyClearDepo"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":1713137114216,"timeOfTransferredMillis":1713137114216,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1713137166668}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1713137148817},"right":{"target":"Closed","timeTargetChangedMillis":1713137148817}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Collector","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Down","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":false,"autoInput":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Position","power":{"x":0.0,"y":0.0,"r":0.0}},"depoInput":"NoInput","handoffInput":"NoInput","wristInput":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"extendoInput":["ExtendoPositions","Min"],"intakeInput":"NoInput","listIndex":null},"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")
//        val actualWorld = snapshot.previousActualWorld!!
//        val previousTarget = snapshot.previousActualTarget!!
////        Assert.assertEquals(RobotTwoTeleOp.GamepadControlMode.Normal, previousTarget.driverInput.gamepad1ControlMode)
//
//        println("previousTarget: " + previousTarget.withoutLights().printPretty())
//
//        // when
//        val newTarget = getLoopFunction()(actualWorld, System.currentTimeMillis(), previousTarget, emptyWorld)
//
//        // then
//        Assert.assertEquals(
//                Extendo.ExtendoTarget(
//                        power = -0.6,
//                        movementMode = DualMovementModeSubsystem.MovementMode.Power,
//                        targetPosition = Extendo.ExtendoPositions.Min
//                ),
//                newTarget.targetRobot.collectorTarget.extendo
//        )
//    }


    @Test
    fun `doesn't retract when you release the extendo extend button in non-manual mode`(){

        // given
        val actualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
//        Assert.assertEquals(RobotTwoTeleOp.GamepadControlMode.Normal, previousTarget.driverInput.gamepad1ControlMode)

        // when
//        val newTarget = runTest(actualWorld, previousTarget, System.currentTimeMillis())
        val newTarget = getLoopFunction()(actualWorld, System.currentTimeMillis(), previousTarget, emptyWorld)

        // then
        println(jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(newTarget.targetRobot.collectorTarget.extendo))
        val extendoTarget = newTarget.targetRobot.collectorTarget.extendo
        Assert.assertEquals(DualMovementModeSubsystem.MovementMode.Power, extendoTarget.movementMode)
    }


    @Test
    fun `latches should stay open when the lift is going up with the pixels after a handoff`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":242.87272727272727,"lift":{"currentPositionTicks":136,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":42,"ticksMovedSinceReset":136,"currentAmps":3.892},"wristAngles":{"leftClawAngleDegrees":115.16363636363633,"rightClawAngleDegrees":345.3454545454546,"left":115.16363636363633,"right":345.3454545454546}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-1,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.004003906,"green":0.0067382813,"blue":0.009277344,"alpha":0.01982422,"asList":[0.004003906,0.0067382813,0.009277344,0.01982422]},"right":{"red":0.057714846,"green":0.050097656,"blue":0.024218751,"alpha":0.13066407,"asList":[0.057714846,0.050097656,0.024218751,0.13066407]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712953661892},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":242.9818181818182,"lift":{"currentPositionTicks":136,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":42,"ticksMovedSinceReset":136,"currentAmps":3.892},"wristAngles":{"leftClawAngleDegrees":115.05454545454546,"rightClawAngleDegrees":345.45454545454544,"left":115.05454545454546,"right":345.45454545454544}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-1,"ticksMovedSinceReset":0,"currentAmps":0.007},"transferState":{"left":{"red":0.004003906,"green":0.0067382813,"blue":0.009277344,"alpha":0.01982422,"asList":[0.004003906,0.0067382813,0.009277344,0.01982422]},"right":{"red":0.057617188,"green":0.050097656,"blue":0.024218751,"alpha":0.13066407,"asList":[0.057617188,0.050097656,0.024218751,0.13066407]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712953661851},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712953640921}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"GoingHome"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712953640921}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":1712953657130}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
//        println(jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(newTarget.targetRobot.collectorTarget.extendo))
        val expectedTarget = previousTarget.copy(
            targetRobot = previousTarget.targetRobot.copy(
                collectorTarget = previousTarget.targetRobot.collectorTarget.copy(
                    latches = Transfer.TransferTarget(
                        left = previousTarget.targetRobot.collectorTarget.latches.left,
                        right = Transfer.LatchTarget(Transfer.LatchPositions.Open, now),
                    )
                )
            )
        )

        assertEqualsJson(expectedTarget.targetRobot.collectorTarget.latches, newTarget.targetRobot.collectorTarget.latches)
    }


    @Test
    fun `latches should stay open when the lift is going up, even after the color sensor stops seeing the pixel`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":241.78181818181818,"lift":{"currentPositionTicks":232,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":37,"ticksMovedSinceReset":2632,"currentAmps":3.3930000000000002},"wristAngles":{"leftClawAngleDegrees":111.23636363636362,"rightClawAngleDegrees":104.47272727272727,"left":111.23636363636362,"right":104.47272727272727}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-5,"ticksMovedSinceReset":0,"currentAmps":0.007},"transferState":{"left":{"red":0.0049804687,"green":0.008691407,"blue":0.01171875,"alpha":0.025,"asList":[0.0049804687,0.008691407,0.01171875,0.025]},"right":{"red":0.0044921874,"green":0.007910157,"blue":0.012109376,"alpha":0.023925781,"asList":[0.0044921874,0.007910157,0.012109376,0.023925781]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712957119929},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":241.78181818181818,"lift":{"currentPositionTicks":232,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":37,"ticksMovedSinceReset":2632,"currentAmps":3.3930000000000002},"wristAngles":{"leftClawAngleDegrees":111.12727272727273,"rightClawAngleDegrees":104.47272727272727,"left":111.12727272727273,"right":104.47272727272727}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-5,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.0049804687,"green":0.008691407,"blue":0.01171875,"alpha":0.025,"asList":[0.0049804687,0.008691407,0.01171875,0.025]},"right":{"red":0.0044921874,"green":0.007910157,"blue":0.012109376,"alpha":0.023925781,"asList":[0.0044921874,0.007910157,0.012109376,0.023925781]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712957119896},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"GoingOut"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712957108970},"right":{"target":"Closed","timeTargetChangedMillis":1712957108848}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        val expectedTarget = previousTarget.copy(
            targetRobot = previousTarget.targetRobot.copy(
                collectorTarget = previousTarget.targetRobot.collectorTarget.copy(
                    latches = Transfer.TransferTarget(
                        left = Transfer.LatchTarget(Transfer.LatchPositions.Open, now),
                        right = Transfer.LatchTarget(Transfer.LatchPositions.Open, now),
                    )
                )
            )
        )

        assertEqualsJson(expectedTarget.targetRobot.collectorTarget.latches, newTarget.targetRobot.collectorTarget.latches)
    }

    @Test
    fun `Claws should be retracted when the extendo is out and the lift is down`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":242.32727272727274,"lift":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-217,"ticksMovedSinceReset":0,"currentAmps":0.003},"wristAngles":{"leftClawAngleDegrees":122.03636363636363,"rightClawAngleDegrees":104.90909090909093,"left":122.03636363636363,"right":104.90909090909093}},"collectorSystemState":{"extendo":{"currentPositionTicks":41,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":6,"ticksMovedSinceReset":763,"currentAmps":1.228},"transferState":{"left":{"red":0.08964844,"green":0.07890625,"blue":0.036621094,"alpha":0.20419922,"asList":[0.08964844,0.07890625,0.036621094,0.20419922]},"right":{"red":0.06796875,"green":0.075683594,"blue":0.07832032,"alpha":0.21865235,"asList":[0.06796875,0.075683594,0.07832032,0.21865235]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712957883853},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":242.32727272727274,"lift":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-217,"ticksMovedSinceReset":0,"currentAmps":0.003},"wristAngles":{"leftClawAngleDegrees":122.03636363636363,"rightClawAngleDegrees":104.90909090909093,"left":122.03636363636363,"right":104.90909090909093}},"collectorSystemState":{"extendo":{"currentPositionTicks":41,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":6,"ticksMovedSinceReset":763,"currentAmps":1.239},"transferState":{"left":{"red":0.08964844,"green":0.07890625,"blue":0.036621094,"alpha":0.20419922,"asList":[0.08964844,0.07890625,0.036621094,0.20419922]},"right":{"red":0.06796875,"green":0.075683594,"blue":0.07832032,"alpha":0.21865235,"asList":[0.06796875,0.075683594,0.07832032,0.21865235]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712957883810},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712957876522},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712957880997}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Collector","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"NoInput","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"GoingHome"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":null,"timeOfTransferredMillis":null,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712957876522},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712957880997}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712957879250},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Collector","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"NoInput","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)

        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)
        // then
        assertEqualsJson(
            Claw.ClawTarget.Retracted,
            newTarget.targetRobot.depoTarget.wristPosition.left)

        assertEqualsJson(
            Claw.ClawTarget.Retracted,
            newTarget.targetRobot.depoTarget.wristPosition.right)
    }

    @Test
    fun `depositor should retract when a handoff is requested and the lift is up`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":69.9636363636364,"lift":{"currentPositionTicks":1204,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":47,"ticksMovedSinceReset":1204,"currentAmps":2.274},"wristAngles":{"leftClawAngleDegrees":20.363636363636374,"rightClawAngleDegrees":346.54545454545456,"left":20.363636363636374,"right":346.54545454545456}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-478,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.0044921874,"green":0.007714844,"blue":0.011035156,"alpha":0.022851562,"asList":[0.0044921874,0.007714844,0.011035156,0.022851562]},"right":{"red":0.0044921874,"green":0.008007812,"blue":0.012207031,"alpha":0.024218751,"asList":[0.0044921874,0.008007812,0.012207031,0.024218751]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":false},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":false},"timestampMilis":1712959431210},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":69.9636363636364,"lift":{"currentPositionTicks":1204,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":47,"ticksMovedSinceReset":1204,"currentAmps":2.27},"wristAngles":{"leftClawAngleDegrees":20.363636363636374,"rightClawAngleDegrees":346.43636363636364,"left":20.363636363636374,"right":346.43636363636364}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-478,"ticksMovedSinceReset":0,"currentAmps":0.007},"transferState":{"left":{"red":0.0044921874,"green":0.007714844,"blue":0.011035156,"alpha":0.022851562,"asList":[0.0044921874,0.007714844,0.011035156,0.022851562]},"right":{"red":0.0044921874,"green":0.008007812,"blue":0.012207031,"alpha":0.024218751,"asList":[0.0044921874,0.008007812,0.012207031,0.024218751]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":false},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":false},"timestampMilis":1712959431181},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"Out","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine3"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Retracted","right":"Retracted","asMap":{"Left":"Retracted","Right":"Retracted"},"bothOrNull":"Retracted"},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":null},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"Out","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine3"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Retracted","right":"Retracted","asMap":{"Left":"Retracted","Right":"Retracted"},"bothOrNull":"Retracted"},"targetType":"GoingOut"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":1712959406272,"timeOfTransferredMillis":1712959406272,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712959416799},"right":{"target":"Closed","timeTargetChangedMillis":1712959411414}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":null}}""")

        val actualFromSnapshot = snapshot.previousActualWorld!!
        println(jacksonObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(actualFromSnapshot))
        val actualWorld = actualFromSnapshot.copy(
            actualRobot = actualFromSnapshot.actualRobot.copy(
                depoState = actualFromSnapshot.actualRobot.depoState.copy(
                    wristAngles = Wrist.ActualWrist(
                        leftClawAngleDegrees = 0.0,
                        rightClawAngleDegrees = 0.0
                    )
                )
            ),
        )
        val previousTargetFromSnapshot = snapshot.previousActualTarget!!
        val previousTarget = previousTargetFromSnapshot.copy(
            driverInput = previousTargetFromSnapshot.driverInput.copy(
                depo = RobotTwoTeleOp.DepoInput.Down
            )
        )
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        assertEqualsJson(
            Lift.LiftPositions.TargetClearForArmToMove,
            newTarget.targetRobot.depoTarget.lift.targetPosition)
    }


    @Test
    fun `When depo is out claws should be allowed to close`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":66.4727272727273,"lift":{"currentPositionTicks":670,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-162,"ticksMovedSinceReset":670,"currentAmps":1.417},"wristAngles":{"leftClawAngleDegrees":122.36363636363635,"rightClawAngleDegrees":347.8545454545455,"left":122.36363636363635,"right":347.8545454545455}},"collectorSystemState":{"extendo":{"currentPositionTicks":-7,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-14,"ticksMovedSinceReset":9,"currentAmps":4.29},"transferState":{"left":{"red":0.14179687,"green":0.17070313,"blue":0.12597656,"alpha":0.43701172,"asList":[0.14179687,0.17070313,0.12597656,0.43701172]},"right":{"red":0.0047851563,"green":0.008398438,"blue":0.012304688,"alpha":0.025097657,"asList":[0.0047851563,0.008398438,0.012304688,0.025097657]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":true,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":false},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":false},"timestampMilis":1713065729550,"timeOfMatchStartMillis":1713065689466},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":66.4727272727273,"lift":{"currentPositionTicks":670,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-162,"ticksMovedSinceReset":670,"currentAmps":1.417},"wristAngles":{"leftClawAngleDegrees":122.36363636363635,"rightClawAngleDegrees":347.8545454545455,"left":122.36363636363635,"right":347.8545454545455}},"collectorSystemState":{"extendo":{"currentPositionTicks":-7,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-14,"ticksMovedSinceReset":9,"currentAmps":4.293},"transferState":{"left":{"red":0.14179687,"green":0.17070313,"blue":0.12597656,"alpha":0.43691406,"asList":[0.14179687,0.17070313,0.12597656,0.43691406]},"right":{"red":0.0047851563,"green":0.008398438,"blue":0.012304688,"alpha":0.025097657,"asList":[0.0047851563,0.008398438,0.012304688,0.025097657]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":true,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1713065729484,"timeOfMatchStartMillis":1713065689466},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"Out","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1713065689466},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Manual","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":false,"autoInput":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Position","power":{"x":0.0,"y":0.0,"r":0.0}},"depoInput":"NoInput","handoffInput":"NoInput","wristInput":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"extendoInput":["ExtendoPositions","Min"],"intakeInput":"NoInput","listIndex":null},"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"Out","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"GoingOut"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":-0.6,"movementMode":"Power"},"timeOfEjectionStartMilis":null,"timeOfTransferredMillis":null,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1713065689466},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Open","timeTargetChangedMillis":1713065692885}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Manual","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":false,"autoInput":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Position","power":{"x":0.0,"y":0.0,"r":0.0}},"depoInput":"NoInput","handoffInput":"NoInput","wristInput":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"extendoInput":["ExtendoPositions","Min"],"intakeInput":"NoInput","listIndex":null},"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorldFromSnapshot = snapshot.previousActualWorld!!
        val actualWorld = actualWorldFromSnapshot
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        val gamepad = actualWorld.actualGamepad1
        val wrist = actualWorld.actualRobot.depoState.wristAngles
        println("gamepad: ${gamepad.printPretty()}")
        println("wrist: ${wrist.printPretty()}")

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        assertEqualsJson(
                DepoTarget(
                        wristPosition = Wrist.WristTargets(Claw.ClawTarget.Retracted),
                        lift = Lift.TargetLift(Lift.LiftPositions.SetLine2),
                        armPosition = Arm.ArmTarget(Arm.Positions.Out),
                        targetType = DepoManager.DepoTargetType.GoingOut
                ),
                newTarget.targetRobot.depoTarget)
    }


    @Test
    fun `Depo should go up when transfer is done, even if extendo is a little out`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":240.72727272727275,"lift":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":0,"ticksMovedSinceReset":0,"currentAmps":0.0},"wristAngles":{"leftClawAngleDegrees":117.45454545454544,"rightClawAngleDegrees":102.72727272727272,"left":117.45454545454544,"right":102.72727272727272}},"collectorSystemState":{"extendo":{"currentPositionTicks":3,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-2,"ticksMovedSinceReset":3,"currentAmps":0.003},"transferState":{"left":{"red":0.14326172,"green":0.16689454,"blue":0.12832032,"alpha":0.43427736,"asList":[0.14326172,0.16689454,0.12832032,0.43427736]},"right":{"red":0.16220704,"green":0.19277345,"blue":0.15136719,"alpha":0.5012695,"asList":[0.16220704,0.19277345,0.15136719,0.5012695]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712978057847},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":240.72727272727275,"lift":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":0,"ticksMovedSinceReset":0,"currentAmps":0.0},"wristAngles":{"leftClawAngleDegrees":117.45454545454544,"rightClawAngleDegrees":102.72727272727272,"left":117.45454545454544,"right":102.72727272727272}},"collectorSystemState":{"extendo":{"currentPositionTicks":3,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-2,"ticksMovedSinceReset":3,"currentAmps":0.003},"transferState":{"left":{"red":0.14316407,"green":0.16679688,"blue":0.12832032,"alpha":0.4341797,"asList":[0.14316407,0.16679688,0.12832032,0.4341797]},"right":{"red":0.16220704,"green":0.19277345,"blue":0.15136719,"alpha":0.5012695,"asList":[0.16220704,0.19277345,0.15136719,0.5012695]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712978057812},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712978007800},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712978007850}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset4","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"GoingHome"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":null,"timeOfTransferredMillis":1712978007850,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712978007800},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712978007850}},"latches":{"left":{"target":"Open","timeTargetChangedMillis":1712978025442},"right":{"target":"Open","timeTargetChangedMillis":1712978025009}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset4","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorldFromSnapshot = snapshot.previousActualWorld!!
        val actualWorld = actualWorldFromSnapshot.copy(
            actualGamepad1 = actualWorldFromSnapshot.actualGamepad1.copy(
                dpad_up = true
            )
        )


        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        assertEqualsJson(Lift.LiftPositions.SetLine3,
            newTarget.targetRobot.depoTarget.lift.targetPosition)
    }


//    @Test
//    fun `robot shouldn't be frozen when only one side is handed off`(){
//        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":243.23636363636365,"lift":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-50,"ticksMovedSinceReset":0,"currentAmps":0.0},"wristAngles":{"leftClawAngleDegrees":121.05454545454543,"rightClawAngleDegrees":0.5090909090909008,"left":121.05454545454543,"right":0.5090909090909008}},"collectorSystemState":{"extendo":{"currentPositionTicks":2,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-27,"ticksMovedSinceReset":2,"currentAmps":0.003},"transferState":{"left":{"red":0.16044922,"green":0.18486328,"blue":0.13867188,"alpha":0.48095703,"asList":[0.16044922,0.18486328,0.13867188,0.48095703]},"right":{"red":0.165625,"green":0.19355468,"blue":0.15078126,"alpha":0.50458986,"asList":[0.165625,0.19355468,0.15078126,0.50458986]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712980429322},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":243.23636363636365,"lift":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-50,"ticksMovedSinceReset":0,"currentAmps":0.003},"wristAngles":{"leftClawAngleDegrees":121.05454545454543,"rightClawAngleDegrees":0.5090909090909008,"left":121.05454545454543,"right":0.5090909090909008}},"collectorSystemState":{"extendo":{"currentPositionTicks":2,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-27,"ticksMovedSinceReset":2,"currentAmps":0.003},"transferState":{"left":{"red":0.16044922,"green":0.18496095,"blue":0.13876954,"alpha":0.4810547,"asList":[0.16044922,0.18496095,0.13876954,0.4810547]},"right":{"red":0.165625,"green":0.19355468,"blue":0.15078126,"alpha":0.50458986,"asList":[0.165625,0.19355468,0.15078126,0.50458986]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712980429280},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712980411097},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712980404111}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Collector","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Down","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":false,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"GoingHome"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":1712980411097,"timeOfTransferredMillis":1712980411097,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712980411097},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712980404111}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712980387001},"right":{"target":"Open","timeTargetChangedMillis":1712980420494}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Collector","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Down","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":false,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")
//
//        //When x is pressed transfer the other pixel
//        val actualWorldFromSnapshot = snapshot.previousActualWorld!!
//        val actualWorld = actualWorldFromSnapshot.copy(
//            actualGamepad1 = actualWorldFromSnapshot.actualGamepad1.copy(
//                a = true
//            )
//        )
//        val previousTarget = snapshot.previousActualTarget!!
//        val now = actualWorld.timestampMilis + 1
//
//        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
//
//        // then
//        assertEqualsJson(
//            Wrist.WristTargets(
//                Claw.ClawTarget.Gripping,
//                Claw.ClawTarget.Gripping
//            ),
//            newTarget.targetRobot.depoTarget.wristPosition)
//
//    }

    @Test
    fun `depo should go down when both claws are closed by gamepad1`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":62.4727272727273,"lift":{"currentPositionTicks":670,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-214,"ticksMovedSinceReset":772,"currentAmps":1.352},"wristAngles":{"leftClawAngleDegrees":120.5090909090909,"rightClawAngleDegrees":346.43636363636364,"left":120.5090909090909,"right":346.43636363636364}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-11,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.0047851563,"green":0.007910157,"blue":0.011035156,"alpha":0.023144532,"asList":[0.0047851563,0.007910157,0.011035156,0.023144532]},"right":{"red":0.0046875,"green":0.008203126,"blue":0.0125,"alpha":0.025097657,"asList":[0.0046875,0.008203126,0.0125,0.025097657]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712982131890},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":62.4727272727273,"lift":{"currentPositionTicks":670,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-214,"ticksMovedSinceReset":772,"currentAmps":1.348},"wristAngles":{"leftClawAngleDegrees":120.39999999999998,"rightClawAngleDegrees":346.43636363636364,"left":120.39999999999998,"right":346.43636363636364}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-11,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.0047851563,"green":0.007910157,"blue":0.011035156,"alpha":0.023144532,"asList":[0.0047851563,0.007910157,0.011035156,0.023144532]},"right":{"red":0.0046875,"green":0.008203126,"blue":0.0125,"alpha":0.025097657,"asList":[0.0046875,0.008203126,0.0125,0.025097657]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712982131853},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"Out","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"Out","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"GoingOut"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":1712982117949,"timeOfTransferredMillis":1712982117949,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712982122250},"right":{"target":"Closed","timeTargetChangedMillis":1712982122250}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorldFromSnapshot = snapshot.previousActualWorld!!
        val actualWorld = actualWorldFromSnapshot.copy(
            actualRobot = actualWorldFromSnapshot.actualRobot.copy(
                depoState = actualWorldFromSnapshot.actualRobot.depoState.copy(
                    wristAngles = Wrist.ActualWrist(
                        Claw.ClawTarget.Retracted.angleDegrees,
                        Claw.ClawTarget.Retracted.angleDegrees
                    ),
                ),
            ),
            actualGamepad1 = actualWorldFromSnapshot.actualGamepad1.copy(
                right_bumper = false,
                left_bumper = false
            )
        )
        val previousTargetFromSnapshot = snapshot.previousActualTarget!!
        val previousTarget = previousTargetFromSnapshot.copy(

        )
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        assertEqualsJson(Lift.LiftPositions.TargetClearForArmToMove,
            newTarget.targetRobot.depoTarget.lift.targetPosition)
    }


    @Test
    fun `claw should close if when depositing even when one claw is already closed`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":62.4727272727273,"lift":{"currentPositionTicks":670,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-214,"ticksMovedSinceReset":772,"currentAmps":1.352},"wristAngles":{"leftClawAngleDegrees":120.5090909090909,"rightClawAngleDegrees":346.43636363636364,"left":120.5090909090909,"right":346.43636363636364}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-11,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.0047851563,"green":0.007910157,"blue":0.011035156,"alpha":0.023144532,"asList":[0.0047851563,0.007910157,0.011035156,0.023144532]},"right":{"red":0.0046875,"green":0.008203126,"blue":0.0125,"alpha":0.025097657,"asList":[0.0046875,0.008203126,0.0125,0.025097657]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712982131890},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":62.4727272727273,"lift":{"currentPositionTicks":670,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-214,"ticksMovedSinceReset":772,"currentAmps":1.348},"wristAngles":{"leftClawAngleDegrees":120.39999999999998,"rightClawAngleDegrees":346.43636363636364,"left":120.39999999999998,"right":346.43636363636364}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-11,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.0047851563,"green":0.007910157,"blue":0.011035156,"alpha":0.023144532,"asList":[0.0047851563,0.007910157,0.011035156,0.023144532]},"right":{"red":0.0046875,"green":0.008203126,"blue":0.0125,"alpha":0.025097657,"asList":[0.0046875,0.008203126,0.0125,0.025097657]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712982131853},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"Out","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"Out","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"GoingOut"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":1712982117949,"timeOfTransferredMillis":1712982117949,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712982122250},"right":{"target":"Closed","timeTargetChangedMillis":1712982122250}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorldFromSnapshot = snapshot.previousActualWorld!!
        val actualWorld = actualWorldFromSnapshot.copy(
            actualGamepad1 = actualWorldFromSnapshot.actualGamepad1.copy(
                left_bumper = true,
//                right_bumper = true
            )
        )
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        assertEqualsJson(Wrist.WristTargets(
            Claw.ClawTarget.Retracted,
            Claw.ClawTarget.Retracted),
            newTarget.targetRobot.depoTarget.wristPosition)
    }

    @Test
    fun `latches should stay open when depo is going up, even if extendo is a little out`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":243.23636363636365,"lift":{"currentPositionTicks":160,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":37,"ticksMovedSinceReset":160,"currentAmps":3.065},"wristAngles":{"leftClawAngleDegrees":106.32727272727271,"rightClawAngleDegrees":104.25454545454545,"left":106.32727272727271,"right":104.25454545454545}},"collectorSystemState":{"extendo":{"currentPositionTicks":7,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-11,"ticksMovedSinceReset":7,"currentAmps":0.007},"transferState":{"left":{"red":0.0046875,"green":0.0078125,"blue":0.010839844,"alpha":0.022949219,"asList":[0.0046875,0.0078125,0.010839844,0.022949219]},"right":{"red":0.0046875,"green":0.008203126,"blue":0.012304688,"alpha":0.024707032,"asList":[0.0046875,0.008203126,0.012304688,0.024707032]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712979789687},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":243.23636363636365,"lift":{"currentPositionTicks":139,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":37,"ticksMovedSinceReset":139,"currentAmps":2.708},"wristAngles":{"leftClawAngleDegrees":119.20000000000002,"rightClawAngleDegrees":101.63636363636365,"left":119.20000000000002,"right":101.63636363636365}},"collectorSystemState":{"extendo":{"currentPositionTicks":6,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-11,"ticksMovedSinceReset":6,"currentAmps":0.007},"transferState":{"left":{"red":0.0046875,"green":0.0078125,"blue":0.010839844,"alpha":0.022949219,"asList":[0.0046875,0.0078125,0.010839844,0.022949219]},"right":{"red":0.0050781253,"green":0.0087890625,"blue":0.013085938,"alpha":0.026660157,"asList":[0.0050781253,0.0087890625,0.013085938,0.026660157]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712979789635},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Retracted","right":"Retracted","asMap":{"Left":"Retracted","Right":"Retracted"},"bothOrNull":"Retracted"},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Retracted","right":"Retracted","asMap":{"Left":"Retracted","Right":"Retracted"},"bothOrNull":"Retracted"},"targetType":"GoingOut"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":1712979780186,"timeOfTransferredMillis":1712979780186,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712979789635},"right":{"target":"Closed","timeTargetChangedMillis":1712979789593}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        assertEqualsJson(
            Transfer.TransferTarget(
                Transfer.LatchTarget(Transfer.LatchPositions.Open, now),
                Transfer.LatchTarget(Transfer.LatchPositions.Open, now)),
            newTarget.targetRobot.collectorTarget.latches)
    }


    @Test
    fun `latches should stay open when depo is going up2`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":243.45454545454544,"lift":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":23,"ticksMovedSinceReset":0,"currentAmps":0.0},"wristAngles":{"leftClawAngleDegrees":119.5272727272727,"rightClawAngleDegrees":100.00000000000003,"left":119.5272727272727,"right":100.00000000000003}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-6,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.07724609,"green":0.079882815,"blue":0.08339844,"alpha":0.23554687,"asList":[0.07724609,0.079882815,0.08339844,0.23554687]},"right":{"red":0.03828125,"green":0.07265625,"blue":0.03232422,"alpha":0.14462891,"asList":[0.03828125,0.07265625,0.03232422,0.14462891]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712976938520},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":243.56363636363636,"lift":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":23,"ticksMovedSinceReset":0,"currentAmps":0.003},"wristAngles":{"leftClawAngleDegrees":114.72727272727269,"rightClawAngleDegrees":104.79999999999998,"left":114.72727272727269,"right":104.79999999999998}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-6,"ticksMovedSinceReset":0,"currentAmps":0.0},"transferState":{"left":{"red":0.07724609,"green":0.079882815,"blue":0.08339844,"alpha":0.23554687,"asList":[0.07724609,0.079882815,0.08339844,0.23554687]},"right":{"red":0.031542968,"green":0.060058594,"blue":0.026757812,"alpha":0.11923828,"asList":[0.031542968,0.060058594,0.026757812,0.11923828]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712976938484},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712976870464},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712976870464}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Collector","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Down","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"In","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","Down"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"GoingHome"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":null,"timeOfTransferredMillis":null,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712976870464},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712976870464}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712976938396},"right":{"target":"Closed","timeTargetChangedMillis":1712976938396}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Collector","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Down","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        assertEqualsJson(
            Transfer.TransferTarget(
                Transfer.LatchTarget(Transfer.LatchPositions.Open, now),
                Transfer.LatchTarget(Transfer.LatchPositions.Open, now)),
            newTarget.targetRobot.collectorTarget.latches)
    }

    @Test
    fun `latches should stay open when depo is going up`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":243.56363636363636,"lift":{"currentPositionTicks":54,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":43,"ticksMovedSinceReset":136,"currentAmps":2.628},"wristAngles":{"leftClawAngleDegrees":118.1090909090909,"rightClawAngleDegrees":103.6,"left":118.1090909090909,"right":103.6}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-60,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.03017578,"green":0.057519533,"blue":0.025,"alpha":0.112988286,"asList":[0.03017578,0.057519533,0.025,0.112988286]},"right":{"red":0.07587891,"green":0.07919922,"blue":0.08681641,"alpha":0.23740235,"asList":[0.07587891,0.07919922,0.08681641,0.23740235]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":true,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712974780276},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":243.45454545454544,"lift":{"currentPositionTicks":38,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":43,"ticksMovedSinceReset":120,"currentAmps":2.996},"wristAngles":{"leftClawAngleDegrees":118.9818181818182,"rightClawAngleDegrees":102.72727272727272,"left":118.9818181818182,"right":102.72727272727272}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-60,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.02890625,"green":0.054980468,"blue":0.023828125,"alpha":0.1078125,"asList":[0.02890625,0.054980468,0.023828125,0.1078125]},"right":{"red":0.07587891,"green":0.07919922,"blue":0.08681641,"alpha":0.23740235,"asList":[0.07587891,0.07919922,0.08681641,0.23740235]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":true,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712974780232},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine3"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712974743474},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712974743474}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset4","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine3"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"GoingOut"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":null,"timeOfTransferredMillis":null,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712974743474},"right":{"hasPixelBeenSeen":true,"timeOfSeeingMillis":1712974743474}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712974780174},"right":{"target":"Closed","timeTargetChangedMillis":1712974780174}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset4","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        assertEqualsJson(
            Transfer.TransferTarget(
                Transfer.LatchTarget(Transfer.LatchPositions.Open, now),
                Transfer.LatchTarget(Transfer.LatchPositions.Open, now)),
            newTarget.targetRobot.collectorTarget.latches)
    }



    @Test
    fun `depositor should go up when a handoff was previously requested and now the depo is requested to move up`(){
        val actualWorld = emptyWorld.copy(
            actualRobot = emptyWorld.actualRobot.copy(
                depoState = emptyWorld.actualRobot.depoState.copy(
                    wristAngles = Wrist.ActualWrist(
                        Claw.ClawTarget.Gripping.angleDegrees,
                        Claw.ClawTarget.Gripping.angleDegrees
                    ),
                    lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.SetLine3.ticks, false, 0, 0, 0.0),
                    armAngleDegrees = Arm.Positions.Out.angleDegrees
                ),
                collectorSystemState = emptyWorld.actualRobot.collectorSystemState.copy(
                    transferState = Transfer.ActualTransfer(
                        ColorReading(1f, 1f, 1f, 1f),
                        ColorReading(1f, 1f, 1f, 1f),
                    )
                )
            ),
            actualGamepad1 = emptyWorld.actualGamepad1.copy(
                dpad_up = true
            )
        )

        val previousTarget = initialPreviousTargetState.copy(
            targetRobot = initialPreviousTargetState.targetRobot.copy(
                depoTarget = initialPreviousTargetState.targetRobot.depoTarget.copy(
                    wristPosition = Wrist.WristTargets(
                        Claw.ClawTarget.Gripping,
                        Claw.ClawTarget.Gripping
                    )
                ),
                collectorTarget = initialPreviousTargetState.targetRobot.collectorTarget.copy(
                    transferSensorState = Transfer.TransferSensorState(
                        Transfer.SensorState(true, 0),
                        Transfer.SensorState(true, 0),
                    )
                )
            ),
            doingHandoff = true
        )

        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        assertEqualsJson(
            Lift.LiftPositions.SetLine3,
            newTarget.targetRobot.depoTarget.lift.targetPosition)
    }


    @Test
    fun `extendo is jittering`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":65.9636363636364,"lift":{"currentPositionTicks":663,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":19,"ticksMovedSinceReset":829,"currentAmps":1.432},"wristAngles":{"leftClawAngleDegrees":113.41818181818178,"rightClawAngleDegrees":341.8545454545455,"left":113.41818181818178,"right":341.8545454545455}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-1,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.0052734376,"green":0.008496094,"blue":0.010839844,"alpha":0.024316406,"asList":[0.0052734376,0.008496094,0.010839844,0.024316406]},"right":{"red":0.0048828125,"green":0.008300781,"blue":0.011914062,"alpha":0.024804687,"asList":[0.0048828125,0.008300781,0.011914062,0.024804687]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":true,"x":false,"start":true,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712986729111},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":65.9636363636364,"lift":{"currentPositionTicks":663,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":19,"ticksMovedSinceReset":829,"currentAmps":1.428},"wristAngles":{"leftClawAngleDegrees":113.30909090909088,"rightClawAngleDegrees":341.8545454545455,"left":113.30909090909088,"right":341.8545454545455}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-1,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.0052734376,"green":0.008496094,"blue":0.010839844,"alpha":0.024316406,"asList":[0.0052734376,0.008496094,0.010839844,0.024316406]},"right":{"red":0.0048828125,"green":0.008300781,"blue":0.011914062,"alpha":0.024804687,"asList":[0.0048828125,0.008300781,0.011914062,0.024804687]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712986729056},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"Out","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":false,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"Out","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Retracted","asMap":{"Left":"Gripping","Right":"Retracted"},"bothOrNull":null},"targetType":"GoingOut"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712986724596},"right":{"target":"Closed","timeTargetChangedMillis":1712986727621}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0},{"red":255.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":false,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
//        val newTarget = runTest(actualWorld, previousTarget, now)
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        //TODO actually implement this test
//        assertEqualsJson(Wrist.WristTargets(
//            Claw.ClawTarget.Retracted,
//            Claw.ClawTarget.Retracted,
//        ),
//            newTarget.targetRobot.depoTarget.wristPosition)
    }


    @Test
    fun `claws close when gamepad1 tells them to even if the bot is below arm clearance height`(){
        val actualWorld = emptyWorld.copy(
            actualRobot = emptyWorld.actualRobot.copy(
                depoState = emptyWorld.actualRobot.depoState.copy(
                    wristAngles = Wrist.ActualWrist(
                        Claw.ClawTarget.Gripping.angleDegrees,
                        Claw.ClawTarget.Gripping.angleDegrees
                    ),
                    lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.ClearForArmToMove.ticks-10, false, 0, 0, 0.0),
                    armAngleDegrees = Arm.Positions.Out.angleDegrees
                ),
                collectorSystemState = emptyWorld.actualRobot.collectorSystemState.copy(
                    transferState = Transfer.ActualTransfer(
                        ColorReading(0f, 0f, 0f, 0f),
                        ColorReading(0f, 0f, 0f, 0f),
                    ),
                    extendo = SlideSubsystem.ActualSlideSubsystem(Extendo.ExtendoPositions.Min.ticks, true, 0, 0, 0.0)
                )
            ),
            actualGamepad1 = emptyWorld.actualGamepad1.copy(
                left_bumper = true,
                right_bumper = true,
            )
        )

        val previousTarget = initialPreviousTargetState.copy(
            targetRobot = initialPreviousTargetState.targetRobot.copy(
                depoTarget = initialPreviousTargetState.targetRobot.depoTarget.copy(
                    wristPosition = Wrist.WristTargets(
                        Claw.ClawTarget.Gripping,
                        Claw.ClawTarget.Gripping
                    ),
                    armPosition = Arm.ArmTarget(Arm.Positions.Out),
                    lift = Lift.TargetLift(Lift.LiftPositions.SetLine1)
                ),
                collectorTarget = initialPreviousTargetState.targetRobot.collectorTarget.copy(
                    transferSensorState = Transfer.TransferSensorState(
                        Transfer.SensorState(false, 0),
                        Transfer.SensorState(false, 0),
                    )
                )
            ),
            driverInput = initialPreviousTargetState.driverInput.copy(
                depo = RobotTwoTeleOp.DepoInput.Preset1,
                bumperMode = RobotTwoTeleOp.Gamepad1BumperMode.Claws
            ),
            doingHandoff = true
        )

        val now = actualWorld.timestampMilis + 1

        // when
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)

        // then
        assertEqualsJson(Wrist.WristTargets(
            Claw.ClawTarget.Retracted,
            Claw.ClawTarget.Retracted,
        ),
            newTarget.targetRobot.depoTarget.wristPosition)
    }


    @Test
    fun `depositor should go down when a handoff just finished and now a handoff is requested again`(){

        val actualWorld = emptyWorld.copy(
            actualRobot = emptyWorld.actualRobot.copy(
                depoState = emptyWorld.actualRobot.depoState.copy(
                    wristAngles = Wrist.ActualWrist(
                        Claw.ClawTarget.Retracted.angleDegrees,
                        Claw.ClawTarget.Retracted.angleDegrees
                    )
                ),
//                collectorSystemState = emptyWorld.actualRobot.collectorSystemState.copy(
//                    transferState = Transfer.ActualTransfer(
//                        ColorReading(1f, 1f, 1f, 1f),
//                        ColorReading(1f, 1f, 1f, 1f),
//                    )
//                )
            ),
            actualGamepad1 = emptyWorld.actualGamepad1.copy(
                a = true
            )
        )

        val previousTarget = initialPreviousTargetState.copy(
            targetRobot = initialPreviousTargetState.targetRobot.copy(
                depoTarget = initialPreviousTargetState.targetRobot.depoTarget.copy(
                    wristPosition = Wrist.WristTargets(
                        Claw.ClawTarget.Retracted,
                        Claw.ClawTarget.Retracted
                    )
                ),
                collectorTarget = initialPreviousTargetState.targetRobot.collectorTarget.copy(
                    transferSensorState = Transfer.TransferSensorState(
                        Transfer.SensorState(false, 0),
                        Transfer.SensorState(false, 0),
                    )
                )
            ),
            doingHandoff = true
        )

        val now = actualWorld.timestampMilis + 1

        // when
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)
//        val newTarget = runTest(actualWorld, previousTarget, now)

        // then
        assertEqualsJson(
            Lift.LiftPositions.TargetClearForArmToMove,
            newTarget.targetRobot.depoTarget.lift.targetPosition)
    }


    @Test
    fun `claws should close when a handoff is finished and input is to close claw`(){

        val actualWorld = emptyWorld.copy(
            actualRobot = emptyWorld.actualRobot.copy(
                depoState = emptyWorld.actualRobot.depoState.copy(
                    wristAngles = Wrist.ActualWrist(
                        Claw.ClawTarget.Gripping.angleDegrees,
                        Claw.ClawTarget.Gripping.angleDegrees
                    ),
                    lift = SlideSubsystem.ActualSlideSubsystem(
                        Lift.LiftPositions.SetLine2.ticks,
                        false, 0, 0, 0.0
                    ),
                    armAngleDegrees = Arm.Positions.Out.angleDegrees
                ),
            ),
            actualGamepad2 = emptyWorld.actualGamepad2.copy(
                right_bumper = true,
                left_bumper = true
            )
        )


        val previousTarget = initialPreviousTargetState.copy(
            targetRobot = initialPreviousTargetState.targetRobot.copy(
                depoTarget = DepoTarget(
                    wristPosition = Wrist.WristTargets(
                        Claw.ClawTarget.Gripping,
                        Claw.ClawTarget.Gripping
                    ),
                    lift = Lift.TargetLift(Lift.LiftPositions.SetLine2),
                    armPosition = Arm.ArmTarget(Arm.Positions.Out),
                    targetType = DepoManager.DepoTargetType.GoingOut
                ),
                collectorTarget = initialPreviousTargetState.targetRobot.collectorTarget.copy(
                    transferSensorState = Transfer.TransferSensorState(
                        Transfer.SensorState(false, 0),
                        Transfer.SensorState(false, 0),
                    )
                )
            ),
            driverInput = initialPreviousTargetState.driverInput.copy(
                depo = RobotTwoTeleOp.DepoInput.Preset3
            ),
            doingHandoff = true
        )

        val now = actualWorld.timestampMilis + 1


        // when
        val newTarget = getLoopFunction()(actualWorld, now, previousTarget, emptyWorld)
//        val newTarget = runTest(actualWorld, previousTarget, now)


        // then
//        val expectedTarget = previousTarget.copy(
//            targetRobot = previousTarget.targetRobot.copy(
//                depoTarget = previousTarget.targetRobot.depoTarget.copy(
//                    wristPosition = Wrist.WristTargets(
//                        Claw.ClawTarget.Retracted,
//                        Claw.ClawTarget.Retracted
//                    ),
//                ),
//            )
//        )
        val expectedTarget = DepoTarget(
            wristPosition = Wrist.WristTargets(
                Claw.ClawTarget.Retracted,
                Claw.ClawTarget.Retracted
            ),
            lift = Lift.TargetLift(Lift.LiftPositions.SetLine2),
            armPosition = Arm.ArmTarget(Arm.Positions.Out),
            targetType = DepoManager.DepoTargetType.GoingOut
        )

        assertEqualsJson(expectedTarget,
                        newTarget.targetRobot.depoTarget)
    }


//    TODO make this test pass when I have more time
    @Test
    fun `does the mash`(){
        //given
        val snapshot: NewCompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":236.54545454545456,"lift":{"currentPositionTicks":186,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-755,"ticksMovedSinceReset":1816,"currentAmps":5.048},"wristAngles":{"leftClawAngleDegrees":124.21818181818182,"rightClawAngleDegrees":105.23636363636362,"left":124.21818181818182,"right":105.23636363636362}},"collectorSystemState":{"extendo":{"currentPositionTicks":123,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-166,"ticksMovedSinceReset":289,"currentAmps":0.903},"transferState":{"left":{"red":0.0048828125,"green":0.007910157,"blue":0.010546875,"alpha":0.022851562,"asList":[0.0048828125,0.007910157,0.010546875,0.022851562]},"right":{"red":0.0048828125,"green":0.008300781,"blue":0.012109376,"alpha":0.025,"asList":[0.0048828125,0.008300781,0.012109376,0.025]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1713079420157,"timeOfMatchStartMillis":1713079405261},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":237.74545454545455,"lift":{"currentPositionTicks":205,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-755,"ticksMovedSinceReset":1797,"currentAmps":4.72},"wristAngles":{"leftClawAngleDegrees":124.21818181818182,"rightClawAngleDegrees":104.58181818181816,"left":124.21818181818182,"right":104.58181818181816}},"collectorSystemState":{"extendo":{"currentPositionTicks":121,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":-166,"ticksMovedSinceReset":287,"currentAmps":0.896},"transferState":{"left":{"red":0.0048828125,"green":0.007910157,"blue":0.010546875,"alpha":0.022851562,"asList":[0.0048828125,0.007910157,0.010546875,0.022851562]},"right":{"red":0.0048828125,"green":0.008300781,"blue":0.012109376,"alpha":0.025,"asList":[0.0048828125,0.008300781,0.012109376,0.025]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1713079420113,"timeOfMatchStartMillis":1713079405261},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","ClearForArmToMove"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Down","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"autoInput":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Position","power":{"x":0.0,"y":0.0,"r":0.0}},"depoInput":"NoInput","handoffInput":"NoInput","wristInput":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"extendoInput":["ExtendoPositions","Min"],"intakeInput":"NoInput","listIndex":null},"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","ClearForArmToMove"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"GoingHome"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","OutFarEnoughToCompletelyClearDepo"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":null,"timeOfTransferredMillis":1713079408762,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Open","timeTargetChangedMillis":1713079418956},"right":{"target":"Open","timeTargetChangedMillis":1713079418875}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Down","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"autoInput":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Position","power":{"x":0.0,"y":0.0,"r":0.0}},"depoInput":"NoInput","handoffInput":"NoInput","wristInput":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"extendoInput":["ExtendoPositions","Min"],"intakeInput":"NoInput","listIndex":null},"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")
        val previousActualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
//        val actualWorld = snapshot.actualWorld
        val actualWorld = previousActualWorld
        val target = snapshot.targetWorld

        val startNow = actualWorld.timestampMilis

        println("actualWorld: ${actualWorld.withoutLights().printPretty().replace("\\n","\n")}")
        println("previousTarget: ${previousTarget.withoutLights().printPretty()}")

        val now = actualWorld.timestampMilis + 1
        val loop = getLoopFunction()

        // when
        println("\n\nfirstTarget\n\n")
        val firstTarget = loop(actualWorld, now, previousTarget, emptyWorld)
        // then
        assertEqualsJson(
                DepoTarget(
                        wristPosition = Wrist.WristTargets(
                                Claw.ClawTarget.Gripping,
                                Claw.ClawTarget.Gripping
                        ),
                        lift = Lift.TargetLift(Lift.LiftPositions.TargetClearForArmToMove),
                        armPosition = Arm.ArmTarget(Arm.Positions.ClearLiftMovement),
                        targetType = DepoManager.DepoTargetType.GoingHome
                ),
                firstTarget.targetRobot.depoTarget
        )

        // when
        println("\n\nsecondTarget\n\n")
        val secondActualWorld = actualWorld.copy(
                actualRobot = actualWorld.actualRobot.copy(
                        depoState = actualWorld.actualRobot.depoState.copy(
                                lift = SlideSubsystem.ActualSlideSubsystem(Lift.LiftPositions.ClearForArmToMove.ticks, false, 0, 0, 0.0),
                                armAngleDegrees = Arm.Positions.ClearLiftMovement.angleDegrees
                        )
                )
        )
        val secondTarget = loop(secondActualWorld, now, firstTarget, actualWorld)
        // then
        assertEqualsJson(
                DepoTarget(
                        wristPosition = Wrist.WristTargets(
                                Claw.ClawTarget.Gripping,
                                Claw.ClawTarget.Gripping
                        ),
                        lift = Lift.TargetLift(Lift.LiftPositions.TargetClearForArmToMove),
                        armPosition = Arm.ArmTarget(Arm.Positions.Out),
                        targetType = DepoManager.DepoTargetType.GoingHome
                ),
                secondTarget.targetRobot.depoTarget
        )
    }


    fun <T>assertEqualsJson(expected:T, actual:T){
        val writer = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        Assert.assertEquals(writer.writeValueAsString(expected), writer.writeValueAsString(actual))
        Assert.assertEquals(expected, actual)
    }


    fun getLoopFunction(): (actual: ActualWorld,
                            now: Long,
                            previousTargetWorld: TargetWorld,
                            previousActualWorld: ActualWorld,
    ) -> TargetWorld {
        val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
        val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
        val teleop = RobotTwoTeleOp(opmode.telemetry)

        teleop.init(hardware)

        return { actualWorld, now, previousTargetWorld, previousActualWorld->
            //Set Inputs
            hardware.actualRobot = actualWorld.actualRobot
            teleop.getTime = {now}
            teleop.functionalReactiveAutoRunner.hackSetForTest(previousActualWorld, previousTargetWorld)

            teleop.loop(gamepad1 = actualWorld.actualGamepad1, gamepad2 = actualWorld.actualGamepad2, hardware)

            //Get result
            teleop.functionalReactiveAutoRunner.previousTargetState!!
        }
    }

//    fun runTest(actualWorld:ActualWorld, previousTarget:TargetWorld, now:Long, previousActualWorld: ActualWorld = emptyWorld): TargetWorld {
//        val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
//        val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
//        val teleop = RobotTwoTeleOp(opmode.telemetry)
//
//        teleop.init(hardware)
//
//
//        //Run Once
//        return {
//            //Set Inputs
//            hardware.actualRobot = actualWorld.actualRobot
//            teleop.getTime = {now}
//            teleop.functionalReactiveAutoRunner.hackSetForTest(previousActualWorld, previousTarget)
//
//            teleop.loop(gamepad1 = actualWorld.actualGamepad1, gamepad2 = actualWorld.actualGamepad2, hardware)
//
//            //Get result
//            teleop.functionalReactiveAutoRunner.previousTargetState!!
//        }
//    }
}