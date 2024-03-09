package com.hartwig.actin.trial.input.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.RadiotherapyType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentCategoryInputTest {

    @Test
    fun `Should create for all categories`() {
        for (category in TreatmentCategory.values()) {
            assertThat(TreatmentCategoryInput.fromString(category.display()).mappedCategory).isEqualTo(category)
        }
    }

    @Test
    fun `Should create for all drug types`() {
        assertCreationFromEnumStrings(DrugType.values())
    }

    @Test
    fun `Should create for all radiotherapy types`() {
        assertCreationFromEnumStrings(RadiotherapyType.values())
    }

    @Test
    fun `Should create for all other treatment types`() {
        assertCreationFromEnumStrings(OtherTreatmentType.values())
    }

    private fun assertCreationFromEnumStrings(values: Array<out TreatmentType>) {
        for (treatmentType in values) {
            val input = treatmentType.toString().replace("_", " ").lowercase()
            val treatmentCategoryInput = TreatmentCategoryInput.fromString(input)
            assertThat(treatmentCategoryInput.mappedType).isEqualTo(treatmentType)
            assertThat(treatmentCategoryInput.mappedCategory).isEqualTo(treatmentType.category)
            assertThat(TreatmentCategoryInput.treatmentTypeFromString(input)).isEqualTo(treatmentType)
        }
    }
}