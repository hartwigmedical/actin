package com.hartwig.actin.icd.datamodel

class IcdMapFactory {

    fun createCodeToNodeMap(icdNodes: List<IcdNode>): Map<String, IcdNode> {
        return icdNodes.associateBy { it.code }
    }

    fun createTitleToCodeMap(icdNodes: List<IcdNode>): Map<String, String> {
        return icdNodes.associate { it.title to it.code }
    }
}