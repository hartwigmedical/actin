package com.hartwig.actin.clinical.sort

import com.google.common.collect.Lists
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.TestMedicationFactory
import org.junit.Assert
import org.junit.Test

class MedicationByNameComparatorTest {
    @Test
    fun canSortMedications() {
        val medication1: Medication = TestMedicationFactory.builder().name("X").build()
        val medication2: Medication = TestMedicationFactory.builder().name("X").build()
        val medication3: Medication = TestMedicationFactory.builder().name("Z").build()
        val medication4: Medication = TestMedicationFactory.builder().name("Y").build()
        val values: List<Medication> = Lists.newArrayList(medication1, medication2, medication3, medication4)
        values.sort(MedicationByNameComparator())
        Assert.assertEquals(medication1, values[0])
        Assert.assertEquals(medication2, values[1])
        Assert.assertEquals(medication4, values[2])
        Assert.assertEquals(medication3, values[3])
    }
}