package com.hartwig.actin.doid

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Maps
import com.google.common.collect.Multimap
import com.hartwig.actin.doid.config.DoidManualConfigFactory
import com.hartwig.actin.doid.datamodel.DoidEntry
import org.apache.logging.log4j.LogManager
import java.util.*

object DoidModelFactory {
    private val LOGGER = LogManager.getLogger(DoidModelFactory::class.java)

    @JvmStatic
    fun createFromDoidEntry(doidEntry: DoidEntry): DoidModel {
        val childToParentsMap: Multimap<String, String> = ArrayListMultimap.create()
        for (edge in doidEntry.edges()) {
            if (edge!!.predicate() == "is_a") {
                val child = edge.subjectDoid()
                val parent = edge.objectDoid()
                if (childToParentsMap.containsKey(child)) {
                    val parents = childToParentsMap[child]
                    if (!parents.contains(parent)) {
                        parents.add(parent)
                    }
                } else {
                    childToParentsMap.put(child, parent)
                }
            }
        }

        // Assume both doid and term are unique.
        val termPerDoidMap: MutableMap<String, String> = Maps.newHashMap()
        val doidPerLowerCaseTermMap: MutableMap<String, String> = Maps.newHashMap()
        for (node in doidEntry.nodes()) {
            val term = node!!.term()
            if (term != null) {
                termPerDoidMap[node.doid()] = term
                val lowerCaseTerm = term.lowercase(Locale.getDefault())
                if (doidPerLowerCaseTermMap.containsKey(lowerCaseTerm)) {
                    LOGGER.warn("DOID term (in lower-case) is not unique: '{}'", term)
                } else {
                    doidPerLowerCaseTermMap[lowerCaseTerm] = node.doid()
                }
            }
        }
        return DoidModel(childToParentsMap, termPerDoidMap, doidPerLowerCaseTermMap, DoidManualConfigFactory.create())
    }
}
