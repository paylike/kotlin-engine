package com.github.paylike.sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto
import com.github.paylike.kotlin_engine.error.PaylikeEngineError
import com.github.paylike.kotlin_engine.model.service.ApiMode
import com.github.paylike.kotlin_engine.view.PaylikeWebview
import com.github.paylike.kotlin_engine.viewmodel.PaylikeEngine
import com.github.paylike.kotlin_money.PaymentAmount
import com.github.paylike.sample.view.theme.PaylikeTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

/**
 * Sample activity to demonstrate Paylike payment method with Compose
 */
class SampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val engine = PaylikeEngine("e393f9ec-b2f7-4f81-b455-ce45b02d355d", ApiMode.TEST)
        setContent {
            SampleScaffold(engine)
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SampleScaffold(engine: PaylikeEngine) {
    PaylikeTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colors.background
        ) {
            Scaffold(
                topBar = { TopAppBar(
                    title = { Text("Pay with PayLike") },
                )  },
                content = { SampleScreen(engine) },
            )
        }
    }
}

/**
 * Helper class to help listen to specific engine states along the tds flow
 */
class StatesListener(
    private val hints: MutableState<String>,
    private val transactionId: MutableState<String>,
    private val error: MutableState<PaylikeEngineError?>,
) : Observer {
    override fun update(p0: Observable?, p1: Any?) {
        if (p0 is PaylikeEngine) {
            if (p0.repository.paymentRepository != null) {
                hints.value = p0.repository.paymentRepository!!.hints.size.toString()
            } else {
                hints.value = "0"
            }
            transactionId.value = p0.repository.transactionId?: "No transaction id yet"
            error.value = p0.error
        }
        else {
            val observableClassName: String = if (p0 != null) {
                p0::class.simpleName!!
            } else { "Anonymous" }
            throw Exception("StatesListener is listening to something unexpected: $observableClassName")
        }
    }
}

@Composable
fun SampleScreen(engine: PaylikeEngine, ) {
    val errorMutableState: PaylikeEngineError? = null
    val error = remember { mutableStateOf(errorMutableState) }

    val hintsText = remember { mutableStateOf("0") }
    val transactionID = remember { mutableStateOf("No transaction id yet") }
    DisposableEffect(LocalLifecycleOwner.current) {
        val statesListener = StatesListener(hintsText, transactionID, error)
        engine.addObserver(statesListener)
        onDispose {
            engine.deleteObserver(statesListener)
        }
    }
    val webview = remember {
        mutableStateOf(PaylikeWebview(engine))
    }

    if (error.value != null) {
        Toast.makeText(LocalContext.current, error.value!!.message, Toast.LENGTH_LONG).show()
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height(30.dp),
                text = hintsText.value,
                textAlign = TextAlign.Center,
            )
        }
        item {
            Text(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height(30.dp),
                text = transactionID.value,
                textAlign = TextAlign.Center,
            )
        }
        item {
            webview.value.WebviewComposable(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .height(300.dp)
            )
            PayButton(
                engine,
            )
        }
    }
}

/**
 * Starts Payment
 */
@Composable
fun PayButton(
    engine: PaylikeEngine,
) {
    Button(
        onClick = {
            engine.resetEngineStates()
            CoroutineScope(Dispatchers.IO).launch {
                engine.initializePaymentData(
                    "4012111111111111",
                    "111",
                    11,
                    2023
                )
                engine.addPaymentDescriptionData(
                    paymentAmount = PaymentAmount("EUR", 1, 0),
                    paymentTestData = PaymentTestDto()
                    )
                engine.addPaymentAdditionalData(
                    textData = "Hello from android client"
                )
                engine.startPayment()
            }
        },
    ) {
        Text(text = "Pay")
    }
}
