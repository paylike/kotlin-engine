package com.github.paylike.kotlin_engine.view.utils

/** Utility const val to help the tds flow through the webview interactions. */
const val IframeWatcher: String =
    """
<!DOCTYPE html>
<html>

<head>
    <style>
        body {
            height: 300px;
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
                #iamframe {
                    width: 100%;
                }
    </style>
</head>

<body>
    <div id="iframe-div">
        <iframe id="iamframe">
    </iframe>
    </div>
    <script>
        (function() {
                    function waitForWindowListener() {
                        if (!PaylikeWebviewListener || !window.paylikeListener) {
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
                        PaylikeWebviewListener.receiveMessage(event.data);
                        return;
                    }
                    PaylikeWebviewListener.receiveMessage(JSON.stringify(event.data));
                }
                window.parent.addEventListener ('message', window.paylikeListener);
    </script>
</body>

</html>
"""
