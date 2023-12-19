package com.hartwig.actin.molecular.sort.driver

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.Disruption
import com.hartwig.actin.molecular.datamodel.driver.DisruptionType
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.TestDisruptionFactory
import org.junit.Assert
import org.junit.Test

class DisruptionComparatorTest {
    @Test
    fun canSortDisruptions() {
        val disruption1 = create("NF1", DriverLikelihood.HIGH, DisruptionType.DEL, 2.0, 1.0)
        val disruption2 = create("APC", DriverLikelihood.LOW, DisruptionType.BND, 2.0, 1.0)
        val disruption3 = create("NF1", DriverLikelihood.LOW, DisruptionType.DUP, 2.0, 1.0)
        val disruption4 = create("NF1", DriverLikelihood.LOW, DisruptionType.DUP, 1.0, 0.0)
        val disruption5 = create("NF1", DriverLikelihood.LOW, DisruptionType.DUP, 1.0, 1.0)
        val disruptions: List<Disruption> = Lists.newArrayList(disruption3, disruption5, disruption4, disruption2, disruption1)
        disruptions.sort(DisruptionComparator())
        Assert.assertEquals(disruption1, disruptions[0])
        Assert.assertEquals(disruption2, disruptions[1])
        Assert.assertEquals(disruption3, disruptions[2])
        Assert.assertEquals(disruption4, disruptions[3])
        Assert.assertEquals(disruption5, disruptions[4])
    }

    companion object {
        private fun create(
            gene: String, driverLikelihood: DriverLikelihood?, type: DisruptionType,
            junctionCopyNumber: Double, undisruptedCopyNumber: Double
        ): Disruption {
            return TestDisruptionFactory.builder()
                .gene(gene)
                .driverLikelihood(driverLikelihood)
                .type(type)
                .junctionCopyNumber(junctionCopyNumber)
                .undisruptedCopyNumber(undisruptedCopyNumber)
                .build()
        }
    }
}