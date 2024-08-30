package com.hartwig.actin.algo.ckb

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.algo.ckb.json.CkbAnalysisGroup
import com.hartwig.actin.algo.ckb.json.CkbDerivedMetric
import com.hartwig.actin.algo.ckb.json.CkbEndPointMetric
import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.json.CkbPatientPopulation
import com.hartwig.actin.algo.ckb.json.CkbTrialReference
import com.hartwig.actin.algo.ckb.json.CkbVariantRequirementDetail
import com.hartwig.actin.algo.ckb.serialization.CkbExtendedEvidenceJson
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.efficacy.AnalysisGroup
import com.hartwig.actin.datamodel.efficacy.ConfidenceInterval
import com.hartwig.actin.datamodel.efficacy.DerivedMetric
import com.hartwig.actin.datamodel.efficacy.EfficacyEntry
import com.hartwig.actin.datamodel.efficacy.EndPoint
import com.hartwig.actin.datamodel.efficacy.EndPointType
import com.hartwig.actin.datamodel.efficacy.EndPointUnit
import com.hartwig.actin.datamodel.efficacy.PatientPopulation
import com.hartwig.actin.datamodel.efficacy.TimeOfMetastases
import com.hartwig.actin.datamodel.efficacy.TrialReference
import com.hartwig.actin.datamodel.efficacy.ValuePercentage
import com.hartwig.actin.datamodel.efficacy.VariantRequirement

class EfficacyEntryFactory(private val treatmentDatabase: TreatmentDatabase) {

    fun extractEfficacyEvidenceFromCkbFile(ckbExtendedEvidenceJson: String): List<EfficacyEntry> {
        return convertCkbExtendedEvidence(CkbExtendedEvidenceJson.read(ckbExtendedEvidenceJson))
    }

    fun convertCkbExtendedEvidence(ckbExtendedEvidenceEntries: List<CkbExtendedEvidenceEntry>): List<EfficacyEntry> {
        return ckbExtendedEvidenceEntries.map(::resolveCkbExtendedEvidence)
    }

    private fun resolveCkbExtendedEvidence(ckbExtendedEvidenceEntry: CkbExtendedEvidenceEntry): EfficacyEntry {
        return EfficacyEntry(
            acronym = ckbExtendedEvidenceEntry.title,
            phase = ckbExtendedEvidenceEntry.phase,
            treatments = ckbExtendedEvidenceEntry.therapies.map { findTreatmentInDatabase(it.therapyName, it.synonyms) },
            therapeuticSetting = ckbExtendedEvidenceEntry.therapeuticSetting?.let(::extractTherapeuticSettingFromString),
            variantRequirements = convertVariantRequirements(ckbExtendedEvidenceEntry.variantRequirementDetails),
            trialReferences = convertTrialReferences(ckbExtendedEvidenceEntry.trialReferences),
        )
    }

    private fun findTreatmentInDatabase(therapyName: String, therapySynonyms: String?): Treatment {
        return generateOptions((therapySynonyms?.split("|") ?: emptyList()) + therapyName)
            .mapNotNull(treatmentDatabase::findTreatmentByName)
            .distinct().singleOrNull()
            ?: throw IllegalStateException("Multiple or no matches found in treatment.json for therapy: $therapyName")
    }

    fun generateOptions(therapies: List<String>): List<String> {
        return therapies.flatMap { therapy ->
            if (therapy.contains(" + ")) {
                permutations(therapy.uppercase().split(" + ")).map { it.joinToString("+") }
            } else {
                listOf(therapy.uppercase())
            }
        }
    }

    fun permutations(drugs: List<String>): Set<List<String>> {
        if (drugs.isEmpty()) return setOf(emptyList())

        val result: MutableSet<List<String>> = mutableSetOf()
        for (i in drugs.indices) {
            permutations(drugs - drugs[i]).forEach { item ->
                result.add(item + drugs[i])
            }
        }
        return result
    }

    fun extractTherapeuticSettingFromString(therapeuticSetting: String): Intent {
        try {
            return Intent.valueOf(therapeuticSetting.uppercase())
        } catch (e: Exception) {
            throw IllegalStateException("Unknown therapeutic setting: $therapeuticSetting ")
        }
    }

    fun convertVariantRequirements(variantRequirements: List<CkbVariantRequirementDetail>): List<VariantRequirement> {
        return variantRequirements.map { variantRequirement ->
            VariantRequirement(
                name = variantRequirement.molecularProfile.profileName,
                requirementType = variantRequirement.requirementType
            )
        }
    }

