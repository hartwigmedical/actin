package com.hartwig.actin.util.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import java.time.LocalDate

object Json {

    fun optionalObject(node: ObjectNode, field: String): ObjectNode? {
        return if (node.has(field)) nullableObject(node, field) else null
    }

    fun nullableObject(node: ObjectNode, field: String): ObjectNode? {
        return if (!isNull(node, field)) objectNode(node, field) else null
    }

    fun objectNode(node: ObjectNode, field: String): ObjectNode {
        return node.get(field) as ObjectNode
    }

    fun optionalArray(node: ObjectNode, field: String): ArrayNode? {
        return if (node.has(field)) nullableArray(node, field) else null
    }

    fun nullableArray(node: ObjectNode, field: String): ArrayNode? {
        return if (!isNull(node, field)) array(node, field) else null
    }

    fun array(node: ObjectNode, field: String): ArrayNode {
        return node.get(field) as ArrayNode
    }

    fun optionalStringList(node: ObjectNode, field: String): List<String>? {
        return if (node.has(field)) nullableStringList(node, field) else null
    }

    fun nullableStringList(node: ObjectNode, field: String): List<String>? {
        return if (!isNull(node, field)) stringList(node, field) else null
    }

    fun stringList(node: ObjectNode, field: String): List<String> {
        val element = node.get(field)
        return if (element.isValueNode) {
            listOf(string(node, field))
        } else {
            require(element.isArray) { "Expected array or primitive for field '$field' but got $element" }
            element.map(JsonNode::asText)
        }
    }

    fun optionalString(node: ObjectNode, field: String): String? {
        return if (node.has(field)) nullableString(node, field) else null
    }

    fun nullableString(node: ObjectNode, field: String): String? {
        return if (!isNull(node, field)) string(node, field) else null
    }

    fun string(node: ObjectNode, field: String): String {
        return node.get(field).asText()
    }

    fun nullableInteger(node: ObjectNode, field: String): Int? {
        return if (!isNull(node, field)) integer(node, field) else null
    }

    fun integer(node: ObjectNode, field: String): Int {
        return node.get(field).asInt()
    }

    fun optionalDouble(node: ObjectNode, field: String): Double? {
        return if (node.has(field)) nullableDouble(node, field) else null
    }

    fun nullableDouble(node: ObjectNode, field: String): Double? {
        return if (!isNull(node, field)) double(node, field) else null
    }

    fun double(node: ObjectNode, field: String): Double {
        return node.get(field).asDouble()
    }

    fun optionalBool(node: ObjectNode, field: String): Boolean? {
        return if (node.has(field)) nullableBool(node, field) else null
    }

    fun nullableBool(node: ObjectNode, field: String): Boolean? {
        return if (!isNull(node, field)) bool(node, field) else null
    }

    fun bool(node: ObjectNode, field: String): Boolean {
        return node.get(field).asBoolean()
    }

    fun nullableDate(node: ObjectNode, field: String): LocalDate? {
        return if (!isNull(node, field)) date(node, field) else null
    }

    fun date(node: ObjectNode, field: String): LocalDate {
        val dateNode = node.get(field)
        return when {
            dateNode.isTextual -> LocalDate.parse(dateNode.asText())
            dateNode.isObject -> LocalDate.of(
                integer(dateNode as ObjectNode, "year"), integer(dateNode, "month"), integer(dateNode, "day")
            )

            else -> throw IllegalArgumentException("Expected ISO date string or {year, month, day} object for field '$field' but got $dateNode")
        }
    }

    private fun isNull(node: ObjectNode, field: String): Boolean {
        return node.get(field).isNull
    }
}
