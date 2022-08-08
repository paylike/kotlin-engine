package com.github.paylike.sample.screen1

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class Screen1View {
    @Composable
    fun Screen1() {
        Button(
            onClick = { /* ... */ },
        ) {
            Text("Pay")
        }
    }

    @Preview
    @Composable
    fun PreviewScreen1() {
        Screen1()
    }
}