package us.brainstormz.telemetryWizard

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode

//@TeleOp(name="Telemetry Wizard Test", group="Tests")
class TelemetryWizardTest(): LinearOpMode() {
    val console = TelemetryConsole(telemetry)
    val wizard = TelemetryWizard(console, this)

    override fun runOpMode() {

        wizard.newMenu("gameType", "Which kind of game is it?", listOf("Remote" to "alliance", "In-Person" to "startPos"), true)
        wizard.newMenu("alliance", "What alliance are we on?", listOf("Red", "Blue"),"startPos")
        wizard.newMenu("startPos", "Which line are we starting in?", listOf("Closer to you", "Closer to the middle"),"ourWobble")
        wizard.newMenu("ourWobble", "Will we do our wobble", listOf("Yes", "No"), "theirWobble")
        wizard.newMenu("theirWobble", "Will we do our partner's wobble", listOf("Yes", "No"))
//        wizard.newMenu("starterStack", "Will we collect the starter stack", listOf("Yes", "No"))
//        wizard.newMenu("powerShot", "Will we do the power shots?", listOf("Yes", "No"))

        wizard.summonWizardBlocking(gamepad1)

        if (wizard.wasItemChosen("alliance", "Red"))
            console.display(2, "works")

        waitForStart()
    }
}
