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
     * Checks if a shift crosses midnight into the next day (e.g. 09:00 PM to 02:00 AM).
     */
    fun isOvernightShift(startTimeRaw: String, endTimeRaw: String): Boolean {
        val (startH, startM) = parseHourAndMinute(startTimeRaw, 9, 0)
        val (endH, endM) = parseHourAndMinute(endTimeRaw, 17, 0)
        val startTotal = startH * 60 + startM
        val endTotal = endH * 60 + endM
        return endTotal < startTotal
    }

    /**
     * Cleanly formats shift start and end times for clean display in UI (e.g. "09:00 AM - 05:00 PM" or "09:00 PM - 02:00 AM (+1d)").
     */
    fun formatDisplayShift(startTimeRaw: String, endTimeRaw: String): String {
        val (startH, startM) = parseHourAndMinute(startTimeRaw, 9, 0)
        val (endH, endM) = parseHourAndMinute(endTimeRaw, 17, 0)

        val startFormatted = formatHourMinute(startH, startM)
        val endFormatted = formatHourMinute(endH, endM)
        return if (isOvernightShift(startTimeRaw, endTimeRaw)) {
            "$startFormatted - $endFormatted (+1d)"
        } else {
            "$startFormatted - $endFormatted"
        }
    }

    fun formatHourMinute(hourOfDay: Int, minute: Int): String {
        val amPm = if (hourOfDay >= 12) "PM" else "AM"
        val hour12 = when {
            hourOfDay == 0 -> 12
            hourOfDay > 12 -> hourOfDay - 12
            else -> hourOfDay
        }
        return String.format(Locale.US, "%02d:%02d %s", hour12, minute, amPm)
    }

    /**
     * Calculates total shift duration in minutes.
     * Fully supports overnight shifts crossing midnight (e.g. 09:00 PM to 02:00 AM = 300 mins = 5 hrs).
     */
    fun calculateShiftDurationMinutes(startTimeRaw: String, endTimeRaw: String): Int {
        val (startH, startM) = parseHourAndMinute(startTimeRaw, 9, 0)
        val (endH, endM) = parseHourAndMinute(endTimeRaw, 17, 0)
        val startTotal = startH * 60 + startM
        val endTotal = endH * 60 + endM

        return when {
            endTotal > startTotal -> endTotal - startTotal
            endTotal < startTotal -> (endTotal + 24 * 60) - startTotal // Crosses midnight into next morning
            else -> 0
        }
    }

    /**
     * Formats shift duration into human-readable string (e.g., "5 hrs 00 mins").
     */
    fun formatShiftDuration(startTimeRaw: String, endTimeRaw: String): String {
        val totalMinutes = calculateShiftDurationMinutes(startTimeRaw, endTimeRaw)
        val hrs = totalMinutes / 60
        val mins = totalMinutes % 60
        val isOvernight = isOvernightShift(startTimeRaw, endTimeRaw)
        val baseDuration = when {
            hrs > 0 && mins > 0 -> "$hrs hrs $mins mins"
            hrs > 0 -> "$hrs hrs"
            else -> "$mins mins"
        }
        return if (isOvernight) "$baseDuration (Overnight)" else baseDuration
    }

    /**
     * Checks if current time is currently within doctor's shift hours.
     * Fully supports overnight shifts crossing midnight.
     */
    fun isDoctorShiftCurrentlyActive(startTimeRaw: String, endTimeRaw: String): Boolean {
        val (startH, startM) = parseHourAndMinute(startTimeRaw, 9, 0)
        val (endH, endM) = parseHourAndMinute(endTimeRaw, 17, 0)
        val startTotal = startH * 60 + startM
        val endTotal = endH * 60 + endM
        val now = Calendar.getInstance()
        val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        return if (endTotal < startTotal) {
            // Overnight shift (e.g. 21:00 to 02:00)
            curMinutes >= startTotal || curMinutes < endTotal
        } else {
            curMinutes in startTotal..endTotal
        }
    }

    /**
     * Checks if today's shift has already finished.
     */
    fun hasShiftEndedForToday(startTimeRaw: String, endTimeRaw: String): Boolean {
        val (startH, startM) = parseHourAndMinute(startTimeRaw, 9, 0)
        val (endH, endM) = parseHourAndMinute(endTimeRaw, 17, 0)
        val startTotal = startH * 60 + startM
        val endTotal = endH * 60 + endM
        val now = Calendar.getInstance()
        val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

        return if (endTotal < startTotal) {
            // Overnight shift starting today (e.g. 9 PM to 2 AM)
            // On the starting day, the shift has not ended yet (it concludes early next morning)
            false
        } else {
            curMinutes >= endTotal
        }
    }

    fun hasShiftEndedForToday(endTimeRaw: String): Boolean {
        var (endH, endM) = parseHourAndMinute(endTimeRaw, 17, 0)
        val now = Calendar.getInstance()
        val curMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
        val endMinutes = endH * 60 + endM
        return curMinutes >= endMinutes
    }

    /**
     * Generates dynamic consultation time slots for the doctor based on shift hours
     * with rock-solid guards against infinite loops and date mismatches.
     * Fully supports overnight shifts crossing midnight (e.g. 09:00 PM to 02:00 AM).
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
        val (rawEndH, rawEndM) = parseHourAndMinute(doctor.shiftEndTime, defaultHour = 17, defaultMinute = 0)

        // Parse selectedDate into year, month, day so real-time comparison works accurately
        val dateCal = Calendar.getInstance().apply {
            val dateParsed = try {
                SimpleDateFormat(DATE_FORMAT, Locale.US).parse(selectedDate)
            } catch (_: Exception) {
                null
            }
            if (dateParsed != null) {
                time = dateParsed
            }
        }

        val startCal = (dateCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, rawStartH)
            set(Calendar.MINUTE, rawStartM)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val endCal = (dateCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, rawEndH)
            set(Calendar.MINUTE, rawEndM)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If end time is before or equal to start time, it crosses midnight into the next day!
        if (!endCal.after(startCal)) {
            endCal.add(Calendar.DAY_OF_YEAR, 1)
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

        // Generate slots up to and including endCal (e.g. 10:00 PM to 05:00 AM includes 05:00 AM)
        while (currentSlotCal.timeInMillis <= endCal.timeInMillis && safetyIterations < 48) {
            safetyIterations++
            val slotStr = timeSdf.format(currentSlotCal.time).uppercase(Locale.getDefault())

            // Check if slot has already passed in real-time if selected date is today
            val isPassed = if (isToday) {
                currentSlotCal.timeInMillis <= now.timeInMillis
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
