package com.example.careflow.data.datasource

import com.example.careflow.domain.model.Appointment
import com.example.careflow.domain.model.AppointmentStatus
import com.example.careflow.domain.model.DailyQueue
import com.example.careflow.domain.model.Department
import com.example.careflow.domain.model.Doctor
import com.example.careflow.domain.model.Hospital
import com.example.careflow.domain.model.QueueStatus
import com.example.careflow.domain.model.User
import com.example.careflow.domain.model.UserRole
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Realistic Multi-Hospital, Department, Doctor, Patient, and Pre-scheduled Queue Seed Directory.
 * Single source of truth for CareFlow Hospital, Dr. Sarfaraz, and all network hospitals.
 */
object CareFlowSeedData {

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // =========================================================================
    // 1. HOSPITALS (First & Primary: CareFlow Hospital)
    // =========================================================================
    val hospitals: List<Hospital> = listOf(
        // Primary Hospital: CareFlow Multi-Specialty Hospital (Merged)
        Hospital(
            id = "hosp_main",
            name = "CareFlow Multi-Specialty Hospital",
            opdName = "Main Outpatient Block",
            address = "Opposite Civil Hospital Road",
            city = "Mumbai",
            phone = "7219889504",
            email = "sarf0203araz@gmail.com",
            adminId = "admin_default",
            adminEmail = "sarf0203araz@gmail.com",
            adminName = "Sarfaraz Shaikh",
            adminPhone = "7219889504",
            departmentIds = listOf(
                "dept_cf_cardio", "dept_cf_genmed", "dept_cf_ortho",
                "dept_cf_pedia", "dept_cf_derma", "dept_cf_neuro", "dept_cf_ent",
                "dept_cf_dental", "dept_cf_eye", "dept_neuro"
            )
        ),
        Hospital(
            id = "hosp_apollo_02",
            name = "Apollo Health City & Multi-Specialty Center",
            opdName = "Block C — OPD & Diagnostic Concourse",
            address = "154/11, Bannerghatta Main Road, Opp. IIM-B",
            city = "Bengaluru",
            phone = "+91 80 2630 4050",
            email = "admin@apollo.com",
            adminId = "adm-apollo-02",
            adminEmail = "admin@apollo.com",
            adminName = "Apollo Administrator",
            adminPhone = "+91 80 2630 4050",
            departmentIds = listOf(
                "dept_ap_cardio", "dept_ap_pedia", "dept_ap_ortho",
                "dept_ap_neuro", "dept_ap_ent", "dept_ap_dental", "dept_ap_eye"
            )
        ),
        Hospital(
            id = "hosp_max_03",
            name = "Max Super Speciality Hospital & Trauma Center",
            opdName = "Plaza Wing — Multi-Specialty Clinics",
            address = "1, 2, Press Enclave Marg, Saket Institutional Area",
            city = "New Delhi",
            phone = "+91 11 2651 5050",
            email = "admin@maxhealth.com",
            adminId = "adm-max-03",
            adminEmail = "admin@maxhealth.com",
            adminName = "Max Administrator",
            adminPhone = "+91 11 2651 5050",
            departmentIds = listOf(
                "dept_mx_cardio", "dept_mx_derma", "dept_mx_neuro", "dept_mx_genmed",
                "dept_mx_ent", "dept_mx_dental"
            )
        ),
        Hospital(
            id = "hosp_fortis_04",
            name = "Fortis Memorial Research Institute (FMRI)",
            opdName = "Ground Floor — Ambulatory Care Suite",
            address = "Sector 44, Opposite HUDA City Metro",
            city = "Gurugram",
            phone = "+91 124 492 1021",
            email = "admin@fortis.com",
            adminId = "adm-fortis-04",
            adminEmail = "admin@fortis.com",
            adminName = "Fortis Administrator",
            adminPhone = "+91 124 492 1021",
            departmentIds = listOf(
                "dept_ft_ortho", "dept_ft_pedia", "dept_ft_ent", "dept_ft_derma"
            )
        ),
        Hospital(
            id = "hosp_manipal_05",
            name = "Manipal Hospital & Advanced Care Center",
            opdName = "North Wing — OPD Chambers 1-20",
            address = "Survey No. 45/1, Kharadi Bypass, Near WTC",
            city = "Pune",
            phone = "+91 20 6723 8888",
            email = "admin@manipal.com",
            adminId = "adm-manipal-05",
            adminEmail = "admin@manipal.com",
            adminName = "Manipal Administrator",
            adminPhone = "+91 20 6723 8888",
            departmentIds = listOf(
                "dept_mn_cardio", "dept_mn_neuro", "dept_mn_derma", "dept_mn_dental"
            )
        )
    )

