package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val POSITIVE_FUSION_GENE = "ALK"
private const val NEGATIVE_FUSION_GENE = "ROS1"
private const val OTHER_GENE = "GENE"

class IHCExtractorTest {
    @Test
    fun `Should extract fusion positives from IHC`() {
        val priorIHCTests = listOf(positiveIHC(POSITIVE_FUSION_GENE))

        val result = IHCExtractor().extract(priorIHCTests)
        assertThat(result).isEqualTo(listOf(IHCExtraction(null, setOf(POSITIVE_FUSION_GENE), emptySet())))
    }

    @Test
    fun `Should extract fusion negatives from IHC`() {
        val priorIHCTests = listOf(negativeIHC(NEGATIVE_FUSION_GENE))

        val result = IHCExtractor().extract(priorIHCTests)
        assertThat(result).isEqualTo(listOf(IHCExtraction(null, emptySet(), setOf(NEGATIVE_FUSION_GENE))))
    }


    @Test
    fun `Should ignore other genes`() {
        val priorIHCTests = listOf(positiveIHC(OTHER_GENE))

        val result = IHCExtractor().extract(priorIHCTests)
        assertThat(result).isEmpty()
    }

    @Test
    fun `Should group IHC tests by date`() {
        val date1 = LocalDate.of(2023, 1, 1)
        val date2 = LocalDate.of(2023, 2, 1)
        val priorIHCTests = listOf(
            positiveIHC(POSITIVE_FUSION_GENE, date1),
            negativeIHC(NEGATIVE_FUSION_GENE, date1),
            positiveIHC(POSITIVE_FUSION_GENE, date2),
            positiveIHC(OTHER_GENE, date2)
        )

        val result = IHCExtractor().extract(priorIHCTests)
        assertThat(result).isEqualTo(
            listOf(
                IHCExtraction(date1, setOf(POSITIVE_FUSION_GENE), setOf(NEGATIVE_FUSION_GENE)),
                IHCExtraction(date2, setOf(POSITIVE_FUSION_GENE), emptySet())
            )
        )
    }

    private fun positiveIHC(gene: String, date: LocalDate? = null): PriorIHCTest {
        return PriorIHCTest(test = "IHC", item = gene, measureDate = date, scoreText = "Positive")
    }

    private fun negativeIHC(gene: String, date: LocalDate? = null): PriorIHCTest {
        return PriorIHCTest(test = "IHC", item = gene, measureDate = date, scoreText = "Negative")
    }
}
