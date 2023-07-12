package com.hartwig.actin.clinical.feed.medication

import com.hartwig.actin.clinical.curation.*
import com.hartwig.actin.clinical.feed.*
import com.hartwig.actin.clinical.feed.medication.MedicationEntryCreator.Companion.isActive
import org.apache.logging.log4j.util.*
import org.assertj.core.api.Assertions.*
import org.junit.*

private const val INCORRECT_VALUE = "incorrect"

class MedicationEntryCreatorTest {
    @Test
    fun shouldInterpretIfMedicationIsActive() {
        assertThat(isActive("stopped")!!).isFalse()
        assertThat(isActive("active")!!).isTrue()
        assertThat(isActive(Strings.EMPTY)).isNull()
    }

    @Test
    fun shouldPassValidityCheckIfAtcClassificationMatches() {
        assertThat(
            isValid(
                arrayOf(
                    METAFORMIN_ATC_CODE,
                    METFORMIN,
                    BIGUANIDES,
                    BLOOD_GLUCOSE_LOWERING_DRUGS_EXCL_INSULINS,
                    DRUGS_USED_IN_DIABETES,
                    ALIMENTARY_TRACT_AND_METABOLISM,
                )
            )
        ).isTrue()
    }

    @Test
    fun shouldFailValidityCheckIfAtcClassificationMatches() {

        val permutations = listOf(
            arrayOf(
                METAFORMIN_ATC_CODE,
                METFORMIN,
                BIGUANIDES,
                BLOOD_GLUCOSE_LOWERING_DRUGS_EXCL_INSULINS,
                DRUGS_USED_IN_DIABETES,
                INCORRECT_VALUE,
            ),
            arrayOf(
                METAFORMIN_ATC_CODE,
                METFORMIN,
                BIGUANIDES,
                BLOOD_GLUCOSE_LOWERING_DRUGS_EXCL_INSULINS,
                INCORRECT_VALUE,
                ALIMENTARY_TRACT_AND_METABOLISM,
            ),
            arrayOf(
                METAFORMIN_ATC_CODE,
                METFORMIN,
                BIGUANIDES,
                INCORRECT_VALUE,
                DRUGS_USED_IN_DIABETES,
                ALIMENTARY_TRACT_AND_METABOLISM,
            ),
            arrayOf(
                METAFORMIN_ATC_CODE,
                METFORMIN,
                INCORRECT_VALUE,
                BLOOD_GLUCOSE_LOWERING_DRUGS_EXCL_INSULINS,
                DRUGS_USED_IN_DIABETES,
                ALIMENTARY_TRACT_AND_METABOLISM,
            ),
            arrayOf(
                METAFORMIN_ATC_CODE,
                INCORRECT_VALUE,
                BIGUANIDES,
                BLOOD_GLUCOSE_LOWERING_DRUGS_EXCL_INSULINS,
                DRUGS_USED_IN_DIABETES,
                ALIMENTARY_TRACT_AND_METABOLISM,
            ),
        )
        permutations.forEach { assertThat(isValid(it)).isFalse() }
    }

    private fun isValid(values: Array<String>): Boolean {
        return MedicationEntryCreator(TestAtcFactory.createMinimalModel()).isValid(FeedLine(fields(), values))
    }

    private fun fields() = mapOf(
        "code5_ATC_code" to 0,
        "code5_ATC_display" to 1,
        "chemical_subgroup_display" to 2,
        "pharmacological_subgroup_display" to 3,
        "therapeutic_subgroup_display" to 4,
        "anatomical_main_group_display" to 5
    )
}