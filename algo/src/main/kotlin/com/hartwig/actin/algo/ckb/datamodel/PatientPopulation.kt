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
    val patientsWithWho0: Int?,
    val patientsWithWho1: Int?,
    val patientsWithWho2: Int?,
    val patientsWithWho3: Int?,
    val patientsWithWho4: Int?,
    val patientsPerPrimaryTumorLocation: Map<String, Int>?,
    val mutations: String?,
    val patientsWithPrimaryTumorRemovedComplete: Int?,
    val patientsWithPrimaryTumorRemovedPartial: Int?,
    val patientsWithPrimaryTumorRemoved: Int?,
    val patientsPerMetastaticSites: Map<String, ValuePercentage>?,
    val priorSystemicTherapy: String?,
    val patientsWithMSI: Int?,
    val medianFollowUpForSurvival: Double?,
    val medianFollowUpPFS: Double?,
    val analysisGroups: List<AnalysisGroup> // a patient population could have multiple different analysis groups
)