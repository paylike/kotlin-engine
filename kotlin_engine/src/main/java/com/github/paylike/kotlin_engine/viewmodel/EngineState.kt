package com.github.paylike.kotlin_engine.viewmodel

/**
 * [WAITING_FOR_INPUT] initial state, [WEBVIEW_CHALLENGE_STARTED] when the first html response
 * arrived, [WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED] when the html form is rendered, [SUCCESS] self
 * explanatory, [ERROR] at any point of the payment flow there are error branches to run into.
 */
enum class EngineState {
    WAITING_FOR_INPUT,
    WEBVIEW_CHALLENGE_STARTED,
    WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED,
    SUCCESS,
    ERROR,
}
