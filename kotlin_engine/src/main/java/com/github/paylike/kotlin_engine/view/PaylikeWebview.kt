package com.github.paylike.kotlin_engine.view

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.paylike.kotlin_engine.BuildConfig
import com.github.paylike.kotlin_engine.error.exceptions.ReceivedWebViewErrorException
import com.github.paylike.kotlin_engine.error.exceptions.WrongTypeOfObservableListened
import com.github.paylike.kotlin_engine.error.exceptions.WrongTypeOfObserverUpdateArg
import com.github.paylike.kotlin_engine.view.webviewframe.IframeWatcher
import com.github.paylike.kotlin_engine.view.webviewframe.setIframeContent
import com.github.paylike.kotlin_engine.view.webviewlistener.HintsListener
import com.github.paylike.kotlin_engine.viewmodel.EngineState
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import java.util.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/** Wrapper class for webView composable and its helper functions */
class PaylikeWebView(private val engine: PaylikeEngine) : Observer {
    private var shouldRenderWebView: MutableState<Boolean> = mutableStateOf(false)
    private lateinit var webView: WebView

    /** Listens to the postMessages of the webView. */
    private val webViewListener = HintsListener { hints, isReady ->
        if (isReady) {
            webView.post {
                webView.evaluateJavascript(
                    setIframeContent(to = engine.repository.htmlRepository ?: ""),
                    null
                )
            }
            return@HintsListener
        }
        engine.repository.paymentRepository!!.hints =
            engine.repository.paymentRepository!!.hints.union(hints).toList()
        when (engine.currentState) {
            EngineState.WEBVIEW_CHALLENGE_STARTED -> {
                CoroutineScope(Dispatchers.IO).launch { engine.continuePayment() }
            }
            EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED -> {
                CoroutineScope(Dispatchers.IO).launch { engine.finishPayment() }
            }
            else -> {}
        }
    }

    /** Initialize the webView class to listen to the engine we provided during instantiation */
    init {
        engine.addObserver(this)
    }

    /**
     * Observer update function overload Sets the visibility and content of the [WebViewComposable]
     * based on the provided [EngineState]
     */
    override fun update(o: Observable?, arg: Any?) {
        if (o !is PaylikeEngine) {
            throw WrongTypeOfObservableListened(
                observer = this::class.simpleName!!,
                observable =
                    if (o != null) {
                        o::class.simpleName!!
                    } else {
                        "Anonymous"
                    },
            )
        }
        if (arg !is EngineState) {
            throw WrongTypeOfObserverUpdateArg(arg)
        }
        when (arg) {
            EngineState.WAITING_FOR_INPUT -> {
                webViewListener.resetHints()
            }
            EngineState.WEBVIEW_CHALLENGE_STARTED -> {
                shouldRenderWebView.value = true
            }
            EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED -> {
                MainScope().launch {
                    webView.post {
                        webView.evaluateJavascript(
                            setIframeContent(to = engine.repository.htmlRepository ?: ""),
                            null
                        )
                    }
                }
            }
            EngineState.SUCCESS -> {
                shouldRenderWebView.value = false
            }
            EngineState.ERROR -> {
                shouldRenderWebView.value = false
            }
        }
    }

    /** WebView composable function to execute the tds flow */
    @SuppressLint("SetJavaScriptEnabled")
    @Composable
    fun WebViewComposable(modifier: Modifier = Modifier) {
        if (shouldRenderWebView.value) {
            AndroidView(
                modifier = modifier,
                factory = {
                    webView =
                        WebView(it).apply {
                            layoutParams =
                                ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                            webViewClient =
                                object : WebViewClient() {
                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        super.onReceivedError(view, request, error)
                                        CoroutineScope(Dispatchers.IO).launch {
                                            engine.setErrorState(
                                                ReceivedWebViewErrorException(error)
                                            )
                                        }
                                    }
                                }
                            this.addJavascriptInterface(webViewListener, "PaylikeWebViewListener")
                            loadDataWithBaseURL(
                                "https:///b.paylike.io",
                                IframeWatcher,
                                "text/html",
                                "utf-8",
                                null
                            )

                            settings.javaScriptEnabled = true
                            settings.allowContentAccess = true
                            this.isVerticalScrollBarEnabled = true
                            if (BuildConfig.DEBUG) {
                                WebView.setWebContentsDebuggingEnabled(true)
                            }
                        }
                    webView
                }
            )
        } else {
            Box(modifier = Modifier.size(0.dp))
        }
    }
}
