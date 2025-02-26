package com.pricetracker.terramonitor.ui.components

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BasicButton(text:String, onClick: () -> Unit) {
    Button(onClick = { onClick() }) {
        Text(text)
    }
}