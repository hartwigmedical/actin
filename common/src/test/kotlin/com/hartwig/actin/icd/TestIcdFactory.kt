package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.IcdNode

object TestIcdFactory {

    private const val DEFAULT_ICD_CODE = "1A01"
    private val DEFAULT_ICD_PARENT_CODE_LIST = listOf("1", "Block-1A")
    private const val DEFAULT_ICD_TITLE = "node"

    fun createTestModel(): IcdModel = create(
        codeToNodeMap = listOf(1, 2, 3).associate { i ->
            "$DEFAULT_ICD_CODE.$i" to IcdNode(
                "$DEFAULT_ICD_CODE.$i",
                DEFAULT_ICD_PARENT_CODE_LIST,
                "$DEFAULT_ICD_TITLE $i"
            )
        },
        titleToCodeMap = listOf(1, 2, 3).associate { i -> "$DEFAULT_ICD_TITLE $i" to "$DEFAULT_ICD_CODE.$i" },
    )

    private fun create(
        codeToNodeMap: Map<String, IcdNode> = emptyMap(),
        titleToCodeMap: Map<String, String> = emptyMap(),
    ): IcdModel {
        return IcdModel(codeToNodeMap, titleToCodeMap)
    }
}