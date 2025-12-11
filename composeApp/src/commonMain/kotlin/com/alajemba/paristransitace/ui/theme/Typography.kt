package com.alajemba.paristransitace.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import paristransitace.composeapp.generated.resources.CrimsonPro_Bold
import paristransitace.composeapp.generated.resources.CrimsonPro_Regular
import paristransitace.composeapp.generated.resources.Res
import paristransitace.composeapp.generated.resources.VT323_Regular

@Composable
fun AppTypography(): Typography {
    val CrimsonPro = FontFamily(
        Font(Res.font.CrimsonPro_Bold, FontWeight.Bold),
        Font(Res.font.CrimsonPro_Regular, FontWeight.Normal),
    )

    val Vt323 = FontFamily(
        Font(Res.font.VT323_Regular, FontWeight.Normal)
    )

    return Typography(
        // DISPLAY (Big Headers - Scenario Titles) -> VT323
        displayLarge = TextStyle(
            fontFamily = Vt323,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp
        ),
        displayMedium = TextStyle(
            fontFamily = Vt323,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.sp
        ),
        displaySmall = TextStyle(
            fontFamily = Vt323,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp
        ),

        // HEADLINE (Subheaders - "Available Actions") -> VT323
        headlineLarge = TextStyle(
            fontFamily = Vt323,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = Vt323,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 36.sp
        ),

        // BODY (The Story Text) -> Crimson Pro
        bodyLarge = TextStyle(
            fontFamily = CrimsonPro,
            fontWeight = FontWeight.Normal,
            fontSize = 18.sp, // Slightly larger for readability
            lineHeight = 24.sp,
            color = PaperText // Default color
        ),
        bodyMedium = TextStyle(
            fontFamily = CrimsonPro,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 22.sp
        ),

        // LABEL (Buttons, Tags, Status Bars) -> VT323
        labelLarge = TextStyle(
            fontFamily = Vt323,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp, // Big buttons
            lineHeight = 20.sp,
            letterSpacing = 0.5.sp
        ),
        labelMedium = TextStyle(
            fontFamily = Vt323,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = Vt323,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

