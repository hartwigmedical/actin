package com.hartwig.actin.doid

import com.hartwig.actin.doid.config.DoidManualConfigFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import org.apache.logging.log4j.LogManager

object DoidModelFactory {
    private val LOGGER = LogManager.getLogger(DoidModelFactory::class.java)

    fun createFromDoidEntry(doidEntry: DoidEntry): DoidModel {
        val childToParentsMap = doidEntry.edges.filter { it.predicate == "is_a" }
            .map { edge ->
                val child = edge.subjectDoid
                val parent = edge.objectDoid
                child to parent
            }
            .distinct()
            .groupBy(Pair<String, String>::first, Pair<String, String>::second)

        // Assume both doid and term are unique.
        val (termPerDoidMap, doidPerLowerCaseTermMap) = doidEntry.nodes.filter { it.term != null }
            .fold(Pair(emptyMap<String, String>(), emptyMap<String, String>())) { (doidsToTerms, termsToDoids), node ->
                val lowerCaseTerm = node.term!!.lowercase()
                if (termsToDoids.containsKey(lowerCaseTerm)) {
                    LOGGER.warn("DOID term (in lower-case) is not unique: '{}'", node.term)
                }
                Pair(doidsToTerms + mapOf(node.doid to node.term), termsToDoids + mapOf(lowerCaseTerm to node.doid))
            }

        return DoidModel(childToParentsMap, termPerDoidMap, doidPerLowerCaseTermMap, DoidManualConfigFactory.create())
    }
}
