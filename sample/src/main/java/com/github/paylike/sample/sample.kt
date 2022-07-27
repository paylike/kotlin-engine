package com.github.paylike.sample

import com.github.paylike.kotlin_engine.engine.PaylikeEngine
import com.github.paylike.kotlin_engine.service.ApiMode
import kotlinx.coroutines.runBlocking

fun main() {
    println("hello paylike engine")
    val engine = PaylikeEngine("kotlinTestId01", ApiMode.TEST)
    runBlocking {
        engine.tokenize("4100000000000000", "111", 12, 2022)
        println(engine.repository.cardRepository)
    }



}
