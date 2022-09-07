package com.github.paylike.kotlin_engine.view

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.paylike.kotlin_engine.BuildConfig
import com.github.paylike.kotlin_engine.error.exceptions.WrongTypeOfObserverUpdateArg
import com.github.paylike.kotlin_engine.view.webviewframe.setIframeContent
import com.github.paylike.kotlin_engine.view.webviewlistener.HintsListener
import com.github.paylike.kotlin_engine.view.webviewlistener.IframeWatcher
import com.github.paylike.kotlin_engine.viewmodel.EngineState
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import kotlinx.coroutines.*
import java.util.*

/** Wrapper class for webview composable and its helper functions */
class PaylikeWebview(private val engine: PaylikeEngine) : Observer {
    private lateinit var webview: WebView
    private val webviewListener = HintsListener { hints, isReady ->
        if (isReady) {
            return@HintsListener
        }
        engine.repository.paymentRepository!!.hints =
            engine.repository.paymentRepository!!.hints.union(hints).toList()
        when (engine.currentState) {
            EngineState.WEBVIEW_CHALLENGE_STARTED -> {
                CoroutineScope(Dispatchers.IO).async { engine.continuePayment() }
            }
            EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED -> {
                CoroutineScope(Dispatchers.IO).async { engine.finishPayment() }
            }
            else -> {}
        }
    }

    /** Initialize the webview class to listen to the engine we provided during instantiation */
    init {
        engine.addObserver(this)
    }

    /**
     * Observer update function overload Sets the visibility and content of the [WebviewComposable]
     * based on the provided [EngineState]
     */
    override fun update(o: Observable?, arg: Any?) {
        if (arg == null || arg !is EngineState) {
            throw WrongTypeOfObserverUpdateArg("The argument we got is ${
                if (arg == null) {
                    "null"
                } else {
                    arg::class.simpleName
                }
            }")
        }
        when (arg) {
            EngineState.WAITING_FOR_INPUT -> {
                webviewListener.resetHints()
            }
            EngineState.WEBVIEW_CHALLENGE_STARTED -> {
                MainScope().launch {
                    webview.post {
                        webview.evaluateJavascript(
                            setIframeContent(to = engine.repository.htmlRepository?: ""),
                            null
                        )
                    }
                }
            }
            EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED -> {
                MainScope().launch {
                    webview.post {
                        webview.evaluateJavascript(
                            setIframeContent(to = engine.repository.htmlRepository?: ""),
                            null
                        )
                    }
                }
            }
            EngineState.SUCCESS -> {
            }
            EngineState.ERROR -> {
            }
        }
    }

    /** Webview composable function to execute the tds flow */
    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun WebviewComposable(
        modifier: Modifier = Modifier,
    ) {
        AndroidView(
            modifier = modifier,
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
                        if (BuildConfig.DEBUG) {
                            WebView.setWebContentsDebuggingEnabled(true)
                        }
                    }
                webview
            },
        )
    }
}
