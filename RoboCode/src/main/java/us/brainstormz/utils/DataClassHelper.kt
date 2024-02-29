package us.brainstormz.utils

import kotlin.reflect.KProperty1
import kotlin.reflect.full.declaredMemberProperties

object DataClassHelper {
    fun dataClassToString(instance: Any): String {
        val className = instance::class.simpleName
        val tabspace = className?.fold("") { acc, _ -> acc + " " } + " "

        val namedArguments: Map<String?, Any?> = instance::class.declaredMemberProperties.mapIndexed {i, it ->
            it.name to (it as KProperty1<Any, *>).get(instance)
        }.toMap()
        val argumentString = namedArguments.toList().foldIndexed("") { i, acc, it->
            acc + it.first + "= " + it.second + if (i != namedArguments.size-1) ",\n" + tabspace else ""
        }

        return className + "(" + argumentString + ")"
    }
}

open class CleanToStringPrint {
    override fun toString(): String = DataClassHelper.dataClassToString(this)
}