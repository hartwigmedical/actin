package com.hartwig.actin.doid

import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.doid.datamodel.DoidEntry
import org.apache.logging.log4j.LogManager

object DoidModelFactory {

    private val LOGGER = LogManager.getLogger(DoidModelFactory::class.java)

    fun createFromDoidEntry(doidEntry: DoidEntry): DoidModel {
        val doidManualConfig = DoidManualConfig.create()
        val parentChildRelationships = doidEntry.edges.asSequence()
            .filter { it.predicate == "is_a" }
            .map { edge ->
                val child = edge.subjectDoid
                val parent = edge.objectDoid
                child to parent
            }
            .distinct()
            .filterNot(doidManualConfig.childToParentRelationshipsToExclude::contains)

        val childToParentsMap = parentChildRelationships
            .groupBy(Pair<String, String>::first, Pair<String, String>::second)

        val parentToChildrenMap = parentChildRelationships
            .groupBy(Pair<String, String>::second, Pair<String, String>::first)

        LOGGER.debug("Loaded {} parent-child relationships", childToParentsMap.size)

        // Assume both doid and term are unique.
        val termPerDoidMap = doidEntry.nodes.filter { it.term != null }.associate { it.doid to it.term!! }

        termPerDoidMap.values.groupBy { it.lowercase() }.filter { it.value.size > 1 }.forEach { (term, _) ->
            LOGGER.warn("DOID term (in lower-case) is not unique: '{}'", term)
        }
        val doidPerLowerCaseTermMap = termPerDoidMap.entries.associate { (doid, term) -> term.lowercase() to doid }

        return DoidModel(childToParentsMap, parentToChildrenMap, termPerDoidMap, doidPerLowerCaseTermMap, doidManualConfig)
    }
}
