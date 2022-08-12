package com.github.paylike.kotlin_engine.helper.exceptions

/** Thrown when the payment body is invalid */
class InvalidPaymentDataException(override val message: String) : EngineException()
