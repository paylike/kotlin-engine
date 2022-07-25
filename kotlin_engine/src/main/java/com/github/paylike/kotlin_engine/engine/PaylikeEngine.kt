package com.github.paylike.kotlin_engine

import com.github.paylike.kotlin_client.PaylikeClient
import com.github.paylike.kotlin_client.domain.dto.payment.request.PaymentData
import com.github.paylike.kotlin_client.domain.dto.payment.request.card.PaylikeCardDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.integration.PaymentIntegrationDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto
import com.github.paylike.kotlin_client.domain.dto.payment.response.PaylikeClientResponse
import java.util.function.Consumer

/**
 */
class PaylikeEngine {
    constructor(clientId: String, apiMode: ApiMode) {
        this.repository = EngineRepository(PaymentIntegrationDto(clientId))
        this.apiService = PaylikeClient()
        this.apiMode = apiMode
    }

    private val currentState: EngineState = EngineState.IDLE

    private val error: PaylikeEngineError? = null

    private val repository: EngineRepository

    private val apiMode: ApiMode

    private val apiService: PaylikeClient

    private val log: Consumer<Any> = Consumer { println(it.toString()) }


    fun tokenize(cardNumber: String, cvc: String): PaylikeCardDto {
        return TODO()
    }

    fun createPayment(paymentData: PaymentData, paymentTestDto: PaymentTestDto?): PaylikeClientResponse {
        return TODO()
    }

    fun continuePayment() {

    }

    fun finishPayment() {

    }

    fun restartPayment() {
        
    }
}
