package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.Fusion
import com.hartwig.actin.molecular.datamodel.driver.TestFusionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtendedFusionComparatorTest {

    @Test
    fun `Should sort fusions`() {
        val fusion1 = create(DriverLikelihood.HIGH, "EML4", "ALK")
        val fusion2 = create(DriverLikelihood.LOW, "APC", "NTRK2")
        val fusion3 = create(DriverLikelihood.LOW, "APC", "NTRK3")
        val fusion4 = create(DriverLikelihood.LOW, "EML4", "ALK")
        val fusions = listOf(fusion3, fusion2, fusion4, fusion1).sortedWith(FusionComparator())

        assertThat(fusions[0]).isEqualTo(fusion1)
        assertThat(fusions[1]).isEqualTo(fusion2)
        assertThat(fusions[2]).isEqualTo(fusion3)
        assertThat(fusions[3]).isEqualTo(fusion4)
    }

    private fun create(driverLikelihood: DriverLikelihood, geneStart: String, geneEnd: String): Fusion {
        return TestFusionFactory.createMinimal().copy(
            driverLikelihood = driverLikelihood,
            geneStart = geneStart,
            geneEnd = geneEnd
        )
    }
}