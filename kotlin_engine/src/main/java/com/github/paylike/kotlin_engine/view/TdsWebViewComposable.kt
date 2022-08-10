package com.github.paylike.kotlin_engine.view

import android.graphics.Bitmap
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
                    WebView.setWebContentsDebuggingEnabled(true)
                    settings.javaScriptEnabled = true
                    settings.allowContentAccess = true
                    settings.loadWithOverviewMode = true
                    settings.mixedContentMode = 0
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    this.addJavascriptInterface(listener, "Android")
                    loadData("<!DOCTYPE html>\n<html>\n$htmlBody\n</html>", "text/html", "utf-8")

                    //                    settings.defaultTextEncodingName = "utf-8";
                }
            myww
        },
        update = {
            it.loadData(
                "<!DOCTYPE html>\n<html>\n$htmlBody\n</html>",
                "text/html; charset=utf-8",
                "utf-8"
            )
            println("<!DOCTYPE html>\n<html>\n$htmlBody\n</html>")
        },
    )
}

interface JsListener {
    @JavascriptInterface fun receiveMessage(data: String) {}
}
