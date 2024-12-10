package com.hartwig.actin.icd.serialization

import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.icd.datamodel.SerializedIcdNode
import com.hartwig.actin.icd.serialization.IcdDeserializer.determineChapterType
import com.hartwig.actin.icd.serialization.IcdDeserializer.resolveCode
import com.hartwig.actin.icd.serialization.IcdDeserializer.resolveParentsForRegularChapter
import com.hartwig.actin.icd.serialization.IcdDeserializer.returnExtensionChapterNodeWithParents
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.Test

private const val DEFAULT_CHAPTER_NO = "1"
private const val DEFAULT_TITLE = "title"
private const val DEFAULT_DEPTH = 1
private const val DEFAULT_CODE = "1A"

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
        assertThat(resolveCode(raw)).isEqualTo("2")
    }

    @Test
    fun `Should resolve code from blockId when classKind is block and chapter type is regular`() {
        val raw = createRawNode(blockId = "Block-2A", classKind = ClassKind.BLOCK)
        assertThat(resolveCode(raw)).isEqualTo("Block-2A")
    }

    @Test
    fun `Should resolve code from linearization URI when classKind is block and chapter type is not regular`() {
        listOf("X", "V").forEach { chapterNo ->
            val raw = createRawNode(chapterNo = chapterNo, blockId = "Block-2A", classKind = ClassKind.BLOCK).copy(linearizationUri = "test")
            assertThat(resolveCode(raw)).isEqualTo("test")
        }
    }

    @Test
    fun `Should resolve code from code when classKind is category`() {
        val raw = createRawNode(code = "1A20", classKind = ClassKind.CATEGORY)
        assertThat(resolveCode(raw)).isEqualTo("1A20")
    }

    @Test
    fun `Should correctly solve codes of full parent tree for regular chapters`() {
        val chapter = createRawNode(classKind = ClassKind.CHAPTER)
        assertThat(resolveParentsForRegularChapter(chapter)).isEmpty()

        val (blockDepthOne, blockDepthTwo) = listOf(1, 2).map {
            createRawNode(chapterNo = "1", classKind = ClassKind.BLOCK, depthInKind = it, grouping1 = "Block1-A", grouping2 = "Block1-B")
        }
        assertThat(resolveParentsForRegularChapter(blockDepthOne)).containsExactlyElementsOf(listOf("1"))
        assertThat(resolveParentsForRegularChapter(blockDepthTwo)).containsExactlyElementsOf(listOf("1", "Block1-A", "Block1-B"))

        val (categoryDepthOne, categoryDepthTwo) = listOf(1 to "1A01", 2 to "1A01.1").map {
            createRawNode(chapterNo = "2", code = it.second, classKind = ClassKind.CATEGORY, depthInKind = it.first, grouping1 = "Block1-A")
        }
        assertThat(resolveParentsForRegularChapter(categoryDepthOne)).containsExactlyElementsOf(listOf("2", "Block1-A"))
        assertThat(resolveParentsForRegularChapter(categoryDepthTwo)).containsExactlyElementsOf(listOf("2", "Block1-A", "1A01"))
    }

    @Test
    fun `Should correctly return ICD Node with full parent tree for extension chapters`() {
        val extensionNodes = rawNodes.filter { determineChapterType(it) == IcdChapterType.EXTENSION_CODES }
        val result = returnExtensionChapterNodeWithParents(extensionNodes)
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

    @Test
    fun `Should trim all leading '-' characters from title`() {
        val raw = createRawNode(title = "---some--title---")
        assertThat(IcdDeserializer.trimTitle(raw)).isEqualTo("some--title---")
    }

    @Test
    fun `Should correctly determine chapter type`() {
        assertThat(determineChapterType(createRawNode(chapterNo = "X"))).isEqualTo(IcdChapterType.EXTENSION_CODES)
        assertThat(determineChapterType(createRawNode(chapterNo = "V"))).isEqualTo(IcdChapterType.FUNCTIONING_ASSESSMENT)
        assertThat(determineChapterType(createRawNode(chapterNo = "1"))).isEqualTo(IcdChapterType.REGULAR)
    }

    private fun createRawNode(
        chapterNo: String = DEFAULT_CHAPTER_NO,
        blockId: String? = null,
        title: String = DEFAULT_TITLE,
        code: String? = DEFAULT_CODE,
        classKind: ClassKind = ClassKind.CATEGORY,
        depthInKind: Int = DEFAULT_DEPTH,
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