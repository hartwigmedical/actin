package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.IcdNode

data class IcdModel(
    val childToParentsMap: Map<IcdNode, IcdNode?>,
    val codeToNodeMap: Map<String, IcdNode>,
    val titleToCodeMap: Map<String, String>
) {

//    fun nodeWithParents(node: IcdNode): Set<String> {
//        //return expandedDoidSet(setOf(doid), emptySet())
//    }

    fun resolveTitleForCode(code: String): String? {
        return codeToNodeMap[code]?.title
    }

    fun resolveCodeForTitle(title: String): String? {
        return titleToCodeMap[title.lowercase()]
    }

//    private tailrec fun expandedDoidSet(doidsToExpand: Set<String>, expandedDoids: Set<String>): Set<String> {
//        if (doidsToExpand.isEmpty()) {
//            return expandedDoids
//        }
//        val nextDoid = doidsToExpand.first()
//        val newDoids = if (nextDoid in expandedDoids) emptySet() else {
//            (childToParentsMap[nextDoid] ?: emptyList()) + listOfNotNull(doidManualConfig.additionalDoidsPerDoid[nextDoid])
//        }
//        return expandedDoidSet(doidsToExpand + newDoids - nextDoid, expandedDoids + nextDoid)
//    }
}