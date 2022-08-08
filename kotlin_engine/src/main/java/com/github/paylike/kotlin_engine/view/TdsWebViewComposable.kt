package com.github.paylike.kotlin_engine.view

import android.graphics.Bitmap
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun TdsWebView(
    htmlBody: String,
    listener: JsListener,
) {
    AndroidView(
        factory = {
            val myww =
                WebView(it).apply {
                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    webViewClient =
                        object : WebViewClient() {
                            override fun shouldInterceptRequest(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): WebResourceResponse? {
                                Log.d("Listener", request?.url.toString())
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
                    this.addJavascriptInterface(listener, "Android")
                    loadData(htmlBody, null, null)
                    settings.javaScriptEnabled = true
                }
            myww
        },
        update = { it.loadData(htmlBody, null, null) },
    )
}

interface JsListener {
    @JavascriptInterface fun receiveMessage(data: String) {}
}
