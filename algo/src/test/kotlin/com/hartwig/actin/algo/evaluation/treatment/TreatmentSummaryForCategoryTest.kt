package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.drugTreatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatment
import com.hartwig.actin.datamodel.clinical.TreatmentTestFactory.treatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TreatmentSummaryForCategoryTest {

    @Test
    fun `Should not report matches for empty summary`() {
        val summary = TreatmentSummaryForCategory()
        assertThat(summary.numSpecificMatches()).isEqualTo(0)
        assertThat(summary.hasSpecificMatch()).isFalse
        assertThat(summary.numApproximateMatches).isEqualTo(0)
        assertThat(summary.hasApproximateMatch()).isFalse
        assertThat(summary.numPossibleTrialMatches).isEqualTo(0)
        assertThat(summary.hasPossibleTrialMatch()).isFalse
    }

    @Test
    fun `Should report specific matches`() {
        assertThat(TreatmentSummaryForCategory(specificMatches = listOf(treatmentHistoryEntry())).hasSpecificMatch()).isTrue
    }

    @Test
    fun `Should report approximate matches`() {
        assertThat(TreatmentSummaryForCategory(numApproximateMatches = 1).hasApproximateMatch()).isTrue
    }

    @Test
    fun `Should report possible trial matches`() {
        assertThat(TreatmentSummaryForCategory(numPossibleTrialMatches = 1).hasPossibleTrialMatch()).isTrue
    }

    @Test
    fun `Should add summaries together`() {
        val treatmentHistoryEntry1 = treatmentHistoryEntry(setOf(treatment("1", true)))
        val treatmentHistoryEntry2 = treatmentHistoryEntry(setOf(treatment("2", false)))
        val summary1 = TreatmentSummaryForCategory(listOf(treatmentHistoryEntry1), 2, 3)
        val summary2 = TreatmentSummaryForCategory(listOf(treatmentHistoryEntry2), 5, 6)
        assertThat(summary1 + summary2).isEqualTo(TreatmentSummaryForCategory(listOf(treatmentHistoryEntry1, treatmentHistoryEntry2), 7, 9))
    }

    @Test
    fun `Should create empty summary for empty treatment list`() {
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(emptyList(), CATEGORY_TO_MATCH))
            .isEqualTo(TreatmentSummaryForCategory())
    }

    @Test
    fun `Should count treatments matching category`() {
        val treatments = listOf(TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY, treatmentHistoryEntryWithCategory(TreatmentCategory.SURGERY))
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, CATEGORY_TO_MATCH))
            .isEqualTo(TreatmentSummaryForCategory(listOf(TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY), 0, 0))
    }

    @Test
    fun `Should collect treatments matching category and custom classification`() {
        val treatments = listOf(
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY,
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_TYPE,
            treatmentHistoryEntryWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, CATEGORY_TO_MATCH, { it.isOfType(TYPE_TO_MATCH) }))
            .isEqualTo(TreatmentSummaryForCategory(listOf(TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_TYPE), 1, 0))
    }

    @Test
    fun `Should count treatments matching category and partial match to custom classification`() {
        val treatments = listOf(
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY,
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_TYPE,
            treatmentHistoryEntryWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(
            treatments, CATEGORY_TO_MATCH, { if (it.isOfType(TYPE_TO_MATCH) == true) null else false }
        )).isEqualTo(TreatmentSummaryForCategory(emptyList(), 1, 0))
    }

    @Test
    fun `Should count trial treatments ignoring custom classification`() {
        val treatments = listOf(
            treatmentHistoryEntry(setOf(treatment("", true)), isTrial = true),
            treatmentHistoryEntryWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, CATEGORY_TO_MATCH, { it.isOfType(TYPE_TO_MATCH) }))
            .isEqualTo(TreatmentSummaryForCategory(emptyList(), 0, 1))
    }

    @Test
    fun `Should accumulate match counts for multiple treatments`() {
        val treatments = listOf(
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY,
            TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_TYPE,
            treatmentHistoryEntry(setOf(treatment("", true)), isTrial = true),
            treatmentHistoryEntryWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, CATEGORY_TO_MATCH))
            .isEqualTo(
                TreatmentSummaryForCategory(
                    listOf(TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY, TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_TYPE),
                    0,
                    1
                )
            )
    }

    @Test
    fun `Should not count trial matches when looking for unlikely trial categories`() {
        val treatments = listOf(
            treatmentHistoryEntry(setOf(treatment("", true)), isTrial = true),
            treatmentHistoryEntryWithCategory(TreatmentCategory.SURGERY)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, TreatmentCategory.TRANSPLANTATION))
            .isEqualTo(TreatmentSummaryForCategory(emptyList(), 0, 0))
    }

    @Test
    fun `Should not count trial treatments with assigned type(s)`() {
        val treatments = listOf(
            treatmentHistoryEntry(setOf(treatment("", true, types = setOf(OtherTreatmentType.RADIOFREQUENCY))), isTrial = true)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(treatments, CATEGORY_TO_MATCH, { it.isOfType(TYPE_TO_MATCH) }))
            .isEqualTo(TreatmentSummaryForCategory(emptyList(), 0, 0))
    }

    @Test
    fun `Should not count trial treatments that are not eligible to match as trials using custom criteria`() {
        val treatments = listOf(
            treatmentHistoryEntry(setOf(treatment("EXCLUDE", true)), isTrial = true),
            treatmentHistoryEntry(setOf(treatment("", true, types = setOf(OtherTreatmentType.RADIOFREQUENCY))), isTrial = true),
            treatmentHistoryEntry(setOf(treatment("", true, types = setOf(OtherTreatmentType.RADIOFREQUENCY))), isTrial = true)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(
            treatments,
            CATEGORY_TO_MATCH,
            classifier = { it.isOfType(TYPE_TO_MATCH) },
            treatmentEligibleToMatchTrials = { it.name != "EXCLUDE" }
        )).isEqualTo(TreatmentSummaryForCategory(emptyList(), 0, 2))
    }

    @Test
    fun `Should not count trial treatment entries that are not eligible to match as trials using custom criteria`() {
        val treatments = listOf(
            treatmentHistoryEntry(setOf(treatment("", true)), isTrial = true, intents = setOf(Intent.PALLIATIVE)),
            treatmentHistoryEntry(setOf(treatment("", true, types = setOf(OtherTreatmentType.RADIOFREQUENCY))), isTrial = true),
            treatmentHistoryEntry(setOf(treatment("", true, types = setOf(OtherTreatmentType.RADIOFREQUENCY))), isTrial = true)
        )
        assertThat(TreatmentSummaryForCategory.createForTreatmentHistory(
            treatments,
            CATEGORY_TO_MATCH,
            classifier = { it.intents?.contains(Intent.ADJUVANT) },
            treatmentEligibleToMatchTrials = { true },
            treatmentHistoryEntryMayMatchTrials = { it.intents.isNullOrEmpty() }
        )).isEqualTo(TreatmentSummaryForCategory(emptyList(), 0, 2))
    }

    companion object {
        private val CATEGORY_TO_MATCH = TreatmentCategory.CHEMOTHERAPY
        private val TYPE_TO_MATCH = DrugType.ANTHRACYCLINE
        private val TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY = treatmentHistoryEntryWithCategory(CATEGORY_TO_MATCH)
        private val TREATMENT_HISTORY_ENTRY_MATCHING_CATEGORY_AND_TYPE = treatmentHistoryEntryWithCategory(CATEGORY_TO_MATCH, TYPE_TO_MATCH)

        private fun treatmentHistoryEntryWithCategory(
            category: TreatmentCategory, type: DrugType? = null, isTrial: Boolean = false
        ) = treatmentHistoryEntry(setOf(drugTreatment("", category, setOfNotNull(type))), isTrial = isTrial)
    }
}