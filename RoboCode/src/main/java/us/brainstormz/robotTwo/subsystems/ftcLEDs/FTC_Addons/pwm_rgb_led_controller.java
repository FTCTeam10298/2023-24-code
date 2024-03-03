package us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.PwmControl;


public class pwm_rgb_led_controller {
   private PwmControl red;
   private PwmControl green;
   private PwmControl blue;


    /**
     * a range from 500-2500
     */
   public double frequency_us = 500;

    public pwm_rgb_led_controller(CRServo red, CRServo green, CRServo blue){
        if (red instanceof PwmControl) {
            this.red = (PwmControl) red;
        }else{
            throw new RuntimeException("red isn't a PwmControl class implementation");
        }
        if (green instanceof PwmControl) {
            this.green = (PwmControl) green;
        }else{
            throw new RuntimeException("green isn't a PwmControl class implementation");
        }
        if (blue instanceof PwmControl) {
            this.blue = (PwmControl) blue;
        }else{
            throw new RuntimeException("blue isn't a PwmControl class implementation");
        }

    }

    /**
     * @param rgb hex value for colors
     */
    public void setColor(int rgb){

        short r = (short) ((rgb & 0xFF0000) >> 16);
        short g = (short) ((rgb & 0xFF00) >> 8);
        short b = (short) (rgb & 0xFF);

        PwmControl.PwmRange red_range = new PwmControl.PwmRange(frequency_us *((255-r)/255.0), frequency_us *(r/255.0));
        this.red.setPwmRange(red_range);

        PwmControl.PwmRange green_range = new PwmControl.PwmRange(frequency_us *((255-g)/255.0), frequency_us *(g/255.0));
        this.green.setPwmRange(green_range);

        PwmControl.PwmRange blue_range = new PwmControl.PwmRange(frequency_us *((255-b)/255.0), frequency_us *(b/255.0));
        this.blue.setPwmRange(blue_range);




    }

    public void setLED(boolean on){
        if (on){
            red.setPwmEnable();
            blue.setPwmEnable();
            green.setPwmEnable();

        }else{
            red.setPwmDisable();
            green.setPwmDisable();
            blue.setPwmDisable();

        }
    }


}
