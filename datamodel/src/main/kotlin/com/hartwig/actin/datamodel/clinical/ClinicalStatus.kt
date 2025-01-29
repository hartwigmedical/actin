package com.hartwig.actin.datamodel.clinical

data class ClinicalStatus(
    val who: Int? = null,
    val infectionStatus: InfectionStatus? = null,
    val ecg: Ecg? = null,
    val lvef: Double? = null,
    val hasComplications: Boolean? = null
)
