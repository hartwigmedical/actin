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
import com.hartwig.actin.icd.datamodel.IcdNode
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

        nodes.forEach { if (!isValid(it)) throw IllegalArgumentException("Invalid ICD node: $it") }
        return nodes
    }

    private fun isValid(rawNode: SerializedIcdNode) = rawNode.chapterNo != "0" && rawNode.depthInKind > 0

    fun trimTitle(rawNode: SerializedIcdNode): String {
        return rawNode.title.trimStart { it == '-' }
    }

    fun resolveCode(rawNode: SerializedIcdNode): String {
        return when (rawNode.classKind) {
            ClassKind.CHAPTER -> rawNode.chapterNo
            ClassKind.BLOCK -> if (determineChapterType(rawNode) != IcdChapterType.REGULAR) rawNode.linearizationUri else rawNode.blockId!!
            ClassKind.CATEGORY -> rawNode.code!!
        }
    }

    fun determineChapterType(rawNode: SerializedIcdNode): IcdChapterType {
        return when (rawNode.chapterNo) {
            "V" -> IcdChapterType.FUNCTIONING_ASSESSMENT
            "X" -> IcdChapterType.EXTENSION_CODES
            else -> IcdChapterType.REGULAR
        }
    }

    fun resolveParentsForRegularChapter(rawNode: SerializedIcdNode): List<String> {
        val groupings = returnAllGroupings(rawNode)
        val chapterNo = listOf(rawNode.chapterNo)

        return when (rawNode.classKind) {
            ClassKind.CHAPTER -> emptyList()
            ClassKind.BLOCK -> if (rawNode.depthInKind == 1) chapterNo else chapterNo + groupings
            ClassKind.CATEGORY -> chapterNo + groupings + if (rawNode.depthInKind > 1) listOfNotNull(removeSubCode(rawNode)) else emptyList()
        }
    }

    fun returnExtensionChapterNodeWithParents(serializedNodes: List<SerializedIcdNode>): List<IcdNode> {
        return serializedNodes.fold(Pair(emptyList<IcdNode>(), emptyList<String>())) { (result, parents), node ->
            val code = resolveCode(node)
            val hyphenLevel = node.title.takeWhile { it == '-' }.length
            val updatedParents = parents.take(hyphenLevel)
            val currentParents = updatedParents + code

            val icdNode = IcdNode(
                code = code,
                parentTreeCodes = updatedParents,
                title = trimTitle(node)
            )

            Pair(result + icdNode, currentParents)
        }.first
    }

    private fun returnAllGroupings(rawNode: SerializedIcdNode): List<String> {
        with(rawNode) {
            return listOfNotNull(grouping1, grouping2, grouping3, grouping4, grouping5).filterNot { it.isBlank() }
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

enum class IcdChapterType {
    REGULAR,
    FUNCTIONING_ASSESSMENT,
    EXTENSION_CODES
}