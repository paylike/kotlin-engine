package com.github.paylike.kotlin_engine.error

import com.github.paylike.kotlin_engine.error.exceptions.EngineException
import com.github.paylike.kotlin_request.exceptions.PaylikeException

/** Describes an error state of the engine */
data class PaylikeEngineError(
    val message: String,
    val paylikeException: PaylikeException? = null,
    val engineException: EngineException? = null,
)
