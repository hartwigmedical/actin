package com.hartwig.actin.util.json

import com.google.gson.JsonObject
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class JsonDatamodelChecker(private val name: String, private val datamodel: Map<String, Boolean>) {

    private val logger: Logger = LogManager.getLogger(JsonDatamodelChecker::class.java)

    fun check(obj: JsonObject): Boolean {
        return objectHasNoUnexpectedKeys(obj) && objectContainsAllRequiredKeys(obj)
    }

    private fun objectHasNoUnexpectedKeys(obj: JsonObject): Boolean {
        val unexpectedKeys = obj.keySet().filterNot(datamodel::containsKey)
        unexpectedKeys.forEach { logger.warn("JSON object contains key '{}' which is not expected for '{}'", it, name) }
        return unexpectedKeys.isEmpty()
    }

    private fun objectContainsAllRequiredKeys(obj: JsonObject): Boolean {
        val foundKeys = obj.keySet()
        val missingKeys = datamodel.entries.filter { (key, required) -> required && !foundKeys.contains(key) }
        missingKeys.forEach { (key, _) -> logger.warn("Mandatory key '{}' missing from JSON object in '{}'", key, name) }
        return missingKeys.isEmpty()
    }
}
