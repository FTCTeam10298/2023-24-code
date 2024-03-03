package us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.PwmControl;

public class pwm_rgbw_led_controller extends pwm_rgb_led_controller {
    private final PwmControl white;
    public pwm_rgbw_led_controller(CRServo red, CRServo green, CRServo blue, CRServo white) {
        super(red, green, blue);
        if (white instanceof PwmControl) {
            this.white = (PwmControl) white;
        }else{
            throw new RuntimeException("white isn't a PwmControl class implementation");
        }
    }

    public void setColor(int rgbw){

        int rgb = (rgbw & 0x00FFFFFF);
        setColor(rgb);
        short w = (short) ((rgbw >> 24) & 0xFF);

    }

    @Override
    public void setLED(boolean on) {
        super.setLED(on);
        if(on){
            this.white.setPwmEnable();
        }else{
            this.white.setPwmDisable();
        }
    }
}
