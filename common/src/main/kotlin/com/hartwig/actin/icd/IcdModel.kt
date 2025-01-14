package com.hartwig.actin.icd

import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.IcdCodeEntity
import com.hartwig.actin.icd.datamodel.IcdMatches
import com.hartwig.actin.icd.datamodel.IcdNode

private enum class IcdMatchCategory {
    FULL_MATCH,
    MATCH_WITH_UNKNOWN_EXTENSION,
    NO_MATCH
}

class IcdModel(
    val codeToNodeMap: Map<String, IcdNode>,
    val titleToCodeMap: Map<String, String>
) {

    fun isValidIcdTitle(icdTitle: String): Boolean {
        val titles = icdTitle.split('&')
        return titles.size in 1..2 && titles.map(String::lowercase).all(titleToCodeMap::containsKey)
    }

    fun isValidIcdCode(icdCode: String): Boolean {
        val codes = icdCode.split('&')
        return codes.size in 1..2 && codes.all(codeToNodeMap::containsKey)
    }

    fun resolveCodeForTitle(icdTitle: String): IcdCode? {
        val split = icdTitle.lowercase().split('&')
        return titleToCodeMap[split[0]]?.let { mainCode ->
            split.takeIf { it.size == 2 }?.get(1)?.trim()?.ifEmpty { null }?.let { extensionTitle ->
                titleToCodeMap[extensionTitle]?.let { IcdCode(mainCode, it) } ?: return null
            } ?: IcdCode(mainCode, null)
        }
    }

    fun resolveTitleForCodeString(code: String): String {
        if (!isValidIcdCode(code)) {
            throw IllegalStateException("Invalid ICD code: $code")
        }
        return resolveTitleForCode(code.split('&').let { IcdCode(it[0], it.getOrNull(1)) }, displayWithSpaces = false)
    }

    fun codeWithAllParents(code: String?): List<String> {
        return code?.let { (codeToNodeMap[code]?.parentTreeCodes ?: emptyList()) + code } ?: emptyList()
    }

    fun resolveTitleForCode(icdCode: IcdCode, displayWithSpaces: Boolean = true): String {
        val separator = if (displayWithSpaces) " & " else "&"
        val extensionTitle = icdCode.extensionCode?.let { titleFromMap(it) }
        return listOfNotNull(titleFromMap(icdCode.mainCode), extensionTitle).joinToString(separator)
    }

    private fun titleFromMap(code: String): String {
        return codeToNodeMap[code]?.title ?: throw IllegalStateException("ICD title unresolvable for code $code")
    }

    fun <T : IcdCodeEntity> findInstancesMatchingAnyIcdCode(instances: List<T>, targetIcdCodes: Set<IcdCode>): IcdMatches<T> {
        val targetMainCodesWithExtensions = targetIcdCodes.mapNotNull { code -> code.extensionCode?.let { code.mainCode } }.toSet()
        val instancesByCategory = instances.groupBy { instance ->
            val allCodes = allCodesForEntity(instance)
            val allMainCodesWithUnknownExtensions = instance.icdCodes.filter { it.extensionCode == null }
                .flatMap { codeWithAllParents(it.mainCode) }
                .toSet()
            when {
                targetIcdCodes.any(allCodes::contains) -> IcdMatchCategory.FULL_MATCH
                targetMainCodesWithExtensions.any(allMainCodesWithUnknownExtensions::contains) -> {
                    IcdMatchCategory.MATCH_WITH_UNKNOWN_EXTENSION
                }

                else -> IcdMatchCategory.NO_MATCH
            }
        }
        return IcdMatches(
            instancesByCategory[IcdMatchCategory.FULL_MATCH] ?: emptyList(),
            instancesByCategory[IcdMatchCategory.MATCH_WITH_UNKNOWN_EXTENSION] ?: emptyList()
        )
    }

    private fun allCodesForEntity(entity: IcdCodeEntity): Set<IcdCode> {
        return entity.icdCodes.flatMap { code ->
            val extensionCodes = code.extensionCode?.let { codeWithAllParents(it) + null } ?: listOf(null)
            codeWithAllParents(code.mainCode).flatMap { mainCode ->
                extensionCodes.map { IcdCode(mainCode, it) }
            }
        }.toSet()
    }

    companion object {
        fun create(nodes: List<IcdNode>): IcdModel {
            return IcdModel(createCodeToNodeMap(nodes), createTitleToCodeMap(nodes))
        }

        private fun createCodeToNodeMap(icdNodes: List<IcdNode>): Map<String, IcdNode> = icdNodes.associateBy { it.code }
        private fun createTitleToCodeMap(icdNodes: List<IcdNode>): Map<String, String> =
            icdNodes.associate { it.title.lowercase() to it.code }
    }
}