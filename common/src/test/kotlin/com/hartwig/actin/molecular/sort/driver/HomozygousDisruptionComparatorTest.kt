package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import com.hartwig.actin.molecular.datamodel.orange.driver.HomozygousDisruption
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HomozygousDisruptionComparatorTest {

    @Test
    fun `Should sort homozygous disruptions`() {
        val homozygousDisruption1 = create("APC", DriverLikelihood.HIGH)
        val homozygousDisruption2 = create("NF1", DriverLikelihood.HIGH)
        val homozygousDisruption3 = create("APC", DriverLikelihood.LOW)
        val disruptions = listOf(homozygousDisruption2, homozygousDisruption1, homozygousDisruption3)
            .sortedWith(HomozygousDisruptionComparator())

        assertThat(disruptions[0]).isEqualTo(homozygousDisruption1)
        assertThat(disruptions[1]).isEqualTo(homozygousDisruption2)
        assertThat(disruptions[2]).isEqualTo(homozygousDisruption3)
    }

    private fun create(gene: String, driverLikelihood: DriverLikelihood): HomozygousDisruption {
        return TestHomozygousDisruptionFactory.createMinimal().copy(driverLikelihood = driverLikelihood, gene = gene)
    }
}