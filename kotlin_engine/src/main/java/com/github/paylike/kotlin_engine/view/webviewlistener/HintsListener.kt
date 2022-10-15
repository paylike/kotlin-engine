package com.github.paylike.kotlin_engine.view.webviewlistener

import android.webkit.JavascriptInterface
import com.github.paylike.kotlin_engine.model.HintsDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Utility class to listen to the posted messages of the webView, catch the upcoming hints, reset
 * these hints if needed, and provide handler to save the hints to the engine.
 */
class HintsListener(val handler: (hint: List<String>, isReady: Boolean) -> Unit) :
    WebViewJsListener {
    private val hints: MutableList<String> = mutableListOf()
    fun resetHints() {
        hints.clear()
    }
    @JavascriptInterface
    override fun receiveMessage(data: String) {
        if (data == "ready") {
            handler(emptyList(), true)
            return
        }
        hints.addAll(Json.decodeFromString<HintsDto>(data).hints)
        handler(hints, false)
    }
}
