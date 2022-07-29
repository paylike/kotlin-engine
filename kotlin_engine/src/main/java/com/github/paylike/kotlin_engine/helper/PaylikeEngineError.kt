package com.github.paylike.kotlin_engine.helper

import com.github.paylike.kotlin_engine.helper.exceptions.InternalException
import com.github.paylike.kotlin_request.exceptions.PaylikeException

class PaylikeEngineError(
    override val message: String = "",
    val internalException: InternalException?,
    val paylikeException: PaylikeException?,
) : Error() {}
