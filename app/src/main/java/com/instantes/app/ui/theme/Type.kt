package com.instantes.app.ui.theme
import com.instantes.app.R
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

val PlayfairDisplay = FontFamily(
    Font(R.font.playfair_regular, FontWeight.Normal),
    Font(R.font.playfair_bold, FontWeight.Bold)
)

val Lora = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_bold, FontWeight.Bold)
)

val baseline = Typography()

val AppTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = PlayfairDisplay),
    displayMedium = baseline.displayMedium.copy(fontFamily = PlayfairDisplay),
    displaySmall = baseline.displaySmall.copy(fontFamily = PlayfairDisplay),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = PlayfairDisplay),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = PlayfairDisplay),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = PlayfairDisplay),
    titleLarge = baseline.titleLarge.copy(fontFamily = PlayfairDisplay),
    titleMedium = baseline.titleMedium.copy(fontFamily = PlayfairDisplay),
    titleSmall = baseline.titleSmall.copy(fontFamily = PlayfairDisplay),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = Lora),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = Lora),
    bodySmall = baseline.bodySmall.copy(fontFamily = Lora),
    labelLarge = baseline.labelLarge.copy(fontFamily = Lora),
    labelMedium = baseline.labelMedium.copy(fontFamily = Lora),
    labelSmall = baseline.labelSmall.copy(fontFamily = Lora),
)