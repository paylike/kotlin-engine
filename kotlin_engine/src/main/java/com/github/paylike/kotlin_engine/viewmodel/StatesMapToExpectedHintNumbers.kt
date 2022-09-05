package com.github.paylike.kotlin_engine.viewmodel

/**
 * Maps the expected number of hints to the examined state
 */
val StatesMapToExpectedHintNumbers: Map<EngineState, Int> = mapOf(
    EngineState.WAITING_FOR_INPUT to 0,
    EngineState.WEBVIEW_CHALLENGE_STARTED to 6,
    EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED to 8,
    EngineState.SUCCESS to 10, // Overall number of hints to catch is 12 but the last 2 are not saved to the repository
    EngineState.ERROR to 0,
)
