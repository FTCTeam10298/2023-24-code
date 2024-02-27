package us.brainstormz.robotTwo.subsystems.ftcLEDs.FTC_Addons;

import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.util.TypeConversion;
import com.qualcomm.robotcore.util.Util;

@I2cDeviceType
@DeviceProperties(name = "Adafruit I2c to Neopixel", description = "Adafruit I2c to Neopixel Seesaw Bridge", xmlTag = "I2c_to_Neopixel")
public class AdafruitNeopixelSeesaw extends I2cDeviceSynchDevice<I2cDeviceSynch> {

    short buffer_length = 3 * 12; // rgb 12 pixel ring. number of bytes needed.
    byte wOffset;
    byte rOffset;
    byte gOffset;
    byte bOffset;

    ColorOrder type = ColorOrder.NEO_GRB;
    I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create7bit(0x60);
    public AdafruitNeopixelSeesaw(I2cDeviceSynch i2cDeviceSynch, boolean deviceClientIsOwned) {
        super(i2cDeviceSynch, deviceClientIsOwned);
        this.deviceClient.setI2cAddress(ADDRESS_I2C_DEFAULT);


        super.registerArmingStateCallback(false); // Deals with USB cables getting unplugged

        // Sensor starts off disengaged so we can change things like I2C address. Need to engage
        this.deviceClient.engage();
    }

    @Override
    protected boolean doInitialize() {
        init_neopixels();
        return true;
    }
    @Override
    public Manufacturer getManufacturer() {
        return Manufacturer.Adafruit;
    }
    @Override
    public String getDeviceName() {
        return "adafruit i2c neopixel";
    }

    public void setBufferLength(short number_of_pixels){
        this.buffer_length = (short) (number_of_pixels * (wOffset == rOffset ? 3 : 4));
        init_neopixels();
    }

    public void setPixelType(ColorOrder type){
        wOffset = (byte) ((type.val >> 6) & 0b11);
        rOffset = (byte) ((type.val >> 4) & 0b11);
        gOffset = (byte) ((type.val >> 2) & 0b11);
        bOffset = (byte) (type.val & 0b11);
        init_neopixels();
        this.type = type;
    }
    public void init_neopixels(){
        byte[] SPEED_CMD = new byte[2];
        SPEED_CMD[0] = 0x02;
        SPEED_CMD[1] = 0x01;
        write(SPEED_CMD);


        byte[] BUFFER_LENGTH_CMD = new byte[4];
        BUFFER_LENGTH_CMD[0] = (byte) seesawFunctions.NEOPIXEL.bVal;
        BUFFER_LENGTH_CMD[1] = 0x03;
        BUFFER_LENGTH_CMD[2] = (byte) (buffer_length >> 8);
        BUFFER_LENGTH_CMD[3] = (byte) (buffer_length & 0xff);
        write(BUFFER_LENGTH_CMD);

        byte[] PIN_CONTROL_CMD = new byte[3];
        PIN_CONTROL_CMD[0] = (byte) seesawFunctions.NEOPIXEL.bVal;
        PIN_CONTROL_CMD[1] = 0x01;
        PIN_CONTROL_CMD[2] = 0x0f;
        write(PIN_CONTROL_CMD);


        //byte[] TEST_COLOR_CMD = new byte[5];
        //TEST_COLOR_CMD[3] = (byte) 0xFF; // test to emulate other command found in PS testing
        //writeToNeopixelBuffer(TEST_COLOR_CMD);
    }



    private void write(byte[] data){
        this.deviceClient.write(data);
    }

    public void setColor(int WRGB, short location){

        byte red = (byte) ((WRGB >> (8*2)) & 0xfe);
        byte green = (byte) ((WRGB >> (8*1)) & 0xfe);
        byte blue = (byte) ((WRGB >> (8*0)) & 0xfe);
        byte white = (byte) ((WRGB >> (8*3)) & 0xfe);
        byte[] data = new byte[wOffset == rOffset ? 3 : 4];
        data[rOffset] = red;
        data[bOffset] = blue;
        data[gOffset] = green;
        data[wOffset] = rOffset == wOffset ? red : white;

        short mem_location = (short) (location * (wOffset == rOffset ? 3 : 4));

        writeToNeopixelBuffer(
                Util.concatenateByteArrays(
                        TypeConversion.shortToByteArray(mem_location),
                        data
                )
        );
        byte[] show_command = {0x0e, 0x05};
        write(show_command);

    }

