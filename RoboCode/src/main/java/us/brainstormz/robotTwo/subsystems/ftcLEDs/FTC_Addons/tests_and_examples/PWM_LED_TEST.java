package us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons.tests_and_examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;


import us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons.pwm_rgb_led_controller;

@TeleOp
public class PWM_LED_TEST extends LinearOpMode {
    pwm_rgb_led_controller rgb;
    @Override
    public void runOpMode() throws InterruptedException {

        initialize_opmode();
        waitForStart();
        if (opModeIsActive()) {
            while (opModeIsActive() && !isStopRequested()) {
                rgb.setColor(0xFFFFFF);
            }
        }
    }

    private void initialize_opmode() {

        CRServo red_led = hardwareMap.get(CRServo.class, "red");
        CRServo green_led = hardwareMap.get(CRServo.class, "green");
        CRServo blue_led = hardwareMap.get(CRServo.class, "blue");
        //Servo white_led = hardwareMap.get(Servo.class, "white");


        rgb = new pwm_rgb_led_controller(red_led, green_led, blue_led);
        //rgb.frequency_us = 1000;
    }
}
