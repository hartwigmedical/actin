package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

data class Intolerance(
    val name: String,
    val doids: Set<String>,
    val category: String,
    val subcategories: Set<String>,
    val type: String,
    val clinicalStatus: String,
    val verificationStatus: String,
    val criticality: String,
    val treatmentCategory: Set<TreatmentCategory>,
)
