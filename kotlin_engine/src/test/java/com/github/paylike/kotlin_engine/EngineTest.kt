package com.github.paylike.kotlin_engine

import com.github.paylike.kotlin_client.domain.dto.tokenize.response.TokenizedResponse
import com.github.paylike.kotlin_engine.engine.PaylikeEngine
import com.github.paylike.kotlin_engine.service.ApiMode
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class EngineTest {
    @Test
    fun tokenizeTest() {
        val engine = PaylikeEngine("testId01", ApiMode.TEST)
        runBlocking {
            engine.tokenize("4100000000000000", "111", 12, 2022)
            assertTrue(engine.repository.cardRepository!!.number.token.isNotEmpty())
            assertEquals(TokenizedResponse::class.java, engine.repository.cardRepository!!.number::class.java)
            assertTrue(engine.repository.cardRepository!!.cvc.token.isNotEmpty())
            assertEquals(TokenizedResponse::class.java, engine.repository.cardRepository!!.cvc::class.java)
            assertEquals(12, engine.repository.cardRepository!!.expiry.month)
            assertEquals(2022, engine.repository.cardRepository!!.expiry.year)
        }
    }
}
