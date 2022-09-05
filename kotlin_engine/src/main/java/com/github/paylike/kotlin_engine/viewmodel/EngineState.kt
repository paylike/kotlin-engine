package com.github.paylike.kotlin_engine.viewmodel

/**
 * [WAITING_FOR_INPUT] - initial state
 * [WEBVIEW_CHALLENGE_STARTED] - the first html response has arrived
 * [WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED] - the html form is rendered
 * [SUCCESS] - successful TDS flow
 * [ERROR] - at any point of the payment flow there are error branches to run into.
 */
enum class EngineState {
    WAITING_FOR_INPUT,
    WEBVIEW_CHALLENGE_STARTED,
    WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED,
    SUCCESS,
    ERROR,
}
