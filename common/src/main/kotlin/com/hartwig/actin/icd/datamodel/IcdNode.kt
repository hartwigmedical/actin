package com.hartwig.actin.icd.datamodel

import com.hartwig.actin.icd.serialization.IcdChapterType
import com.hartwig.actin.icd.serialization.IcdDeserializer

class IcdNode(
    val code: String,
    val parentTreeCodes: List<String>,
    val title: String
) {

    companion object {
        fun create(rawNodes: List<SerializedIcdNode>): List<IcdNode> {
            val (extensionCodeNodes, otherNodes) = rawNodes.map { it.copy(code = IcdDeserializer.resolveCode(it)) }
                .partition { IcdDeserializer.determineChapterType(it) == IcdChapterType.EXTENSION_CODES }

            val regularNodes = otherNodes.map {
                IcdNode(
                    it.code!!,
                    IcdDeserializer.resolveParentsForRegularChapter(it),
                    IcdDeserializer.trimTitle(it)
                )
            }

            val extensionNodesWithParents = IcdDeserializer.returnExtensionChapterNodeWithParents(extensionCodeNodes)

            return regularNodes + extensionNodesWithParents
        }
    }
}
