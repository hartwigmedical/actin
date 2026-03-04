package com.hartwig.actin.doid

import com.hartwig.actin.doid.config.AdenoSquamousMapping
import com.hartwig.actin.doid.config.DoidManualConfig

data class DoidModel(
    val childToParentsMap: Map<String, List<String>>,
    val parentToChildrenMap: Map<String, List<String>>,
    val termForDoidMap: Map<String, String>,
    val doidForLowerCaseTermMap: Map<String, String>,
    private val doidManualConfig: DoidManualConfig
) {

    fun doidWithParents(doid: String): Set<String> {
        return expandedParentsDoidSet(setOf(doid), emptySet(), emptySet(), childToParentsMap)
    }

    fun doidWithChildren(doid: String, exclude: Set<String> = emptySet()): Set<String> {
        return expandedParentsDoidSet(setOf(doid), emptySet(), exclude, parentToChildrenMap)
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

    private tailrec fun expandedParentsDoidSet(doidsToExpand: Set<String>, expandedDoids: Set<String>, excludedDoids: Set<String>, mapping: Map<String, List<String>>): Set<String> {
        val remainingDoids = doidsToExpand - excludedDoids
        if (remainingDoids.isEmpty()) {
            return expandedDoids
        }
        val nextDoid = remainingDoids.first()
        val newDoids = if (nextDoid in expandedDoids) emptySet() else {
            (mapping[nextDoid] ?: emptyList()) + listOfNotNull(doidManualConfig.additionalDoidsPerDoid[nextDoid])
        }
        return expandedParentsDoidSet(doidsToExpand + newDoids - nextDoid, expandedDoids + nextDoid, excludedDoids, mapping)
    }
}
