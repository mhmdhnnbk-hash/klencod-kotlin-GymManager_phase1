package com.gym.manager.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * To use a custom Arabic font: put the .ttf in res/font/ and replace with
 * FontFamily(Font(R.font.your_font)).
 */
val GymFontFamily: FontFamily = FontFamily.Default

val GymTypography = Typography(
    headlineMedium = TextStyle(fontFamily = GymFontFamily, fontWeight = FontWeight.Bold, fontSize = 28.sp),
    titleLarge = TextStyle(fontFamily = GymFontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp),
    titleMedium = TextStyle(fontFamily = GymFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
    bodyLarge = TextStyle(fontFamily = GymFontFamily, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = GymFontFamily, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = GymFontFamily, fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
    labelMedium = TextStyle(fontFamily = GymFontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp)
)
