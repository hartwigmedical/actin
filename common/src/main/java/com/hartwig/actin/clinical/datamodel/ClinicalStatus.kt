package com.hartwig.actin.clinical.datamodel

data class ClinicalStatus(
    val who: Int? = null,
    val infectionStatus: InfectionStatus? = null,
    val ecg: ECG? = null,
    val lvef: Double? = null,
    val hasComplications: Boolean? = null
)
