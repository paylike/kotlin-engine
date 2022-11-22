package com.github.paylike.kotlin_engine.view.webviewframe

import android.util.Base64
import java.util.regex.Pattern

fun setIframeContent(to: String): String {
    /** Injects Object.prototype.entries and Object.prototype.fromEntries functions to */
    val extendedTo = Pattern.compile("[\n]").matcher(to).replaceFirst(ObjectExtension)

    return """
        var iframe = document.getElementById('tdsiframe');
        iframe = iframe.contentWindow || ( iframe.contentDocument.document || iframe.contentDocument);
        iframe.document.open();
        window.iframeContent = `${Base64.encodeToString(extendedTo.toByteArray(), Base64.DEFAULT)}`;
        iframe.document.write(window.b64Decoder(window.iframeContent));
        iframe.document.close();
    """
}
