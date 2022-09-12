package com.github.paylike.kotlin_engine.viewmodel

/**
 * The engine is one of the following states all the time:
 * - initial state: [WAITING_FOR_INPUT]
 * - the first html response has arrived: [WEBVIEW_CHALLENGE_STARTED]
 * - the html form is rendered: [WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED]
 * - successful TDS flow: [SUCCESS]
 * - in any case of error: [ERROR]
 * - At any point of the payment flow there are error branches to run into.
 */
enum class EngineState {
    WAITING_FOR_INPUT,
    WEBVIEW_CHALLENGE_STARTED,
    WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED,
    SUCCESS,
    ERROR,
}
