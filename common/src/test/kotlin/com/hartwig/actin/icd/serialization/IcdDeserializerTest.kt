package com.hartwig.actin.icd.serialization

import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.icd.serialization.IcdDeserializer.readFromFile
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.Test

class IcdDeserializerTest {

    private val result =
        readFromFile("${System.getProperty("user.home")}/hmf/repos/actin/common/src/test/resources/icd/example_icd.tsv")

    @Test
    fun `Should not ingest nodes with a letter as ChapterNo`() {
        assertThat(result).hasSize(6)

        listOf(1, 2, 3, 4, 5).forEach {
            assertThat(result[it].chapterNo).isEqualTo("1")
        }
    }

    @Test
    fun `Should read from file and create list of IcdNode instances`() {
        assertThat(result[0].foundationUri).isEqualTo("http://foundationlink/1234")
        assertThat(result[0].linearizationUri).isEqualTo("http://linearizationlink/4321")
        assertThat(result[0].blockId).isNull()
        assertThat(result[0].title).isEqualTo("Test chapter 1")
        assertThat(result[0].classKind).isEqualTo(ClassKind.CHAPTER)
        assertThat(result[0].depthInKind).isEqualTo(1)
        assertThat(result[0].isResidual).isFalse()
        assertThat(result[0].chapterNo).isEqualTo("1")
        assertThat(result[0].browserLink).isEqualTo("browser")
        assertThat(result[0].isLeaf).isFalse()
        assertThat(result[0].grouping1).isNull()
        assertThat(result[0].grouping2).isNull()
        assertThat(result[0].grouping3).isNull()
        assertThat(result[0].grouping4).isNull()
        assertThat(result[0].grouping5).isNull()

        assertThat(result[1].classKind).isEqualTo(ClassKind.BLOCK)

        assertThat(result[2].foundationUri).isNull()
        assertThat(result[2].blockId).isEqualTo("Block1-B")
        assertThat(result[2].depthInKind).isEqualTo(2)
        assertThat(result[2].grouping1).isEqualTo("Block1-A")

        assertThat(result[3].code).isEqualTo("1A01")
        assertThat(result[3].classKind).isEqualTo(ClassKind.CATEGORY)
        assertThat(result[3].grouping1).isEqualTo("Block1-A")
        assertThat(result[3].grouping2).isEqualTo("Block1-B")

        assertThat(result[4].code).isEqualTo("1A01.1")
        assertThat(result[4].depthInKind).isEqualTo(2)
        assertThat(result[4].isLeaf).isTrue()
    }

    @Test
    fun `Should use ChapterNo (chapters) and BlockId (blocks) as code property`() {
        assertThat(result[0].code).isEqualTo("1").isEqualTo(result[0].chapterNo)
        assertThat(result[1].code).isEqualTo("Block1-A").isEqualTo(result[1].blockId)
    }

    @Test
    fun `Should throw IllegalArgumentException when DepthInKind or ChapterNo is zero`() {
        assertThatIllegalArgumentException().isThrownBy {
            readFromFile("${System.getProperty("user.home")}/hmf/repos/actin/common/src/test/resources/icd/invalid_icd_example.tsv")
        }
    }
}