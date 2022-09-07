package com.github.paylike.sample.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.github.paylike.kotlin_engine.error.PaylikeEngineError
import com.github.paylike.kotlin_engine.model.service.ApiMode
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import com.github.paylike.sample.BuildConfig
import java.util.*

class SampleViewModel: ViewModel(), Observer {
    val paylikeEngine: PaylikeEngine = PaylikeEngine(
        merchantId = BuildConfig.PaylikeMerchantApiKey,
        apiMode = ApiMode.TEST,
    )
    var uiState by mutableStateOf(UiState())
        private set

    override fun update(o: Observable?, arg: Any?) {
        if (o is PaylikeEngine) {
            if (o.repository.paymentRepository != null) {
                uiState.numberOfHints = o.repository.paymentRepository!!.hints.size.toString()
            } else {
                uiState.numberOfHints = "0"
            }
            uiState.transactionId = o.repository.transactionId?: "No transaction id yet"
            uiState.errorInstance = o.error
//            uiState.shouldRenderWebview = arg == (EngineState.WEBVIEW_CHALLENGE_STARTED || arg == EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED)
        }
        else {
            val observableClassName: String = if (o != null) {
                o::class.simpleName!!
            } else { "Anonymous" }
            throw Exception("ViewModel is listening to something unexpected: $observableClassName")
        }
    }
}

data class UiState(
    var numberOfHints: String = "0",
    var transactionId: String = "No Id has arrived yet",
    var errorInstance: PaylikeEngineError? = null,
//    var shouldRenderWebview: Boolean = false,
)
