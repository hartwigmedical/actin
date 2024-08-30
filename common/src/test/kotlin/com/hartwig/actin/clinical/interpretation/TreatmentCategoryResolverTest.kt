package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver.fromStringList
import com.hartwig.actin.clinical.interpretation.TreatmentCategoryResolver.toStringList
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentCategoryResolverTest {

    @Test
    fun `Should convert treatment categories back and forth`() {
        for (category in TreatmentCategory.values()) {
            val set = setOf(category)
            assertThat(fromStringList(toStringList(set))).isEqualTo(set)
        }
    }

    @Test
    fun `Should convert categories to strings`() {
        assertThat(toStringList(emptySet())).isEqualTo("")
        assertThat(toStringList(setOf(TreatmentCategory.CHEMOTHERAPY))).isEqualTo("Chemotherapy")
        assertThat(toStringList(setOf(TreatmentCategory.TARGETED_THERAPY))).isEqualTo("Targeted therapy")

        val categories = sortedSetOf(TreatmentCategory.CHEMOTHERAPY, TreatmentCategory.RADIOTHERAPY)
        assertThat(toStringList(categories)).isEqualTo("Chemotherapy, Radiotherapy")
    }

    @Test
    fun `Should convert strings to categories`() {
        assertThat(fromStringList("Targeted therapy")).isEqualTo(setOf(TreatmentCategory.TARGETED_THERAPY))
    }
}