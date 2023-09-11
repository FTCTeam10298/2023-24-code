package us.brainstormz

import com.qualcomm.robotcore.eventloop.opmode.OpMode
import us.brainstormz.hardwareClasses.HardwareClass
import us.brainstormz.telemetryWizard.TelemetryConsole
import us.brainstormz.telemetryWizard.TelemetryWizard

class IterativeOpMode(private val opMode: OpMode) {

    private lateinit var autoTasks: List<AutoTask>
    private lateinit var autoTaskIterator: ListIterator<AutoTask> // FIXME: Duplicate of AutoTaskManager's taskListIterator?  Potential race condition?
    private lateinit var currentTask: AutoTask

    fun init(tasksTodo: List<AutoTask>) {
        autoTasks = tasksTodo
        autoTaskIterator = autoTasks.listIterator()
        currentTask = autoTaskIterator.next()
    }

    private var prevTime = System.currentTimeMillis()
    fun loop() {
        /** AUTONOMOUS  PHASE */
        val dt = System.currentTimeMillis() - prevTime
        prevTime = System.currentTimeMillis()

        val nextDeadlinedTask = findNextDeadlinedTask(autoTasks)
        val nextDeadlineSeconds = nextDeadlinedTask?.startDeadlineSeconds
        val isDeadlineUponUs: Boolean = if (nextDeadlineSeconds != null) nextDeadlineSeconds <= getEffectiveRuntime() else false

        if (isDeadlineUponUs) {
            failSkippedTasks(nextDeadlinedTask!!, autoTasks)
            currentTask = nextDeadlinedTask
        } else {
            if (currentTask.isFinished()) {
                if (autoTaskIterator.hasNext()) {
                    currentTask = autoTaskIterator.next()
                }
//                else {
//                    requestOpModeStop()
//                }
            }
        }

        currentTask.taskStatus = TaskStatus.Running
        if (currentTask.timeStartedSeconds != null) {
            currentTask.timeStartedSeconds = getEffectiveRuntime()
        }

        currentTask.subassemblyTasks?.forEach { subassemblyTask ->
            val isSubTaskComplete = subassemblyTask.action()
            if (isSubTaskComplete)
                subassemblyTask.taskStatus = TaskStatus.Completed
        }

        val isTaskCompleted: Boolean = currentTask.subassemblyTasks?.fold(true) { prevTasksCompleted, subassemblyTask ->
            if (subassemblyTask.requiredForCompletion)
                prevTasksCompleted && subassemblyTask.isFinished()
            else
                prevTasksCompleted
        } ?: true

        if (isTaskCompleted) {
            currentTask.taskStatus = TaskStatus.Completed
            currentTask.timeFinishedSeconds = getEffectiveRuntime()
        }
    }

    open class Task {
        var taskStatus: TaskStatus = TaskStatus.Todo
        fun isFinished() = taskStatus == TaskStatus.Completed || taskStatus == TaskStatus.Failed
    }

    class AutoTask(val subassemblyTasks: List<SubassemblyTask>? = null,
                        val startDeadlineSeconds: Double? = null /* time after start by which this is guaranteed to start */,
                        val timeoutSeconds: Double? = null /* time after which the task will be skipped if not already complete */): Task() {
        var timeStartedSeconds: Double? = null
        var timeFinishedSeconds: Double? = null
    }

    abstract class SubassemblyTask(open val requiredForCompletion: Boolean): Task() {
        abstract val action: ()->Boolean
    }

    enum class TaskStatus {
        Todo,
        Running,
        Failed,
        Completed
    }

    private var tasksWithDeadlines: List<AutoTask> = emptyList()
    private fun findNextDeadlinedTask(tasks: List<AutoTask>): AutoTask? {
        if (tasksWithDeadlines.isEmpty()) {
            tasksWithDeadlines = tasks.filter{ it.startDeadlineSeconds != null && (it.taskStatus != TaskStatus.Completed || it.taskStatus != TaskStatus.Failed)}
            tasksWithDeadlines = tasksWithDeadlines.sortedBy { task ->
                task.startDeadlineSeconds
            }
        }
        tasksWithDeadlines.filter { task -> !task.isFinished() }

        return tasksWithDeadlines.firstOrNull()
    }
    private fun failSkippedTasks(deadlinedTask: AutoTask, tasks: List<AutoTask>) {
        tasks.forEach { task ->
            if (task != deadlinedTask && !task.isFinished()) {
                task.taskStatus = TaskStatus.Failed
                task.timeFinishedSeconds = getEffectiveRuntime()
            }
        }
    }

    private fun getEffectiveRuntime() = opMode.runtime - 2
}