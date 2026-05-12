package com.hartwig.actin.util.json

import com.google.gson.JsonObject
import io.github.oshai.kotlinlogging.KotlinLogging

class JsonDatamodelChecker(private val name: String, private val datamodel: Map<String, Boolean>) {

    private val logger = KotlinLogging.logger {}

    fun check(obj: JsonObject): Boolean {
        return objectHasNoUnexpectedKeys(obj) && objectContainsAllRequiredKeys(obj)
    }

    private fun objectHasNoUnexpectedKeys(obj: JsonObject): Boolean {
        val unexpectedKeys = obj.keySet().filterNot(datamodel::containsKey)
        unexpectedKeys.forEach { logger.warn { "JSON object contains key '$it' which is not expected for '$name'" } }
        return unexpectedKeys.isEmpty()
    }

    private fun objectContainsAllRequiredKeys(obj: JsonObject): Boolean {
        val foundKeys = obj.keySet()
        val missingKeys = datamodel.entries.filter { (key, required) -> required && !foundKeys.contains(key) }
        missingKeys.forEach { (key, _) -> logger.warn { "Mandatory key '$key' missing from JSON object in '$name'" } }
        return missingKeys.isEmpty()
    }
}
