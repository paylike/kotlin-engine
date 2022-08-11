package com.github.paylike.kotlin_engine.view

import android.graphics.Bitmap
import android.util.Base64
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import com.github.paylike.kotlin_engine.model.HintsDto
import com.github.paylike.kotlin_engine.viewmodel.EngineState
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import java.util.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class HintListener(val handler: (hint: List<String>) -> Unit) : JsListener {
    val hints: MutableList<String> = mutableListOf()
    @JavascriptInterface
    override fun receiveMessage(data: String) {
        println("listening to data: $data")
        hints.addAll(Json.decodeFromString<HintsDto>(data).hints)
        handler(hints)
    }
    fun giveHints(): List<String> {
        return hints
    }
}

val baseHTML =
    "<!DOCTYPE html>\n" +
        "<html>\n" +
        "<body>\n" +
        "\n" +
        "<h1>My First Heading</h1>\n" +
        "<p>My first paragraph.</p>\n" +
        "\n" +
        "</body>\n" +
        "</html>"

class PaylikeWebview(
    private val engine: PaylikeEngine,
) {
    private lateinit var webview: WebView
    init {
        engine.addObserver { _, _ ->
            if (engine.currentState === EngineState.WEBVIEW_CHALLENGE_STARTED) {
                webview.loadDataWithBaseURL(
                    "https://b.paylike.io",
                    engine.repository.htmlRepository as String,
                    "text/html",
                    "UTF-8",
                    null
                )
            } else if (engine.currentState === EngineState.WAITING_FOR_INPUT) {
                val base64 = Base64.encodeToString(baseHTML.toByteArray(), Base64.DEFAULT)
                webview.loadData(base64, "text/html", "base64")
            } else {
                val base64 =
                    Base64.encodeToString(
                        engine.repository.htmlRepository?.toByteArray(),
                        Base64.DEFAULT
                    )
                webview.loadData(base64, "text/html", "base64")
            }
        }
    }
    @Composable
    fun WebviewComponent() {
        AndroidView(
            factory = {
                webview =
                    WebView(it).apply {
                        layoutParams =
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        webViewClient =
                            object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    println("Navigating to: ${request?.url.toString()}")
                                    return super.shouldOverrideUrlLoading(view, request)
                                }
                                override fun shouldInterceptRequest(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): WebResourceResponse? {
                                    return super.shouldInterceptRequest(view, request)
                                }
                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: Bitmap?
                                ) {
                                    super.onPageStarted(view, url, favicon)
                                    view?.loadUrl(
                                        "javascript:(function() {" +
                                            "window.parent.addEventListener ('message', function(event) {" +
                                            " Android.receiveMessage(JSON.stringify(event.data));});" +
                                            "})()"
                                    )
                                }
                            }
                        val listener = HintListener { hints ->
                            engine.repository.paymentRepository!!.hints =
                                engine.repository.paymentRepository!!.hints.union(hints).toList()
                        }
                        this.addJavascriptInterface(listener, "Android")
                        var base64: String
                        if (engine.currentState == EngineState.WAITING_FOR_INPUT) {
                            base64 = Base64.encodeToString(baseHTML.toByteArray(), Base64.DEFAULT)
                        } else {
                            base64 =
                                Base64.encodeToString(
                                    engine.repository.htmlRepository?.toByteArray(),
                                    Base64.DEFAULT
                                )
                        }
                        loadData(
                            base64,
                            "text/html",
                            "base64",
                        )
                        settings.javaScriptEnabled = true
                        settings.allowContentAccess = true
                        WebView.setWebContentsDebuggingEnabled(true)
                    }
                webview
            },
            update = {
                if (engine.currentState === EngineState.WEBVIEW_CHALLENGE_STARTED) {
                    it.loadDataWithBaseURL(
                        "https://b.paylike.io",
                        engine.repository.htmlRepository as String,
                        "text/html",
                        "UTF-8",
                        null
                    )
                } else if (engine.currentState === EngineState.WAITING_FOR_INPUT) {
                    val base64 = Base64.encodeToString(baseHTML.toByteArray(), Base64.DEFAULT)
                    it.loadData(base64, "text/html", "base64")
                } else {
                    val base64 =
                        Base64.encodeToString(
                            engine.repository.htmlRepository?.toByteArray(),
                            Base64.DEFAULT
                        )
                    it.loadData(base64, "text/html", "base64")
                }
            },
        )
    }
}

interface JsListener {
    @JavascriptInterface fun receiveMessage(data: String) {}
}
