package com.github.paylike.sample.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.github.paylike.kotlin_engine.error.PaylikeEngineError

/**
 * Data class to store variables for the UI logic.
 */
data class UiStateRepository(
    var numberOfHints: MutableState<String> = mutableStateOf("0"),
    var transactionId: MutableState<String> = mutableStateOf("No transaction id yet"),
    var errorInstance: MutableState<PaylikeEngineError?> = mutableStateOf(null),
    var shouldRenderPayButton: MutableState<Boolean> = mutableStateOf(true),
    var shouldRenderResetButton: MutableState<Boolean> = mutableStateOf(false),
)
