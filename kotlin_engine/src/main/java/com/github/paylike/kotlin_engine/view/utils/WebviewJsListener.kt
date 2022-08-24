package com.github.paylike.kotlin_engine.view.utils

import android.webkit.JavascriptInterface

/** Utility interface to provide bridge between the webview Javascript and listener. */
interface WebviewJsListener {
    @JavascriptInterface fun receiveMessage(data: String)
}
