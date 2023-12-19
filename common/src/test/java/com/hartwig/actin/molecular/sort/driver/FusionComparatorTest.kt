package com.hartwig.actin.molecular.sort.driver

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.Fusion
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import org.junit.Assert
import org.junit.Test

class FusionComparatorTest {
    @Test
    fun canSortFusions() {
        val fusion1 = create(DriverLikelihood.HIGH, "EML4", "ALK")
        val fusion2 = create(DriverLikelihood.LOW, "APC", "NTRK2")
        val fusion3 = create(DriverLikelihood.LOW, "APC", "NTRK3")
        val fusion4 = create(DriverLikelihood.LOW, "EML4", "ALK")
        val fusions: List<Fusion> = Lists.newArrayList(fusion3, fusion2, fusion4, fusion1)
        fusions.sort(FusionComparator())
        Assert.assertEquals(fusion1, fusions[0])
        Assert.assertEquals(fusion2, fusions[1])
        Assert.assertEquals(fusion3, fusions[2])
        Assert.assertEquals(fusion4, fusions[3])
    }

    companion object {
        private fun create(driverLikelihood: DriverLikelihood, geneStart: String, geneEnd: String): Fusion {
            return TestFusionFactory.builder()
                .driverLikelihood(driverLikelihood)
                .geneStart(geneStart)
                .geneEnd(geneEnd)
                .build()
        }
    }
}