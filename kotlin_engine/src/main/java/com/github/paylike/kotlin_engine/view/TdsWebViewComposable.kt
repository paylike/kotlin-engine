package com.github.paylike.kotlin_engine.view

import android.graphics.Bitmap
import android.util.Base64
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.runtime.*
import androidx.compose.ui.viewinterop.AndroidView
import com.github.paylike.kotlin_engine.viewmodel.EngineState
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import kotlinx.coroutines.runBlocking
import java.lang.Exception
import java.util.*

class PaylikeWebview(private val engine: PaylikeEngine) : Observer {
    private val baseHTML = "<!DOCTYPE html>\n<html>\n<body>\n</body>\n</html>\n"
    private val webviewListener = HintsListener { hints, isReady ->
        if (isReady) {
            webview.post {
                webview.evaluateJavascript(
                """
                        var iframe = document.getElementById('iamframe');
                        iframe = iframe.contentWindow || ( iframe.contentDocument.document || iframe.contentDocument);
                        iframe.document.open();
                        window.iframeContent = `${Base64.encodeToString(engine.repository.htmlRepository?.toByteArray(), Base64.DEFAULT)}`;
                        iframe.document.write(window.b64Decoder(window.iframeContent));
                        iframe.document.close();
                        """,
                null
                )
            }
        } else {
            engine.repository.paymentRepository!!.hints =
                engine.repository.paymentRepository!!.hints.union(hints).toList()
            if (engine.getCurrentState() == EngineState.WEBVIEW_CHALLENGE_STARTED) {
                runBlocking {
                    engine.continuePayment()
                }
            } else if (engine.getCurrentState() == EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED) {
                runBlocking {
                    engine.finishPayment()
                }
            }
        }
    }
    private lateinit var webview: WebView
    init {
        engine.addObserver(this)
    }
    override fun update(o: Observable?, arg: Any?) {
        if (arg == null || arg !is EngineState) {
            throw Exception("Something's fucky...")
        }
        when (arg as EngineState) {
            EngineState.WAITING_FOR_INPUT -> {
                //                webviewListener.resetHints() TODO
                val base64 = Base64.encodeToString(baseHTML.toByteArray(), Base64.DEFAULT)
                webview.loadData(base64, "text/html", "base64")
            }
            EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED -> {
                webview.post {
                    webview.evaluateJavascript(
                        """
                            var iframe = document.getElementById('iamframe');
                            iframe = iframe.contentWindow || ( iframe.contentDocument.document || iframe.contentDocument);
                            iframe.document.open();
                            window.iframeContent = `${
                                Base64.encodeToString(
                                    engine.repository.htmlRepository?.toByteArray(),
                                    Base64.DEFAULT
                                )
                            }`;
                            iframe.document.write(window.b64Decoder(window.iframeContent));
                            iframe.document.close();
                            """,
                        null
                    )
                }
            }
            EngineState.ERROR -> {
                // TODO
            }
            else -> {
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
                        webViewClient = WebViewClient()
                        this.addJavascriptInterface(webviewListener, "Android")
                        loadDataWithBaseURL(
                            "https:///b.paylike.io",
                            HTMLService().generateWatcher(),
                            "text/html",
                            "utf-8",
                            null
                        )
                        settings.javaScriptEnabled = true
                        settings.allowContentAccess = true
                        WebView.setWebContentsDebuggingEnabled(true)
                    }
                webview
            },
        )
    }
}
