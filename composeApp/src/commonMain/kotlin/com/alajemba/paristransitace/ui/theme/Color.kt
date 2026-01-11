package com.alajemba.paristransitace.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color


// The Voids (Backgrounds)
val VoidBlack = Color(0xFF11100f)
val PanelBlack = Color(0xFF1c1a17)
val DeepShadow = Color(0xFF000000)

// The Lights (Text & Highlights)
val RetroAmber = Color(0xFFfabd2f)
val PaperText = Color(0xFFbdae93)
val DimGray = Color(0xFF7c6f64)
val BorderGray = Color(0xFF504945)

// The Alerts (Status)
val AlertRed = Color(0xFFfb4934)        // Custom Red (Brighter for dark mode)
val CrimsonRed = Color(0xFFcc241d)
val SuccessGreen = Color(0xFF4CAF50)    // For correct choices
val ElectricBlue = Color(0xFF83a598)    // For "System" messages (Optional but good to have)

// The FX (Transparencies)
val ScanlineBlack = Color(0x1A000000)   // 10% Black for scanlines
val VignetteBlack = Color(0xE6000000)   // 90% Black for edges


val TerminalColors = darkColorScheme(
    // Primary: The "Action" color (Amber buttons, glowing text)
    primary = RetroAmber,
    onPrimary = VoidBlack, // Text on top of an Amber button should be black
    primaryContainer = RetroAmber.copy(alpha = 0.2f),
    onPrimaryContainer = RetroAmber,

    // Secondary: The "Reading" color (Paper text)
    secondary = PaperText,
    onSecondary = VoidBlack,
    secondaryContainer = PanelBlack,
    onSecondaryContainer = PaperText,

    // Tertiary: Used for "System" or "AI" messages
    tertiary = ElectricBlue,
    onTertiary = VoidBlack,

    // Backgrounds: The deep void
    background = VoidBlack,
    onBackground = PaperText,

    // Surfaces: Cards, Dialogs, Popups
    surface = PanelBlack,
    onSurface = PaperText,
    surfaceVariant = PanelBlack,
    onSurfaceVariant = DimGray,

    // Outlines: The borders of the terminal windows
    outline = BorderGray,
    outlineVariant = DimGray.copy(alpha = 0.5f),

    // Errors: Fines, Controllers, Wrong Answers
    error = AlertRed,
    onError = VoidBlack,
    errorContainer = CrimsonRed.copy(alpha = 0.3f), // Red background for error alerts
    onErrorContainer = AlertRed
)


object TerminalPalette {
    val ConsoleCursor = RetroAmber
    val DisabledText = DimGray
    val Separator = BorderGray
    val AISpeech = ElectricBlue
    val FineOverlay = CrimsonRed.copy(alpha = 0.2f)
}