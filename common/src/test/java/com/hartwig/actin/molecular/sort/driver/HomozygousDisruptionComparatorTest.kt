package com.hartwig.actin.molecular.sort.driver

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory
import org.junit.Assert
import org.junit.Test

class HomozygousDisruptionComparatorTest {
    @Test
    fun canSortHomozygousDisruptions() {
        val homozygousDisruption1 = create("APC", DriverLikelihood.HIGH)
        val homozygousDisruption2 = create("NF1", DriverLikelihood.HIGH)
        val homozygousDisruption3 = create("APC", DriverLikelihood.LOW)
        val disruptions: List<HomozygousDisruption> =
            Lists.newArrayList(homozygousDisruption2, homozygousDisruption1, homozygousDisruption3)
        disruptions.sort(HomozygousDisruptionComparator())
        Assert.assertEquals(homozygousDisruption1, disruptions[0])
        Assert.assertEquals(homozygousDisruption2, disruptions[1])
        Assert.assertEquals(homozygousDisruption3, disruptions[2])
    }

    companion object {
        private fun create(gene: String, driverLikelihood: DriverLikelihood): HomozygousDisruption {
            return TestHomozygousDisruptionFactory.builder().driverLikelihood(driverLikelihood).gene(gene).build()
        }
    }
}