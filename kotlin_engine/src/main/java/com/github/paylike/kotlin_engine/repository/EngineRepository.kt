package com.github.paylike.kotlin_engine.repository

import com.github.paylike.kotlin_client.domain.dto.payment.request.PaymentData
import com.github.paylike.kotlin_client.domain.dto.payment.request.card.PaylikeCardDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.integration.PaymentIntegrationDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto

data class EngineRepository(
    val clientId: PaymentIntegrationDto,
    var cardRepository: PaylikeCardDto? = null,
    var paymentRepository: PaymentData? = null,
    var htmlRepository: String? = null,
    val hintsRepository: List<String> = mutableListOf(),
    var transactionId: String? = null,
    var testConfig: PaymentTestDto? = null,
)
