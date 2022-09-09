package com.github.paylike.sample.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
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
    /**
     * Injected viewModel handling the intermediate role of MVVM architecture.
     */
    private val sampleViewModel: SampleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            /**
             * Sample UI implementation
             */
            SampleComposable(sampleViewModel)
        }
    }
}

/**
 * Single composable consisting every external component to set up the UI for the sample.
 * Features: shows hints counter, shows transactionId if any,
 * handles scaffold frame and state, shows error message if any.
 */
@Composable
fun SampleComposable(viewModel: SampleViewModel) {
    /**
     * Custom theme to demonstrate paylike-ish environment
     */
    PaylikeTheme {
        /**
         * Remembering the local compose states
         */
        val scaffoldState = rememberScaffoldState()
        val uiState = remember { viewModel.uiState }

        /**
         * Remembering the webview state
         */
        val webview = remember {
            mutableStateOf(PaylikeWebview(viewModel.paylikeEngine))
        }

        /**
         * Scaffold frame
         */
        Scaffold(
            topBar = { TopAppBar(
                title = { Text("Pay with PayLike") },
            )  },
            scaffoldState = scaffoldState,
            content = { padding ->
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    color = MaterialTheme.colors.background
                ) {
                    /**
                     * Displays a snackBar with the recently received error message.
                     */
                    if (uiState.errorInstance.value != null) {
                        uiState.errorInstance.value?.let {
                            LaunchedEffect(scaffoldState.snackbarHostState) {
                                scaffoldState.snackbarHostState.showSnackbar(
                                    message = it.message,
                                    duration = SnackbarDuration.Long,
                                )
                            }
                        }
                    }

                    /**
                     * Arranges the UI elements of the sample application.
                     * 1st: textField showing the actual number of hints saved,
                     * 2nd: textField showing the transactionId if any,
                     * 3rd: the webview managing the the TDS flow, it"s render is set internally,
                     * 4th: [PayButton] initializing a hardwired simple payment flow,
                     * 5th: [ResetButton] resetting the engine to be able to reinitialise the payment flow.
                     */
                    Column(
                        Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceEvenly,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .height(30.dp),
                            text = uiState.numberOfHints.value,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .height(30.dp),
                            text = uiState.transactionId.value,
                            textAlign = TextAlign.Center,
                        )
                        webview.value.WebviewComposable(
                            modifier = Modifier
                                .fillMaxWidth(1f)
                                .height(300.dp)
                        )
                        PayButton(
                            viewModel,
                            uiState.shouldRenderPayButton.value
                        )
                        ResetButton(
                            viewModel,
                            uiState.shouldRenderResetButton.value
                        )
                    }
                }
            },
        )
    }
}

/**
 * Initializing a hardwired simple payment flow
 */
@Composable
fun PayButton(
    viewModel: SampleViewModel,
    shouldRender: Boolean,
) {
    if (shouldRender) {
        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.paylikeEngine.initializePaymentData(
                        "4012111111111111",
                        "111",
                        11,
                        2023
                    )
                    viewModel.paylikeEngine.addPaymentDescriptionData(
                        paymentAmount = PaymentAmount("EUR", 1, 0),
                        paymentTestData = PaymentTestDto()
                    )
                    viewModel.paylikeEngine.addPaymentAdditionalData(
                        textData = "Hello from android client"
                    )
                    viewModel.paylikeEngine.startPayment()
                }
            },
        ) {
            Text(text = "Pay")
        }
    }
}

/**
 * Resetting the engine to be able to reinitialise the payment flow.
 */
@Composable
fun ResetButton(
    viewModel: SampleViewModel,
    shouldRender: Boolean,
) {
    if (shouldRender) {
        Button(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    viewModel.paylikeEngine.resetEngineStates()
                }
            }
        ) {
            Text(text = "Reset")
        }
    }
}
