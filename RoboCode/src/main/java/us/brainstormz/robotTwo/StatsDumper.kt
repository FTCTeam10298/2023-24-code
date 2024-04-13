import android.app.ActivityManager
import android.content.Context
import android.os.Debug
import us.brainstormz.utils.measured
import us.brainstormz.utils.runOnDedicatedThread

class StatsDumper(val reportingIntervalMillis:Long, context: Context) {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val memInfo = Debug.MemoryInfo()
    val outFoo = ActivityManager.RunningAppProcessInfo()
    val memInfo2 = ActivityManager.MemoryInfo()

    fun dumpRuntimeStats() = measured("dumpRuntimeStats"){
        logRuntimeStat("Runtime.freeMemory", formatBytes(Runtime.getRuntime().freeMemory()))
        logRuntimeStat("Runtime.maxMemory", formatBytes(Runtime.getRuntime().maxMemory()))
        logRuntimeStat("Runtime.totalMemory", formatBytes(Runtime.getRuntime().totalMemory()))

        logRuntimeStat("Debug.getNativeHeapSize", formatBytes(Debug.getNativeHeapSize()))
        logRuntimeStat("Debug.getNativeHeapFreeSize", formatBytes(Debug.getNativeHeapFreeSize()))
        logRuntimeStat("Debug.getNativeHeapAllocatedSize", formatBytes(Debug.getNativeHeapAllocatedSize()))

        Debug.getMemoryInfo(memInfo)
        memInfo.memoryStats.entries.forEach{(key, value) ->
            logRuntimeStat("Debug.memoryStats.$key", value)
        }

//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.native-heap] 5360
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.system] 10936
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.total-swap] 2388
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.graphics] 0
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.java-heap] 43304
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.total-pss] 112800
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.private-other] 19124
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.code] 32572
//        03-07 17:49:02.263  4428  4569 I System.out: [MEMORY_STATS] [Debug.memoryStats.summary.stack] 1504

        ActivityManager.getMyMemoryState(outFoo)
        am.getMemoryInfo(memInfo2)

        logRuntimeStat("ActivityManager.memoryInfo.availMem", formatBytes(memInfo2.availMem))
        logRuntimeStat("ActivityManager.memoryInfo.totalMem", formatBytes(memInfo2.totalMem))
        logRuntimeStat("ActivityManager.memoryInfo.threshold", formatBytes(memInfo2.threshold))
        logRuntimeStat("ActivityManager.memoryInfo.lowMemory", memInfo2.lowMemory)

//        logRuntimeStat("Debug.memoryStats.$key", outFoo.)

        printRuntimeStat("art.gc.gc-count-rate-histogram")
        printRuntimeStat("art.gc.blocking-gc-count-rate-histogram")
        printRuntimeStat("art.gc.blocking-gc-count")
        printRuntimeStat("art.gc.blocking-gc-time")
        printRuntimeStat("art.gc.gc-count")
        printRuntimeStat("art.gc.bytes-freed")
        printRuntimeStat("art.gc.gc-time")
        printRuntimeStat("art.gc.bytes-allocated")
    }


    fun printRuntimeStat(tag:String){
        logRuntimeStat(tag, Debug.getRuntimeStat(tag))
    }
    fun logRuntimeStat(tag:String, value:Any){
        println("[MEMORY_STATS] [$tag] $value")
    }

    fun formatBytes(bytes:Long):String {
        val kbytes = bytes/1000
        val mbytes = kbytes/1000
        return "$bytes bytes (${kbytes}kb, ${mbytes}mb)"
    }

    fun start() {
        runOnDedicatedThread("bot stats thread"){
            while(true){
                Thread.sleep(reportingIntervalMillis)
                dumpRuntimeStats()
            }
        }
    }

}