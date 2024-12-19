package com.hartwig.actin.icd.serialization

import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.icd.datamodel.IcdNode
import com.hartwig.actin.icd.datamodel.SerializedIcdNode

object IcdDeserializer {

    fun deserialize(rawNodes: List<SerializedIcdNode>): List<IcdNode> {
        val (extensionCodeNodes, otherNodes) = rawNodes.map { it.copy(code = resolveCode(it)) }
            .partition { determineChapterType(it) == IcdChapterType.EXTENSION_CODES }
        val regularNodes = otherNodes.map { IcdNode(it.code!!, resolveParentsForRegularChapter(it), trimTitle(it)) }
        val extensionNodesWithParents = returnExtensionChapterNodeWithParents(extensionCodeNodes)

        return regularNodes + extensionNodesWithParents
    }

    private fun resolveParentsForRegularChapter(rawNode: SerializedIcdNode): List<String> {
        val groupings = returnAllGroupings(rawNode)
        val chapterNo = listOf(rawNode.chapterNo)

        return when (rawNode.classKind) {
            ClassKind.CHAPTER -> emptyList()
            ClassKind.BLOCK -> if (rawNode.depthInKind == 1) chapterNo else chapterNo + groupings
            ClassKind.CATEGORY -> chapterNo + groupings + if (rawNode.depthInKind > 1) listOfNotNull(removeSubCode(rawNode)) else emptyList()
        }
    }

    private fun returnExtensionChapterNodeWithParents(serializedNodes: List<SerializedIcdNode>): List<IcdNode> {
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

    private fun trimTitle(rawNode: SerializedIcdNode): String {
        return rawNode.title.trimStart { it == '-' || it.isWhitespace() }
    }

    private fun resolveCode(rawNode: SerializedIcdNode): String {
        return when (rawNode.classKind) {
            ClassKind.CHAPTER -> rawNode.chapterNo
            ClassKind.BLOCK -> if (determineChapterType(rawNode) != IcdChapterType.REGULAR) rawNode.linearizationUri else rawNode.blockId!!
            ClassKind.CATEGORY -> rawNode.code!!
        }
    }

    private fun determineChapterType(rawNode: SerializedIcdNode): IcdChapterType {
        return when (rawNode.chapterNo) {
            "V" -> IcdChapterType.FUNCTIONING_ASSESSMENT
            "X" -> IcdChapterType.EXTENSION_CODES
            else -> IcdChapterType.REGULAR
        }
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

private enum class IcdChapterType {
    REGULAR,
    FUNCTIONING_ASSESSMENT,
    EXTENSION_CODES
}