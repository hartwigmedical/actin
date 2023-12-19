package com.hartwig.actin.doid

import com.hartwig.actin.doid.config.AdenoSquamousMapping
import com.hartwig.actin.doid.config.DoidManualConfig

data class DoidModel(
    val childToParentsMap: Map<String, List<String>>,
    val termForDoidMap: Map<String, String>,
    val doidForLowerCaseTermMap: Map<String, String>,
    private val doidManualConfig: DoidManualConfig
) {

    fun doidWithParents(doid: String): Set<String> {
        val expandedDoids = expandedWithAllParents(doid)
        val additionalDoids = expandedDoids.mapNotNull { doidManualConfig.additionalDoidsPerDoid[it] }
            .flatMap(::expandedWithAllParents)
            .toSet()
        return expandedDoids + additionalDoids
    }

    fun mainCancerDoids(doid: String): Set<String> {
        return doidWithParents(doid).intersect(doidManualConfig.mainCancerDoids)
    }

    fun adenoSquamousMappingsForDoid(doidToFind: String): Set<AdenoSquamousMapping> {
        val expandedDoids = doidWithParents(doidToFind)
        return doidManualConfig.adenoSquamousMappings.filter { it.adenoDoid in expandedDoids || it.squamousDoid in expandedDoids }.toSet()
    }

    fun resolveTermForDoid(doid: String): String? {
        return termForDoidMap[doid]
    }

    fun resolveDoidForTerm(term: String): String? {
        return doidForLowerCaseTermMap[term.lowercase()]
    }

    private fun expandedWithAllParents(doid: String): Set<String> {
        return expandedDoidSet(setOf(doid), emptySet())
    }

    private tailrec fun expandedDoidSet(doidsToExpand: Set<String>, expandedDoids: Set<String>): Set<String> {
        if (doidsToExpand.isEmpty()) {
            return expandedDoids
        }
        val nextDoid = doidsToExpand.first()
        val newDoids = if (nextDoid in expandedDoids) emptySet() else {
            childToParentsMap[nextDoid] ?: emptySet()
        }
        return expandedDoidSet(doidsToExpand + newDoids - nextDoid, expandedDoids + nextDoid)
    }
}