    private fun convertTrialReferences(trialReferences: List<CkbTrialReference>): List<TrialReference> {
        return trialReferences.map { trialReference ->
            TrialReference(
                url = trialReference.reference.url,
                patientPopulations = convertPatientPopulations(trialReference.patientPopulations)
            )
        }
    }

    private fun convertPatientPopulations(patientPopulations: List<CkbPatientPopulation>): List<PatientPopulation> {
        return patientPopulations.map { patientPopulation ->
            PatientPopulation(
                name = patientPopulation.groupName,
                isControl = patientPopulation.isControl,
                ageMin = patientPopulation.ageMin.toInt(),
                ageMax = patientPopulation.ageMax.toInt(),
                ageMedian = patientPopulation.medianAge.toDouble(),
                numberOfPatients = patientPopulation.nPatients.toInt(),
                numberOfMale = convertGender(patientPopulation.nMale, patientPopulation.nFemale, patientPopulation.nPatients),
                numberOfFemale = convertGender(patientPopulation.nFemale, patientPopulation.nMale, patientPopulation.nPatients),
                patientsWithWho0 = patientPopulation.nEcog0?.toInt(),
                patientsWithWho1 = patientPopulation.nEcog1?.toInt(),
                patientsWithWho2 = patientPopulation.nEcog2?.toInt(),
                patientsWithWho3 = patientPopulation.nEcog3?.toInt(),
                patientsWithWho4 = patientPopulation.nEcog4?.toInt(),
                patientsWithWho0to1 = if (patientPopulation.nEcog0.isNullOrEmpty() && patientPopulation.nEcog1.isNullOrEmpty()) patientPopulation.nEcog0to1?.toInt() else null,
                patientsWithWho1to2 = if (patientPopulation.nEcog1.isNullOrEmpty() && patientPopulation.nEcog2.isNullOrEmpty()) patientPopulation.nEcog1to2?.toInt() else null,
                patientsPerPrimaryTumorLocation = patientPopulation.nLocalizationPrimaryTumor?.let { convertPrimaryTumorLocation(it) },
                mutations = patientPopulation.otherMutations, //TODO: convert to map once CKB has made notation consistent
                patientsWithPrimaryTumorRemovedComplete = patientPopulation.nPrimaryTumorRemovedComplete?.toInt(),
                patientsWithPrimaryTumorRemovedPartial = patientPopulation.nPrimaryTumorRemovedPartial?.toInt(),
                patientsWithPrimaryTumorRemoved = patientPopulation.nPrimaryTumorRemoved?.toInt(),
                patientsPerMetastaticSites = patientPopulation.metastaticSites?.let { convertMetastaticSites(it) },
                timeOfMetastases = patientPopulation.timeOfMetastases?.let { convertTimeOfMetastases(it) },
                treatment = patientPopulation.therapy?.let { findTreatmentInDatabase(it.therapyName, it.synonyms) },
                priorSystemicTherapy = patientPopulation.nPriorSystemicTherapy, //TODO: convert to number or percentage
                patientsWithMSI = patientPopulation.nHighMicrosatelliteStability?.toInt(),
                medianFollowUpForSurvival = patientPopulation.medianFollowUpForSurvival,
                medianFollowUpPFS = patientPopulation.medianFollowUpForProgressionFreeSurvival,
                analysisGroups = convertAnalysisGroup(patientPopulation.analysisGroups),
                priorTherapies = patientPopulation.priorTherapies,
                patientsPerRace = if (patientPopulation.race.isNotEmpty()) convertRaceOrRegion(patientPopulation.race) else null,
                patientsPerRegion = if (patientPopulation.region.isNotEmpty()) convertRaceOrRegion(patientPopulation.region) else null,
            )
        }
    }

    fun convertTimeOfMetastases(timeOfMetastases: String): TimeOfMetastases {
        try {
            return TimeOfMetastases.valueOf(timeOfMetastases.uppercase())
        } catch (e: Exception) {
            throw IllegalStateException("Unknown time of metastases: $timeOfMetastases")
        }
    }

    fun convertGender(numberOfGender: String?, numberOfOtherGender: String?, numberOfPatients: String): Int? {
        return numberOfGender?.toInt() ?: numberOfOtherGender?.let { numberOfPatients.toInt() - numberOfOtherGender.toInt() }
    }

