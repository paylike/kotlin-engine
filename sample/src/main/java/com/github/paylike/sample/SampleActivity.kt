package com.github.paylike.sample

import android.os.Bundle
import android.webkit.JavascriptInterface
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
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
import com.github.paylike.kotlin_engine.view.TdsWebView
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import com.github.paylike.kotlin_money.PaymentAmount
import com.github.paylike.kotlin_request.exceptions.ServerErrorException
import com.github.paylike.sample.ui.theme.Kotlin_engineTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement

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
    val engine = PaylikeEngine(BuildConfig.PaylikeMerchantApiKey, ApiMode.TEST)
    val listener = MyListener()
    val htmlBody: MutableState<String> = remember {
        mutableStateOf("<html>\n" +
                "<body>\n" +
                "\n" +
                "<h1>My First Heading</h1>\n" +
                "</body>\n" +
                "</html>")
    }
    Kotlin_engineTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            LazyColumn(Modifier.fillMaxSize()
//                .verticalScroll(enabled = false, reverseScrolling = false, state = ScrollState(initial = 0))
                ,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.SpaceEvenly)
            {
                item {
                    Button(
                        onClick = {
                            engine.resetPaymentFlow()
                            listener.hints.clear()
                            runBlocking {
                                try {
                                    engine.createPaymentDataDto("4012111111111111", "111", 11, 2023)
                                    engine.startPayment(
                                        PaymentAmount("EUR", 10, 0),
                                        PaymentTestDto()
                                    )
                                } catch (e: ServerErrorException) {
                                    println("serverErrorException " + e.status.toString())
                                }
                                if (!engine.repository.htmlRepository.isNullOrEmpty()) {
                                    htmlBody.value = engine.repository.htmlRepository!!
                                }
                            }
                        },
                    ) {
                        Text(text = "Pay")
                    }
                }
                item {
                    TdsWebView(htmlBody = htmlBody.value, listener)
                }
                item {
                    Button(onClick = {
                            engine.repository.paymentRepository!!.hints = engine.repository.paymentRepository!!.hints.union(listener.giveHints()).toList()
                            engine.repository.paymentRepository!!.hints.forEach { println(it) }
                    }
                    ) {
                        Text(text = "Hints save")
                    }
                }
                item {
                    Button(onClick = {
                        runBlocking {
                            engine.continuePayment()
                            if (!engine.repository.htmlRepository.isNullOrEmpty()) {
                                htmlBody.value = engine.repository.htmlRepository!!
                            }
                        }
                    }) {
                        Text(text = "Webview finish")
                    }
                }
                item {
                    Button(onClick = {
                        runBlocking {
                            engine.finishPayment()
                        }
                    }) {
                        Text(text = "Finish")
                    }
                }
            }
        }
    }
}

class MyListener : JsListener {

    val hints: MutableList<String> = mutableListOf()

    @JavascriptInterface override fun receiveMessage(data: String) {
        println("listening to data: $data" )
        hints.addAll(Json.decodeFromString<HintsDto>(data).hints)
    }

    fun giveHints(): List<String> {
        return hints
    }
    suspend fun waitForHints() {
        coroutineScope {
            while (hints.isEmpty()) {

            }
        }
    }
}
