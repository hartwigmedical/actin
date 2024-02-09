package com.hartwig.actin.algo.ckb.datamodel

data class PatientPopulation(
    val name: String,
    val isControl: Boolean,
    val ageMin: Int,
    val ageMax: Int,
    val ageMedian: Double,
    val numberOfPatients: Int,
    val numberOfMale: Int?,
    val numberOfFemale: Int?,
    val who0: Int?,
    val who1: Int?,
    val who2: Int?,
    val who3: Int?,
    val who4: Int?,
    val primaryTumorLocation: Map<String, Int>?,
    val mutations: String?,
    val primaryTumorRemovedComplete: Int?,
    val primaryTumorRemovedPartial: Int?,
    val primaryTumorRemoved: Int?,
    val metastaticSites: Map<String, ValuePercentage>?,
    val priorSystemicTherapy: String?,
    val highMSI: Int?,
    val medianFollowUpForSurvival: Double?,
    val medianFollowUpPFS: Double?,
    val analysisGroup: List<AnalysisGroup> // a patient population could have multiple different analysis groups
)