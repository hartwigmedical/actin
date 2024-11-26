package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.icd.datamodel.IcdNode

private const val DEFAULT_ICD_URI = "uri"
const val DEFAULT_ICD_CODE = "1A01"
private const val DEFAULT_ICD_TITLE = "node"
private val DEFAULT_ICD_CLASS_KIND = ClassKind.CATEGORY
private const val DEFAULT_ICD_DEPTH_IN_KIND = 1
private const val DEFAULT_ICD_IS_RESIDUAL = false
private const val DEFAULT_ICD_CHAPTER_NO = "01"
private const val DEFAULT_ICD_BROWSER_LINK = "browser"
private const val DEFAULT_ICD_IS_LEAF = true

object TestIcdFactory {

    fun createProperTestModel(): IcdModel = create(
        titleToCodeMap = listOf(1, 2, 3).associate { i -> "$DEFAULT_ICD_TITLE$i" to "$DEFAULT_ICD_CODE.$i" },
    )

    private fun createMinimalTestIcdNode(): IcdNode {
        return IcdNode(
            linearizationUri = DEFAULT_ICD_URI,
            code = DEFAULT_ICD_CODE,
            title = DEFAULT_ICD_TITLE,
            classKind = DEFAULT_ICD_CLASS_KIND,
            depthInKind = DEFAULT_ICD_DEPTH_IN_KIND,
            isResidual = DEFAULT_ICD_IS_RESIDUAL,
            chapterNo = DEFAULT_ICD_CHAPTER_NO,
            browserLink = DEFAULT_ICD_BROWSER_LINK,
            isLeaf = DEFAULT_ICD_IS_LEAF
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