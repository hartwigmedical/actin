package com.hartwig.actin.algo.ckb.json

import com.fasterxml.jackson.annotation.JsonProperty

data class CkbExtendedEvidenceEntry(
    val nctId: String,
    val title: String,
    val phase: String,
    val recruitment: String,
    var therapies: List<CkbTherapy>,
    val ageGroups: List<String>,
    val gender: String,
    val variantRequirements: String,
    val sponsors: String,
    val updateDate: String,
    val indications: List<CkbIndication>,
    val variantRequirementDetails: List<CkbVariantRequirementDetail>,
    val clinicalTrialLocations: List<CkbClinicalTrialLocation>,
    val coveredCountries: List<String>,
    @JsonProperty("trial_references") val trialReferences: List<CkbTrialReference>,
    @JsonProperty("other_trial_registration_numbers") val otherTrialRegistrationNumbers: String?,
    val masking: String,
    val allocation: String,
    @JsonProperty("cancer_stage") val cancerStage: String,
    @JsonProperty("disease_assessment") val diseaseAssessment: String,
    @JsonProperty("disease_assessment_criteria") val diseaseAssessmentCriteria: String,
    @JsonProperty("therapeutic_setting") val therapeuticSetting: String?
)
