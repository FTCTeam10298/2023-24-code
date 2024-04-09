package us.brainstormz.robotTwo.localTests

sealed interface TestResult
data object Passed:TestResult
data class Error(val message:String):TestResult

fun <T>assertEquals(expected:T, actual:T){
    if(expected!=actual){
        throw Exception("Expected $expected but was $actual")
    }
}

fun assertNotNull(value:Any?){
    if(value==null){
        throw Exception("Expected something but was null")
    }
}


object TestRunner {
    fun runTests(tests:Map<String, ()-> TestResult>){
        val outcomes = tests.map {(name, fn)->
            println("Running $name")
            val r = try{
                fn()
            }catch (t:Throwable){
                t.printStackTrace()
                Error("Test threw an exception: $t")
            }
            name to r
        }

        println(" ${outcomes.size} test ran")
        println(" ${outcomes.map{it.second}.filterNot { it == Passed }.size} failures ")
    }
}