package com.offordflix.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.Font as ResourceFont

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)


// Google Fonts provider and modern font family for synopses/overviews
private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = com.offordflix.R.array.com_google_android_gms_fonts_certs
)

private val InterFont = GoogleFont("Inter")

// Prefer bundled Outfit variable font for reliability on TVs; fall back to Inter via Google Fonts if unavailable
val SynopsisFontFamily: FontFamily = try {
    FontFamily(
        ResourceFont(resId = com.offordflix.R.font.outfit_variable, weight = FontWeight.Normal),
        ResourceFont(resId = com.offordflix.R.font.outfit_variable, weight = FontWeight.Medium),
        ResourceFont(resId = com.offordflix.R.font.outfit_variable, weight = FontWeight.SemiBold)
    )
} catch (e: Throwable) {
    FontFamily(
        Font(googleFont = InterFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
        Font(googleFont = InterFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
        Font(googleFont = InterFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold)
    )
}


