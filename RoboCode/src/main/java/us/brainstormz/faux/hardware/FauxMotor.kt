package us.brainstormz.faux.hardware

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorController
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.DcMotorSimple
import com.qualcomm.robotcore.hardware.PIDCoefficients
import com.qualcomm.robotcore.hardware.PIDFCoefficients
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit

abstract class FauxDcMotorSimple: DcMotorSimple, FauxDevice() {
    abstract override val printSignature: String

    final override fun setDirection(direction: DcMotorSimple.Direction?) {
        printInput("setDirection $direction")
    }

    final override fun getDirection(): DcMotorSimple.Direction {
        printInput("Not yet implemented")
        return DcMotorSimple.Direction.FORWARD
    }

    final override fun setPower(power: Double) {
        printInput("setPower $power")
    }

    final override fun getPower(): Double {
        printInput("Not yet implemented")
        return 0.0
    }
}

class FauxMotor: FauxDcMotorSimple(), DcMotor, DcMotorEx {
    override val printSignature = "Motor"

    override fun getMotorType(): MotorConfigurationType {
        printInput("Not yet implemented")
        return MotorConfigurationType.getUnspecifiedMotorType()
    }

    override fun setMotorType(motorType: MotorConfigurationType?) {
        printInput("Not yet implemented")
    }

    override fun getController(): DcMotorController {
        printInput("Not yet implemented")
        return TODO()
    }

    override fun getPortNumber(): Int {
        printInput("Not yet implemented")
        return 0
    }

    override fun setZeroPowerBehavior(zeroPowerBehavior: DcMotor.ZeroPowerBehavior?) {
        printInput("setZeroPowerBehavior $zeroPowerBehavior")
    }

    override fun getZeroPowerBehavior(): DcMotor.ZeroPowerBehavior {
        printInput("Not yet implemented")
        return DcMotor.ZeroPowerBehavior.UNKNOWN
    }

    override fun setPowerFloat() {
        printInput("setPowerFloat")
    }

    override fun getPowerFloat(): Boolean {
        printInput("getPowerFloat")
        return false
    }

    override fun setTargetPosition(position: Int) {
        printInput("Not yet implemented")
    }

    override fun getTargetPosition(): Int {
        printInput("Not yet implemented")
        return 0
    }

    override fun isBusy(): Boolean {
        printInput("Not yet implemented")
        return false
    }

    override fun getCurrentPosition(): Int {
        printInput("getCurrentPosition")
        return 0
    }

    override fun setMode(mode: DcMotor.RunMode?) {
        printInput("setMode $mode")
    }

    override fun getMode(): DcMotor.RunMode {
        printInput("Not yet implemented")
        return DcMotor.RunMode.STOP_AND_RESET_ENCODER
    }

    override fun setMotorEnable() {
        printInput("Not yet implemented")
    }

    override fun setMotorDisable() {
        printInput("Not yet implemented")
    }

    override fun isMotorEnabled(): Boolean {
        printInput("Not yet implemented")
        return false
    }

    override fun setVelocity(angularRate: Double) {
        printInput("Not yet implemented")
    }

    override fun setVelocity(angularRate: Double, unit: AngleUnit?) {
        printInput("Not yet implemented")
    }

    override fun getVelocity(): Double {
        printInput("Not yet implemented")
        return 0.0
    }

    override fun getVelocity(unit: AngleUnit?): Double {
        printInput("Not yet implemented")
        return 0.0
    }

    override fun setPIDCoefficients(mode: DcMotor.RunMode?, pidCoefficients: PIDCoefficients?) {
        printInput("Not yet implemented")
    }

    override fun setPIDFCoefficients(mode: DcMotor.RunMode?, pidfCoefficients: PIDFCoefficients?) {
        printInput("Not yet implemented")
    }

    override fun setVelocityPIDFCoefficients(p: Double, i: Double, d: Double, f: Double) {
        printInput("Not yet implemented")
    }

    override fun setPositionPIDFCoefficients(p: Double) {
        printInput("Not yet implemented")
    }

    override fun getPIDCoefficients(mode: DcMotor.RunMode?): PIDCoefficients {
        printInput("Not yet implemented")
        return PIDCoefficients()
    }

    override fun getPIDFCoefficients(mode: DcMotor.RunMode?): PIDFCoefficients {
        printInput("Not yet implemented")
        return PIDFCoefficients()
    }

    override fun setTargetPositionTolerance(tolerance: Int) {
        printInput("Not yet implemented")
    }

    override fun getTargetPositionTolerance(): Int {
        printInput("Not yet implemented")
        return 0
    }

    override fun getCurrent(unit: CurrentUnit?): Double {
        printInput("getCurrent")
        return 0.0
    }

    override fun getCurrentAlert(unit: CurrentUnit?): Double {
        printInput("Not yet implemented")
        return 0.0
    }

    override fun setCurrentAlert(current: Double, unit: CurrentUnit?) {
        printInput("Not yet implemented")
    }

    override fun isOverCurrent(): Boolean {
        printInput("isOverCurrent")
        return false
    }
}
