package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.IcdNode

data class IcdModel(
    val codeToNodeMap: Map<String, IcdNode>,
    val titleToCodeMap: Map<String, String>
) {

    fun isValidIcdTitle(icdTitle: String): Boolean {
        return if (hasExtension(icdTitle)) {
            icdTitle.split('&').all { titleToCodeMap.containsKey(it) }
        } else {
            titleToCodeMap.containsKey(icdTitle)
        }
    }

    fun resolveCodeForTitle(icdTitle: String): String? {
        return titleToCodeMap[icdTitle]
    }

    fun resolveCodesForTitle(icdTitle: String): IcdCodes? {
        val split = icdTitle.split('&')
        val mainCode = titleToCodeMap[split[0]] ?: return null
        val extensionCode = split.getOrNull(1)?.let { titleToCodeMap[it] }
        return IcdCodes(mainCode, extensionCode)
    }

    fun returnCodeWithParents(code: String?): List<String> {
        return code?.let { (codeToNode(code)?.parentTreeCodes ?: emptyList()) + code } ?: emptyList ()
    }

    private fun codeToNode(code: String): IcdNode? = codeToNodeMap[code]

    private fun hasExtension(icd: String) = icd.contains('&')

    companion object {
        fun create(nodes: List<IcdNode>): IcdModel {
            return IcdModel(createCodeToNodeMap(nodes), createTitleToCodeMap(nodes))
        }

        private fun createCodeToNodeMap(icdNodes: List<IcdNode>): Map<String, IcdNode> = icdNodes.associateBy { it.code }
        private fun createTitleToCodeMap(icdNodes: List<IcdNode>): Map<String, String> = icdNodes.associate { it.title to it.code }
    }

    data class IcdCodes(val mainCode: String, val extensionCode: String?)
}