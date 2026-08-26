package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.IcdNode

object TestIcdFactory {

    private const val DEFAULT_ICD_CODE = "1A01"
    private val DEFAULT_ICD_PARENT_CODE_LIST = listOf("1", "Block-1A")
    private const val DEFAULT_ICD_TITLE = "node"
    private const val DEFAULT_EXTENSION_CODE = "XA01"
    private val DEFAULT_EXTENSION_PARENT_CODE_LIST = listOf("X")
    private const val DEFAULT_EXTENSION_TITLE = "extension node"

    fun createTestModel(): IcdModel = IcdModel.create(
        listOf(1, 2, 3).map { i ->
            IcdNode("$DEFAULT_ICD_CODE.$i", DEFAULT_ICD_PARENT_CODE_LIST, "$DEFAULT_ICD_TITLE $i")
        } + IcdNode(DEFAULT_EXTENSION_CODE, DEFAULT_EXTENSION_PARENT_CODE_LIST, DEFAULT_EXTENSION_TITLE, isExtension = true)
    )

    fun createModelWithSpecificNodes(mainNodePrefixes: List<String>, extensionNodePrefixes: List<String> = emptyList()) =
        IcdModel.create(
            mainNodePrefixes.map { node(it, isExtension = false) } + extensionNodePrefixes.map { node(it, isExtension = true) }
        )

    private fun node(prefix: String, isExtension: Boolean) =
        IcdNode(prefix + "Code", listOf(prefix + "ParentCode"), prefix + "Title", isExtension)
}
