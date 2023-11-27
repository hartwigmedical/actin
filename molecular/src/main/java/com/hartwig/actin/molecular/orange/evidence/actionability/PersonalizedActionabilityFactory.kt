package com.hartwig.actin.molecular.orange.evidence.actionability

import com.hartwig.actin.doid.DoidModel
import com.hartwig.serve.datamodel.ActionableEvent

internal class PersonalizedActionabilityFactory internal constructor(
    private val doidModel: DoidModel,
    private val applicableDoids: Set<String>
) {

    fun create(matches: List<ActionableEvent>): ActionabilityMatch {
        val expandedTumorDoids = expandDoids(doidModel, applicableDoids)
        val (onLabelEvents, offLabelEvents) = matches.partition { isOnLabel(it, expandedTumorDoids) }
        return ActionabilityMatch(onLabelEvents, offLabelEvents)
    }

    private fun isOnLabel(event: ActionableEvent, expandedTumorDoids: Set<String>): Boolean {
        if (!expandedTumorDoids.contains(event.applicableCancerType().doid())) {
            return false
        }
        for (blacklist in event.blacklistCancerTypes()) {
            if (expandedTumorDoids.contains(blacklist.doid())) {
                return false
            }
        }
        return true
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
