package com.hartwig.actin.clinical.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

data class Intolerance(
    val name: String,
    val doids: Set<String>,
    val category: String? = null,
    val subcategories: Set<String>? = null,
    val type: String? = null,
    val clinicalStatus: String? = null,
    val verificationStatus: String? = null,
    val criticality: String? = null,
    val treatmentCategories: Set<TreatmentCategory>? = null
)
