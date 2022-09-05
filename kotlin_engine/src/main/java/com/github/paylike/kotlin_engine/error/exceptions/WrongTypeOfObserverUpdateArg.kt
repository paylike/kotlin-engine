package com.github.paylike.kotlin_engine.error.exceptions

/**
 * Thrown when [com.github.paylike.kotlin_engine.view.PaylikeWebview]
 * does not get [com.github.paylike.kotlin_engine.viewmodel.EngineState] on arg.
 */
class WrongTypeOfObserverUpdateArg(override val message: String) : EngineException()
