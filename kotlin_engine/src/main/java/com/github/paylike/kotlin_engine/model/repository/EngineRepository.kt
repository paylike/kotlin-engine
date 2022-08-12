package com.github.paylike.kotlin_engine.model.repository

import com.github.paylike.kotlin_client.domain.dto.payment.request.PaymentData

/**
 * Holds consistently [paymentRepository] which holds the payment payload, [htmlRepository] which
 * holds the actual html to render in webView, [transactionId] which is the final response of the
 * api.
 */
data class EngineRepository(
    /** Payment payload information */
    var paymentRepository: PaymentData? = null,
    /** Payment response information */
    var htmlRepository: String? = null,
    var transactionId: String? = null,
)
