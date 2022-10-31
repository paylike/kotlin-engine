package com.github.paylike.kotlin_engine.error.exceptions

import com.github.paylike.kotlin_client.domain.dto.payment.request.card.PaylikeCardDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.integration.PaymentIntegrationDto

/**
 * Thrown when the payment data is not initialized properly Any of these fields are missing:
 * [PaylikeCardDto] [PaymentIntegrationDto]
 */
class EssentialPaymentDataMissing(
    integrationDto: PaymentIntegrationDto?,
    cardDto: PaylikeCardDto?
) : EngineException() {
    override val message: String =
        (if (integrationDto == null) "PaymentIntegrationDto is missing. " else "").plus(
            if (cardDto == null) "PaylikeCardDto is missing" else ""
        )
}
