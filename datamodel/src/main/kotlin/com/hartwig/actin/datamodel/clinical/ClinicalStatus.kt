package com.hartwig.actin.datamodel.clinical

data class ClinicalStatus(
    val infectionStatus: InfectionStatus? = null,
    val lvef: Double? = null
)
