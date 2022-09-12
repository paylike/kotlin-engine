package com.github.paylike.kotlin_engine.error.exceptions

import com.github.paylike.kotlin_client.domain.dto.payment.request.card.PaylikeCardDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.integration.PaymentIntegrationDto

/**
 * Thrown when the payment data is not initialized properly Any of these fields are missing:
 * [PaylikeCardDto] [PaymentIntegrationDto]
 */
class PaymentDataIsNotInitialized(override val message: String) : EngineException()
