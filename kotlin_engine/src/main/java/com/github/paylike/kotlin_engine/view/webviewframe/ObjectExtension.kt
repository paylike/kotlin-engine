package com.github.paylike.kotlin_engine.view.webviewframe

/** Extend object with .entries and .fromEntries functions to support older browsers on android */
const val ObjectExtension =
    """

        Object.prototype.entries = (obj) =>
            Object.keys(obj).reduce((prev, key) => {
                prev.push([key, obj[key]])
                return prev;
            }, [])

        Object.prototype.fromEntries = (entries) => {
            const result = {};
            for (const [key, value] of entries) {
                let coercedKey;
                if (typeof key === 'string' || typeof key === 'symbol') {
                    coercedKey = key;
                } else {
                    coercedKey = String(key);
                }
                Object.defineProperty(result, coercedKey, {
                    value,
                    writeable: true,
                    enumerable: true,
                    configurable: true,
                });
            }
            return result;
        }

"""
