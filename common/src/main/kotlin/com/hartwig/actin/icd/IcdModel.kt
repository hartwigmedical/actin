package com.hartwig.actin.icd

import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.datamodel.IcdMatches
import com.hartwig.actin.icd.datamodel.IcdNode

private const val SLOT_SEPARATOR = '&'
private const val MAIN_SLOT = "a main title"
private const val EXTENSION_SLOT = "an extension title"

private enum class IcdMatchCategory {
    FULL_MATCH,
    MATCH_WITH_UNKNOWN_EXTENSION,
    NO_MATCH
}

class IcdModel(
    val codeToNodeMap: Map<String, IcdNode>,
    val titleToCodeMap: Map<String, String>
) {

    val mainTitleToCodeMap = titleToCodeMapForSlot(extensionSlot = false)
    val extensionTitleToCodeMap = titleToCodeMapForSlot(extensionSlot = true)

    fun isMainCode(code: String): Boolean = codeToNodeMap[code]?.isExtension == false

    fun isExtensionCode(code: String): Boolean = codeToNodeMap[code]?.isExtension == true

    fun isValidIcdTitle(icdTitle: String): Boolean = resolveCodeForTitle(icdTitle) != null

    fun isValidIcdCode(icdCode: String): Boolean = resolveCodeForCodeString(icdCode) != null

    fun resolveCodeForTitle(icdTitle: String): IcdCode? {
        val (mainTitle, extensionTitle) = splitSlots(icdTitle) ?: return null
        val mainCode = mainTitleToCodeMap[mainTitle.lowercase()] ?: return null
        val extensionCode = extensionTitle?.let { extensionTitleToCodeMap[it.lowercase()] ?: return null }

        return IcdCode(mainCode, extensionCode)
    }

    fun invalidTitleReason(icdTitle: String): String? {
        val (mainTitle, extensionTitle) = splitSlots(icdTitle) ?: return malformedTitleReason(icdTitle)

        return invalidMainTitleReason(mainTitle) ?: extensionTitle?.let(::invalidExtensionTitleReason)
    }

    fun invalidMainTitleReason(mainTitle: String): String? =
        unknownOrMisplacedTitleReason(mainTitle, mainTitleToCodeMap, extensionTitleToCodeMap, EXTENSION_SLOT, MAIN_SLOT)

    fun invalidExtensionTitleReason(extensionTitle: String): String? =
        unknownOrMisplacedTitleReason(extensionTitle, extensionTitleToCodeMap, mainTitleToCodeMap, MAIN_SLOT, EXTENSION_SLOT)

    fun resolveTitleForCodeString(code: String): String {
        val icdCode = resolveCodeForCodeString(code) ?: throw IllegalStateException("Invalid ICD code: $code")
        return resolveTitleForCode(icdCode, displayWithSpaces = false)
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

    fun <T : Comorbidity> findInstancesMatchingAnyIcdCode(instances: List<T>, targetIcdCodes: Iterable<IcdCode>): IcdMatches<T> {
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

    fun <T: Comorbidity> findInstancesMatchingAnyExtensionCode(instances: List<T>, targetExtensionCodes: Set<String>): List<Comorbidity> {
        return instances.filter { entity ->
            entity.icdCodes.any { codeWithAllParents(it.extensionCode).any(targetExtensionCodes::contains) }
        }
    }

    private fun allCodesForEntity(entity: Comorbidity): Set<IcdCode> {
        return entity.icdCodes.flatMap { code ->
            val extensionCodes = code.extensionCode?.let { codeWithAllParents(it) + null } ?: listOf(null)
            codeWithAllParents(code.mainCode).flatMap { mainCode ->
                extensionCodes.map { IcdCode(mainCode, it) }
            }
        }.toSet()
    }

    private fun titleToCodeMapForSlot(extensionSlot: Boolean): Map<String, String> =
        codeToNodeMap.values.filter { it.isExtension == extensionSlot }
            .associate { it.title.lowercase() to it.code }

    private fun resolveCodeForCodeString(code: String): IcdCode? {
        val (mainCode, extensionCode) = splitSlots(code) ?: return null
        val slotsAreValid = isMainCode(mainCode) && (extensionCode == null || isExtensionCode(extensionCode))

        return IcdCode(mainCode, extensionCode).takeIf { slotsAreValid }
    }

    private fun malformedTitleReason(icdTitle: String): String =
        "ICD title [$icdTitle] must be a single main title, optionally followed by '$SLOT_SEPARATOR' and one extension title"

    private fun unknownOrMisplacedTitleReason(
        title: String,
        expectedSlotTitles: Map<String, String>,
        otherSlotTitles: Map<String, String>,
        otherSlot: String,
        expectedSlot: String
    ): String? {
        val lookupTitle = title.lowercase()
        return when {
            lookupTitle in expectedSlotTitles -> null
            lookupTitle in otherSlotTitles -> "ICD title [$title] is $otherSlot and cannot be used as $expectedSlot"
            else -> "ICD title [$title] is not known - check for existence in ICD model"
        }
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

private fun splitSlots(input: String): Pair<String, String?>? {
    val slots = input.split(SLOT_SEPARATOR).map(String::trim)
    return slots.takeIf { it.size in 1..2 && it.first().isNotEmpty() }
        ?.let { it.first() to it.getOrNull(1)?.ifEmpty { null } }
}
