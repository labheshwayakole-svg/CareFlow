package com.example.careflow.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.careflow.domain.util.BookingTimeUtils
import com.example.careflow.theme.CardBorderSubtle
import com.example.careflow.theme.CareBackground
import com.example.careflow.theme.CarePrimary
import com.example.careflow.theme.CareSurface
import com.example.careflow.theme.CareTextMuted
import com.example.careflow.theme.CareTextPrimary
import com.example.careflow.theme.CareTextSecondary
import com.example.careflow.theme.PastelPeach
import com.example.careflow.theme.PastelSky
import com.example.careflow.theme.QueueActiveGreen
import com.example.careflow.theme.QueueWaitingAmber

data class ShiftTemplate(
    val id: String,
    val name: String,
    val icon: String,
    val startTime: String,
    val endTime: String,
    val badge: String
)

val DEFAULT_SHIFT_TEMPLATES = listOf(
    ShiftTemplate("morning", "Morning Shift", "🌅", "09:00 AM", "01:00 PM", "4 hrs"),
    ShiftTemplate("afternoon", "Afternoon Shift", "☀️", "01:00 PM", "05:00 PM", "4 hrs"),
    ShiftTemplate("evening", "Evening Shift", "🌆", "05:00 PM", "09:00 PM", "4 hrs"),
    ShiftTemplate("night_overnight", "Night (Overnight)", "🌙", "09:00 PM", "02:00 AM", "5 hrs • Next Day"),
    ShiftTemplate("fullday", "Full Day OPD", "🏥", "09:00 AM", "05:00 PM", "8 hrs"),
    ShiftTemplate("early", "Early OPD", "🩺", "08:00 AM", "02:00 PM", "6 hrs"),
    ShiftTemplate("late_opd", "Late OPD", "🕒", "02:00 PM", "08:00 PM", "6 hrs")
)

/**
 * Clean, intuitive Shift Time Selector with:
 * 1. 1-tap quick Shift Presets (including overnight shifts like 9 PM to 2 AM)
 * 2. High-visibility 12-hour keypad grid and minute + AM/PM selectors (no cut-off numbers)
 * 3. Native overnight shift detection with "+1 Day" notification
 */
