package com.github.paylike.sample.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.github.paylike.kotlin_engine.error.exceptions.WrongTypeOfObservableListened
import com.github.paylike.kotlin_engine.error.exceptions.WrongTypeOfObserverUpdateArg
import com.github.paylike.kotlin_engine.model.service.ApiMode
import com.github.paylike.kotlin_engine.viewmodel.EngineState
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import com.github.paylike.sample.BuildConfig
import java.util.*

/**
 * Sample viewModel to handle and store ui logic. Implements [Observer] to be able to listen to the
 * consisting engine.
 */
class SampleViewModel : ViewModel(), Observer {
    /**
     * [paylikeEngine] is set to test mode. You can obtain your own [PaylikeMerchantApiKey] from
     * Paylike and include it in the gradle build files to use it.
     */
    val paylikeEngine: PaylikeEngine =
        PaylikeEngine(
            merchantId = BuildConfig.PaylikeMerchantApiKey,
            apiMode = ApiMode.TEST,
        )

    /** Stores the necessary state variable for the UI logic. */
    var uiState =
        UiStateRepository(
            shouldRenderPayButton =
                mutableStateOf(isWaitingForInputState(paylikeEngine.currentState)),
            shouldRenderResetButton =
                mutableStateOf(isSuccessOrErrorState(paylikeEngine.currentState)),
        )
        private set

    /** Subscribe to the engine */
    init {
        paylikeEngine.addObserver(this)
    }

    /** Private functions to evaluate shouldRender state variables */
    private fun isWaitingForInputState(state: EngineState) = state == EngineState.WAITING_FOR_INPUT
    private fun isSuccessOrErrorState(state: EngineState) =
        (state == EngineState.SUCCESS || state == EngineState.ERROR)

    /** Observer update function overload. Sets the necessary state variables for the UI logic. */
    @Deprecated("Deprecated in Java")
    override fun update(o: Observable?, arg: Any?) {
        if (o !is PaylikeEngine) {
            throw WrongTypeOfObservableListened(
                observer = this::class.simpleName!!,
                observable =
                    if (o != null) {
                        o::class.simpleName!!
                    } else {
                        "Anonymous"
                    },
            )
        }
        if (arg !is EngineState) {
            throw WrongTypeOfObserverUpdateArg(arg)
        }
        if (o.repository.paymentRepository != null) {
            uiState.numberOfHints.value = o.repository.paymentRepository!!.hints.size.toString()
        } else {
            uiState.numberOfHints.value = "0"
        }
        uiState.transactionId.value = o.repository.transactionId ?: "No transaction id yet"
        uiState.errorInstance.value = o.error
        uiState.shouldRenderPayButton.value = isWaitingForInputState(arg)
        uiState.shouldRenderResetButton.value = isSuccessOrErrorState(arg)
    }
}
