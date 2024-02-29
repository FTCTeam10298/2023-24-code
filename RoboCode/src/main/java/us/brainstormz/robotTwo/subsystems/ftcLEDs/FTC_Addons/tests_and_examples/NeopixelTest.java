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


                double redDouble = 0.0; //-> red
                double greenDouble = 255.0; //-> blue
                double blueDouble = 0.0; //-> white
                double whiteDouble = 0.0; //-> green

                ///IS THAT THE BYTE OF '87??

                int redInt = ((int) redDouble);
                int greenInt = ((int) greenDouble);
                int blueInt = ((int) blueDouble);
                int whiteInt = ((int) whiteDouble);

                byte redByte = ((byte) redInt);
                byte greenByte = ((byte) greenInt);
                byte blueByte = ((byte) blueInt);
                byte whiteByte = ((byte) whiteInt);

                double redPUKEDouble = 0.0;
                double greenPUKEDouble = 0.0;
                double bluePUKEDouble = 255.0;
                double whitePUKEDouble = 0.0;

                ///IS THAT THE BYTE OF '87??

                int redPUKEInt = ((int) redPUKEDouble);
                int greenPUKEInt = ((int) greenPUKEDouble);
                int bluePUKEInt = ((int) bluePUKEDouble);
                int whitePUKEInt = ((int) whitePUKEDouble);

                byte redPUKEByte = ((byte) redPUKEInt);
                byte greenPUKEByte = ((byte) greenPUKEInt);
                byte bluePUKEByte = ((byte) bluePUKEInt);
                byte whitePUKEByte = ((byte) whitePUKEInt);

                int WRGB = 0x333333;
//                for (int i = 0; i < 30; i++){
//                    neo.setColor(WRGB, (short) i);
//                }


                for (int i = 0; i < 30; i++) {
                    neo.setColorRGBW(redByte, greenByte, blueByte, whiteByte, ((short) i));
                }
                for (int i = 30; i < 60; i++) {
                    neo.setColorRGBW(redPUKEByte, greenPUKEByte, bluePUKEByte, whitePUKEByte, ((short) i));
                }

//                    neo.setColorRGBW(redPUKEByte, greenPUKEByte, bluePUKEByte, whitePUKEByte, ((short) 0));
//                    neo.setColorRGBW(redByte, greenByte, blueByte, whiteByte, ((short) 1));


                    int red = ((WRGB >> (8 * 2)) & 0xfe);
                    int green = ((WRGB >> (8 * 1)) & 0xfe);
                    int blue = ((WRGB >> (8 * 0)) & 0xfe);
                    int white = ((WRGB >> (8 * 3)) & 0xfe);

                    telemetry.addData("red = ", red);
                    telemetry.addData("green = ", green);
                    telemetry.addData("blue = ", blue);
                    telemetry.addData("white = ", white);


                    telemetry.update();

                }
            }
        }

    public void initialize_opmode(){

        double redZERODouble = 0.0;
        double greenZERODouble = 0.0;
        double blueZERODouble = 0.0;
        double whiteZERODouble = 0.0;

        ///IS THAT THE BYTE OF '87??

        int redZEROInt = ((int) redZERODouble);
        int greenZEROInt = ((int) greenZERODouble);
        int blueZEROInt = ((int) blueZERODouble);
        int whiteZEROInt = ((int) whiteZERODouble);

        byte redZEROByte = ((byte) redZEROInt);
        byte greenZEROByte = ((byte) greenZEROInt);
        byte blueZEROByte = ((byte) blueZEROInt);
        byte whiteZEROByte = ((byte) whiteZEROInt);

        neo = hardwareMap.get(AdafruitNeopixelSeesaw.class, "neopixels");
        neo.setPixelType(AdafruitNeopixelSeesaw.ColorOrder.NEO_WRGB);
        neo.setBufferLength((short) 60);

        for (int i = 0; i <= 100; i++) {
            neo.setColorRGBW(redZEROByte, greenZEROByte, blueZEROByte, whiteZEROByte, ((short) i));
        }
        neo.init_neopixels();



    }
}