@Composable
fun ShiftTimePickerSection(
    initialStartTime: String,
    initialEndTime: String,
    onShiftChange: (startTime: String, endTime: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Parse initial times into hour (1..12), minute (0, 15, 30, 45), and amPm ("AM"/"PM")
    val (initStartHour24, initStartMin) = remember(initialStartTime) {
        BookingTimeUtils.parseHourAndMinute(initialStartTime, 9, 0)
    }
    val (initEndHour24, initEndMin) = remember(initialEndTime) {
        BookingTimeUtils.parseHourAndMinute(initialEndTime, 14, 0)
    }

    var startHour by remember(initialStartTime) {
        mutableIntStateOf(if (initStartHour24 == 0) 12 else if (initStartHour24 > 12) initStartHour24 - 12 else initStartHour24)
    }
    var startMinute by remember(initialStartTime) {
        mutableIntStateOf((initStartMin / 15) * 15)
    }
    var startAmPm by remember(initialStartTime) {
        mutableStateOf(if (initStartHour24 >= 12) "PM" else "AM")
    }

    var endHour by remember(initialEndTime) {
        mutableIntStateOf(if (initEndHour24 == 0) 12 else if (initEndHour24 > 12) initEndHour24 - 12 else initEndHour24)
    }
    var endMinute by remember(initialEndTime) {
        mutableIntStateOf((initEndMin / 15) * 15)
    }
    var endAmPm by remember(initialEndTime) {
        mutableStateOf(if (initEndHour24 >= 12) "PM" else "AM")
    }

    var selectedTemplateId by remember { mutableStateOf<String?>(null) }

    fun formatTimeString(hour: Int, min: Int, amPm: String): String {
        return String.format("%02d:%02d %s", hour, min, amPm)
    }

    val formattedStart = formatTimeString(startHour, startMinute, startAmPm)
    val formattedEnd = formatTimeString(endHour, endMinute, endAmPm)
    val durationMinutes = BookingTimeUtils.calculateShiftDurationMinutes(formattedStart, formattedEnd)
    val isOvernight = BookingTimeUtils.isOvernightShift(formattedStart, formattedEnd)
    val isShiftValid = durationMinutes > 0
    val durationStr = BookingTimeUtils.formatShiftDuration(formattedStart, formattedEnd)

    // Notify parent on change
    LaunchedEffect(formattedStart, formattedEnd) {
        onShiftChange(formattedStart, formattedEnd)

        // Check if matching preset
        val match = DEFAULT_SHIFT_TEMPLATES.find {
            it.startTime.equals(formattedStart, ignoreCase = true) &&
            it.endTime.equals(formattedEnd, ignoreCase = true)
        }
        selectedTemplateId = match?.id
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. ACTIVE SHIFT SUMMARY CARD ---
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isShiftValid) Color(0xFFF0FDF4) else Color(0xFFFEF2F2),
            border = BorderStroke(1.dp, if (isShiftValid) Color(0xFF86EFAC) else Color(0xFFFECACA)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isOvernight) Icons.Default.NightsStay else Icons.Default.Schedule,
                            contentDescription = null,
                            tint = if (isShiftValid) QueueActiveGreen else Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isOvernight) "Overnight OPD Shift" else "Active Shift Window",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isShiftValid) QueueActiveGreen else Color(0xFFDC2626)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (isShiftValid) QueueActiveGreen.copy(alpha = 0.15f) else Color(0xFFFEE2E2),
                        border = BorderStroke(1.dp, if (isShiftValid) QueueActiveGreen else Color(0xFFDC2626))
                    ) {
                        Text(
                            text = if (isShiftValid) "⏱ $durationStr" else "⚠️ Invalid Timing",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isShiftValid) QueueActiveGreen else Color(0xFFDC2626),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$formattedStart  ➔  $formattedEnd",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CareTextPrimary
                    )
                    if (isOvernight) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = PastelSky
                        ) {
                            Text(
                                text = "Ends Next Day",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CarePrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- 2. QUICK PRESET TEMPLATES ---
        Text(
            text = "Quick Shift Presets (Tap to Apply):",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = CareTextSecondary
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
        ) {
            items(DEFAULT_SHIFT_TEMPLATES) { template ->
                val isSelected = selectedTemplateId == template.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) CarePrimary.copy(alpha = 0.12f) else CareSurface,
                    border = BorderStroke(
                        1.5.dp,
                        if (isSelected) CarePrimary else CardBorderSubtle
                    ),
                    modifier = Modifier.clickable {
                        selectedTemplateId = template.id
                        val (sH24, sM) = BookingTimeUtils.parseHourAndMinute(template.startTime, 9, 0)
                        val (eH24, eM) = BookingTimeUtils.parseHourAndMinute(template.endTime, 17, 0)

                        startHour = if (sH24 == 0) 12 else if (sH24 > 12) sH24 - 12 else sH24
                        startMinute = sM
                        startAmPm = if (sH24 >= 12) "PM" else "AM"

                        endHour = if (eH24 == 0) 12 else if (eH24 > 12) eH24 - 12 else eH24
                        endMinute = eM
                        endAmPm = if (eH24 >= 12) "PM" else "AM"
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(text = template.icon, fontSize = 13.sp)
                            Text(
                                text = template.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = if (isSelected) CarePrimary else CareTextPrimary
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = CarePrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${template.startTime} – ${template.endTime}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) CarePrimary else CareTextSecondary
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isSelected) CarePrimary.copy(alpha = 0.15f) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = template.badge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CarePrimary else CareTextMuted,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // --- 3. CUSTOM TIME PICKER CARDS ---
        Text(
            text = "Or Customize Hours & AM/PM:",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = CareTextSecondary
        )

        // START TIME CARD
        ClearTimeCard(
            title = "1. Shift Start Time",
            icon = "🕒",
            hour = startHour,
            minute = startMinute,
            amPm = startAmPm,
            badge = null,
            onHourSelect = { startHour = it },
            onMinuteSelect = { startMinute = it },
            onAmPmSelect = { startAmPm = it }
        )

        // END TIME CARD
        ClearTimeCard(
            title = "2. Shift End Time",
            icon = if (isOvernight) "🌙" else "🕒",
            hour = endHour,
            minute = endMinute,
            amPm = endAmPm,
            badge = if (isOvernight) "Next Day" else null,
            onHourSelect = { endHour = it },
            onMinuteSelect = { endMinute = it },
            onAmPmSelect = { endAmPm = it }
        )
    }
}

/**
 * Clear, spacious Time Card where all 12 hours are visible in a clean 2-row grid.
 * Minutes and AM/PM are placed directly underneath with generous touch targets.
 */
@Composable
private fun ClearTimeCard(
    title: String,
    icon: String,
    hour: Int,
    minute: Int,
    amPm: String,
    badge: String?,
    onHourSelect: (Int) -> Unit,
    onMinuteSelect: (Int) -> Unit,
    onAmPmSelect: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CareSurface,
        border = BorderStroke(1.dp, CardBorderSubtle),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title and Digital Clock Output
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = icon, fontSize = 14.sp)
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CareTextPrimary
                    )
                    if (badge != null) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = badge,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Digital Pill Display
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CarePrimary.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, CarePrimary.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = String.format("%02d:%02d %s", hour, minute, amPm),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = CarePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // 12-Hour Selection Grid (2 clean rows of 6 numbers: 1-6, 7-12)
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "Select Hour (1 - 12):",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CareTextMuted
                )

                // Row 1: 1 to 6
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (1..6).forEach { h ->
                        val isSelected = h == hour
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clickable { onHourSelect(h) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CarePrimary else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) CarePrimary else Color(0xFFE2E8F0))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$h",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else CareTextPrimary
                                )
                            }
                        }
                    }
                }

                // Row 2: 7 to 12
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    (7..12).forEach { h ->
                        val isSelected = h == hour
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clickable { onHourSelect(h) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) CarePrimary else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSelected) CarePrimary else Color(0xFFE2E8F0))
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$h",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else CareTextPrimary
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            // Minutes (:00, :15, :30, :45) & AM/PM Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minutes
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Minutes:",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CareTextMuted
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(0, 15, 30, 45).forEach { m ->
                            val isSelected = m == minute
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CarePrimary else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (isSelected) CarePrimary else Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .size(width = 38.dp, height = 30.dp)
                                    .clickable { onMinuteSelect(m) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = String.format(":%02d", m),
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else CareTextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // AM / PM Segmented Control
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Period (AM / PM):",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = CareTextMuted
                    )
                    Row(
                        modifier = Modifier
                            .height(30.dp)
                            .background(Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        listOf("AM", "PM").forEach { p ->
                            val isSelected = p == amPm
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) CarePrimary else Color.Transparent,
                                modifier = Modifier
                                    .clickable { onAmPmSelect(p) }
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = p,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else CareTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
