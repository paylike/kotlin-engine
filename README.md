# Paylike's Android engine

<a href="https://jitpack.io/#paylike/kotlin_engine" target="_blank">
    <img src="https://jitpack.io/v/paylike/kotlin_engine.svg" />
</a>
<a href="https://github.com/kocsislaci/kotlin_engine/actions/workflows/AssembleOnMain.yml" target="_blank">
    <img src="https://github.com/kocsislaci/kotlin_engine/actions/workflows/AssembleOnMain.yml/badge.svg?branch=main" />
</a>

This library includes the core elements required to implement a payment flow towards the Paylike API.
If you are looking for our high level component providing complete payment forms as well, [check here](https://github.com/paylike/kotlin_sdk).

## Table of contents
* [API Reference](#api-reference)
* [PaylikeWebview](#paylikeWebview) (Webview composable)
    * [Modifier](#modifier)
    * [Understanding TDS](#understanding-tds)
* [PaylikeEngine](#paylikeengine) (Underlying business logic service)
    * [Engine events](#engine-events)
* [Sample application](#sample-application)

## API Reference

For the library you can find the API reference [here](https://paylike.io#todo-link).
To get more familiar with our server API you can find here the [official documentation](https:/github.com/paylike/api-reference).

## PaylikeWebview

Webview component of the payment flow, able to render the webview required to execute the TDS challenge.

```kotlin
val paylikeWebview = PaylikeWebview(/*...*/)

//...

// @Composable context
paylikeWebview.WebviewComposable()
```
For a realistic usage check out the sample [here](./sample/src/main/java/com/github/paylike/sample).

### Modifier

The webview composable has optional Modifier parameter. The underlying AndroidView composable directly gets this Modifier.

Example usage:

```kotlin
val paylikeWebview = PaylikeWebview(/*...*/)

//...

// @Composable context
paylikeWebview.WebviewComposable(
  modifier = Modifier.fillMaxWidth(1f).height(300.dp) // Optional arbitrary modifier
)
```

### Understanding TDS

TDS is required to execute the payment flow and it is a core part of accepting payments online. Every bank is required by financial laws to provide this methodology for their customers in order to achieve higher security measures.

## PaylikeEngine

The core component of the payment flow.

Essentially designed to be event based to allow as much flexibility as possible on the implementer side.

Example card payment usage:

```kotlin
val paylikeEngine = PaylikeEngine(
  merchantId = BuildConfig.PaylikeMerchantApiKey, // Your merchantId loaded from environment
  apiMode = ApiMode.TEST,
)

/**
 * After the startPayment() function the engine updates it's state to render TDS webview challenge.
 * A PaylikeWebview instance is required to listen to the states of the engine,
 * so it can help teh TDS challenge flow.
 */
CoroutineScope(Dispatchers.IO).launch {
  paylikeEngine.initializePaymentData( // Suspend function
    "4012111111111111",
    "111",
    11,
    2023
  )
  paylikeEngine.addPaymentDescriptionData(
    paymentAmount = PaymentAmount("EUR", 1, 0),
    paymentTestData = PaymentTestDto()
  )
  paylikeEngine.addPaymentAdditionalData(
    textData = "Hello from android client"
  )
  paylikeEngine.startPayment() // Suspend function
}
```

### Engine events

The library exposes an enum called EngineState which describes the following states:

* `WAITING_FOR_INPUT` - Indicates that the engine is ready to be used and waiting for input
* `WEBVIEW_CHALLENGE_STARTED` -  Indicates that a webview challenge is required to complete the TDS flow, this is the first state when the webview has to be rendered
* `WEBVIEW_CHALLENGE_USER_INPUT_REQUIRED` - Indicates that the first step of the TDS flow is done and the challenge needs interraction from the end user to resolve
* `SUCCESS` - Indicates that all challenges are done successfully and the payment is being processed
* `ERROR` - Happens when the flow could not be completed successfully

## Sample Application

In the [sample directory](./sample) you can find a simple example of how to use the library.

You need to [register a merchant account](https://paylike.io/sign-up) with Paylike before you can use the sample application. Once you have created your account you can create a new client ID for yourself and use it in the sandbox environment.

You have to enter your client ID to local.properties which stores the environmental variables, so the sample application can include it.