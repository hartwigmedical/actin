package com.hartwig.actin.icd.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.icd.datamodel.SerializedIcdNode
import java.io.File

object CsvReader {

    fun readFromFile(tsvPath: String): List<SerializedIcdNode> {
        val reader = CsvMapper().apply {
            enable(CsvParser.Feature.FAIL_ON_MISSING_HEADER_COLUMNS)
            enable(CsvParser.Feature.EMPTY_STRING_AS_NULL)
            enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING)
            registerModule(
                SimpleModule().apply {
                    addDeserializer(ClassKind::class.java, ClassKindDeserializer())
                }
            )
        }.readerFor(SerializedIcdNode::class.java).with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))

        val file = File(tsvPath)
        val nodes = reader.readValues<SerializedIcdNode>(file).readAll().toList()

        nodes.filterNot(::isValid).takeIf { it.isNotEmpty() }?.let { invalidNodes ->
            throw IllegalArgumentException("Invalid ICD node(s): ${invalidNodes.joinToString(", ")}")
        }
        return nodes
    }

    private fun isValid(rawNode: SerializedIcdNode) = rawNode.chapterNo != "0" && rawNode.depthInKind > 0
}

private class ClassKindDeserializer : JsonDeserializer<ClassKind>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): ClassKind {
        val node = parser.readValueAsTree<JsonNode>()
        return ClassKind.valueOf(node.asText().uppercase())
    }
}