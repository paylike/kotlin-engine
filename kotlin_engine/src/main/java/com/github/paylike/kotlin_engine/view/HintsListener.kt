package com.github.paylike.kotlin_engine.view

import android.webkit.JavascriptInterface
import com.github.paylike.kotlin_engine.model.HintsDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class HintsListener(val handler: (hint: List<String>) -> Unit) :
    WebviewJsListener { // TODO reset hint list if we reset engine, companion object of engine?
    private val hints: MutableList<String> = mutableListOf()
    fun resetHints() {
        hints.clear()
    }
    @JavascriptInterface
    override fun receiveMessage(data: String) {
        hints.addAll(Json.decodeFromString<HintsDto>(data).hints)
        handler(hints)
    }
}
