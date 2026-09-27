package com.hartwig.actin.util.json

import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.oshai.kotlinlogging.KotlinLogging

class JsonDatamodelChecker(private val name: String, private val datamodel: Map<String, Boolean>) {

    private val logger = KotlinLogging.logger {}

    fun check(node: ObjectNode): Boolean {
        return objectHasNoUnexpectedKeys(node) && objectContainsAllRequiredKeys(node)
    }

    private fun objectHasNoUnexpectedKeys(node: ObjectNode): Boolean {
        val unexpectedKeys = node.fieldNames().asSequence().filterNot(datamodel::containsKey).toList()
        unexpectedKeys.forEach { logger.warn { "JSON object contains key '$it' which is not expected for '$name'" } }
        return unexpectedKeys.isEmpty()
    }

    private fun objectContainsAllRequiredKeys(node: ObjectNode): Boolean {
        val foundKeys = node.fieldNames().asSequence().toSet()
        val missingKeys = datamodel.entries.filter { (key, required) -> required && !foundKeys.contains(key) }
        missingKeys.forEach { (key, _) -> logger.warn { "Mandatory key '$key' missing from JSON object in '$name'" } }
        return missingKeys.isEmpty()
    }
}
