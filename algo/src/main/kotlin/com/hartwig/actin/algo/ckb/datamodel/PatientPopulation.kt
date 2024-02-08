package com.hartwig.actin.algo.ckb.datamodel

data class PatientPopulation(
    val name: String,
    val isControl: Boolean,
    val ageMin: Int,
    val ageMax: Int,
    val ageMedian: Int,
    val numberOfPatients: Int,
    val numberOfMen: Int,
    val numberOfWomen: Int,
    val who0: Int?,
    val who1: Int?,
    val who2: Int?,
    val who3: Int?,
    val who4: Int?,
    val primaryTumorLocation: String?,
    val mutations: String?,
    val primaryTumorRemovedComplete: Int?,
    val primaryTumorRemovedPartial: Int?,
    val primaryTumorRemoved: Int?,
    val metastaticSites: String,
    val priorSystemicTherapy: Int,
    val highMSI: Int,
    val medianFollowUpForSurvival: Double,
    val medianFollowUpPFS: Double,
    val primaryEndPoints: Set<PrimaryEndPoint>?
)