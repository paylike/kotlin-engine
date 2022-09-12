package com.github.paylike.kotlin_engine.model.repository

import com.github.paylike.kotlin_client.domain.dto.payment.request.PaymentData

/**
 * Engine repository is used to store essential data required for the engine to execute the payment
 * flow [paymentRepository]
 * - Body of the request payment [htmlRepository]
 * - HTMLs acquired during the TDS part of the payment flow [transactionId]
 * - Reference for the created transaction after successful payment transactions
 */
data class EngineRepository(
    /** Payment payload information */
    var paymentRepository: PaymentData? = null,
    /** Payment response information */
    var htmlRepository: String? = null,
    var transactionId: String? = null,
)
