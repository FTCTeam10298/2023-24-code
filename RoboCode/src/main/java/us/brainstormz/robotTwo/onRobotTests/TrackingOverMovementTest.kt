package us.brainstormz.robotTwo.onRobotTests

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import us.brainstormz.localizer.PositionAndRotation
import us.brainstormz.localizer.RRTwoWheelLocalizer
import us.brainstormz.robotTwo.RobotTwoHardware
import us.brainstormz.robotTwo.subsystems.Drivetrain
import java.io.File
import java.lang.Exception
import kotlin.math.absoluteValue
import kotlin.math.hypot

@TeleOp
class TrackingOverMovementTest: OpMode() {
    private val hardware: RobotTwoHardware = RobotTwoHardware(telemetry= telemetry, opmode = this)

    private lateinit var localizer: RRTwoWheelLocalizer
    private lateinit var drivetrain: Drivetrain

    override fun init() {
        hardware.init(hardwareMap)

        localizer = RRTwoWheelLocalizer(hardware, hardware.inchesPerTick)
        drivetrain = Drivetrain(hardware, localizer, telemetry)
    }

    private val inchesToMoveToAccumulateError: Double = 12*12.0
    private val movementRectangleXInches = 20
    private val movementRectangleYInches = 20

    private var previousTarget = PositionAndRotation()

    private var previousInchesMovedSinceStart = 0.0
    private var lastActualPosition = PositionAndRotation()

    private var previousGamepad1 = Gamepad()

    override fun loop() {
        localizer.recalculatePositionAndRotation()
        val currentPosition = localizer.currentPositionAndRotation()

        val inchesMovedSinceLastLoop = hypot(currentPosition.x - lastActualPosition.x, currentPosition.y - lastActualPosition.y)
        val newInchesMovedSinceStart = previousInchesMovedSinceStart + inchesMovedSinceLastLoop

        val atTarget = drivetrain.isRobotAtPosition(previousTarget, currentPosition, precisionInches = 5.0, precisionDegrees = 10.0)
        val newTarget = if (atTarget) {
            val doneWithErrorAccumulationPhase = newInchesMovedSinceStart >= inchesToMoveToAccumulateError

            if (doneWithErrorAccumulationPhase) {
                PositionAndRotation()
            } else {
                PositionAndRotation(
                        x = movementRectangleXInches * Math.random(),
                        y = movementRectangleYInches * Math.random(),
                        r = 180 * Math.random()
                )
            }
        } else {
            previousTarget
        }

        if (!(atTarget && newTarget == PositionAndRotation())) {
            drivetrain.actuateDrivetrain(Drivetrain.DrivetrainTarget(newTarget), Drivetrain.DrivetrainTarget(previousTarget), currentPosition)
        } else {
            drivetrain.powerDrivetrain(Drivetrain.DrivetrainPower())
            telemetry.addLine("drivetrain power 0")

            if (gamepad1.cross && !previousGamepad1.cross) {
                saveAsJson(currentPosition)
            }
        }

        telemetry.addLine("currentPosition: $currentPosition")
        telemetry.addLine("targetPosition: $newTarget")
        val deltaPosition = currentPosition - PositionAndRotation()
        telemetry.addLine("deltaPosition: $deltaPosition")
        telemetry.update()


        previousInchesMovedSinceStart = newInchesMovedSinceStart
        previousTarget = newTarget
        lastActualPosition = currentPosition
        previousGamepad1.copy(gamepad1)
    }

    private fun saveAsJson(info: PositionAndRotation) {
        val directoryPath = "/storage/emulated/0/Download"
        val numberOfFilesInDirectory = File(directoryPath).listFiles().size

        val file = File("$directoryPath/odomSnapshot$numberOfFilesInDirectory.json")
        file.createNewFile()
        if (file.exists() && file.isFile) {

            telemetry.addLine("Saving snapshot to: ${file.absolutePath}")

            val json = Json { ignoreUnknownKeys = true }
            val jsonEncoded = json.encodeToString(info)

            file.printWriter().use {
                it.print(jsonEncoded)
            }
        }
    }
}

fun main() {

    fun getPositionAndRotationFromFile(file: File): PositionAndRotation? {

        val newData = file.reader().readText()

        val json = Json { ignoreUnknownKeys = true }
        return try {
            json.decodeFromString<PositionAndRotation>(newData)
        } catch (e: Exception) {
            null
        }
    }


    val directoryPath = "/Users/jamespenrose/ftc/odomCalibrate/Download"
    val allFiles = File(directoryPath).listFiles { file, name ->
        name[0] != '.'
    }

    val positionAndRotationOrNull = allFiles.map {file ->
        if (file.isFile) {
            val fileName = file.name

            val positionAndRotation = getPositionAndRotationFromFile(file)

            if (positionAndRotation != null) {
                fileName to positionAndRotation
            } else {
                null
            }
        } else {
            null
        }
    }
    println("positionAndRotationOrNull: $positionAndRotationOrNull")

    val allPositionAndRotations = positionAndRotationOrNull.filterNotNull()



    val summedAbs = allPositionAndRotations.fold(PositionAndRotation()) { acc, (name, positionAndRotation) ->
        PositionAndRotation(
                x = acc.x + positionAndRotation.x.absoluteValue,
                y = acc.y + positionAndRotation.y.absoluteValue,
                r = acc.r + positionAndRotation.r.absoluteValue
        )
    }

    val averaged = PositionAndRotation(
            x = summedAbs.x/allPositionAndRotations.size,
            y = summedAbs.y/allPositionAndRotations.size,
            r = summedAbs.r/allPositionAndRotations.size
    )

    println("averaged: $averaged")

    val allX = allPositionAndRotations.map { it.second.x }
    val allY = allPositionAndRotations.map { it.second.y }
    val allR = allPositionAndRotations.map { it.second.r }

    val biggestX = allX.maxBy {
        it.absoluteValue
    }
    val biggestY = allY.maxBy {
        it.absoluteValue
    }
    val biggestR = allR.maxBy {
        it.absoluteValue
    }

    println("biggestX: $biggestX")
    println("biggestY: $biggestY")
    println("biggestR: $biggestR")
}