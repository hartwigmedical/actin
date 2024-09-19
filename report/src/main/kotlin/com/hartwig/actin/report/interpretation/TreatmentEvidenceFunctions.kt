package com.hartwig.actin.report.interpretation

import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.serve.datamodel.EvidenceLevelDetails

object TreatmentEvidenceFunctions {

    data class TreatmentEvidenceContent(val treatment: String, val cancerTypesWithDate: String, val isResistant: Boolean)

    fun filterTreatmentEvidence(treatmentEvidenceSet: Set<TreatmentEvidence>, isOnLabel: Boolean?): Set<TreatmentEvidence> {
        val (onLabelEvidence, offLabelEvidence) = treatmentEvidenceSet.partition { it.onLabel }
        val onLabelHighestEvidenceLevels = getHighestEvidenceLevelPerTreatment(onLabelEvidence)

        val evidence = when (isOnLabel) {
            true -> onLabelEvidence
            false -> filterOffLabelEvidence(offLabelEvidence, onLabelHighestEvidenceLevels)
            else -> onLabelEvidence + offLabelEvidence
        }.toSet()

        val onlyBenefitAndResistanceEvidence = onlyIncludeBenefitAndResistanceEvidence(evidence)
        val preClinicalFilteredEvidence = filterPreClinicalEvidence(onlyBenefitAndResistanceEvidence)
        val levelDFilteredEvidence = filterLevelDWhenAorBExists(preClinicalFilteredEvidence)
        return prioritizeNonCategoryEvidence(levelDFilteredEvidence)
    }

    fun getHighestEvidenceLevelPerTreatment(onLabelEvidence: List<TreatmentEvidence>): Map<String, EvidenceLevel?> {
        return groupByTreatment(onLabelEvidence).mapValues { (_, evidences) -> evidences.minOfOrNull { it.evidenceLevel } }
    }

    fun filterOffLabelEvidence(
        offLabelEvidence: List<TreatmentEvidence>,
        onLabelHighestEvidencePerTreatment: Map<String, EvidenceLevel?>
    ): Set<TreatmentEvidence> {
        return offLabelEvidence.filter { offLabel ->
            val highestOnLabelLevel = onLabelHighestEvidencePerTreatment[offLabel.treatment]
            highestOnLabelLevel == null || offLabel.evidenceLevel < highestOnLabelLevel
        }.toSet()
    }

    fun onlyIncludeBenefitAndResistanceEvidence(evidenceSet: Set<TreatmentEvidence>): Set<TreatmentEvidence> {
        return evidenceSet.filter { it.direction.hasBenefit || it.direction.isResistant }.toSet()
    }

    fun filterLevelDWhenAorBExists(evidenceSet: Set<TreatmentEvidence>): Set<TreatmentEvidence> {
        return groupBySourceEvent(evidenceSet).flatMap { (_, evidencesInEvent) ->
            val evidenceLevels = createPerLevelEvidenceList(evidencesInEvent)
            val hasHigherEvidence = evidenceLevels[0].isNotEmpty() || evidenceLevels[1].isNotEmpty()
            if (hasHigherEvidence) {
                evidencesInEvent.filter { it.evidenceLevel != EvidenceLevel.D }
            } else {
                evidencesInEvent
            }
        }.toSet()
    }

    fun createPerLevelEvidenceList(treatmentEvidenceList: List<TreatmentEvidence>): List<List<TreatmentEvidence>> {
        return EvidenceLevel.entries.map { level ->
            treatmentEvidenceList.filter { it.evidenceLevel == level }
        }
    }

    fun groupBySourceEvent(treatmentEvidenceSet: Set<TreatmentEvidence>) =
        treatmentEvidenceSet.groupBy { it.sourceEvent }

    fun filterPreClinicalEvidence(treatmentEvidenceSet: Set<TreatmentEvidence>): Set<TreatmentEvidence> {
        return treatmentEvidenceSet.filter { it.evidenceLevel != EvidenceLevel.D || !isPreclinical(it) }.toSet()
    }

    private fun isPreclinical(evidence: TreatmentEvidence): Boolean {
        return evidence.evidenceLevelDetails == EvidenceLevelDetails.PRECLINICAL
    }

    fun prioritizeNonCategoryEvidence(treatmentEvidenceSet: Set<TreatmentEvidence>): Set<TreatmentEvidence> {
        return groupBySourceEvent(treatmentEvidenceSet).flatMap { (_, evidencesInEvent) ->
            groupByTreatmentAndCancerType(evidencesInEvent).mapNotNull { (_, evidences) ->
                val highestCategoryEvidence = evidences.filter { it.isCategoryEvent }.minByOrNull { it.evidenceLevel }
                val highestNonCategoryEvidence = evidences.filter { !it.isCategoryEvent }.minByOrNull { it.evidenceLevel }

                highestNonCategoryEvidence ?: highestCategoryEvidence
            }
        }.toSet()
    }

    fun groupByTreatment(treatmentEvidence: List<TreatmentEvidence>) =
        treatmentEvidence.groupBy { it.treatment }

    fun groupByTreatmentAndCancerType(treatmentEvidence: List<TreatmentEvidence>) =
        treatmentEvidence.groupBy { Pair(it.treatment, it.applicableCancerType.cancerType) }

    fun generateEvidenceCellContents(evidenceList: List<TreatmentEvidence>): List<TreatmentEvidenceContent> {
        return groupByTreatment(evidenceList).map { (treatment, evidences) ->
            val cancerTypesWithYears = evidences
                .groupBy { it.applicableCancerType.cancerType }
                .map { (cancerType, evidenceGroup) ->
                    val years = evidenceGroup.map { it.date.year }.distinct().sorted()
                    "$cancerType (${years.joinToString(", ")})"
                }
                .joinToString(", ")
            val isResistant = evidences.any { it.direction.isResistant }
            TreatmentEvidenceContent(treatment, cancerTypesWithYears, isResistant)
        }
    }

    fun sortTreatmentEvidence(evidence: List<TreatmentEvidence>): Set<TreatmentEvidence> {
        return evidence
            .sortedWith(compareBy<TreatmentEvidence> { it.evidenceLevel }.thenBy { it.isCategoryEvent }.thenByDescending { it.onLabel })
            .toSet()
    }
}