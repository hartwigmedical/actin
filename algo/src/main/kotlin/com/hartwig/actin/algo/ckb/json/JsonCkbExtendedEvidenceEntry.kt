package com.hartwig.actin.algo.ckb.json

import com.google.gson.annotations.SerializedName

data class JsonCkbExtendedEvidenceEntry(
    val nctId: String,
    val title: String,
    val phase: String,
    val recruitment: String,
    val therapies: List<JsonCkbTherapy>,
    val ageGroups: List<String>,
    val gender: String,
    val variantRequirements: String,
    val sponsors: String,
    val updateDate: String,
    val indications: List<JsonCkbIndication>,
    val variantRequirementDetails: List<JsonCkbVariantRequirementDetail>,
    val clinicalTrialLocations: List<JsonCkbClinicalTrialLocation>,
    val coveredCountries: List<String>,
    @SerializedName("trial_references") val trialReferences: List<JsonCkbTrialReference>,
    @SerializedName("other_trial_registration_numbers") val otherTrialRegistrationNumbers: String?,
    val masking: String,
    val allocation: String,
    @SerializedName("cancer_stage") val cancerStage: String,
    @SerializedName("disease_assessment") val diseaseAssessment: String,
    @SerializedName("disease_assessment_criteria") val diseaseAssessmentCriteria: String,
    @SerializedName("therapeutic_setting") val therapeuticSetting: String?
)
