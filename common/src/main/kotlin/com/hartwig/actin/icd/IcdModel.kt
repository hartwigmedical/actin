package com.hartwig.actin.icd

import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.IcdCodeHolder
import com.hartwig.actin.icd.datamodel.IcdMatches
import com.hartwig.actin.icd.datamodel.IcdNode

class IcdModel(
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

    fun resolveCodeForTitle(icdTitle: String): IcdCode? {
        val split = icdTitle.split('&')
        val mainCode = titleToCodeMap[split[0]] ?: return null
        val extensionCode = when {
            split.size != 2 || split[1].trim().isEmpty() -> null
            else -> titleToCodeMap[split[1]] ?: return null
        }
        return IcdCode(mainCode, extensionCode)
    }

    fun returnCodeWithParents(code: String?): List<String> {
        return code?.let { (codeToNode(code)?.parentTreeCodes ?: emptyList()) + code } ?: emptyList()
    }

    fun resolveTitleForCode(icdCode: IcdCode): String {
        val mainTitle = codeToNode(icdCode.mainCode)?.title ?: return ""
        val extensionTitle = icdCode.extensionCode?.let { codeToNode(it)?.title }
        return extensionTitle?.let { "$mainTitle & $it" } ?: mainTitle
    }

    fun <T : IcdCodeHolder> findInstancesMatchingAnyIcdCode(instances: List<T>?, targetIcdCodes: Set<IcdCode>): IcdMatches<T> {
        val (fullMatches, unknownExtensionMatches) = if (instances == null) {
            Pair(emptyList<T>(), emptyList<T>())
        } else {
            targetIcdCodes.fold(Pair(emptyList(), emptyList())) { acc, targetCode ->
                val (fullMatch, unknownMatch) = returnIcdMatches(targetCode, instances)
                Pair(acc.first + fullMatch, acc.second + unknownMatch)
            }
        }
        return IcdMatches(fullMatches, unknownExtensionMatches)
    }

    private fun <T : IcdCodeHolder> returnIcdMatches(targetCode: IcdCode, instances: List<T>): Pair<List<T>, List<T>> {
        val mainMatches = instances.filter { instance ->
            returnCodeWithParents(instance.icdCode.mainCode).any(targetCode.mainCode::equals)
        }

        return if (targetCode.extensionCode == null) {
            Pair(mainMatches, emptyList())
        } else {
            mainMatches.filter {
                it.icdCode.extensionCode?.let { code ->
                    returnCodeWithParents(code).any(targetCode.extensionCode::equals)
                } != false
            }.partition { it.icdCode.extensionCode != null }
        }
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
}