package com.github.paylike.sample

import androidx.compose.runtime.MutableState
import com.github.paylike.kotlin_engine.helper.PaylikeEngineError
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import java.util.*

class StatesListener(
    private val hints: MutableState<String>,
    private val transactionId: MutableState<String>,
    private val error: MutableState<PaylikeEngineError?>,
    ) : Observer {
    override fun update(p0: Observable?, p1: Any?) {
        if (p0 is PaylikeEngine) {
            hints.value = p0.repository.paymentRepository!!.hints.size.toString()
            transactionId.value = p0.repository.transactionId?: "No id.."
            error.value = p0.error
        }
    }
}
