package com.github.paylike.kotlin_engine.helper.exceptions

/** Thrown when the TDS flow cannot provide HTML */
class HtmlResponseException(override val message: String) : EngineException()
