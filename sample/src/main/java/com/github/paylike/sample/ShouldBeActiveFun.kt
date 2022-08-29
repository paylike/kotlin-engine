package com.github.paylike.sample

import com.github.paylike.kotlin_engine.viewmodel.EngineState

fun shouldBeActive(currentState: EngineState): Boolean {
    return currentState === EngineState.WEBVIEW_CHALLENGE_STARTED ||
            currentState === EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED
}
