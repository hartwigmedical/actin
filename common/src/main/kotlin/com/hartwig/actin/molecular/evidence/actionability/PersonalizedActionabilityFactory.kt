package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.doid.DoidModel
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial

class PersonalizedActionabilityFactory(private val expandedTumorDoids: Set<String>) {

    fun create(match: ActionabilityMatch): ClinicalEvidence {
        val (onLabelEvidences, offLabelEvidences) = partitionEvidences(match.evidenceMatches)

        return ClinicalEvidenceFactory.create(
            onLabelEvidences = onLabelEvidences,
            offLabelEvidences = offLabelEvidences,
            matchingCriteriaAndIndicationsPerEligibleTrial = determineOnLabelTrials(match.matchingCriteriaPerTrialMatch)
        )
    }

    private fun partitionEvidences(evidence: List<EfficacyEvidence>): Pair<List<EfficacyEvidence>, List<EfficacyEvidence>> {
        return evidence.partition { isOnLabel(it.indication()) }
    }

    private fun determineOnLabelTrials(matchingCriteriaPerTrialMatch: Map<ActionableTrial, Set<MolecularCriterium>>):
            Map<ActionableTrial, Pair<Set<MolecularCriterium>, Set<Indication>>> {
        return matchingCriteriaPerTrialMatch.mapValues { (trial, criteria) ->
            criteria to trial.indications().filter(::isOnLabel).toSet()
        }
            .filter { (_, criteriaAndIndications) -> criteriaAndIndications.second.isNotEmpty() }
    }

    private fun isOnLabel(indication: Indication): Boolean {
        return expandedTumorDoids.contains(indication.applicableType().doid()) &&
                indication.excludedSubTypes().none { expandedTumorDoids.contains(it.doid()) }
    }

    companion object {
        fun create(doidModel: DoidModel, tumorDoids: Set<String>): PersonalizedActionabilityFactory {
            return PersonalizedActionabilityFactory(expandDoids(doidModel, tumorDoids))
        }

        private fun expandDoids(doidModel: DoidModel, doids: Set<String>): Set<String> {
            return doids.flatMap { doidModel.doidWithParents(it) }.toSet()
        }
    }
}
