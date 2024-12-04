package com.hartwig.actin.icd.datamodel

import com.hartwig.actin.icd.serialization.IcdDeserializer

class IcdNode(val code: String, val parentTreeCodes: List<String>, val title: String) {

    companion object {
        fun create(rawNode: SerializedIcdNode): IcdNode {
            return IcdNode(
                IcdDeserializer.resolveCode(rawNode),
                IcdDeserializer.resolveFullParentTree(rawNode),
                IcdDeserializer.trimTitle(rawNode)
            )
        }
    }
}
