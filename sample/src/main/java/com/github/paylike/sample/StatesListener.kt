package com.github.paylike.sample

import androidx.compose.runtime.MutableState
import com.github.paylike.kotlin_engine.error.PaylikeEngineError
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import java.util.*

/**
 * Helper class to help listen to specific engine states along the tds flow
 */
class StatesListener(
    private val hints: MutableState<String>,
    private val transactionId: MutableState<String>,
    private val error: MutableState<PaylikeEngineError?>,
    ) : Observer {
    override fun update(p0: Observable?, p1: Any?) {
        if (p0 is PaylikeEngine) {
            if (p0.repository.paymentRepository != null) {
                hints.value = p0.repository.paymentRepository!!.hints.size.toString()
            } else {
                hints.value = "0"
            }
            transactionId.value = p0.repository.transactionId?: "No transaction id yet"
            error.value = p0.error
        }
        else {
            val observableClassName: String = if (p0 != null) {
                p0::class.simpleName!!
            } else { "Anonymous" }
            throw Exception("StatesListener is listening to something unexpected: $observableClassName")
        }
    }
}
