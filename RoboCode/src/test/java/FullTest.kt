import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.Assert
import org.junit.Test
import us.brainstormz.faux.FauxOpMode
import us.brainstormz.faux.PrintlnTelemetry
import us.brainstormz.robotTwo.ActualWorld
import us.brainstormz.robotTwo.CompleteSnapshot
import us.brainstormz.robotTwo.RobotTwoTeleOp
import us.brainstormz.robotTwo.TargetWorld
import us.brainstormz.robotTwo.localTests.FauxRobotTwoHardware
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Transfer

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

    @Test
    fun `doesn't retract when you release the extendo extend button in non-manual mode`(){

        // given
        val actualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
//        Assert.assertEquals(RobotTwoTeleOp.GamepadControlMode.Normal, previousTarget.driverInput.gamepad1ControlMode)

        // when
        val newTarget = runTest(actualWorld, previousTarget, System.currentTimeMillis())

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
        val newTarget = runTest(actualWorld, previousTarget, now)

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

    fun <T>assertEqualsJson(expected:T, actual:T){
        val writer = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
        Assert.assertEquals(writer.writeValueAsString(expected), writer.writeValueAsString(actual))
        Assert.assertEquals(expected, actual)
    }

    fun runTest(actualWorld:ActualWorld, previousTarget:TargetWorld, now:Long): TargetWorld {
        val opmode = FauxOpMode(telemetry = PrintlnTelemetry())
        val hardware = FauxRobotTwoHardware(opmode = opmode, telemetry = opmode.telemetry)
        val teleop = RobotTwoTeleOp(opmode.telemetry)

        teleop.init(hardware)

        //Set Inputs
        hardware.actualRobot = actualWorld.actualRobot
        teleop.getTime = {now}
        teleop.functionalReactiveAutoRunner.hackSetForTest(previousTarget)

        //Run Once
        teleop.loop(gamepad1 = actualWorld.actualGamepad1, gamepad2 = actualWorld.actualGamepad2, hardware)

        //Get result
        return teleop.functionalReactiveAutoRunner.previousTargetState!!
    }
}