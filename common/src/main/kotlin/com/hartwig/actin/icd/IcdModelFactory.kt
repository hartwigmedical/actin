package com.hartwig.actin.icd

import com.google.common.annotations.VisibleForTesting
import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.icd.datamodel.IcdNode

object IcdModelFactory {

    fun create(icdNodes: List<IcdNode>): IcdModel {
        return IcdModel(
            createChildToParentMap(icdNodes),
            createCodeToNodeMap(icdNodes),
            createTitleToCodeMap(icdNodes)
        )
    }

    @VisibleForTesting
    fun solveParentForChild(node: IcdNode, codeToNodeMap: CodeToNodeMap): IcdNode? {
        return when (node.classKind) {
            ClassKind.CHAPTER -> null

            ClassKind.BLOCK -> {
                if (node.depthInKind == 1) {
                    codeToNodeMap[node.chapterNo]
                } else {
                    returnHighestGroupingEntry(node)?.let { codeToNodeMap[it] }
                }
            }

            ClassKind.CATEGORY -> if (node.depthInKind == 1) {
                returnHighestGroupingEntry(node)?.let { codeToNodeMap[it] }
            } else removeSubCode(node).let { codeToNodeMap[it] }
        }
    }

    fun createCodeToNodeMap(icdNodes: List<IcdNode>): CodeToNodeMap = icdNodes.associateBy { it.code }

    private fun createChildToParentMap(icdNodes: List<IcdNode>): Map<IcdNode, IcdNode?> {
        val codeToNodeMap = createCodeToNodeMap(icdNodes)
        return icdNodes.associateWith { solveParentForChild(it, codeToNodeMap) }
    }

    private fun returnHighestGroupingEntry(node: IcdNode): String? {
        with(node) {
            return sequenceOf(grouping5, grouping4, grouping3, grouping2, grouping1).firstOrNull { !it.isNullOrBlank() }
        }
    }

    private fun removeSubCode(node: IcdNode): String {
        val subtractionLength = when (node.depthInKind) {
            1 -> 0
            2 -> 2
            else -> 1
        }
        return node.code.substring(0, node.code.length - subtractionLength)
    }

    private fun createTitleToCodeMap(icdNodes: List<IcdNode>): Map<String, String> {
        return icdNodes.associate { it.title to it.code }
    }
}

private typealias CodeToNodeMap = Map<String, IcdNode>