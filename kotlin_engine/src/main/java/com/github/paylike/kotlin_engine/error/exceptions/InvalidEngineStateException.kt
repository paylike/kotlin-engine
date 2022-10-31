package com.github.paylike.kotlin_engine.error.exceptions

import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine

/**
 * Thrown when one of the engine payment functions ( [PaylikeEngine.addEssentialPaymentData],
 * [PaylikeEngine.startPayment], [PaylikeEngine.continuePayment], [PaylikeEngine.finishPayment] ) is
 * unexpectedly invoked in the wrong engine state
 * @see PaylikeEngine
 */
class InvalidEngineStateException(override val message: String) : EngineException()
