package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.icd.datamodel.IcdNode

object TestIcdFactory {

    fun createMinimalIcdModel(): IcdModel = create()

    private fun createMinimalTestIcdNode(): IcdNode {
        return IcdNode(
            linearizationUri = "uri",
            code = "A01",
            title = "Test entry",
            classKind = ClassKind.CATEGORY,
            depthInKind = 1,
            isResidual = false,
            chapterNo = "01",
            browserLink = "browser",
            isLeaf = true
        )
    }

    fun createChapter(chapterNo: String): IcdNode {
        return createMinimalTestIcdNode().copy(
            classKind = ClassKind.CHAPTER,
            code = chapterNo,
            chapterNo = chapterNo
        )
    }

    fun createBlock(blockId: String): IcdNode {
        return createMinimalTestIcdNode().copy(
            classKind = ClassKind.BLOCK,
            code = blockId,
            blockId = blockId
        )
    }

    fun withGrouping(
        classKind: ClassKind,
        grouping1: String,
        grouping2: String? = null,
        grouping3: String? = null,
        grouping4: String? = null,
        grouping5: String? = null
    ): IcdNode {
        return createMinimalTestIcdNode().copy(
            classKind = classKind,
            grouping1 = grouping1,
            grouping2 = grouping2,
            grouping3 = grouping3,
            grouping4 = grouping4,
            grouping5 = grouping5
        )
    }

    private fun create(
        childToParentMap: Map<IcdNode, IcdNode> = emptyMap(),
        codeToNodeMap: Map<String, IcdNode> = emptyMap(),
        titleToCodeMap: Map<String, String> = emptyMap(),
    ): IcdModel {
        return IcdModel(childToParentMap, codeToNodeMap, titleToCodeMap)
    }
}