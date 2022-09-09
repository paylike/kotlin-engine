package com.github.paylike.kotlin_engine.error.exceptions

import com.github.paylike.kotlin_engine.viewmodel.EngineState
import java.util.*

/**
 * Thrown when [Observer] does not get [EngineState] as [arg].
 */
class WrongTypeOfObserverUpdateArg(val arg: Any?) : EngineException() {
    override val message: String = "The argument we got is ${
        if (arg == null) {
            "null"
        } else {
            arg::class.simpleName
        }
    }"
}
