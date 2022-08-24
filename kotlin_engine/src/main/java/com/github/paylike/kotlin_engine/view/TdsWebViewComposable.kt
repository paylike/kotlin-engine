package com.github.paylike.kotlin_engine.view

import android.annotation.SuppressLint
import android.util.Base64
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.github.paylike.kotlin_engine.view.utils.HintsListener
import com.github.paylike.kotlin_engine.view.utils.IframeWatcher
import com.github.paylike.kotlin_engine.viewmodel.EngineState
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import java.util.*
import kotlinx.coroutines.runBlocking

class PaylikeWebview(private val engine: PaylikeEngine) : Observer {
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
            runBlocking {
                when (engine.currentState) {
                    EngineState.WEBVIEW_CHALLENGE_STARTED -> {
                        engine.continuePayment()
                    }
                    EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED -> {
                        engine.finishPayment()
                    }
                    else -> {}
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
        when (arg) {
            EngineState.WAITING_FOR_INPUT -> {
                webviewListener.resetHints()
            }
            EngineState.WEBVIEW_CHALLENGE_STARTED -> {
                val base64 =
                    Base64.encodeToString(
                        engine.repository.htmlRepository?.toByteArray(),
                        Base64.DEFAULT
                    )
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
            EngineState.ERROR -> {}
            EngineState.SUCCESS -> {}
        }
    }
    @SuppressLint("SetJavaScriptEnabled")
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
                        this.addJavascriptInterface(webviewListener, "PaylikeWebviewListener")
                        loadDataWithBaseURL(
                            "https:///b.paylike.io",
                            IframeWatcher,
                            "text/html",
                            "utf-8",
                            null
                        )
                        settings.javaScriptEnabled = true
                        settings.allowContentAccess = true
                        WebView.setWebContentsDebuggingEnabled(true)
                    }
                webview
            }
        )
    }
}
