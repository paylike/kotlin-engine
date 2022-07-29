package com.github.paylike.kotlin_engine.model.repository

import com.github.paylike.kotlin_client.domain.dto.payment.request.PaymentData

data class EngineRepository(
    /** Payment payload information */
    var paymentRepository: PaymentData? = null,
    /** Payment response information */
    var htmlRepository: String? = null,
    var transactionId: String? = null,
)
