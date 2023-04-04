package com.github.paylike.kotlin_engine

import com.github.paylike.kotlin_client.domain.dto.tokenize.response.TokenizedResponse
import com.github.paylike.kotlin_engine.model.service.ApiMode
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EngineTest {
    @Test
    fun tokenizeTest() {
        val engine = PaylikeEngine("testId01", ApiMode.TEST)
        runBlocking {
            engine.addEssentialPaymentData("4012111111111111", "111", 11, 2023)
            assertTrue(engine.repository.paymentRepository!!.card!!.number.token.isNotEmpty())
            assertEquals(
                TokenizedResponse::class.java,
                engine.repository.paymentRepository!!.card!!.number::class.java
            )
            assertTrue(engine.repository.paymentRepository!!.card!!.cvc.token.isNotEmpty())
            assertEquals(
                TokenizedResponse::class.java,
                engine.repository.paymentRepository!!.card!!.cvc::class.java
            )
            assertEquals(11, engine.repository.paymentRepository!!.card!!.expiry.month)
            assertEquals(2023, engine.repository.paymentRepository!!.card!!.expiry.year)
        }
    }
}
