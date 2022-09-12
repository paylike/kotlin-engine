package com.github.paylike.kotlin_engine.error.exceptions

import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import java.util.*

/**
 * Thrown when any class that implements [Observer] and listens to a [Observable] and [Observable]
 * is not [PaylikeEngine].
 */
class WrongTypeOfObservableListened(
    observer: String,
    observable: String,
) : EngineException() {
    override val message: String = "$observer is listening to something unexpected: $observable"
}
