package com.github.paylike.kotlin_engine.view.webviewlistener

import android.webkit.JavascriptInterface

/** Utility interface to provide bridge between the webView Javascript and listener. */
interface WebViewJsListener {
    @JavascriptInterface fun receiveMessage(data: String)
}
