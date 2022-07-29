package com.github.paylike.kotlin_engine.helper.exceptions

import java.lang.Exception

/** Thrown when the payment body is invalid */
class InvalidPaymentBodyException(private val reason: String) : Exception(reason)
