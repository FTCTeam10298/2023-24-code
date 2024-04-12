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
import us.brainstormz.robotTwo.subsystems.Claw
import us.brainstormz.robotTwo.subsystems.DualMovementModeSubsystem
import us.brainstormz.robotTwo.subsystems.Lift
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


    @Test
    fun `latches should stay open when the lift is going up, even after the color sensor stops seeing the pixel`(){
        val snapshot:CompleteSnapshot = jacksonObjectMapper().readValue("""{"actualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":241.78181818181818,"lift":{"currentPositionTicks":232,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":37,"ticksMovedSinceReset":2632,"currentAmps":3.3930000000000002},"wristAngles":{"leftClawAngleDegrees":111.23636363636362,"rightClawAngleDegrees":104.47272727272727,"left":111.23636363636362,"right":104.47272727272727}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-5,"ticksMovedSinceReset":0,"currentAmps":0.007},"transferState":{"left":{"red":0.0049804687,"green":0.008691407,"blue":0.01171875,"alpha":0.025,"asList":[0.0049804687,0.008691407,0.01171875,0.025]},"right":{"red":0.0044921874,"green":0.007910157,"blue":0.012109376,"alpha":0.023925781,"asList":[0.0044921874,0.007910157,0.012109376,0.023925781]}}},"neopixelState":{"wroteForward":false,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":true,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712957119929},"previousActualWorld":{"actualRobot":{"positionAndRotation":{"x":0.0,"y":0.0,"r":0.0},"depoState":{"armAngleDegrees":241.78181818181818,"lift":{"currentPositionTicks":232,"limitSwitchIsActivated":false,"zeroPositionOffsetTicks":37,"ticksMovedSinceReset":2632,"currentAmps":3.3930000000000002},"wristAngles":{"leftClawAngleDegrees":111.12727272727273,"rightClawAngleDegrees":104.47272727272727,"left":111.12727272727273,"right":104.47272727272727}},"collectorSystemState":{"extendo":{"currentPositionTicks":0,"limitSwitchIsActivated":true,"zeroPositionOffsetTicks":-5,"ticksMovedSinceReset":0,"currentAmps":0.003},"transferState":{"left":{"red":0.0049804687,"green":0.008691407,"blue":0.01171875,"alpha":0.025,"asList":[0.0049804687,0.008691407,0.01171875,0.025]},"right":{"red":0.0044921874,"green":0.007910157,"blue":0.012109376,"alpha":0.023925781,"asList":[0.0044921874,0.007910157,0.012109376,0.023925781]}}},"neopixelState":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}},"aprilTagReadings":[],"actualGamepad1":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"actualGamepad2":{"touchpad":false,"dpad_up":false,"dpad_down":false,"dpad_left":false,"dpad_right":false,"right_stick_x":0.0,"right_stick_y":0.0,"left_stick_x":0.0,"left_stick_y":0.0,"right_bumper":false,"left_bumper":false,"right_trigger":0.0,"left_trigger":0.0,"square":false,"a":false,"x":false,"start":false,"left_stick_button":false,"right_stick_button":false,"y":false,"b":false,"isRumbling":true},"timestampMilis":1712957119896},"targetWorld":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Power","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":-0.0,"movementMode":"Power"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"Manual"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Power"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":0},"right":{"target":"Closed","timeTargetChangedMillis":0}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Manual","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"},"previousActualTarget":{"targetRobot":{"drivetrainTarget":{"targetPosition":{"x":0.0,"y":0.0,"r":0.0},"movementMode":"Power","power":{"x":0.0,"y":0.0,"r":-0.0}},"depoTarget":{"armPosition":{"targetPosition":"ClearLiftMovement","movementMode":"Position","power":0.0},"lift":{"targetPosition":["LiftPositions","SetLine2"],"power":0.0,"movementMode":"Position"},"wristPosition":{"left":"Gripping","right":"Gripping","asMap":{"Left":"Gripping","Right":"Gripping"},"bothOrNull":"Gripping"},"targetType":"GoingOut"},"collectorTarget":{"extendo":{"targetPosition":["ExtendoPositions","Min"],"power":0.0,"movementMode":"Position"},"timeOfEjectionStartMilis":0,"timeOfTransferredMillis":0,"intakeNoodles":"Off","dropDown":{"targetPosition":"Up","movementMode":"Position","power":0.0},"transferSensorState":{"left":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0},"right":{"hasPixelBeenSeen":false,"timeOfSeeingMillis":0}},"latches":{"left":{"target":"Closed","timeTargetChangedMillis":1712957108970},"right":{"target":"Closed","timeTargetChangedMillis":1712957108848}}},"hangPowers":"Holding","launcherPosition":"Holding","lights":{"pattern":{"leftPixel":"Unknown","rightPixel":"Unknown","asList":["Unknown","Unknown"]},"stripTarget":{"wroteForward":true,"pixels":[{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0},{"red":0.0,"blue":0.0,"white":0.0,"green":0.0}]}}},"driverInput":{"bumperMode":"Claws","gamepad1ControlMode":"Normal","gamepad2ControlMode":"Normal","lightInput":"NoInput","depo":"Preset3","depoScoringHeightAdjust":-0.0,"armOverridePower":0.0,"wrist":{"left":"NoInput","right":"NoInput","bothClaws":{"Left":"NoInput","Right":"NoInput"}},"collector":"NoInput","dropdown":"NoInput","dropdownPositionOverride":0.0,"extendo":"NoInput","extendoManualPower":0.0,"hang":"NoInput","launcher":"NoInput","handoff":"NoInput","leftLatch":"NoInput","rightLatch":"NoInput","driveVelocity":{"x":0.0,"y":0.0,"r":-0.0}},"doingHandoff":true,"timeTargetStartedMilis":0,"gamepad1Rumble":"Throb"}}""")

        val actualWorld = snapshot.previousActualWorld!!
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
        val newTarget = runTest(actualWorld, previousTarget, now)

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
        val newTarget = runTest(actualWorld, previousTarget, now)

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
        val actualWorld = actualFromSnapshot.copy(
            actualRobot = actualFromSnapshot.actualRobot.copy(
                depoState = actualFromSnapshot.actualRobot.depoState.copy(
                    wristAngles = Wrist.ActualWrist(
                        leftClawAngleDegrees = 0.0,
                        rightClawAngleDegrees = 0.0
                    )
                )
            )
        )
        val previousTarget = snapshot.previousActualTarget!!
        val now = actualWorld.timestampMilis + 1

        // when
        val newTarget = runTest(actualWorld, previousTarget, now)

        // then
        assertEqualsJson(
            Lift.LiftPositions.ClearForArmToMove,
            newTarget.targetRobot.depoTarget.lift.targetPosition)
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