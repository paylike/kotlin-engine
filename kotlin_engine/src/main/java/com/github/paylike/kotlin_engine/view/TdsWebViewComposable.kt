package com.github.paylike.kotlin_engine.view

import android.graphics.Bitmap
import android.util.Base64
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import com.github.paylike.kotlin_engine.viewmodel.EngineState
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import java.lang.Exception
import java.util.*

class PaylikeWebview(private val engine: PaylikeEngine) : Observer {
    private val baseHTML = "<!DOCTYPE html>\n<html>\n<body>\n</body>\n</html>\n"
    val isRendered = mutableStateOf(false)
    private val webviewListener = HintsListener { hints ->
        engine.repository.paymentRepository!!.hints =
            engine.repository.paymentRepository!!.hints.union(hints).toList()
    }
    private lateinit var webview: WebView
    init {
        engine.addObserver(this)
    }
    override fun update(o: Observable?, arg: Any?) {
        if (arg == null || arg !is EngineState) {
            throw Exception("Something's fucky...") // TODO not to leave it like this
        }
        when (arg) {
            EngineState.WAITING_FOR_INPUT -> {
                isRendered.value = false
                webviewListener.resetHints() // TODO is it a good place for that?
            }
            EngineState.WEBVIEW_CHALLENGE_STARTED -> {
                isRendered.value = true
                val base64 =
                    Base64.encodeToString(
                        engine.repository.htmlRepository?.toByteArray(),
                        Base64.DEFAULT
                    )
                webview.loadData(base64, "text/html", "base64")
            }
            EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED -> {
                isRendered.value = true
                webview.loadDataWithBaseURL(
                    "https://b.paylike.io",
                    engine.repository.htmlRepository as String,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
            EngineState.ERROR -> {
                isRendered.value = false
            }
            EngineState.SUCCESS -> {
                isRendered.value = false
            }
        }
    }
    @Composable
    fun WebviewComposable() {
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
                        this.addJavascriptInterface(webviewListener, "Android")
                        settings.javaScriptEnabled = true
                        settings.allowContentAccess = true
                        WebView.setWebContentsDebuggingEnabled(true)
                    }
                webview
            }
        )
    }
}
