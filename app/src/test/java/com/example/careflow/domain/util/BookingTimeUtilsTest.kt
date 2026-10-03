package com.example.careflow.domain.util

import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.Doctor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BookingTimeUtilsTest {

    private val testDoctor = Doctor(
        id = "doc_safa",
        userId = "user_doc_safa",
        name = "Dr. Safa Kazi",
        specialization = "Cardiologist",
        departmentId = "dept_cardio",
        departmentName = "Cardiology",
        qualification = "MBBS, MD (Cardiology)",
        experienceYears = 12,
        consultationFee = 600.0,
        shiftStartTime = "09:00 AM",
        shiftEndTime = "05:00 PM"
    )

    @Test
    fun testGenerateOneWeekDaysReturns8Days() {
        val days = BookingTimeUtils.generateOneWeekDays()
        assertEquals(8, days.size)
        assertEquals("Today", days[0].dayName)
        assertEquals("Tomorrow", days[1].dayName)
        assertTrue(days[0].isToday)
        assertFalse(days[1].isToday)
    }

    @Test
    fun testGenerateDoctorSlotsSlicesShiftAndMarksBookedSlots() {
        val testDate = "2026-10-03"

        // Simulate appointments from 09:00 AM to 03:00 PM (12 appointments)
        val bookedTimeSlots = listOf(
            "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM",
            "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM",
            "01:00 PM", "01:30 PM", "02:00 PM", "02:30 PM"
        )

        val appointments = bookedTimeSlots.mapIndexed { index, slot ->
            Appointment(
                id = "apt_$index",
                patientId = "patient_$index",
                patientName = "Patient $index",
                patientPhone = "9876543210",
                doctorId = testDoctor.id,
                doctorName = testDoctor.name,
                departmentName = testDoctor.departmentName,
                date = testDate,
                timeSlot = slot,
                tokenNumber = index + 1,
                status = AppointmentStatus.BOOKED
            )
        }

        val slots = BookingTimeUtils.generateDoctorSlots(
            doctor = testDoctor,
            existingAppointments = appointments,
            selectedDate = testDate,
            slotIntervalMinutes = 30
        )

        // Shift 9:00 AM to 5:00 PM in 30 min intervals = 16 slots
        assertEquals(16, slots.size)

        // First 12 slots (09:00 AM to 02:30 PM) must be booked
        for (i in 0 until 12) {
            assertTrue("Slot ${slots[i].timeSlot} should be booked", slots[i].isBooked)
            assertEquals(i + 1, slots[i].bookedTokenNumber)
        }

        // Slots starting from 03:00 PM onwards must be available
        val availableSlots = slots.subList(12, 16)
        assertEquals(4, availableSlots.size)
        assertEquals("03:00 PM", availableSlots[0].timeSlot)
        assertEquals("03:30 PM", availableSlots[1].timeSlot)
        assertEquals("04:00 PM", availableSlots[2].timeSlot)
        assertEquals("04:30 PM", availableSlots[3].timeSlot)

        availableSlots.forEach { slot ->
            assertFalse("Slot ${slot.timeSlot} should be available", slot.isBooked)
        }
    }

    @Test
    fun testDoctorWithWeirdShiftEndTimeDoesNotHangOrCrash() {
        // User's exact situation from screenshot: "09:00 AM - 0500 Am"
        val doctorWithWeirdEndTime = testDoctor.copy(
            shiftStartTime = "09:00 AM",
            shiftEndTime = "0500 Am"
        )

        val slots = BookingTimeUtils.generateDoctorSlots(
            doctor = doctorWithWeirdEndTime,
            existingAppointments = emptyList(),
            selectedDate = "2026-10-03"
        )

        // Should parse 0500 Am cleanly to 5:00 PM and generate exactly 16 slots without infinite loop
        assertEquals(16, slots.size)
        assertEquals("09:00 AM", slots.first().timeSlot)
        assertEquals("04:30 PM", slots.last().timeSlot)
    }

    @Test
    fun testFormatDisplayShiftCleansUpWeirdInputs() {
        val formatted = BookingTimeUtils.formatDisplayShift("09:00 AM", "0500 Am")
        assertEquals("09:00 AM - 05:00 PM", formatted)
    }

    @Test
    fun testCalculateNextPredictedToken() {
        val testDate = "2026-10-03"
        val appointments = (1..12).map { token ->
            Appointment(
                id = "apt_$token",
                patientId = "patient_$token",
                patientName = "Patient $token",
                patientPhone = "9876543210",
                doctorId = testDoctor.id,
                doctorName = testDoctor.name,
                departmentName = testDoctor.departmentName,
                date = testDate,
                timeSlot = "09:00 AM",
                tokenNumber = token
            )
        }

        val nextToken = BookingTimeUtils.calculateNextPredictedToken(appointments, testDate, testDoctor.id)
        assertEquals(13, nextToken)
    }
}
