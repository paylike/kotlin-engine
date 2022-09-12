package com.github.paylike.kotlin_engine.error.exceptions

/** Thrown when the payment body is invalid */
class InvalidPaymentDataException(override val message: String) : EngineException()
