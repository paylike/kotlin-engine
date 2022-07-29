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
        this.repository = EngineRepository(PaymentIntegrationDto(clientId))
        this.apiService = PaylikeClient()
        this.apiMode = apiMode
        if (this.apiMode == ApiMode.TEST) this.repository.testConfig = PaymentTestDto()
    }

    private var currentState: EngineState = EngineState.WAITING_FOR_INPUT

    private val error: PaylikeEngineError? = null

    val repository: EngineRepository

    private val apiMode: ApiMode

    private val apiService: PaylikeClient

    private val log: Consumer<Any> = Consumer { println(it.toString()) }

    suspend fun tokenize(cardNumber: String, cvc: String, month: Int, year: Int) {
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
        repository.cardRepository = paylikeCardDto
    }

    suspend fun startPayment(paymentData: PaymentData, paymentTestDto: PaymentTestDto?) {
        try {
            val response =
                if (paymentTestDto == null && apiMode == ApiMode.TEST) {
                    payment(paymentData, repository.testConfig)
                } else {
                    payment(paymentData, paymentTestDto)
                }
            repository.paymentRepository = paymentData
            repository.cardRepository = paymentData.card
            repository.hintsRepository.plus(response.paymentResponse.hints)
            repository.testConfig = paymentTestDto
            if (response.isHTML) {
                currentState = EngineState.WEBVIEW_CHALLENGE_REQUIRED
                repository.htmlRepository = response.htmlBody
            } else {
                currentState = EngineState.SUCCESS
                repository.transactionId = response.paymentResponse.transactionId
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
        this.notifyObservers()
    }

    suspend fun continuePayment() {
        try {
            val resp: PaylikeClientResponse
            if (repository.paymentRepository != null) {
                resp = apiService.paymentCreate(repository.paymentRepository!!)
                repository.hintsRepository.plus(resp.paymentResponse.hints)
            } else {
                throw Exception("Engine does not have required information to continue payment")
            }
            if (resp.isHTML) {
                if (currentState == EngineState.WEBVIEW_CHALLENGE_REQUIRED) {
                    currentState = EngineState.WEBVIEW_CHALLENGE_STARTED
                } else {
                    throw Exception("Engine state invalid $currentState")
                }
            } else {
                if (!resp.paymentResponse.transactionId.isNullOrEmpty()) {
                    currentState = EngineState.SUCCESS
                    repository.transactionId = resp.paymentResponse.transactionId
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
        this.notifyObservers()
    }

    suspend fun finishPayment() {
        try {
            val resp: PaylikeClientResponse
            if (repository.cardRepository != null) {
                resp = apiService.paymentCreate(repository.paymentRepository!!)
            } else {
                throw Exception("Engine does not have required information to continue payment")
            }
            if (resp.isHTML) {
                throw Exception("Should not be HTML anymore")
            } else {
                repository.transactionId = resp.paymentResponse.transactionId
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
        this.notifyObservers()
    }

    fun resetPaymentFlow() {
        repository.hintsRepository.clear()
        repository.cardRepository = null
        repository.paymentRepository = null
        repository.htmlRepository = null
        repository.transactionId = null
        repository.testConfig = null
        currentState = EngineState.WAITING_FOR_INPUT
    }

    private suspend fun payment(
        paymentData: PaymentData,
        paymentTestDto: PaymentTestDto?
    ): PaylikeClientResponse {
        val response: PaylikeClientResponse
        coroutineScope {
            response =
                when (apiMode) {
                    ApiMode.LIVE -> {
                        apiService.paymentCreate(paymentData)
                    }
                    ApiMode.TEST -> {
                        if (paymentTestDto == null) {
                            throw InvalidPaymentBodyException("No PaymentTestDto is provided.")
                        }
                        apiService.paymentCreate(paymentData.copy(test = paymentTestDto))
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
