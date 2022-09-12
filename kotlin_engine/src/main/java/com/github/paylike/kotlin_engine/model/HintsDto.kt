package com.github.paylike.kotlin_engine.model

import kotlinx.serialization.Serializable

/**
 * Used when hints are gathered from the webview window during TDS Flow. It is needed to define a
 * deserializer for the Json format.
 */
@Serializable data class HintsDto(var hints: List<String> = emptyList())
