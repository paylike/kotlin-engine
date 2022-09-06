package com.github.paylike.kotlin_engine.view.webviewframe

import android.util.Base64

fun injectIframeContent(to: String): String =
    """
        var iframe = document.getElementById('tdsiframe');
        iframe = iframe.contentWindow || ( iframe.contentDocument.document || iframe.contentDocument);
        iframe.document.open();
        window.iframeContent = `${Base64.encodeToString(to.toByteArray(), Base64.DEFAULT)}`;
        iframe.document.write(window.b64Decoder(window.iframeContent));
        iframe.document.close();
    """
