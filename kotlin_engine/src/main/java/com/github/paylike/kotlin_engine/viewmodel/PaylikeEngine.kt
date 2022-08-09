package com.github.paylike.kotlin_engine.viewmodel

import com.github.paylike.kotlin_client.PaylikeClient
import com.github.paylike.kotlin_client.domain.dto.payment.request.PaymentData
import com.github.paylike.kotlin_client.domain.dto.payment.request.card.ExpiryDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.card.PaylikeCardDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.integration.PaymentIntegrationDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto
import com.github.paylike.kotlin_client.domain.dto.payment.response.PaylikeClientResponse
import com.github.paylike.kotlin_client.domain.dto.tokenize.request.TokenizeData
import com.github.paylike.kotlin_client.domain.dto.tokenize.request.TokenizeTypes
import com.github.paylike.kotlin_engine.helper.PaylikeEngineError
import com.github.paylike.kotlin_engine.helper.exceptions.InvalidCardNumberException
import com.github.paylike.kotlin_engine.helper.exceptions.InvalidPaymentBodyException
import com.github.paylike.kotlin_engine.model.repository.EngineRepository
import com.github.paylike.kotlin_engine.model.service.ApiMode
import com.github.paylike.kotlin_luhn.PaylikeLuhn
import com.github.paylike.kotlin_money.PaymentAmount
import com.github.paylike.kotlin_request.exceptions.PaylikeException
import java.util.*
import java.util.function.Consumer
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 */
class PaylikeEngine :
    Observable { // TODO if its not working then try https://in-kotlin.com/design-patterns/observer/
    constructor(clientId: String, apiMode: ApiMode) {
        this.clientId = clientId
        this.apiMode = apiMode
    }

    var currentState: EngineState = EngineState.WAITING_FOR_INPUT

    private val error: PaylikeEngineError? = null

    val repository: EngineRepository = EngineRepository()

    private val clientId: String

    private val apiMode: ApiMode

    private val apiService: PaylikeClient = PaylikeClient()

    val log: Consumer<Any> = Consumer { println(it.toString()) }

    suspend fun createPaymentDataDto(cardNumber: String, cvc: String, month: Int, year: Int) {
        if (
            !PaylikeLuhn.isValid(cardNumber) && apiMode == ApiMode.LIVE
        ) { // TODO ez a plusz feltetel kell?
            throw InvalidCardNumberException()
        }
        val paylikeCardDto: PaylikeCardDto
        coroutineScope {
            val cardNumberToken = async {
                apiService.tokenize(TokenizeData(TokenizeTypes.PCN, cardNumber))
            }
            val cvcToken = async { apiService.tokenize(TokenizeData(TokenizeTypes.PCSC, cvc)) }
            paylikeCardDto =
                PaylikeCardDto(cardNumberToken.await(), cvcToken.await(), ExpiryDto(month, year))
        }
        repository.paymentRepository =
            PaymentData(
                card = paylikeCardDto,
                integration = PaymentIntegrationDto(this.clientId),
            ) // TODO make it exception safe
    }

    suspend fun startPayment(paymentAmount: PaymentAmount, paymentTestDto: PaymentTestDto?) {
        if (currentState != EngineState.WAITING_FOR_INPUT) {
            throw Exception("Can't call this function in this state $currentState")
        }
        repository.paymentRepository =
            repository.paymentRepository!!.copy(
                amount = paymentAmount,
                test = paymentTestDto,
            )
        try {
            val response = payment()
            repository.paymentRepository!!.hints =
                repository.paymentRepository!!
                    .hints
                    .union(response.paymentResponse.hints!!)
                    .toList()
            if (response.isHTML) {
                repository.htmlRepository = response.htmlBody
                currentState = EngineState.WEBVIEW_CHALLENGE_REQUIRED
            } else {
                repository.transactionId = response.paymentResponse.transactionId
                currentState = EngineState.SUCCESS
            }
            log.accept(repository.paymentRepository!!.hints) // TODO debuging
        } catch (e: PaylikeException) {
            log.accept("An API exception happened: ${e.code} ${e.cause}")
            currentState = EngineState.ERROR
            // TODO set error
        } catch (e: Exception) {
            log.accept("An internal exception happened: $e")
            currentState = EngineState.ERROR
            // TODO set error
        }
        this.setChanged()
        this.notifyObservers()
    }

    suspend fun continuePayment() {
        try {
            val response: PaylikeClientResponse
            if (repository.paymentRepository != null) {
                response = payment()
                repository.paymentRepository!!.hints =
                    repository.paymentRepository!!
                        .hints
                        .union(response.paymentResponse.hints ?: emptyList())
                        .toList()
            } else {
                throw Exception("Engine does not have required information to continue payment")
            }
            if (response.isHTML) {
                if (currentState == EngineState.WEBVIEW_CHALLENGE_REQUIRED) {
                    repository.htmlRepository = response.htmlBody
                    currentState = EngineState.WEBVIEW_CHALLENGE_STARTED
                } else {
                    throw Exception("Engine state invalid $currentState")
                }
            } else {
                if (!response.paymentResponse.transactionId.isNullOrEmpty()) {
                    repository.transactionId = response.paymentResponse.transactionId
                    currentState = EngineState.SUCCESS
                } else {
                    throw Exception("Unexpected payment challenge failure")
                }
            }
        } catch (e: PaylikeException) {
            log.accept("An API exception happened: ${e.code} ${e.cause}")
            currentState = EngineState.ERROR
            // TODO set error
        } catch (e: Exception) {
            log.accept("An internal exception happened: $e")
            currentState = EngineState.ERROR
            // TODO set error
        }
        this.setChanged()
        this.notifyObservers()
    }

    suspend fun finishPayment() {
        try {
            val response: PaylikeClientResponse
            if (repository.paymentRepository != null) {
                response = payment()
            } else {
                throw Exception("Engine does not have required information to continue payment")
            }
            if (response.isHTML) {
                throw Exception("Should not be HTML anymore")
            } else {
                repository.transactionId = response.paymentResponse.transactionId
                currentState = EngineState.SUCCESS
            }
        } catch (e: PaylikeException) {
            log.accept("An API exception happened: ${e.code} ${e.cause}")
            currentState = EngineState.ERROR
            // TODO set error
        } catch (e: Exception) {
            log.accept("An internal exception happened: $e")
            currentState = EngineState.ERROR
            // TODO set error
        }
        this.setChanged()
        this.notifyObservers()
    }

    fun resetPaymentFlow() {
        repository.paymentRepository = null
        repository.htmlRepository = null
        repository.transactionId = null
        currentState = EngineState.WAITING_FOR_INPUT
    }

    private suspend fun payment(): PaylikeClientResponse {
        val response: PaylikeClientResponse
        coroutineScope {
            response =
                when (apiMode) {
                    ApiMode.LIVE -> {
                        apiService.paymentCreate(repository.paymentRepository!!)
                    }
                    ApiMode.TEST -> {
                        if (repository.paymentRepository!!.test == null) {
                            throw InvalidPaymentBodyException("No PaymentTestDto is provided.")
                        }
                        apiService.paymentCreate(repository.paymentRepository!!)
                    }
                }
        }
        return response
    }

    override fun notifyObservers() {
        notifyObservers(currentState)
    }

    override fun notifyObservers(arg: Any?) {
        super.notifyObservers(arg)
        // TODO do we have to do anything here?
    }
}
