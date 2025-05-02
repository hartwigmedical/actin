package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.evidence.CancerTypeMatchApplicability
import com.hartwig.actin.doid.DoidModel
import com.hartwig.serve.datamodel.common.Indication

const val ADVANCED_SOLID_TUMOR_DOID = "162"

class CancerTypeApplicabilityResolver(private val expandedTumorDoids: Set<String>) {

    fun resolve(indication: Indication): CancerTypeMatchApplicability {
        return when {
            isOnLabel(indication) -> CancerTypeMatchApplicability.SPECIFIC_TYPE
            matchDoid(indication, setOf(ADVANCED_SOLID_TUMOR_DOID)) -> CancerTypeMatchApplicability.ALL_TYPES
            !isOnLabel(indication) -> CancerTypeMatchApplicability.OTHER_TYPE
            else -> throw IllegalStateException("Cannot determine evidence cancer type match applicability, this should not be possible and indicates a bug in EvidenceCancerTypeResolver")
        }
    }

    private fun isOnLabel(indication: Indication): Boolean {
        return matchDoid(indication, expandedTumorDoids)
    }

    private fun matchDoid(indication: Indication, doidsToMatch: Set<String>): Boolean {
        return doidsToMatch.contains(indication.applicableType().doid()) &&
                indication.excludedSubTypes().none { doidsToMatch.contains(it.doid()) }
    }

    companion object {
        fun create(doidModel: DoidModel, tumorDoids: Set<String>): CancerTypeApplicabilityResolver {
            return CancerTypeApplicabilityResolver(expandDoids(doidModel, tumorDoids))
        }

        private fun expandDoids(doidModel: DoidModel, doids: Set<String>): Set<String> {
            return doids.flatMap { doidModel.doidWithParents(it) }.toSet()
        }
    }
}