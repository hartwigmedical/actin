package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.IcdNode

data class IcdModel(
    val codeToNodeMap: Map<String, IcdNode>,
    val titleToCodeMap: Map<String, String>
) {

    fun isValidIcdTitle(icdTitle: String): Boolean {
        return titleToCodeMap.containsKey(icdTitle)
    }

    fun resolveCodeForTitle(icdTitle: String): String? {
        return titleToCodeMap[icdTitle]
    }

    fun returnCodeWithParents(code: String): List<String> = resolveParentCodes(code) + code
    private fun resolveParentCodes(code: String): List<String> = codeToNode(code)?.parentTreeCodes ?: emptyList()
    private fun codeToNode(code: String): IcdNode? = codeToNodeMap[code]

    companion object {
        fun create(nodes: List<IcdNode>): IcdModel {
            return IcdModel(createCodeToNodeMap(nodes), createTitleToCodeMap(nodes))
        }

        private fun createCodeToNodeMap(icdNodes: List<IcdNode>): Map<String, IcdNode> = icdNodes.associateBy { it.code }
        private fun createTitleToCodeMap(icdNodes: List<IcdNode>): Map<String, String> = icdNodes.associate { it.title to it.code }
    }
}