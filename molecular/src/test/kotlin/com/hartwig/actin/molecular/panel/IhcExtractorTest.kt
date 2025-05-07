package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.IhcTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private const val POSITIVE_FUSION_GENE = "ALK"
private const val NEGATIVE_FUSION_GENE = "ROS1"
private const val OTHER_GENE = "GENE"

class IhcExtractorTest {

    @Test
    fun `Should extract fusion positives from IHC`() {
        val ihcTests = listOf(positiveIhc(POSITIVE_FUSION_GENE))

        val result = IhcExtractor().extract(ihcTests)
        assertThat(result).isEqualTo(listOf(IhcExtraction(null, setOf(POSITIVE_FUSION_GENE), emptySet())))
    }

    @Test
    fun `Should extract fusion negatives from IHC`() {
        val ihcTests = listOf(negativeIhc(NEGATIVE_FUSION_GENE))

        val result = IhcExtractor().extract(ihcTests)
        assertThat(result).isEqualTo(listOf(IhcExtraction(null, emptySet(), setOf(NEGATIVE_FUSION_GENE))))
    }

    @Test
    fun `Should ignore other genes`() {
        val ihcTests = listOf(positiveIhc(OTHER_GENE))

        val result = IhcExtractor().extract(ihcTests)
        assertThat(result).isEmpty()
    }

    @Test
    fun `Should group IHC tests by date`() {
        val date1 = LocalDate.of(2023, 1, 1)
        val date2 = LocalDate.of(2023, 2, 1)
        val ihcTests = listOf(
            positiveIhc(POSITIVE_FUSION_GENE, date1),
            negativeIhc(NEGATIVE_FUSION_GENE, date1),
            positiveIhc(POSITIVE_FUSION_GENE, date2),
            positiveIhc(OTHER_GENE, date2)
        )

        val result = IhcExtractor().extract(ihcTests)
        assertThat(result).isEqualTo(
            listOf(
                IhcExtraction(date1, setOf(POSITIVE_FUSION_GENE), setOf(NEGATIVE_FUSION_GENE)),
                IhcExtraction(date2, setOf(POSITIVE_FUSION_GENE), emptySet())
            )
        )
    }

    private fun positiveIhc(gene: String, date: LocalDate? = null): IhcTest {
        return IhcTest(item = gene, measureDate = date, scoreText = "Positive")
    }

    private fun negativeIhc(gene: String, date: LocalDate? = null): IhcTest {
        return IhcTest(item = gene, measureDate = date, scoreText = "Negative")
    }
}
