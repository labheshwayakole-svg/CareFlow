package com.example.careflow.theme

import androidx.compose.ui.graphics.Color

// =========================================================================
// CAREFLOW WARM TERRACOTTA CORAL & SUNSET PEACH THEME PALETTE
// Warm linen cream canvas, welcoming terracotta coral and apricot accents,
// calming sage-teal clinical touches, and crisp porcelain white surfaces
// =========================================================================

// Primary Brand: Warm Terracotta Coral (MediCare+ Logo & Primary Action)
val CarePrimary = Color(0xFFE06D53)           // Warm Terracotta Coral
val CareOnPrimary = Color(0xFFFFFFFF)         // Pure White
val CarePrimaryContainer = Color(0xFFFDE8DB)  // Soft Apricot Peach (Token 14 Card Tint)
val CareOnPrimaryContainer = Color(0xFF3D1F17)// Deep Warm Espresso / Earthy Cocoa

// Secondary: Calming Clinical Sage-Teal (From Quick Links: Labs, Reports, Pharmacy)
val CareSecondary = Color(0xFF4A9B8F)         // Calming Sage Teal
val CareOnSecondary = Color(0xFFFFFFFF)
val CareSecondaryContainer = Color(0xFFE3F3F0)// Soothing Sage Mint Tint
val CareOnSecondaryContainer = Color(0xFF134E45)

// Tertiary: Sunset Peach
val CareTertiary = Color(0xFFE27D60)          // Sunset Peach Coral
val CareOnTertiary = Color(0xFFFFFFFF)
val CareTertiaryContainer = Color(0xFFFFF1EB) // Warm Apricot Tint
val CareOnTertiaryContainer = Color(0xFF5A2518)

// Canvas & Surfaces: Warm Linen Cream & Crisp Porcelain
val CareBackground = Color(0xFFFAF7F2)        // Soft Warm Linen Cream (Comforting, anti-glare)
val CareOnBackground = Color(0xFF261C16)      // Deep Warm Espresso Black Text
val CareSurface = Color(0xFFFFFFFF)           // Pure Crisp White Cards
val CareOnSurface = Color(0xFF261C16)         // Deep Warm Espresso Black Text
val CareSurfaceVariant = Color(0xFFF5EFE6)    // Warm Biscuit Alabaster for input fields & subtle chips
val CareOnSurfaceVariant = Color(0xFF6E5F55)  // Warm Cocoa Slate Text

// Outlines & Borders
val CareOutline = Color(0xFFE3D9CC)           // Warm Subtle Border
val CareOutlineVariant = Color(0xFFEDE4D8)    // Ultra-light Warm Border
val CardBorderSubtle = Color(0xFFEDE4D8)      // Delicate Warm Border for Cards
val CardShadowSubtle = Color(0x143B2219)      // Soft Warm Umber Shadow

// Real-Time Queue & Consultation Accents
val QueueActiveGreen = Color(0xFF2E8B6D)      // Calm Forest Mint / Active Turn
val QueueWaitingAmber = Color(0xFFE27D60)     // Warm Apricot Coral / Waiting in Queue
val QueueServingCyan = Color(0xFF4A9B8F)      // Calming Sage Teal / In Consultation
val QueueCancelledRed = Color(0xFFDC2626)     // Emergency Crimson (Used ONLY for errors/cancellations)

// CareFlow Signature Terracotta & Coral Tokens
val CoralDeep = Color(0xFFC85A3F)             // Deep Terracotta Coral
val CoralWarm = Color(0xFFE06D53)             // Warm Terracotta Coral
val CoralBright = Color(0xFFF2856D)           // Vibrant Sunset Coral
val CoralApricot = Color(0xFFF59E89)          // Apricot Coral
val CoralMistBg = Color(0xFFFDF2EB)           // Warm Coral Mist Pill Background
val CoralMistBorder = Color(0xFFFCDAC8)       // Soft Apricot Pill Border
val CoralDeepText = Color(0xFFC85A3F)         // Deep Terracotta Text
val CoralShadow = Color(0x28E06D53)           // Ambient Coral Drop Shadow
val CoralCardGlow = Color(0x18E06D53)         // Warm Ambient Glow

// Design System Aliases for Seamless Backward-Compatibility
val PulseCrimsonDark = CoralDeep
val PulseRed = CoralWarm
val PulseRedBright = CoralBright
val PulseCoral = CoralApricot
val PulseRoseBg = CoralMistBg
val PulseRoseBorder = CoralMistBorder
val PulseRoseText = CoralDeepText
val PulseShadow = CoralShadow
val PulseCardGlow = CoralCardGlow

// Emerald / Sage Live State Badges
val PulseLiveGreen = Color(0xFF2E8B6D)
val PulseLiveGreenBg = Color(0xFFE2F5EE)
val PulseLiveGreenBorder = Color(0xFFA7E3CE)

// Pastel Accents for Diagnosis & Department Chips
val PastelSky = Color(0xFFE3F3F0)             // Soft Sage Teal
val PastelMint = Color(0xFFE2F5EE)            // Calming Mint
val PastelAmber = Color(0xFFFDEBD9)           // Warm Apricot Amber
val PastelRose = Color(0xFFFDF2EB)            // Warm Coral Mist
val PastelPurple = Color(0xFFF5EEF8)
val PastelTeal = Color(0xFFE3F3F0)
val PastelPeach = Color(0xFFFDE8DB)           // Soft Apricot Peach (Token pass background)
val PastelLavender = Color(0xFFF5EEF8)

val QueueActiveGreenContainer = Color(0xFFE2F5EE)
val QueueWaitingAmberContainer = Color(0xFFFDEBD9)
val QueueServingCyanContainer = Color(0xFFE3F5F2)
val QueueCancelledRedContainer = Color(0xFFFEE2E2)

// High contrast typography colors for warm light theme
val CareTextPrimary = CareOnSurface
val CareTextSecondary = CareOnSurfaceVariant
val CareTextMuted = Color(0xFF9E8F84)         // Warm Taupe

// Backward-compatibility aliases
val PrimaryTeal = CarePrimary
val OnPrimaryTeal = CareOnPrimary
val PrimaryContainerTeal = CarePrimaryContainer
val OnPrimaryContainerTeal = CareOnPrimaryContainer
val SecondarySlate = CareSecondary
val OnSecondarySlate = CareOnSecondary
val SecondaryContainerSlate = CareSecondaryContainer
val OnSecondaryContainerSlate = CareOnSecondaryContainer
val TertiaryCyan = CareTertiary
val OnTertiaryCyan = CareOnTertiary
val TertiaryContainerCyan = CareTertiaryContainer
val OnTertiaryContainerCyan = CareOnTertiaryContainer
val SurfaceLight = CareSurface
val OnSurfaceLight = CareOnSurface
val SurfaceVariantLight = CareSurfaceVariant
val OnSurfaceVariantLight = CareOnSurfaceVariant
val BackgroundLight = CareBackground

