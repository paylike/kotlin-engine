package com.github.paylike.sample

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto
import com.github.paylike.kotlin_engine.model.service.ApiMode
import com.github.paylike.kotlin_engine.view.HintsListener
import com.github.paylike.kotlin_engine.view.PaylikeWebview
import com.github.paylike.kotlin_engine.viewmodel.EngineState
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import com.github.paylike.kotlin_money.PaymentAmount
import com.github.paylike.sample.ui.theme.Kotlin_engineTheme
import kotlinx.coroutines.runBlocking
import java.util.*

val engine = PaylikeEngine("e393f9ec-b2f7-4f81-b455-ce45b02d355d", ApiMode.TEST)
val paylikeGreen= Color(0xFF2e8f29)

class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScaffoldDemo()
        }
    }
}

fun shouldBeActive(): Boolean {
    var currentState = engine.getCurrentState()

    return currentState === EngineState.WEBVIEW_CHALLENGE_STARTED ||
            currentState === EngineState.WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun ScaffoldDemo() {
    var isActive by remember { mutableStateOf(shouldBeActive()) }
    println("isActive: $isActive")
    Scaffold(
        topBar = { TopAppBar(title = {Text("Pay with PayLike")}, backgroundColor = paylikeGreen)  },

        content = {
            SampleScreen(isActive, isActiveChange = { isActive = it })
        },
    )
}

@Composable
fun SampleScreen(isActive: Boolean, isActiveChange: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(), // 1
        horizontalAlignment = Alignment.CenterHorizontally, // 2
        verticalArrangement = Arrangement.Center // 3
    ) {
        println(isActive)
        if (isActive) {
            EngineSampleComposable()
        } else {
            val error = engine.getError()
            if(error != null) {
                ErrorText(error.message)
            }
            PayButton(isActiveChange)
        }
    }
}

@Composable
fun PayButton(isActiveChange: (Boolean) -> Unit) {
    Button(
        colors = ButtonDefaults.buttonColors(
            backgroundColor = paylikeGreen,
            contentColor = androidx.compose.ui.graphics.Color.White
        ),
        onClick = {
            engine.resetEngineStates()
            runBlocking {
                engine.createPaymentDataDto("4012111111111111", "111", 11, 2023)
                engine.startPayment(
                    PaymentAmount("EUR", 1, 0),
                    PaymentTestDto()
                )
            }
            isActiveChange(shouldBeActive())
        },
    ) {
        Text(text = "Pay")
    }
}

@Composable
fun ErrorText(errorMessage: String) {
    Text(
        "Error: $errorMessage", // TODO: On error show error message (put to state)
        fontSize = 30.sp,
    )
}

@Composable
fun EngineSampleComposable() {
    val hintsText = remember { mutableStateOf("0") }
    val transactionID = remember { mutableStateOf("No id...") }
    val paylikeWebview = PaylikeWebview(engine)
    val listener = Listener(hintsText, transactionID)
    engine.addObserver(listener)
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
                    paylikeWebview.WebviewComposable()
                }
                item {
                    Text(text = transactionID.value)
                }
            }
        }
    }
}

class Listener(private val hints: MutableState<String>,private val transactionId: MutableState<String>) : Observer {
    override fun update(p0: Observable?, p1: Any?) {
        if (p0 is PaylikeEngine) {
            hints.value = p0.repository.paymentRepository!!.hints.size.toString()
            transactionId.value = p0.repository.transactionId?: "No id.."
        }
    }
}