    // =========================================================================
    // 2. DEPARTMENTS
    // =========================================================================
    val departments: List<Department> = listOf(
        // CareFlow Multi-Specialty Hospital Departments (Mumbai)
        Department(
            id = "dept_cf_cardio",
            name = "Cardiology & Vascular Sciences",
            description = "Clinical cardiology, 2D Echo, preventive heart screenings & heart care",
            hospitalId = "hosp_main",
            doctorCount = 1,
            iconName = "heart"
        ),
        Department(
            id = "dept_cf_genmed",
            name = "General & Internal Medicine",
            description = "Preventive checkups, fever evaluations, hypertension & diabetes clinics",
            hospitalId = "hosp_main",
            doctorCount = 1,
            iconName = "stethoscope"
        ),
        Department(
            id = "dept_cf_ortho",
            name = "Orthopedics & Joint Care",
            description = "Bone health, arthroscopy, joint pain & sports medicine",
            hospitalId = "hosp_main",
            doctorCount = 1,
            iconName = "bone"
        ),
        Department(
            id = "dept_cf_pedia",
            name = "Pediatrics & Child Health",
            description = "Child development, newborn screenings, vaccination & general pediatrics",
            hospitalId = "hosp_main",
            doctorCount = 1,
            iconName = "child"
        ),
        Department(
            id = "dept_cf_derma",
            name = "Dermatology & Skin Care",
            description = "Clinical dermatology, eczema, allergies, and aesthetic skin health",
            hospitalId = "hosp_main",
            doctorCount = 1,
            iconName = "skin"
        ),
        Department(
            id = "dept_cf_neuro",
            name = "Neurology & Brain Sciences",
            description = "Management of headaches, migraines, nerve health and vertigo",
            hospitalId = "hosp_main",
            doctorCount = 2,
            iconName = "brain"
        ),
        Department(
            id = "dept_neuro",
            name = "Neurology",
            description = "Neurosurgery, spine, migraine and brain health consultations",
            hospitalId = "hosp_main",
            doctorCount = 2,
            iconName = "brain"
        ),
        Department(
            id = "dept_cf_ent",
            name = "ENT (Ear, Nose & Throat)",
            description = "Sinus care, hearing assessment, throat examinations & allergy management",
            hospitalId = "hosp_main",
            doctorCount = 1,
            iconName = "ent"
        ),
        Department(
            id = "dept_cf_dental",
            name = "Dental Surgery & Orthodontics",
            description = "Advanced dental care, root canals, braces, aligners & smile design",
            hospitalId = "hosp_main",
            doctorCount = 1,
            iconName = "dental"
        ),
        Department(
            id = "dept_cf_eye",
            name = "Ophthalmology & Vision Care",
            description = "Comprehensive eye exams, cataract screening, refractive & retina care",
            hospitalId = "hosp_main",
            doctorCount = 1,
            iconName = "eye"
        ),

        // Apollo Bengaluru Departments
        Department(
            id = "dept_ap_cardio",
            name = "Cardiology & Heart Institute",
            description = "Advanced preventive and surgical cardiac consultations",
            hospitalId = "hosp_apollo_02",
            doctorCount = 1,
            iconName = "heart"
        ),
        Department(
            id = "dept_ap_pedia",
            name = "Pediatrics & Adolescent Care",
            description = "Pediatric emergency, neonatal follow-ups, and childhood healthcare",
            hospitalId = "hosp_apollo_02",
            doctorCount = 1,
            iconName = "child"
        ),
        Department(
            id = "dept_ap_ortho",
            name = "Orthopedic Sciences & Trauma",
            description = "Sports medicine, robotic joint replacement, fractures and rehabilitation",
            hospitalId = "hosp_apollo_02",
            doctorCount = 1,
            iconName = "bone"
        ),
        Department(
            id = "dept_ap_neuro",
            name = "Neurology & Brain Sciences",
            description = "Management of stroke, epilepsy, neuromuscular disorders and migraines",
            hospitalId = "hosp_apollo_02",
            doctorCount = 1,
            iconName = "brain"
        ),
        Department(
            id = "dept_ap_ent",
            name = "ENT & Head-Neck Surgery",
            description = "Ear micro-surgeries, sinus treatments, voice and allergy clinics",
            hospitalId = "hosp_apollo_02",
            doctorCount = 1,
            iconName = "ent"
        ),
        Department(
            id = "dept_ap_dental",
            name = "Dental & Maxillofacial Sciences",
            description = "Implantology, orthodontics, cosmetic dentistry and oral surgery",
            hospitalId = "hosp_apollo_02",
            doctorCount = 1,
            iconName = "dental"
        ),
        Department(
            id = "dept_ap_eye",
            name = "Ophthalmology & Cornea Institute",
            description = "Cataract microsurgery, LASIK evaluations, glaucoma & pediatric eye care",
            hospitalId = "hosp_apollo_02",
            doctorCount = 1,
            iconName = "eye"
        ),

        // Max New Delhi Departments
        Department(
            id = "dept_mx_cardio",
            name = "Cardiology & Heart Care",
            description = "Cardiac OPD, lipid management, echocardiography & arrhythmia care",
            hospitalId = "hosp_max_03",
            doctorCount = 1,
            iconName = "heart"
        ),
        Department(
            id = "dept_mx_derma",
            name = "Dermatology & Cosmetology",
            description = "Skin rejuvenation, eczema treatments, hair and scalp therapies",
            hospitalId = "hosp_max_03",
            doctorCount = 1,
            iconName = "skin"
        ),
        Department(
            id = "dept_mx_neuro",
            name = "Neurology & Spine Clinic",
            description = "Comprehensive neurological examinations and neuro-diagnostics",
            hospitalId = "hosp_max_03",
            doctorCount = 1,
            iconName = "brain"
        ),
        Department(
            id = "dept_mx_genmed",
            name = "General & Family Medicine",
            description = "Outpatient diagnosis, acute fever, lifestyle disease care & checkups",
            hospitalId = "hosp_max_03",
            doctorCount = 1,
            iconName = "stethoscope"
        ),
        Department(
            id = "dept_mx_ent",
            name = "ENT & Cochlear Clinic",
            description = "Sinusitis, ear disorders, snoring evaluations & micro-laryngeal surgery",
            hospitalId = "hosp_max_03",
            doctorCount = 1,
            iconName = "ent"
        ),
        Department(
            id = "dept_mx_dental",
            name = "Dental Specialties & Aligners",
            description = "General and aesthetic dentistry, gum treatments & full mouth rehab",
            hospitalId = "hosp_max_03",
            doctorCount = 1,
            iconName = "dental"
        ),

        // Fortis Gurugram Departments
        Department(
            id = "dept_ft_ortho",
            name = "Orthopedics & Spine Institute",
            description = "Minimally invasive spine surgery, knee replacements & athletic trauma",
            hospitalId = "hosp_fortis_04",
            doctorCount = 1,
            iconName = "bone"
        ),
        Department(
            id = "dept_ft_pedia",
            name = "Pediatrics & Neonatology",
            description = "Specialized infant care, growth assessments, immunization and nutrition",
            hospitalId = "hosp_fortis_04",
            doctorCount = 1,
            iconName = "child"
        ),
        Department(
            id = "dept_ft_ent",
            name = "ENT & Cochlear Implant Center",
            description = "Hearing evaluation, endoscopic sinus procedures and voice therapy",
            hospitalId = "hosp_fortis_04",
            doctorCount = 1,
            iconName = "ent"
        ),
        Department(
            id = "dept_ft_derma",
            name = "Dermatology & Allergy Center",
            description = "Clinical skin care, psoriasis, eczema, pediatric allergies and laser care",
            hospitalId = "hosp_fortis_04",
            doctorCount = 1,
            iconName = "skin"
        ),

        // Manipal Pune Departments
        Department(
            id = "dept_mn_cardio",
            name = "Cardiology & Preventive Care",
            description = "Hypertension clinics, post-angioplasty care and diagnostic consultations",
            hospitalId = "hosp_manipal_05",
            doctorCount = 1,
            iconName = "heart"
        ),
        Department(
            id = "dept_mn_neuro",
            name = "Neurology & Neurophysiology",
            description = "Memory disorders, vertigo, headache, and peripheral nerve conditions",
            hospitalId = "hosp_manipal_05",
            doctorCount = 1,
            iconName = "brain"
        ),
        Department(
            id = "dept_mn_derma",
            name = "Dermatology & Skin Center",
            description = "Psoriasis, chronic dermatoses, pediatric dermatology & allergy tests",
            hospitalId = "hosp_manipal_05",
            doctorCount = 1,
            iconName = "skin"
        ),
        Department(
            id = "dept_mn_dental",
            name = "Dental & Oral Maxillofacial Surgery",
            description = "Dental implants, cosmetic smile correction, trauma and pediatric dental care",
            hospitalId = "hosp_manipal_05",
            doctorCount = 1,
            iconName = "dental"
        )
    )

