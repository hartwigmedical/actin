package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.IcdNode

const val DEFAULT_ICD_CODE = "1A01"
const val DEFAULT_ICD_PARENT_CODE = "Block-1A"
private const val DEFAULT_ICD_TITLE = "node"

object TestIcdFactory {

    fun createTestModel(): IcdModel = create(
        codeToNodeMap = listOf(1, 2, 3).associate { i ->
            "$DEFAULT_ICD_CODE.$i" to IcdNode(
                "$DEFAULT_ICD_CODE.$i",
                DEFAULT_ICD_PARENT_CODE,
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