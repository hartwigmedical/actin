package com.hartwig.actin.datamodel.molecular.driver

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FusionTest {

    @Test
    fun `Should sort fusions`() {
        val fusion1 = create(DriverLikelihood.HIGH, "EML4", "ALK")
        val fusion2 = create(DriverLikelihood.LOW, "APC", "NTRK2")
        val fusion3 = create(DriverLikelihood.LOW, "APC", "NTRK3")
        val fusion4 = create(DriverLikelihood.LOW, "EML4", "ALK")

        val fusions = listOf(fusion3, fusion2, fusion4, fusion1).sorted()

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