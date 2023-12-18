package com.hartwig.actin.util.json

import com.google.gson.JsonObject
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class JsonDatamodelChecker(private val name: String, private val datamodel: Map<String, Boolean>) {
    fun check(`object`: JsonObject): Boolean {
        var correct: Boolean = true
        val keys: Set<String> = `object`.keySet()
        for (key: String in keys) {
            if (!datamodel.containsKey(key)) {
                LOGGER.warn("JSON object contains key '{}' which is not expected for '{}'", key, name)
                correct = false
            }
        }
        for (datamodelEntry: Map.Entry<String, Boolean> in datamodel.entries) {
            val isMandatory: Boolean = datamodelEntry.value
            if (isMandatory && !keys.contains(datamodelEntry.key)) {
                LOGGER.warn("Mandatory key '{}' missing from JSON object in '{}'", datamodelEntry.key, name)
                correct = false
            }
        }
        return correct
    }

    companion object {
        private val LOGGER: Logger = LogManager.getLogger(JsonDatamodelChecker::class.java)
    }
}
