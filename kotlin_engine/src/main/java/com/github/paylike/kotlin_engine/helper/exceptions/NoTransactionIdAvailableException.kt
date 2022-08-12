package com.github.paylike.kotlin_engine.helper.exceptions

/**
 * Thrown when the engine did not get to the final part of the flow and does not have a transaction
 * ID to provide
 */
class NoTransactionIdAvailableException(override val message: String) : EngineException()
