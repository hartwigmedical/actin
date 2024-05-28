package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.hmf.driver.ExhaustiveVariant
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExhaustiveVariantComparatorTest {

    @Test
    fun `Should sort variants`() {
        val variant1 = create(DriverLikelihood.HIGH, "BRAF", "V600E", "1800")
        val variant2 = create(DriverLikelihood.HIGH, "BRAF", "V600E", "1801")
        val variant3 = create(DriverLikelihood.HIGH, "BRAF", "V601E", "1800")
        val variant4 = create(DriverLikelihood.HIGH, "NTRK", "V601E", "1800")
        val variant5 = create(DriverLikelihood.MEDIUM, "BRAF", "V600E", "1800")
        val variants = listOf(variant3, variant5, variant1, variant4, variant2).sortedWith(VariantComparator())

        assertThat(variants[0]).isEqualTo(variant1)
        assertThat(variants[1]).isEqualTo(variant2)
        assertThat(variants[2]).isEqualTo(variant3)
        assertThat(variants[3]).isEqualTo(variant4)
        assertThat(variants[4]).isEqualTo(variant5)
    }

    private fun create(
        driverLikelihood: DriverLikelihood, gene: String, hgvsProteinImpact: String,
        hgvsCodingImpact: String
    ): ExhaustiveVariant {
        val canonicalImpact: TranscriptImpact = TestTranscriptImpactFactory.createMinimal().copy(
            hgvsProteinImpact = hgvsProteinImpact,
            hgvsCodingImpact = hgvsCodingImpact
        )
        return TestVariantFactory.createMinimal().copy(
            driverLikelihood = driverLikelihood,
            gene = gene,
            canonicalImpact = canonicalImpact
        )
    }
}