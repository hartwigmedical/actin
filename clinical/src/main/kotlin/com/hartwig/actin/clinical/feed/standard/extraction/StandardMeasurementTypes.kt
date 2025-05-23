package com.hartwig.actin.clinical.feed.standard.extraction

enum class MeasurementCategory {
    HEART_RATE,
    PULSE_OXIMETRY,
    `NON-INVASIVE_BLOOD_PRESSURE`,
    ARTERIAL_BLOOD_PRESSURE,
    BODY_WEIGHT,
    BODY_HEIGHT,
    BMI,
    OTHER
}

enum class MeasurementSubcategory {
    NA,
    SYSTOLIC_BLOOD_PRESSURE,
    DIASTOLIC_BLOOD_PRESSURE,
    MEAN_BLOOD_PRESSURE,
    OTHER
}

enum class MeasurementUnit {
    BPM,
    PERCENT,
    MMHG,
    KILOGRAMS,
    CENTIMETERS,
    KG_M2,
    OTHER
}