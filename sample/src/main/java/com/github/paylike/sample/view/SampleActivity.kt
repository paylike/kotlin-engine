package com.github.paylike.sample.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.paylike.kotlin_client.domain.dto.payment.request.test.PaymentTestDto
import com.github.paylike.kotlin_engine.view.PaylikeWebview
import com.github.paylike.kotlin_money.PaymentAmount
import com.github.paylike.sample.view.theme.PaylikeTheme
import com.github.paylike.sample.viewmodel.SampleViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Sample activity to demonstrate Paylike payment method with Compose
 */
class SampleActivity : ComponentActivity() {

    private val sampleViewModel: SampleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PaylikeTheme {
                SampleScreen(sampleViewModel)
            }
        }
    }
}

@Composable
fun SampleScreen(
    model: SampleViewModel
) {
    val uiState = remember { model.uiState }
    val scaffoldState = rememberScaffoldState()
    val webview = PaylikeWebview(model.paylikeEngine)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Pay with PayLike")
                },
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = { scaffoldState.snackbarHostState },
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            color = MaterialTheme.colors.background
        ) {
            if (uiState.errorInstance != null) {
                LaunchedEffect(scaffoldState.snackbarHostState) {
                    scaffoldState.snackbarHostState.showSnackbar(
                        message = uiState.errorInstance!!.message,
                        duration = SnackbarDuration.Long,
                    )
                }
            }
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(30.dp),
                    text = uiState.numberOfHints,
                    textAlign = TextAlign.Center,
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(30.dp),
                    text = uiState.transactionId,
                    textAlign = TextAlign.Center,
                )
//                if (uiState.shouldRenderWebview) {
                webview.WebviewComposable(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .height(300.dp),
                )
//                } else {
                PayButton(
                    model = model,
                )
//                }
            }
        }

    }
}

/**
 * Starts Payment
 */
@Composable
fun PayButton(
    model: SampleViewModel
) {
    Button(
        onClick = {
            CoroutineScope(Dispatchers.IO).launch {
                model.paylikeEngine.resetEngineStates()
                model.paylikeEngine.initializePaymentData(
                    "4012111111111111",
                    "111",
                    11,
                    2023
                )
                model.paylikeEngine.addPaymentDescriptionData(
                    paymentAmount = PaymentAmount("EUR", 1, 0),
                    paymentTestData = PaymentTestDto()
                )
                model.paylikeEngine.addPaymentAdditionalData(
                    textData = "Hello from android client"
                )
                model.paylikeEngine.startPayment()
            }
        },
    ) {
        Text(text = "Pay")
    }
}
