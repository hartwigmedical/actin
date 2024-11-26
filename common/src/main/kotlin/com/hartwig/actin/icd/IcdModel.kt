package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.IcdNode

data class IcdModel(
    val childToParentsMap: Map<IcdNode, IcdNode?>,
    val codeToNodeMap: Map<String, IcdNode>,
    val titleToCodeMap: Map<String, String>
) {

    fun resolveTitleForCode(code: String): String? {
        return codeToNodeMap[code]?.title
    }

    fun resolveCodeForTitle(title: String): String? {
        return titleToCodeMap[title.lowercase()]
    }
}