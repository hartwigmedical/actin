package com.hartwig.actin.icd

import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.IcdCodeEntity
import com.hartwig.actin.icd.datamodel.IcdMatches
import com.hartwig.actin.icd.datamodel.IcdNode

class IcdModel(
    val codeToNodeMap: Map<String, IcdNode>,
    val titleToCodeMap: Map<String, String>
) {

    fun isValidIcdTitle(icdTitle: String): Boolean {
        val titles = icdTitle.split('&')
        return titles.size in 1..2 && titles.all(titleToCodeMap::containsKey)
    }

    fun resolveCodeForTitle(icdTitle: String): IcdCode? {
        val split = icdTitle.split('&')
        return titleToCodeMap[split[0]]?.let { mainCode ->
            split.takeIf { it.size == 2 }?.get(1)?.trim()?.ifEmpty { null }?.let { extensionTitle ->
                titleToCodeMap[extensionTitle]?.let { IcdCode(mainCode, it) } ?: return null
            } ?: IcdCode(mainCode, null)
        }
    }

    fun returnCodeWithParents(code: String?): List<String> {
        return code?.let { (codeToNodeMap[code]?.parentTreeCodes ?: emptyList()) + code } ?: emptyList()
    }

    fun resolveTitleForCode(icdCode: IcdCode): String {
        val mainTitle = codeToNodeMap[icdCode.mainCode]?.title ?: return ""
        val extensionTitle = icdCode.extensionCode?.let { codeToNodeMap[it]?.title }
        return extensionTitle?.let { "$mainTitle & $it" } ?: mainTitle
    }

    fun <T : IcdCodeEntity> findInstancesMatchingAnyIcdCode(instances: List<T>?, targetIcdCodes: Set<IcdCode>): IcdMatches<T> {
        return instances?.let {
            targetIcdCodes.fold(IcdMatches(emptyList(), emptyList())) { acc, targetCode ->
                val (fullMatch, unknownMatch) = returnIcdMatches(targetCode, instances)
                IcdMatches(acc.fullMatches + fullMatch, acc.mainCodeMatchesWithUnknownExtension + unknownMatch)
            }
        } ?: IcdMatches(emptyList(), emptyList())
    }

    private fun <T : IcdCodeEntity> returnIcdMatches(targetCode: IcdCode, instances: List<T>): Pair<List<T>, List<T>> {
        val mainMatches = instances.filter { instance ->
            instance.icdCodes.any {
                returnCodeWithParents(it.mainCode).any(targetCode.mainCode::equals)
            }
        }

        return if (targetCode.extensionCode == null) {
            Pair(mainMatches, emptyList())
        } else {
            mainMatches.filter { match ->
                match.icdCodes.any {
                    it.extensionCode?.let { code ->
                        returnCodeWithParents(code).any(targetCode.extensionCode::equals)
                    } != false
                }
            }.partition { it.icdCodes.none { it.extensionCode == null } }
        }
    }

    companion object {
        fun create(nodes: List<IcdNode>): IcdModel {
            return IcdModel(createCodeToNodeMap(nodes), createTitleToCodeMap(nodes))
        }

        private fun createCodeToNodeMap(icdNodes: List<IcdNode>): Map<String, IcdNode> = icdNodes.associateBy { it.code }
        private fun createTitleToCodeMap(icdNodes: List<IcdNode>): Map<String, String> = icdNodes.associate { it.title to it.code }
    }
}