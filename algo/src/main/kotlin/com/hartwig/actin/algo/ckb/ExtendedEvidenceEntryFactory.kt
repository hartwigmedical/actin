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
import java.util.*
import kotlin.collections.HashMap

object ExtendedEvidenceEntryFactory {

    fun extractCkbExtendedEvidence(jsonCkbExtendedEvidenceEntries: List<CkbExtendedEvidenceEntry>): List<ExtendedEvidenceEntry> {
        val ckbExtendedEvidenceEntries = mutableListOf<ExtendedEvidenceEntry>()
        for (jsonCkbExtendedEvidenceEntry in jsonCkbExtendedEvidenceEntries) {
            ckbExtendedEvidenceEntries.add(resolveCkbExtendedEvidence(jsonCkbExtendedEvidenceEntry))
        }
        return ckbExtendedEvidenceEntries
    }

    private fun resolveCkbExtendedEvidence(jsonCkbExtendedEvidenceEntry: CkbExtendedEvidenceEntry): ExtendedEvidenceEntry {
        return ExtendedEvidenceEntry(
            acronym = jsonCkbExtendedEvidenceEntry.title,
            phase = jsonCkbExtendedEvidenceEntry.phase,
            therapeuticSetting = jsonCkbExtendedEvidenceEntry.therapeuticSetting?.uppercase(Locale.getDefault())
                ?.let {
                    try {
                        Intent.valueOf(it)
                    } catch (e: Exception) {
                        throw IllegalStateException("Unknown therapeutic setting: $jsonCkbExtendedEvidenceEntry.therapeuticSetting ")
                    }
                },
            variantRequirements = convertVariantRequirements(jsonCkbExtendedEvidenceEntry.variantRequirementDetails),
            trialReferences = convertTrialReferences(jsonCkbExtendedEvidenceEntry.trialReferences),
        )
    }

    fun convertVariantRequirements(jsonVariantRequirements: List<CkbVariantRequirementDetail>): List<VariantRequirement> {
        val variantRequirements = mutableListOf<VariantRequirement>()
        for (variantRequirement in jsonVariantRequirements) {
            variantRequirements.add(
                VariantRequirement(
                    name = variantRequirement.molecularProfile.profileName,
                    requirementType = variantRequirement.requirementType
                )
            )
        }
        return variantRequirements
    }

    private fun convertTrialReferences(jsonTrialReferences: List<CkbTrialReference>): List<TrialReference> {
        val trialReferences = mutableListOf<TrialReference>()
        for (jsonTrialReference in jsonTrialReferences) {
            trialReferences.add(
                TrialReference(
                    url = jsonTrialReference.reference.url,
                    patientPopulations = convertPatientPopulations(jsonTrialReference.patientPopulations)
                )
            )
        }
        return trialReferences
    }

