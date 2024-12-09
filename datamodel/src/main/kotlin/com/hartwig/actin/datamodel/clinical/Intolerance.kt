package com.hartwig.actin.datamodel.clinical

import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

data class Intolerance(
    val name: String,
    val doids: Set<String>,
    val icdCode: String,
    val category: String? = null,
    val subcategories: Set<String>? = null,
    val type: String? = null,
    val clinicalStatus: String? = null,
    val verificationStatus: String? = null,
    val criticality: String? = null,
    val treatmentCategories: Set<TreatmentCategory>? = null
)
