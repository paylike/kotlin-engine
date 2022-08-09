package com.github.paylike.kotlin_engine.model

import kotlinx.serialization.Serializable

@Serializable data class HintsDto(var hints: List<String> = emptyList())
