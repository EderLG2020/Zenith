package com.zenith.player.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.zenith.player.R

@OptIn(ExperimentalTextApi::class)
private fun jakarta(weight: FontWeight) = Font(
    resId = R.font.plus_jakarta_sans,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

val ZenithFontFamily: FontFamily = FontFamily(
    jakarta(FontWeight.Normal),
    jakarta(FontWeight.Medium),
    jakarta(FontWeight.SemiBold),
    jakarta(FontWeight.Bold),
)

private fun style(size: Int, weight: FontWeight) =
    TextStyle(fontFamily = ZenithFontFamily, fontSize = size.sp, fontWeight = weight)

val ZenithTypography: Typography = Typography(
    headlineMedium = style(26, FontWeight.SemiBold), // saludo "Hola, …"
    titleLarge = style(20, FontWeight.SemiBold),     // títulos de sección
    titleMedium = style(15, FontWeight.SemiBold),    // título de canción
    bodyMedium = style(14, FontWeight.Medium),       // chips, texto general
    bodySmall = style(12, FontWeight.Normal),        // subtítulos
    labelLarge = style(14, FontWeight.Medium),
)
