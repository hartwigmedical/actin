package com.hartwig.actin.icd.serialization

import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.icd.datamodel.SerializedIcdNode
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.Test

class IcdDeserializerTest {

    private val rawNodes = IcdDeserializer.readFromFile(ResourceLocator.resourceOnClasspath("icd/example_icd.tsv"))

    @Test
    fun `Should read from file and create list of IcdNode instances`() {
        assertThat(rawNodes[0].foundationUri).isEqualTo("http://foundationlink/1234")
        assertThat(rawNodes[0].linearizationUri).isEqualTo("http://linearizationlink/4321")
        assertThat(rawNodes[0].blockId).isNull()
        assertThat(rawNodes[0].title).isEqualTo("Test chapter 1")
        assertThat(rawNodes[0].classKind).isEqualTo(ClassKind.CHAPTER)
        assertThat(rawNodes[0].depthInKind).isEqualTo(1)
        assertThat(rawNodes[0].isResidual).isFalse()
        assertThat(rawNodes[0].chapterNo).isEqualTo("1")
        assertThat(rawNodes[0].browserLink).isEqualTo("browser")
        assertThat(rawNodes[0].isLeaf).isFalse()
        assertThat(rawNodes[0].grouping1).isNull()
        assertThat(rawNodes[0].grouping2).isNull()
        assertThat(rawNodes[0].grouping3).isNull()
        assertThat(rawNodes[0].grouping4).isNull()
        assertThat(rawNodes[0].grouping5).isNull()

        assertThat(rawNodes[1].classKind).isEqualTo(ClassKind.BLOCK)

        assertThat(rawNodes[2].foundationUri).isNull()
        assertThat(rawNodes[2].blockId).isEqualTo("Block1-B")
        assertThat(rawNodes[2].depthInKind).isEqualTo(2)
        assertThat(rawNodes[2].grouping1).isEqualTo("Block1-A")

        assertThat(rawNodes[3].code).isEqualTo("1A01")
        assertThat(rawNodes[3].classKind).isEqualTo(ClassKind.CATEGORY)
        assertThat(rawNodes[3].grouping1).isEqualTo("Block1-A")
        assertThat(rawNodes[3].grouping2).isEqualTo("Block1-B")

        assertThat(rawNodes[4].code).isEqualTo("1A01.1")
        assertThat(rawNodes[4].depthInKind).isEqualTo(2)
        assertThat(rawNodes[4].isLeaf).isTrue()
    }

    @Test
    fun `Should throw IllegalArgumentException when DepthInKind or ChapterNo is zero`() {
        assertThatIllegalArgumentException().isThrownBy {
            IcdDeserializer.readFromFile(ResourceLocator.resourceOnClasspath("icd/invalid_icd_example.tsv"))
        }
    }

    @Test
    fun `Should resolve code from chapterNo when classKind is chapter`() {
        val raw = createRawNode(chapterNo = "2", classKind = ClassKind.CHAPTER)
        assertThat(IcdDeserializer.resolveCode(raw)).isEqualTo("2")
    }

    @Test
    fun `Should resolve code from blockId when classKind is block`() {
        val raw = createRawNode(blockId = "Block-2A", classKind = ClassKind.BLOCK)
        assertThat(IcdDeserializer.resolveCode(raw)).isEqualTo("Block-2A")
    }

    @Test
    fun `Should resolve code from code when classKind is category`() {
        val raw = createRawNode(code = "1A20", classKind = ClassKind.CATEGORY)
        assertThat(IcdDeserializer.resolveCode(raw)).isEqualTo("1A20")
    }

    @Test
    fun `Should set parent code to null when classKind is chapter`() {
        val child = createRawNode(classKind = ClassKind.CHAPTER)
        assertThat(IcdDeserializer.resolveParentCode(child)).isNull()
    }

    @Test
    fun `Should set parent code to chapterNo when classKind is block and depthInKind is 1`() {
        val child = createRawNode(chapterNo = "3", classKind = ClassKind.BLOCK)
        assertThat(IcdDeserializer.resolveParentCode(child)).isEqualTo("3")
    }

    @Test
    fun `Should set parent code to highest grouping when classKind is block and depthInKind is not 1`() {
        val child = createRawNode(classKind = ClassKind.BLOCK, depthInKind = 2, grouping1 = "Block1-A", grouping2 = "Block1-B")
        assertThat(IcdDeserializer.resolveParentCode(child)).isEqualTo("Block1-B")
    }

    @Test
    fun `Should set parent code to highest grouping when classKind is category and depthInKind is 1`() {
        val child = createRawNode(classKind = ClassKind.CATEGORY, depthInKind = 1, grouping1 = "Block1-A", grouping2 = "Block1-B")
        assertThat(IcdDeserializer.resolveParentCode(child)).isEqualTo("Block1-B")
    }

    @Test
    fun `Should set parent code to code with subcode removed when classKind is category and depthInKind is not 1`() {
        val (depthTwoChild, depthThreeChild) = listOf(2 to "1A01.1", 3 to "1A01.10").map {
            createRawNode(
                classKind = ClassKind.CATEGORY,
                depthInKind = it.first,
                code = it.second
            )
        }
        assertThat(IcdDeserializer.resolveParentCode(depthTwoChild)).isEqualTo("1A01")
        assertThat(IcdDeserializer.resolveParentCode(depthThreeChild)).isEqualTo("1A01.1")
    }

    @Test
    fun `Should correctly solve codes of full parent tree`() {
        val chapter = createRawNode(classKind = ClassKind.CHAPTER)
        assertThat(IcdDeserializer.resolveFullParentTree(chapter)).isEmpty()

        val (blockDepthOne, blockDepthTwo) = listOf(1, 2).map {
            createRawNode(chapterNo = "1", classKind = ClassKind.BLOCK, depthInKind = it, grouping1 = "Block1-A", grouping2 = "Block1-B")
        }
        assertThat(IcdDeserializer.resolveFullParentTree(blockDepthOne)).containsExactlyElementsOf(listOf("1"))
        assertThat(IcdDeserializer.resolveFullParentTree(blockDepthTwo)).containsExactlyElementsOf(listOf("1", "Block1-A", "Block1-B"))

        val (categoryDepthOne, categoryDepthTwo) = listOf(1 to "1A01", 2 to "1A01.1").map {
            createRawNode(chapterNo = "2", code = it.second, classKind = ClassKind.CATEGORY, depthInKind = it.first, grouping1 = "Block1-A")
        }
        assertThat(IcdDeserializer.resolveFullParentTree(categoryDepthOne)).containsExactlyElementsOf(listOf("2", "Block1-A"))
        assertThat(IcdDeserializer.resolveFullParentTree(categoryDepthTwo)).containsExactlyElementsOf(listOf("2", "Block1-A", "1A01"))
    }

    @Test
    fun `Should trim all leading '-' characters from title`() {
        val raw = createRawNode(title = "---some--title---")
        assertThat(IcdDeserializer.trimTitle(raw)).isEqualTo("some--title---")
    }

    private fun createRawNode(
        chapterNo: String = "1",
        blockId: String? = null,
        title: String = "title",
        code: String? = null,
        classKind: ClassKind = ClassKind.CATEGORY,
        depthInKind: Int = 1,
        grouping1: String? = null,
        grouping2: String? = null,
        grouping3: String? = null,
        grouping4: String? = null,
        grouping5: String? = null,
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
            grouping2,
            grouping3,
            grouping4,
            grouping5,
        )
    }
}