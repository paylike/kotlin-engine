package com.github.paylike.kotlin_engine.helper.exceptions

/** Thrown when the given card number is invalid */
class InvalidCardNumberException(override val message: String) : EngineException()
