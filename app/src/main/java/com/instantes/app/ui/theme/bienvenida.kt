package com.instantes.app.ui.theme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.instantes.app.R
import kotlinx.coroutines.delay

@Composable
fun Bienvenida(
    esPrimeraVez: Boolean,
    onComenzarClick: () -> Unit,
    onAutoNavegar: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val navegado = rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
        if (!esPrimeraVez && !navegado.value) {
            navegado.value = true
            delay(2000)
            onAutoNavegar()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.inverseSurface)
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(durationMillis = 1000))
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.bienvenida_titulo),
                    fontFamily = PlayfairDisplay,
                    fontSize = 56.sp,
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.bienvenida_subtitulo),
                    fontFamily = Lora,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                if (esPrimeraVez) {
                    Spacer(modifier = Modifier.height(48.dp))

                    Button(
                        onClick = { onComenzarClick() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(text = stringResource(R.string.bienvenida_comenzar), fontFamily = Lora)
                    }
                }
            }
        }
    }
}
