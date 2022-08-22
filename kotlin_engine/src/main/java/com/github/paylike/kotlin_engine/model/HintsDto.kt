package com.github.paylike.kotlin_engine.model

import kotlinx.serialization.Serializable

/** Sole purpose is to define a deserializer for the caught postMessage from the webView. */
@Serializable data class HintsDto(var hints: List<String> = emptyList())
