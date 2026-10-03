package com.example.careflow.domain.util

import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.DayBookingSummary
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.DoctorTimeSlot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object BookingTimeUtils {

    private const val DATE_FORMAT = "yyyy-MM-dd"
    private const val DISPLAY_DATE_FORMAT = "dd MMM"
    private const val TIME_FORMAT = "hh:mm a"

    /**
     * Generates a 1-week horizon (8 days from today: Today, Tomorrow, and subsequent 6 days).
     */
    fun generateOneWeekDays(startDate: Date = Date()): List<DayBookingSummary> {
        val list = mutableListOf<DayBookingSummary>()
        val cal = Calendar.getInstance()
        cal.time = startDate

        val dateSdf = SimpleDateFormat(DATE_FORMAT, Locale.US)
        val displaySdf = SimpleDateFormat(DISPLAY_DATE_FORMAT, Locale.getDefault())
        val dayNameSdf = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 0 until 8) {
            val date = cal.time
            val dateStr = dateSdf.format(date)
            val displayDate = displaySdf.format(date)
            val dayName = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> dayNameSdf.format(date)
            }

            list.add(
                DayBookingSummary(
                    dateString = dateStr,
                    dayName = dayName,
                    displayDate = displayDate,
                    isToday = i == 0
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    /**
     * Parses time strings into (hourOfDay 0..23, minute 0..59) robustly.
     * Supports formats like:
     * - "09:00 AM", "9:00AM", "05:00 PM", "5:00PM"
     * - "0500 Am", "0500 PM", "0500AM"
     * - "5 PM", "9 AM", "17:00", "09:00"
     */
    fun parseHourAndMinute(rawStr: String, defaultHour: Int, defaultMinute: Int): Pair<Int, Int> {
        if (rawStr.isBlank()) return Pair(defaultHour, defaultMinute)
        val cleaned = rawStr.trim().uppercase(Locale.getDefault())

        // 1. Colon format: "09:00 AM", "05:00 PM", "9:30 AM", "17:00"
        val colonRegex = Regex("""(\d{1,2}):(\d{2})\s*(AM|PM)?""")
        colonRegex.find(cleaned)?.let { match ->
            var hour = match.groupValues[1].toIntOrNull() ?: defaultHour
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val amPm = match.groupValues[3]
            if (amPm == "PM" && hour < 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0
            return Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        }

        // 2. 4-digit format: "0500 AM", "0500 Am", "0500 PM", "0900"
        val fourDigitRegex = Regex("""(\d{2})(\d{2})\s*(AM|PM)?""")
        fourDigitRegex.find(cleaned)?.let { match ->
            var hour = match.groupValues[1].toIntOrNull() ?: defaultHour
            val minute = match.groupValues[2].toIntOrNull() ?: 0
            val amPm = match.groupValues[3]
            if (amPm == "PM" && hour < 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0
            return Pair(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        }

        // 3. Single hour with AM/PM: "5 PM", "9 AM"
        val singleHourRegex = Regex("""(\d{1,2})\s*(AM|PM)""")
        singleHourRegex.find(cleaned)?.let { match ->
            var hour = match.groupValues[1].toIntOrNull() ?: defaultHour
            val amPm = match.groupValues[2]
            if (amPm == "PM" && hour < 12) hour += 12
            if (amPm == "AM" && hour == 12) hour = 0
            return Pair(hour.coerceIn(0, 23), 0)
        }

        return Pair(defaultHour, defaultMinute)
    }

    /**
     * Cleanly formats shift start and end times for clean display in UI (e.g. "09:00 AM - 05:00 PM").
     */
    fun formatDisplayShift(startTimeRaw: String, endTimeRaw: String): String {
        val (startH, startM) = parseHourAndMinute(startTimeRaw, 9, 0)
        var (endH, endM) = parseHourAndMinute(endTimeRaw, 17, 0)

        // Fix PM shift if entered like "0500 Am" or 5 AM for closing
        if (endH <= startH && endH in 1..11) {
            endH += 12
        }

        val startFormatted = formatHourMinute(startH, startM)
        val endFormatted = formatHourMinute(endH, endM)
        return "$startFormatted - $endFormatted"
    }

    private fun formatHourMinute(hourOfDay: Int, minute: Int): String {
        val amPm = if (hourOfDay >= 12) "PM" else "AM"
        val hour12 = when {
            hourOfDay == 0 -> 12
            hourOfDay > 12 -> hourOfDay - 12
            else -> hourOfDay
        }
        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
    }

    /**
     * Generates dynamic consultation time slots for the doctor based on shift hours
     * with rock-solid guards against infinite loops and date mismatches.
     */
    fun generateDoctorSlots(
        doctor: Doctor,
        existingAppointments: List<Appointment>,
        selectedDate: String,
        slotIntervalMinutes: Int = 30
    ): List<DoctorTimeSlot> {
        val slotsList = mutableListOf<DoctorTimeSlot>()
        val timeSdf = SimpleDateFormat(TIME_FORMAT, Locale.US)
        val now = Calendar.getInstance()
        val todayStr = SimpleDateFormat(DATE_FORMAT, Locale.US).format(now.time)
        val isToday = selectedDate == todayStr

        // Parse start and end hours robustly
        val (rawStartH, rawStartM) = parseHourAndMinute(doctor.shiftStartTime, defaultHour = 9, defaultMinute = 0)
        var (rawEndH, rawEndM) = parseHourAndMinute(doctor.shiftEndTime, defaultHour = 17, defaultMinute = 0)

        // If end hour is <= start hour (e.g. 5:00 AM for closing time entered as "0500 Am"),
        // convert closing hour to PM (17:00)
        if (rawEndH <= rawStartH && rawEndH in 1..11) {
            rawEndH += 12
        }

        // If end time is still before or equal to start time, default to standard 9:00 AM to 5:00 PM
        var finalStartH = rawStartH
        var finalStartM = rawStartM
        var finalEndH = rawEndH
        var finalEndM = rawEndM

        if (finalEndH < finalStartH || (finalEndH == finalStartH && finalEndM <= finalStartM)) {
            finalStartH = 9
            finalStartM = 0
            finalEndH = 17
            finalEndM = 0
        }

        // ANCHOR BOTH ON THE EXACT SAME CALENDAR DAY to avoid cross-century/cross-year while loop hangs
        val baseCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2026)
            set(Calendar.MONTH, Calendar.JANUARY)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startCal = (baseCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, finalStartH)
            set(Calendar.MINUTE, finalStartM)
        }

        val endCal = (baseCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, finalEndH)
            set(Calendar.MINUTE, finalEndM)
        }

        // Filter active appointments for this doctor on the selected date
        val activeBookings = existingAppointments.filter { apt ->
            apt.doctorId == doctor.id &&
            apt.date == selectedDate &&
            apt.status != AppointmentStatus.CANCELLED &&
            apt.status != AppointmentStatus.NO_SHOW
        }

        val currentSlotCal = (startCal.clone() as Calendar)
        var safetyIterations = 0
        val safeInterval = slotIntervalMinutes.coerceAtLeast(10)

        // Strict limit of at most 48 slots per day (prevents any infinite loop under all conditions)
        while (currentSlotCal.before(endCal) && safetyIterations < 48) {
            safetyIterations++
            val slotStr = timeSdf.format(currentSlotCal.time).uppercase(Locale.getDefault())

            // Check if slot has already passed if selected date is today
            val isPassed = if (isToday) {
                val slotHour = currentSlotCal.get(Calendar.HOUR_OF_DAY)
                val slotMinute = currentSlotCal.get(Calendar.MINUTE)
                val currentHour = now.get(Calendar.HOUR_OF_DAY)
                val currentMinute = now.get(Calendar.MINUTE)
                (slotHour < currentHour) || (slotHour == currentHour && slotMinute <= currentMinute)
            } else false

            // Find if any booking matches this slot
            val matchedBooking = activeBookings.find { apt ->
                val normalizedAptSlot = apt.timeSlot.trim().uppercase(Locale.getDefault())
                normalizedAptSlot == slotStr ||
                normalizedAptSlot.startsWith(slotStr) ||
                slotStr.startsWith(normalizedAptSlot)
            }

            val isBooked = matchedBooking != null

            slotsList.add(
                DoctorTimeSlot(
                    timeSlot = slotStr,
                    isBooked = isBooked,
                    bookedTokenNumber = matchedBooking?.tokenNumber,
                    bookedPatientName = matchedBooking?.patientName,
                    isPassed = isPassed
                )
            )

            currentSlotCal.add(Calendar.MINUTE, safeInterval)
        }

        return slotsList
    }

    /**
     * Calculates the next expected token number for the given date and appointments.
     */
    fun calculateNextPredictedToken(appointments: List<Appointment>, selectedDate: String, doctorId: String): Int {
        val forDay = appointments.filter { it.doctorId == doctorId && it.date == selectedDate }
        val maxToken = forDay.maxOfOrNull { it.tokenNumber } ?: 0
        return (maxToken + 1).coerceAtLeast(forDay.size + 1)
    }
}
