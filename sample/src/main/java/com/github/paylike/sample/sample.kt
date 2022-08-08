package com.github.paylike.sample

import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import com.github.paylike.kotlin_engine.model.service.ApiMode
import kotlinx.coroutines.runBlocking

fun main() {
    println("hello paylike engine")
    val engine = PaylikeEngine("kotlinTestId01", ApiMode.TEST)
    runBlocking {
        engine.createPaymentDataDto("4012111111111111", "111", 11, 2023)
        println(engine.repository.paymentRepository?.card)
    }



}
