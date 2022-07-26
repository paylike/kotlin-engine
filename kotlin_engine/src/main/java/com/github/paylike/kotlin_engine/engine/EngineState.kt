package com.github.paylike.kotlin_engine.engine

enum class EngineState {
    WAITING_FOR_INPUT,
    WEBVIEW_CHALLENGE_REQUIRED,
    WEBVIEW_CHALLENGE_STARTED,
    SUCCESS,
    ERROR,
}
