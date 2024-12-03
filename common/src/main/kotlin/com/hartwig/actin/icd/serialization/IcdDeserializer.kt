package com.hartwig.actin.icd.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.module.SimpleModule
import com.hartwig.actin.icd.datamodel.ClassKind
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvParser
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.hartwig.actin.icd.datamodel.SerializedIcdNode
import java.io.File

object IcdDeserializer {

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
            .filterNot { it.chapterNo.any { char -> char.isLetter() } }

        nodes.forEach { if (!isValid(it)) throw IllegalArgumentException("Invalid ICD node: $it") }
        return nodes
    }

    fun resolveCode(rawNode: SerializedIcdNode): String {
        return when (rawNode.classKind) {
            ClassKind.CHAPTER -> rawNode.chapterNo
            ClassKind.BLOCK -> rawNode.blockId!!
            ClassKind.CATEGORY -> rawNode.code!!
        }
    }

    fun resolveParentCode(rawNode: SerializedIcdNode): String? {
        return when (rawNode.classKind) {
            ClassKind.CHAPTER -> null
            ClassKind.BLOCK -> if (rawNode.depthInKind == 1) rawNode.chapterNo else returnHighestGrouping(rawNode)
            ClassKind.CATEGORY -> if (rawNode.depthInKind == 1) returnHighestGrouping(rawNode)!! else removeSubCode(rawNode)
        }
    }

    fun trimTitle(rawNode: SerializedIcdNode): String {
        return rawNode.title.trimStart { it == '-' }
    }

    private fun isValid(rawNode: SerializedIcdNode) = rawNode.chapterNo != "0" && rawNode.depthInKind > 0

    private fun returnHighestGrouping(rawNode: SerializedIcdNode): String? {
        with(rawNode) {
            return sequenceOf(grouping5, grouping4, grouping3, grouping2, grouping1).firstOrNull { !it.isNullOrBlank() }
        }
    }

    private fun removeSubCode(rawNode: SerializedIcdNode): String? {
        val subtractionLength = when (rawNode.depthInKind) {
            1 -> 0
            2 -> 2
            else -> 1
        }
        return rawNode.code?.substring(0, rawNode.code.length - subtractionLength)
    }
}

class ClassKindDeserializer : JsonDeserializer<ClassKind>() {
    override fun deserialize(parser: JsonParser, context: DeserializationContext): ClassKind {
        val node = parser.readValueAsTree<JsonNode>()
        return ClassKind.valueOf(node.asText().uppercase())
    }
}