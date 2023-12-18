package com.hartwig.actin.doid

import com.google.common.annotations.VisibleForTesting
import com.google.common.collect.Multimap
import com.google.common.collect.Sets
import com.hartwig.actin.doid.config.AdenoSquamousMapping
import com.hartwig.actin.doid.config.DoidManualConfig
import java.util.*

class DoidModel internal constructor(
    private val childToParentsMap: Multimap<String, String>, private val termPerDoidMap: Map<String, String>,
    private val doidPerLowerCaseTermMap: Map<String, String>, private val doidManualConfig: DoidManualConfig
) {
    @VisibleForTesting
    fun childToParentsMap(): Multimap<String, String> {
        return childToParentsMap
    }

    @VisibleForTesting
    fun termForDoidMap(): Map<String, String> {
        return termPerDoidMap
    }

    @VisibleForTesting
    fun doidForLowerCaseTermMap(): Map<String, String> {
        return doidPerLowerCaseTermMap
    }

    fun doidWithParents(doid: String): Set<String?> {
        val expandedDoids: MutableSet<String?> = Sets.newHashSet()
        for (expandedDoid in expandedWithAllParents(doid)) {
            expandedDoids.add(expandedDoid)
            val additionalDoid = doidManualConfig.additionalDoidsPerDoid()[expandedDoid]
            if (additionalDoid != null) {
                expandedDoids.addAll(expandedWithAllParents(additionalDoid))
            }
        }
        return expandedDoids
    }

    fun mainCancerDoids(doid: String): Set<String?> {
        val doids = doidWithParents(doid)
        val matches: MutableSet<String?> = Sets.newHashSet()
        for (mainCancerDoid in doidManualConfig.mainCancerDoids()) {
            if (doids.contains(mainCancerDoid)) {
                matches.add(mainCancerDoid)
            }
        }
        return matches
    }

    fun adenoSquamousMappingsForDoid(doidToFind: String): Set<AdenoSquamousMapping?> {
        val mappings: MutableSet<AdenoSquamousMapping?> = Sets.newHashSet()
        for (doid in doidWithParents(doidToFind)) {
            for (mapping in doidManualConfig.adenoSquamousMappings()) {
                if (mapping!!.adenoDoid() == doid || mapping.squamousDoid() == doid) {
                    mappings.add(mapping)
                }
            }
        }
        return mappings
    }

    fun resolveTermForDoid(doid: String): String? {
        return termPerDoidMap[doid]
    }

    fun resolveDoidForTerm(term: String): String? {
        return doidPerLowerCaseTermMap[term.lowercase(Locale.getDefault())]
    }

    private fun expandedWithAllParents(doid: String): Set<String?> {
        val doids: MutableSet<String?> = Sets.newHashSet(doid)
        addParents(doid, doids)
        return doids
    }

    private fun addParents(child: String, result: MutableSet<String?>) {
        if (!childToParentsMap.containsKey(child)) {
            return
        }
        for (parent in childToParentsMap[child]) {
            if (result.add(parent)) {
                addParents(parent, result)
            }
        }
    }
}
