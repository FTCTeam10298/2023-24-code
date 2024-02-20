package us.brainstormz.utils

import java.io.BufferedReader
import java.io.InputStreamReader

object Utils {

    fun consoleLines(): Sequence<String> {
        val input = BufferedReader(InputStreamReader(System.`in`))
        return generateSequence { input.readLine() }
    }
}