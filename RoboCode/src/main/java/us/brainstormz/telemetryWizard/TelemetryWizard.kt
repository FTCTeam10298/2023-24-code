package us.brainstormz.telemetryWizard

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.Gamepad

class TelemetryWizard(private val console: TelemetryConsole, private val opmode: LinearOpMode?) {

    private val startLine = 2
    private var endLine = 0

//    Menu organization
    private var menuList: List<Menu> = listOf()

    data class Menu(val id: String, val caption: String, val items: List<Pair<String, String?>>, val firstMenu: Boolean = false, var answer: Pair<String, String?>? = null)

    fun newMenu(name: String, caption: String, items: List<String>, nextMenu: String? = null, firstMenu: Boolean = false) {
        var item: List<Pair<String, String?>> = listOf()
        items.forEach{ item += it to nextMenu }

        menuList += Menu(name, caption, item, firstMenu)
    }

    fun newMenu(name: String, caption: String, items: List<Pair<String, String?>>, firstMenu: Boolean = false) {
        menuList += Menu(name, caption, items, firstMenu)
    }

    fun getMenu(id: String?): Menu? = menuList.firstOrNull{ it.id == id }

    fun wasItemChosen(id: String, item: String): Boolean = getMenu(id)?.answer?.first == item

    private fun formatMenu(menu: Menu): List<String> {
        var formattedMenu = listOf(menu.caption + ":\n")

        menu.items.forEach{ formattedMenu += placeCursor(menu.items.indexOf(it)) + it.first }

        return formattedMenu
    }

    private fun displayMenu(formattedMenu: List<String>) {
        formattedMenu.forEachIndexed{ index, action ->
            console.replaceLine(index + startLine, action)
        }
        endLine = formattedMenu.size + startLine
        console.queueToTelemetry()
    }

    private fun placeCursor(option: Int): String {
        return if (option == cursorLine) {
            "-"
        } else {
            " "
        }
    }

    private fun eraseLastMenu() {
        console.clearAll()
    }

//    Gamepad input handler
    private var cursorLine = 0
    private var menuDone = false
    private var keyDown = false

    private fun changeCursorBasedOnDPad(gamepad: Gamepad, currentMenu: Menu) {
        val cursorMax = currentMenu.items.size - 1

        when {
            gamepad.dpad_up && !keyDown -> {keyDown = true; if (cursorLine > 0) cursorLine -= 1} //moves cursor up
            gamepad.dpad_down && !keyDown -> {keyDown = true; if (cursorLine < cursorMax) cursorLine += 1}  //moves cursor down
            gamepad.dpad_right && !keyDown -> {keyDown = true; currentMenu.answer = currentMenu.items[cursorLine]; menuDone = true} //selects option
//            gamepad.dpad_left && !keyDown -> { keyDown = true; currentMenu.answer = currentMenu.items[1]; menuDone = true} //Stops wizard or menu (haven't decided) and sets answers to default
            !gamepad.dpad_up && !gamepad.dpad_down && !gamepad.dpad_right && !gamepad.dpad_left -> keyDown = false
        }
        if (cursorLine >= cursorMax)
            cursorLine = cursorMax
    }

    fun summonWizardBlocking(gamepad: Gamepad) {
        var thisMenu: Menu = menuList.first{ it.firstMenu }

        for (i in (0 .. menuList.size)) {
            if (opmode!!.opModeIsActive())
                break

            menuDone = false

            while (!menuDone && opmode.opModeInInit()) {
                changeCursorBasedOnDPad(gamepad, thisMenu)

                displayMenu(formatMenu(thisMenu))
            }

            eraseLastMenu()

            if (thisMenu.answer?.second !== null)
                thisMenu = getMenu(thisMenu.answer?.second)!!
            else
                break
        }

        console.display(startLine, "Wizard Complete!")
    }

    private fun doMenu(menu: Menu, gamepad: Gamepad) {
        changeCursorBasedOnDPad(gamepad, menu)

        displayMenu(formatMenu(menu))

        eraseLastMenu()
    }

    private var thisMenu: Menu? = null
    fun summonWizard(gamepad: Gamepad): Boolean {
        if (thisMenu == null) {
            thisMenu = menuList.first { it.firstMenu }
            menuDone = false
        }

        if (!menuDone) {
            doMenu(thisMenu!!, gamepad)
        } else {
            eraseLastMenu()

            if (thisMenu!!.answer?.second == null) {
                console.display(startLine, "Wizard Complete!")
                return true
            }

            thisMenu = getMenu(thisMenu!!.answer?.second)!!

            menuDone = false
        }

        return false
    }

    fun getAnswersAsString(): String {
        return menuList.fold("") { acc, menu ->
            acc + "${menu.caption.dropLast(0)}: ${menu.answer?.first}\n"
        }
    }
}
