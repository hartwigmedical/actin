package com.hartwig.actin.algo.matchcomparison

import com.hartwig.actin.algo.evaluation.util.Format
import org.apache.logging.log4j.LogManager

object DifferenceExtractionUtil {

    private val LOGGER = LogManager.getLogger(DifferenceExtractionUtil::class.java)

    fun <T> extractDifferences(old: T, new: T, properties: Map<String, (T) -> Any>): List<String> {
        return properties.mapNotNull { (description, property) ->
            val oldValue = property(old)
            val newValue = property(new)
            if (oldValue != newValue) "> ${old!!::class.java.simpleName} difference in $description: $oldValue != $newValue" else null
        }
    }

    fun <T> mapKeyDifferences(old: Map<T, Any>, new: Map<T, Any>, description: String, keyToString: (T) -> String): List<String> {
        val added = new.keys - old.keys
        val removed = old.keys - new.keys
        val differences = listOf("added" to added, "removed" to removed)
            .filter { (_, differences) -> differences.isNotEmpty() }
            .map { (modification, differences) ->
                "${differences.size} $description were $modification: ${Format.concatWithCommaAndAnd(differences.map(keyToString))}"
            }
        differences.forEach(LOGGER::debug)
        return differences
    }
}