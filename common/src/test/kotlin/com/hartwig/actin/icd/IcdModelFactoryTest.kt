package com.hartwig.actin.icd

import com.hartwig.actin.icd.datamodel.ClassKind
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class IcdModelFactoryTest {

    private val mainCode = DEFAULT_ICD_CODE
    private val subCode = "$DEFAULT_ICD_CODE.1"
    private val secondDegreeSubcode = "$DEFAULT_ICD_CODE.10"
    private val chapterOne = TestIcdFactory.createChapter("01")
    private val chapterTwo = TestIcdFactory.createChapter("02")
    private val blockInChapterOne = TestIcdFactory.createBlock("Block1-C").copy(chapterNo = "01")
    private val blockInChapterTwo = TestIcdFactory.createBlock("xyz").copy(chapterNo = "02")
    private val categoryWithGroupings = TestIcdFactory.withGrouping(ClassKind.CATEGORY, "Block1-A", "Block1-B")
    private val codeToNodeMap =
        IcdModelFactory.createCodeToNodeMap(listOf(chapterOne, chapterTwo, blockInChapterOne, blockInChapterTwo, categoryWithGroupings))


    @Test
    fun `Should return null as parent for chapter node`() {
        listOf(chapterOne, chapterTwo).forEach { node ->
            assertThat(IcdModelFactory.solveParentForChild(node, codeToNodeMap)).isNull()
        }
    }

    @Test
    fun `Should return chapter as parent for block node with depth of 1`() {
        listOf(
            Pair(blockInChapterOne, chapterOne),
            Pair(blockInChapterTwo, chapterTwo)
        ).forEach {
            assertThat(IcdModelFactory.solveParentForChild(it.first, codeToNodeMap)).isEqualTo(it.second)
        }
    }

    @Test
    fun `Should return block matching blockId of highest grouping as parent for block node with depth above 1`() {
        val blockWithinOtherBlock =
            TestIcdFactory.withGrouping(ClassKind.BLOCK, grouping1 = "Other block", grouping2 = blockInChapterOne.code)
                .copy(depthInKind = 2)
        assertThat(IcdModelFactory.solveParentForChild(blockWithinOtherBlock, codeToNodeMap)).isEqualTo(blockInChapterOne)
    }

    @Test
    fun `Should return block matching blockId of highest grouping as parent for category node with depth of 1`() {
        val node = categoryWithGroupings.copy(depthInKind = 1, grouping3 = blockInChapterOne.blockId)
        assertThat(IcdModelFactory.solveParentForChild(node, codeToNodeMap)).isEqualTo(blockInChapterOne)
    }

    @Test
    fun `Should return category matching main code of category node with depth of 2`() {
        val mainCategory = categoryWithGroupings.copy(code = mainCode)
        val subCategory = categoryWithGroupings.copy(code = subCode, depthInKind = 2, grouping3 = blockInChapterOne.blockId)
        val codeToNodeMap = IcdModelFactory.createCodeToNodeMap(listOf(mainCategory, subCategory))

        assertThat(IcdModelFactory.solveParentForChild(subCategory, codeToNodeMap)).isEqualTo(mainCategory)
    }

    @Test
    fun `Should return category matching main code and sub code start of category node with depth of 3`() {
        val mainCategory = categoryWithGroupings.copy(code = subCode)
        val subCategory = categoryWithGroupings.copy(code = secondDegreeSubcode, depthInKind = 3)
        val codeToNodeMap = IcdModelFactory.createCodeToNodeMap(listOf(mainCategory, subCategory))

        assertThat(IcdModelFactory.solveParentForChild(subCategory, codeToNodeMap)).isEqualTo(mainCategory)

    }
}