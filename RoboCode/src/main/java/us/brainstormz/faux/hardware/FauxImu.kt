package us.brainstormz.faux.hardware

import com.qualcomm.robotcore.hardware.IMU
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference
import org.firstinspires.ftc.robotcore.external.navigation.Orientation
import org.firstinspires.ftc.robotcore.external.navigation.Quaternion
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles

class FauxImu: FauxDevice(), IMU {
    override val printSignature: String = "Imu"

    override fun initialize(parameters: IMU.Parameters?): Boolean {
        printInput("Not yet implemented")
        return false
    }

    override fun resetYaw() {
        printInput("Not yet implemented")
    }

    override fun getRobotYawPitchRollAngles(): YawPitchRollAngles {
        printInput("Not yet implemented")
        return YawPitchRollAngles(AngleUnit.DEGREES, 0.0, 0.0, 0.0, 0)
    }

    override fun getRobotOrientation(reference: AxesReference?, order: AxesOrder?, angleUnit: AngleUnit?): Orientation {
        printInput("Not yet implemented")
        return Orientation()
    }

    override fun getRobotOrientationAsQuaternion(): Quaternion {
        printInput("Not yet implemented")
        return Quaternion()
    }

    override fun getRobotAngularVelocity(angleUnit: AngleUnit?): AngularVelocity {
        printInput("Not yet implemented")
        return AngularVelocity()
    }
}