package com.hartwig.actin.datamodel.molecular.driver

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VirusTest {

    @Test
    fun `Should sort viruses`() {
        val virus1 = create(DriverLikelihood.HIGH, "Human 16", VirusType.HPV)
        val virus2 = create(DriverLikelihood.LOW, "Epstein 1", VirusType.EBV)
        val virus3 = create(DriverLikelihood.LOW, "Human 1", VirusType.HPV)
        val virus4 = create(DriverLikelihood.LOW, "Human 2", VirusType.HPV)

        val viruses = listOf(virus2, virus4, virus1, virus3).sorted()

        assertThat(viruses[0]).isEqualTo(virus1)
        assertThat(viruses[1]).isEqualTo(virus2)
        assertThat(viruses[2]).isEqualTo(virus3)
        assertThat(viruses[3]).isEqualTo(virus4)
    }

    private fun create(driverLikelihood: DriverLikelihood, name: String, type: VirusType): Virus {
        return TestVirusFactory.createMinimal().copy(
            driverLikelihood = driverLikelihood,
            name = name,
            type = type
        )
    }
}