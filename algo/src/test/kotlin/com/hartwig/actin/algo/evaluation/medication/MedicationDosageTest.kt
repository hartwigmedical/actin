package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.medication.MedicationDosage.hasMatchingDosing
import com.hartwig.actin.datamodel.clinical.Dosage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MedicationDosageTest {

    @Test
    fun `Should accurately determine dosage stability`() {
        val dosing1 = Dosage(
            dosageMin = 1.0,
            dosageMax = 2.0,
            dosageUnit = "unit 1",
            frequency = 3.0,
            frequencyUnit = "unit 2",
            ifNeeded = false
        )
        val dosing2 = Dosage(
            dosageMin = 2.0,
            dosageMax = 3.0,
            dosageUnit = "unit 2",
            frequency = 4.0,
            frequencyUnit = "unit 1",
            ifNeeded = false
        )
        assertThat(hasMatchingDosing(dosing1, dosing1)).isTrue
        assertThat(hasMatchingDosing(dosing2, dosing2)).isTrue
        assertThat(hasMatchingDosing(dosing1, dosing2)).isFalse
        assertThat(hasMatchingDosing(dosing1, Dosage())).isFalse
    }
}