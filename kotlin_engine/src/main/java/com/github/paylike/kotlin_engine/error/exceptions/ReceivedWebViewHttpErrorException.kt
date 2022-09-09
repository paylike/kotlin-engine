package com.github.paylike.kotlin_engine.error.exceptions

import android.webkit.WebResourceResponse

/**
 * Thrown when the webview run into http error.
 */
class ReceivedWebViewHttpErrorException(errorResponse: WebResourceResponse?): WebViewException() {
    override val message: String = if (errorResponse != null) {
        "A webview error has occurred: ${errorResponse.statusCode} Reason: ${errorResponse.reasonPhrase}"
    } else {
        "A webview error has occurred."
    }
}
