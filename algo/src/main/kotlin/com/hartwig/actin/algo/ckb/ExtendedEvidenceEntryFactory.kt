package com.hartwig.actin.algo.ckb

import com.google.gson.Gson
import com.hartwig.actin.algo.ckb.datamodel.AnalysisGroup
import com.hartwig.actin.algo.ckb.datamodel.ExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.datamodel.ConfidenceInterval
import com.hartwig.actin.algo.ckb.datamodel.DerivedMetric
import com.hartwig.actin.algo.ckb.datamodel.PatientPopulation
import com.hartwig.actin.algo.ckb.datamodel.PrimaryEndPoint
import com.hartwig.actin.algo.ckb.datamodel.PrimaryEndPointType
import com.hartwig.actin.algo.ckb.datamodel.PrimaryEndPointUnit
import com.hartwig.actin.algo.ckb.datamodel.TrialReference
import com.hartwig.actin.algo.ckb.datamodel.ValuePercentage
import com.hartwig.actin.algo.ckb.datamodel.VariantRequirement
import com.hartwig.actin.algo.ckb.json.CkbAnalysisGroup
import com.hartwig.actin.algo.ckb.json.CkbDerivedMetric
import com.hartwig.actin.algo.ckb.json.CkbEndPointMetric
import com.hartwig.actin.algo.ckb.json.CkbExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.json.CkbPatientPopulation
import com.hartwig.actin.algo.ckb.json.CkbTrialReference
import com.hartwig.actin.algo.ckb.json.CkbVariantRequirementDetail
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

object ExtendedEvidenceEntryFactory {

    fun extractCkbExtendedEvidence(jsonCkbExtendedEvidenceEntries: List<CkbExtendedEvidenceEntry>): List<ExtendedEvidenceEntry> {
        return jsonCkbExtendedEvidenceEntries.map(::resolveCkbExtendedEvidence)
    }

    private fun resolveCkbExtendedEvidence(jsonCkbExtendedEvidenceEntry: CkbExtendedEvidenceEntry): ExtendedEvidenceEntry {
        return ExtendedEvidenceEntry(
            acronym = jsonCkbExtendedEvidenceEntry.title,
            phase = jsonCkbExtendedEvidenceEntry.phase,
            therapeuticSetting = jsonCkbExtendedEvidenceEntry.therapeuticSetting?.let(::extractTherapeuticSettingFromString),
            variantRequirements = convertVariantRequirements(jsonCkbExtendedEvidenceEntry.variantRequirementDetails),
            trialReferences = convertTrialReferences(jsonCkbExtendedEvidenceEntry.trialReferences),
        )
    }

    fun extractTherapeuticSettingFromString(therapeuticSetting: String): Intent {
        try {
            return Intent.valueOf(therapeuticSetting.uppercase())
        } catch (e: Exception) {
            throw IllegalStateException("Unknown therapeutic setting: $therapeuticSetting ")
        }
    }

    fun convertVariantRequirements(jsonVariantRequirements: List<CkbVariantRequirementDetail>): List<VariantRequirement> {
        return jsonVariantRequirements.map { variantRequirement ->
            VariantRequirement(
                name = variantRequirement.molecularProfile.profileName,
                requirementType = variantRequirement.requirementType
            )
        }
    }

    private fun convertTrialReferences(jsonTrialReferences: List<CkbTrialReference>): List<TrialReference> {
        return jsonTrialReferences.map { jsonTrialReference ->
            TrialReference(
                url = jsonTrialReference.reference.url,
                patientPopulations = convertPatientPopulations(jsonTrialReference.patientPopulations)
            )
        }
    }

    private fun convertPatientPopulations(jsonPatientPopulations: List<CkbPatientPopulation>): List<PatientPopulation> {
        return jsonPatientPopulations.map { patientPopulation ->
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
                patientsPerPrimaryTumorLocation = convertPrimaryTumorLocation(patientPopulation.nLocalizationPrimaryTumor),
                mutations = patientPopulation.otherMutations, //TODO: convert to map once CKB has made notation consistent
                patientsWithPrimaryTumorRemovedComplete = patientPopulation.nPrimaryTumorRemovedComplete?.toInt(),
                patientsWithPrimaryTumorRemovedPartial = patientPopulation.nPrimaryTumorRemovedPartial?.toInt(),
                patientsWithPrimaryTumorRemoved = patientPopulation.nPrimaryTumorRemoved?.toInt(),
                patientsPerMetastaticSites = patientPopulation.metastaticSites?.let { convertMetastaticSites(it) },
                priorSystemicTherapy = patientPopulation.nPriorSystemicTherapy, //TODO: convert to number or percentage
                patientsWithMSI = patientPopulation.nHighMicrosatelliteStability?.toInt(),
                medianFollowUpForSurvival = patientPopulation.medianFollowUpForSurvival?.toDouble(),
                medianFollowUpPFS = patientPopulation.medianFollowUpForProgressionFreeSurvival?.toDouble(),
                analysisGroups = convertAnalysisGroup(patientPopulation.analysisGroups)
            )
        }
    }

    fun convertGender(numberOfGender: String?, numberOfOtherGender: String?, numberOfPatients: String): Int? {
        return numberOfGender?.toInt() ?: numberOfOtherGender?.let { numberOfPatients.toInt() - numberOfOtherGender.toInt() }
    }

    fun convertPrimaryTumorLocation(jsonPrimaryTumorLocations: String?): Map<String, Int>? {
        return jsonPrimaryTumorLocations?.let { Gson().fromJson(it, hashMapOf<String, Int>()::class.java) }
    }

    fun convertMetastaticSites(jsonMetastaticSites: String): Map<String, ValuePercentage> {
        val regex = """^(.*): (\d+) \((\d+(?:\.\d+)?)%\)?$""".toRegex()
        return jsonMetastaticSites.split(", ").associate { item ->
            regex.find(item)?.let {
                val (label, value, percentage) = it.destructured
                label.trim() to ValuePercentage(value.toInt(), percentage.toDouble())
            } ?: throw IllegalStateException("Incorrect metastatic site formatting $jsonMetastaticSites")
        }
    }

    private fun convertAnalysisGroup(jsonAnalysisGroups: List<CkbAnalysisGroup>): List<AnalysisGroup> {
        return jsonAnalysisGroups.map { jsonAnalysisGroup ->
            AnalysisGroup(
                id = jsonAnalysisGroup.id,
                primaryEndPoints = convertPrimaryEndPoints(jsonAnalysisGroup.endPointMetrics),
            )
        }
    }

    private fun convertPrimaryEndPoints(jsonPrimaryEndPoints: List<CkbEndPointMetric>): List<PrimaryEndPoint> {
        return jsonPrimaryEndPoints.map { primaryEndPoint ->
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

    fun convertDerivedMetric(jsonDerivedMetrics: List<CkbDerivedMetric>): List<DerivedMetric> {
        return jsonDerivedMetrics.map { jsonDerivedMetric ->
            DerivedMetric(
                relativeMetricId = jsonDerivedMetric.relativeMetricId,
                value = jsonDerivedMetric.comparatorStatistic?.toDouble(),
                type = jsonDerivedMetric.comparatorStatisticType,
                confidenceInterval = jsonDerivedMetric.confidenceInterval95Cs?.let(::convertConfidenceInterval),
                pValue = jsonDerivedMetric.pValue
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