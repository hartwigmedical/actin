package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.DoidModel
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.trial.ActionableTrial

internal class PersonalizedActionabilityFactory internal constructor(
    private val expandedTumorDoids: Set<String>
) {

    fun create(matches: ActionableEvents): ActionabilityMatch {
        val (onLabelEvidences, offLabelEvidences) = partitionEvidences(matches.evidences)
        val (onLabelTrials, offLabelTrials) = partitionTrials(matches.trials)

        return ActionabilityMatch(
            onLabelEvidence = onLabelEvidences,
            offLabelEvidence = offLabelEvidences,
            onLabelTrials = onLabelTrials,
            offLabelTrials = offLabelTrials
        )
    }

    private fun partitionEvidences(evidence: List<EfficacyEvidence>): Pair<List<EfficacyEvidence>, List<EfficacyEvidence>> {
        return evidence.partition { isOnLabel(it.indication()) }
    }

    private fun partitionTrials(trials: List<ActionableTrial>): Pair<List<ActionableTrial>, List<ActionableTrial>> {
        // TODO (KD): Deal with N indications
        return trials.partition { isOnLabel(it.indications().iterator().next()) }
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
