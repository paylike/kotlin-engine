package com.github.paylike.kotlin_engine.helper.exceptions

/** Thrown when some function is called in the wrong state */
class InvalidEngineStateException(override val message: String) : EngineException()
