package com.github.paylike.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto
import com.github.paylike.kotlin_engine.helper.PaylikeEngineError
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isActive) {
            SampleTdsPaymentComposable(engine)
        } else {
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
fun SampleTdsPaymentComposable(engine: PaylikeEngine) {
    val hintsText = remember { mutableStateOf("0") }
    val transactionID = remember { mutableStateOf("No id...") }
    val errorMutableState: PaylikeEngineError? = null
    val error = remember { mutableStateOf(errorMutableState) }
    val statesListener = StatesListener(hintsText, transactionID, error)
    engine.addObserver(statesListener)

    if (error.value != null) {
        Toast.makeText(LocalContext.current, error.value!!.message, Toast.LENGTH_LONG).show()
    }

    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(20.dp),
            text = hintsText.value,
            textAlign = TextAlign.Start,
            )
        Text(
            modifier = Modifier
                .fillMaxWidth(1f)
                .height(20.dp),
            text = transactionID.value,
            textAlign = TextAlign.Start,
        )
        PaylikeWebview(engine).WebviewComposable(
            modifier =  Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(0.4f)
        )
    }
}
