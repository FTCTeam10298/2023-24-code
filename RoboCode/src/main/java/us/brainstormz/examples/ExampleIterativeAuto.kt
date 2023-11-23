package us.brainstormz.examples

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.OldIterativeOpMode
import us.brainstormz.OldIterativeOpMode.*

//@Autonomous
class ExampleIterativeAuto/** Change depending on robot */: OpMode() {
    val hardware = ExampleHardware()/** Change depending on robot */
    val iterativeOpMode = OldIterativeOpMode(this)

    /** AUTONOMOUS TASKS */
    var autoTasks: List<AutoTask> = listOf(
            AutoTask(subassemblyTasks = listOf(
                    MyTask(target = 1, requiredForCompletion = true)
            ))
    )

    override fun init() {
        /** INIT PHASE */
        hardware.init(hardwareMap)
        iterativeOpMode.init(autoTasks)
    }

    override fun loop() {
        iterativeOpMode.loop()
    }

    /** Declare new task types for your subassemblies */
    class MyTask(val target: Int, override val requiredForCompletion: Boolean): SubassemblyTask(requiredForCompletion) {
        override val action: () -> Boolean = {
            val isDone = 10 > target
            isDone
        }
    }

}