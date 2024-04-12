package us.brainstormz.faux.hardware

import com.qualcomm.robotcore.hardware.HardwareDevice
import com.qualcomm.robotcore.hardware.HardwareDeviceHealth
import com.qualcomm.robotcore.hardware.I2cAddr
import com.qualcomm.robotcore.hardware.I2cDeviceSynch
import com.qualcomm.robotcore.hardware.I2cWaitControl
import com.qualcomm.robotcore.hardware.TimestampedData

class FauxI2cDeviceSynch: I2cDeviceSynch, FauxDevice() {
    override fun setHealthStatus(p0: HardwareDeviceHealth.HealthStatus?) {
        
    }

    override fun getHealthStatus(): HardwareDeviceHealth.HealthStatus {
        TODO("Not yet implemented")
    }

    override fun getI2cAddress(): I2cAddr {
        TODO("Not yet implemented")
    }

    override fun setI2cAddress(p0: I2cAddr?) {
        
    }

    override fun setUserConfiguredName(p0: String?) {
        
    }

    override fun getUserConfiguredName(): String? {
        TODO("Not yet implemented")
    }

    override fun read8(): Byte {
        TODO("Not yet implemented")
    }

    override fun read8(p0: Int): Byte {
        TODO("Not yet implemented")
    }

    override fun read(p0: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun read(p0: Int, p1: Int): ByteArray {
        TODO("Not yet implemented")
    }

    override fun readTimeStamped(
        p0: Int,
        p1: Int,
        p2: I2cDeviceSynch.ReadWindow?,
        p3: I2cDeviceSynch.ReadWindow?
    ): TimestampedData {
        TODO("Not yet implemented")
    }

    override fun readTimeStamped(p0: Int): TimestampedData {
        TODO("Not yet implemented")
    }

    override fun readTimeStamped(p0: Int, p1: Int): TimestampedData {
        TODO("Not yet implemented")
    }

    override fun write8(p0: Int) {
        
    }

    override fun write8(p0: Int, p1: Int) {
        
    }

    override fun write8(p0: Int, p1: I2cWaitControl?) {
        
    }

    override fun write8(p0: Int, p1: Int, p2: I2cWaitControl?) {
        
    }

    override fun write(p0: ByteArray?) {
        
    }

    override fun write(p0: Int, p1: ByteArray?) {
        
    }

    override fun write(p0: ByteArray?, p1: I2cWaitControl?) {
        
    }

    override fun write(p0: Int, p1: ByteArray?, p2: I2cWaitControl?) {
        
    }

    override fun waitForWriteCompletions(p0: I2cWaitControl?) {
        
    }

    override fun enableWriteCoalescing(p0: Boolean) {
        
    }

    override fun isWriteCoalescingEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isArmed(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setI2cAddr(p0: I2cAddr?) {
        
    }

    override fun getI2cAddr(): I2cAddr {
        TODO("Not yet implemented")
    }

    override fun setLogging(p0: Boolean) {
        
    }

    override fun getLogging(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setLoggingTag(p0: String?) {
        
    }

    override fun getLoggingTag(): String {
        TODO("Not yet implemented")
    }

    override fun disengage() {
        
    }

    override fun engage() {
        
    }

    override fun isEngaged(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setReadWindow(p0: I2cDeviceSynch.ReadWindow?) {
        
    }

    override fun getReadWindow(): I2cDeviceSynch.ReadWindow {
        TODO("Not yet implemented")
    }

    override fun ensureReadWindow(p0: I2cDeviceSynch.ReadWindow?, p1: I2cDeviceSynch.ReadWindow?) {
        
    }

    override fun setHeartbeatInterval(p0: Int) {
        
    }

    override fun getHeartbeatInterval(): Int {
        TODO("Not yet implemented")
    }

    override fun setHeartbeatAction(p0: I2cDeviceSynch.HeartbeatAction?) {
        
    }

    override fun getHeartbeatAction(): I2cDeviceSynch.HeartbeatAction {
        TODO("Not yet implemented")
    }

    override val printSignature: String = "FauxI2cDeviceSynch"
}