package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.doid.DoidModel
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.trial.ActionableTrial

const val ADVANCED_SOLID_TUMOR_DOID = "162"

class PersonalizedActionabilityFactory(private val expandedTumorDoids: Set<String>) {

    fun create(match: ActionabilityMatch): ClinicalEvidence {

        return ClinicalEvidenceFactory.create(
            tumorSpecificEvidence = match.evidenceMatches.filter { isOnLabel(it.indication()) },
            tumorAgnosticEvidence = match.evidenceMatches.filter { matchDoid(it.indication(), setOf(ADVANCED_SOLID_TUMOR_DOID)) },
            offTumorEvidences = match.evidenceMatches.filter { !isOnLabel(it.indication()) },
            matchingCriteriaAndIndicationsPerEligibleTrial = determineOnLabelTrials(match.matchingCriteriaPerTrialMatch)
        )
    }

    private fun determineOnLabelTrials(matchingCriteriaPerTrialMatch: Map<ActionableTrial, Set<MolecularCriterium>>):
            Map<ActionableTrial, Pair<Set<MolecularCriterium>, Set<Indication>>> {
        return matchingCriteriaPerTrialMatch.mapValues { (trial, criteria) ->
            criteria to trial.indications().filter(::isOnLabel).toSet()
        }
            .filter { (_, criteriaAndIndications) -> criteriaAndIndications.second.isNotEmpty() }
    }

    private fun isOnLabel(indication: Indication): Boolean {
        return matchDoid(indication, expandedTumorDoids)
    }

    private fun matchDoid(indication: Indication, doidsToMatch: Set<String>): Boolean {
        return doidsToMatch.contains(indication.applicableType().doid()) &&
                indication.excludedSubTypes().none { doidsToMatch.contains(it.doid()) }
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
