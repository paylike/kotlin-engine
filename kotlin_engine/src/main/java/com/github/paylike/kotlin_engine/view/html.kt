package com.github.paylike.kotlin_engine.view

/// Utility class to help the generation of a standard
/// html structure that can be loaded to the webview
class HTMLService() {
    fun generateWatcher() : String {
        return """
            <!DOCTYPE html><html>
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
                        if (!Android || !window.paylikeListener) {
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
                        Android.receiveMessage(event.data);
                        return;
                    }
                    Android.receiveMessage(JSON.stringify(event.data));
                }                                                
                window.parent.addEventListener ('message', window.paylikeListener);
            </script>
            </body>
            </html>
        """
    }
}
