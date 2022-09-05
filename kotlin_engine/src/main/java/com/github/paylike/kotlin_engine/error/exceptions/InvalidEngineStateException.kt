package com.github.paylike.kotlin_engine.error.exceptions

/** Thrown when one of the engine payment functions (
 * [com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine.createPaymentDataDto],
 * [com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine.startPayment],
 * [com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine.continuePayment],
 * [com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine.finishPayment]
 * )
 * is unexpectedly invoked in the wrong engine state
 * @see com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
 * */
class InvalidEngineStateException(override val message: String) : EngineException()
