package com.hartwig.actin.efficacy

import com.hartwig.actin.clinical.datamodel.treatment.Treatment

data class PatientPopulation(
    val name: String,
    val isControl: Boolean,
    val ageMin: Int,
    val ageMax: Int,
    val ageMedian: Double,
    val numberOfPatients: Int,
    val numberOfMale: Int? = null,
    val numberOfFemale: Int? = null,
    val patientsWithWho0: Int? = null,
    val patientsWithWho1: Int? = null,
    val patientsWithWho2: Int? = null,
    val patientsWithWho3: Int? = null,
    val patientsWithWho4: Int? = null,
    val patientsWithWho0to1: Int? = null,
    val patientsWithWho1to2: Int? = null,
    val patientsPerPrimaryTumorLocation: Map<String, Int>? = null,
    val mutations: String? = null,
    val patientsWithPrimaryTumorRemovedComplete: Int? = null,
    val patientsWithPrimaryTumorRemovedPartial: Int? = null,
    val patientsWithPrimaryTumorRemoved: Int? = null,
    val patientsPerMetastaticSites: Map<String, ValuePercentage>? = null,
    val timeOfMetastases: TimeOfMetastases? = null,
    val treatment: Treatment? = null,
    val priorSystemicTherapy: String? = null,
    val patientsWithMSI: Int? = null,
    val medianFollowUpForSurvival: String? = null,
    val medianFollowUpPFS: String? = null,
    val analysisGroups: List<AnalysisGroup> = emptyList(), // a patient population could have multiple different analysis groups
    val priorTherapies: String? = null,
    val patientsPerRace: Map<String, Int>? = null,
    val patientsPerRegion: Map<String, Int>? = null,
) {

    fun formatMetastaticSites(): String? = patientsPerMetastaticSites?.entries?.joinToString(", ") { (key, value) ->
        "$key: ${value.value} (${value.percentage}%)"
    }

    fun formatTumorLocation(separator: String): String? =
        patientsPerPrimaryTumorLocation?.entries?.joinToString(separator) { (key, value) ->
            "${key.replaceFirstChar(Char::uppercase)}: $value"
        }
}