    private fun convertPatientPopulations(jsonPatientPopulations: List<CkbPatientPopulation>): Set<PatientPopulation> {
        val patientPopulations = mutableSetOf<PatientPopulation>()
        for (patientPopulation in jsonPatientPopulations) {
            patientPopulations.add(
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
                    primaryTumorLocation = convertPrimaryTumorLocation(patientPopulation.nLocalizationPrimaryTumor),
                    mutations = patientPopulation.otherMutations, //TODO: convert to map once CKB has made notation consistent
                    patientsWithPrimaryTumorRemovedComplete = patientPopulation.nPrimaryTumorRemovedComplete?.toInt(),
                    patientsWithPrimaryTumorRemovedPartial = patientPopulation.nPrimaryTumorRemovedPartial?.toInt(),
                    patientsWithPrimaryTumorRemoved = patientPopulation.nPrimaryTumorRemoved?.toInt(),
                    metastaticSites = patientPopulation.metastaticSites?.let { convertMetastaticSites(it) },
                    priorSystemicTherapy = patientPopulation.nPriorSystemicTherapy, //TODO: convert to number or percentage
                    patientsWithMSI = patientPopulation.nHighMicrosatelliteStability?.toInt(),
                    medianFollowUpForSurvival = patientPopulation.medianFollowUpForSurvival?.toDouble(),
                    medianFollowUpPFS = patientPopulation.medianFollowUpForProgressionFreeSurvival?.toDouble(),
                    analysisGroups = convertAnalysisGroup(patientPopulation.analysisGroups)
                )
            )
        }
        return patientPopulations
    }

    fun convertGender(numberOfGender: String?, numberOfOtherGender: String?, numberOfPatients: String): Int? {
        return numberOfGender?.toInt()
            ?: if (numberOfOtherGender != null) {
                numberOfPatients.toInt() - numberOfOtherGender.toInt()
            } else {
                null
            }
    }

    fun convertPrimaryTumorLocation(jsonPrimaryTumorLocations: String?): Map<String, Int>? {
        val returnType: Map<String, Int> = HashMap()
        return if (jsonPrimaryTumorLocations == null) {
            null
        } else Gson().fromJson(jsonPrimaryTumorLocations, returnType.javaClass)
    }

    fun convertMetastaticSites(jsonMetastaticSites: String): Map<String, ValuePercentage> {
        val metastaticSites = mutableMapOf<String, ValuePercentage>()
        val list = jsonMetastaticSites.split(", ")
        for (item in list) {
            //TODO: use regex
            val itemStripped = item.replace("(", "").replace(")", "").replace("%", "")
            val tumorTypeSplit = itemStripped.split(":")
            val values = tumorTypeSplit[1].split(" ")
            metastaticSites[tumorTypeSplit[0]] = ValuePercentage(value = values[1].toInt(), percentage = values[2].toDouble())
        }
        return metastaticSites
    }

    private fun convertAnalysisGroup(jsonAnalysisGroups: List<CkbAnalysisGroup>): List<AnalysisGroup> {
        val analysisGroups = mutableListOf<AnalysisGroup>()
        for (jsonAnalysisGroup in jsonAnalysisGroups) {
            analysisGroups.add(
                AnalysisGroup(
                    id = jsonAnalysisGroup.id,
                    primaryEndPoints = convertPrimaryEndPoints(jsonAnalysisGroup.endPointMetrics),
                )
            )
        }
        return analysisGroups
    }

    private fun convertPrimaryEndPoints(jsonPrimaryEndPoints: List<CkbEndPointMetric>): Set<PrimaryEndPoint> {
        val primaryEndPoints = mutableSetOf<PrimaryEndPoint>()
        for (primaryEndPoint in jsonPrimaryEndPoints) {
            primaryEndPoints.add(
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
                    confidenceInterval = ConfidenceInterval(
                        lowerLimit = primaryEndPoint.confidenceInterval95?.let { convertConfidenceInterval(it)[0].toDoubleOrNull() },
                        upperLimit = primaryEndPoint.confidenceInterval95?.let { convertConfidenceInterval(it)[1].toDoubleOrNull() }
                    ),
                    type = PrimaryEndPointType.valueOf(primaryEndPoint.endPointType.uppercase()),
                    derivedMetrics = convertDerivedMetric(primaryEndPoint.derivedMetrics)
                )
            )
        }
        return primaryEndPoints
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
                value.toDouble()
            }
        }
    }

    fun convertDerivedMetric(jsonDerivedMetrics: List<CkbDerivedMetric>): List<DerivedMetric> {
        val derivedMetrics = mutableListOf<DerivedMetric>()
        for (derivedMetric in jsonDerivedMetrics) {
            derivedMetrics.add(
                DerivedMetric(
                    relativeMetricId = derivedMetric.relativeMetricId,
                    value = derivedMetric.comparatorStatistic?.toDouble(),
                    type = derivedMetric.comparatorStatisticType,
                    confidenceInterval = ConfidenceInterval(
                        lowerLimit = derivedMetric.confidenceInterval95Cs?.let { convertConfidenceInterval(it)[0].toDoubleOrNull() },
                        upperLimit = derivedMetric.confidenceInterval95Cs?.let { convertConfidenceInterval(it)[1].toDoubleOrNull() }
                    ),
                    pValue = derivedMetric.pValue
                )
            )
        }
        return derivedMetrics
    }

    fun convertConfidenceInterval(confidenceInterval: String): List<String> {
        if (confidenceInterval.contains("-")) {
            return confidenceInterval.split("-")
        } else {
            throw IllegalStateException("Incorrect confidence interval found: $confidenceInterval")
        }
    }
}