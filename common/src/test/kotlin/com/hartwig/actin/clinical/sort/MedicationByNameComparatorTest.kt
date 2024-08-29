package com.hartwig.actin.clinical.sort

import com.hartwig.actin.datamodel.clinical.Medication
import com.hartwig.actin.datamodel.clinical.TestMedicationFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MedicationByNameComparatorTest {

    @Test
    fun `Should sort medications`() {
        val medication1 = medication("X")
        val medication2 = medication("X")
        val medication3 = medication("Z")
        val medication4 = medication("Y")
        val values = listOf(medication1, medication2, medication3, medication4).sortedWith(MedicationByNameComparator())

        assertThat(values).containsExactly(medication1, medication2, medication4, medication3)
    }

    private fun medication(name: String): Medication {
        return TestMedicationFactory.createMinimal().copy(name = name)
    }
}