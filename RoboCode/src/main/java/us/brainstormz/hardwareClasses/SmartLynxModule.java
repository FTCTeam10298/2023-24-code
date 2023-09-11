package us.brainstormz.hardwareClasses;

import com.qualcomm.hardware.lynx.LynxAnalogInputController;
import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.hardware.lynx.LynxDigitalChannelController;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cConfigureChannelCommand;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorImplEx;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelImpl;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration.I2cChannel;
import com.qualcomm.robotcore.hardware.configuration.LynxI2cDeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.util.HashMap;

public class SmartLynxModule {
    private LynxModule module;

    private LynxDcMotorController motorController;
    private LynxServoController servoController;
    private LynxAnalogInputController lynxAnalogInputController;
    private LynxDigitalChannelController lynxDigitalChannelController;
//    private I2cDeviceSynchDevice lynxI2cSynchDevice;

    private HashMap<Integer, DcMotorEx> cachedMotors;
    private HashMap<Integer, ServoImplEx> cachedServos;
    private HashMap<Integer, CRServoImplEx> cachedCRServos;
    private HashMap<Integer, AnalogInput> cachedAI;
    private HashMap<Integer, DigitalChannel> cachedDC;
//    private HashMap<Integer, I2cChannel> cachedI2c;

    public SmartLynxModule(LynxModule module) {
        this.module = module;
        try {
            motorController = new LynxDcMotorController(AppUtil.getDefContext(), module);
            servoController = new LynxServoController(AppUtil.getDefContext(), module);
            lynxAnalogInputController = new LynxAnalogInputController(AppUtil.getDefContext(), module);
            lynxDigitalChannelController = new LynxDigitalChannelController(AppUtil.getDefContext(), module);
        } catch (RobotCoreException | InterruptedException e) {
            e.printStackTrace();
        }

        cachedMotors = new HashMap<>();
        cachedServos = new HashMap<>();
        cachedCRServos = new HashMap<>();
        cachedAI = new HashMap<>();
        cachedDC = new HashMap<>();
//        cachedI2c = new HashMap<>();
    }

    public DcMotorEx getMotor(int port){
        if(!cachedMotors.containsKey(port)){
            cachedMotors.put(port, new DcMotorImplEx(motorController, port));
        }
        return cachedMotors.get(port);
    }

    public ServoImplEx getServo(int port){
        try{
            if(!cachedServos.containsKey(port)){
                cachedServos.put(port, new ServoImplEx(servoController, port, ServoConfigurationType.getStandardServoType()));
            }
            return cachedServos.get(port);
        }catch(Throwable t){
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }
    public CRServoImplEx getCRServo(int port){
        try{
            if(!cachedCRServos.containsKey(port)){
                cachedCRServos.put(port, new CRServoImplEx(servoController, port, ServoConfigurationType.getStandardServoType()));
            }
            return cachedCRServos.get(port);
        }catch(Throwable t){
            t.printStackTrace();
            throw new RuntimeException(t);
        }
    }

    public AnalogInput getAnalogInput(int port){
        if(!cachedAI.containsKey(port)){
            cachedAI.put(port, new AnalogInput(lynxAnalogInputController, port));
        }
        return cachedAI.get(port);
    }

    public DigitalChannel getDigitalController(int port){
        if(!cachedDC.containsKey(port)){
            cachedDC.put(port, new DigitalChannelImpl(lynxDigitalChannelController, port));
        }
        return cachedDC.get(port);
    }


//    public I2cChannel getI2cController(int port){
//        int channel = 0;
//        if(!cachedI2c.containsKey(port)){
//            cachedI2c.put(port, new I2cChannel(channel));
//        }
//        return cachedI2c.get(port);
//    }
}