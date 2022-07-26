package com.github.paylike.kotlin_engine

import com.github.paylike.kotlin_client.domain.dto.tokenize.response.TokenizedResponse
import com.github.paylike.kotlin_engine.engine.PaylikeEngine
import com.github.paylike.kotlin_engine.service.ApiMode
import com.github.paylike.kotlin_luhn.PaylikeLuhn
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class EngineTest {
    @Test
    fun tokenizeTest() {
        val engine = PaylikeEngine("testId01", ApiMode.TEST)
        runBlocking {
            val paylikeCardDto = engine.tokenize("4100000000000000", "111", 12, 2022)
            assertTrue(paylikeCardDto.number.token.isNotEmpty())
            assertEquals(TokenizedResponse::class.java, paylikeCardDto.number::class.java)
            assertTrue(paylikeCardDto.cvc.token.isNotEmpty())
            assertEquals(TokenizedResponse::class.java, paylikeCardDto.cvc::class.java)
            assertEquals(12, paylikeCardDto.expiry.month)
            assertEquals(2022, paylikeCardDto.expiry.year)
        }
    }
}
