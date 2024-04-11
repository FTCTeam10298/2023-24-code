package us.brainstormz.robotTwo.onRobotTests

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import kotlinx.serialization.Serializable
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

    private val inchesToMoveToAccumulateError: Double = 30 *12.0
    private val movementRectangleXInches = 40
    private val movementRectangleYInches = 0
    private val movementAngleDegrees = 0

    private var previousTarget = PositionAndRotation()

    private var previousInchesMovedSinceStart = 0.0
    private var lastActualPosition = PositionAndRotation()

    private var previousGamepad1 = Gamepad()

    private var numberOfFilesInDirectory: Int? = null

    override fun start() {
        numberOfFilesInDirectory = getNumberOfFilesInDirectory(directoryPath)
    }

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
                        r = movementAngleDegrees * Math.random()
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
                saveAsJson(currentPosition, numberOfFilesInDirectory ?: -1)
            }
        }

        telemetry.addLine("test number: $numberOfFilesInDirectory")
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

    val directoryPath = "/storage/emulated/0/Download"
    private fun getNumberOfFilesInDirectory(pathname: String): Int {
        return File(pathname).listFiles().size
    }

    private fun saveAsJson(positionAndRotation: PositionAndRotation, numberOfFilesInDirectory: Int) {
//        val numberOfFilesInDirectory = getNumberOfFilesInDirectory(directoryPath)

        val file = File("$directoryPath/savedData$numberOfFilesInDirectory.json")
        file.createNewFile()
        if (file.exists() && file.isFile) {

            telemetry.addLine("Saving snapshot to: ${file.absolutePath}")

            val json = Json { ignoreUnknownKeys = true }
            val jsonEncoded = json.encodeToString(OdomOffsetDataPoint(
                    positionAndRotation = positionAndRotation,
                    inchesToMoveToAccumulateError = inchesToMoveToAccumulateError,
                    movementRectangleXInches = movementRectangleXInches.toDouble(),
                    movementRectangleYInches = movementRectangleYInches.toDouble(),
                    movementAngleDegrees = movementAngleDegrees.toDouble(),
                    testDescription = "slower"
            ))

            file.printWriter().use {
                it.print(jsonEncoded)
            }
        }
    }
}

@Serializable
data class OdomOffsetDataPoint(
        val positionAndRotation: PositionAndRotation,
        val inchesToMoveToAccumulateError: Double,
        val movementRectangleXInches: Double,
        val movementRectangleYInches: Double,
        val movementAngleDegrees: Double,
        val testDescription: String
) {
    fun getTestIdentifier(): String = "" + inchesToMoveToAccumulateError + movementRectangleXInches + movementRectangleYInches + movementAngleDegrees + testDescription
}


fun main() {
//    val process = Runtime.getRuntime().exec("./Users/jamespenrose/ftc/2023-24-code/RoboCode/src/main/java/us/brainstormz/pullAndroidFiles.sh")
//    val result = process.inputStream.reader().readText()
//    println(result)

    val directoryPath = "/Users/jamespenrose/ftc/odomCalibrate/allData"
    fun sortNewAllFilesIntoFolders(fileToDataPoint: List<Pair<File, OdomOffsetDataPoint?>>) {
        val groupedByTest = fileToDataPoint.groupBy {(file, dataPoint) ->
            dataPoint?.getTestIdentifier()
        }
        groupedByTest.forEach { (testIdentifier, group) ->
            if (testIdentifier!=null) {
                val groupDirectoryPath = "$directoryPath/$testIdentifier"
                val directory = File(groupDirectoryPath)
                if (!directory.isDirectory) {
                    directory.mkdir()
                }

                group.forEachIndexed { i, (file, dataPoint) ->
                    fun getFileToCopyTo(number: Int): File {
                        val fileNumber = number + 1
                        val newFile = File("$groupDirectoryPath/$fileNumber.json")
                        return if (newFile.exists()) {
                            getFileToCopyTo(fileNumber)
                        } else {
                            newFile
                        }
                    }
                    file.copyTo(getFileToCopyTo(i))
                    file.delete()
                }
            }
        }

    }

    fun getOdomOffsetDataPointFromFile(file: File): OdomOffsetDataPoint? {

        val newData = file.reader().readText()

        val json = Json { ignoreUnknownKeys = true }
        return try {
            json.decodeFromString<OdomOffsetDataPoint>(newData)
        } catch (e: Exception) {
            null
        }
    }

    val allFiles = File(directoryPath).listFiles { file, name ->
        name[0] != '.'
    }

    val fileToDataPoint = allFiles.map {file ->
        file to if (file.isFile) {
            val odomOffsetDataPoint = getOdomOffsetDataPointFromFile(file)
            odomOffsetDataPoint
        } else {
            null
        }
    }

    sortNewAllFilesIntoFolders(fileToDataPoint)


    val odomOffsetDataPoint = fileToDataPoint.mapNotNull { (file, dataPoint) ->
        dataPoint
    }
    println("odomOffsetDataPoint: $odomOffsetDataPoint")


    val allPositionAndRotations = odomOffsetDataPoint.map { it.positionAndRotation }

    val summedAbs = allPositionAndRotations.fold(PositionAndRotation()) { acc, positionAndRotation ->
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

    val allX = allPositionAndRotations.map { it.x }
    val allY = allPositionAndRotations.map { it.y }
    val allR = allPositionAndRotations.map { it.r }

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