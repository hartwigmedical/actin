package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.IcdNode

const val DEFAULT_ICD_CODE = "1A01"
private const val DEFAULT_ICD_TITLE = "node"

object TestIcdFactory {

    fun createProperTestModel(): IcdModel = create(
        titleToCodeMap = listOf(1, 2, 3).associate { i -> "$DEFAULT_ICD_TITLE$i" to "$DEFAULT_ICD_CODE.$i" },
    )

    private fun create(
        codeToNodeMap: Map<String, IcdNode> = emptyMap(),
        titleToCodeMap: Map<String, String> = emptyMap(),
    ): IcdModel {
        return IcdModel(codeToNodeMap, titleToCodeMap)
    }
}