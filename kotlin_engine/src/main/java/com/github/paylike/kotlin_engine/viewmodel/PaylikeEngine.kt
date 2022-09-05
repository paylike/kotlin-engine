package com.github.paylike.kotlin_engine.viewmodel

import android.util.Log
import com.github.paylike.kotlin_client.PaylikeClient
import com.github.paylike.kotlin_client.domain.dto.payment.request.PaymentData
import com.github.paylike.kotlin_client.domain.dto.payment.request.card.ExpiryDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.card.PaylikeCardDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.integration.PaymentIntegrationDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.plan.PaymentPlanDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto
import com.github.paylike.kotlin_client.domain.dto.payment.request.unplanned.PaymentUnplannedDto
import com.github.paylike.kotlin_client.domain.dto.payment.response.PaylikeClientResponse
import com.github.paylike.kotlin_client.domain.dto.tokenize.request.TokenizeData
import com.github.paylike.kotlin_client.domain.dto.tokenize.request.TokenizeTypes
import com.github.paylike.kotlin_engine.error.PaylikeEngineError
import com.github.paylike.kotlin_engine.error.exceptions.*
import com.github.paylike.kotlin_engine.model.repository.EngineRepository
import com.github.paylike.kotlin_engine.model.service.ApiMode
import com.github.paylike.kotlin_luhn.PaylikeLuhn
import com.github.paylike.kotlin_money.PaymentAmount
import com.github.paylike.kotlin_request.exceptions.PaylikeException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.JsonObject
import java.util.*
import java.util.function.Consumer
import kotlin.reflect.full.superclasses

/** Paylike engine
 * Observable wrapper class to support Paylike transactions towards the API
 */
class PaylikeEngine(private val merchantId: String, private val apiMode: ApiMode) : Observable() {

    val repository: EngineRepository = EngineRepository()

    var currentState: EngineState = EngineState.WAITING_FOR_INPUT
        private set

    var error: PaylikeEngineError? = null
        private set

    private val apiClient: PaylikeClient = PaylikeClient()

    var log: Consumer<Any> = Consumer { Log.e("Engine logger", it.toString()) }

    /**
     * Execute api calls and create the necessary data for the [EngineRepository.paymentRepository]
     * These are:
     * [PaylikeCardDto],
     * [PaymentIntegrationDto]
     * @see <a href="https://github.com/paylike/api-docs">Api Docs</a>
     */
    suspend fun initializePaymentData(cardNumber: String, cvc: String, month: Int, year: Int) {
        try {
            checkValidState(
                validState = EngineState.WAITING_FOR_INPUT,
                callerFun = object {}.javaClass.enclosingMethod?.name!!
            )
            if (apiMode == ApiMode.LIVE && !PaylikeLuhn.isValid(cardNumber)) {
                throw InvalidCardNumberException(
                    "Card number is not valid according to Luhn algorithm"
                )
            }
            val paylikeCardDto: PaylikeCardDto
            coroutineScope {
                val cardNumberToken = async {
                    apiClient.tokenize(TokenizeData(TokenizeTypes.PCN, cardNumber))
                }
                val cvcToken = async { apiClient.tokenize(TokenizeData(TokenizeTypes.PCSC, cvc)) }
                paylikeCardDto =
                    PaylikeCardDto(
                        cardNumberToken.await(),
                        cvcToken.await(),
                        ExpiryDto(month, year)
                    )
            }
            repository.paymentRepository =
                PaymentData(
                    card = paylikeCardDto,
                    integration = PaymentIntegrationDto(this.merchantId),
                )
        } catch (e: Exception) {
            setErrorState(e)
            this.notifyObservers(currentState)
        }
    }

