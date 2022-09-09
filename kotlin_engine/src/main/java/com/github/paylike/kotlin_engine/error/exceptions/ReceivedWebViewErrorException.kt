package com.github.paylike.kotlin_engine.error.exceptions

import android.webkit.WebResourceError

/**
 * Thrown when the webview run into an error during loading of web resources.
 */
class ReceivedWebViewErrorException(error: WebResourceError?): WebViewException() {
    override val message: String = if (error != null) {
        "A webview error has occurred: ${error.errorCode} Reason: ${error.description}"
    } else {
        "A webview error has occurred."
    }
}