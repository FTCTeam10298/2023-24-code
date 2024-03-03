package us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons.tests_and_examples;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons.AdafruitNeopixelSeesaw;

@TeleOp
public class NeopixelTest extends LinearOpMode {

    AdafruitNeopixelSeesaw neo;



    @Override
    public void runOpMode() throws InterruptedException {

        initialize_opmode();

        waitForStart();
        if (opModeIsActive()) {
            while (opModeIsActive() && !isStopRequested()) {



                int WRGB = 0x333333;
                for (int i = 0; i < 12; i++){
                    neo.setColor(WRGB, (short) i);
                }


                int red = ((WRGB >> (8*2)) & 0xfe);
                int green = ((WRGB >> (8*1)) & 0xfe);
                int blue = ((WRGB >> (8*0)) & 0xfe);
                int white = ((WRGB >> (8*3)) & 0xfe);

                telemetry.addData("red = ", red);
                telemetry.addData("green = ", green);
                telemetry.addData("blue = ", blue);
                telemetry.addData("white = ", white);


                telemetry.update();

            }
        }
    }

    public void initialize_opmode(){
        neo = hardwareMap.get(AdafruitNeopixelSeesaw.class, "neopixels");
        neo.setPixelType(AdafruitNeopixelSeesaw.ColorOrder.NEO_GRB);
        neo.init_neopixels();

    }
}
