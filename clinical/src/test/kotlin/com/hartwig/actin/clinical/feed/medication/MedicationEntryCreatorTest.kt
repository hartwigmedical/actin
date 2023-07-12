package com.hartwig.actin.clinical.feed.medication

import com.hartwig.actin.clinical.curation.ANATOMICAL
import com.hartwig.actin.clinical.curation.ATC_CODE
import com.hartwig.actin.clinical.curation.CHEMICAL
import com.hartwig.actin.clinical.curation.CHEMICAL_SUBSTANCE
import com.hartwig.actin.clinical.curation.PHARMACOLOGICAL
import com.hartwig.actin.clinical.curation.THERAPEUTIC
import com.hartwig.actin.clinical.curation.TestAtcFactory
import com.hartwig.actin.clinical.feed.FeedLine
import com.hartwig.actin.clinical.feed.medication.MedicationEntryCreator.Companion.isActive
import org.apache.logging.log4j.util.Strings
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


private const val INCORRECT_VALUE = "incorrect"

class MedicationEntryCreatorTest {
    @Test
    fun shouldInterpretIfMedicationIsActive() {
        assertThat(isActive("stopped")!!).isFalse()
        assertThat(isActive("active")!!).isTrue()
        assertThat(isActive(Strings.EMPTY)).isNull()
    }

    @Test
    fun shouldPassValidityCheckForTrialAtcCodes() {
        assertThat(MedicationEntryCreator(TestAtcFactory.createMinimalAtcModel()).isValid(FeedLine(fields(), arrayOf("123")))).isTrue()
    }

    @Test
    fun shouldPassValidityCheckIfAtcClassificationMatches() {
        assertThat(
            isValid(
                arrayOf(
                    ATC_CODE,
                    CHEMICAL_SUBSTANCE,
                    CHEMICAL,
                    PHARMACOLOGICAL,
                    THERAPEUTIC,
                    ANATOMICAL,
                )
            )
        ).isTrue()
    }

    @Test
    fun shouldFailValidityCheckIfAtcClassificationMatches() {

        val permutations = listOf(
            arrayOf(
                ATC_CODE,
                CHEMICAL_SUBSTANCE,
                CHEMICAL,
                PHARMACOLOGICAL,
                THERAPEUTIC,
                INCORRECT_VALUE,
            ),
            arrayOf(
                ATC_CODE,
                CHEMICAL_SUBSTANCE,
                CHEMICAL,
                PHARMACOLOGICAL,
                INCORRECT_VALUE,
                ANATOMICAL,
            ),
            arrayOf(
                ATC_CODE,
                CHEMICAL_SUBSTANCE,
                CHEMICAL,
                INCORRECT_VALUE,
                THERAPEUTIC,
                ANATOMICAL,
            ),
            arrayOf(
                ATC_CODE,
                CHEMICAL_SUBSTANCE,
                INCORRECT_VALUE,
                PHARMACOLOGICAL,
                THERAPEUTIC,
                ANATOMICAL,
            ),
            arrayOf(
                ATC_CODE,
                INCORRECT_VALUE,
                CHEMICAL,
                PHARMACOLOGICAL,
                THERAPEUTIC,
                ANATOMICAL,
            ),
        )
        permutations.forEach { assertThat(isValid(it)).isFalse() }
    }

    private fun isValid(values: Array<String>): Boolean {
        return MedicationEntryCreator(TestAtcFactory.createProperAtcModel()).isValid(FeedLine(fields(), values))
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