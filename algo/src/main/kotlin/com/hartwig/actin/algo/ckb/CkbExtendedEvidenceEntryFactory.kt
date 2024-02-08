package com.hartwig.actin.algo.ckb

import com.hartwig.actin.algo.ckb.datamodel.CkbExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.datamodel.ConfidenceInterval
import com.hartwig.actin.algo.ckb.datamodel.DerivedMetric
import com.hartwig.actin.algo.ckb.datamodel.PatientPopulation
import com.hartwig.actin.algo.ckb.datamodel.PrimaryEndPoint
import com.hartwig.actin.algo.ckb.datamodel.PrimaryEndPointType
import com.hartwig.actin.algo.ckb.datamodel.PrimaryEndPointUnit
import com.hartwig.actin.algo.ckb.datamodel.VariantRequirement
import com.hartwig.actin.algo.ckb.json.JsonCkbDerivedMetric
import com.hartwig.actin.algo.ckb.json.JsonCkbEndPointMetric
import com.hartwig.actin.algo.ckb.json.JsonCkbExtendedEvidenceEntry
import com.hartwig.actin.algo.ckb.json.JsonCkbPatientPopulation
import com.hartwig.actin.algo.ckb.json.JsonCkbVariantRequirementDetail
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import java.util.*

class CkbExtendedEvidenceEntryFactory {

    fun resolveCkbExtendedEvidence(jsonCkbExtendedEvidenceEntries: List<JsonCkbExtendedEvidenceEntry>) {
        for (jsonCkbExtendedEvidenceEntry in jsonCkbExtendedEvidenceEntries) {
            CkbExtendedEvidenceEntry(
                acronym = jsonCkbExtendedEvidenceEntry.title,
                phase = jsonCkbExtendedEvidenceEntry.phase,
                therapeuticSetting = Intent.valueOf(jsonCkbExtendedEvidenceEntry.therapeuticSetting!!.uppercase(Locale.getDefault())),
                variantRequirements = convertVariantRequirements(jsonCkbExtendedEvidenceEntry.variantRequirementDetails),
                stratificationFactors = null,
                url = "test",
                patientPopulations = convertPatientPopulations(
                    jsonCkbExtendedEvidenceEntry.trialReferences.iterator().next().patientPopulations
                )
            )
        }
    }

    private fun convertVariantRequirements(jsonVariantRequirements: List<JsonCkbVariantRequirementDetail>): List<VariantRequirement> {
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

    private fun convertPatientPopulations(jsonPatientPopulations: List<JsonCkbPatientPopulation>): Set<PatientPopulation> {
        val patientPopulations = mutableSetOf<PatientPopulation>()
        for (patientPopulation in jsonPatientPopulations) {
            patientPopulations.add(
                PatientPopulation(
                    name = patientPopulation.groupName,
                    isControl = patientPopulation.isControl,
                    ageMin = patientPopulation.ageMin.toInt(),
                    ageMax = patientPopulation.ageMax.toInt(),
                    ageMedian = patientPopulation.medianAge.toInt(),
                    numberOfPatients = patientPopulation.nPatients.toInt(),
                    numberOfMen = patientPopulation.nMale.toInt(),
                    numberOfWomen = patientPopulation.nFemale.toInt(),
                    who0 = patientPopulation.nEcog0!!.toInt(),
                    who1 = patientPopulation.nEcog1!!.toInt(),
                    who2 = patientPopulation.nEcog2!!.toInt(),
                    who3 = patientPopulation.nEcog3!!.toInt(),
                    who4 = patientPopulation.nEcog4!!.toInt(),
                    primaryTumorLocation = patientPopulation.nLocalizationPrimaryTumor,
                    mutations = patientPopulation.otherMutations,
                    primaryTumorRemovedComplete = patientPopulation.nPrimaryTumorRemovedComplete!!.toInt(),
                    primaryTumorRemovedPartial = patientPopulation.nPrimaryTumorRemovedPartial!!.toInt(),
                    primaryTumorRemoved = patientPopulation.nPrimaryTumorRemoved!!.toInt(),
                    metastaticSites = patientPopulation.metastaticSites,
                    priorSystemicTherapy = patientPopulation.nPriorSystemicTherapy!!.toInt(),
                    highMSI = patientPopulation.nHighMicrosatelliteStability!!.toInt(),
                    medianFollowUpForSurvival = patientPopulation.medianFollowUpForSurvival!!.toDouble(),
                    medianFollowUpPFS = patientPopulation.medianFollowUpForProgressionFreeSurvival!!.toDouble(),
                    primaryEndPoints = convertPrimaryEndPoints(patientPopulation.analysisGroups.iterator().next().endPointMetrics)
                )
            )
        }
        return patientPopulations
    }

    private fun convertPrimaryEndPoints(jsonPrimaryEndPoints: List<JsonCkbEndPointMetric>): Set<PrimaryEndPoint> {
        val primaryEndPoints = mutableSetOf<PrimaryEndPoint>()
        for (primaryEndPoint in jsonPrimaryEndPoints) {
            PrimaryEndPoint(
                id = primaryEndPoint.id,
                name = primaryEndPoint.endPoint.name,
                value = primaryEndPoint.value,
                unitOfMeasure = PrimaryEndPointUnit.valueOf(primaryEndPoint.endPoint.unitOfMeasure),
                confidenceInterval = ConfidenceInterval(
                    upperLimit = primaryEndPoint.confidenceInterval95!!.toDouble(),
                    lowerLimit = primaryEndPoint.confidenceInterval95.toDouble()
                ),
                type = PrimaryEndPointType.valueOf(primaryEndPoint.endPointType),
                derivedMetrics = convertDerivedMetric(primaryEndPoint.derivedMetrics)
            )
        }
        return primaryEndPoints
    }

    private fun convertDerivedMetric(jsonDerivedMetrics: List<JsonCkbDerivedMetric>): List<DerivedMetric> {
        val derivedMetrics = mutableListOf<DerivedMetric>()
        for (derivedMetric in jsonDerivedMetrics) {
            DerivedMetric(
                relativeMetricId = derivedMetric.relativeMetricId,
                value = derivedMetric.comparatorStatistic!!.toInt(),
                type = derivedMetric.comparatorStatisticType!!,
                confidenceInterval = ConfidenceInterval(
                    upperLimit = derivedMetric.confidenceInterval95Cs!!.toDouble(),
                    lowerLimit = derivedMetric.confidenceInterval95Cs.toDouble()
                ),
                pValue = derivedMetric.pValue.toDouble()
            )
        }
        return derivedMetrics
    }


}