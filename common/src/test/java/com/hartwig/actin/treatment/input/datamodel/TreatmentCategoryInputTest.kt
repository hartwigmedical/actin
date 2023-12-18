package com.hartwig.actin.treatment.input.datamodel

import com.hartwig.actin.clinical.datamodel.treatment.DrugType
import com.hartwig.actin.clinical.datamodel.treatment.OtherTreatmentType
import com.hartwig.actin.clinical.datamodel.treatment.RadiotherapyType
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType
import org.junit.Test
import java.util.*

class TreatmentCategoryInputTest {
    @Test
    fun shouldCreateForAllCategories() {
        for (category in TreatmentCategory.values()) {
            assertThat(TreatmentCategoryInput.fromString(category.display()).mappedCategory()).isEqualTo(category)
        }
    }

    @Test
    fun shouldCreateForAllDrugTypes() {
        assertCreationFromEnumStrings(DrugType.values())
    }

    @Test
    fun shouldCreateForAllRadiotherapyTypes() {
        assertCreationFromEnumStrings(RadiotherapyType.values())
    }

    @Test
    fun shouldCreateForAllOtherTreatmentTypes() {
        assertCreationFromEnumStrings(OtherTreatmentType.values())
    }

    companion object {
        private fun assertCreationFromEnumStrings(values: Array<TreatmentType>) {
            for (treatmentType in values) {
                val input = treatmentType.toString().replace("_", " ").lowercase(Locale.getDefault())
                val treatmentCategoryInput = TreatmentCategoryInput.fromString(input)
                assertThat(treatmentCategoryInput.mappedType()).isEqualTo(treatmentType)
                assertThat(treatmentCategoryInput.mappedCategory()).isEqualTo(treatmentType.category())
                assertThat(TreatmentCategoryInput.treatmentTypeFromString(input)).isEqualTo(treatmentType)
            }
        }
    }
}