package com.github.paylike.kotlin_engine.view

import android.webkit.JavascriptInterface

interface WebviewJsListener {
    @JavascriptInterface fun receiveMessage(data: String)
}
