package com.github.paylike.sample

import androidx.compose.runtime.MutableState
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import java.util.*

class Listener(private val hints: MutableState<String>, private val transactionId: MutableState<String>) :
    Observer {
    override fun update(p0: Observable?, p1: Any?) {
        if (p0 is PaylikeEngine) {
            hints.value = p0.repository.paymentRepository!!.hints.size.toString()
            transactionId.value = p0.repository.transactionId?: "No id.."
        }
    }
}