    // =========================================================================
    // 3. DOCTORS (Lead: Dr. Sarfaraz Shaikh at CareFlow Hospital)
    // =========================================================================
    val doctors: List<Doctor> = listOf(
        // CareFlow Lead Doctor: Dr. Safaa Kazi (Neurosurgery & Neurology)
        Doctor(
            id = "doc_a42161",
            userId = "user_doc_safaa_kazi",
            hospitalId = "hosp_main",
            hospitalName = "CareFlow Multi-Specialty Hospital",
            roomNumber = "Chamber 105",
            name = "Dr. Safaa Kazi",
            specialization = "Consultant Neuro Surgeon & Neurologist",
            departmentId = "dept_cf_neuro",
            departmentName = "Neurology & Brain Sciences",
            qualification = "MBBS, MS (Neuro Surgery), MD",
            experienceYears = 6,
            consultationFee = 1000.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "02:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = true,
            rating = 4.9,
            totalReviews = 98,
            loginEmail = "dr.safaa.kazi@careflow.hospital",
            password = "Doctor@1234"
        ),

        // Lead Doctor: Dr. Sarfaraz Shaikh (Cardiology)
        Doctor(
            id = "DOC-CF-100",
            userId = "user_doc_sarfaraz",
            hospitalId = "hosp_main",
            hospitalName = "CareFlow Multi-Specialty Hospital",
            roomNumber = "Chamber 101",
            name = "Dr. Sarfaraz Shaikh",
            specialization = "Senior Consultant Physician & Cardiologist",
            departmentId = "dept_cf_cardio",
            departmentName = "Cardiology & Vascular Sciences",
            qualification = "MBBS, MD (Medicine), DM (Cardiology), FACC",
            experienceYears = 15,
            consultationFee = 800.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "02:00 PM",
            maxTokensPerDay = 35,
            isAvailable = true,
            isInOPD = true,
            rating = 4.9,
            totalReviews = 215,
            loginEmail = "dr.sarfaraz@careflow.hospital",
            password = "Doctor@1234"
        ),

        // CareFlow Multi-Specialty Doctors
        Doctor(
            id = "DOC-CF-102",
            userId = "user_doc_cf_102",
            hospitalId = "hosp_main",
            hospitalName = "CareFlow Multi-Specialty Hospital",
            roomNumber = "Chamber 104",
            name = "Dr. Ananya Deshmukh",
            specialization = "Pediatrician & Neonatal Health Specialist",
            departmentId = "dept_cf_pedia",
            departmentName = "Pediatrics & Child Health",
            qualification = "MBBS, MD (Pediatrics), DNB",
            experienceYears = 12,
            consultationFee = 700.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "10:00 AM",
            shiftEndTime = "02:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = true,
            rating = 4.8,
            totalReviews = 110,
            loginEmail = "dr.ananya.deshmukh@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-CF-103",
            userId = "user_doc_cf_103",
            hospitalId = "hosp_main",
            hospitalName = "CareFlow Multi-Specialty Hospital",
            roomNumber = "Chamber 108",
            name = "Dr. Rajesh Sharma",
            specialization = "Joint Replacement & Spine Surgeon",
            departmentId = "dept_cf_ortho",
            departmentName = "Orthopedics & Joint Care",
            qualification = "MBBS, MS (Ortho), MCh",
            experienceYears = 16,
            consultationFee = 900.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "09:30 AM",
            shiftEndTime = "01:30 PM",
            maxTokensPerDay = 30,
            isAvailable = true,
            isInOPD = true,
            rating = 4.9,
            totalReviews = 145,
            loginEmail = "dr.rajesh.sharma@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-CF-104",
            userId = "user_doc_cf_104",
            hospitalId = "hosp_main",
            hospitalName = "CareFlow Multi-Specialty Hospital",
            roomNumber = "Chamber 112",
            name = "Dr. Meera Iyer",
            specialization = "Clinical Dermatologist & Trichologist",
            departmentId = "dept_cf_derma",
            departmentName = "Dermatology & Skin Care",
            qualification = "MBBS, MD (Dermatology), FAAD",
            experienceYears = 11,
            consultationFee = 800.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "03:00 PM",
            shiftEndTime = "07:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = false,
            rating = 4.9,
            totalReviews = 112,
            loginEmail = "dr.meera.iyer@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-CF-105",
            userId = "user_doc_cf_105",
            hospitalId = "hosp_main",
            hospitalName = "CareFlow Multi-Specialty Hospital",
            roomNumber = "Chamber 115",
            name = "Dr. Sunil Joshi",
            specialization = "Consultant Physician & Diabetologist",
            departmentId = "dept_cf_genmed",
            departmentName = "General & Internal Medicine",
            qualification = "MBBS, MD (Internal Medicine)",
            experienceYears = 18,
            consultationFee = 600.0,
            avgConsultationMinutes = 10,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "02:00 PM",
            maxTokensPerDay = 35,
            isAvailable = true,
            isInOPD = true,
            rating = 4.7,
            totalReviews = 165,
            loginEmail = "dr.sunil.joshi@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-CF-106",
            userId = "user_doc_cf_106",
            hospitalId = "hosp_main",
            hospitalName = "CareFlow Multi-Specialty Hospital",
            roomNumber = "Chamber 120",
            name = "Dr. Ayesha Khan",
            specialization = "ENT & Micro-Endoscopic Surgeon",
            departmentId = "dept_cf_ent",
            departmentName = "ENT (Ear, Nose & Throat)",
            qualification = "MBBS, MS (ENT), DLO",
            experienceYears = 10,
            consultationFee = 650.0,
            avgConsultationMinutes = 12,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "02:00 PM",
            shiftEndTime = "06:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = false,
            rating = 4.8,
            totalReviews = 88,
            loginEmail = "dr.ayesha.khan@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-CF-107",
            userId = "user_doc_cf_107",
            hospitalId = "hosp_main",
            hospitalName = "CareFlow Multi-Specialty Hospital",
            roomNumber = "Chamber 116",
            name = "Dr. Farhan Shaikh",
            specialization = "Consultant Neurologist & Epilepsy Specialist",
            departmentId = "dept_cf_neuro",
            departmentName = "Neurology & Brain Sciences",
            qualification = "MBBS, MD, DM (Neurology)",
            experienceYears = 14,
            consultationFee = 900.0,
            avgConsultationMinutes = 20,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "10:00 AM",
            shiftEndTime = "02:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = true,
            rating = 4.9,
            totalReviews = 132,
            loginEmail = "dr.farhan.shaikh@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-CF-108",
            userId = "user_doc_cf_108",
            hospitalId = "hosp_main",
            hospitalName = "CareFlow Multi-Specialty Hospital",
            roomNumber = "Chamber 118",
            name = "Dr. Sana Khan",
            specialization = "Consultant Dental Surgeon & Orthodontist",
            departmentId = "dept_cf_dental",
            departmentName = "Dental Surgery & Orthodontics",
            qualification = "BDS, MDS (Orthodontics & Dentofacial Orthopedics)",
            experienceYears = 11,
            consultationFee = 600.0,
            avgConsultationMinutes = 20,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "09:30 AM",
            shiftEndTime = "01:30 PM",
            maxTokensPerDay = 20,
            isAvailable = true,
            isInOPD = true,
            rating = 4.8,
            totalReviews = 94,
            loginEmail = "dr.sana.khan@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-CF-109",
            userId = "user_doc_cf_109",
            hospitalId = "hosp_main",
            hospitalName = "CareFlow Multi-Specialty Hospital",
            roomNumber = "Chamber 122",
            name = "Dr. Tariq Mansoor",
            specialization = "Senior Consultant Ophthalmologist & Eye Surgeon",
            departmentId = "dept_cf_eye",
            departmentName = "Ophthalmology & Vision Care",
            qualification = "MBBS, MS (Ophthalmology), FICO",
            experienceYears = 15,
            consultationFee = 700.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "11:00 AM",
            shiftEndTime = "03:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = false,
            rating = 4.9,
            totalReviews = 108,
            loginEmail = "dr.tariq.mansoor@careflow.hospital",
            password = "Doctor@1234"
        ),

        // Apollo Bengaluru Doctors
        Doctor(
            id = "DOC-AP-201",
            userId = "user_doc_ap_201",
            hospitalId = "hosp_apollo_02",
            hospitalName = "Apollo Health City & Multi-Specialty Center",
            roomNumber = "Chamber 201",
            name = "Dr. Vikram Patel",
            specialization = "Senior Cardiac Consultant",
            departmentId = "dept_ap_cardio",
            departmentName = "Cardiology & Heart Institute",
            qualification = "MBBS, MD, DM (Cardiology)",
            experienceYears = 20,
            consultationFee = 1200.0,
            avgConsultationMinutes = 20,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "01:00 PM",
            maxTokensPerDay = 20,
            isAvailable = true,
            isInOPD = true,
            rating = 4.9,
            totalReviews = 210,
            loginEmail = "dr.vikram.patel@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-AP-202",
            userId = "user_doc_ap_202",
            hospitalId = "hosp_apollo_02",
            hospitalName = "Apollo Health City & Multi-Specialty Center",
            roomNumber = "Chamber 205",
            name = "Dr. Priya Nair",
            specialization = "Child Health & Pediatric Specialist",
            departmentId = "dept_ap_pedia",
            departmentName = "Pediatrics & Adolescent Care",
            qualification = "MBBS, DCH, DNB (Pediatrics)",
            experienceYears = 12,
            consultationFee = 750.0,
            avgConsultationMinutes = 12,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "10:00 AM",
            shiftEndTime = "02:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = true,
            rating = 4.8,
            totalReviews = 115,
            loginEmail = "dr.priya.nair@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-AP-203",
            userId = "user_doc_ap_203",
            hospitalId = "hosp_apollo_02",
            hospitalName = "Apollo Health City & Multi-Specialty Center",
            roomNumber = "Chamber 209",
            name = "Dr. Harish Rao",
            specialization = "Arthroscopy & Trauma Surgeon",
            departmentId = "dept_ap_ortho",
            departmentName = "Orthopedic Sciences & Trauma",
            qualification = "MBBS, MS (Orthopedics)",
            experienceYears = 14,
            consultationFee = 850.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Wed", "Fri", "Sat"),
            shiftStartTime = "02:00 PM",
            shiftEndTime = "06:00 PM",
            maxTokensPerDay = 24,
            isAvailable = true,
            isInOPD = false,
            rating = 4.8,
            totalReviews = 92,
            loginEmail = "dr.harish.rao@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-AP-204",
            userId = "user_doc_ap_204",
            hospitalId = "hosp_apollo_02",
            hospitalName = "Apollo Health City & Multi-Specialty Center",
            roomNumber = "Chamber 214",
            name = "Dr. Shalini Menon",
            specialization = "Consultant Neurologist",
            departmentId = "dept_ap_neuro",
            departmentName = "Neurology & Brain Sciences",
            qualification = "MBBS, MD, DM (Neurology)",
            experienceYears = 15,
            consultationFee = 1100.0,
            avgConsultationMinutes = 20,
            availableDays = listOf("Tue", "Thu", "Fri", "Sat"),
            shiftStartTime = "03:00 PM",
            shiftEndTime = "07:00 PM",
            maxTokensPerDay = 20,
            isAvailable = true,
            isInOPD = false,
            rating = 4.9,
            totalReviews = 104,
            loginEmail = "dr.shalini.menon@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-AP-205",
            userId = "user_doc_ap_205",
            hospitalId = "hosp_apollo_02",
            hospitalName = "Apollo Health City & Multi-Specialty Center",
            roomNumber = "Chamber 218",
            name = "Dr. Karthik Sundaram",
            specialization = "ENT & Head-Neck Surgeon",
            departmentId = "dept_ap_ent",
            departmentName = "ENT & Head-Neck Surgery",
            qualification = "MBBS, MS (ENT), DLO",
            experienceYears = 9,
            consultationFee = 650.0,
            avgConsultationMinutes = 12,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "09:30 AM",
            shiftEndTime = "01:30 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = true,
            rating = 4.7,
            totalReviews = 76,
            loginEmail = "dr.karthik.sundaram@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-AP-206",
            userId = "user_doc_ap_206",
            hospitalId = "hosp_apollo_02",
            hospitalName = "Apollo Health City & Multi-Specialty Center",
            roomNumber = "Chamber 222",
            name = "Dr. Ritu Hegde",
            specialization = "Dental Surgeon & Implantologist",
            departmentId = "dept_ap_dental",
            departmentName = "Dental & Maxillofacial Sciences",
            qualification = "BDS, MDS (Prosthodontics)",
            experienceYears = 11,
            consultationFee = 700.0,
            avgConsultationMinutes = 20,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "10:00 AM",
            shiftEndTime = "02:00 PM",
            maxTokensPerDay = 20,
            isAvailable = true,
            isInOPD = true,
            rating = 4.8,
            totalReviews = 84,
            loginEmail = "dr.ritu.hegde@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-AP-207",
            userId = "user_doc_ap_207",
            hospitalId = "hosp_apollo_02",
            hospitalName = "Apollo Health City & Multi-Specialty Center",
            roomNumber = "Chamber 226",
            name = "Dr. Mohan Kulkarni",
            specialization = "Cataract & Refractive Eye Surgeon",
            departmentId = "dept_ap_eye",
            departmentName = "Ophthalmology & Cornea Institute",
            qualification = "MBBS, MS (Ophthalmology), DO",
            experienceYears = 16,
            consultationFee = 850.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "01:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = false,
            rating = 4.9,
            totalReviews = 118,
            loginEmail = "dr.mohan.kulkarni@careflow.hospital",
            password = "Doctor@1234"
        ),

        // Max New Delhi Doctors
        Doctor(
            id = "DOC-MX-301",
            userId = "user_doc_mx_301",
            hospitalId = "hosp_max_03",
            hospitalName = "Max Super Speciality Hospital & Trauma Center",
            roomNumber = "Chamber 301",
            name = "Dr. Arjun Mehta",
            specialization = "Interventional Cardiologist",
            departmentId = "dept_mx_cardio",
            departmentName = "Cardiology & Heart Care",
            qualification = "MBBS, MD, DNB (Cardiology)",
            experienceYears = 17,
            consultationFee = 1100.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "01:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = true,
            rating = 4.9,
            totalReviews = 188,
            loginEmail = "dr.arjun.mehta@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-MX-302",
            userId = "user_doc_mx_302",
            hospitalId = "hosp_max_03",
            hospitalName = "Max Super Speciality Hospital & Trauma Center",
            roomNumber = "Chamber 306",
            name = "Dr. Sneha Verma",
            specialization = "Clinical & Aesthetic Dermatologist",
            departmentId = "dept_mx_derma",
            departmentName = "Dermatology & Cosmetology",
            qualification = "MBBS, MD (Skin & VD)",
            experienceYears = 10,
            consultationFee = 800.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "11:00 AM",
            shiftEndTime = "03:00 PM",
            maxTokensPerDay = 24,
            isAvailable = true,
            isInOPD = true,
            rating = 4.8,
            totalReviews = 95,
            loginEmail = "dr.sneha.verma@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-MX-303",
            userId = "user_doc_mx_303",
            hospitalId = "hosp_max_03",
            hospitalName = "Max Super Speciality Hospital & Trauma Center",
            roomNumber = "Chamber 310",
            name = "Dr. Alok Bansal",
            specialization = "Stroke & Neurophysiology Specialist",
            departmentId = "dept_mx_neuro",
            departmentName = "Neurology & Spine Clinic",
            qualification = "MBBS, MD, DM (Neurology)",
            experienceYears = 19,
            consultationFee = 1200.0,
            avgConsultationMinutes = 20,
            availableDays = listOf("Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "02:00 PM",
            shiftEndTime = "06:00 PM",
            maxTokensPerDay = 20,
            isAvailable = true,
            isInOPD = false,
            rating = 4.9,
            totalReviews = 130,
            loginEmail = "dr.alok.bansal@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-MX-304",
            userId = "user_doc_mx_304",
            hospitalId = "hosp_max_03",
            hospitalName = "Max Super Speciality Hospital & Trauma Center",
            roomNumber = "Chamber 315",
            name = "Dr. Neha Kapoor",
            specialization = "Family Physician & Lifestyle Medicine",
            departmentId = "dept_mx_genmed",
            departmentName = "General & Family Medicine",
            qualification = "MBBS, MD (General Medicine)",
            experienceYears = 12,
            consultationFee = 600.0,
            avgConsultationMinutes = 10,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "08:30 AM",
            shiftEndTime = "12:30 PM",
            maxTokensPerDay = 30,
            isAvailable = true,
            isInOPD = true,
            rating = 4.7,
            totalReviews = 145,
            loginEmail = "dr.neha.kapoor@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-MX-305",
            userId = "user_doc_mx_305",
            hospitalId = "hosp_max_03",
            hospitalName = "Max Super Speciality Hospital & Trauma Center",
            roomNumber = "Chamber 318",
            name = "Dr. Pradeep Oberoi",
            specialization = "ENT & Sinus Micro-Surgeon",
            departmentId = "dept_mx_ent",
            departmentName = "ENT & Cochlear Clinic",
            qualification = "MBBS, MS (ENT), FICS",
            experienceYears = 14,
            consultationFee = 750.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "01:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = true,
            rating = 4.8,
            totalReviews = 96,
            loginEmail = "dr.pradeep.oberoi@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-MX-306",
            userId = "user_doc_mx_306",
            hospitalId = "hosp_max_03",
            hospitalName = "Max Super Speciality Hospital & Trauma Center",
            roomNumber = "Chamber 322",
            name = "Dr. Ananya Chopra",
            specialization = "Aesthetic Dentist & Orthodontist",
            departmentId = "dept_mx_dental",
            departmentName = "Dental Specialties & Aligners",
            qualification = "BDS, MDS (Orthodontics)",
            experienceYears = 10,
            consultationFee = 650.0,
            avgConsultationMinutes = 20,
            availableDays = listOf("Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "11:00 AM",
            shiftEndTime = "03:00 PM",
            maxTokensPerDay = 20,
            isAvailable = true,
            isInOPD = false,
            rating = 4.8,
            totalReviews = 82,
            loginEmail = "dr.ananya.chopra@careflow.hospital",
            password = "Doctor@1234"
        ),

        // Fortis Gurugram Doctors
        Doctor(
            id = "DOC-FT-401",
            userId = "user_doc_ft_401",
            hospitalId = "hosp_fortis_04",
            hospitalName = "Fortis Memorial Research Institute (FMRI)",
            roomNumber = "Chamber 401",
            name = "Dr. Rohit Vashisht",
            specialization = "Robotic Knee & Hip Specialist",
            departmentId = "dept_ft_ortho",
            departmentName = "Orthopedics & Spine Institute",
            qualification = "MBBS, MS (Ortho), MCh (UK)",
            experienceYears = 16,
            consultationFee = 1000.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "01:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = true,
            rating = 4.9,
            totalReviews = 160,
            loginEmail = "dr.rohit.vashisht@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-FT-402",
            userId = "user_doc_ft_402",
            hospitalId = "hosp_fortis_04",
            hospitalName = "Fortis Memorial Research Institute (FMRI)",
            roomNumber = "Chamber 405",
            name = "Dr. Tanvi Sethi",
            specialization = "Developmental Pediatrics Specialist",
            departmentId = "dept_ft_pedia",
            departmentName = "Pediatrics & Neonatology",
            qualification = "MBBS, MD (Pediatrics), FIAP",
            experienceYears = 11,
            consultationFee = 800.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "10:00 AM",
            shiftEndTime = "02:00 PM",
            maxTokensPerDay = 24,
            isAvailable = true,
            isInOPD = true,
            rating = 4.8,
            totalReviews = 88,
            loginEmail = "dr.tanvi.sethi@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-FT-403",
            userId = "user_doc_ft_403",
            hospitalId = "hosp_fortis_04",
            hospitalName = "Fortis Memorial Research Institute (FMRI)",
            roomNumber = "Chamber 409",
            name = "Dr. Gaurav Chawla",
            specialization = "Endoscopic Sinus & Airway Surgeon",
            departmentId = "dept_ft_ent",
            departmentName = "ENT & Cochlear Implant Center",
            qualification = "MBBS, MS (Otorhinolaryngology)",
            experienceYears = 13,
            consultationFee = 700.0,
            avgConsultationMinutes = 12,
            availableDays = listOf("Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "02:00 PM",
            shiftEndTime = "06:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = false,
            rating = 4.7,
            totalReviews = 72,
            loginEmail = "dr.gaurav.chawla@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-FT-404",
            userId = "user_doc_ft_404",
            hospitalId = "hosp_fortis_04",
            hospitalName = "Fortis Memorial Research Institute (FMRI)",
            roomNumber = "Chamber 414",
            name = "Dr. Simran Chadha",
            specialization = "Consultant Dermatologist & Allergo-Immunologist",
            departmentId = "dept_ft_derma",
            departmentName = "Dermatology & Allergy Center",
            qualification = "MBBS, MD (Dermatology)",
            experienceYears = 12,
            consultationFee = 900.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "11:00 AM",
            shiftEndTime = "03:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = true,
            rating = 4.8,
            totalReviews = 91,
            loginEmail = "dr.simran.chadha@careflow.hospital",
            password = "Doctor@1234"
        ),

        // Manipal Pune Doctors
        Doctor(
            id = "DOC-MN-501",
            userId = "user_doc_mn_501",
            hospitalId = "hosp_manipal_05",
            hospitalName = "Manipal Hospital & Advanced Care Center",
            roomNumber = "Chamber 502",
            name = "Dr. Kavita Rao",
            specialization = "Clinical & Preventive Cardiologist",
            departmentId = "dept_mn_cardio",
            departmentName = "Cardiology & Preventive Care",
            qualification = "MBBS, MD, PGDCC",
            experienceYears = 14,
            consultationFee = 950.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "09:00 AM",
            shiftEndTime = "01:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = true,
            rating = 4.8,
            totalReviews = 110,
            loginEmail = "dr.kavita.rao@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-MN-502",
            userId = "user_doc_mn_502",
            hospitalId = "hosp_manipal_05",
            hospitalName = "Manipal Hospital & Advanced Care Center",
            roomNumber = "Chamber 507",
            name = "Dr. Manish Gokhale",
            specialization = "Headache & Epilepsy Neurologist",
            departmentId = "dept_mn_neuro",
            departmentName = "Neurology & Neurophysiology",
            qualification = "MBBS, MD, DM (Neurology)",
            experienceYears = 17,
            consultationFee = 1050.0,
            avgConsultationMinutes = 20,
            availableDays = listOf("Mon", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "10:30 AM",
            shiftEndTime = "02:30 PM",
            maxTokensPerDay = 20,
            isAvailable = true,
            isInOPD = true,
            rating = 4.9,
            totalReviews = 95,
            loginEmail = "dr.manish.gokhale@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-MN-503",
            userId = "user_doc_mn_503",
            hospitalId = "hosp_manipal_05",
            hospitalName = "Manipal Hospital & Advanced Care Center",
            roomNumber = "Chamber 511",
            name = "Dr. Pooja Jadhav",
            specialization = "Skin, Hair & Aesthetic Specialist",
            departmentId = "dept_mn_derma",
            departmentName = "Dermatology & Skin Center",
            qualification = "MBBS, DDVL",
            experienceYears = 9,
            consultationFee = 750.0,
            avgConsultationMinutes = 15,
            availableDays = listOf("Tue", "Wed", "Thu", "Fri", "Sat"),
            shiftStartTime = "03:00 PM",
            shiftEndTime = "07:00 PM",
            maxTokensPerDay = 25,
            isAvailable = true,
            isInOPD = false,
            rating = 4.7,
            totalReviews = 68,
            loginEmail = "dr.pooja.jadhav@careflow.hospital",
            password = "Doctor@1234"
        ),
        Doctor(
            id = "DOC-MN-504",
            userId = "user_doc_mn_504",
            hospitalId = "hosp_manipal_05",
            hospitalName = "Manipal Hospital & Advanced Care Center",
            roomNumber = "Chamber 515",
            name = "Dr. Rohan Salunkhe",
            specialization = "Dental & Maxillofacial Surgeon",
            departmentId = "dept_mn_dental",
            departmentName = "Dental & Oral Maxillofacial Surgery",
            qualification = "BDS, MDS (Oral & Maxillofacial)",
            experienceYears = 13,
            consultationFee = 800.0,
            avgConsultationMinutes = 20,
            availableDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri"),
            shiftStartTime = "09:30 AM",
            shiftEndTime = "01:30 PM",
            maxTokensPerDay = 20,
            isAvailable = true,
            isInOPD = true,
            rating = 4.8,
            totalReviews = 79,
            loginEmail = "dr.rohan.salunkhe@careflow.hospital",
            password = "Doctor@1234"
        )
    )

    // =========================================================================
    // 4. USERS (ADMINS, DOCTORS, PATIENTS)
    // =========================================================================

    // Administrators
    val adminUsers: List<User> = listOf(
        User(
            id = "admin_default",
            name = "CareFlow Hospital Administrator",
            email = "sarf0203araz@gmail.com",
            phone = "7219889504",
            role = UserRole.ADMIN,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        ),
        User(
            id = "adiPY1P5MiepIpTLJM7ZqvBgDM53",
            name = "Sarfaraz Shaikh",
            email = "sarf0203araz@gmail.com",
            phone = "7219889504",
            role = UserRole.ADMIN,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        ),
        User(
            id = "adm-apollo-02",
            name = "Apollo Hospital Administrator",
            email = "admin@apollo.com",
            phone = "+91 80 2630 4050",
            role = UserRole.ADMIN,
            hospitalId = "hosp_apollo_02",
            selectedHospitalId = "hosp_apollo_02"
        ),
        User(
            id = "adm-max-03",
            name = "Max Health Administrator",
            email = "admin@maxhealth.com",
            phone = "+91 11 2651 5050",
            role = UserRole.ADMIN,
            hospitalId = "hosp_max_03",
            selectedHospitalId = "hosp_max_03"
        ),
        User(
            id = "adm-fortis-04",
            name = "Fortis Health Administrator",
            email = "admin@fortis.com",
            phone = "+91 124 492 1021",
            role = UserRole.ADMIN,
            hospitalId = "hosp_fortis_04",
            selectedHospitalId = "hosp_fortis_04"
        ),
        User(
            id = "adm-manipal-05",
            name = "Manipal Hospital Administrator",
            email = "admin@manipal.com",
            phone = "+91 20 6723 8888",
            role = UserRole.ADMIN,
            hospitalId = "hosp_manipal_05",
            selectedHospitalId = "hosp_manipal_05"
        )
    )

    // Doctor User Accounts
    val doctorUsers: List<User> = doctors.map { doc ->
        User(
            id = doc.userId,
            name = doc.name,
            email = doc.loginEmail,
            phone = "",
            role = UserRole.DOCTOR,
            hospitalId = doc.hospitalId,
            selectedHospitalId = doc.hospitalId
        )
    }

    // Patient User Accounts (with assigned hospital)
    val patientUsers: List<User> = listOf(
        User(
            id = "patient_sarf0203araz_gmail_com",
            name = "Sarfaraz Shaikh",
            email = "sarf0203araz@gmail.com",
            phone = "7219889504",
            role = UserRole.PATIENT,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        ),
        User(
            id = "pat_rahul_01",
            name = "Rahul Sharma",
            email = "rahul.sharma@gmail.com",
            phone = "+919876543210",
            role = UserRole.PATIENT,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        ),
        User(
            id = "pat_pooja_02",
            name = "Pooja Verma",
            email = "pooja.verma@gmail.com",
            phone = "+919812345678",
            role = UserRole.PATIENT,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        ),
        User(
            id = "pat_amit_03",
            name = "Amit Patel",
            email = "amit.patel@gmail.com",
            phone = "+919823456789",
            role = UserRole.PATIENT,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        ),
        User(
            id = "pat_neha_04",
            name = "Neha Gupta",
            email = "neha.gupta@gmail.com",
            phone = "+919834567890",
            role = UserRole.PATIENT,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        ),
        User(
            id = "pat_vikas_05",
            name = "Vikas Joshi",
            email = "vikas.joshi@gmail.com",
            phone = "+919845678901",
            role = UserRole.PATIENT,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        ),
        User(
            id = "pat_sneha_06",
            name = "Sneha Kulkarni",
            email = "sneha.kulkarni@gmail.com",
            phone = "+919856789012",
            role = UserRole.PATIENT,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        ),
        User(
            id = "pat_zaid_07",
            name = "Mohammad Zaid",
            email = "zaid.khan@gmail.com",
            phone = "+919867890123",
            role = UserRole.PATIENT,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        ),
        User(
            id = "pat_farhan_08",
            name = "Farhan Akhtar",
            email = "farhan.akhtar@gmail.com",
            phone = "+919878901234",
            role = UserRole.PATIENT,
            hospitalId = "hosp_main",
            selectedHospitalId = "hosp_main"
        )
    )

    val allUsers: List<User> = adminUsers + doctorUsers + patientUsers

    // =========================================================================
    // 5. PRE-ASSIGNED APPOINTMENTS FOR DR. SARFARAZ (TODAY'S LIVE QUEUE)
    // =========================================================================
    fun getSeedAppointmentsForToday(): List<Appointment> {
        val today = getTodayDateString()
        val docSarfaraz = doctors.first { it.id == "DOC-CF-100" }

        return listOf(
            Appointment(
                id = "apt_sarfaraz_tok1",
                patientId = "pat_rahul_01",
                patientName = "Rahul Sharma",
                patientPhone = "+91 9876543210",
                doctorId = docSarfaraz.id,
                doctorName = docSarfaraz.name,
                departmentId = docSarfaraz.departmentId,
                departmentName = docSarfaraz.departmentName,
                hospitalId = docSarfaraz.hospitalId,
                hospitalName = docSarfaraz.hospitalName,
                tokenNumber = 1,
                date = today,
                timeSlot = "09:00 AM",
                status = AppointmentStatus.COMPLETED,
                notes = "Routine BP and cholesterol review. Prescription issued."
            ),
            Appointment(
                id = "apt_sarfaraz_tok2",
                patientId = "pat_pooja_02",
                patientName = "Pooja Verma",
                patientPhone = "+91 9812345678",
                doctorId = docSarfaraz.id,
                doctorName = docSarfaraz.name,
                departmentId = docSarfaraz.departmentId,
                departmentName = docSarfaraz.departmentName,
                hospitalId = docSarfaraz.hospitalId,
                hospitalName = docSarfaraz.hospitalName,
                tokenNumber = 2,
                date = today,
                timeSlot = "09:15 AM",
                status = AppointmentStatus.CALLED,
                notes = "Mild chest heaviness post-walking. ECG checkup."
            ),
            Appointment(
                id = "apt_sarfaraz_tok3",
                patientId = "pat_amit_03",
                patientName = "Amit Patel",
                patientPhone = "+91 9823456789",
                doctorId = docSarfaraz.id,
                doctorName = docSarfaraz.name,
                departmentId = docSarfaraz.departmentId,
                departmentName = docSarfaraz.departmentName,
                hospitalId = docSarfaraz.hospitalId,
                hospitalName = docSarfaraz.hospitalName,
                tokenNumber = 3,
                date = today,
                timeSlot = "09:30 AM",
                status = AppointmentStatus.WAITING,
                notes = "Follow up on high fasting blood sugar & BP medicine adjustment."
            ),
            Appointment(
                id = "apt_sarfaraz_tok4",
                patientId = "pat_neha_04",
                patientName = "Neha Gupta",
                patientPhone = "+91 9834567890",
                doctorId = docSarfaraz.id,
                doctorName = docSarfaraz.name,
                departmentId = docSarfaraz.departmentId,
                departmentName = docSarfaraz.departmentName,
                hospitalId = docSarfaraz.hospitalId,
                hospitalName = docSarfaraz.hospitalName,
                tokenNumber = 4,
                date = today,
                timeSlot = "09:45 AM",
                status = AppointmentStatus.WAITING,
                notes = "Pre-operative cardiac fitness evaluation."
            ),
            Appointment(
                id = "apt_sarfaraz_tok5",
                patientId = "pat_vikas_05",
                patientName = "Vikas Joshi",
                patientPhone = "+91 9845678901",
                doctorId = docSarfaraz.id,
                doctorName = docSarfaraz.name,
                departmentId = docSarfaraz.departmentId,
                departmentName = docSarfaraz.departmentName,
                hospitalId = docSarfaraz.hospitalId,
                hospitalName = docSarfaraz.hospitalName,
                tokenNumber = 5,
                date = today,
                timeSlot = "10:00 AM",
                status = AppointmentStatus.WAITING,
                notes = "Palpitations during exercise and stress."
            ),
            Appointment(
                id = "apt_sarfaraz_tok6",
                patientId = "pat_sneha_06",
                patientName = "Sneha Kulkarni",
                patientPhone = "+91 9856789012",
                doctorId = docSarfaraz.id,
                doctorName = docSarfaraz.name,
                departmentId = docSarfaraz.departmentId,
                departmentName = docSarfaraz.departmentName,
                hospitalId = docSarfaraz.hospitalId,
                hospitalName = docSarfaraz.hospitalName,
                tokenNumber = 6,
                date = today,
                timeSlot = "10:15 AM",
                status = AppointmentStatus.WAITING,
                notes = "General preventive cardiovascular health checkup."
            ),
            Appointment(
                id = "apt_sarfaraz_tok7",
                patientId = "pat_zaid_07",
                patientName = "Mohammad Zaid",
                patientPhone = "+91 9867890123",
                doctorId = docSarfaraz.id,
                doctorName = docSarfaraz.name,
                departmentId = docSarfaraz.departmentId,
                departmentName = docSarfaraz.departmentName,
                hospitalId = docSarfaraz.hospitalId,
                hospitalName = docSarfaraz.hospitalName,
                tokenNumber = 7,
                date = today,
                timeSlot = "10:30 AM",
                status = AppointmentStatus.WAITING,
                notes = "Shortness of breath on climbing stairs."
            ),
            Appointment(
                id = "apt_sarfaraz_tok8",
                patientId = "pat_farhan_08",
                patientName = "Farhan Akhtar",
                patientPhone = "+91 9878901234",
                doctorId = docSarfaraz.id,
                doctorName = docSarfaraz.name,
                departmentId = docSarfaraz.departmentId,
                departmentName = docSarfaraz.departmentName,
                hospitalId = docSarfaraz.hospitalId,
                hospitalName = docSarfaraz.hospitalName,
                tokenNumber = 8,
                date = today,
                timeSlot = "10:45 AM",
                status = AppointmentStatus.WAITING,
                notes = "Holter monitor test review & report discussion."
            )
        )
    }

    // DailyQueue instance for Dr. Sarfaraz for Today
    fun getSeedQueuesForToday(): Map<String, DailyQueue> {
        val today = getTodayDateString()
        val docSarfaraz = doctors.first { it.id == "DOC-CF-100" }
        val queueKey = "${docSarfaraz.id}_$today"

        val queue = DailyQueue(
            queueId = queueKey,
            doctorId = docSarfaraz.id,
            doctorName = docSarfaraz.name,
            hospitalId = docSarfaraz.hospitalId,
            date = today,
            currentToken = 2,
            totalTokensIssued = 8,
            status = QueueStatus.ACTIVE,
            updatedAt = System.currentTimeMillis()
        )

        return mapOf(queueKey to queue)
    }

    // Helper to get demo password
    fun getDemoPassword(identifier: String): String {
        val lower = identifier.lowercase().trim()
        val cleanPhone = lower.replace("+", "").replace(" ", "").replace("-", "")
        return when {
            cleanPhone == "7219889504" || cleanPhone.endsWith("7219889504") || lower == "sarf0203araz@gmail.com" -> "Admin@1234"
            lower.contains("admin") || lower.startsWith("adm-") -> "Admin@1234"
            lower.contains("dr.") || lower.startsWith("doc-") || lower.contains("@careflow.hospital") || lower.contains("sarfaraz") -> "Doctor@1234"
            else -> "Patient@1234"
        }
    }
}
