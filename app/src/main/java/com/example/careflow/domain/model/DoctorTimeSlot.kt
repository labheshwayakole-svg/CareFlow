package com.example.careflow.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class DoctorTimeSlot(
    val timeSlot: String,                  // e.g. "09:00 AM", "09:30 AM"
    val isBooked: Boolean = false,         // true if appointment exists for this slot
    val bookedTokenNumber: Int? = null,    // token assigned to the booked slot
    val bookedPatientName: String? = null, // optional summary info
    val isPassed: Boolean = false          // true if time has already passed today
)

@Immutable
data class DayBookingSummary(
    val dateString: String,                // "2026-10-02"
    val dayName: String,                   // "Today", "Tomorrow", "Sun", "Mon", etc.
    val displayDate: String,               // "02 Oct"
    val isToday: Boolean = false,
    val totalSlots: Int = 0,
    val bookedSlots: Int = 0
) {
    val isFull: Boolean get() = totalSlots > 0 && bookedSlots >= totalSlots
    val availableSlots: Int get() = (totalSlots - bookedSlots).coerceAtLeast(0)
}
