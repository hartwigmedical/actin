package com.hartwig.actin.molecular.orange.evidence.actionability

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Sets
import com.hartwig.actin.doid.DoidModel
import com.hartwig.serve.datamodel.ActionableEvent

internal class PersonalizedActionabilityFactory @VisibleForTesting constructor(private val doidModel: DoidModel, private val applicableDoids: MutableSet<String?>) {
    fun create(matches: MutableList<ActionableEvent?>): ActionabilityMatch {
        val expandedTumorDoids = expandDoids(doidModel, applicableDoids)
        val builder = ImmutableActionabilityMatch.builder()
        for (match in matches) {
            if (isOnLabel(match, expandedTumorDoids)) {
                builder.addOnLabelEvents(match)
            } else {
                builder.addOffLabelEvents(match)
            }
        }
        return builder.build()
    }

    private fun isOnLabel(event: ActionableEvent, expandedTumorDoids: MutableSet<String?>): Boolean {
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
        fun create(doidModel: DoidModel, tumorDoids: MutableSet<String?>?): PersonalizedActionabilityFactory {
            return PersonalizedActionabilityFactory(doidModel, expandDoids(doidModel, tumorDoids))
        }

        private fun expandDoids(doidModel: DoidModel, doids: MutableSet<String?>?): MutableSet<String?> {
            if (doids == null) {
                return Sets.newHashSet()
            }
            val expanded: MutableSet<String?>? = Sets.newHashSet()
            for (doid in doids) {
                expanded.addAll(doidModel.doidWithParents(doid))
            }
            return expanded
        }
    }
}
