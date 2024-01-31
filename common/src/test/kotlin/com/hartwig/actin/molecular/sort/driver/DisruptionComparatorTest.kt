package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DisruptionComparatorTest {

    @Test
    fun `Should sort disruptions`() {
        val disruption1 = create("NF1", DriverLikelihood.HIGH, DisruptionType.DEL, 2.0, 1.0)
        val disruption2 = create("APC", DriverLikelihood.LOW, DisruptionType.BND, 2.0, 1.0)
        val disruption3 = create("NF1", DriverLikelihood.LOW, DisruptionType.DUP, 2.0, 1.0)
        val disruption4 = create("NF1", DriverLikelihood.LOW, DisruptionType.DUP, 1.0, 0.0)
        val disruption5 = create("NF1", DriverLikelihood.LOW, DisruptionType.DUP, 1.0, 1.0)
        val disruptions = listOf(disruption3, disruption5, disruption4, disruption2, disruption1).sortedWith(DisruptionComparator())

        assertThat(disruptions[0]).isEqualTo(disruption1)
        assertThat(disruptions[1]).isEqualTo(disruption2)
        assertThat(disruptions[2]).isEqualTo(disruption3)
        assertThat(disruptions[3]).isEqualTo(disruption4)
        assertThat(disruptions[4]).isEqualTo(disruption5)
    }

    companion object {
        private fun create(
            gene: String, driverLikelihood: DriverLikelihood?, type: DisruptionType,
            junctionCopyNumber: Double, undisruptedCopyNumber: Double
        ): Disruption {
            return TestDisruptionFactory.createMinimal().copy(
                gene = gene,
                driverLikelihood = driverLikelihood,
                type = type,
                junctionCopyNumber = junctionCopyNumber,
                undisruptedCopyNumber = undisruptedCopyNumber
            )
        }
    }
}