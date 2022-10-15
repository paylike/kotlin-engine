package com.github.paylike.kotlin_engine.view.webviewframe

/** Utility const val to help the tds flow through the webView interactions. */
const val IframeWatcher: String =
    """
<!DOCTYPE html>
<html>

<head>
    <style>
        body {
            height: 100%;
            width: 100%;
                }
                #iframe-div {
                    position: absolute;
                    top: 0;
                    left: 0;
                    width: 100%;
                    height: 100%;
                    display: flex;
                    justify-content: center;
                }
                #tdsiframe {
                    width: 100%;
                    height: 100%;
                }
    </style>
</head>

<body>
    <div id="iframe-div">
        <iframe id="tdsiframe">
    </iframe>
    </div>
    <script>
        (function() {
                    function waitForWindowListener() {
                        if (!PaylikeWebViewListener || !window.paylikeListener) {
                            setTimeout(waitForWindowListener, 100);
                            return;
                        }
                        window.postMessage("ready");
                    }
                    waitForWindowListener();
                })();
                if (!window.b64Decoder) {
                    window.b64Decoder = (str) =>
                        decodeURIComponent(
                            atob(str)
                            .split("")
                            .map((c) => "%" + ("00" + c.charCodeAt(0).toString(16)).slice(-2))
                            .join("")
                    );
                }
                window.paylikeListener = function(event) {
                    if (event.data == "ready") {
                        PaylikeWebViewListener.receiveMessage(event.data);
                        return;
                    }
                    PaylikeWebViewListener.receiveMessage(JSON.stringify(event.data));
                }
                window.parent.addEventListener ('message', window.paylikeListener);
    </script>
</body>

</html>
"""
