package com.hartwig.actin.trial.input.datamodel

import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.RadiotherapyType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import kotlin.enums.EnumEntries
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class TreatmentCategoryInputTest {

    @Test
    fun `Should create for all categories`() {
        for (category in TreatmentCategory.entries) {
            assertThat(TreatmentCategoryInput.fromString(category.display()).mappedCategory).isEqualTo(category)
        }
    }

    @Test
    fun `Should create for all drug types`() {
        assertCreationFromEnumStrings(DrugType.entries)
    }

    @Test
    fun `Should create for all radiotherapy types`() {
        assertCreationFromEnumStrings(RadiotherapyType.entries)
    }

    @Test
    fun `Should create for all other treatment types`() {
        assertCreationFromEnumStrings(OtherTreatmentType.entries)
    }

    private fun <T> assertCreationFromEnumStrings(entries: EnumEntries<T>) where T: Enum<T>, T : TreatmentType {
        for (treatmentType in entries) {
            val input = treatmentType.toString().replace("_", " ").lowercase()
            val treatmentCategoryInput = TreatmentCategoryInput.fromString(input)
            assertThat(treatmentCategoryInput.mappedType).isEqualTo(treatmentType)
            assertThat(treatmentCategoryInput.mappedCategory).isEqualTo(treatmentType.category)
            assertThat(TreatmentCategoryInput.treatmentTypeFromString(input)).isEqualTo(treatmentType)
        }
    }
}