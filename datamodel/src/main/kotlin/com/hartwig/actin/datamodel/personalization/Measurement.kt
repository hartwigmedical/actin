package com.hartwig.actin.datamodel.personalization

const val MIN_PATIENT_COUNT = 20

data class Measurement(val value: Double, val numPatients: Int, val min: Int? = null, val max: Int? = null, val iqr: Double? = null)
