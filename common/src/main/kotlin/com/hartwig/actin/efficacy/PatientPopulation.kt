package com.hartwig.actin.efficacy

import com.hartwig.actin.clinical.datamodel.treatment.Treatment

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
    val patientsWithWho0to1: Int?,
    val patientsWithWho1to2: Int?,
    val patientsPerPrimaryTumorLocation: Map<String, Int>?,
    val mutations: String?,
    val patientsWithPrimaryTumorRemovedComplete: Int?,
    val patientsWithPrimaryTumorRemovedPartial: Int?,
    val patientsWithPrimaryTumorRemoved: Int?,
    val patientsPerMetastaticSites: Map<String, ValuePercentage>?,
    val timeOfMetastases: TimeOfMetastases?,
    val treatment: Treatment?,
    val priorSystemicTherapy: String?,
    val patientsWithMSI: Int?,
    val medianFollowUpForSurvival: String?,
    val medianFollowUpPFS: String?,
    val analysisGroups: List<AnalysisGroup>, // a patient population could have multiple different analysis groups
    val priorTherapies: String?,
    val patientsPerRace: Map<String, Int>?,
    val patientsPerRegion: Map<String, Int>?,
) {

    fun formatMetastaticSites(): String? = patientsPerMetastaticSites?.entries?.joinToString(", ") { (key, value) ->
        "$key: ${value.value} (${value.percentage}%)"
    }

    fun formatTumorLocation(separator: String): String? =
        patientsPerPrimaryTumorLocation?.entries?.joinToString(separator) { (key, value) ->
            "${key.replaceFirstChar(Char::uppercase)}: $value"
        }
}