    /**
     * These fields describe the payment characteristics.
     * To set up check the api docs below.
     * @param paymentAmount define a single payment amount
     * @param paymentPlanDataList define reoccurring payments
     * @param paymentUnplannedData define TODO()
     * @see <a href="https://github.com/paylike/api-docs">Api Docs</a>
     */
    fun addPaymentDescriptionData(
        paymentAmount: PaymentAmount? = null,
        paymentPlanDataList: List<PaymentPlanDto>? = null,
        paymentUnplannedData: PaymentUnplannedDto? = null,
        paymentTestData: PaymentTestDto? = null,
    ) {
        try {
        isPaymentDataInitialized()
        repository.paymentRepository =
                repository.paymentRepository!!.copy(
                    amount = paymentAmount,
                    plan = paymentPlanDataList,
                    unplanned = paymentUnplannedData,
                    test = paymentTestData,
                )
        } catch (e: Exception) {
            setErrorState(e)
            this.notifyObservers(currentState)
        }
    }

    /**
     * These field are optional to define.
     * @param textData is a simple text shown on the paylike dashboard
     * @param customData is a custom Json object defined by the user
     * @see <a href="https://github.com/paylike/api-docs">Api Docs</a>
     */
    fun addPaymentAdditionalData(
        textData: String? = null,
        customData: JsonObject? = null,
    ) {
        try {
            isPaymentDataInitialized()
        repository.paymentRepository =
            repository.paymentRepository!!.copy(
                text = textData,
                custom = customData,
            )
        } catch (e: Exception) {
            setErrorState(e)
            this.notifyObservers(currentState)
        }
    }

    /** Start function for a payment flow */
    suspend fun startPayment() {
        try {
            checkValidState(
                validState = EngineState.WAITING_FOR_INPUT,
                callerFun = object {}.javaClass.enclosingMethod?.name!!
            )
            isPaymentDataInitialized()
            isNumberOfHintsRight()
            val response = payment()
            addHintsToRepository(response.paymentResponse.hints)
            if (response.isHTML) {
                repository.htmlRepository = response.htmlBody
                currentState = EngineState.WEBVIEW_CHALLENGE_STARTED
            } else {
                if (!response.paymentResponse.transactionId.isNullOrEmpty()) {
                    repository.transactionId = response.paymentResponse.transactionId
                    currentState = EngineState.SUCCESS
                } else {
                    throw NoTransactionIdAvailableException(
                        "No transactionId or AuthorizationId was found in response."
                    )
                }
            }
        } catch (e: Exception) {
            setErrorState(e)
        } finally {
            this.notifyObservers(currentState)
        }
    }

    /** Private function to implement the flow */
    suspend fun continuePayment() {
        try {
            checkValidState(
                validState = EngineState.WEBVIEW_CHALLENGE_STARTED,
                callerFun = object {}.javaClass.enclosingMethod?.name!!
            )
            isNumberOfHintsRight()
            val response = payment()
            addHintsToRepository(response.paymentResponse.hints)
            if (response.isHTML) {
                repository.htmlRepository = response.htmlBody
                currentState = EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED
            } else {
                if (!response.paymentResponse.transactionId.isNullOrEmpty()) {
                    repository.transactionId = response.paymentResponse.transactionId
                    currentState = EngineState.SUCCESS
                } else {
                    throw NoTransactionIdAvailableException(
                        "No transactionId or AuthorizationId was found in response."
                    )
                }
            }
        } catch (e: Exception) {
            setErrorState(e)
        } finally {
            this.notifyObservers(currentState)
        }
    }

    /** Private function to implement the flow */
    suspend fun finishPayment() {
        try {
            checkValidState(
                validState = EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED,
                callerFun = object {}.javaClass.enclosingMethod?.name!!
            )
            isNumberOfHintsRight()
            val response = payment()
            addHintsToRepository(response.paymentResponse.hints)
            if (response.isHTML) {
                throw HtmlResponseException("Response should not be HTML anymore")
            } else {
                if (!response.paymentResponse.transactionId.isNullOrEmpty()) {
                    repository.transactionId = response.paymentResponse.transactionId
                    currentState = EngineState.SUCCESS
                } else if (!response.paymentResponse.authorizationId.isNullOrEmpty()) {
                    repository.transactionId = response.paymentResponse.authorizationId
                    currentState = EngineState.SUCCESS
                } else {
                    throw NoTransactionIdAvailableException(
                        "No transactionId or AuthorizationId was found in response."
                    )
                }
            }
        } catch (e: Exception) {
            setErrorState(e)
        } finally {
            this.notifyObservers(currentState)
        }
    }

