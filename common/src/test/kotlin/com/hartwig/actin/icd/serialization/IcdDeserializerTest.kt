package com.hartwig.actin.icd.serialization

import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.icd.datamodel.SerializedIcdNode
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val DEFAULT_CHAPTER_NO = "1"
private const val DEFAULT_TITLE = "title"
private const val DEFAULT_DEPTH = 1
private const val DEFAULT_CODE = "1A"

class IcdDeserializerTest {

    @Test
    fun `Should correctly solve codes of full parent tree for regular chapters`() {
        val chapter = createRawNode(classKind = ClassKind.CHAPTER)

        val (blockDepthOne, blockDepthTwo) = listOf(1, 2).map {
            createRawNode(
                chapterNo = DEFAULT_CHAPTER_NO,
                blockId = "Block1-B",
                classKind = ClassKind.BLOCK,
                depthInKind = it,
                grouping1 = "Block1-A"
            )
        }

        val (categoryDepthOne, categoryDepthTwo) = listOf(1 to "1A01", 2 to "1A01.1").map {
            createRawNode(
                chapterNo = DEFAULT_CHAPTER_NO,
                code = it.second,
                classKind = ClassKind.CATEGORY,
                depthInKind = it.first,
                grouping1 = "Block1-A"
            )
        }

        val result = IcdDeserializer.create(listOf(chapter, blockDepthOne, blockDepthTwo, categoryDepthOne, categoryDepthTwo))

        assertThat(result).hasSize(5)
        assertThat(result[0].parentTreeCodes).isEmpty()
        assertThat(result[1].parentTreeCodes).containsExactlyElementsOf(listOf(DEFAULT_CHAPTER_NO))
        assertThat(result[2].parentTreeCodes).containsExactlyElementsOf(listOf(DEFAULT_CHAPTER_NO, "Block1-A"))
        assertThat(result[3].parentTreeCodes).containsExactlyElementsOf(listOf(DEFAULT_CHAPTER_NO, "Block1-A"))
        assertThat(result[4].parentTreeCodes).containsExactlyElementsOf(listOf(DEFAULT_CHAPTER_NO, "Block1-A", "1A01"))
    }

    @Test
    fun `Should correctly return ICD Node with full parent tree for extension chapters`() {
        val extensionNodes =
            CsvReader.readFromFile(ResourceLocator.resourceOnClasspath("icd/example_icd.tsv")).filter { it.chapterNo == "X" }

        val result = IcdDeserializer.create(extensionNodes)
        assertThat(result).hasSize(8)
        assertThat(result[0].parentTreeCodes).isEmpty()
        assertThat(result[1].parentTreeCodes).containsExactly("X")
        assertThat(result[2].parentTreeCodes).containsExactly("X", "http://linearizationlink/X1")
        assertThat(result[3].parentTreeCodes).containsExactly("X", "http://linearizationlink/X1", "http://linearizationlink/X11")
        assertThat(result[4].parentTreeCodes).containsExactly("X", "http://linearizationlink/X1", "http://linearizationlink/X11", "X00")
        assertThat(result[5].parentTreeCodes).containsExactly("X", "http://linearizationlink/X1", "http://linearizationlink/X11")
        assertThat(result[6].parentTreeCodes).containsExactly("X", "http://linearizationlink/X1", "http://linearizationlink/X11", "X01")
        assertThat(result[7].parentTreeCodes).containsExactly("X", "http://linearizationlink/X1")
    }

    private fun createRawNode(
        chapterNo: String = DEFAULT_CHAPTER_NO,
        blockId: String? = null,
        title: String = DEFAULT_TITLE,
        code: String? = DEFAULT_CODE,
        classKind: ClassKind = ClassKind.CATEGORY,
        depthInKind: Int = DEFAULT_DEPTH,
        grouping1: String? = null
    ): SerializedIcdNode {
        return SerializedIcdNode(
            "http://foundationlink/1234",
            "http://linearizationlink/4321",
            code,
            blockId,
            title,
            classKind,
            depthInKind,
            isResidual = false,
            chapterNo,
            "browser",
            isLeaf = false,
            primaryTabulation = true,
            grouping1,
            null,
            null,
            null,
            null,
        )
    }
}