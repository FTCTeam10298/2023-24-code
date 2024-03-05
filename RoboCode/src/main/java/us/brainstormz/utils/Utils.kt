package us.brainstormz.utils

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.abs

object Utils {

    fun consoleLines(): Sequence<String> {
        val input = BufferedReader(InputStreamReader(System.`in`))
        return generateSequence { input.readLine() }
    }

    fun sqrKeepSign(n: Double): Double {
        return -n * abs(n)
    }
}