package com.hartwig.actin.algo.ckb.datamodel

data class CkbExtendedEvidenceEntry(
    val nctId: String,
    val title: String,
    val phase: String,
    val recruitment: String,
    val therapies: List<CkbTherapy>,
    val ageGroups: List<String>,
    val gender: String,
    val variantRequirements: String,
    val sponsors: String,
    val updateDate: String,
    val indications: List<CkbIndication>,
    val variantRequirementDetails: List<CkbVariantRequirementDetail>,
    val clinicalTrialLocations: List<String>,
    val coveredCountries: List<String>,
    val trialReferences: List<CkbTrialReference>,
    val otherTrialRegistrationNumbers: String?,
    val masking: String,
    val allocation: String,
    val cancerStage: String,
    val diseaseAssessment: String,
    val diseaseAssessmentCriteria: String,
    val therapeuticSetting: String?
)
