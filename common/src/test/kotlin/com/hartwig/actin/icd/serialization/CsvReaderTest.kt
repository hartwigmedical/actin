package com.hartwig.actin.icd.serialization

import com.hartwig.actin.icd.datamodel.ClassKind
import com.hartwig.actin.testutil.ResourceLocator
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatIllegalArgumentException
import org.junit.Test

class CsvReaderTest {

    private val rawNodes = CsvReader.readFromFile(ResourceLocator.resourceOnClasspath("icd/example_icd.tsv"))

    @Test
    fun `Should read from file and create list of SerializedIcdNode instances`() {
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
            CsvReader.readFromFile(ResourceLocator.resourceOnClasspath("icd/invalid_icd_example.tsv"))
        }
    }
}