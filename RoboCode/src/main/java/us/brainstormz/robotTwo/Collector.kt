package us.brainstormz.robotTwo

import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit

class Collector(private val extendoMotorMaster: DcMotorEx, private val extendoMotorSlave: DcMotor) {
    val maxSafeCurrentAmps = 5.5

    init {
        extendoMotorMaster.setCurrentAlert(maxSafeCurrentAmps, CurrentUnit.AMPS)
    }


    fun powerExtendo(power: Double) {
        val allowedPower = if (extendoMotorMaster.isOverCurrent) {
            0.0
        } else {
            power
        }

        extendoMotorMaster.power = allowedPower
        extendoMotorSlave.power = allowedPower
    }
    private fun powerExtendoEncoderStops(power: Double) {
        val currentPosition = extendoMotorMaster.currentPosition.toDouble()
        val allowedPower = power
//        if (currentPosition > RobotTwoHardware.ExtendoPositions.Max.position) {
//            power.coerceAtMost(0.0)
//        } else if (currentPosition < RobotTwoHardware.ExtendoPositions.Min.position) {
//            power.coerceAtLeast(0.0)
//        } else {
//            power
//        }

        extendoMotorMaster.power = allowedPower
        extendoMotorSlave.power = allowedPower
    }

//    private fun moveExtendoTowardPosition(targetPosition: Double) {
//        val currentPosition = hardware.extendoMotorMaster.currentPosition.toDouble()
//        val power = hardware.extendoPositionPID.calcPID(targetPosition, currentPosition)
//        powerExtendo(power)
//    }
}