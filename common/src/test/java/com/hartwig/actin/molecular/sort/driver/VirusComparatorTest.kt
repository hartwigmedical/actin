package com.hartwig.actin.molecular.sort.driver

import com.google.common.collect.Lists
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.TestVirusFactory
import com.hartwig.actin.molecular.datamodel.driver.Virus
import com.hartwig.actin.molecular.datamodel.driver.VirusType
import org.junit.Assert
import org.junit.Test

class VirusComparatorTest {
    @Test
    fun canSortViruses() {
        val virus1 = create(DriverLikelihood.HIGH, "Human 16", VirusType.HUMAN_PAPILLOMA_VIRUS)
        val virus2 = create(DriverLikelihood.LOW, "Epstein 1", VirusType.EPSTEIN_BARR_VIRUS)
        val virus3 = create(DriverLikelihood.LOW, "Human 1", VirusType.HUMAN_PAPILLOMA_VIRUS)
        val virus4 = create(DriverLikelihood.LOW, "Human 2", VirusType.HUMAN_PAPILLOMA_VIRUS)
        val viruses: List<Virus> = Lists.newArrayList(virus2, virus4, virus1, virus3)
        viruses.sort(VirusComparator())
        Assert.assertEquals(virus1, viruses[0])
        Assert.assertEquals(virus2, viruses[1])
        Assert.assertEquals(virus3, viruses[2])
        Assert.assertEquals(virus4, viruses[3])
    }

    companion object {
        private fun create(driverLikelihood: DriverLikelihood, name: String, type: VirusType): Virus {
            return TestVirusFactory.builder().driverLikelihood(driverLikelihood).name(name).type(type).build()
        }
    }
}