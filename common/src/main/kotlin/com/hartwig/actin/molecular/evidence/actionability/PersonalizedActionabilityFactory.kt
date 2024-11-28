package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.doid.DoidModel
import com.hartwig.serve.datamodel.common.Indication
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.trial.ActionableTrial

internal class PersonalizedActionabilityFactory internal constructor(
    private val doidModel: DoidModel,
    private val applicableDoids: Set<String>
) {

    fun create(matches: ActionableEvents): ActionabilityMatch {
        val expandedTumorDoids = expandDoids(doidModel, applicableDoids)
        val (onLabelEvents, offLabelEvents) = partitionEvidences(matches.evidences, expandedTumorDoids)
        val (onLabelEventsTrials, offLabelEventsTrials) = partitionTrials(matches.trials, expandedTumorDoids)
        return ActionabilityMatch(
            ActionableEvents(onLabelEvents, onLabelEventsTrials),
            ActionableEvents(offLabelEvents, offLabelEventsTrials)
        )
    }

    private fun partitionEvidences(
        evidences: List<EfficacyEvidence>,
        expandedTumorDoids: Set<String>
    ): Pair<List<EfficacyEvidence>, List<EfficacyEvidence>> {
        return evidences.partition { isOnLabel(it.indication(), expandedTumorDoids) }
    }

    private fun partitionTrials(
        trials: List<ActionableTrial>,
        expandedTumorDoids: Set<String>
    ): Pair<List<ActionableTrial>, List<ActionableTrial>> {
        return trials.partition { isOnLabel(it.indications().iterator().next(), expandedTumorDoids) }
    }

    private fun isOnLabel(indication: Indication, expandedTumorDoids: Set<String>): Boolean {
        return expandedTumorDoids.contains(indication.applicableType().doid()) &&
                indication.excludedSubTypes().none { expandedTumorDoids.contains(it.doid()) }
    }

    companion object {
        fun create(doidModel: DoidModel, tumorDoids: Set<String>): PersonalizedActionabilityFactory {
            return PersonalizedActionabilityFactory(doidModel, expandDoids(doidModel, tumorDoids))
        }

        private fun expandDoids(doidModel: DoidModel, doids: Set<String>): Set<String> {
            return doids.flatMap { doidModel.doidWithParents(it) }.toSet()
        }
    }
}