    fun convertPrimaryTumorLocation(primaryTumorLocations: String): Map<String, Int> {
        return try {
            primaryTumorLocations.let { Gson().fromJson(it, hashMapOf<String, Int>()::class.java) }
        } catch (e: JsonSyntaxException) {
            val regex = """^(\w+): (\d+)(?: \(\d+(?:\.\d+)?%\))?$""".toRegex()
            primaryTumorLocations.split(", ").associate { item ->
                regex.find(item)?.let {
                    val (label, value) = it.destructured
                    label.trim() to value.toInt()
                } ?: throw IllegalStateException("Incorrect primary tumor locations formatting: $primaryTumorLocations")
            }
        }
    }

    fun convertMetastaticSites(metastaticSites: String): Map<String, ValuePercentage> {
        val regex = """^(.*): (\d+) \((\d+(?:\.\d+)?)%\)?$""".toRegex()
        return metastaticSites.split(",").associate { item ->
            regex.find(item)?.let {
                val (label, value, percentage) = it.destructured
                label.trim() to ValuePercentage(value.toInt(), percentage.toDouble())
            } ?: throw IllegalStateException("Incorrect metastatic site formatting: $metastaticSites")
        }
    }

    fun convertRaceOrRegion(raceOrRegion: String): Map<String, Int> {
        val regex = """^(.*?):\s*(\d+)$""".toRegex()
        return raceOrRegion.replace("\n", "").split(",").associate { item ->
            regex.find(item)?.let {
                val (label, value) = it.destructured
                label.trim() to value.toInt()
            } ?: throw IllegalStateException("Incorrect race or region formatting: $raceOrRegion")
        }
    }

    private fun convertAnalysisGroup(analysisGroups: List<CkbAnalysisGroup>): List<AnalysisGroup> {
        return analysisGroups.map { analysisGroup ->
            AnalysisGroup(
                id = analysisGroup.id,
                name = analysisGroup.name,
                nPatients = analysisGroup.nPatients.toInt(),
                endPoints = convertEndPoints(analysisGroup.endPointMetrics),
            )
        }
    }

    private fun convertEndPoints(EndPoints: List<CkbEndPointMetric>): List<EndPoint> {
        return EndPoints.map { EndPoint ->
            EndPoint(
                id = EndPoint.id,
                name = EndPoint.endPoint.name,
                value = convertEndPointValue(
                    EndPoint.value,
                    EndPoint.endPoint.unitOfMeasure
                ),
                unitOfMeasure = if (EndPoint.endPoint.unitOfMeasure == "Y/N") {
                    EndPointUnit.YES_OR_NO
                } else {
                    try {
                        EndPointUnit.valueOf(EndPoint.endPoint.unitOfMeasure.uppercase())
                    } catch (e: Exception) {
                        throw IllegalStateException("Unknown end point unit measure: $EndPoint.endPoint.unitOfMeasure")
                    }
                },
                confidenceInterval = EndPoint.confidenceInterval95?.let(::convertConfidenceInterval),
                type = EndPointType.valueOf(EndPoint.endPointType.uppercase()),
                derivedMetrics = convertDerivedMetric(EndPoint.derivedMetrics)
            )
        }
    }

    fun convertEndPointValue(value: String, unit: String): Double? {
        return if (unit == "Y/N") {
            when (value) {
                "Y" -> {
                    1.0
                }

                "N" -> {
                    0.0
                }

                else -> throw IllegalStateException("Incorrect primary end point value found: $value")
            }
        } else {
            if (value == "NR") {
                null
            } else {
                value.toDoubleOrNull() ?: throw IllegalStateException("Incorrect primary end point value found: $value")
            }
        }
    }

    fun convertDerivedMetric(derivedMetrics: List<CkbDerivedMetric>): List<DerivedMetric> {
        return derivedMetrics.map { derivedMetric ->
            DerivedMetric(
                relativeMetricId = derivedMetric.relativeMetricId,
                value = derivedMetric.comparatorStatistic?.toDouble(),
                type = derivedMetric.comparatorStatisticType,
                confidenceInterval = derivedMetric.confidenceInterval95Cs?.let(::convertConfidenceInterval),
                pValue = derivedMetric.pValue
            )
        }
    }

    fun convertConfidenceInterval(confidenceInterval: String): ConfidenceInterval {
        if (confidenceInterval.contains("-")) {
            val confidenceIntervalSplit = confidenceInterval.split("-")
            return ConfidenceInterval(
                lowerLimit = confidenceIntervalSplit[0].toDoubleOrNull(),
                upperLimit = confidenceIntervalSplit[1].toDoubleOrNull()
            )
        } else {
            throw IllegalStateException("Incorrect confidence interval found: $confidenceInterval")
        }
    }
}