package com.hartwig.actin.datamodel.clinical

data class PriorPrimary(
    val name: String,
    val doids: Set<String> = emptySet(),
    val diagnosedYear: Int? = null,
    val diagnosedMonth: Int? = null,
    val treatmentHistory: String,
    val lastTreatmentYear: Int? = null,
    val lastTreatmentMonth: Int? = null,
    val status: TumorStatus
)
