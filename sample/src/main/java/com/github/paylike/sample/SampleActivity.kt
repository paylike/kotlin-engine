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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto
import com.github.paylike.kotlin_engine.model.service.ApiMode
import com.github.paylike.kotlin_engine.view.PaylikeWebview
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import com.github.paylike.kotlin_money.PaymentAmount
import com.github.paylike.sample.ui.theme.PaylikeTheme
import kotlinx.coroutines.runBlocking

class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SampleScaffold()
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SampleScaffold() {
    val engine = PaylikeEngine(BuildConfig.PaylikeMerchantApiKey, ApiMode.TEST)
    var isActive by remember { mutableStateOf(shouldBeActive(engine.currentState)) }

    PaylikeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Scaffold(
                topBar = { TopAppBar(title = {Text("Pay with PayLike")})  },
                content = {
                    SampleScreen(engine, isActive, isActiveChange = { isActive = it })
                },
            )
        }
    }
}

@Composable
fun SampleScreen(engine: PaylikeEngine, isActive: Boolean, isActiveChange: (Boolean) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(), // 1
        horizontalAlignment = Alignment.CenterHorizontally, // 2
        verticalArrangement = Arrangement.Center // 3
    ) {
        if (isActive) {
            EngineSampleComposable(engine)
        } else {
            val error = engine.error
            if(error != null) {
                ErrorText(error.message)
            }
            PayButton(engine, isActiveChange)
        }
    }
}

@Composable
fun PayButton(engine: PaylikeEngine, isActiveChange: (Boolean) -> Unit) {

    Button(
        onClick = {
            engine.resetEngineStates()
            runBlocking {
                engine.createPaymentDataDto("4012111111111111", "111", 11, 2023)
                engine.startPayment(
                    PaymentAmount("EUR", 1, 0),
                    PaymentTestDto()
                )
            }
            isActiveChange(shouldBeActive(engine.currentState))
        },
    ) {
        Text(text = "Pay")
    }
}

@Composable
fun EngineSampleComposable(engine: PaylikeEngine) {
    val hintsText = remember { mutableStateOf("0") }
    val transactionID = remember { mutableStateOf("No id...") }

    val listener = Listener(hintsText, transactionID)
    engine.addObserver(listener)

    val paylikeWebview = PaylikeWebview(engine)

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

@Composable
fun ErrorText(errorMessage: String) {
    Text(
        "Error: $errorMessage",
        fontSize = 30.sp,
    )
}
