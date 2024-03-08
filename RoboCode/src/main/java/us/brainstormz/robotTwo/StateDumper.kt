package us.brainstormz.robotTwo

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import us.brainstormz.operationFramework.FunctionalReactiveAutoRunner
import us.brainstormz.utils.runOnDedicatedThread

class StateDumper(val reportingIntervalMillis:Long, val functionalReactiveAutoRunner: FunctionalReactiveAutoRunner<TargetWorld, ActualWorld>){

    private var expensiveTelemetryLines:List<String> = emptyList()

    fun lines() = expensiveTelemetryLines

//        private fun printRefectively(o:Any):String{
//            val clazz = o.javaClass
//
//            return clazz.simpleName + "(" + clazz.declaredFields.map {field ->
//                field.name + " = " + printRefectively(field.get(o))
//            }.joinToString("\n    ", "\n    ") + ")"
//        }

    private fun looksLikeItsStillRunning():Boolean{
        val last = functionalReactiveAutoRunner.whenLastRun
        return last!=null && ((System.currentTimeMillis() - last) < 5000)
    }
    fun start(){
        runOnDedicatedThread("state reporter") {
            val writer = jacksonObjectMapper().writerWithDefaultPrettyPrinter()
            while (looksLikeItsStillRunning()) {
                Thread.sleep(reportingIntervalMillis)
                expensiveTelemetryLines = listOf(
                    "actualState: ${writer.writeValueAsString(functionalReactiveAutoRunner.previousActualState)}\n",
                    "\ntargetState: ${writer.writeValueAsString(functionalReactiveAutoRunner.previousTargetState)}"
                )
            }
        }
    }
}