    /** Resets the [repository], [currentState] and [error] */
    fun resetEngineStates() {
        currentState = EngineState.WAITING_FOR_INPUT
        repository.apply {
            paymentRepository = null
            htmlRepository = null
            transactionId = null
        }
        error = null
        this.notifyObservers(currentState)
    }

    /**
     * Checks if the necessary data are all set
     * These are:
     * [PaylikeCardDto]
     * [PaymentIntegrationDto]
     * @throws [PaymentDataIsNotInitialized]
     */
    private fun isPaymentDataInitialized() {
        if (repository.paymentRepository == null) {
            throw InvalidEngineStateException(
                "Payment data is not initialized. "
            )
        }
    }

    /** Checks if we are in the valid state, if not throw exception
     * @throws [InvalidEngineStateException]
     */
    private fun checkValidState(validState: EngineState, callerFun: String) {
        if (currentState != validState) {
            throw InvalidEngineStateException(
                "Can't call $callerFun in this state: $currentState\nThe valid state now is $validState"
            )
        }
    }

    /**
     * Checks the number of hints saved in repository
     * @throws [WrongAmountOfHintsException]
     */
    private fun isNumberOfHintsRight() {
        if (repository.paymentRepository!!.hints.size != StatesMapToExpectedHintNumbers[currentState])
        {
            throw WrongAmountOfHintsException(
                StatesMapToExpectedHintNumbers[currentState]!!,
                repository.paymentRepository!!.hints.size,
            )
        }
    }

    /**
     * Concatenates newly received hints to the repository
     */
    private fun addHintsToRepository(listToAdd: List<String>?) {
        repository.paymentRepository!!.hints =
            repository.paymentRepository!!
                .hints
                .union(listToAdd ?: emptyList())
                .toList()
    }

    /** Internal function to execute api call respecting [ApiMode] state
     * @throws [InvalidPaymentDataException]
     */
    private suspend fun payment(): PaylikeClientResponse {
        if (repository.paymentRepository == null) {
            throw InvalidPaymentDataException(
                "PaymentData is invalid.\n${repository.paymentRepository!!::class.simpleName} is missing."
            )
        }
        if (
            repository.paymentRepository?.integration == null ||
            repository.paymentRepository?.card == null
        ) {
            throw InvalidPaymentDataException("PaymentBody is not valid.")
        }
        if (this.apiMode == ApiMode.TEST && repository.paymentRepository?.test == null) {
            throw InvalidPaymentDataException("PaymentBody is not valid. Test DTO is missing.")
        }
        val response: PaylikeClientResponse
        coroutineScope {
            response =
                when (apiMode) {
                    ApiMode.LIVE -> {
                        apiClient.paymentCreate(repository.paymentRepository!!)
                    }
                    ApiMode.TEST -> {
                        apiClient.paymentCreate(repository.paymentRepository!!)
                    }
                }
        }
        return response
    }

    /** Sets error corresponding to the cause */
    private fun setErrorState(e: Exception) {
        when (e::class.superclasses.first()) {
            PaylikeException::class -> {
                e as PaylikeException
                log.accept("An API exception occurred: ${e.code} ${e.cause}")
                error =
                    PaylikeEngineError(
                        e.message ?: "No exception message is included.",
                        paylikeException = e
                    )
            }
            EngineException::class -> {
                e as EngineException
                log.accept("An engine exception occurred: ${e.message}")
                error =
                    PaylikeEngineError(
                        e.message ?: "No exception message is included.",
                        engineException = e
                    )
            }
            else -> {
                log.accept("A not paylike nor engine exception has occurred: $e")
                error = PaylikeEngineError(e.message ?: "No exception message is included.")
            }
        }
        currentState = EngineState.ERROR
    }

    override fun notifyObservers(arg: Any?) {
        this.setChanged()
        super.notifyObservers(arg)
    }
}
