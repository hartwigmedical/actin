package com.hartwig.actin.algo.ckb.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

data class CkbExtendedEvidenceEntry(
    val acronym: String,
    val phase: String,
    val therapeuticSetting: Intent,
    val variantRequirements: List<VariantRequirement>,
    val stratificationFactors: Set<String>? = null,
    val url: String,
    val patientPopulations: Set<PatientPopulation>?
)