    public void setColorRGBW(byte red, byte green, byte blue, byte white, short location){

        byte[] data = new byte[wOffset == rOffset ? 3 : 4];
        data[rOffset] = red;
        data[bOffset] = blue;
        data[gOffset] = green;
        data[wOffset] = rOffset == wOffset ? red : white;

        short mem_location = (short) (location * (wOffset == rOffset ? 3 : 4));

        writeToNeopixelBuffer(
                Util.concatenateByteArrays(
                        TypeConversion.shortToByteArray(mem_location),
                        data
                )
        );
        byte[] show_command = {0x0e, 0x05};
        write(show_command);

    }

    public void writeToNeopixelBuffer(byte[] data){


        byte[] write_to_buffer_command = {0x0e, 0x04};
        byte[] new_data = Util.concatenateByteArrays(write_to_buffer_command, data);
        write(new_data);

    }

    /**
     * @param location bytearray of location to read from.
     * @param numBytes number of bytes to be read
     * @return byte array that was read. likely will need to do a 0xff*(byte array length) bitmask to not get garbage data
     */
    public byte[] readFromLocation(byte[] location, int numBytes){
        write(location);
        //deviceClient.waitForWriteCompletions(I2cWaitControl.ATOMIC);

        return this.deviceClient.read(numBytes);
    }
    public byte[] getHardwareID(){
        byte[] reg = new byte[2];
        byte reg1 = (byte) seesawFunctions.STATUS.bVal;
        byte reg2 = (byte) 0x01;
        reg[0] = reg1;
        reg[1] = reg2;
        return readFromLocation(reg, 1);
    }

    public enum seesawFunctions {
        NEOPIXEL(0x0E),
        STATUS(0x00);

        public final int bVal;
         seesawFunctions(int bVal){
            this.bVal = bVal;
        }
    }

    public enum ColorOrder{
        NEO_RGB((0 << 6) | (0 << 4) | (1 << 2) | (2)),
        NEO_RBG((0 << 6) | (0 << 4) | (2 << 2) | (1)),
        NEO_GRB((1 << 6) | (1 << 4) | (0 << 2) | (2)),
        NEO_GBR((2 << 6) | (2 << 4) | (0 << 2) | (1)),
        NEO_BRG((1 << 6) | (1 << 4) | (2 << 2) | (0)),
        NEO_BGR((2 << 6) | (2 << 4) | (1 << 2) | (0)),

        NEO_WRGB ((0 << 6) | (1 << 4) | (2 << 2) | (3)),
        NEO_WRBG((0 << 6) | (1 << 4) | (3 << 2) | (2)),
        NEO_WGRB((0 << 6) | (2 << 4) | (1 << 2) | (3)),
        NEO_WGBR((0 << 6) | (3 << 4) | (1 << 2) | (2)),
        NEO_WBRG((0 << 6) | (2 << 4) | (3 << 2) | (1)),
        NEO_WBGR((0 << 6) | (3 << 4) | (2 << 2) | (1)),

        NEO_RWGB((1 << 6) | (0 << 4) | (2 << 2) | (3)),
        NEO_RWBG((1 << 6) | (0 << 4) | (3 << 2) | (2)),
        NEO_RGWB((2 << 6) | (0 << 4) | (1 << 2) | (3)),
        NEO_RGBW((3 << 6) | (0 << 4) | (1 << 2) | (2)),
        NEO_RBWG((2 << 6) | (0 << 4) | (3 << 2) | (1)),
        NEO_RBGW((3 << 6) | (0 << 4) | (2 << 2) | (1)),

        NEO_GWRB((1 << 6) | (2 << 4) | (0 << 2) | (3)),
        NEO_GWBR((1 << 6) | (3 << 4) | (0 << 2) | (2)),
        NEO_GRWB((2 << 6) | (1 << 4) | (0 << 2) | (3)),
        NEO_GRBW((3 << 6) | (1 << 4) | (0 << 2) | (2)),
        NEO_GBWR((2 << 6) | (3 << 4) | (0 << 2) | (1)),
        NEO_GBRW((3 << 6) | (2 << 4) | (0 << 2) | (1)),

        NEO_BWRG((1 << 6) | (2 << 4) | (3 << 2) | (0)),
        NEO_BWGR((1 << 6) | (3 << 4) | (2 << 2) | (0)),
        NEO_BRWG((2 << 6) | (1 << 4) | (3 << 2) | (0)),
        NEO_BRGW((3 << 6) | (1 << 4) | (2 << 2) | (0)),
        NEO_BGWR((2 << 6) | (3 << 4) | (1 << 2) | (0)),
        NEO_BGRW((3 << 6) | (2 << 4) | (1 << 2) | (0));
        final int val;
        ColorOrder(int stored_value){
            this.val = stored_value;
        }


    }


}
