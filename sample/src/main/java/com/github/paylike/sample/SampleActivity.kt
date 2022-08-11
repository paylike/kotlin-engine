package com.github.paylike.sample

import android.os.Bundle
import android.webkit.JavascriptInterface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto
import com.github.paylike.kotlin_engine.model.HintsDto
import com.github.paylike.kotlin_engine.model.service.ApiMode
import com.github.paylike.kotlin_engine.view.JsListener
import com.github.paylike.kotlin_engine.view.PaylikeWebview
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import com.github.paylike.kotlin_money.PaymentAmount
import com.github.paylike.kotlin_request.exceptions.ServerErrorException
import com.github.paylike.sample.ui.theme.Kotlin_engineTheme
import kotlinx.coroutines.runBlocking


class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngineSampleComposable()
        }
    }
}

@Composable
fun EngineSampleComposable() {
    val engine = PaylikeEngine("e393f9ec-b2f7-4f81-b455-ce45b02d355d", ApiMode.TEST)
    val paylikeWebview = PaylikeWebview(engine)
    val htmlBody: MutableState<String> = remember {
        mutableStateOf("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<body>\n" +
                "\n" +
                "<h1>My First Heading</h1>\n" +
                "<p>My first paragraph.</p>\n" +
                "\n" +
                "</body>\n" +
                "</html>")
    }
    Kotlin_engineTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            LazyColumn(Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.SpaceEvenly) {
                item {
                    Button(
                        onClick = {
                            engine.resetPaymentFlow()
                            runBlocking {
                                try {
                                    engine.createPaymentDataDto("4012111111111111", "111", 11, 2023)
                                    engine.startPayment(
                                        PaymentAmount("EUR", 10, 0),
                                        PaymentTestDto()
                                    )
                                } catch (e: ServerErrorException) {
                                    println("serverErrorException " + e.status.toString())
                                    println("serverErrorException " + e.headers.toString())
                                }
                                if (!engine.repository.htmlRepository.isNullOrEmpty()) {
                                    htmlBody.value = engine.repository.htmlRepository!!
                                    println(htmlBody.value)
                                }
                                println(engine.currentState)
                            }
                        },
                    ) {
                        Text(text = "Pay")
                    }
                }
                item {
                    paylikeWebview.WebviewComponent()
                }
                item {
                    Button(onClick = {
                        runBlocking {
                            engine.continuePayment()
                            if (!engine.repository.htmlRepository.isNullOrEmpty()) {
                                htmlBody.value = engine.repository.htmlRepository!!
                                // println(htmlBody.value)
                            }
                            println(engine.currentState)
                            println(engine.repository.paymentRepository?.hints?.size)
                        }
                    }) {
                        Text(text = "Webview finish")
                    }
                }
                item {
                    Button(onClick = {
                        runBlocking {
                            engine.finishPayment()
                            println(engine.currentState)
                        }
                    }) {
                        Text(text = "Finish")
                    }
                }
            }
        }
    }
}

