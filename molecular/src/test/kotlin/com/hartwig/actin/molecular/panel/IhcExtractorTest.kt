package com.hartwig.actin.molecular.panel

import com.hartwig.actin.datamodel.clinical.IhcTest
import com.hartwig.actin.molecular.util.GeneConstants
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

private val FUSION_TESTED_GENE = GeneConstants.IHC_FUSION_EVALUABLE_GENES.first()
private val OTHER_FUSION_TESTED_GENE = GeneConstants.IHC_FUSION_EVALUABLE_GENES.last()
private val DELETION_OR_MUTATION_TESTED_GENE = GeneConstants.IHC_LOSS_EVALUABLE_GENES.first()
private const val OTHER_GENE = "GENE"

class IhcExtractorTest {

    @Test
    fun `Should extract specific fusion tested genes from IHC`() {
        val ihcTests = listOf(positiveIhc(FUSION_TESTED_GENE))
        val result = IhcExtractor().extract(ihcTests)
        assertThat(result).isEqualTo(
            listOf(
                IhcExtraction(
                    null,
                    setOf(FUSION_TESTED_GENE),
                    emptySet(),
                )
            )
        )
    }

    @Test
    fun `Should extract specific mutation or deletion tested genes from IHC`() {
        val ihcTests = listOf(anyIhc(DELETION_OR_MUTATION_TESTED_GENE, false))
        val result = IhcExtractor().extract(ihcTests)
        assertThat(result).isEqualTo(
            listOf(
                IhcExtraction(
                    null,
                    emptySet(),
                    setOf(DELETION_OR_MUTATION_TESTED_GENE)
                )
            )
        )
    }

    @Test
    fun `Should not extract tested genes from IHC if indeterminate result`() {
        val ihcTests = listOf(negativeIhc(FUSION_TESTED_GENE, true))
        val result = IhcExtractor().extract(ihcTests)
        assertThat(result).isEmpty()
    }

    @Test
    fun `Should ignore other genes`() {
        val ihcTests = listOf(positiveIhc(OTHER_GENE), negativeIhc(OTHER_GENE, false))
        val result = IhcExtractor().extract(ihcTests)
        assertThat(result).isEmpty()
    }

    @Test
    fun `Should group IHC tests by date`() {
        val date1 = LocalDate.of(2023, 1, 1)
        val date2 = LocalDate.of(2023, 2, 1)
        val ihcTests = listOf(
            positiveIhc(FUSION_TESTED_GENE, date1),
            negativeIhc(OTHER_FUSION_TESTED_GENE, false, date1),
            positiveIhc(FUSION_TESTED_GENE, date2),
            positiveIhc(DELETION_OR_MUTATION_TESTED_GENE, date2),
            positiveIhc(OTHER_GENE, date2)
        )

        val result = IhcExtractor().extract(ihcTests)
        assertThat(result).isEqualTo(
            listOf(
                IhcExtraction(date1, setOf(FUSION_TESTED_GENE, OTHER_FUSION_TESTED_GENE), emptySet()),
                IhcExtraction(date2, setOf(FUSION_TESTED_GENE), setOf(DELETION_OR_MUTATION_TESTED_GENE))
            )
        )
    }

    private fun positiveIhc(gene: String, date: LocalDate? = null): IhcTest {
        return IhcTest(item = gene, measureDate = date, scoreText = "Positive")
    }

    private fun negativeIhc(gene: String, indeterminate: Boolean, date: LocalDate? = null): IhcTest {
        return IhcTest(item = gene, measureDate = date, scoreText = "Negative", impliesPotentialIndeterminateStatus = indeterminate)
    }

    private fun anyIhc(gene: String, indeterminate: Boolean, date: LocalDate? = null): IhcTest {
        return IhcTest(item = gene, measureDate = date, scoreText = "", impliesPotentialIndeterminateStatus = indeterminate)
    }
}
