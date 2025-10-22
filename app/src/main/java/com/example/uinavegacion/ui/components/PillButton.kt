package com.example.uinavegacion.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.uinavegacion.ui.components.AnimatedOutlinedButton

@Composable
fun PillButton(onClick: () -> Unit, text: String, modifier: Modifier = Modifier) {
    AnimatedOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.outlinedButtonColors()
    ) {
        Text(text)
    }
}
