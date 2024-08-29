package com.hartwig.actin.datamodel.clinical

data class PriorSecondPrimary(
    val tumorLocation: String,
    val tumorSubLocation: String,
    val tumorType: String,
    val tumorSubType: String,
    val doids: Set<String> = emptySet(),
    val diagnosedYear: Int? = null,
    val diagnosedMonth: Int? = null,
    val treatmentHistory: String,
    val lastTreatmentYear: Int? = null,
    val lastTreatmentMonth: Int? = null,
    val status: TumorStatus
)
