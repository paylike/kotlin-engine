package com.github.paylike.kotlin_engine.viewmodel

enum class EngineState {
    WAITING_FOR_INPUT,
    WEBVIEW_CHALLENGE_REQUIRED,
    WEBVIEW_CHALLENGE_STARTED,
    SUCCESS,
    ERROR,
}
