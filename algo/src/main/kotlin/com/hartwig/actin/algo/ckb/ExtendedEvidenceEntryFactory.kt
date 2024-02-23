package com.hartwig.actin.algo.ckb

import com.google.gson.Gson
import com.hartwig.actin.algo.ckb.json.CkbAnalysisGroup
import com.hartwig.actin.algo.ckb.json.CkbDerivedMetric
import com.hartwig.actin.algo.ckb.json.CkbEndPointMetric
import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.json.CkbPatientPopulation
import com.hartwig.actin.algo.ckb.json.CkbTherapy
import com.hartwig.actin.algo.ckb.json.CkbTrialReference
import com.hartwig.actin.algo.ckb.json.CkbVariantRequirementDetail
import com.hartwig.actin.algo.ckb.serialization.CkbExtendedEvidenceJson
import com.hartwig.actin.efficacy.AnalysisGroup
import com.hartwig.actin.efficacy.ConfidenceInterval
import com.hartwig.actin.efficacy.DerivedMetric
import com.hartwig.actin.efficacy.EfficacyEntry
import com.hartwig.actin.efficacy.PatientPopulation
import com.hartwig.actin.efficacy.PrimaryEndPoint
import com.hartwig.actin.efficacy.PrimaryEndPointType
import com.hartwig.actin.efficacy.PrimaryEndPointUnit
import com.hartwig.actin.efficacy.TimeOfMetastases
import com.hartwig.actin.efficacy.TrialReference
import com.hartwig.actin.efficacy.ValuePercentage
import com.hartwig.actin.efficacy.VariantRequirement
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.efficacy.Therapy

object ExtendedEvidenceEntryFactory {

    fun readEvidenceFromFile(ckbExtendedEvidenceJson: String): List<CkbExtendedEvidenceEntry> {
        return CkbExtendedEvidenceJson.read(ckbExtendedEvidenceJson)
    }

    fun extractCkbExtendedEvidence(ckbExtendedEvidenceEntries: List<CkbExtendedEvidenceEntry>): List<EfficacyEntry> {
        return ckbExtendedEvidenceEntries.map(::resolveCkbExtendedEvidence)
    }

    private fun resolveCkbExtendedEvidence(ckbExtendedEvidenceEntry: CkbExtendedEvidenceEntry): EfficacyEntry {
        return EfficacyEntry(
            acronym = ckbExtendedEvidenceEntry.title,
            phase = ckbExtendedEvidenceEntry.phase,
            therapies = convertTherapies(ckbExtendedEvidenceEntry.therapies),
            therapeuticSetting = ckbExtendedEvidenceEntry.therapeuticSetting?.let(::extractTherapeuticSettingFromString),
            variantRequirements = convertVariantRequirements(ckbExtendedEvidenceEntry.variantRequirementDetails),
            trialReferences = convertTrialReferences(ckbExtendedEvidenceEntry.trialReferences),
        )
    }

    fun convertTherapies(therapies: List<CkbTherapy>): List<Therapy> {
        return therapies.map { therapy -> Therapy(therapyName = therapy.therapyName, synonyms = therapy.synonyms) }
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
                patientsWithWho0to1 = patientPopulation.nEcog0to1?.toInt(),
                patientsWithWho1to2 = patientPopulation.nEcog1to2?.toInt(),
                patientsPerPrimaryTumorLocation = patientPopulation.nLocalizationPrimaryTumor?.let { convertPrimaryTumorLocation(it) },
                mutations = patientPopulation.otherMutations, //TODO: convert to map once CKB has made notation consistent
                patientsWithPrimaryTumorRemovedComplete = patientPopulation.nPrimaryTumorRemovedComplete?.toInt(),
                patientsWithPrimaryTumorRemovedPartial = patientPopulation.nPrimaryTumorRemovedPartial?.toInt(),
                patientsWithPrimaryTumorRemoved = patientPopulation.nPrimaryTumorRemoved?.toInt(),
                patientsPerMetastaticSites = patientPopulation.metastaticSites?.let { convertMetastaticSites(it) },
                timeOfMetastases = patientPopulation.timeOfMetastases?.let { convertTimeOfMetastases(it) },
                therapy = patientPopulation.therapy?.therapyName,
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
        } catch (e: Exception) {
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
                primaryEndPoints = convertPrimaryEndPoints(analysisGroup.endPointMetrics),
            )
        }
    }

    private fun convertPrimaryEndPoints(primaryEndPoints: List<CkbEndPointMetric>): List<PrimaryEndPoint> {
        return primaryEndPoints.map { primaryEndPoint ->
            PrimaryEndPoint(
                id = primaryEndPoint.id,
                name = primaryEndPoint.endPoint.name,
                value = convertPrimaryEndPointValue(
                    primaryEndPoint.value,
                    primaryEndPoint.endPoint.unitOfMeasure
                ),
                unitOfMeasure = if (primaryEndPoint.endPoint.unitOfMeasure == "Y/N") {
                    PrimaryEndPointUnit.YES_OR_NO
                } else {
                    try {
                        PrimaryEndPointUnit.valueOf(primaryEndPoint.endPoint.unitOfMeasure.uppercase())
                    } catch (e: Exception) {
                        throw IllegalStateException("Unknown primary end point unit measure: $primaryEndPoint.endPoint.unitOfMeasure")
                    }
                },
                confidenceInterval = primaryEndPoint.confidenceInterval95?.let(::convertConfidenceInterval),
                type = PrimaryEndPointType.valueOf(primaryEndPoint.endPointType.uppercase()),
                derivedMetrics = convertDerivedMetric(primaryEndPoint.derivedMetrics)
            )
        }
    }

    fun convertPrimaryEndPointValue(value: String, unit: String): Double? {
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