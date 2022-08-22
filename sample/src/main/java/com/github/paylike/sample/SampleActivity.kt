package com.github.paylike.sample

import android.os.Bundle
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
import androidx.compose.ui.unit.dp
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto
import com.github.paylike.kotlin_engine.model.service.ApiMode
import com.github.paylike.kotlin_engine.view.PaylikeWebview
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import com.github.paylike.kotlin_money.PaymentAmount
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
    val hintsText = remember { mutableStateOf("0") }
    val transactionID = remember { mutableStateOf("") }
    val engine = PaylikeEngine(BuildConfig.PaylikeMerchantApiKey, ApiMode.TEST)
    val paylikeWebview = PaylikeWebview(engine)
    Kotlin_engineTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            LazyColumn(Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.SpaceEvenly) {
                item {
                    Text(text = hintsText.value)
                }
                item {
                    Button(
                        onClick = {
                            engine.resetEngineStates()
                            runBlocking {
                                engine.createPaymentDataDto("4012111111111111", "111", 11, 2023)
                                engine.startPayment(
                                    PaymentAmount("EUR", 1, 0),
                                    PaymentTestDto()
                                )
                                hintsText.value = engine.repository.paymentRepository!!.hints.size.toString()
                            }
                        },
                    ) {
                        Text(text = "Pay")
                    }
                }
                item {
                    paylikeWebview.WebviewComposable()
                }
                item {
                    Button(onClick = {
                        runBlocking {
                            hintsText.value = engine.repository.paymentRepository!!.hints.size.toString()
                            engine.continuePayment()
                            hintsText.value = engine.repository.paymentRepository!!.hints.size.toString()
                        }
                    }) {
                        Text(text = "Webview")
                    }
                }
                item {
                    Button(onClick = {
                        runBlocking {
                            hintsText.value = engine.repository.paymentRepository!!.hints.size.toString()
                            engine.finishPayment()
                            hintsText.value = engine.repository.paymentRepository!!.hints.size.toString()
                            transactionID.value = engine.repository.transactionId?: "No id..."
                        }
                    }) {
                        Text(text = "Finish")
                    }
                }
                    item {
                        Text(text = transactionID.value)
                    }
            }
        }
    }
}
