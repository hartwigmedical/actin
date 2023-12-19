package com.hartwig.actin.molecular.sort.driver

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.TestTranscriptImpactFactory
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact
import com.hartwig.actin.molecular.datamodel.driver.Variant
import org.junit.Assert
import org.junit.Test

class VariantComparatorTest {
    @Test
    fun canSortVariants() {
        val variant1 = create(DriverLikelihood.HIGH, "BRAF", "V600E", "1800")
        val variant2 = create(DriverLikelihood.HIGH, "BRAF", "V600E", "1801")
        val variant3 = create(DriverLikelihood.HIGH, "BRAF", "V601E", "1800")
        val variant4 = create(DriverLikelihood.HIGH, "NTRK", "V601E", "1800")
        val variant5 = create(DriverLikelihood.MEDIUM, "BRAF", "V600E", "1800")
        val variants: List<Variant> = Lists.newArrayList(variant3, variant5, variant1, variant4, variant2)
        variants.sort(VariantComparator())
        Assert.assertEquals(variant1, variants[0])
        Assert.assertEquals(variant2, variants[1])
        Assert.assertEquals(variant3, variants[2])
        Assert.assertEquals(variant4, variants[3])
        Assert.assertEquals(variant5, variants[4])
    }

    companion object {
        private fun create(
            driverLikelihood: DriverLikelihood, gene: String, hgvsProteinImpact: String,
            hgvsCodingImpact: String
        ): Variant {
            val canonicalImpact: TranscriptImpact =
                TestTranscriptImpactFactory.builder().hgvsProteinImpact(hgvsProteinImpact).hgvsCodingImpact(hgvsCodingImpact).build()
            return TestVariantFactory.builder().driverLikelihood(driverLikelihood).gene(gene).canonicalImpact(canonicalImpact).build()
        }
    }
}